package me.ccrama.rssslide;

import android.app.Activity;
import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
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
                        feedList = realm.where(Feed.class).findAll();
                        if (feedList.isEmpty()) {
                            doAddFeed("https://www.reddit.com/r/slideforreddit/.rss", mainActivity);
                        }

                    }
                });
                mainActivity.setDataSet(feedList);
    }

    public static RealmResults<Feed> getAllUserFeeds(MainActivity mainActivity) {
        return feedList;
    }
    static InputStream stream = null;

    public static void doAddFeed(final String url, final Activity a) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {


                final FeedInfoParser xmlParser = new FeedInfoParser();
                List<FeedParser.Entry> entries = null;

                try {
                    stream = downloadUrl(url);
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Feed f;
                            try {
                                f = xmlParser.parse(stream);
                                f.url = url;
                                Realm.getDefaultInstance().copyToRealmOrUpdate(f);

                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    // Makes sure that the InputStream is closed after the app is
                    // finished using it.
                } catch (IOException e) {
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
                return null;
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

    public static void addFeed(String url, GenerateFeedCallback c){

    }
}
