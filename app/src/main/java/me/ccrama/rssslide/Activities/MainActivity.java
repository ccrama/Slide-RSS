package me.ccrama.rssslide.Activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.lang3.StringEscapeUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import me.ccrama.rssslide.Adapters.SideArrayAdapter;
import me.ccrama.rssslide.CheckForPosts.CheckForPosts;
import me.ccrama.rssslide.CheckForPosts.NotificationJobScheduler;
import me.ccrama.rssslide.ColorPreferences;
import me.ccrama.rssslide.DragSort.ReorderFeeds;
import me.ccrama.rssslide.Fragments.FeedFragment;
import me.ccrama.rssslide.Palette;
import me.ccrama.rssslide.PreCachingLayoutManager;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.Realm.Feed;
import me.ccrama.rssslide.Realm.FeedWrapper;
import me.ccrama.rssslide.SettingValues;
import me.ccrama.rssslide.UserFeeds;
import me.ccrama.rssslide.Util.Constants;
import me.ccrama.rssslide.Util.LinkUtil;
import me.ccrama.rssslide.Util.LogUtil;
import me.ccrama.rssslide.Util.OnSingleClickListener;
import me.ccrama.rssslide.Util.TimeUtils;
import me.ccrama.rssslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.rssslide.Views.ToggleSwipeViewPager;

public class MainActivity extends BaseActivity {
    // Instance state keys
    static final int SETTINGS_RESULT = 2;
    public static final String EXTRA_PAGE_TO = "pageTo";
    public static FeedWrapper shouldLoad;
    public static int restartPage;
    private final long ANIMATE_DURATION_OFFSET = 45; //offset for smoothing out the exit animations
    public static int notificationTime;
    public boolean singleMode;
    public ToggleSwipeViewPager pager;
    public ArrayList<FeedWrapper> usedArray;
    public DrawerLayout drawerLayout;
    public View hea;
    public View header;
    public OverviewPagerAdapter adapter;
    public int toGoto = 0;
    public TabLayout mTabLayout;
    public ListView drawerFeedList;
    public FeedWrapper selectedFeed; //currently selected feed
    public boolean commentPager = false;
    public String tabViewModeTitle;
    public boolean inNightMode;
    boolean changed;
    View headerMain;
    MaterialDialog d;
    View accountsArea;
    SideArrayAdapter sideArrayAdapter;
    Menu menu;
    AsyncTask caching;
    int back;
    private int headerHeight; //height of the header
    public int reloadItemNumber = -2;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_RESULT) {
            int current = pager.getCurrentItem();
            if (current < 0) current = 0;
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(adapter);
            pager.setCurrentItem(current);
            if (mTabLayout != null) {
                mTabLayout.setupWithViewPager(pager);
                scrollToTabAfterLayout(current);
            }
            setToolbarClick();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)
                || drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawers();
        } else if (SettingValues.exit) {
            final AlertDialogWrapper.Builder builder =
                    new AlertDialogWrapper.Builder(MainActivity.this);
            builder.setTitle(R.string.general_confirm_exit);
            builder.setMessage(R.string.general_confirm_exit_msg);
            builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                   /* not yet runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialogWrapper.Builder(MainActivity.this).setTitle(
                                    R.string.err_permission)
                                    .setMessage(R.string.err_permission_msg)
                                    .setPositiveButton(R.string.btn_yes,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    ActivityCompat.requestPermissions(
                                                            MainActivity.this, new String[]{
                                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                            }, 1);

                                                }
                                            })
                                    .setNegativeButton(R.string.btn_no,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                    .show();
                        }
                    });*/

                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            changed = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            changed = true;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_subreddit_overview, menu);
        //Only show the "Share Slide" menu item if the user doesn't have Pro installed
        if (SettingValues.tabletUI) {
            menu.findItem(R.id.share).setVisible(false);
        }
        if (SettingValues.fab) {
            menu.findItem(R.id.hide_posts).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        this.menu = menu;
        menu.findItem(R.id.theme)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int style = new ColorPreferences(MainActivity.this).getThemeSubreddit(
                                selectedFeed.getTitle());
                        final Context contextThemeWrapper =
                                new ContextThemeWrapper(MainActivity.this, style);
                        LayoutInflater localInflater =
                                getLayoutInflater().cloneInContext(contextThemeWrapper);
                        final View dialoglayout =
                                localInflater.inflate(R.layout.colorsub, null);
                        ArrayList<String> arrayList = new ArrayList<>();
                        arrayList.add(selectedFeed.getTitle());
                        Palette.showSubThemeEditor(arrayList, MainActivity.this,
                                dialoglayout);
                        return false;
                    }
                });
        return true;
    }

    String term;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.night: {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.choosethemesmall, null);
                AlertDialogWrapper.Builder builder =
                        new AlertDialogWrapper.Builder(MainActivity.this);
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setBackgroundColor(Palette.getDefaultColor());

                builder.setView(dialoglayout);
                final Dialog d = builder.show();
                back = new ColorPreferences(MainActivity.this).getFontStyle().getThemeType();
                if (SettingValues.isNight()) {
                    dialoglayout.findViewById(R.id.nightmsg).setVisibility(View.VISIBLE);
                }
                dialoglayout.findViewById(R.id.black)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String[] names =
                                        new ColorPreferences(MainActivity.this).getFontStyle()
                                                .getTitle()
                                                .split("_");
                                String name = names[names.length - 1];
                                final String newName = name.replace("(", "");
                                for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                    if (theme.toString().contains(newName)
                                            && theme.getThemeType() == 2) {
                                        back = theme.getThemeType();
                                        new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                        d.dismiss();
                                        restartTheme();
                                        break;
                                    }
                                }
                            }
                        });
                dialoglayout.findViewById(R.id.blacklighter)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String[] names =
                                        new ColorPreferences(MainActivity.this).getFontStyle()
                                                .getTitle()
                                                .split("_");
                                String name = names[names.length - 1];
                                final String newName = name.replace("(", "");
                                for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                    if (theme.toString().contains(newName)
                                            && theme.getThemeType() == 4) {
                                        back = theme.getThemeType();
                                        new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                        d.dismiss();
                                        restartTheme();
                                        break;
                                    }
                                }
                            }
                        });
                dialoglayout.findViewById(R.id.sepia)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String[] names =
                                        new ColorPreferences(MainActivity.this).getFontStyle()
                                                .getTitle()
                                                .split("_");
                                String name = names[names.length - 1];
                                final String newName = name.replace("(", "");
                                for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                    if (theme.toString().contains(newName)
                                            && theme.getThemeType() == 5) {
                                        back = theme.getThemeType();
                                        new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                        d.dismiss();
                                        restartTheme();
                                        break;
                                    }
                                }
                            }
                        });
                dialoglayout.findViewById(R.id.red).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] names = new ColorPreferences(MainActivity.this).getFontStyle()
                                .getTitle()
                                .split("_");
                        String name = names[names.length - 1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 6) {
                                back = theme.getThemeType();
                                new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                d.dismiss();
                                restartTheme();
                                break;
                            }
                        }
                    }
                });
                dialoglayout.findViewById(R.id.light)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String[] names =
                                        new ColorPreferences(MainActivity.this).getFontStyle()
                                                .getTitle()
                                                .split("_");
                                String name = names[names.length - 1];
                                final String newName = name.replace("(", "");
                                for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                    if (theme.toString().contains(newName)
                                            && theme.getThemeType() == 1) {
                                        new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                        back = theme.getThemeType();
                                        d.dismiss();
                                        restartTheme();
                                        break;
                                    }
                                }
                            }
                        });
                dialoglayout.findViewById(R.id.dark).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] names = new ColorPreferences(MainActivity.this).getFontStyle()
                                .getTitle()
                                .split("_");
                        String name = names[names.length - 1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 0) {
                                new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                back = theme.getThemeType();
                                d.dismiss();
                                restartTheme();
                                break;
                            }
                        }
                    }
                });
                dialoglayout.findViewById(R.id.blue).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] names = new ColorPreferences(MainActivity.this).getFontStyle()
                                .getTitle()
                                .split("_");
                        String name = names[names.length - 1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 3) {
                                new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                back = theme.getThemeType();
                                d.dismiss();
                                restartTheme();
                                break;
                            }
                        }
                    }
                });
            }
            return true;
            case R.id.action_refresh:
                if (adapter != null && adapter.getCurrentFragment() != null) {
                    ((FeedFragment) adapter.getCurrentFragment()).forceRefresh();
                }
                return true;
            case R.id.search:
                MaterialDialog.Builder builder =
                        new MaterialDialog.Builder(this).title(R.string.search_title)
                                .alwaysCallInputCallback()
                                .input(getString(R.string.search_msg), "",
                                        new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(MaterialDialog materialDialog,
                                                                CharSequence charSequence) {
                                                term = charSequence.toString();
                                            }
                                        });

                //Add "search current sub" if it is not frontpage/all/random

                builder.positiveText(R.string.search_all)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog,
                                                @NonNull DialogAction dialogAction) {
                                Intent i = new Intent(MainActivity.this, SearchFeeds.class);
                                i.putExtra(SearchFeeds.EXTRA_SEARCH, term);
                                startActivity(i);
                            }
                        });

                builder.show();
                return true;
            case R.id.save:
                /*TODO cache
                saveOffline(((SubmissionsView) adapter.getCurrentFragment()).posts.posts,
                        ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);*/
                return true;
            case R.id.hide_posts:
                ((FeedFragment) adapter.getCurrentFragment()).clearSeenPosts(false);
                return true;
            case R.id.clear:
                Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        ((FeedFragment) adapter.getCurrentFragment()).dataSet.feed.getArticles().deleteAllFromRealm();
                        ((FeedFragment) adapter.getCurrentFragment()).forceRefresh();
                    }
                });
                return true;
            case R.id.share:
                defaultShareText("Slide for Reddit",
                        "https://play.google.com/store/apps/details?id=me.ccrama.redditslide",
                        MainActivity.this);
                return true;
            case R.id.action_shadowbox:
                /* TODO SHadowbox
                if (SettingValues.tabletUI) {
                    List<Submission> posts =
                            ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i2 = new Intent(this, Shadowbox.class);
                        i2.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                        i2.putExtra("offline",
                                ((SubmissionsView) adapter.getCurrentFragment()).posts.cached
                                        != null
                                        ? ((SubmissionsView) adapter.getCurrentFragment()).posts.cached.time
                                        : 0L);
                        i2.putExtra(Shadowbox.EXTRA_SUBREDDIT,
                                ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                        startActivity(i2);
                    }
                } else {
                    AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(this).setTitle(
                            R.string.general_shadowbox_ispro)
                            .setMessage(R.string.pro_upgrade_msg)
                            .setPositiveButton(R.string.btn_yes_exclaim,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                            } catch (ActivityNotFoundException e) {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                            }
                                        }
                                    })
                            .setNegativeButton(R.string.btn_no_danks,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            dialog.dismiss();
                                        }
                                    });
                    if (SettingValues.previews > 0) {
                        b.setNeutralButton("Preview (" + SettingValues.previews + ")",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SettingValues.prefs.edit()
                                                .putInt(SettingValues.PREVIEWS_LEFT,
                                                        SettingValues.previews - 1)
                                                .apply();
                                        SettingValues.previews = SettingValues.prefs.getInt(
                                                SettingValues.PREVIEWS_LEFT, 10);
                                        List<Submission> posts =
                                                ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                                        if (posts != null && !posts.isEmpty()) {
                                            Intent i2 =
                                                    new Intent(MainActivity.this, Shadowbox.class);
                                            i2.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                                            i2.putExtra("offline",
                                                    ((SubmissionsView) adapter.getCurrentFragment()).posts.cached
                                                            != null
                                                            ? ((SubmissionsView) adapter.getCurrentFragment()).posts.cached.time
                                                            : 0L);
                                            i2.putExtra(Shadowbox.EXTRA_SUBREDDIT,
                                                    ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                                            startActivity(i2);
                                        }
                                    }
                                });
                    }
                    b.show();
                }*/
                return true;
            default:
                return false;
        }
    }

    public static SharedPreferences colors;
    public static int dpWidth;

    /**
     * Converts px to dp
     *
     * @param px to convert to dp
     * @param c  context of view
     * @return dp
     */
    public static int pxToDp(int px, Context c) {
        final DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Converts dp to px, uses vertical density
     *
     * @param dp to convert to px
     * @return px
     */
    public static int dpToPxVertical(int dp) {
        final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Converts dp to px, uses horizontal density
     *
     * @param dp to convert to px
     * @return px
     */
    public static int dpToPxHorizontal(int dp) {
        final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void defaultShareText(String title, String url, Context c) {
        url = StringEscapeUtils.unescapeHtml4(Html.fromHtml(url).toString());
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        /* Decode html entities */
        title = StringEscapeUtils.unescapeHtml4(title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, url);
        c.startActivity(Intent.createChooser(sharingIntent, c.getString(R.string.title_share)));
    }

    public static void defaultShare(String url, Context c) {
        url = StringEscapeUtils.unescapeHtml4(Html.fromHtml(url).toString());
        Uri webpage = LinkUtil.formatURL(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(c.getPackageManager()) != null) {
            c.startActivity(intent);
        }
    }

    public static String arrayToString(ArrayList<String> array) {
        if (array != null) {
            StringBuilder b = new StringBuilder();
            for (String s : array) {
                b.append(s).append(",");


            }
            String f = b.toString();
            if (f.length() > 0) {
                f = f.substring(0, f.length() - 1);
            }

            return f;
        } else {
            return "";
        }
    }

    public static String arrayToString(ArrayList<String> array, String separator) {
        if (array != null) {
            StringBuilder b = new StringBuilder();
            for (String s : array) {
                b.append(s).append(separator);
            }
            String f = b.toString();
            if (f.length() > 0) {
                f = f.substring(0, f.length() - separator.length());
            }
            return f;
        } else {
            return "";
        }
    }

    public static ArrayList<String> stringToArray(String string) {
        ArrayList<String> f = new ArrayList<>();
        Collections.addAll(f, string.split(","));
        return f;
    }

    public static boolean isPackageInstalled(final Context ctx, String s) {
        try {
            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo pi = pm.getPackageInfo(s, 0);
            if (pi != null && pi.applicationInfo.enabled) return true;
        } catch (final Throwable ignored) {
        }
        return false;
    }

    private static boolean isPackageInstalled(final Context ctx) {
        try {
            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo pi = pm.getPackageInfo("me.ccrama.slideforreddittabletuiunlock", 0);
            if (pi != null && pi.applicationInfo.enabled) return true;
        } catch (final Throwable ignored) {
        }
        return false;
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        inNightMode = SettingValues.isNight();
        disableSwipeBackLayout();
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        int widthDp = this.getResources().getConfiguration().screenWidthDp;
        int heightDp = this.getResources().getConfiguration().screenHeightDp;

        int fina = (widthDp > heightDp) ? widthDp : heightDp;
        fina += 99;

        colors = getSharedPreferences("COLORS", 0);
        if (colors.contains("tabletOVERRIDE")) {
            dpWidth = colors.getInt("tabletOVERRIDE", fina / 300);
        } else {
            dpWidth = fina / 300;
        }

        if (colors.contains("notificationOverride")) {
            notificationTime = colors.getInt("notificationOverride", 360);
        } else {
            notificationTime = 30;
        }

        SettingValues.setAllValues(colors);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        boolean first = false;
        if (getIntent().getBooleanExtra("EXIT", false)) finish();
        applyColorTheme();
        setContentView(R.layout.activity_overview);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());
        setSupportActionBar(mToolbar);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_PAGE_TO)) {
            toGoto = getIntent().getIntExtra(EXTRA_PAGE_TO, 0);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(
                    Palette.getDarkerColor(Palette.getDarkerColor(Palette.getDefaultColor())));
        }

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        header = findViewById(R.id.header);

        //Gets the height of the header
        if (header != null) {
            header.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            headerHeight = header.getHeight();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                header.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                header.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                        }
                    });
        }

        pager = (ToggleSwipeViewPager)

                findViewById(R.id.content_view);

        singleMode = SettingValues.single;
        if (singleMode)

        {
            commentPager = SettingValues.commentPager;
        }
        // Inflate tabs if single mode is disabled
        if (!singleMode)

        {
            mTabLayout = (TabLayout) ((ViewStub) findViewById(R.id.stub_tabs)).inflate();
        }
        // Disable swiping if single mode is enabled
        if (singleMode)

        {
            pager.setSwipingEnabled(false);
        }

        UserFeeds.doMainActivitySubs(this);

        /**
         * int for the current base theme selected.
         * 0 = Dark, 1 = Light, 2 = AMOLED, 3 = Dark blue, 4 = AMOLED with contrast, 5 = Sepia
         */
        SettingValues.currentTheme = new ColorPreferences(this).getFontStyle().getThemeType();

        if (notificationTime != -1) {
            notifications = new NotificationJobScheduler(MainActivity.this);
            notifications.start(getApplicationContext());
        }
    }

    NotificationJobScheduler notifications;

    @Override
    public void onResume() {
        super.onResume();

        if ((!inNightMode && SettingValues.isNight()) || (inNightMode
                && !SettingValues.isNight())) {
            restartTheme();
        }

     /* remove   if (datasetChanged && UserSubscriptions.hasSubs() && !usedArray.isEmpty()) {
            usedArray = new ArrayList<>(UserSubscriptions.getSubscriptions(this));
            adapter.notifyDataSetChanged();
            sideArrayAdapter.notifyDataSetChanged();
            datasetChanged = false;
            if (mTabLayout != null) {
                mTabLayout.setupWithViewPager(pager);
                scrollToTabAfterLayout(pager.getCurrentItem());
            }
            setToolbarClick();
        }*/
        //Only refresh the view if a Setting was altered
        if (Settings.changed || SettingsTheme.changed) {

            reloadSubs();
            //If the user changed a Setting regarding the app's theme, restartTheme()
            if (SettingsTheme.changed /* todo maybe later || (usedArray != null && usedArray.size() != UserSubscriptions.getSubscriptions(this).size())*/) {
                restartTheme();
            }

            //Need to change the subreddit search method
            if (SettingsGeneral.searchChanged) {
                setDrawerSubList();

                if (SettingValues.subredditSearchMethod
                        == Constants.SUBREDDIT_SEARCH_METHOD_DRAWER) {
                    mToolbar.setOnLongClickListener(
                            null); //remove the long click listener from the toolbar
                    findViewById(R.id.drawer_divider).setVisibility(View.GONE);
                }
                SettingsGeneral.searchChanged = false;
            }
            SettingsTheme.changed = false;
            Settings.changed = false;
            setToolbarClick();
        }
    }

    @Override
    public void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    public static String abbreviate(final String str, final int maxWidth) {
        if (str.length() <= maxWidth) {
            return str;
        }

        final String abrevMarker = "...";
        return str.substring(0, maxWidth - 3) + abrevMarker;
    }

    /**
     * Set the drawer edge (i.e. how sensitive the drawer is) Based on a given screen width
     * percentage.
     *
     * @param displayWidthPercentage larger the value, the more sensitive the drawer swipe is;
     *                               percentage of screen width
     * @param drawerLayout           drawerLayout to adjust the swipe edge
     */
    public static void setDrawerEdge(Activity activity, final float displayWidthPercentage,
                                     DrawerLayout drawerLayout) {
        try {
            Field mDragger =
                    drawerLayout.getClass().getSuperclass().getDeclaredField("mLeftDragger");
            mDragger.setAccessible(true);

            ViewDragHelper leftDragger = (ViewDragHelper) mDragger.get(drawerLayout);
            Field mEdgeSize = leftDragger.getClass().getDeclaredField("mEdgeSize");
            mEdgeSize.setAccessible(true);
            final int currentEdgeSize = mEdgeSize.getInt(leftDragger);

            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            mEdgeSize.setInt(leftDragger,
                    Math.max(currentEdgeSize, (int) (displaySize.x * displayWidthPercentage)));
        } catch (Exception e) {
            LogUtil.e(e + ": Exception thrown while changing navdrawer edge size");
        }
    }

    public void doDrawer() {
        drawerFeedList = (ListView) findViewById(R.id.drawerlistview);
        drawerFeedList.setDividerHeight(0);
        drawerFeedList.setDescendantFocusability(ListView.FOCUS_BEFORE_DESCENDANTS);
        final LayoutInflater inflater = getLayoutInflater();
        final View header;


        header = inflater.inflate(R.layout.drawer, drawerFeedList, false);
        headerMain = header;
        hea = header.findViewById(R.id.back);

        drawerFeedList.addHeaderView(header, null, false);
        header.findViewById(R.id.nav_manage).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                //todo manage feeds
                Intent i = new Intent(MainActivity.this, ReorderFeeds.class);
                startActivity(i);

            }
        });
        findViewById(R.id.currentlyDoing).setVisibility(View.GONE);
        header.findViewById(R.id.nav_cache).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                new CheckForPosts.AsyncGetFeeds(MainActivity.this);
                findViewById(R.id.currentlyDoing).setVisibility(View.VISIBLE);
                findViewById(R.id.saving).setVisibility(View.GONE);

            }
        });
        header.findViewById(R.id.nav_support).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                //todo go pro
            }
        });
        header.findViewById(R.id.nav_settings).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(MainActivity.this, Settings.class);
                startActivity(i);
                drawerLayout.closeDrawers();
            }
        });

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        final ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar,
                        R.string.btn_open, R.string.btn_close) {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        super.onDrawerSlide(drawerView, 0); // this disables the animation
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                        sideArrayAdapter.notifyDataSetChanged();
                        ((TextView) findViewById(R.id.synced)).setText("Sync feeds now (last update " + TimeUtils.getTimeAgo(getLastUpdate(), MainActivity.this).toLowerCase() + ")");
                    }

                    @Override
                    public void onDrawerClosed(View view) {
                        super.onDrawerClosed(view);
                        InputMethodManager imm =
                                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(drawerLayout.getWindowToken(), 0);
                    }
                };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
        header.findViewById(R.id.back).setBackgroundColor(Palette.getColor("alsdkfjasld"));
        if (accountsArea != null) {
            accountsArea.setBackgroundColor(Palette.getDarkerColor("alsdkfjasld"));
        }

        setDrawerSubList();
    }


    /**
     * Starts the enter animations for various UI components of the toolbar subreddit search
     *
     * @param ANIMATION_DURATION     duration of the animation in ms
     * @param SUGGESTIONS_BACKGROUND background of subreddit suggestions list
     * @param GO_TO_SUB_FIELD        search field in toolbar
     * @param CLOSE_BUTTON           button that clears the search and closes the search UI
     */
    public void enterAnimationsForToolbarSearch(final long ANIMATION_DURATION,
                                                final CardView SUGGESTIONS_BACKGROUND, final AutoCompleteTextView GO_TO_SUB_FIELD,
                                                final ImageView CLOSE_BUTTON) {
        SUGGESTIONS_BACKGROUND.animate()
                .translationY(headerHeight)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION + ANIMATE_DURATION_OFFSET)
                .start();

        GO_TO_SUB_FIELD.animate()
                .alpha(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION)
                .start();

        CLOSE_BUTTON.animate()
                .alpha(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION)
                .start();
    }

    /**
     * Starts the exit animations for various UI components of the toolbar subreddit search
     *
     * @param ANIMATION_DURATION     duration of the animation in ms
     * @param SUGGESTIONS_BACKGROUND background of subreddit suggestions list
     * @param GO_TO_SUB_FIELD        search field in toolbar
     * @param CLOSE_BUTTON           button that clears the search and closes the search UI
     */
    public void exitAnimationsForToolbarSearch(final long ANIMATION_DURATION,
                                               final CardView SUGGESTIONS_BACKGROUND, final AutoCompleteTextView GO_TO_SUB_FIELD,
                                               final ImageView CLOSE_BUTTON) {
        SUGGESTIONS_BACKGROUND.animate()
                .translationY(-SUGGESTIONS_BACKGROUND.getHeight())
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION + ANIMATE_DURATION_OFFSET)
                .start();

        GO_TO_SUB_FIELD.animate()
                .alpha(0f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION)
                .start();

        CLOSE_BUTTON.animate()
                .alpha(0f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION)
                .start();

//Helps smooth the transition between the toolbar title being reset and the search elements
//fading out.
        final long OFFSET_ANIM = (ANIMATION_DURATION == 0) ? 0 : ANIMATE_DURATION_OFFSET;

        //Hide the various UI components after the animations are complete and
        //reset the toolbar title
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SUGGESTIONS_BACKGROUND.setVisibility(View.GONE);
                GO_TO_SUB_FIELD.setVisibility(View.GONE);
                CLOSE_BUTTON.setVisibility(View.GONE);

                if (SettingValues.single) {
                    getSupportActionBar().setTitle(selectedFeed.getTitle());
                } else {
                    getSupportActionBar().setTitle(tabViewModeTitle);
                }
            }
        }, ANIMATION_DURATION + ANIMATE_DURATION_OFFSET);
    }

    public int getCurrentPage() {
        int position = 0;
        int currentOrientation = getResources().getConfiguration().orientation;
        if (adapter.getCurrentFragment() == null) {
            return 0;
        }
        if (((FeedFragment) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof LinearLayoutManager
                && currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            position =
                    ((LinearLayoutManager) ((FeedFragment) adapter.getCurrentFragment()).rv.getLayoutManager())
                            .findFirstCompletelyVisibleItemPosition() - 1;
        } else if (((FeedFragment) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof CatchStaggeredGridLayoutManager) {
            int[] firstVisibleItems = null;
            firstVisibleItems =
                    ((CatchStaggeredGridLayoutManager) ((FeedFragment) adapter.getCurrentFragment()).rv
                            .getLayoutManager()).findFirstCompletelyVisibleItemPositions(
                            firstVisibleItems);
            if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                position = firstVisibleItems[0] - 1;
            }
        } else {
            position =
                    ((PreCachingLayoutManager) ((FeedFragment) adapter.getCurrentFragment()).rv.getLayoutManager())
                            .findFirstCompletelyVisibleItemPosition() - 1;
        }
        return position;
    }

    public void reloadSubs() {
        int current = pager.getCurrentItem();
        if (current < 0) {
            current = 0;
        }
        reloadItemNumber = current;
        adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        reloadItemNumber = -2;
        shouldLoad = usedArray.get(current);
        pager.setCurrentItem(current);
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(pager);
            scrollToTabAfterLayout(current);
        }


        if (SettingValues.single) {
            getSupportActionBar().setTitle(shouldLoad.getTitle());
        }

        setToolbarClick();
    }

    public void restartTheme() {
        restartPage = getCurrentPage();
        Intent intent = this.getIntent();
        int page = pager.getCurrentItem();
        intent.putExtra(EXTRA_PAGE_TO, page);
        finish();
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_real, R.anim.fading_out_real);
    }

    public void scrollToTop() {
        int[] firstVisibleItems;
        int pastVisiblesItems = 0;

        if (((adapter.getCurrentFragment()) == null)) return;
        firstVisibleItems =
                ((CatchStaggeredGridLayoutManager) (((FeedFragment) adapter.getCurrentFragment()).rv
                        .getLayoutManager())).findFirstVisibleItemPositions(null);
        if (firstVisibleItems != null && firstVisibleItems.length > 0) {
            for (int firstVisibleItem : firstVisibleItems) {
                pastVisiblesItems = firstVisibleItem;
            }
        }
        if (pastVisiblesItems > 8) {
            ((FeedFragment) adapter.getCurrentFragment()).rv.scrollToPosition(0);
            header.animate()
                    .translationY(header.getHeight())
                    .setInterpolator(new LinearInterpolator())
                    .setDuration(0);
        } else {
            ((FeedFragment) adapter.getCurrentFragment()).rv.smoothScrollToPosition(0);
        }
        ((FeedFragment) adapter.getCurrentFragment()).resetScroll();
    }

    public void setDataSet(List<FeedWrapper> data, boolean sidebar) {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setDrawerEdge(this, Constants.DRAWER_SWIPE_EDGE, drawerLayout);

        if (sidebar) {
            doDrawer();
        } else {
            setDrawerSubList();
        }
        if (data != null && !data.isEmpty()) {
            usedArray = new ArrayList(data);
            if (adapter == null) {
                adapter = new OverviewPagerAdapter(getSupportFragmentManager());
            } else {
                adapter.notifyDataSetChanged();
            }
            pager.setAdapter(adapter);

            pager.setOffscreenPageLimit(1);
            if (toGoto == -1) {
                toGoto = 0;
            }
            if (toGoto >= usedArray.size()) {
                toGoto -= 1;
            }
            shouldLoad = usedArray.get(toGoto);
            selectedFeed = (usedArray.get(toGoto));
            themeSystemBars(usedArray.get(toGoto).getTitle());

            final String USEDARRAY_0 = usedArray.get(0).getTitle();
            header.setBackgroundColor(Palette.getColor(USEDARRAY_0));

            if (hea != null) {
                hea.setBackgroundColor(Palette.getColor(USEDARRAY_0));
                if (accountsArea != null) {
                    accountsArea.setBackgroundColor(Palette.getDarkerColor(USEDARRAY_0));
                }
            }

            if (!SettingValues.single) {
                mTabLayout.setSelectedTabIndicatorColor(
                        new ColorPreferences(MainActivity.this).getColor(USEDARRAY_0));
                pager.setCurrentItem(toGoto);
                mTabLayout.setupWithViewPager(pager);
                if (mTabLayout != null) {
                    mTabLayout.setupWithViewPager(pager);
                    scrollToTabAfterLayout(toGoto);
                }
            } else {
                getSupportActionBar().setTitle(usedArray.get(toGoto).getTitle());
                pager.setCurrentItem(toGoto);
            }
            setToolbarClick();

            setRecentBar(usedArray.get(toGoto).getTitle());
        } else {
            UserFeeds.doMainActivitySubs(this);
        }
    }

    public void setDrawerSubList() {
        ArrayList<FeedWrapper> copy = new ArrayList<>(UserFeeds.getAllUserFeeds());

        sideArrayAdapter = new SideArrayAdapter(this, copy, drawerFeedList);
        drawerFeedList.setAdapter(sideArrayAdapter);

        drawerFeedList.setFocusable(false);
    }

    public void setToolbarClick() {
        if (mTabLayout != null) {
            mTabLayout.setOnTabSelectedListener(
                    new TabLayout.ViewPagerOnTabSelectedListener(pager) {
                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {
                            super.onTabReselected(tab);
                            scrollToTop();
                        }
                    });
        } else {
            LogUtil.v("notnull");
            mToolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scrollToTop();
                }
            });
        }
    }

    public void updateColor(int color, String subreddit) {
        hea.setBackgroundColor(color);
        header.setBackgroundColor(color);
        if (accountsArea != null) {
            accountsArea.setBackgroundColor(Palette.getDarkerColor(color));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Palette.getDarkerColor(color));
        }
        setRecentBar(subreddit, color);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap =
                Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static int toDp(Context context, int px) {
        return convert(context, px, TypedValue.COMPLEX_UNIT_PX);
    }

    private static int convert(Context context, int amount, int conversionUnit) {
        if (amount < 0) {
            throw new IllegalArgumentException("px should not be less than zero");
        }

        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(conversionUnit, amount, r.getDisplayMetrics());
    }

    public static Bitmap clipToCircle(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle((float) (width / 2), (float) (height / 2),
                (float) Math.min(width, (height / 2)), Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }

    private static ValueAnimator flipAnimator(boolean isFlipped, final View v) {
        ValueAnimator animator = ValueAnimator.ofFloat(isFlipped ? -1f : 1f, isFlipped ? 1f : -1f);
        animator.setInterpolator(new FastOutSlowInInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                v.setScaleY((Float) valueAnimator.getAnimatedValue());
            }
        });
        return animator;
    }

    private void collapse(final LinearLayout v) {
        int finalHeight = v.getHeight();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0, v);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    private void dismissProgressDialog() {
        if (d != null && d.isShowing()) {
            d.dismiss();
        }
    }

    private void expand(LinearLayout v) {
        //set Visible
        v.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(widthSpec, heightSpec);

        ValueAnimator mAnimator = slideAnimator(0, v.getMeasuredHeight(), v);
        mAnimator.start();
    }

    private void scrollToTabAfterLayout(final int tabIndex) {
        //from http://stackoverflow.com/a/34780589/3697225
        if (mTabLayout != null) {
            final ViewTreeObserver observer = mTabLayout.getViewTreeObserver();

            if (observer.isAlive()) {
                observer.dispatchOnGlobalLayout(); // In case a previous call is waiting when this call is made
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mTabLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        mTabLayout.getTabAt(tabIndex).select();
                    }
                });
            }
        }
    }

    private ValueAnimator slideAnimator(int start, int end, final View v) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.setInterpolator(new FastOutSlowInInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = value;
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    public boolean feedContains(String base) {
        for (FeedWrapper f : usedArray) {
            if (f.getTitle().equalsIgnoreCase(base)) {
                return true;
            }
        }
        return false;
    }

    public int feedIndexOf(String base) {
        for (int i = 0; i < usedArray.size(); i++) {
            if (usedArray.get(i).getTitle().equalsIgnoreCase(base)) {
                return i;
            }
        }
        return 0;
    }

    public void newArticles(int counter, long time) {
        if (counter > 0)
            Snackbar.make(drawerLayout, counter + " new articles loaded", Snackbar.LENGTH_SHORT).show();
        findViewById(R.id.currentlyDoing).setVisibility(View.GONE);
        findViewById(R.id.saving).setVisibility(View.VISIBLE);
        sideArrayAdapter.notifyDataSetChanged();
        ((TextView) findViewById(R.id.synced)).setText("Sync feeds now (last update " + TimeUtils.getTimeSince(time, this) + ")");
    }

    public long getLastUpdate() {
        return colors.getLong("lastUpate", System.currentTimeMillis());
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {
        protected FeedFragment mCurrentFragment;

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);

            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                    if (positionOffset == 0) {
                        header.animate()
                                .translationY(0)
                                .setInterpolator(new LinearInterpolator())
                                .setDuration(180);
                    }
                }

                @Override
                public void onPageSelected(final int position) {
                    selectedFeed = usedArray.get(position);
                    FeedFragment page = (FeedFragment) adapter.getCurrentFragment();

                    if (hea != null) {
                        hea.setBackgroundColor(Palette.getColor(selectedFeed.getTitle()));
                        if (accountsArea != null) {
                            accountsArea.setBackgroundColor(Palette.getDarkerColor(selectedFeed.getTitle()));
                        }
                    }

                    int colorFrom = ((ColorDrawable) header.getBackground()).getColor();
                    int colorTo = Palette.getColor(selectedFeed.getTitle());

                    ValueAnimator colorAnimation =
                            ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int color = (int) animator.getAnimatedValue();

                            header.setBackgroundColor(color);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                getWindow().setStatusBarColor(Palette.getDarkerColor(color));
                                if (SettingValues.colorNavBar) {
                                    getWindow().setNavigationBarColor(
                                            Palette.getDarkerColor(color));
                                }
                            }
                        }

                    });
                    colorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                    colorAnimation.setDuration(200);
                    colorAnimation.start();

                    setRecentBar(selectedFeed.getTitle());

                    if (SettingValues.single || mTabLayout == null) {
                        //Smooth out the fading animation for the toolbar subreddit search UI
                        getSupportActionBar().setTitle(selectedFeed.getTitle());
                    } else {
                        mTabLayout.setSelectedTabIndicatorColor(
                                new ColorPreferences(MainActivity.this).getColor(selectedFeed.getTitle()));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            if (pager.getAdapter() != null) {
                pager.getAdapter().notifyDataSetChanged();
                pager.setCurrentItem(1);
                pager.setCurrentItem(0);
            }
        }

        @Override
        public int getCount() {
            if (usedArray == null) {
                return 1;
            } else {
                return usedArray.size();
            }
        }

        @Override
        public Fragment getItem(int i) {

            FeedFragment f = new FeedFragment();
            Bundle args = new Bundle();
            String name;
            name = usedArray.get(i).getTitle();
            args.putString("id", name);
            f.setArguments(args);

            return f;


        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (reloadItemNumber == position || reloadItemNumber < 0) {
                super.setPrimaryItem(container, position, object);
                if (usedArray.size() >= position) doSetPrimary(object, position);
            } else {
                shouldLoad = usedArray.get(reloadItemNumber);
                shouldLoad = usedArray.get(reloadItemNumber);
            }
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        public void doSetPrimary(Object object, int position) {
            if (object != null
                    && getCurrentFragment() != object
                    && object instanceof FeedFragment) {
                shouldLoad = usedArray.get(position);
                shouldLoad = usedArray.get(position);
                mCurrentFragment = ((FeedFragment) object);
                if (mCurrentFragment.dataSet == null && mCurrentFragment.isAdded()) {
                    mCurrentFragment.doAdapter();

                }
            }
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }


        @Override
        public CharSequence getPageTitle(int position) {

            if (usedArray != null) {
                try {
                    return abbreviate(usedArray.get(position).getTitle(), 25);
                } catch (Exception e) {
                    return "";
                }
            } else {
                return "";
            }


        }
    }
}
