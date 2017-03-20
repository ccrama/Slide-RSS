package me.ccrama.rssslide;

import android.text.Html;
import android.util.Log;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;

import org.jdom2.Element;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void setSeen() {
        seen = true;
    }

    public void setAll(SyndEntry article) {
        Log.v("Feed", "Converting " + article.getTitle());
        this.link = article.getLink();
        if (article.getUri() == null || article.getUri().isEmpty()) {
            this.id = article.getTitle().replace(" ", "");
        } else {
            this.id = article.getUri();
        }
        if (article.getPublishedDate() != null)
            this.published = article.getPublishedDate().getTime();
        this.title = article.getTitle();
        if (article.getDescription() != null) {
            this.summary = article.getDescription().getValue();
        } else {
            this.summary = "";
        }
        String imgURL = null;
        List<Element> foreignMarkups = article.getForeignMarkup();
        for (Element foreignMarkup : foreignMarkups) {
            LogUtil.v(foreignMarkup.toString());
            if (foreignMarkup.getAttribute("url") != null)
                imgURL = foreignMarkup.getAttribute("url").getValue();
            else
                if(foreignMarkup.getChildren() != null){
                    for(Element e : foreignMarkup.getChildren()){
                        if (e.getAttribute("url") != null) {
                            imgURL = e.getAttribute("url").getValue();
                            break;
                        }
                    }
                }

        }
        if (imgURL == null) {
            List<SyndEnclosure> encls = article.getEnclosures();
            if (!encls.isEmpty()) {
                for (SyndEnclosure e : encls) {
                    LogUtil.v(e.getUrl());

                    imgURL = e.getUrl();
                    if (!imgURL.isEmpty()) {
                        break;
                    }
                }
            }
        }
        if(imgURL == null && article.getDescription() != null){
            //Regex for possible images
            String imgRegex = "<[iI][mM][gG][^>]+[sS][rR][cC]\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";

            Pattern p = Pattern.compile(imgRegex);
            Matcher m = p.matcher(article.getDescription().getValue());

            if (m.find()) {
                imgURL = m.group(1);
            }

        }
        if(imgURL == null && article.getContents() != null){
            for(SyndContent c : article.getContents()){
                //Regex for possible images
                String imgRegex = "<[iI][mM][gG][^>]+[sS][rR][cC]\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";

                Pattern p = Pattern.compile(imgRegex);
                Matcher m = p.matcher(c.getValue());

                if (m.find()) {
                    imgURL = m.group(1);
                }
                if(this.summary == null || this.summary.trim().isEmpty()){
                    this.summary = c.getValue();
                }
            }
        }
        this.image = imgURL;
    }
}
