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
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import me.ccrama.rssslide.Util.LogUtil;

public class CheckForPosts extends BroadcastReceiver {

    public static final String MESSAGE_EXTRA = "MESSAGE_FULLNAMES";
    public static final String SUBS_TO_GET = "SUBREDDIT_NOTIFS";
    private Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        new AsyncGetFeeds(c);
        if (MainActivity.notificationTime != -1)
            new NotificationJobScheduler(context).start(context);
    }

    public static class AsyncGetFeeds {

        public Context c;

        // Uploads XML from stackoverflow.com, parses it, and combines it with
// HTML markup. Returns HTML string.
        private List<SyndEntry> loadXmlFromNetwork(String urlString, Feed feed) throws XmlPullParserException, IOException, ParseException {
            SyndFeed f = null;
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(new URL(urlString).openStream()));

                f = new SyndFeedInput().build(doc);
                return f.getEntries();
            } catch (FeedException | ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
            return null;
        }

        public AsyncGetFeeds(Context context) {
            this.c = context;
            amount = 0;
            final Realm r = Realm.getDefaultInstance();
            r.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    List<Feed> f = realm.where(Feed.class).findAllSorted("order");
                    for (final Feed feed : f) {
                        try {
                            List<SyndEntry> entries =  loadXmlFromNetwork(feed.url, feed);
                            XMLToRealm.convertInTransaction(realm, feed, entries, new ConversionCallback() {
                                @Override
                                public void onCompletion(int size) {
                                    if (size > 0) {
                                        amount += size;
                                        NotificationManager notificationManager =
                                                (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                                        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

                                        int count = 0;
                                        for (Article a : feed.getUnseen()) {
                                            style.addLine(a.getTitle());
                                            count++;
                                        }

                                        style.setBigContentTitle("New " + feed.name + " articles")
                                                .setSummaryText("+" + count + " more");

                                        Intent openPIBase;
                                        openPIBase = new Intent(c, FeedViewSingle.class);
                                        openPIBase.putExtra(FeedViewSingle.EXTRA_FEED, feed.name);
                                        openPIBase.setFlags(
                                                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                                        PendingIntent openPi =
                                                PendingIntent.getActivity(c, feed.order,
                                                        openPIBase, 0);

                                        Notification notifb = new NotificationCompat.Builder(c)
                                                .setContentTitle(feed.name + " " + size + " new articles")
                                                .setContentText(size + " new articles")
                                                .setContentIntent(openPi)
                                                .setSmallIcon(R.drawable.newarticle)
                                                .setColor(Palette.getColor(feed.name))
                                                .setWhen(System.currentTimeMillis())
                                                .setStyle(style)
                                                .build();

                                        notificationManager.notify(feed.order, notifb);
                                    }

                                }
                            });

                        } catch (XmlPullParserException | IOException | ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    MainActivity.colors.edit().putLong("lastUpate", System.currentTimeMillis()).commit();
                    if (c instanceof MainActivity) {
                        ((MainActivity) c).newArticles(amount, System.currentTimeMillis());
                    } else {
                        if (MainActivity.notificationTime != -1)
                            new NotificationJobScheduler(c).start(c);
                    }
                }
            });
        }

        int amount;
        }

}