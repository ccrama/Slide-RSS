package me.ccrama.rssslide.Adapters;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import me.ccrama.rssslide.Realm.Article;
import me.ccrama.rssslide.Realm.Feed;
import me.ccrama.rssslide.Realm.XMLToRealm;
import me.ccrama.rssslide.Util.ConversionCallback;
import me.ccrama.rssslide.Util.LogUtil;

/**
 * Created by Carlos on 3/17/2017.
 */

public class FeedLoader implements ConversionCallback, DataSet {
    public boolean nomore;
    public boolean offline;
    public Feed feed;
    Activity context;
    public boolean loading;
    FeedAdapter adapter;

    public FeedLoader(final Feed id) {
        feed = id;
    }

    public void loadMore(Activity context, FeedAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
        LogUtil.v("Loading more!");
        new DownloadXmlTask().execute(feed.url); //todo turn feed name into feed URL
    }

    @Override
    public void onCompletion(int count) {
        Toast.makeText(context, count + " articles loaded", Toast.LENGTH_SHORT).show();
    }

    private class DownloadXmlTask extends AsyncTask<String, Void, List<Article>> {
        @Override
        protected List<Article> doInBackground(String... urls) {
            loading = true;
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<Article> articles) {
            loading = false;
            adapter.refreshView();
        }
    }

    // Uploads XML from stackoverflow.com, parses it, and combines it with
// HTML markup. Returns HTML string.
    private List<Article> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException, ParseException {
        SyndFeed f = null;
        try {
            f = new SyndFeedInput().build(new XmlReader(new URL(urlString)));
            XMLToRealm.convertSync(feed, f.getEntries(), this, context);
        } catch (FeedException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Given a string representation of a URL, sets up a connection and gets
// an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
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
}
