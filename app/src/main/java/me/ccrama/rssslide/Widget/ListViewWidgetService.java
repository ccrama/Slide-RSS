package me.ccrama.rssslide.Widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

import io.realm.Realm;
import me.ccrama.rssslide.BaseApplication;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.Realm.Article;
import me.ccrama.rssslide.Realm.Feed;
import me.ccrama.rssslide.Util.TimeUtils;

/**
 * Created by carlo_000 on 5/4/2016.
 */
public class ListViewWidgetService extends RemoteViewsService {
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListViewRemoteViewsFactory(this.getApplicationContext(), intent, "android",
                intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0));
    }
}

class ListViewRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private ArrayList<String> records;
    String feed;
    int id;

    public ListViewRemoteViewsFactory(Context context, Intent intent, String feed, int id) {
        mContext = context;
        this.feed = feed;
        this.id = id;
    }

    // Initialize the data set.
    public void onCreate() {
        // In onCreate() you set up any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        records = new ArrayList<>();
        String sub = SubredditWidgetProvider.getFeedFromId(id, mContext);
        Feed f = Realm.getDefaultInstance().where(Feed.class).equalTo("name", sub).findFirst();
        for(Article a : f.getArticles()){
            records.add(a.getId());
        }

        Intent widgetUpdateIntent = new Intent(mContext, SubredditWidgetProvider.class);
        widgetUpdateIntent.setAction(SubredditWidgetProvider.UPDATE_MEETING_ACTION);
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        mContext.sendBroadcast(widgetUpdateIntent);
    }

    // Given the position (index) of a WidgetItem in the array, use the item's text value in
    // combination with the app widget item XML file to construct a RemoteViews object.
    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.
        // Construct a RemoteViews item based on the app widget item XML file, and set the
        // text based on the position.
        int view = R.layout.submission_widget_light;
        switch (SubredditWidgetProvider.getViewType(id, mContext)) {
            case 1:
            case 0:
                if (SubredditWidgetProvider.getThemeFromId(id, mContext) == 2) {
                    view = R.layout.submission_widget_light;
                } else {
                    view = R.layout.submission_widget;
                }

                break;
            case 2:
                if (SubredditWidgetProvider.getThemeFromId(id, mContext) == 2) {
                    view = R.layout.submission_widget_compact_light;
                } else {
                    view = R.layout.submission_widget_compact;
                }
                break;
        }
        final RemoteViews rv = new RemoteViews(mContext.getPackageName(), view);
        try {

            // feed row
            Article data = Realm.getDefaultInstance().where(Article.class).equalTo("id", records.get(position)).findFirst();

            rv.setTextViewText(R.id.title, Html.fromHtml(data.getTitle()));
            rv.setTextViewText(R.id.information,
                    data.getAuthor() + " " + TimeUtils.getTimeAgo(data.getPublished(),
                            mContext));
            if (SubredditWidgetProvider.getViewType(id, mContext) == 1) {
                rv.setViewVisibility(R.id.thumbimage2, View.GONE);
                if (data.image != null && !data.image.isEmpty()) {
                    rv.setImageViewBitmap(R.id.bigpic,
                            BaseApplication.getImageLoader(mContext)
                                    .loadImageSync(data.image));
                    rv.setViewVisibility(R.id.bigpic, View.VISIBLE);
                } else {
                    rv.setViewVisibility(R.id.bigpic, View.GONE);
                }
            } else {
                if (SubredditWidgetProvider.getViewType(id, mContext) != 2) {
                    rv.setViewVisibility(R.id.bigpic, View.GONE);
                }
                if (data.image != null && !data.image.isEmpty()) {
                    rv.setImageViewBitmap(R.id.thumbimage2,
                            BaseApplication.getImageLoader(mContext)
                                    .loadImageSync(data.image));
                    rv.setViewVisibility(R.id.thumbimage2, View.VISIBLE);
                } else {
                    rv.setViewVisibility(R.id.thumbimage2, View.GONE);
                }
            }
            Bundle infos = new Bundle();
            infos.putString("url", data.getLink());
            infos.putBoolean("popup", true);
            final Intent activityIntent = new Intent();
            activityIntent.putExtras(infos);

            rv.setOnClickFillInIntent(R.id.card, activityIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rv;
    }

    public int getCount() {
        return records.size();
    }


    public void onDataSetChanged() {

        // Fetching JSON data from server and add them to records arraylist
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public void onDestroy() {
        records.clear();
    }

    public boolean hasStableIds() {
        return true;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

}
