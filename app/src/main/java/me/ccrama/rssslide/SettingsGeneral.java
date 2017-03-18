package me.ccrama.rssslide;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsGeneral extends BaseActivityAnim {

    public static boolean searchChanged; //whether or not the subreddit search method changed

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_general);
        setupAppBar(R.id.toolbar, R.string.settings_title_general, true, true);

        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.immersivemode);

            single.setChecked(SettingValues.immersiveMode);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingsTheme.changed = true;
                    SettingValues.immersiveMode = isChecked;
                    SettingValues.prefs.edit()
                            .putBoolean(SettingValues.PREF_IMMERSIVE_MODE, isChecked)
                            .apply();
                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.forcelanguage);

            single.setChecked(SettingValues.overrideLanguage);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingsTheme.changed = true;
                    SettingValues.overrideLanguage = isChecked;
                    SettingValues.prefs.edit()
                            .putBoolean(SettingValues.PREF_OVERRIDE_LANGUAGE, isChecked)
                            .apply();
                }
            });
        }

        findViewById(R.id.viewtype).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(SettingsGeneral.this, SettingsViewType.class);
                startActivity(i);
            }
        });

        //FAB multi choice//
        ((TextView) findViewById(R.id.fab_current)).setText(
                SettingValues.fab ? (SettingValues.fabType == Constants.FAB_DISMISS ? getString(
                        R.string.fab_hide) : getString(R.string.fab_create))
                        : getString(R.string.fab_disabled));

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsGeneral.this, v);
                popup.getMenuInflater().inflate(R.menu.fab_settings, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.disabled:
                                SettingValues.fab = false;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_FAB, false)
                                        .apply();
                                break;
                            case R.id.hide:
                                SettingValues.fab = true;
                                SettingValues.fabType = Constants.FAB_DISMISS;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_FAB, true)
                                        .apply();
                                break;

                        }
                        ((TextView) findViewById(R.id.fab_current)).setText(
                                SettingValues.fab ? (SettingValues.fabType == Constants.FAB_DISMISS
                                        ? getString(R.string.fab_hide)
                                        : getString(R.string.fab_create))
                                        : getString(R.string.fab_disabled));

                        return true;
                    }
                });

                popup.show();
            }
        });

        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.exitcheck);

            single.setChecked(SettingValues.exit);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.exit = isChecked;
                    SettingValues.prefs.edit()
                            .putBoolean(SettingValues.PREF_EXIT, isChecked)
                            .apply();
                }
            });
        }
    }
}
