package me.ccrama.rssslide;

import android.os.AsyncTask;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.realm.Realm;
import me.ccrama.rssslide.Activities.MainActivity;
import me.ccrama.rssslide.Realm.Category;
import me.ccrama.rssslide.Realm.Feed;
import me.ccrama.rssslide.Realm.FeedWrapper;
import me.ccrama.rssslide.Util.GenerateFeedCallback;
import me.ccrama.rssslide.Util.LogUtil;

/**
 * Created by Carlos on 3/16/2017.
 */

public class UserFeeds {

    public static ArrayList<FeedWrapper> feedList;

    public static void doMainActivitySubs(final MainActivity mainActivity) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                feedList = new ArrayList<FeedWrapper>(realm.where(Feed.class).findAllSorted("order"));
                feedList.addAll(realm.where(Category.class).findAllSorted("order"));
                Collections.sort(feedList, new Comparator<FeedWrapper>() {
                    @Override
                    public int compare(FeedWrapper p1, FeedWrapper p2) {
                        return p1.getOrder() - p2.getOrder();// Ascending
                    }

                });
            }
        });
        LogUtil.v("Doing realm stuff");
        if (feedList.isEmpty()) {
            doAddFeedAsync("https://www.reddit.com/r/slideforreddit/.rss", mainActivity);
        } else {
            mainActivity.setDataSet(feedList, true);
        }
    }

    public static ArrayList<FeedWrapper> getAllUserFeeds() {
        return feedList;
    }

    static InputStream stream = null;

    public static Feed doAddFeed(final String url) {
        LogUtil.v("Adding feed to " + url);
        try {
            stream = downloadUrl(url);
            final Feed f;
            try {
                SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));
                f = new Feed();
                f.setName(feed.getTitle());
                if (feed.getImage() != null)
                    f.setIcon(feed.getImage().getUrl());
                if (f.getIcon() != null && f.getIcon().endsWith("/")) {
                    f.setIcon(f.getIcon().substring(0, f.getIcon().length() - 1));
                }
                f.url = url;
                LogUtil.v("Doing feed " + f.getTitle());
                return f;
            } catch (FeedException e) {
                e.printStackTrace();
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void doAddFeedAsync(final String url, final MainActivity a) {
        new AsyncTask<Void, Void, Feed>() {
            @Override
            protected Feed doInBackground(Void... params) {
                return doAddFeed(url);
            }

            @Override
            protected void onPostExecute(final Feed feed) {
                Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(feed);
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        a.setDataSet(feedList, true);
                    }
                });


            }
        }.execute();
    }

    private static InputStream downloadUrl(String urlString) throws IOException {
        java.net.URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    public static void addFeed(String url, GenerateFeedCallback c) {

    }

    public static void setFeeds(final ArrayList<FeedWrapper> subs) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = 0; i < subs.size(); i++) {
                    subs.get(i).setOrder(i);
                }
                for (FeedWrapper f : subs) {
                    if(f instanceof Feed){
                        realm.insertOrUpdate((Feed)f);
                    } else {
                        realm.insertOrUpdate((Category)f);
                    }
                }
            }
        });
    }
}
