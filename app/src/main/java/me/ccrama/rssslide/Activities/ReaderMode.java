package me.ccrama.rssslide.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.wuman.jreadability.Readability;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ccrama.rssslide.R;
import me.ccrama.rssslide.SettingValues;
import me.ccrama.rssslide.Util.LogUtil;
import me.ccrama.rssslide.Views.SpoilerRobotoTextView;

public class ReaderMode extends BaseActivityAnim {

    public static final String EXTRA_URL = "url";
    public static String html;
    SpoilerRobotoTextView v;
    String url;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        super.onCreate(savedInstanceState);
        applyColorTheme("");
        setContentView(R.layout.activity_reader);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setupAppBar(R.id.toolbar,"",true,true);

        if (getIntent().hasExtra("url")) {
            url = getIntent().getExtras().getString(EXTRA_URL, "");
            ((Toolbar)findViewById(R.id.toolbar)).setTitle(url);

        }

        v = (SpoilerRobotoTextView) findViewById(R.id.body);


        new AsyncGetArticle().execute();
    }

    public class AsyncGetArticle extends AsyncTask<Void, Void, Void> {
        String title = "";
        String articleText;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (url != null) {
                    URL url = new URL(ReaderMode.this.url);
                    URLConnection con = url.openConnection();
                    Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
                    Matcher m = p.matcher(con.getContentType());
/* If Content-Type doesn't match this pre-conception, choose default and
 * hope for the best. */
                    String charset = m.matches() ? m.group(1) : "ISO-8859-1";
                    Reader r = new InputStreamReader(con.getInputStream(), charset);
                    StringBuilder buf = new StringBuilder();
                    while (true) {
                        int ch = r.read();
                        if (ch < 0) break;
                        buf.append((char) ch);
                    }
                    html = buf.toString();
                    Readability readability = new Readability(html);  // URL
                    readability.init();
                    articleText = readability.outerHtml();
                } else {
                    Readability readability = new Readability(StringEscapeUtils.unescapeJava(html));  // URL
                    readability.init();
                    articleText = readability.outerHtml();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ReaderMode.this.findViewById(R.id.progress).setVisibility(View.GONE);
            if (articleText != null) {
                v.setTextHtml(articleText, "nosub");
                ((Toolbar)findViewById(R.id.toolbar)).setTitle(v.getText().toString().substring(0, v.getText().toString().indexOf("\n")));
            } else {
                new AlertDialogWrapper.Builder(ReaderMode.this)
                        .setTitle(R.string.internal_browser_extracting_error)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        }

        @Override
        protected void onPreExecute() {

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(url != null) {
            inflater.inflate(R.menu.menu_reader, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;
            case R.id.web:
                Intent i = new Intent(this, Website.class);
                i.putExtra(Website.EXTRA_URL, url);
                ReaderMode.this.startActivity(i);
                finish();
                return true;
            case R.id.share:
                MainActivity.defaultShareText(((Toolbar)findViewById(R.id.toolbar)).getTitle().toString(), url, ReaderMode.this);

                return true;

        }
        return false;
    }
}
