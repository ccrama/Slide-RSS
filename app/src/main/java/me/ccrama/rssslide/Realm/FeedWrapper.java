package me.ccrama.rssslide.Realm;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.internal.RealmObjectProxy;

/**
 * Created by Carlos on 3/26/2017.
 */

public interface FeedWrapper {
    public RealmResults<Article> getArticles();

    public String getTitle();

    public String getIcon();

    public RealmResults<Article> getUnread();

    public boolean isCategory();

    public long getAccessed();

    public int getOrder();

    public void setOrder(int i);

    ArrayList<Feed> getFeeds();
}
