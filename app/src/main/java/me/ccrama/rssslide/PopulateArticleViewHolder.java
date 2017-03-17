package me.ccrama.rssslide;


import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.devspark.robototextview.util.RobotoTypefaceManager;

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
                                final Article a, final ArticleViewHolder holder, final Listing posts,
                                final String baseSub, final RecyclerView recyclerview) {

        int[] attrs = new int[]{R.attr.tint};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        /* todo menu
        Drawable share =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_menu_share, null);
        final Drawable sub =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.sub, null);
        Drawable saved =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.iconstarfilled,
                        null);
        Drawable hide = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.hide, null);
        final Drawable report =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.report, null);
        Drawable copy =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_content_copy,
                        null);
        final Drawable readLater =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.save, null);
        Drawable open =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.openexternal, null);
        Drawable link = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.link, null);
        Drawable reddit =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.commentchange,
                        null);
        Drawable filter =
                ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.filter, null);

        profile.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        sub.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        saved.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        hide.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        report.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        copy.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        open.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        link.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        reddit.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        readLater.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        filter.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        ta.recycle();

        final BottomSheet.Builder b =
                new BottomSheet.Builder(mContext).title(Html.fromHtml(submission.getTitle()));


        final boolean isReadLater = mContext instanceof PostReadLater;
        final boolean isAddedToReadLaterList = ReadLater.isToBeReadLater(submission);
        if (Authentication.didOnline) {
            b.sheet(1, profile, "/u/" + submission.getAuthor())
                    .sheet(2, sub, "/r/" + submission.getSubredditName());
            String save = mContext.getString(R.string.btn_save);
            if (ActionStates.isSaved(submission)) {
                save = mContext.getString(R.string.comment_unsave);
            }
            if (Authentication.isLoggedIn) {
                b.sheet(3, saved, save);

            }
        }

        if (isAddedToReadLaterList) {
            b.sheet(28, readLater, "Mark As Read");
        } else {
            b.sheet(28, readLater, "Read later");
        }

        if (Authentication.didOnline) {
            if (Authentication.isLoggedIn) {
                b.sheet(12, report, mContext.getString(R.string.btn_report));
            }
        }

        if (submission.getSelftext() != null && !submission.getSelftext().isEmpty() && full) {
            b.sheet(25, copy, mContext.getString(R.string.submission_copy_text));
        }

        boolean hidden = submission.isHidden();
        if (!full && Authentication.didOnline) {
            if (!hidden) {
                b.sheet(5, hide, mContext.getString(R.string.submission_hide));
            } else {
                b.sheet(5, hide, mContext.getString(R.string.submission_unhide));
            }
        }
        b.sheet(7, open, mContext.getString(R.string.submission_link_extern));

        b.sheet(4, link, mContext.getString(R.string.submission_share_permalink))
                .sheet(8, reddit, mContext.getString(R.string.submission_share_reddit_url));
        if ((mContext instanceof MainActivity) || (mContext instanceof SubredditView)) {
            b.sheet(10, filter, mContext.getString(R.string.filter_content));
        }

        b.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 1: {
                        Intent i = new Intent(mContext, Profile.class);
                        i.putExtra(Profile.EXTRA_PROFILE, submission.getAuthor());
                        mContext.startActivity(i);
                    }
                    break;
                    case 2: {
                        Intent i = new Intent(mContext, SubredditView.class);
                        i.putExtra(SubredditView.EXTRA_SUBREDDIT, submission.getSubredditName());
                        mContext.startActivityForResult(i, 14);
                    }
                    break;
                    case 10:


                        String[] choices;
                        final String flair = submission.getSubmissionFlair().getText() != null
                                ? submission.getSubmissionFlair().getText() : "";
                        if (flair.isEmpty()) {
                            choices = new String[]{
                                    mContext.getString(R.string.filter_posts_sub,
                                            submission.getSubredditName()),
                                    mContext.getString(R.string.filter_posts_user,
                                            submission.getAuthor()),
                                    mContext.getString(R.string.filter_posts_urls,
                                            submission.getDomain()),
                                    mContext.getString(R.string.filter_open_externally,
                                            submission.getDomain())
                            };

                            chosen = new boolean[]{
                                    Arrays.asList(SettingValues.subredditFilters.toLowerCase()
                                            .split(",")).contains(
                                            submission.getSubredditName().toLowerCase()),
                                    Arrays.asList(SettingValues.userFilters.toLowerCase()
                                            .split(",")).contains(
                                            submission.getAuthor().toLowerCase()), Arrays.asList(
                                    SettingValues.domainFilters.toLowerCase().split(",")).contains(
                                    submission.getDomain().toLowerCase()), Arrays.asList(
                                    SettingValues.alwaysExternal.toLowerCase().split(",")).contains(
                                    submission.getDomain().toLowerCase())
                            };
                            oldChosen = chosen.clone();
                        } else {
                            choices = new String[]{
                                    mContext.getString(R.string.filter_posts_sub,
                                            submission.getSubredditName()),
                                    mContext.getString(R.string.filter_posts_user,
                                            submission.getAuthor()),
                                    mContext.getString(R.string.filter_posts_urls,
                                            submission.getDomain()),
                                    mContext.getString(R.string.filter_open_externally,
                                            submission.getDomain()),
                                    mContext.getString(R.string.filter_posts_flair, flair, baseSub)
                            };
                        }
                        ;
                        chosen = new boolean[]{
                                Arrays.asList(SettingValues.subredditFilters.toLowerCase()
                                        .split(",")).contains(
                                        submission.getSubredditName().toLowerCase()), Arrays.asList(
                                SettingValues.userFilters.toLowerCase().split(",")).contains(
                                submission.getAuthor().toLowerCase()), Arrays.asList(
                                SettingValues.domainFilters.toLowerCase().split(",")).contains(
                                submission.getDomain().toLowerCase()), Arrays.asList(
                                SettingValues.alwaysExternal.toLowerCase().split(",")).contains(
                                submission.getDomain().toLowerCase()), Arrays.asList(
                                SettingValues.flairFilters.toLowerCase().split(",")).contains(
                                baseSub + ":" + flair)
                        };
                        oldChosen = chosen.clone();

                        new AlertDialogWrapper.Builder(mContext).setTitle(R.string.filter_title)
                                .alwaysCallMultiChoiceCallback()
                                .setMultiChoiceItems(choices, chosen,
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which,
                                                    boolean isChecked) {
                                                chosen[which] = isChecked;
                                            }
                                        })
                                .setPositiveButton(R.string.filter_btn,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                boolean filtered = false;
                                                SharedPreferences.Editor e =
                                                        SettingValues.prefs.edit();
                                                if (chosen[0] && chosen[0] != oldChosen[0]) {
                                                    SettingValues.subredditFilters =
                                                            SettingValues.subredditFilters
                                                                    + (
                                                                    (SettingValues.subredditFilters.isEmpty()
                                                                            || SettingValues.subredditFilters
                                                                            .endsWith(",")) ? ""
                                                                            : ",")
                                                                    + submission.getSubredditName();
                                                    filtered = true;
                                                    e.putString(
                                                            SettingValues.PREF_SUBREDDIT_FILTERS,
                                                            SettingValues.subredditFilters);
                                                    PostMatch.subreddits = null;
                                                } else if (!chosen[0]
                                                        && chosen[0] != oldChosen[0]) {
                                                    SettingValues.subredditFilters =
                                                            SettingValues.subredditFilters.replace(
                                                                    submission.getSubredditName(),
                                                                    "");
                                                    filtered = false;
                                                    e.putString(
                                                            SettingValues.PREF_SUBREDDIT_FILTERS,
                                                            SettingValues.subredditFilters);
                                                    e.apply();
                                                    PostMatch.subreddits = null;
                                                }
                                                if (chosen[1] && chosen[1] != oldChosen[1]) {
                                                    SettingValues.userFilters =
                                                            SettingValues.userFilters + ((
                                                                    SettingValues.userFilters.isEmpty()
                                                                            || SettingValues.userFilters
                                                                            .endsWith(",")) ? ""
                                                                    : ",") + submission.getAuthor();
                                                    filtered = true;
                                                    e.putString(SettingValues.PREF_USER_FILTERS,
                                                            SettingValues.userFilters);
                                                    PostMatch.users = null;
                                                } else if (!chosen[1]
                                                        && chosen[1] != oldChosen[1]) {
                                                    SettingValues.userFilters =
                                                            SettingValues.userFilters.replace(
                                                                    submission.getAuthor(), "");
                                                    filtered = false;
                                                    e.putString(SettingValues.PREF_USER_FILTERS,
                                                            SettingValues.userFilters);
                                                    e.apply();
                                                    PostMatch.users = null;
                                                }
                                                if (chosen[2] && chosen[2] != oldChosen[2]) {
                                                    SettingValues.domainFilters =
                                                            SettingValues.domainFilters + ((
                                                                    SettingValues.domainFilters.isEmpty()
                                                                            || SettingValues.domainFilters
                                                                            .endsWith(",")) ? ""
                                                                    : ",") + submission.getDomain();
                                                    filtered = true;
                                                    e.putString(SettingValues.PREF_DOMAIN_FILTERS,
                                                            SettingValues.domainFilters);
                                                    PostMatch.domains = null;
                                                } else if (!chosen[2]
                                                        && chosen[2] != oldChosen[2]) {
                                                    SettingValues.domainFilters =
                                                            SettingValues.domainFilters.replace(
                                                                    submission.getDomain(), "");
                                                    filtered = false;
                                                    e.putString(SettingValues.PREF_DOMAIN_FILTERS,
                                                            SettingValues.domainFilters);
                                                    e.apply();
                                                    PostMatch.domains = null;
                                                }
                                                if (chosen[3] && chosen[3] != oldChosen[3]) {
                                                    SettingValues.alwaysExternal =
                                                            SettingValues.alwaysExternal + ((
                                                                    SettingValues.alwaysExternal.isEmpty()
                                                                            || SettingValues.alwaysExternal
                                                                            .endsWith(",")) ? ""
                                                                    : ",") + submission.getDomain();
                                                    e.putString(SettingValues.PREF_ALWAYS_EXTERNAL,
                                                            SettingValues.alwaysExternal);
                                                    e.apply();
                                                } else if (!chosen[3]
                                                        && chosen[3] != oldChosen[3]) {
                                                    SettingValues.alwaysExternal =
                                                            SettingValues.alwaysExternal.replace(
                                                                    submission.getDomain(), "");
                                                    e.putString(SettingValues.PREF_ALWAYS_EXTERNAL,
                                                            SettingValues.alwaysExternal);
                                                    e.apply();
                                                }
                                                if (chosen.length > 4) {
                                                    if (chosen[4] && chosen[4] != oldChosen[4]) {
                                                        SettingValues.flairFilters =
                                                                SettingValues.flairFilters + ((
                                                                        SettingValues.flairFilters.isEmpty()
                                                                                || SettingValues.flairFilters
                                                                                .endsWith(",")) ? ""
                                                                        : ",") + (baseSub
                                                                        + ":"
                                                                        + flair);
                                                        e.putString(
                                                                SettingValues.PREF_FLAIR_FILTERS,
                                                                SettingValues.flairFilters);
                                                        e.apply();
                                                        PostMatch.flairs = null;
                                                        filtered = true;
                                                    } else if (!chosen[4]
                                                            && chosen[4] != oldChosen[4]) {
                                                        SettingValues.flairFilters =
                                                                SettingValues.flairFilters.toLowerCase()
                                                                        .replace((baseSub
                                                                                        + ":"
                                                                                        + flair).toLowerCase(),
                                                                                "");
                                                        e.putString(
                                                                SettingValues.PREF_FLAIR_FILTERS,
                                                                SettingValues.flairFilters);
                                                        e.apply();
                                                        PostMatch.flairs = null;
                                                    }
                                                }
                                                if (filtered) {
                                                    e.apply();
                                                    PostMatch.domains = null;
                                                    PostMatch.subreddits = null;
                                                    PostMatch.users = null;
                                                    ArrayList<Contribution> toRemove =
                                                            new ArrayList<>();
                                                    for (Contribution s : posts) {
                                                        if (s instanceof Submission
                                                                && PostMatch.doesMatch(
                                                                (Submission) s)) {
                                                            toRemove.add(s);
                                                        }
                                                    }

                                                    OfflineSubreddit s =
                                                            OfflineSubreddit.getSubreddit(baseSub,
                                                                    false, mContext);

                                                    for (Contribution remove : toRemove) {
                                                        final int pos = posts.indexOf(remove);
                                                        posts.remove(pos);
                                                        if (baseSub != null) {
                                                            s.hideMulti(pos);
                                                        }
                                                    }
                                                    s.writeToMemoryNoStorage();
                                                    recyclerview.getAdapter()
                                                            .notifyDataSetChanged();
                                                }
                                            }
                                        })
                                .setNegativeButton(R.string.btn_cancel, null)
                                .show();
                        break;

                    case 3:
                        saveSubmission(submission, mContext, holder, full);
                        break;
                    case 5: {
                        hideSubmission(submission, posts, baseSub, recyclerview, mContext);
                    }
                    break;
                    case 7:
                        LinkUtil.openExternally(submission.getUrl(), mContext, true);
                        if (submission.isNsfw() && !SettingValues.storeNSFWHistory) {
                            //Do nothing if the post is NSFW and storeNSFWHistory is not enabled
                        } else if (SettingValues.storeHistory) {
                            HasSeen.addSeen(submission.getFullName());
                        }
                        break;
                    case 28:
                        if (!isAddedToReadLaterList) {
                            ReadLater.setReadLater(submission, true);
                            Snackbar s = Snackbar.make(holder.itemView, "Added to read later!",
                                    Snackbar.LENGTH_SHORT);
                            View view = s.getView();
                            TextView tv = (TextView) view.findViewById(
                                    android.support.design.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.setAction(R.string.btn_undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ReadLater.setReadLater(submission, false);
                                    Snackbar s2 = Snackbar.make(holder.itemView,
                                            "Removed from read later", Snackbar.LENGTH_SHORT);
                                    View view2 = s2.getView();
                                    TextView tv2 = (TextView) view2.findViewById(
                                            android.support.design.R.id.snackbar_text);
                                    tv2.setTextColor(Color.WHITE);
                                    s2.show();
                                }
                            });
                            if (NetworkUtil.isConnected(mContext)) {
                                new CommentCacheAsync(Arrays.asList(submission), mContext,
                                        CommentCacheAsync.SAVED_SUBMISSIONS,
                                        new boolean[]{true, true}).executeOnExecutor(
                                        AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                            s.show();
                        } else {
                            ReadLater.setReadLater(submission, false);
                            if (isReadLater || !Authentication.didOnline) {
                                final int pos = posts.indexOf(submission);
                                posts.remove(submission);

                                recyclerview.getAdapter()
                                        .notifyItemRemoved(holder.getAdapterPosition());

                                Snackbar s2 =
                                        Snackbar.make(holder.itemView, "Removed from read later",
                                                Snackbar.LENGTH_SHORT);
                                View view2 = s2.getView();
                                TextView tv2 = (TextView) view2.findViewById(
                                        android.support.design.R.id.snackbar_text);
                                tv2.setTextColor(Color.WHITE);
                                s2.setAction(R.string.btn_undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        posts.add(pos, (T) submission);
                                        recyclerview.getAdapter().notifyDataSetChanged();
                                    }
                                });
                            } else {
                                Snackbar s2 =
                                        Snackbar.make(holder.itemView, "Removed from read later",
                                                Snackbar.LENGTH_SHORT);
                                View view2 = s2.getView();
                                TextView tv2 = (TextView) view2.findViewById(
                                        android.support.design.R.id.snackbar_text);
                                s2.show();
                            }
                            OfflineSubreddit.newSubreddit(CommentCacheAsync.SAVED_SUBMISSIONS)
                                    .deleteFromMemory(submission.getFullName());

                        }
                        break;
                    case 4:
                        Reddit.defaultShareText(Html.fromHtml(submission.getTitle()).toString(),
                                StringEscapeUtils.escapeHtml4(submission.getUrl()), mContext);
                        break;
                    case 12:
                        reportReason = "";
                        new MaterialDialog.Builder(mContext).input(
                                mContext.getString(R.string.input_reason_for_report), null, true,
                                new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        reportReason = input.toString();
                                    }
                                })
                                .alwaysCallInputCallback()
                                .positiveText(R.string.btn_report)
                                .negativeText(R.string.btn_cancel)
                                .onNegative(null)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(MaterialDialog dialog, DialogAction which) {
                                        new AsyncTask<Void, Void, Void>() {
                                            @Override
                                            protected Void doInBackground(Void... params) {
                                                try {
                                                    new AccountManager(
                                                            Authentication.reddit).report(
                                                            submission, reportReason);
                                                } catch (ApiException e) {
                                                    e.printStackTrace();
                                                }
                                                return null;
                                            }

                                            @Override
                                            protected void onPostExecute(Void aVoid) {
                                                if (holder.itemView != null) {
                                                    try {
                                                        Snackbar s = Snackbar.make(holder.itemView,
                                                                R.string.msg_report_sent, Snackbar.LENGTH_SHORT);
                                                        View view = s.getView();
                                                        TextView tv = (TextView) view.findViewById(
                                                                android.support.design.R.id.snackbar_text);
                                                        tv.setTextColor(Color.WHITE);
                                                        s.show();
                                                    } catch(Exception ignored){

                                                    }
                                                }
                                            }
                                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    }
                                })
                                .show();

                        break;
                    case 8:
                        Reddit.defaultShareText(Html.fromHtml(submission.getTitle()).toString(),
                                "https://reddit.com" + submission.getPermalink(), mContext);
                        break;
                    case 6: {
                        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(
                                Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Link", submission.getUrl());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mContext, R.string.submission_link_copied,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                    case 25:
                        final TextView showText = new TextView(mContext);
                        showText.setText(StringEscapeUtils.unescapeHtml4(
                                submission.getTitle() + "\n\n" + submission.getSelftext()));
                        showText.setTextIsSelectable(true);
                        int sixteen = Reddit.dpToPxVertical(24);
                        showText.setPadding(sixteen, 0, sixteen, 0);
                        AlertDialogWrapper.Builder builder =
                                new AlertDialogWrapper.Builder(mContext);
                        builder.setView(showText)
                                .setTitle("Select text to copy")
                                .setCancelable(true)
                                .setPositiveButton("COPY SELECTED",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String selected = showText.getText()
                                                        .toString()
                                                        .substring(showText.getSelectionStart(),
                                                                showText.getSelectionEnd());
                                                ClipboardManager clipboard =
                                                        (ClipboardManager) mContext.getSystemService(
                                                                Context.CLIPBOARD_SERVICE);
                                                ClipData clip =
                                                        ClipData.newPlainText("Selftext", selected);
                                                clipboard.setPrimaryClip(clip);

                                                Toast.makeText(mContext,
                                                        R.string.submission_comment_copied,
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                .setNegativeButton(R.string.btn_cancel, null)
                                .setNeutralButton("COPY ALL",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                ClipboardManager clipboard =
                                                        (ClipboardManager) mContext.getSystemService(
                                                                Context.CLIPBOARD_SERVICE);
                                                ClipData clip = ClipData.newPlainText("Selftext",
                                                        Html.fromHtml(submission.getTitle()
                                                                + "\n\n"
                                                                + submission.getSelftext()));
                                                clipboard.setPrimaryClip(clip);

                                                Toast.makeText(mContext,
                                                        R.string.submission_comment_copied,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                .show();
                        break;
                }
            }
        });
        b.show();
        */
    }

    public void populateArticleViewHolder(final ArticleViewHolder holder, final Article article, final Activity context, final Listing parent, final String feed, final RecyclerView list) {

        holder.title.setText(article.getTitle()); // title is a spoiler roboto textview so it will format the html


        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheet(context, article, holder, parent, feed, list);
            }
        });

        holder.feed.setText(feed);
        MainActivity.getImageLoader(context)
                .displayImage(UserFeeds.getIcon(feed), holder.icon);


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
                //todo star
            }
        });

        ImageView thumbImage2 = ((ImageView) holder.thumbimage);

        if (holder.leadImage.thumbImage2 == null) {
            holder.leadImage.setThumbnail(thumbImage2);
        }

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

        holder.info.setText(getInfoSpannable(article, feed, context));

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

        holder.body.setTextHtml(Html.fromHtml(
                text.substring(0, text.contains("\n") ? text.indexOf("\n") : text.length()))
                .toString()
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

        titleString.append(spacer);


        if (SettingValues.showDomain) {
            titleString.append(spacer);
            titleString.append(article.getLink());
        }


        return titleString;
    }

}