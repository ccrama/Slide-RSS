package me.ccrama.rssslide.Realm;

import android.app.Activity;

import com.rometools.rome.feed.synd.SyndEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import me.ccrama.rssslide.BaseApplication;
import me.ccrama.rssslide.Util.ConversionCallback;

/**
 * Created by Carlos on 3/16/2017.
 */

public class XMLToRealm {
    public static void convert(final String feed, final List<SyndEntry> items, final ConversionCallback c) {
        Realm.getInstance(BaseApplication.config).executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Feed f = realm.where(Feed.class).equalTo("name", feed).findFirst();
                ArrayList<Article> toAdd = new ArrayList<>();
                for (SyndEntry i : items) {
                    Article a = new Article();
                    a.setAll(i);
                    boolean exists = false;
                    for (Article a2 : f.articles) {
                        if (a2.getId().equals(a.getId())) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        realm.copyToRealmOrUpdate(a);
                        toAdd.add(a);
                    }
                }
                Collections.reverse(toAdd);
                for (Article a : toAdd) {
                    f.addArticle(a);
                }
                c.onCompletion(toAdd.size());
            }
        });
    }

    public static void convertSync(final Feed feed, final List<SyndEntry> items, final ConversionCallback c, Activity baseActivity) {
        baseActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        ArrayList<Article> toAdd = new ArrayList<>();
                        for (SyndEntry i : items) {
                            Article a = new Article();
                            a.setAll(i);
                            boolean exists = realm.where(Article.class).equalTo("id", a.getId()).findFirst() != null;
                            if (!exists) {
                                realm.copyToRealmOrUpdate(a);
                                toAdd.add(a);
                            }
                        }
                        Collections.reverse(toAdd);
                        for (Article a : toAdd) {
                            feed.articles.add(0, a);
                        }
                        c.onCompletion(toAdd.size());
                    }
                });

            }
        });

    }
}
