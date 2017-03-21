package me.ccrama.rssslide;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.core.ImageLoader;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import me.ccrama.rssslide.Util.ImageLoaderUtils;

/**
 * Created by Carlos on 3/20/2017.
 */

public class BaseApplication extends Application {

    public static RealmConfiguration config;
    private static ImageLoader defaultImageLoader;
    public static ImageLoader getImageLoader(Context context) {
        if (defaultImageLoader == null || !defaultImageLoader.isInited()) {
            ImageLoaderUtils.initImageLoader(context.getApplicationContext());
            defaultImageLoader = ImageLoaderUtils.imageLoader;
        }

        return defaultImageLoader;
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        getImageLoader(this).clearMemoryCache();
    }


    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        config = new RealmConfiguration.Builder().name("posts").deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(config);
    }
}