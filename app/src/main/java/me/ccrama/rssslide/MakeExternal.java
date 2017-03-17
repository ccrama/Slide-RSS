package me.ccrama.rssslide;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by ccrama on 9/28/2015.
 */
public class MakeExternal extends Activity {

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        String url = getIntent().getStringExtra("url");
        if (url != null) {
            try {
                URL u = new URL(url);

                ArrayList<String> domains = new ArrayList<>();
                for (String s : MainActivity.stringToArray(SettingValues.alwaysExternal)) {
                    if (!s.isEmpty()) {
                        s = s.trim();
                        final String finalS = s;
                        domains.add(finalS);
                    }
                }

                if (!domains.contains(u.getHost())) {
                    domains.add(u.getHost());
                }

                SharedPreferences.Editor e = SettingValues.prefs.edit();
                e.putString(SettingValues.PREF_ALWAYS_EXTERNAL, MainActivity.arrayToString(domains));
                e.apply();
                SettingValues.alwaysExternal = SettingValues.prefs.getString(SettingValues.PREF_ALWAYS_EXTERNAL, "");


            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }
        finish();
    }
}
