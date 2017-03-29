package me.ccrama.rssslide.Fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.itemanimators.SlideUpAlphaAnimator;

import io.realm.Realm;
import me.ccrama.rssslide.Activities.BaseActivity;
import me.ccrama.rssslide.Realm.Category;
import me.ccrama.rssslide.Realm.FeedWrapper;
import me.ccrama.rssslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.rssslide.ColorPreferences;
import me.ccrama.rssslide.Util.Constants;
import me.ccrama.rssslide.Views.CreateCardView;
import me.ccrama.rssslide.Adapters.FeedAdapter;
import me.ccrama.rssslide.Adapters.FeedLoader;
import me.ccrama.rssslide.Activities.FeedViewSingle;
import me.ccrama.rssslide.Util.LogUtil;
import me.ccrama.rssslide.Activities.MainActivity;
import me.ccrama.rssslide.Palette;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.Realm.Article;
import me.ccrama.rssslide.Realm.Feed;
import me.ccrama.rssslide.SettingValues;
import me.ccrama.rssslide.Adapters.ToolbarScrollHideHandler;

public class FeedFragment extends Fragment {
    public static int adapterPosition;
    private static int currentPosition;
    public RecyclerView rv;
    public FeedAdapter adapter;
    public FeedWrapper id;
    public boolean main;
    public boolean forced;
    int diff;
    boolean forceLoad;
    private FloatingActionButton fab;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private int totalItemCount;
    public SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        final int currentOrientation = newConfig.orientation;

        final CatchStaggeredGridLayoutManager mLayoutManager =
                (CatchStaggeredGridLayoutManager) rv.getLayoutManager();

        mLayoutManager.setSpanCount(getNumColumns(currentOrientation));
    }

    Runnable mLongPressRunnable;
    GestureDetector detector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener());
    float origY;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(),
                new ColorPreferences(inflater.getContext()).getThemeSubreddit(id.getTitle()));
        final View v = ((LayoutInflater) contextThemeWrapper.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_verticalcontent,
                container, false);

        if (getActivity() instanceof MainActivity) {
            v.findViewById(R.id.back).setBackgroundResource(0);
        }
        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));

        rv.setHasFixedSize(true);

        final RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager =
                createLayoutManager(getNumColumns(getResources().getConfiguration().orientation));

        if (!(getActivity() instanceof FeedViewSingle)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                v.findViewById(R.id.back).setBackground(null);
            } else {
                v.findViewById(R.id.back).setBackgroundDrawable(null);
            }
        }
        rv.setLayoutManager(mLayoutManager);
        rv.setItemAnimator(new SlideUpAlphaAnimator());
        rv.getLayoutManager().scrollToPosition(0);

        mSwipeRefreshLayout =
                (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(id.getTitle(), getContext()));

        /**
         * If using List view mode, we need to remove the start margin from the SwipeRefreshLayout.
         * The scrollbar style of "outsideInset" creates a 4dp padding around it. To counter this,
         * change the scrollbar style to "insideOverlay" when list view is enabled.
         * To recap: this removes the margins from the start/end so list view is full-width.
         */
        if (SettingValues.defaultCardView == CreateCardView.CardEnum.LIST) {
            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                params.setMarginStart(0);
            } else {
                MarginLayoutParamsCompat.setMarginStart(params, 0);
            }
            rv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            mSwipeRefreshLayout.setLayoutParams(params);
        }

        /**
         * If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
         * So, we estimate the height of the header in dp.
         * If the view type is "single" (and therefore "commentPager"), we need a different offset
         */
        final int HEADER_OFFSET = (SettingValues.single || getActivity() instanceof FeedViewSingle)
                ? Constants.SINGLE_HEADER_VIEW_OFFSET : Constants.TAB_HEADER_VIEW_OFFSET;

        mSwipeRefreshLayout.setProgressViewOffset(false, HEADER_OFFSET - Constants.PTR_OFFSET_TOP,
                HEADER_OFFSET + Constants.PTR_OFFSET_BOTTOM);

        if (SettingValues.fab) {
            fab = (FloatingActionButton) v.findViewById(R.id.post_floating_action_button);

            fab.setImageResource(R.drawable.hide);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearSeenPosts(false);
                }
            });
            final Handler handler = new Handler();
            fab.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    detector.onTouchEvent(event);
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        origY = event.getY();
                        handler.postDelayed(mLongPressRunnable, android.view.ViewConfiguration.getLongPressTimeout());
                    }
                    if (((event.getAction() == MotionEvent.ACTION_MOVE) && Math.abs(event.getY() - origY) > fab.getHeight() / 2) || (event.getAction() == MotionEvent.ACTION_UP)) {
                        handler.removeCallbacks(mLongPressRunnable);
                    }
                    return false;
                }
            });
            mLongPressRunnable = new Runnable() {
                public void run() {
                    fab.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    clearSeenPosts(true);
                    Snackbar s = Snackbar.make(rv,
                            getResources().getString(R.string.posts_hidden_forever),
                            Snackbar.LENGTH_LONG);
                    View view = s.getView();
                    TextView tv = (TextView) view.findViewById(
                            android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                }
            };
        } else {
            v.findViewById(R.id.post_floating_action_button).setVisibility(View.GONE);
        }
        if (fab != null) fab.show();

        header = getActivity().findViewById(R.id.header);
        resetScroll();

        if (MainActivity.shouldLoad == null
                || id == null
                || (MainActivity.shouldLoad != null
                && MainActivity.shouldLoad.getTitle().equals(id.getTitle()))
                || !(getActivity() instanceof MainActivity)) {
            doAdapter();
        }
        return v;
    }

    View header;

    ToolbarScrollHideHandler toolbarScroll;

    @NonNull
    private RecyclerView.LayoutManager createLayoutManager(final int numColumns) {
        return new CatchStaggeredGridLayoutManager(numColumns,
                CatchStaggeredGridLayoutManager.VERTICAL);
    }

    public static int getNumColumns(final int orientation) {
        final int numColumns;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.tabletUI) {
            numColumns = MainActivity.dpWidth;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT
                && SettingValues.dualPortrait) {
            numColumns = 2;
        } else {
            numColumns = 1;
        }
        return numColumns;
    }

    public FeedLoader dataSet;

    @Override
    public void setMenuVisibility(final boolean visible) {
        if (!visible) {
            if(viewed){
                Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if(dataSet.feed instanceof Feed) {
                            ((Feed)dataSet.feed).setSeen();
                            realm.insertOrUpdate(((Feed)dataSet.feed));
                        }
                    }
                });
                LogUtil.v("Hiding");
                viewed = false;
            }
        }

        super.setMenuVisibility(visible);
    }

    public void doAdapter() {
        viewed = true;
        dataSet = new FeedLoader(id, getActivity());
        adapter = new FeedAdapter(getActivity(), dataSet, rv, mSwipeRefreshLayout);
        adapter.setHasStableIds(true);

        rv.setAdapter(adapter);
       if(dataSet.feed.getArticles().isEmpty()) {
           dataSet.loadMore(getActivity(), adapter);
           mSwipeRefreshLayout.post(new Runnable() {
               @Override
               public void run() {
                   mSwipeRefreshLayout.setRefreshing(true);
               }
           });
       }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    public void clearSeenPosts(boolean forever) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        String idb= bundle.getString("id", "");
        LogUtil.v("Loading " + idb);
        id = Realm.getDefaultInstance().where(Feed.class).equalTo("name", idb).findFirst();
        if(id == null){
            id = Realm.getDefaultInstance().where(Category.class).equalTo("name", idb).findFirst();
        }
        main = bundle.getBoolean("main", false);
        forceLoad = bundle.getBoolean("load", false);

    }

    public Article currentArticle;

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null && adapterPosition > 0 && currentPosition == adapterPosition) {
            if (id.getArticles().size() >= adapterPosition - 1
                    && id.getArticles().get(adapterPosition - 1) == currentArticle) {
                adapter.performClick(adapterPosition);
                adapterPosition = -1;
            }
        }
    }

    boolean viewed;

    public static void datachanged(int adaptorPosition2) {
        adapterPosition = adaptorPosition2;
    }

    private void refresh() {
        forced = true;
        dataSet.loadMore(getActivity(), adapter);
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if(dataSet.feed instanceof Feed) {
                    ((Feed)dataSet.feed).setSeen();
                    realm.insertOrUpdate(((Feed)dataSet.feed));
                }
            }
        });
    }

    public void forceRefresh() {
        rv.scrollToPosition(0);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                refresh();
            }
        });
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void resetScroll() {
        if (toolbarScroll == null) {
            toolbarScroll =
                    new ToolbarScrollHideHandler(((BaseActivity) getActivity()).mToolbar, header) {
                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            if (!dataSet.loading) {
                                visibleItemCount = rv.getLayoutManager().getChildCount();
                                totalItemCount = rv.getLayoutManager().getItemCount();

                                int[] firstVisibleItems;
                                firstVisibleItems =
                                        ((CatchStaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(
                                                null);
                                if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                                    for (int firstVisibleItem : firstVisibleItems) {
                                        pastVisiblesItems = firstVisibleItem;
                                        if (SettingValues.scrollSeen
                                                && pastVisiblesItems > 0
                                                && SettingValues.storeHistory) {
                                            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    Article a = dataSet.feed.getArticles().get(pastVisiblesItems - 1);
                                                    a.setSeen();
                                                    realm.copyToRealmOrUpdate(a);
                                                }
                                            });
                                        }
                                    }
                                }
                            }

                /*
                if(dy <= 0 && !down){
                    (getActivity()).findViewById(R.id.header).animate().translationY(((BaseActivity)getActivity()).mToolbar.getTop()).setInterpolator(new AccelerateInterpolator()).start();
                    down = true;
                } else if(down){
                    (getActivity()).findViewById(R.id.header).animate().translationY(((BaseActivity)getActivity()).mToolbar.getTop()).setInterpolator(new AccelerateInterpolator()).start();
                    down = false;
                }*///todo For future implementation instead of scrollFlags

                            if (recyclerView.getScrollState()
                                    == RecyclerView.SCROLL_STATE_DRAGGING) {
                                diff += dy;
                            } else {
                                diff = 0;
                            }
                            if (fab != null) {
                                if (dy <= 0 && fab.getId() != 0 && SettingValues.fab) {
                                    if (recyclerView.getScrollState()
                                            != RecyclerView.SCROLL_STATE_DRAGGING
                                            || diff < -fab.getHeight() * 2) {
                                        fab.show();
                                    }
                                } else {
                                    fab.hide();
                                }
                            }

                        }

                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (getActivity() instanceof MainActivity
                                    && (SettingValues.subredditSearchMethod
                                    == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                                    || SettingValues.subredditSearchMethod
                                    == Constants.SUBREDDIT_SEARCH_METHOD_BOTH)
                                    && ((MainActivity) getContext()).findViewById(
                                    R.id.toolbar_search).getVisibility() == View.VISIBLE) {
                                ((MainActivity) getContext()).findViewById(
                                        R.id.close_search_toolbar).performClick();
                            }
                        }
                    };
            rv.addOnScrollListener(toolbarScroll);
        } else {
            toolbarScroll.reset = true;
        }
    }

    public static void currentPosition(int adapterPosition) {
        currentPosition = adapterPosition;
    }

}