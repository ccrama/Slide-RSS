package me.ccrama.rssslide.Realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Carlos on 3/17/2017.
 */

public class Feed extends RealmObject {
    public String name;
    public String icon;

    @PrimaryKey
    public String url;

    public RealmList<Article> articles;
    public RealmList<Article> unseen;
    public int order;

    public void addArticle(Article a) {
        articles.add(0,a);
        unseen.add(0,a);
    }

    public void setSeen(){
        unseen.clear();
    }

    public String getName(){
        return name + (unseen.isEmpty()? "" : " [" + unseen.size() + "]");
    }
}
