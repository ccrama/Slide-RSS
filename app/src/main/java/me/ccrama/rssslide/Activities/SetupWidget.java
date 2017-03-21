package me.ccrama.rssslide.Activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioGroup;

import java.util.ArrayList;

import io.realm.Realm;
import me.ccrama.rssslide.Adapters.SideArrayAdapter;
import me.ccrama.rssslide.ColorPreferences;
import me.ccrama.rssslide.FontPreferences;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.Realm.Feed;
import me.ccrama.rssslide.Widget.SubredditWidgetProvider;

/**
 * Created by carlo_000 on 5/4/2016.
 */
public class SetupWidget extends BaseActivity {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        disableSwipeBackLayout();
        getTheme().applyStyle(new FontPreferences(this).getCommentFontStyle().getResId(), true);
        getTheme().applyStyle(new FontPreferences(this).getPostFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        super.onCreate(savedInstanceState);
        assignAppWidgetId();
        doShortcut();
    }

    /**
     * Widget configuration activity,always receives appwidget Id appWidget Id =
     * unique id that identifies your widget analogy : same as setting view id
     * via @+id/viewname on layout but appwidget id is assigned by the system
     * itself
     */
    private void assignAppWidgetId() {
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    View header;

    public void doShortcut() {

        setContentView(R.layout.activity_setup_widget);
        setupAppBar(R.id.toolbar, R.string.widget_creation_title, true, true);
        header = getLayoutInflater().inflate(R.layout.widget_header, null);

        ListView list = (ListView) findViewById(R.id.subs);
        final ArrayList<Feed> sorted = new ArrayList<>(Realm.getDefaultInstance().where(Feed.class).findAll());
        final SideArrayAdapter adapter = new SideArrayAdapter(this, sorted, list);

        list.addHeaderView(header);
        list.setAdapter(adapter);
    }

    public String name;


    /**
     * This method right now displays the widget and starts a Service to fetch
     * remote data from Server
     */
    public void startWidget() {

        SubredditWidgetProvider.setSubFromid(appWidgetId, name, SetupWidget.this);
        int theme = 0;
        switch (((RadioGroup) header.findViewById(R.id.theme)).getCheckedRadioButtonId()) {
            case R.id.dark:
                theme = 1;
                break;
            case R.id.light:
                theme = 2;
                break;
        }
        int view = 0;
        switch (((RadioGroup) header.findViewById(R.id.type)).getCheckedRadioButtonId()) {
            case R.id.big:
                view = 1;
                break;
            case R.id.compact:
                view = 2;
                break;
        }

        SubredditWidgetProvider.setThemeToId(appWidgetId, theme, SetupWidget.this);
        SubredditWidgetProvider.setViewType(appWidgetId, view, SetupWidget.this);

        {
            Intent intent = new Intent();
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(Activity.RESULT_OK, intent);
        }

        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null,
                SetupWidget.this, SubredditWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
        sendBroadcast(intent);

        finish();
    }

}