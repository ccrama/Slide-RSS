package me.ccrama.rssslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import me.ccrama.rssslide.Activities.ReaderMode;
import me.ccrama.rssslide.Activities.ShouldOpenExternally;
import me.ccrama.rssslide.Activities.Website;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.Realm.Article;
import me.ccrama.rssslide.SettingValues;
import me.ccrama.rssslide.Util.OnSingleClickListener;
import me.ccrama.rssslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.rssslide.Views.CreateCardView;
import me.ccrama.rssslide.Views.PopulateArticleViewHolder;


public class FeedAdapter extends RealmRecyclerViewAdapter<Article, RecyclerView.ViewHolder> {

    private final RecyclerView listView;
    private final SwipeRefreshLayout refreshLayout;
    public Activity context;
    public DataSet dataSet;
    private final int NO_MORE = 3;
    private final int SPACER = 6;

    public FeedAdapter(Activity context, DataSet dataSet, RecyclerView listView,
                       SwipeRefreshLayout refreshLayout) {
        super(dataSet.getData(), true);
        this.listView = listView;
        this.dataSet = dataSet;
        this.context = context;
        this.refreshLayout = refreshLayout;
        if(dataSet instanceof FeedLoader) {
            ((FeedLoader)dataSet).cancelNotifs();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0 && !dataSet.getData().isEmpty()) {
            return SPACER;
        } else if (!dataSet.getData().isEmpty()) {
            position -= (1);
        }
        if (position == dataSet.getData().size()
                && !dataSet.getData().isEmpty()) {
            return NO_MORE;
        } else if (position == dataSet.getData().size()) {
            return NO_MORE;
        }
        int SUBMISSION = 1;
        return SUBMISSION;
    }

    int tag = 1;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        tag++;

        if (i == SPACER) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.spacer, viewGroup, false);
            return new SpacerViewHolder(v);

        } else if (i == NO_MORE) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.nomoreposts, viewGroup, false);
            return new SubmissionFooterViewHolder(v);
        } else {
            View v = CreateCardView.CreateView(viewGroup);
            return new ArticleViewHolder(v);
        }
    }

    int clicked;

    public void refreshView() {
        final RecyclerView.ItemAnimator a = listView.getItemAnimator();
        listView.setItemAnimator(null);
        notifyDataSetChanged();
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.setItemAnimator(a);
            }
        }, 500);
        refreshLayout.setRefreshing(false);

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder2, final int pos) {

        int i = pos != 0 ? pos - 1 : pos;

        if (holder2 instanceof ArticleViewHolder) {
            final ArticleViewHolder holder = (ArticleViewHolder) holder2;

            final Article obj = getItem(i);

            holder.itemView.setOnClickListener(new OnSingleClickListener() {
                                                   @Override
                                                   public void onSingleClick(View v) {

                                                       if (ShouldOpenExternally.openExternal(obj.getLink())) {
                                                           Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(obj.getLink()));
                                                           context.startActivity(browserIntent);
                                                       } else {
                                                           if (SettingValues.readabilityDefault) {
                                                               Intent i = new Intent(context, ReaderMode.class);
                                                               i.putExtra("url", obj.getLink());
                                                               context.startActivity(i);
                                                           } else {
                                                               Intent i = new Intent(context, Website.class);
                                                               i.putExtra(Website.EXTRA_URL, obj.getLink());
                                                               //todo do color i.putExtra(Website.EXTRA_COLOR, Palette.getColor(feed.name));
                                                               context.startActivity(i);
                                                           }
                                                           Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                                                               @Override
                                                               public void execute(Realm realm) {
                                                                   obj.setSeen();
                                                                   realm.copyToRealmOrUpdate(obj);
                                                                   notifyDataSetChanged();
                                                               }
                                                           });
                                                       }
                                                   }
                                               }

            );
            new PopulateArticleViewHolder().populateArticleViewHolder(holder, obj, context, this, listView);
        }
        if (holder2 instanceof SubmissionFooterViewHolder) {
            Handler handler = new Handler();

            final Runnable r = new Runnable() {
                public void run() {
                    notifyItemChanged(dataSet.getData().size()
                            + 1); // the loading spinner to replaced by nomoreposts.xml
                }
            };

            handler.post(r);

            if (holder2.itemView.findViewById(R.id.reload) != null) {
                holder2.itemView.findViewById(R.id.reload)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dataSet.loadMore(context, FeedAdapter.this);
                            }
                        });

            }
        }
        if (holder2 instanceof SpacerViewHolder) {
            View header = (context).findViewById(R.id.header);

            int height = header.getHeight();

            if (height == 0) {
                header.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                height = header.getMeasuredHeight();
                holder2.itemView.findViewById(R.id.height)
                        .setLayoutParams(
                                new LinearLayout.LayoutParams(holder2.itemView.getWidth(), height));

                if (listView.getLayoutManager() instanceof CatchStaggeredGridLayoutManager) {
                    CatchStaggeredGridLayoutManager.LayoutParams layoutParams =
                            new CatchStaggeredGridLayoutManager.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, height);
                    layoutParams.setFullSpan(true);
                    holder2.itemView.setLayoutParams(layoutParams);
                }
            } else {
                holder2.itemView.findViewById(R.id.height)
                        .setLayoutParams(
                                new LinearLayout.LayoutParams(holder2.itemView.getWidth(), height));
                if (listView.getLayoutManager() instanceof CatchStaggeredGridLayoutManager) {
                    CatchStaggeredGridLayoutManager.LayoutParams layoutParams =
                            new CatchStaggeredGridLayoutManager.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, height);
                    layoutParams.setFullSpan(true);
                    holder2.itemView.setLayoutParams(layoutParams);
                }
            }
        }
    }


    public class SubmissionFooterViewHolder extends RecyclerView.ViewHolder {
        public SubmissionFooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {
        if (dataSet.getData() == null || dataSet.getData().isEmpty()) {
            return 0;
        } else {
            return super.getItemCount() + 2; // Always account for footer
        }
    }

    public void performClick(int adapterPosition) {
        if (listView != null) {
            RecyclerView.ViewHolder holder =
                    listView.findViewHolderForLayoutPosition(adapterPosition);
            if (holder != null) {
                View view = holder.itemView;
                if (view != null) {
                    view.performClick();
                }
            }
        }
    }
}