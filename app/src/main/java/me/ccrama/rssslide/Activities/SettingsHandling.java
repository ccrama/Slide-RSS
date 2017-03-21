package me.ccrama.rssslide.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import me.ccrama.rssslide.R;
import me.ccrama.rssslide.SettingValues;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsHandling extends BaseActivityAnim {

    public ArrayList<String> domains = new ArrayList<>();
    EditText domain;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_handling);
        setupAppBar(R.id.toolbar, R.string.settings_link_handling, true, true);

        TextView web = (TextView) findViewById(R.id.browser);

        //todo web stuff
        {
            SwitchCompat read = (SwitchCompat) findViewById(R.id.readability);

            read.setChecked(SettingValues.readabilityDefault);
            read.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.readabilityDefault = isChecked;
                    SettingValues.prefs.edit()
                            .putBoolean(SettingValues.PREF_READ_DEFAULT, isChecked)
                            .apply();
                }
            });
            if (!SettingValues.tabletUI) {
                read.setEnabled(false);
            }
        }


        /* todo maybe implement CCT
        ((TextView) findViewById(R.id.browser)).setText(
                SettingValues.web ? (SettingValues.customtabs ? getString(
                        R.string.settings_link_chrome)
                        : getString(R.string.handling_internal_browser))
                        : getString(R.string.handling_external_browser));

        findViewById(R.id.select_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsHandling.this, v);
                popup.getMenuInflater().inflate(R.menu.browser_type, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.chrome:
                                SettingValues.customtabs = true;
                                SettingValues.web = true;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREFS_WEB, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_CUSTOMTABS, true)
                                        .apply();
                                break;
                            case R.id.internal:
                                SettingValues.customtabs = false;
                                SettingValues.web = true;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREFS_WEB, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_CUSTOMTABS, false)
                                        .apply();
                                break;
                            case R.id.external:
                                SettingValues.web = false;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREFS_WEB, false)
                                        .apply();
                                break;
                        }
                        ((TextView) findViewById(R.id.browser)).setText(
                                SettingValues.web ? (SettingValues.customtabs ? getString(
                                        R.string.settings_link_chrome)
                                        : getString(R.string.handling_internal_browser))
                                        : getString(R.string.handling_external_browser));

                        return true;
                    }
                });
                popup.show();
            }
        });*/

        domain = (EditText) findViewById(R.id.domain);
        domain.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SettingValues.alwaysExternal =
                            SettingValues.alwaysExternal + ", " + domain.getText().toString();
                    domain.setText("");
                    updateFilters();
                }
                return false;
            }
        });
        updateFilters();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor e = SettingValues.prefs.edit();

        e.putString(SettingValues.PREF_ALWAYS_EXTERNAL, MainActivity.arrayToString(domains));
        e.apply();

        ShouldOpenExternally.externalDomain = null;

        SettingValues.alwaysExternal =
                SettingValues.prefs.getString(SettingValues.PREF_ALWAYS_EXTERNAL, "");
    }

    public void updateFilters() {
        domains = new ArrayList<>();

        ((LinearLayout) findViewById(R.id.domainlist)).removeAllViews();
        for (String s : SettingValues.alwaysExternal.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
            if (!s.isEmpty()) {
                s = s.trim();
                final String finalS = s;
                domains.add(finalS);
                final View t = getLayoutInflater().inflate(R.layout.account_textview,
                        ((LinearLayout) findViewById(R.id.domainlist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(s);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        domains.remove(finalS);
                        SettingValues.alwaysExternal = MainActivity.arrayToString(domains);
                        updateFilters();
                    }
                });
                ((LinearLayout) findViewById(R.id.domainlist)).addView(t);

            }
        }
    }

}