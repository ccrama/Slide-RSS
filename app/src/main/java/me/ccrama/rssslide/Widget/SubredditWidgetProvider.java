package me.ccrama.rssslide.Widget;

/**
 * Created by carlo_000 on 5/4/2016.
 */

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import me.ccrama.rssslide.Activities.FeedViewSingle;
import me.ccrama.rssslide.Activities.SetupWidget;
import me.ccrama.rssslide.Activities.Website;
import me.ccrama.rssslide.Palette;
import me.ccrama.rssslide.R;

public class SubredditWidgetProvider extends AppWidgetProvider {
    public static final String UPDATE_MEETING_ACTION = "android.appwidget.action.APPWIDGET_UPDATE";
    public static final String SUBMISSION = "SUBMISSION";
    public static final String REFRESH = "REFRESH";

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(UPDATE_MEETING_ACTION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            mgr.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view);
            int view = R.layout.widget;
            switch(getThemeFromId(appWidgetId, context)){
                case 1:
                    view = R.layout.widget_dark;
                    break;
                case 2:
                    view = R.layout.widget_light;
                    break;
            }
            RemoteViews rv = new RemoteViews(context.getPackageName(), view);
            rv.setViewVisibility(R.id.loading, View.GONE);
            rv.setViewVisibility(R.id.refresh, View.VISIBLE);

            mgr.partiallyUpdateAppWidget(appWidgetId, rv);
        } else if (intent.getAction().contains(REFRESH)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            updateFromIdPartially(appWidgetId, context, mgr);
        } else if (intent.getAction().contains(SUBMISSION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            Intent i = new Intent(context, FeedViewSingle.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(FeedViewSingle.EXTRA_FEED, getFeedFromId(appWidgetId, context));
            context.startActivity(i);
        }
        super.onReceive(context, intent);
    }

    public static String lastDone;

    public static String getFeedFromId(int id, Context mContext) {
        String sub = mContext.getSharedPreferences("widget", 0).getString(id + "_sub", "");
        if (sub.isEmpty() && lastDone != null) {
            return lastDone;
        } else {
            return sub;
        }
    }

    public static int getThemeFromId(int id, Context mContext) {
        return mContext.getSharedPreferences("widget", 0).getInt(id + "_sub_theme", 0);
    }
    public static int getViewType(int id, Context mContext) {
        return mContext.getSharedPreferences("widget", 0).getInt(id + "_sub_view", 0);
    }
    public static void setSubFromid(int id, String sub, Context mContext) {
        mContext.getSharedPreferences("widget", 0).edit().putString(id + "_sub", sub).apply();
    }
    public static void setThemeToId(int id, int theme, Context mContext) {
        mContext.getSharedPreferences("widget", 0).edit().putInt(id + "_sub_theme", theme).apply();
    }
    public static void setViewType(int id, int checked, SetupWidget mContext) {
        mContext.getSharedPreferences("widget", 0).edit().putInt(id + "_sub_view", checked).apply();
    }
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // update each of the app widgets with the remote adapter
        for (int appWidgetId : appWidgetIds) {
            updateFromId(appWidgetId, context, appWidgetManager);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public void updateFromId(int appWidgetId, Context context, AppWidgetManager appWidgetManager) {
        // Set up the intent that starts the ListViewService, which will
        // provide the views for this collection.
        Intent intent = new Intent(context, ListViewWidgetService.class);
        // Add the app widget ID to the intent extras.
        Uri data = Uri.withAppendedPath(
                Uri.parse("slide" + "://widget/id/")
                , String.valueOf(appWidgetId));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        // Instantiate the RemoteViews object for the app widget layout.
        int view = R.layout.widget;
        switch(getThemeFromId(appWidgetId, context)){
            case 1:
                view = R.layout.widget_dark;
                break;
            case 2:
                view = R.layout.widget_light;
                break;
        }
        RemoteViews rv = new RemoteViews(context.getPackageName(), view);
        // Set up the RemoteViews object to use a RemoteViews adapter.
        // This adapter connects
        // to a RemoteViewsService  through the specified intent.
        // This is how you populate the data.
        rv.setRemoteAdapter(appWidgetId, R.id.list_view, intent);
        // Trigger listview item click
        String sub = getFeedFromId(appWidgetId, context);
        Intent startActivityIntent = new Intent(context, FeedViewSingle.class);
        startActivityIntent.putExtra(FeedViewSingle.EXTRA_FEED, sub);
        //todo go to sub
        {
            Intent refreshIntent = new Intent(context, SubredditWidgetProvider.class);
            refreshIntent.setData(data);
            refreshIntent.setAction(SUBMISSION);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingRefresh = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            rv.setOnClickPendingIntent(R.id.open, pendingRefresh);
        }
        {
            Intent refreshIntent = new Intent(context, SubredditWidgetProvider.class);
            refreshIntent.setAction(REFRESH);
            refreshIntent.setData(data);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingRefresh = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            rv.setOnClickPendingIntent(R.id.refresh, pendingRefresh);
        }
        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews  object above.
        rv.setEmptyView(R.id.list_view, R.id.empty_view);
        rv.setTextViewText(R.id.subreddit, sub);
        rv.setTextColor(R.id.subreddit, Palette.getColor(sub));

        //
        // Do additional processing specific to this app widget...
        //
        final Intent activityIntent = new Intent(context, Website.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.list_view, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }

    public void updateFromIdPartially(int appWidgetId, Context context, AppWidgetManager appWidgetManager) {
        // Set up the intent that starts the ListViewService, which will
        // provide the views for this collection.
        Intent intent = new Intent(context, ListViewWidgetService.class);
        // Add the app widget ID to the intent extras.
        Uri data = Uri.withAppendedPath(
                Uri.parse("slide" + "://widget/id/")
                , String.valueOf(appWidgetId)+ System.currentTimeMillis());
        intent.setData(data);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        // Instantiate the RemoteViews object for the app widget layout.
        int view = R.layout.widget;
        switch(getThemeFromId(appWidgetId, context)){
            case 1:
                view = R.layout.widget_dark;
                break;
            case 2:
                view = R.layout.widget_light;
                break;
        }
        RemoteViews rv = new RemoteViews(context.getPackageName(), view);
        // Set up the RemoteViews object to use a RemoteViews adapter.
        // This adapter connects
        // to a RemoteViewsService  through the specified intent.
        // This is how you populate the data.
        rv.setRemoteAdapter(appWidgetId, R.id.list_view, intent);
        rv.setViewVisibility(R.id.loading, View.VISIBLE);
        rv.setViewVisibility(R.id.refresh, View.GONE);

        // Trigger listview item click
        String sub = getFeedFromId(appWidgetId, context);
        Intent startActivityIntent = new Intent(context, FeedViewSingle.class);
        startActivityIntent.putExtra(FeedViewSingle.EXTRA_FEED, sub);
        //todo go to sub
        {
            Intent refreshIntent = new Intent(context, SubredditWidgetProvider.class);
            refreshIntent.setAction(SUBMISSION);
            refreshIntent.setData(data);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingRefresh = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            rv.setOnClickPendingIntent(R.id.open, pendingRefresh);
        }
        {
            Intent refreshIntent = new Intent(context, SubredditWidgetProvider.class);
            refreshIntent.setAction(REFRESH);
            refreshIntent.setData(data);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingRefresh = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            rv.setOnClickPendingIntent(R.id.refresh, pendingRefresh);
        }
        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews  object above.
        rv.setEmptyView(R.id.list_view, R.id.empty_view);
        rv.setTextViewText(R.id.subreddit, sub);
        rv.setTextColor(R.id.subreddit, Palette.getColor(sub));

        //
        // Do additional processing specific to this app widget...
        //
        final Intent activityIntent = new Intent(context, Website.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.list_view, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, rv);
    }



}
