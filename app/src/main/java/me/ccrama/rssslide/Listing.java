package me.ccrama.rssslide;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Carlos on 3/16/2017.
 */

public class Listing extends RealmObject{
    RealmList<Article> articles;
    String URL;
    Long time;

    public void init(){
        articles = new RealmList<>();
    }
    public void addArticle(Article a){
        articles.add(a);
    }
}
