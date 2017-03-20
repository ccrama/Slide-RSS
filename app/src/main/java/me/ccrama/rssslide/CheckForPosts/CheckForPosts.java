package me.ccrama.rssslide.CheckForPosts;

/**
 * Created by carlo_000 on 10/13/2015.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import me.ccrama.rssslide.Article;
import me.ccrama.rssslide.ConversionCallback;
import me.ccrama.rssslide.Feed;
import me.ccrama.rssslide.MainActivity;
import me.ccrama.rssslide.Palette;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.XMLToRealm;

public class CheckForPosts extends BroadcastReceiver {

    public static final String MESSAGE_EXTRA = "MESSAGE_FULLNAMES";
    public static final String SUBS_TO_GET = "SUBREDDIT_NOTIFS";
    private Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        new AsyncGetFeeds(c).execute();
        if (MainActivity.notificationTime != -1)
            new NotificationJobScheduler(context).start(context);
    }

    public static class AsyncGetFeeds extends AsyncTask<Void, Void, Boolean> {

        public Context c;

        // Uploads XML from stackoverflow.com, parses it, and combines it with
// HTML markup. Returns HTML string.
        private List<SyndEntry> loadXmlFromNetwork(String urlString, Feed feed) throws XmlPullParserException, IOException, ParseException {
            SyndFeed f = null;
            try {
                f = new SyndFeedInput().build(new XmlReader(new URL(urlString)));
                return f.getEntries();
            } catch (FeedException e) {
                e.printStackTrace();
            }
            return null;
        }

        public AsyncGetFeeds(Context context) {
            this.c = context;
        }

        @Override
        public void onPostExecute(Boolean success) {
            if (success) {
                for (final String f : loaded.keySet()) {
                    XMLToRealm.convert(f, loaded.get(f), new ConversionCallback() {
                        @Override
                        public void onCompletion(int size) {
                            if (size > 0) {
                                NotificationManager notificationManager =
                                        (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                                NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                                Feed feed = Realm.getInstance(MainActivity.config).where(Feed.class).equalTo("name", f).findFirst();
                                for (Article a : feed.unseen) {
                                    style.addLine(a.getTitle());
                                }

                                Notification notifb = new NotificationCompat.Builder(c)
                                        .setContentTitle(feed.name + " " + size + " new articles")
                                        .setContentText(size + " new articles")
                                        .setSmallIcon(R.drawable.newarticle)
                                        .setColor(Palette.getColor(feed.name))
                                        .setStyle(style).build();

                                notificationManager.notify(feed.order, notifb);
                            }
                        }
                    });
                }
                if (MainActivity.notificationTime != -1) new NotificationJobScheduler(c).start(c);
            }
        }

        HashMap<String, List<SyndEntry>> loaded;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                loaded = new HashMap<>();
                if(MainActivity.config == null){
                    Realm.init(c);
                    MainActivity.config = new RealmConfiguration.Builder().name("posts").deleteRealmIfMigrationNeeded().build();
                    Realm.setDefaultConfiguration(MainActivity.config);
                }
                Realm r = Realm.getInstance(MainActivity.config);
                r.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        List<Feed> f = realm.where(Feed.class).findAllSorted("order");
                        for (Feed s : f) {
                            try {
                                loaded.put(s.name, loadXmlFromNetwork(s.url, s));
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                return true;

            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            return false;
        }
    }

}