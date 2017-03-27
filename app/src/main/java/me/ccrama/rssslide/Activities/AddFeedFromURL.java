package me.ccrama.rssslide.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import io.realm.Realm;
import me.ccrama.rssslide.Realm.Feed;
import me.ccrama.rssslide.UserFeeds;
import me.ccrama.rssslide.Util.LogUtil;

/**
 * Created by Carlos on 3/20/2017.
 */
public class AddFeedFromURL extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent

            }
        }
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            LogUtil.v(sharedText);
            new ParseFeedTask().execute(sharedText);
        }
    }


    public class ParseFeedTask extends AsyncTask<String, Void, Feed> {

        String url;

        @Override
        public void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected Feed doInBackground(String... input) {
            url = input[0];
            Feed f = UserFeeds.doAddFeed(url);
            return f;
        }

        @Override
        protected void onPostExecute(final Feed feed) {
            if (feed != null) {
                new AlertDialogWrapper.Builder(AddFeedFromURL.this).setTitle("Feed added successfully!").setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddFeedFromURL.this.finish();
                    }
                }).show();
                Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(feed);
                        ArrayList<Feed> subs = new ArrayList<>(realm.where(Feed.class).findAllSorted("order"));
                        subs.add(feed);
                        for (int i = 0; i < subs.size(); i++) {
                            subs.get(i).setOrder(i);
                        }
                        for (Feed f : subs) {
                            realm.insertOrUpdate(f);
                        }
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {

                    }
                });
            } else {
                new SearchSiteTask().execute(url);
            }
        }
    }

    public class SearchSiteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String urlBase = strings[0];
            Document doc = null;
            try {
                String url = urlBase.startsWith("http")? urlBase : "http://" + urlBase;
                doc = Jsoup.connect(url).get();
                Elements links = doc.select("link[type=application/rss+xml]");

                if (links.size() > 0) {
                    String rss_url = links.get(0).attr("abs:href");
                    return rss_url;
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String feed) {
            if (feed == null) {
                new AlertDialogWrapper.Builder(AddFeedFromURL.this).setTitle("Error adding feed! This site may not have an RSS feed available.").setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
            } else {
                new ParseFeedTask().execute(feed);
            }
        }
    }
}
