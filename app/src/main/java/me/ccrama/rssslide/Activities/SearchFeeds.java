package me.ccrama.rssslide.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;

import me.ccrama.rssslide.ColorPreferences;
import me.ccrama.rssslide.Fragments.BlankFragment;
import me.ccrama.rssslide.Fragments.FeedFragment;
import me.ccrama.rssslide.Fragments.SearchFragment;
import me.ccrama.rssslide.Palette;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.SettingValues;
import me.ccrama.rssslide.Util.Constants;
import me.ccrama.rssslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.rssslide.Views.ToggleSwipeViewPager;

public class SearchFeeds extends BaseActivity {

    public static final String EXTRA_SEARCH = "search";
    public String search;
    public OverviewPagerAdapter adapter;
    public ToggleSwipeViewPager pager;
    public boolean singleMode;
    public boolean loaded;
    View header;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 2) {
            // Make sure the request was successful
            pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));
        } else if (requestCode == 1) {
            restartTheme();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getDecorView().setBackgroundDrawable(null);
        super.onCreate(savedInstanceState);

        search = getIntent().getExtras().getString(EXTRA_SEARCH, "");
        applyColorTheme("");
        setContentView(R.layout.activity_singlefeed);
        setupSubredditAppBar(R.id.toolbar, search, true, "");

        header = findViewById(R.id.header);
        setResult(3);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());
        pager = (ToggleSwipeViewPager) findViewById(R.id.content_view);
        singleMode = SettingValues.single;
        adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setCurrentItem(1);
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] firstVisibleItems;
                int pastVisiblesItems = 0;
                firstVisibleItems =
                        ((CatchStaggeredGridLayoutManager) ((FeedFragment) (adapter.getCurrentFragment())).rv
                                .getLayoutManager()).findFirstVisibleItemPositions(null);
                if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                    for (int firstVisibleItem : firstVisibleItems) {
                        pastVisiblesItems = firstVisibleItem;
                    }
                }
                if (pastVisiblesItems > 8) {
                    ((FeedFragment) (adapter.getCurrentFragment())).rv.scrollToPosition(0);
                    header.animate()
                            .translationY(header.getHeight())
                            .setInterpolator(new LinearInterpolator())
                            .setDuration(180);
                } else {
                    ((FeedFragment) (adapter.getCurrentFragment())).rv.smoothScrollToPosition(0);
                }
                ((FeedFragment) (adapter.getCurrentFragment())).resetScroll();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        //todo inflater.inflate(R.menu.menu_single_subreddit, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_refresh:
                if (adapter != null && adapter.getCurrentFragment() != null) {
                    ((FeedFragment) adapter.getCurrentFragment()).forceRefresh();
                }
                return true;

            case R.id.search:
               /* todo search
                MaterialDialog.Builder builder =

                        new MaterialDialog.Builder(this).title(R.string.search_title)
                                .alwaysCallInputCallback()
                                .input(getString(R.string.search_msg), "",
                                        new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(MaterialDialog materialDialog,
                                                    CharSequence charSequence) {
                                                term = charSequence.toString();
                                            }
                                        })
                                .neutralText(R.string.search_all)
                                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog,
                                            @NonNull DialogAction dialogAction) {
                                        Intent i = new Intent(FeedViewSingle.this, Search.class);
                                        i.putExtra(Search.EXTRA_TERM, term);
                                        startActivity(i);
                                    }
                                });

                //Add "search current sub" if it is not frontpage/all/random
                if (!subreddit.equalsIgnoreCase("frontpage")
                        && !subreddit.equalsIgnoreCase("all")
                        && !subreddit.equalsIgnoreCase("random")
                        && !subreddit.equalsIgnoreCase("popular")
                        && !subreddit.equals("myrandom")
                        && !subreddit.equals("randnsfw")
                        && !subreddit.equalsIgnoreCase("friends")
                        && !subreddit.equalsIgnoreCase("mod")) {
                    builder.positiveText(getString(R.string.search_subreddit, subreddit))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                                    Intent i = new Intent(FeedViewSingle.this, Search.class);
                                    i.putExtra(Search.EXTRA_TERM, term);
                                    i.putExtra(Search.EXTRA_SUBREDDIT, subreddit);
                                    Log.v(LogUtil.getTag(),
                                            "INTENT SHOWS " + term + " AND " + subreddit);
                                    startActivity(i);
                                }
                            });
                }
                builder.show();*/
                return true;
            case R.id.hide_posts:
                ((FeedFragment) adapter.getCurrentFragment()).clearSeenPosts(false);
                return true;
            case R.id.action_shadowbox:
                /* todo shadowbox
                if (SettingValues.tabletUI) {
                    List<Submission> posts =
                            ((SubmissionsView) ((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i2 = new Intent(this, Shadowbox.class);
                        i2.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                        i2.putExtra(Shadowbox.EXTRA_SUBREDDIT,
                                ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                        startActivity(i2);
                    }
                } else {
                    new AlertDialogWrapper.Builder(this).setTitle(R.string.general_shadowbox_ispro)
                            .setMessage(R.string.pro_upgrade_msg)
                            .setPositiveButton(R.string.btn_yes_exclaim,

                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                            } catch (ActivityNotFoundException e) {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                            }
                                        }
                                    })
                            .setNegativeButton(R.string.btn_no_danks,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            dialog.dismiss();
                                        }
                                    })
                            .show();
                }*/
                return true;
            default:
                return false;
        }
    }

    public int adjustAlpha(float factor) {
        int alpha = Math.round(Color.alpha(Color.BLACK) * factor);
        int red = Color.red(Color.BLACK);
        int green = Color.green(Color.BLACK);
        int blue = Color.blue(Color.BLACK);
        return Color.argb(alpha, red, green, blue);
    }

    public void restartTheme() {
        Intent intent = this.getIntent();
        intent.putExtra(EXTRA_SEARCH, search);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {
        private FeedFragment mCurrentFragment;
        private BlankFragment blankPage;

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                    if (position == 0) {
                        CoordinatorLayout.LayoutParams params =
                                (CoordinatorLayout.LayoutParams) header.getLayoutParams();
                        params.setMargins(header.getWidth() - positionOffsetPixels, 0,
                                -((header.getWidth() - positionOffsetPixels)), 0);
                        header.setLayoutParams(params);
                        if (positionOffsetPixels == 0) {
                            finish();
                            overridePendingTransition(0, R.anim.fade_out);
                        }
                    }

                    if (position == 0) {
                        ((OverviewPagerAdapter) pager.getAdapter()).blankPage.doOffset(
                                positionOffset);
                        pager.setBackgroundColor(adjustAlpha(positionOffset * 0.7f));
                    }
                }

                @Override
                public void onPageSelected(final int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            if (pager.getAdapter() != null) {
                pager.setCurrentItem(1);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {

            if (i == 1) {
                SearchFragment f = new SearchFragment();
                Bundle args = new Bundle();
                args.putString("search", search);
                f.setArguments(args);

                return f;
            } else {
                blankPage = new BlankFragment();
                return blankPage;
            }


        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            doSetPrimary(object, position);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        public void doSetPrimary(Object object, int position) {
            if (object != null
                    && getCurrentFragment() != object
                    && position != 3
                    && object instanceof FeedFragment) {
                mCurrentFragment = ((FeedFragment) object);
                if (mCurrentFragment.dataSet == null && mCurrentFragment.isAdded()) {
                    mCurrentFragment.doAdapter();

                }
            }
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

    }
}
