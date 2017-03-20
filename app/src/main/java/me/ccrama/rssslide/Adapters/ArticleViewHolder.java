package me.ccrama.rssslide.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.ccrama.rssslide.Views.HeaderImageLinkView;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.Views.SpoilerRobotoTextView;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ArticleViewHolder extends RecyclerView.ViewHolder {
    public final TextView title;
    public final ImageView icon;
    public final TextView feed;

    public final TextView info;
    public final View menu;
    public final View share;
    public final View hide;
    public final View thumbimage;
    public final View secondMenu;
    public final HeaderImageLinkView leadImage;
    public final View save;
    public final TextView flairText;
    public final SpoilerRobotoTextView body;

    public ArticleViewHolder(View v) {
        super(v);
        title = (SpoilerRobotoTextView) v.findViewById(R.id.title);
        info = (TextView) v.findViewById(R.id.information);
        hide = v.findViewById(R.id.hide);
        menu = v.findViewById(R.id.menu);
        leadImage = (HeaderImageLinkView) v.findViewById(R.id.headerimage);
        secondMenu = v.findViewById(R.id.secondMenu);
        flairText = (TextView) v.findViewById(R.id.text);
        icon = (ImageView) v.findViewById(R.id.icon);
        share = v.findViewById(R.id.share);
        feed = (TextView) v.findViewById(R.id.feed);
        thumbimage = v.findViewById(R.id.thumbimage2);
        save = v.findViewById(R.id.save);
        body = (SpoilerRobotoTextView) v.findViewById(R.id.body);
    }
}
