package me.ccrama.rssslide.Realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Carlos on 3/26/2017.
 */

public class WebsiteText extends RealmObject {
    @PrimaryKey
    public String url;
    public String body;
    public String title;
}
