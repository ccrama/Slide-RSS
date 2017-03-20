package me.ccrama.rssslide.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

import me.ccrama.rssslide.Views.CreateCardView;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.SettingValues;

/**
 * Created by ccrama on 9/17/2015.
 */
public class EditCardsLayout extends BaseActivityAnim {
    @Override
    public void onCreate(Bundle savedInstance) {
        overrideRedditSwipeAnywhere();
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);

        applyColorTheme();
        setContentView(R.layout.activity_settings_theme_card);

        setupAppBar(R.id.toolbar, R.string.settings_layout_default, true, true);

        final LinearLayout layout = (LinearLayout) findViewById(R.id.card);
        layout.removeAllViews();
        layout.addView(CreateCardView.CreateView(layout));

        //View type//
        //Cards or List//
        ((TextView) findViewById(R.id.view_current)).setText(CreateCardView.isCard() ? (CreateCardView.isMiddle() ? getString(R.string.mode_centered) : getString(R.string.mode_card)) : CreateCardView.isDesktop() ? getString(R.string.mode_desktop_compact) : getString(R.string.mode_list));

        findViewById(R.id.view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EditCardsLayout.this, v);
                popup.getMenuInflater().inflate(R.menu.card_mode_settings, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.card:
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.LARGE, layout));
                                break;
                            case R.id.list:
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.LIST, layout));
                                break;
                            case R.id.desktop:
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.DESKTOP, layout));
                                break;

                        }
                        ((TextView) findViewById(R.id.view_current)).setText(CreateCardView.isCard() ? (CreateCardView.isMiddle() ? getString(R.string.mode_centered) : getString(R.string.mode_card)) : CreateCardView.isDesktop() ? getString(R.string.mode_desktop_compact) : getString(R.string.mode_list));
                        return true;
                    }
                });

                popup.show();
            }
        });


        //Pic modes//
        final TextView CURRENT_PICTURE = (TextView) findViewById(R.id.picture_current);
        assert CURRENT_PICTURE != null; //it won't be

        if (SettingValues.bigPicCropped) {
            CURRENT_PICTURE.setText(R.string.mode_cropped);
        } else if (SettingValues.bigPicEnabled) {
            CURRENT_PICTURE.setText(R.string.mode_bigpic);
        } else {
            CURRENT_PICTURE.setText(R.string.mode_thumbnail);
        }

        findViewById(R.id.picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EditCardsLayout.this, v);
                popup.getMenuInflater().inflate(R.menu.pic_mode_settings, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bigpic:
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setBigPicEnabled(true, layout));
                            {
                                SharedPreferences.Editor e = SettingValues.prefs.edit();
                                for (Map.Entry<String, ?> map : SettingValues.prefs.getAll().entrySet()) {
                                    if (map.getKey().startsWith("picsenabled")) {
                                        e.remove(map.getKey()); //reset all overridden values
                                    }
                                }
                                e.apply();
                            }
                            break;
                            case R.id.thumbnail:
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setBigPicEnabled(false, layout));
                            {
                                SharedPreferences.Editor e = SettingValues.prefs.edit();
                                for (Map.Entry<String, ?> map : SettingValues.prefs.getAll().entrySet()) {
                                    if (map.getKey().startsWith("picsenabled")) {
                                        e.remove(map.getKey()); //reset all overridden values
                                    }
                                }
                                e.apply();
                            }
                            break;
                        }

                        if (SettingValues.bigPicCropped) {
                            CURRENT_PICTURE.setText(R.string.mode_cropped);
                        } else if (SettingValues.bigPicEnabled) {
                            CURRENT_PICTURE.setText(R.string.mode_bigpic);
                        } else {
                            CURRENT_PICTURE.setText(R.string.mode_thumbnail);
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        final SwitchCompat bigThumbnails = (SwitchCompat) findViewById(R.id.bigThumbnails);
        assert bigThumbnails != null; //def won't be null

        bigThumbnails.setChecked(SettingValues.bigThumbnails);
        bigThumbnails.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.prefs.edit().putBoolean("bigThumbnails", isChecked).apply();
                SettingValues.bigThumbnails = isChecked;

                if (!SettingValues.bigPicCropped && !SettingValues.bigPicCropped) {
                    layout.removeAllViews();

                    layout.addView(CreateCardView.setBigPicEnabled(false, layout));
                    {
                        SharedPreferences.Editor e = SettingValues.prefs.edit();
                        for (Map.Entry<String, ?> map : SettingValues.prefs.getAll().entrySet()) {
                            if (map.getKey().startsWith("picsenabled")) {
                                e.remove(map.getKey()); //reset all overridden values
                            }
                        }
                        e.apply();
                    }
                }
            }
        });

        //Actionbar//
        ((TextView) findViewById(R.id.actionbar_current)).setText(!SettingValues.actionbarVisible ? (SettingValues.actionbarTap ? getString(R.string.tap_actionbar) : getString(R.string.press_actionbar)) : getString(R.string.always_actionbar));

        findViewById(R.id.actionbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EditCardsLayout.this, v);
                popup.getMenuInflater().inflate(R.menu.actionbar_mode, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.always:
                                SettingValues.actionbarTap = false;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ACTIONBAR_TAP, false).apply();
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setActionbarVisible(true, layout));
                                break;
                            case R.id.tap:
                                SettingValues.actionbarTap = true;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ACTIONBAR_TAP, true).apply();
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setActionbarVisible(false, layout));
                                break;
                            case R.id.button:
                                SettingValues.actionbarTap = false;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ACTIONBAR_TAP, false).apply();
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setActionbarVisible(false, layout));
                                break;
                        }
                        ((TextView) findViewById(R.id.actionbar_current)).setText(!SettingValues.actionbarVisible ? (SettingValues.actionbarTap ? getString(R.string.tap_actionbar) : getString(R.string.press_actionbar)) : getString(R.string.always_actionbar));
                        return true;
                    }
                });

                popup.show();
            }
        });


        //Other buttons//
        final AppCompatCheckBox hidebutton = (AppCompatCheckBox) findViewById(R.id.hidebutton);
        layout.findViewById(R.id.hide).setVisibility(SettingValues.hideButton && SettingValues.actionbarVisible ? View.VISIBLE : View.GONE);
        layout.findViewById(R.id.save).setVisibility(SettingValues.saveButton && SettingValues.actionbarVisible ? View.VISIBLE : View.GONE);

        hidebutton.setChecked(SettingValues.hideButton);
        hidebutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.hideButton = isChecked;
                layout.findViewById(R.id.hide).setVisibility(SettingValues.hideButton && SettingValues.actionbarVisible ? View.VISIBLE : View.GONE);
                layout.findViewById(R.id.save).setVisibility(SettingValues.saveButton && SettingValues.actionbarVisible ? View.VISIBLE : View.GONE);
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_HIDEBUTTON, isChecked).apply();

            }
        });
        final AppCompatCheckBox savebutton = (AppCompatCheckBox) findViewById(R.id.savebutton);
        layout.findViewById(R.id.save).setVisibility(SettingValues.saveButton && SettingValues.actionbarVisible ? View.VISIBLE : View.GONE);

        savebutton.setChecked(SettingValues.saveButton);
        savebutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.saveButton = isChecked;
                layout.findViewById(R.id.hide).setVisibility(SettingValues.hideButton && SettingValues.actionbarVisible ? View.VISIBLE : View.GONE);
                layout.findViewById(R.id.save).setVisibility(SettingValues.saveButton && SettingValues.actionbarVisible ? View.VISIBLE : View.GONE);
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SAVE_BUTTON, isChecked).apply();

            }
        });

        //Actionbar//
        //Enable, collapse//
        final SwitchCompat switchThumb = (SwitchCompat) findViewById(R.id.action);
        switchThumb.setChecked(SettingValues.switchThumb);
        switchThumb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout.removeAllViews();
                layout.addView(CreateCardView.setSwitchThumb(isChecked, layout));
            }
        });


    }
}
