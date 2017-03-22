package me.ccrama.rssslide.Views;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.cocosw.bottomsheet.BottomSheet;
import com.devspark.robototextview.util.RobotoTypefaceManager;

import java.net.URI;
import java.net.URISyntaxException;

import io.realm.Realm;
import me.ccrama.rssslide.Activities.MainActivity;
import me.ccrama.rssslide.Adapters.ArticleViewHolder;
import me.ccrama.rssslide.Adapters.FeedAdapter;
import me.ccrama.rssslide.BaseApplication;
import me.ccrama.rssslide.FontPreferences;
import me.ccrama.rssslide.Palette;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.Realm.Article;
import me.ccrama.rssslide.Realm.Feed;
import me.ccrama.rssslide.SettingValues;
import me.ccrama.rssslide.Util.TimeUtils;

/**
 * Created by ccrama on 9/19/2015.
 */
public class PopulateArticleViewHolder {

    public PopulateArticleViewHolder() {
    }

    public static int getStyleAttribColorValue(final Context context, final int attribResId,
                                               final int defaultValue) {
        final TypedValue tv = new TypedValue();
        final boolean found = context.getTheme().resolveAttribute(attribResId, tv, true);
        return found ? tv.data : defaultValue;
    }

    public static int getCurrentTintColor(Context v) {
        return getStyleAttribColorValue(v, R.attr.tint, Color.WHITE);

    }

    public static int getWhiteTintColor() {
        return Palette.ThemeEnum.DARK.getTint();
    }

    public void showBottomSheet(final Activity mContext,
                                final Article a, final ArticleViewHolder holder, final FeedAdapter posts, final Feed feed,
                                final RecyclerView recyclerview) {

        int[] attrs = new int[]{R.attr.tint};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable share =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_share, null);
        Drawable saved =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.iconstarfilled,
                        null);
        Drawable hide = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.hide, null);
        Drawable open =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.openexternal, null);

        share.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        saved.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        hide.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        open.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        ta.recycle();

        final BottomSheet.Builder b =
                new BottomSheet.Builder(mContext).title(a.getTitle());


        b.sheet(1, share, "Share link");

        String save = mContext.getString(R.string.btn_save);
        if (a.isStarred()) {
            save = mContext.getString(R.string.comment_unsave);
        }
        b.sheet(2, saved, save);

        if(feed != null) {
            boolean hidden = a.isHidden();
            if (!hidden) {
                b.sheet(3, hide, mContext.getString(R.string.submission_hide));
            } else {
                b.sheet(3, hide, mContext.getString(R.string.submission_unhide));
            }
        }
        b.sheet(4, open, mContext.getString(R.string.submission_link_extern));

        b.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 1: {
                        MainActivity.defaultShareText(a.getTitle(), a.getLink(), mContext);
                    }
                    break;
                    case 2: {
                        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                a.starred = !a.starred;
                                populateArticleViewHolder(holder, a, mContext, posts, recyclerview);
                            }
                        });
                    }
                    break;
                    case 3:
                        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                a.hidden = !a.isHidden();
                                final int index = feed.articles.indexOf(a);
                                if (a.hidden) {
                                    feed.articles.remove(a);
                                }
                                Snackbar s = Snackbar.make(recyclerview, "Post hidden", Snackbar.LENGTH_SHORT);
                                s.setAction("Undo", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                feed.articles.add(index, a);
                                                a.hidden = false;
                                            }
                                        });
                                    }
                                });
                                s.show();
                            }
                        });
                        break;
                    case 4: {
                        MainActivity.defaultShare(a.getLink(), mContext);
                    }
                    break;
                }
            }
        });
        b.show();

    }

    public void populateArticleViewHolder(final ArticleViewHolder holder, final Article article, final Activity context, final FeedAdapter adapter, final RecyclerView list) {

        final Feed feed = Realm.getDefaultInstance().where(Feed.class).equalTo("name", article.feed).findFirst();
        holder.title.setText(article.getTitle()); // title is a spoiler roboto textview so it will format the html


        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheet(context, article, holder, adapter, feed, list);
            }
        });


        holder.feed.setText(feed.name);
        BaseApplication.getImageLoader(context)
                .displayImage(feed.icon, holder.icon);


        final ImageView hideButton = (ImageView) holder.hide;
        if (hideButton != null) {
            if (SettingValues.hideButton) {
                hideButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //todo hide
                    }
                });
            } else {
                hideButton.setVisibility(View.GONE);
            }
        }
        if (article.isStarred()) {
            ((ImageView) holder.save).setColorFilter(
                    ContextCompat.getColor(context, R.color.md_amber_500),
                    PorterDuff.Mode.SRC_ATOP);
        } else {
            ((ImageView) holder.save).setColorFilter(
                    (((holder.itemView.getTag(holder.itemView.getId())) != null
                            && holder.itemView.getTag(holder.itemView.getId()).equals("none"))) ? getCurrentTintColor(context) : getWhiteTintColor(),
                    PorterDuff.Mode.SRC_ATOP);
        }
        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        article.starred = !article.starred;
                        if (article.starred) {
                            ((ImageView) holder.save).setColorFilter(
                                    ContextCompat.getColor(context, R.color.md_amber_500),
                                    PorterDuff.Mode.SRC_ATOP);
                        } else {
                            ((ImageView) holder.save).setColorFilter(
                                    (((holder.itemView.getTag(holder.itemView.getId())) != null
                                            && holder.itemView.getTag(holder.itemView.getId()).equals("none")
                                    )) ? getCurrentTintColor(context) : getWhiteTintColor(),
                                    PorterDuff.Mode.SRC_ATOP);
                        }
                    }
                });
            }
        });

        if (article.starred) {
            ((ImageView) holder.save).setColorFilter(
                    ContextCompat.getColor(context, R.color.md_amber_500),
                    PorterDuff.Mode.SRC_ATOP);
        } else {
            ((ImageView) holder.save).setColorFilter(
                    (((holder.itemView.getTag(holder.itemView.getId())) != null
                            && holder.itemView.getTag(holder.itemView.getId()).equals("none")
                    )) ? getCurrentTintColor(context) : getWhiteTintColor(),
                    PorterDuff.Mode.SRC_ATOP);
        }
        ImageView thumbImage2 = ((ImageView) holder.thumbimage);

        if (holder.leadImage.thumbImage2 == null) {
            holder.leadImage.setThumbnail(thumbImage2);
        }


        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.defaultShareText(article.getTitle(), article.getLink(), context);
            }
        });

        holder.leadImage.setArticle(article);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (SettingValues.actionbarTap) {
                    CreateCardView.toggleActionbar(holder.itemView);
                } else {
                    holder.itemView.findViewById(R.id.menu).callOnClick();
                }

                return true;
            }

        });

        if (feed.accessed < article.created) {
            holder.title.setTextColor(Palette.getColor(feed.name));
        } else {
            holder.title.setTextColor(holder.info.getCurrentTextColor());
        }
        holder.info.setText(getInfoSpannable(article, feed.name, context));

        holder.body.setVisibility(View.VISIBLE);
        String text = article.summary;
        int typef = new FontPreferences(context).getFontTypeComment().getTypeface();
        Typeface typeface;
        if (typef >= 0) {
            typeface = RobotoTypefaceManager.obtainTypeface(context, typef);
        } else {
            typeface = Typeface.DEFAULT;
        }
        holder.body.setTypeface(typeface);

        if (article.seen) {

        }

        if (text != null && !text.isEmpty()) {
            holder.body.setTextHtml(Html.fromHtml(
                    text.substring(0, text.contains("\n") ? text.indexOf("\n") : text.length()))
                    .toString().trim()
                    .replace("<sup>", "<sup><small>")
                    .replace("</sup>", "</small></sup>"), "none ");
            holder.body.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.itemView.callOnClick();
                }
            });
            holder.body.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    holder.menu.callOnClick();
                    return true;
                }
            });
        } else {
            holder.body.setTextHtml("");
        }

        if (article.seen) {
            holder.title.setAlpha(0.54f);
            holder.body.setAlpha(0.54f);
        } else {
            holder.title.setAlpha(1f);
            holder.body.setAlpha(1f);

        }
    }

    private static SpannableStringBuilder getInfoSpannable(Article article, String feed, Context mContext) {
        String spacer = mContext.getString(R.string.submission_properties_seperator);
        SpannableStringBuilder titleString = new SpannableStringBuilder();


        try {
            String time = TimeUtils.getTimeAgo(article.getPublished(), mContext);
            titleString.append(time);
        } catch (Exception e) {
            titleString.append("just now");
        }

        try {
            titleString.append(spacer);
            titleString.append(getDomainName(article.getLink()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (article.getAuthor() != null) {
            titleString.append(spacer);
            titleString.append(article.getAuthor());
        }


        return titleString;
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        if (domain != null && !domain.isEmpty()) {
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } else {
            return "";
        }
    }

}