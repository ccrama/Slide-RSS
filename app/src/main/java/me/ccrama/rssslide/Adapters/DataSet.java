package me.ccrama.rssslide.Adapters;

import android.app.Activity;

import io.realm.RealmResults;
import me.ccrama.rssslide.Realm.Article;

/**
 * Created by Carlos on 3/17/2017.
 */

public interface DataSet {
    public void loadMore(Activity context, FeedAdapter adapter);
    public RealmResults<Article> getData();
    long getAccessed();
}