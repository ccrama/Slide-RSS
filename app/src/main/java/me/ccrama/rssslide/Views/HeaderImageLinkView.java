package me.ccrama.rssslide.Views;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import me.ccrama.rssslide.BaseApplication;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.Realm.Article;
import me.ccrama.rssslide.SettingValues;

/**
 * Created by carlo_000 on 2/7/2016.
 */
public class HeaderImageLinkView extends RelativeLayout {
    public String loadedUrl;
    public boolean lq;
    public ImageView thumbImage2;
    DisplayImageOptions bigOptions = new DisplayImageOptions.Builder().resetViewBeforeLoading(false)
            .cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheInMemory(false)
            .displayer(new FadeInBitmapDisplayer(250))
            .build();
    Activity activity = null;
    float position;
    String lastDone = "";
    public ImageView backdrop;

    public HeaderImageLinkView(Context context) {
        super(context);
        init();
    }

    public void setArticle(final Article article) {
        if (!lastDone.equals(article.getId())) {
            lq = false;
            lastDone = article.getId();
            backdrop.setImageResource(
                    android.R.color.transparent); //reset the image view in case the placeholder is still visible
            thumbImage2.setImageResource(android.R.color.transparent);
            doImageAndText(article);
        }
    }

    public void setThumbnail(ImageView v) {
        thumbImage2 = v;
    }

    public HeaderImageLinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeaderImageLinkView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    boolean thumbUsed;

    public void doImageAndText(final Article article) {

        thumbUsed = false;

        setVisibility(View.VISIBLE);
        String url = "";
        boolean forceThumb = false;
        thumbImage2.setImageResource(android.R.color.transparent);

        if (article.image != null && SettingValues.bigPicEnabled) {
            backdrop.setLayoutParams(
                    new LayoutParams(LayoutParams.MATCH_PARENT,
                            dpToPx(200)));
        } else {
            forceThumb = true;
        }

        if (SettingValues.noImages) {
            setVisibility(View.GONE);
            thumbImage2.setVisibility(View.GONE);
            thumbUsed = false;
        } else {
            url = article.image;
            if (forceThumb) {
                thumbImage2.setVisibility(View.VISIBLE);

                loadedUrl = url;
                BaseApplication.getImageLoader(getContext())
                        .displayImage(url, thumbImage2, bigOptions);

                setVisibility(View.GONE);

            } else {
                loadedUrl = url;
                BaseApplication.getImageLoader(getContext())
                        .displayImage(url, backdrop);
                setVisibility(View.VISIBLE);
                thumbImage2.setVisibility(View.GONE);
            }

        }

        if(article.image == null || article.image.isEmpty()){
            thumbImage2.setVisibility(View.GONE);
        }


    }

    public int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void init() {
        inflate(getContext(), R.layout.header_image_title_view, this);
        this.backdrop = (ImageView) findViewById(R.id.leadimage);
    }
}