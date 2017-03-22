package me.ccrama.rssslide.Adapters;

import android.app.Activity;

import io.realm.Realm;
import io.realm.RealmResults;
import me.ccrama.rssslide.Realm.Article;

/**
 * Created by Carlos on 3/17/2017.
 */

public class SearchLoader implements DataSet {
    public boolean offline;
    public String search;
    public boolean loading;
    RealmResults<Article> results;

    public SearchLoader(final String id) {
        search = id;
        results = Realm.getDefaultInstance().where(Article.class).contains("summary", search).findAll();
    }

    public void loadMore(Activity context, FeedAdapter adapter) {
    }

    @Override
    public RealmResults<Article> getData() {
        return results;
    }

}
