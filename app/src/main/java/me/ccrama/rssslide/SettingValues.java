package me.ccrama.rssslide;

import android.content.SharedPreferences;

import java.util.Calendar;

import me.ccrama.rssslide.Util.Constants;
import me.ccrama.rssslide.Views.CreateCardView;

/**
 * Created by ccrama on 9/19/2015.
 */
public class SettingValues {
    public static final String PREF_SINGLE = "Single";
    public static final String PREF_FAB = "Fab";
    public static final String PREF_NIGHT_MODE = "nightMode";
    public static final String PREF_NIGHT_THEME = "nightTheme";
    public static final String PREF_NO_IMAGES = "noImages";
    public static final String PREF_AUTOTHEME = "autotime";
    public static final String PREVIEWS_LEFT = "previewsLeft";
    public static final String PREF_COLOR_NAV_BAR = "colorNavBar";
    public static final String PREF_COLOR_EVERYWHERE = "colorEverywhere";
    public static final String PREFS_WEB = "web";
    public static final String PREF_ACTIONBAR_VISIBLE = "actionbarVisible";
    public static final String PREF_ACTIONBAR_TAP = "actionbarTap";
    public static final String PREF_CUSTOMTABS = "customtabs";
    public static final String PREF_STORE_HISTORY = "storehistory";
    public static final String PREF_SCROLL_SEEN = "scrollSeen";
    public static final String PREF_TITLE_FILTERS = "titleFilters";
    public static final String PREF_TEXT_FILTERS = "textFilters";
    public static final String PREF_DUAL_PORTRAIT = "dualPortrait";
    public static final String PREF_SWITCH_THUMB = "switchThumb";
    public static final String PREF_BIG_THUMBS = "bigThumbnails";
    public static final String PREF_OVERRIDE_LANGUAGE = "overrideLanguage";
    public static final String PREF_IMMERSIVE_MODE = "immersiveMode";
    public static final String PREF_SOUND_NOTIFS = "soundNotifs";
    public static final String PREF_COOKIES = "storeCookies";
    public static final String PREF_NIGHT_START = "nightStart";
    public static final String PREF_NIGHT_END = "nightEnd";

    public static final String PREF_FULL_COMMENT_OVERRIDE = "fullCommentOverride";
    public static final String PREF_EXIT = "Exit";
    public static final String PREF_FAB_CLEAR = "fabClear";
    public static final String PREF_HIDEBUTTON = "Hidebutton";
    public static final String PREF_SAVE_BUTTON = "saveButton";
    public static final String PREF_IMAGE = "image";
    public static final String PREF_ALWAYS_EXTERNAL = "alwaysexternal";

    public static CreateCardView.CardEnum defaultCardView;
    public static boolean bigPicEnabled;
    public static ColorMatchingMode colorMatchingMode;
    public static ColorIndicator colorIndicator;
    public static Palette.ThemeEnum theme;
    public static SharedPreferences prefs;
    public static boolean single;
    public static boolean album;
    public static boolean cache;
    public static boolean cacheDefault;
    public static boolean image;
    public static boolean video;
    public static boolean colorNavBar;
    public static boolean actionbarVisible;
    public static boolean actionbarTap;
    public static boolean fullCommentOverride;
    public static boolean noImages;
    public static boolean swipeAnywhere;
    public static boolean storeHistory;
    public static boolean scrollSeen;
    public static boolean saveButton;
    public static boolean colorEverywhere;
    public static boolean gif;
    public static boolean web;
    public static boolean postNav;
    public static boolean exit;
    public static int subredditSearchMethod;
    public static int nightStart;
    public static int nightEnd;

    public static int previews;

    public static String titleFilters;
    public static String textFilters;
    public static String alwaysExternal;

    public static boolean fab = true;
    public static int fabType = Constants.FAB_POST;
    public static boolean hideButton;
    public static boolean tabletUI = true;
    public static boolean customtabs;
    public static boolean dualPortrait;
    public static boolean nightMode;
    public static boolean autoTime;
    public static boolean albumSwipe;
    public static boolean switchThumb;
    public static boolean bigThumbnails;
    public static boolean commentPager;
    public static boolean overrideLanguage;
    public static boolean immersiveMode;
    public static boolean showDomain;
    public static int currentTheme; //current base theme (Light, Dark, Dark blue, etc.)
    public static int nightTheme;
    public static boolean notifSound;
    public static boolean cookies;
    public static boolean peek;

    public static void setAllValues(SharedPreferences settings) {
        prefs = settings;
        defaultCardView = CreateCardView.CardEnum.valueOf(
                settings.getString("defaultCardViewNew", "LARGE").toUpperCase());
        bigPicEnabled = settings.getBoolean("bigPicEnabled", true);

        colorMatchingMode = ColorMatchingMode.valueOf(
                settings.getString("ccolorMatchingModeNew", "MATCH_EXTERNALLY"));
        colorIndicator =
                ColorIndicator.valueOf(settings.getString("colorIndicatorNew", "CARD_BACKGROUND"));

        single = prefs.getBoolean(PREF_SINGLE, false);
        overrideLanguage = prefs.getBoolean(PREF_OVERRIDE_LANGUAGE, false);
        immersiveMode = prefs.getBoolean(PREF_IMMERSIVE_MODE, false);

        postNav = false;

        fab = prefs.getBoolean(PREF_FAB, true);

        nightMode = prefs.getBoolean(PREF_NIGHT_MODE, false);
        nightTheme = prefs.getInt(PREF_NIGHT_THEME, 0);
        autoTime = prefs.getBoolean(PREF_AUTOTHEME, false);
        colorNavBar = prefs.getBoolean(PREF_COLOR_NAV_BAR, false);
        colorEverywhere = prefs.getBoolean(PREF_COLOR_EVERYWHERE, true);
        noImages = prefs.getBoolean(PREF_NO_IMAGES, false);

        fullCommentOverride = prefs.getBoolean(PREF_FULL_COMMENT_OVERRIDE, false);
        web = prefs.getBoolean(PREFS_WEB, true);
        image = prefs.getBoolean(PREF_IMAGE, true);
        cache = true;
        cacheDefault = false;
        customtabs = prefs.getBoolean(PREF_CUSTOMTABS, false);
        storeHistory = prefs.getBoolean(PREF_STORE_HISTORY, true);
        scrollSeen = prefs.getBoolean(PREF_SCROLL_SEEN, false);
        notifSound = prefs.getBoolean(PREF_SOUND_NOTIFS, false);
        cookies = prefs.getBoolean(PREF_COOKIES, true);

        previews = prefs.getInt(PREVIEWS_LEFT, 10);
        nightStart = prefs.getInt(PREF_NIGHT_START, 9);
        nightEnd = prefs.getInt(PREF_NIGHT_END, 5);

        titleFilters = prefs.getString(PREF_TITLE_FILTERS, "");
        textFilters = prefs.getString(PREF_TEXT_FILTERS, "");

        dualPortrait = prefs.getBoolean(PREF_DUAL_PORTRAIT, false);

        switchThumb = prefs.getBoolean(PREF_SWITCH_THUMB, true);
        bigThumbnails = prefs.getBoolean(PREF_BIG_THUMBS, false);

        swipeAnywhere = true; //override this always now
        video = true;
        exit = prefs.getBoolean(PREF_EXIT, true);
        hideButton = prefs.getBoolean(PREF_HIDEBUTTON, false);
        saveButton = prefs.getBoolean(PREF_SAVE_BUTTON, false);
        actionbarVisible = prefs.getBoolean(PREF_ACTIONBAR_VISIBLE, true);
        actionbarTap = prefs.getBoolean(PREF_ACTIONBAR_TAP, false);
    }

    public static boolean isNight() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return (hour >= nightStart + 12 || hour <= nightEnd) && tabletUI && nightMode;
    }

    public enum ColorIndicator {
        CARD_BACKGROUND, NONE

    }

    public enum ColorMatchingMode {
        ALWAYS_MATCH, MATCH_EXTERNALLY
    }


}
