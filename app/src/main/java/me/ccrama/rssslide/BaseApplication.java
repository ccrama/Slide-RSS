package me.ccrama.rssslide;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Carlos on 3/20/2017.
 */

public class BaseApplication extends Application {

    public static RealmConfiguration config;

    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        config = new RealmConfiguration.Builder().name("posts").deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(config);
    }
}