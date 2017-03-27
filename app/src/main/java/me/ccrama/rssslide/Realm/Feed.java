package me.ccrama.rssslide.Realm;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Carlos on 3/17/2017.
 */

public class Feed extends RealmObject implements FeedWrapper {
    private String name;
    private String icon;
    private long accessed;

    @PrimaryKey
    public String url;
    private int order;

    public void setSeen() {
        accessed = System.currentTimeMillis();
    }

    @Override
    public RealmResults<Article> getArticles() {
        return Realm.getDefaultInstance().where(Article.class).equalTo("feed", getTitle()).findAllSorted("published", Sort.DESCENDING);
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public RealmResults<Article> getUnread() {
        return Realm.getDefaultInstance().where(Article.class).equalTo("feed", getTitle()).greaterThan("created", accessed).findAll();
    }

    @Override
    public boolean isCategory() {
        return false;
    }

    @Override
    public long getAccessed() {
        return accessed;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public ArrayList<Feed> getFeeds() {
        return new ArrayList<Feed>() {{
            add(Feed.this);
        }};
    }

    @Override
    public void setOrder(int i) {
        order = i;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
