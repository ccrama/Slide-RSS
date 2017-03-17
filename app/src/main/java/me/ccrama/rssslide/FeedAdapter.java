package me.ccrama.rssslide;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import io.realm.RealmRecyclerViewAdapter;


public class FeedAdapter extends RealmRecyclerViewAdapter<Article, RecyclerView.ViewHolder> {

    private final RecyclerView listView;
    public final String feed;
    public Activity context;
    public FeedLoader dataSet;
    private final int LOADING_SPINNER = 5;
    private final int NO_MORE = 3;
    private final int SPACER = 6;
    private ArrayList<String> seen;

    public FeedAdapter(Activity context, FeedLoader dataSet, RecyclerView listView,
                       String feed) {
        super(dataSet.listing.articles, true);
        this.feed = feed.toLowerCase();
        this.listView = listView;
        this.dataSet = dataSet;
        this.context = context;
        this.seen = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        if (position <= 0 && !dataSet.listing.articles.isEmpty()) {
            return SPACER;
        } else if (!dataSet.listing.articles.isEmpty()) {
            position -= (1);
        }
        if (position == dataSet.listing.articles.size()
                && !dataSet.listing.articles.isEmpty()
                && !dataSet.offline
                && !dataSet.nomore) {
            return LOADING_SPINNER;
        } else if (position == dataSet.listing.articles.size() && (dataSet.offline || dataSet.nomore)) {
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

        } else if (i == LOADING_SPINNER) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.loadingmore, viewGroup, false);
            return new SubmissionFooterViewHolder(v);
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
        notifyItemChanged(clicked);
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.setItemAnimator(a);
            }
        }, 500);
    }

    public void refreshView(boolean ignore18) {
        final RecyclerView.ItemAnimator a = listView.getItemAnimator();
        listView.setItemAnimator(null);
        notifyItemChanged(clicked);
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.setItemAnimator(a);
            }
        }, 500);
    }

    public void refreshView(ArrayList<Integer> seen) {
        listView.setItemAnimator(null);
        final RecyclerView.ItemAnimator a = listView.getItemAnimator();

        for (int i : seen) {
            notifyItemChanged(i + 1);
        }
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                listView.setItemAnimator(a);
            }
        }, 500);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder2, final int pos) {

        int i = pos != 0 ? pos - 1 : pos;

        if (holder2 instanceof ArticleViewHolder) {
            final ArticleViewHolder holder = (ArticleViewHolder) holder2;

            final Article obj = getItem(pos);

            holder.itemView.setOnClickListener(new OnSingleClickListener() {
                                                   @Override
                                                   public void onSingleClick(View v) {
                                                       Intent i = new Intent(context, Website.class);
                                                       i.putExtra(Website.EXTRA_URL, obj.getLink());
                                                       context.startActivity(i);
                                                   }
                                               }

            );
            new PopulateArticleViewHolder().populateArticleViewHolder(holder, obj, context, dataSet.listing, feed, listView);
        }
        if (holder2 instanceof SubmissionFooterViewHolder) {
            Handler handler = new Handler();

            final Runnable r = new Runnable() {
                public void run() {
                    notifyItemChanged(dataSet.listing.articles.size()
                            + 1); // the loading spinner to replaced by nomoreposts.xml
                }
            };

            handler.post(r);

            if (holder2.itemView.findViewById(R.id.reload) != null) {
                holder2.itemView.findViewById(R.id.reload)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dataSet.loadMore(context, feed, dataSet.listing, FeedAdapter.this);
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
        if (dataSet.listing.articles == null || dataSet.listing.articles.isEmpty()) {
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