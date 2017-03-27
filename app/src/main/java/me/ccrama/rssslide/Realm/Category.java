package me.ccrama.rssslide.Realm;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Carlos on 3/17/2017.
 */

public class Category extends RealmObject implements FeedWrapper {

    @PrimaryKey
    private String name;
    private RealmList<Feed> feeds;
    private int accessed;
    private int order;

    @Override
    public RealmResults<Article> getArticles() {
        RealmQuery<Article> a = Realm.getDefaultInstance().where(Article.class);
        for(Feed f : feeds){
            a.equalTo("feed", f.getTitle()).or();
        }
        return a.findAllSorted("published", Sort.DESCENDING);
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getIcon() {
        return null;
    }

    @Override
    public RealmResults<Article> getUnread() {
       //todo return getArticles().where().greaterThan("created", accessed).findAll();
        return null;
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
    public void setOrder(int i) {
        order = i;
    }

    @Override
    public ArrayList<Feed> getFeeds() {
        return new ArrayList<>(feeds);
    }
}
