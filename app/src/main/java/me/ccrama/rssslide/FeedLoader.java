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

/**
 * Created by Carlos on 3/17/2017.
 */

public class FeedLoader implements ConversionCallback {
    public Listing listing;
    public boolean nomore;
    public boolean offline;
    public String feed;
    Activity context;
    FeedAdapter adapter;

    public void loadMore(Activity context, String feed, Listing listing, FeedAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
        new DownloadXmlTask().execute(UserFeeds.urlFromFeed(feed)); //todo turn feed name into feed URL
    }

    @Override
    public void onCompletion(Listing l) {
    }

    private class DownloadXmlTask extends AsyncTask<String, Void, List<Article>> {
        @Override
        protected List<Article> doInBackground(String... urls) {
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
        }
    }

    // Uploads XML from stackoverflow.com, parses it, and combines it with
// HTML markup. Returns HTML string.
    private List<Article> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException, ParseException {
        InputStream stream = null;
        // Instantiate the parser
        FeedParser xmlParser = new FeedParser();
        List<FeedParser.Entry> entries = null;

        try {
            stream = downloadUrl(urlString);
            entries = xmlParser.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }


        XMLToRealm.convert(urlString, entries, this, context);
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
