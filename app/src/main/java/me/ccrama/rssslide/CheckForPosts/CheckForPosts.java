package me.ccrama.rssslide.CheckForPosts;

/**
 * Created by carlo_000 on 10/13/2015.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import me.ccrama.rssslide.Activities.FeedViewSingle;
import me.ccrama.rssslide.Activities.MainActivity;
import me.ccrama.rssslide.BaseApplication;
import me.ccrama.rssslide.Palette;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.Realm.Article;
import me.ccrama.rssslide.Realm.Feed;
import me.ccrama.rssslide.Realm.XMLToRealm;
import me.ccrama.rssslide.Util.ConversionCallback;

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

        int amount;

        @Override
        public void onPostExecute(Boolean success) {
            if (success) {

                for(String s : loaded.keySet()){
                    NotificationManager notificationManager =
                            (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                    Feed feed = Realm.getInstance(BaseApplication.config).where(Feed.class).equalTo("name", s).findFirst();

                    for (Article a : feed.unseen) {
                        style.addLine(a.getTitle());
                    }

                    style.setBigContentTitle("New " + feed.name + " articles")
                            .setSummaryText("+" + feed.unseen.size() + " more");

                    Intent openPIBase;
                    openPIBase = new Intent(c, FeedViewSingle.class);
                    openPIBase.putExtra(FeedViewSingle.EXTRA_FEED, feed.name);
                    openPIBase.setFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent openPi =
                            PendingIntent.getActivity(c, feed.order,
                                    openPIBase, 0);

                    Notification notifb = new NotificationCompat.Builder(c)
                            .setContentTitle(feed.name + " " + loaded.get(s) + " new articles")
                            .setContentText(loaded.get(s) + " new articles")
                            .setContentIntent(openPi)
                            .setSmallIcon(R.drawable.newarticle)
                            .setColor(Palette.getColor(feed.name))
                            .setWhen(System.currentTimeMillis())
                            .setStyle(style)
                            .build();

                    notificationManager.notify(feed.order, notifb);
                }
                if (c instanceof MainActivity) {
                    ((MainActivity) c).newArticles(amount);
                } else {
                    if (MainActivity.notificationTime != -1)
                        new NotificationJobScheduler(c).start(c);
                }
            }
        }

        HashMap<String, Integer> loaded;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Realm r = Realm.getInstance(BaseApplication.config);
                loaded = new HashMap<>();
                r.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        final List<Feed> f = realm.where(Feed.class).findAllSorted("order");
                        for (final Feed s : f) {
                            try {
                                XMLToRealm.convert(s.getName(), loadXmlFromNetwork(s.url, s), new ConversionCallback() {
                                    @Override
                                    public void onCompletion(int size) {
                                        if (size > 0) {
                                            loaded.put(s.getName(), size);
                                            amount += size;
                                        }
                                    }
                                });
                            } catch (ParseException e) {
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
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