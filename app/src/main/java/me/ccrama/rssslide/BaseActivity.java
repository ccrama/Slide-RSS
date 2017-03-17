package me.ccrama.rssslide;

import android.app.ActivityManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.util.Locale;

import me.ccrama.rssslide.SwipeLayout.SwipeBackLayout;
import me.ccrama.rssslide.SwipeLayout.Utils;
import me.ccrama.rssslide.SwipeLayout.app.SwipeBackActivityBase;
import me.ccrama.rssslide.SwipeLayout.app.SwipeBackActivityHelper;


/**
 * This is an activity which is the base for most of Slide's activities. It has support for handling
 * of swiping, setting up the AppBar (toolbar), and coloring of applicable views.
 */

public class BaseActivity extends AppCompatActivity
        implements SwipeBackActivityBase {
    @Nullable
    public Toolbar mToolbar;
    protected SwipeBackActivityHelper mHelper;
    protected boolean overrideRedditSwipeAnywhere = false;
    protected boolean enableSwipeBackLayout = true;
    protected boolean overrideSwipeFromAnywhere = false;
    NfcAdapter mNfcAdapter;

    /**
     * Enable fullscreen immersive mode if setting is checked
     */
    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (SettingValues.immersiveMode) {
            if (hasFocus) {
                hideDecor();
            }
        }
    }

    public void hideDecor() {
        if (SettingValues.immersiveMode) {
            final View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            decorView.setOnSystemUiVisibilityChangeListener(
                    new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility) == 0) {
                                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
                            } else {
                                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                            }
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            try {
                onBackPressed();
            } catch (IllegalStateException ignored) {

            }
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean shouldInterceptAlways = false;

    /**
     * Force English locale if setting is checked
     */
    public void applyOverrideLanguage() {
        if (SettingValues.overrideLanguage) {
            Locale locale = new Locale("en", "US");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources()
                    .updateConfiguration(config,
                            getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyOverrideLanguage();

        super.onCreate(savedInstanceState);

        /**
         * Enable fullscreen immersive mode if setting is checked
         *
         * Adding this check in the onCreate method prevents the status/nav bars from appearing
         * briefly when changing from one activity to another
         *
         */
        hideDecor();

        if (enableSwipeBackLayout) {
            mHelper = new SwipeBackActivityHelper(this);
            mHelper.onActivityCreate();

            if (SettingValues.swipeAnywhere || overrideRedditSwipeAnywhere) {
                if (overrideSwipeFromAnywhere) {
                    shouldInterceptAlways = true;
                } else {
                    mHelper.getSwipeBackLayout().setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
                    mHelper.getSwipeBackLayout().setFullScreenSwipeEnabled(true);
                }
            } else {
                shouldInterceptAlways = true;
            }
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (enableSwipeBackLayout) mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null) return mHelper.findViewById(id);
        return v;
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        if (enableSwipeBackLayout) {
            return mHelper.getSwipeBackLayout();
        } else {
            return null;
        }
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        if (enableSwipeBackLayout) getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        if (enableSwipeBackLayout) {
            Utils.convertActivityToTranslucent(this);
            getSwipeBackLayout().scrollToFinishActivity();
        }
    }

    /**
     * Disables the Swipe-Back-Layout. Should be called before calling super.onCreate()
     */
    protected void disableSwipeBackLayout() {
        enableSwipeBackLayout = false;
    }

    protected void overrideSwipeFromAnywhere() {
        overrideSwipeFromAnywhere = true;
    }

    protected void overrideRedditSwipeAnywhere() {
        overrideRedditSwipeAnywhere = true;
    }

    /**
     * Applies the activity's base color theme. Should be called before inflating any layouts.
     */
    protected void applyColorTheme() {
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
    }

    /**
     * Applies the activity's base color theme based on the theme of a specific subreddit. Should be
     * called before inflating any layouts.
     *
     * @param subreddit The subreddit to base the theme on
     */
    protected void applyColorTheme(String subreddit) {
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(subreddit), true);

    }

    /**
     * Applies the activity's base color theme based on the theme of a specific subreddit. Should be
     * called before inflating any layouts.
     * <p/>
     * This will take the accent colors from the sub theme but return the AMOLED with contrast base
     * theme.
     *
     * @param subreddit The subreddit to base the theme on
     */
    protected void applyDarkColorTheme(String subreddit) {
        getTheme().applyStyle(new ColorPreferences(this).getDarkThemeSubreddit(subreddit), true);
    }

    @Override
    public void onResume() {
        super.onResume();
        hideDecor();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar.
     *
     * @param toolbar        The toolbar's id
     * @param title          String resource for the toolbar's title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     */
    protected void setupAppBar(@IdRes int toolbar, @StringRes int title, boolean enableUpButton,
                               boolean colorToolbar) {
        setupAppBar(toolbar, getString(title), enableUpButton, colorToolbar);
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar.
     *
     * @param toolbar        The toolbar's id
     * @param title          String to be set as the toolbar title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     */
    protected void setupAppBar(@IdRes int toolbar, String title, boolean enableUpButton,
                               boolean colorToolbar) {
        int systemBarColor = Palette.getStatusBarColor();
        mToolbar = (Toolbar) findViewById(toolbar);

        if (colorToolbar) {
            mToolbar.setBackgroundColor(Palette.getDefaultColor());
        }
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enableUpButton);
            getSupportActionBar().setTitle(title);
        }

        themeSystemBars(systemBarColor);
        setRecentBar(title, systemBarColor);
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar to a specific color
     *
     * @param toolbar        The toolbar's id
     * @param title          String to be set as the toolbar title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     * @param color          Color to color the tab bar
     */
    protected void setupAppBar(@IdRes int toolbar, String title, boolean enableUpButton, int color,
                               @IdRes int appbar) {
        int systemBarColor = Palette.getDarkerColor(color);
        mToolbar = (Toolbar) findViewById(toolbar);
        findViewById(appbar).setBackgroundColor(color);

        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enableUpButton);
            getSupportActionBar().setTitle(title);
        }

        themeSystemBars(systemBarColor);
        setRecentBar(title, systemBarColor);
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar. Applies color theming
     * based on the theme for the username specified.
     *
     * @param toolbar        The toolbar's id
     * @param title          String to be set as the toolbar title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     * @param username       The username to base the theme on
     */
    protected void setupUserAppBar(@IdRes int toolbar, @Nullable String title,
                                   boolean enableUpButton, String username) {
        int systemBarColor = Palette.getUserStatusBarColor(username);
        mToolbar = (Toolbar) findViewById(toolbar);
        mToolbar.setBackgroundColor(Palette.getColorUser(username));
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enableUpButton);
            if (title != null) {
                getSupportActionBar().setTitle(title);
            }
        }

        themeSystemBars(systemBarColor);
        setRecentBar(title, systemBarColor);
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar. Applies color theming
     * based on the theme for the subreddit specified.
     *
     * @param toolbar        The toolbar's id
     * @param title          String to be set as the toolbar title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     * @param subreddit      The subreddit to base the theme on
     */
    protected void setupSubredditAppBar(@IdRes int toolbar, String title, boolean enableUpButton,
                                        String subreddit) {
        mToolbar = (Toolbar) findViewById(toolbar);
        mToolbar.setBackgroundColor(Palette.getColor(subreddit));
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enableUpButton);
            getSupportActionBar().setTitle(title);
        }

        themeSystemBars(subreddit);
        setRecentBar(title, Palette.getSubredditStatusBarColor(subreddit));
    }

    /**
     * Sets the status bar and navigation bar color for the activity based on a specific subreddit.
     *
     * @param subreddit The subreddit to base the color on.
     */
    protected void themeSystemBars(String subreddit) {
        themeSystemBars(Palette.getSubredditStatusBarColor(subreddit));
    }

    /**
     * Sets the status bar and navigation bar color for the activity
     *
     * @param color The color to tint the bars with
     */
    protected void themeSystemBars(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
            if (SettingValues.colorNavBar) {
                getWindow().setNavigationBarColor(color);
            }
        }
    }

    /**
     * Sets the title and color of the recent bar based on the subreddit
     *
     * @param subreddit Name of the subreddit
     */
    protected void setRecentBar(String subreddit) {
        setRecentBar(subreddit, Palette.getColor(subreddit));
    }

    /**
     * Sets the title in the recent overview with the given title and the default color
     *
     * @param title Title as string for the recent app bar
     * @param color Color for the recent app bar
     */
    protected void setRecentBar(@Nullable String title, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (title == null || title.equals("")) title = getString(R.string.app_name);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

            setTaskDescription(
                    new ActivityManager.TaskDescription(title, bitmap, color));

            bitmap.recycle();
        }
    }
}