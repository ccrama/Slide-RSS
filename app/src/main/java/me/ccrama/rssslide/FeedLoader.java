package me.ccrama.rssslide;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Item;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Carlos on 3/17/2017.
 */

public class FeedLoader implements ConversionCallback {
    public boolean nomore;
    public boolean offline;
    public Feed feed;
    Activity context;
    public boolean loading;
    FeedAdapter adapter;

    public FeedLoader(final Feed id) {
        feed = id;
    }

    public void loadMore(Activity context, Feed feed, FeedAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
        LogUtil.v("Loading more!");
        new DownloadXmlTask().execute(feed.url); //todo turn feed name into feed URL
    }

    @Override
    public void onCompletion(int count) {
        Toast.makeText(context,count + " articles loaded", Toast.LENGTH_SHORT).show();
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
        InputStream stream = null;
        // Instantiate the parser
        com.einmalfel.earl.Feed feedBase = null;
        try {
            stream = downloadUrl(urlString);
            feedBase = EarlParser.parseOrThrow(stream, 0);
        } catch (DataFormatException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }


        LogUtil.v("Converting " + feedBase.getItems());

        XMLToRealm.convert(feed, feedBase.getItems(), this, context);
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
