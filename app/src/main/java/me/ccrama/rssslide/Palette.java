package me.ccrama.rssslide;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.util.ArrayList;

import me.ccrama.rssslide.Activities.MainActivity;
import me.ccrama.rssslide.Util.LogUtil;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


public class Palette {
    private int fontColor;
    public int backgroundColor;

    public static int getDefaultColor() {
        if (MainActivity.colors.contains("DEFAULTCOLOR")) {
            return MainActivity.colors.getInt("DEFAULTCOLOR", Color.parseColor("#e64a19"));
        } else {
            return Color.parseColor("#e64a19");
        }
    }

    /**
     * Gets the status bar color for the activity.
     * @return Color-int for the status bar
     */
    public static int getStatusBarColor() {
        return getDarkerColor(getDefaultColor());
    }

    /**
     * Gets the status bar color for the activity based on the specified username.
     * @param username The username to base the theme on
     * @return Color-int for the status bar
     */
    public static int getUserStatusBarColor(String username) {
        return getDarkerColor(getColorUser(username));
    }

    /**
     * Gets the status bar color for the activity based on the specified subreddit.
     * @param subreddit The subreddit to base the theme on
     * @return Color-int for the status bar
     */
    public static int getSubredditStatusBarColor(String subreddit) {
        return getDarkerColor(getColor(subreddit));
    }

    public static int getDefaultAccent() {
        if (MainActivity.colors.contains("ACCENTCOLOR")) {
            return MainActivity.colors.getInt("ACCENTCOLOR", Color.parseColor("#ff6e40"));
        } else {
            return Color.parseColor("#ff6e40");
        }
    }

    private int mainColor;
    private int accentColor;

    private static int getColorAccent(final String subreddit) {
        if (MainActivity.colors.contains("ACCENT" + subreddit.toLowerCase())) {
            return MainActivity.colors.getInt("ACCENT" + subreddit.toLowerCase(), getDefaultColor());
        } else {
            return getDefaultColor();
        }
    }

    public static int getFontColorUser(final String subreddit) {
        if (MainActivity.colors.contains("USER" + subreddit.toLowerCase())) {
            final int color = MainActivity.colors.getInt("USER" + subreddit.toLowerCase(), getDefaultColor());

            if (color == getDefaultColor()) {
                return 0;
            } else {
                return color;
            }
        } else {
            return 0;
        }
    }

    public static int[] getColors(String subreddit, Context context) {
        int[] ints = new int[2];

        ints[0] = getColor(subreddit);
        ints[1] = new ColorPreferences(context).getColor(subreddit);

        return ints;
    }

    public static int getColor(final String subreddit) {
        if (subreddit != null && MainActivity.colors.contains(subreddit.toLowerCase())) {
            return MainActivity.colors.getInt(subreddit.toLowerCase(), getDefaultColor());
        }
        return getDefaultColor();
    }

    public static void setColor(final String subreddit, int color) {
        MainActivity.colors.edit().putInt(subreddit.toLowerCase(), color).apply();
    }

    public static void removeColor(final String subreddit) {
        MainActivity.colors.edit().remove(subreddit.toLowerCase()).apply();
    }

    public static int getColorUser(final String username) {
        if (MainActivity.colors.contains("USER" + username.toLowerCase())) {
            return MainActivity.colors.getInt("USER" + username.toLowerCase(), getDefaultColor());
        } else {
            return getDefaultColor();
        }
    }

    public static void setColorUser(final String username, int color) {
        MainActivity.colors.edit().putInt("USER" + username.toLowerCase(), color).apply();
    }

    public static Palette getSubredditPallete(String subredditname) {
        Palette p = new Palette();

        p.theme = ThemeEnum.valueOf(MainActivity.colors.getString("ThemeDefault", "DARK"));
        p.fontColor = p.theme.getFontColor();
        p.backgroundColor = p.theme.getBackgroundColor();
        p.mainColor = getColor(subredditname);
        p.accentColor = getColorAccent(subredditname);

        return p;
    }

    public static Palette getDefaultPallete() {
        Palette p = new Palette();

        p.theme = ThemeEnum.valueOf(MainActivity.colors.getString("ThemeDefault", "DARK"));
        p.fontColor = p.theme.getFontColor();
        p.backgroundColor = p.theme.getBackgroundColor();

        return p;
    }

    /**
     * Displays the subreddit color chooser
     * It is possible to color multiple subreddits at the same time
     *
     * @param subreddits   Subreddits as an array
     * @param context      Context for getting colors
     * @param dialoglayout The subchooser layout (R.layout.colorsub)
     */
    public static void showSubThemeEditor(final ArrayList<String> subreddits, final Activity context, View dialoglayout) {
        if (subreddits.isEmpty()) {
            return;
        }

        final boolean multipleSubs = (subreddits.size() > 1);
        int currentColor;
        int currentAccentColor;

        final ColorPreferences colorPrefs = new ColorPreferences(context);
        final String subreddit = multipleSubs ? null : subreddits.get(0);
       dialoglayout.findViewById(R.id.bigpics).setVisibility(View.GONE);
      dialoglayout.findViewById(R.id.selftext).setVisibility(View.GONE);

        //Selected multiple subreddits
        if (multipleSubs) {
            //Check if all selected subs have the same settings
            int previousSubColor = 0;
            int previousSubAccent = 0;
            boolean sameMainColor = true;
            boolean sameAccentColor = true;

            for (String sub : subreddits) {
                int currentSubColor = Palette.getColor(sub);
                int currentSubAccent = colorPrefs.getColor("");

                if (previousSubColor != 0 && previousSubAccent != 0) {
                    if (currentSubColor != previousSubColor) {
                        sameMainColor = false;
                    } else if (currentSubAccent != previousSubAccent) {
                        sameAccentColor = false;
                    }
                }
                if (!sameMainColor && !sameAccentColor) {
                    break;
                }

                previousSubAccent = currentSubAccent;
                previousSubColor = currentSubColor;
            }

            currentColor = Palette.getDefaultColor();
            currentAccentColor = colorPrefs.getColor("");

            //If all selected subs have the same settings, display them
            if (sameMainColor) {
                currentColor = previousSubColor;
            }
            if (sameAccentColor) {
                currentAccentColor = previousSubAccent;
            }
        } else {  //Is only one selected sub
            currentColor = Palette.getColor(subreddit);
            currentAccentColor = colorPrefs.getColor(subreddit);
        }

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);

        final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
        title.setBackgroundColor(currentColor);

        if (multipleSubs) {
            String titleString = "";

            for (String sub : subreddits) {
                //if the subreddit is the frontpage, don't put "/r/" in front of it
                if (sub.equals("frontpage")) {
                    titleString += sub + ", ";
                } else {
                    if (sub.contains("/m/")) {
                        titleString += sub + ", ";
                    } else {
                        titleString += "/r/" + sub + ", ";
                    }
                }
            }
            titleString = titleString.substring(0, titleString.length() - 2);
            title.setMaxLines(3);
            title.setText(titleString);
        } else {
            if (subreddit.contains("/m/")) {
                title.setText(subreddit);
            } else {
                //if the subreddit is the frontpage, don't put "/r/" in front of it
                title.setText(((subreddit.equals("frontpage")) ? "frontpage" : "/r/" + subreddit));
            }
        }

        {
            //Primary color pickers
            final LineColorPicker colorPickerPrimary = (LineColorPicker) dialoglayout.findViewById(R.id.picker);
            //shades of primary colors
            final LineColorPicker colorPickerPrimaryShades = (LineColorPicker) dialoglayout.findViewById(R.id.picker2);

            colorPickerPrimary.setColors(ColorPreferences.getBaseColors(context));

            //Iterate through all colors and check if it matches the current color of the sub, then select it
            for (int i : colorPickerPrimary.getColors()) {
                for (int i2 : ColorPreferences.getColors(context, i)) {
                    if (i2 == currentColor) {
                        colorPickerPrimary.setSelectedColor(i);
                        colorPickerPrimaryShades.setColors(ColorPreferences.getColors(context, i));
                        colorPickerPrimaryShades.setSelectedColor(i2);
                        break;
                    }
                }
            }

            //Base color changed
            colorPickerPrimary.setOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void onColorChanged(int c) {
                    //Show variations of the base color
                    colorPickerPrimaryShades.setColors(ColorPreferences.getColors(context, c));
                    colorPickerPrimaryShades.setSelectedColor(c);
                }
            });
            colorPickerPrimaryShades.setOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void onColorChanged(int i) {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).updateColor(colorPickerPrimaryShades.getColor(), subreddit);
                    }
                    title.setBackgroundColor(colorPickerPrimaryShades.getColor());
                }
            });

            {
                         /* TODO   TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.reset);

                            // if button is clicked, close the custom dialog
                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Palette.removeColor(subreddit);
                                    hea.setBackgroundColor(Palette.getDefaultColor());
                                    findViewById(R.id.header).setBackgroundColor(Palette.getDefaultColor());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        Window window = getWindow();
                                        window.setStatusBarColor(Palette.getDarkerColor(Palette.getDefaultColor()));
                                        context.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

                                    }
                                    title.setBackgroundColor(Palette.getDefaultColor());


                                    int cx = center.getWidth() / 2;
                                    int cy = center.getHeight() / 2;

                                    int initialRadius = body.getWidth();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                        Animator anim =
                                                ViewAnimationUtils.createCircularReveal(body, cx, cy, initialRadius, 0);

                                        anim.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                body.setVisibility(View.GONE);
                                            }
                                        });
                                        anim.start();

                                    } else {
                                        body.setVisibility(View.GONE);

                                    }

                                }
                            });*/
            }

            //Accent color picker
            final LineColorPicker colorPickerAcc = (LineColorPicker) dialoglayout.findViewById(R.id.picker3);

            {
                //Get all possible accent colors (for day theme)
                int[] arrs = new int[ColorPreferences.Theme.values().length / 7];
                int i = 0;
                for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                    if (type.getThemeType() == 0) {
                        arrs[i] = ContextCompat.getColor(context, type.getColor());
                        i++;
                    }
                    colorPickerAcc.setColors(arrs);
                    colorPickerAcc.setSelectedColor(currentAccentColor);
                }
            }

            builder.setView(dialoglayout);
            builder.setCancelable(false);
            builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).updateColor(Palette.getColor(subreddit), subreddit);
                    }
                }
            }).setNeutralButton(R.string.btn_reset, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String subTitles = "";

                    if (multipleSubs) {
                        for (String sub : subreddits) {
                            //if the subreddit is the frontpage, don't put "/r/" in front of it
                            if (sub.equals("frontpage")) {
                                subTitles += sub + ", ";
                            } else {
                                subTitles += "/r/" + sub + ", ";
                            }
                        }
                        subTitles = subTitles.substring(0, subTitles.length() - 2);
                    } else {
                        //if the subreddit is the frontpage, don't put "/r/" in front of it
                        subTitles = (subreddit.equals("frontpage") ? "frontpage" : "/r/" + subreddit);
                    }
                    String titleStart = context.getString(R.string.settings_delete_sub_settings, subTitles);
                    titleStart = titleStart.replace("/r//r/", "/r/");
                    if (titleStart.contains("/r/frontpage")) {
                        titleStart = titleStart.replace("/r/frontpage", "frontpage");
                    }
                    new AlertDialogWrapper.Builder(context).setTitle(titleStart)
                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (String sub : subreddits) {
                                        Palette.removeColor(sub);
                                        // Remove layout settings
                                        // Remove accent / font color settings
                                        new ColorPreferences(context).removeFontStyle(sub);
                                    }

                                    if (context instanceof MainActivity) {
                                        ((MainActivity) context).reloadSubs();
                                    }
                                }
                            }).setNegativeButton(R.string.btn_no, null).show();
                }
            }).setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final int newPrimaryColor = colorPickerPrimaryShades.getColor();
                    final int newAccentColor = colorPickerAcc.getColor();

                    for (String sub : subreddits) {
                        // Set main color
                        //Only do set colors if either subreddit theme color has changed
                        if (Palette.getColor(sub) != newPrimaryColor || Palette.getDarkerColor(sub) != newAccentColor) {

                            if (newPrimaryColor != Palette.getDefaultColor()) {
                                Palette.setColor(sub, newPrimaryColor);
                            } else {
                                Palette.removeColor(sub);
                            }

                            // Set accent color
                            ColorPreferences.Theme t = null;

                            //Do not save accent color if it matches the default accent color
                            if (newAccentColor != ContextCompat.getColor(context, colorPrefs.getFontStyle().getColor()) || newAccentColor != ContextCompat.getColor(context, colorPrefs.getFontStyleSubreddit(sub).getColor())) {
                                LogUtil.v("Accent colors not equal");
                                int back = new ColorPreferences(context).getFontStyle().getThemeType();
                                for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                                    if (ContextCompat.getColor(context, type.getColor()) == newAccentColor && back == type.getThemeType()) {
                                        t = type;
                                        LogUtil.v("Setting accent color to " + t.getTitle());
                                        break;
                                    }
                                }
                            } else {
                                new ColorPreferences(context).removeFontStyle(sub);
                            }

                            if (t != null) {
                                colorPrefs.setFontStyle(t, sub);
                            }
                        }
                    }

                    //Only refresh stuff if the user changed something
                    if (Palette.getColor(subreddit) != newPrimaryColor || Palette.getDarkerColor(subreddit) != newAccentColor) {
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).reloadSubs();
                        }
                    }
                }
            }).show();
        }
    }
    public static int getDarkerColor(String s) {
        return getDarkerColor(getColor(s));
    }

    public static int getDarkerColor(int color) {
        float[] hsv = new float[3];

        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        color = Color.HSVToColor(hsv);

        return color;
    }

    public ThemeEnum theme;

    public enum ThemeEnum {
        DARK("Dark", Color.parseColor("#303030"), Color.parseColor("#424242"), Color.parseColor("#ffffff"), Color.parseColor("#B3FFFFFF")),
        LIGHT("Light",Color.parseColor("#e5e5e5"), Color.parseColor("#ffffff"), Color.parseColor("#de000000"), Color.parseColor("#8A000000") ),
        AMOLEDBLACK("Black", Color.parseColor("#000000"), Color.parseColor("#212121"), Color.parseColor("#ffffff"), Color.parseColor("#B3FFFFFF")),
        SEPIA("Sepia", Color.parseColor("#cac5ad"), Color.parseColor("#e2dfd7"), Color.parseColor("#DE3e3d36"), Color.parseColor("#8A3e3d36")),
        BLUE("Dark Blue", Color.parseColor("#2F3D44"), Color.parseColor("#37474F"), Color.parseColor("#ffffff"), Color.parseColor("#B3FFFFFF"));

        public String getDisplayName() {
            return displayName;
        }

        public int getBackgroundColor() {
            return backgroundColor;
        }

        public int getCardBackgroundColor() {
            return cardBackgroundColor;
        }

        public int getFontColor() {
            return fontColor;
        }
        public int getTint() {
            return tint;
        }

        final String displayName;
        final int backgroundColor;
        final int cardBackgroundColor;
        final int tint;
        final int fontColor;

        ThemeEnum(String s, int backgroundColor, int cardBackgroundColor, int fontColor, int tint){
            this.displayName = s;
            this.backgroundColor = backgroundColor;
            this.cardBackgroundColor = cardBackgroundColor;
            this.fontColor = fontColor;
            this.tint = tint;
        }
    }
}
