package me.ccrama.rssslide;

import android.util.Log;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Carlos on 3/16/2017.
 */

public class Article extends RealmObject {

    @PrimaryKey
    private String id;

    private String title;
    private String link;
    public String summary;
    public String image;
    private long published;
    public boolean read;
    public boolean starred;
    public boolean seen;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getPublished() {
        return published;
    }

    public void setPublished(long published) {
        this.published = published;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public void setSeen(){
        seen = true;
    }

    public void setAll(FeedParser.Entry article) {
        Log.v("Feed", "Converting " + article.title);
        this.link = article.link;
        if(article.id == null || article.id.isEmpty()){
            article.id = article.title.replace(" ", "");
        }
        this.id = article.id;
        this.published = article.published;
        this.title = article.title;
        this.summary = article.summary;
        this.image = article.image;
    }
}
