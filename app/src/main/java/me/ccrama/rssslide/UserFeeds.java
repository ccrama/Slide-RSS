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

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Carlos on 3/16/2017.
 */

public class UserFeeds {

    public static RealmResults<Feed> feedList;

    public static void doMainActivitySubs(final MainActivity mainActivity) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                feedList = realm.where(Feed.class).findAllSorted("order");

            }
        });
        LogUtil.v("Doing realm stuff");
        if (feedList.isEmpty()) {
            doAddFeedAsync("https://www.reddit.com/r/slideforreddit/.rss", mainActivity);
        } else {
            mainActivity.setDataSet(feedList, true);
        }
    }

    public static RealmResults<Feed> getAllUserFeeds() {
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
                f.name = feed.getTitle();
                if(feed.getImage() != null)
                f.icon = feed.getImage().getUrl();
                if (f.icon != null && f.icon.endsWith("/")) {
                    f.icon = f.icon.substring(0, f.icon.length() - 1);
                }
                f.url = url;
                LogUtil.v("Doing feed " + f.name);
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

    public static void setFeeds(final ArrayList<Feed> subs) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (int i = 0; i < subs.size(); i++) {
                    subs.get(i).order = i;
                }
                for (Feed f : subs) {
                    realm.insertOrUpdate(f);
                }
            }
        });
    }
}
