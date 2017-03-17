package me.ccrama.rssslide;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Carlos on 3/17/2017.
 */

public class Feed extends RealmObject {
    public String name;
    public String icon;

    @PrimaryKey
    public String url;
}
