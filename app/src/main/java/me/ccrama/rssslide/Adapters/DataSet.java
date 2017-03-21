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

public interface DataSet {
    public void loadMore(Activity context, FeedAdapter adapter);
}