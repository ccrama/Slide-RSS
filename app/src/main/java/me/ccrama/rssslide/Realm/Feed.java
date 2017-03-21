package me.ccrama.rssslide.Realm;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Carlos on 3/17/2017.
 */

public class Feed extends RealmObject {
    public String name;
    public String icon;
    public long accessed;

    @PrimaryKey
    public String url;

    public RealmList<Article> articles;
    public int order;

    public void addArticle(Article a) {
        articles.add(0, a);
    }

    public void setSeen() {
        accessed = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Article> getUnseen() {
        return new ArrayList<>(articles.where().greaterThan("created", accessed).findAll());
    }
}
