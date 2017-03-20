/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ccrama.rssslide.DragSort;

import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import me.ccrama.rssslide.Activities.BaseActivityAnim;
import me.ccrama.rssslide.ColorPreferences;
import me.ccrama.rssslide.Realm.Feed;
import me.ccrama.rssslide.Activities.MainActivity;
import me.ccrama.rssslide.Palette;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.Activities.SettingsTheme;
import me.ccrama.rssslide.UserFeeds;

public class ReorderSubreddits extends BaseActivityAnim {

    private ArrayList<Feed> subs;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private String input;
    public static final String MULTI_REDDIT = "/m/";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reorder_subs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.add:
                new MaterialDialog.Builder(ReorderSubreddits.this)
                        .alwaysCallInputCallback()
                        .input("Feed URL", null,
                                new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(@NonNull MaterialDialog dialog,
                                                        CharSequence input) {
                                        final EditText editText = dialog.getInputEditText();
                                        if (input.length() >= 3 && input.length() <= 20) {
                                            dialog.getActionButton(DialogAction.POSITIVE)
                                                    .setEnabled(true);
                                        }
                                    }
                                })
                        .inputType(InputType.TYPE_TEXT_VARIATION_URI)
                        .positiveText("Add feed")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull final MaterialDialog d,
                                                @NonNull DialogAction which) {
                                new ParseFeedTask().execute(d.getInputEditText().getText().toString());
                            }
                        })
                        .negativeText(R.string.btn_cancel)
                        .show();
                return true;
        }
        return false;
    }

    @Override
    public void onPause() {
        try {
            UserFeeds.setFeeds(subs);
            SettingsTheme.changed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (isMultiple) {
            chosen = new ArrayList<>();
            doOldToolbar();
            adapter.notifyDataSetChanged();
            isMultiple = false;
        } else {
            super.onBackPressed();
        }
    }

    private ArrayList<Feed> chosen = new ArrayList<>();
    HashMap<String, Boolean> isSubscribed;
    private boolean isMultiple;
    private int done = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_sort);
        setupAppBar(R.id.toolbar, R.string.settings_manage_subscriptions, false, true);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        isSubscribed = new HashMap<>();
        doShowSubs();
    }

    public void doShowSubs() {
        subs = new ArrayList(UserFeeds.getAllUserFeeds());
        recyclerView = (RecyclerView) findViewById(R.id.subslist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);

        DragSortRecycler dragSortRecycler = new DragSortRecycler();
        dragSortRecycler.setViewHandleId();
        dragSortRecycler.setFloatingAlpha();
        dragSortRecycler.setAutoScrollSpeed();
        dragSortRecycler.setAutoScrollWindow();

        dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
            @Override
            public void onItemMoved(int from, int to) {
                if (to == subs.size()) {
                    to -= 1;
                }
                Feed item = subs.remove(from);
                subs.add(to, item);
                adapter.notifyDataSetChanged();
            }
        });

        dragSortRecycler.setOnDragStateChangedListener(
                new DragSortRecycler.OnDragStateChangedListener() {
                    @Override
                    public void onDragStart() {
                    }

                    @Override
                    public void onDragStop() {
                    }
                });

        recyclerView.addItemDecoration(dragSortRecycler);
        recyclerView.addOnItemTouchListener(dragSortRecycler);
        recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());
        dragSortRecycler.setViewHandleId();

        if (subs != null && !subs.isEmpty()) {
            adapter = new CustomAdapter(subs);
            //  adapter.setHasStableIds(true);
            recyclerView.setAdapter(adapter);
        } else {
            subs = new ArrayList<>();
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
                    diff += dy;
                } else {
                    diff = 0;
                }

            }
        });
    }

    public int diff;

    public void doAddSub(Feed f) {
            subs.add(f);
            adapter = new CustomAdapter(subs);
            recyclerView.setAdapter(adapter);
    }


    public void doOldToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.VISIBLE);
    }

    public class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final ArrayList<Feed> items;

        public CustomAdapter(ArrayList<Feed> items) {
            this.items = items;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 2) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.spacer, parent, false);
                return new SpacerViewHolder(v);
            }
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.subforsublistdrag, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == items.size()) {
                return 2;
            }
            return 1;
        }

        public void doNewToolbar() {
            mToolbar.setVisibility(View.GONE);
            mToolbar = (Toolbar) findViewById(R.id.toolbar2);
            mToolbar.setTitle(
                    getResources().getQuantityString(R.plurals.reorder_selected, chosen.size(),
                            chosen.size()));
            mToolbar.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialogWrapper.Builder b =
                            new AlertDialogWrapper.Builder(ReorderSubreddits.this).setTitle(
                                    R.string.reorder_remove_title)
                                    .setPositiveButton(R.string.btn_remove,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    for (Feed s : chosen) {
                                                        int index = subs.indexOf(s);
                                                        subs.remove(index);
                                                        adapter.notifyItemRemoved(index);
                                                    }
                                                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                                                        @Override
                                                        public void execute(Realm realm) {
                                                            for (Feed s : chosen) {
                                                                s.deleteFromRealm();
                                                            }
                                                        }
                                                    });
                                                    isMultiple = false;
                                                    chosen = new ArrayList<>();
                                                    doOldToolbar();

                                                }
                                            });
                    b.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
                }
            });
            mToolbar.findViewById(R.id.top).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (Feed s : chosen) {
                        int index = subs.indexOf(s);
                        subs.remove(index);
                        subs.add(0, s);
                    }
                    isMultiple = false;
                    doOldToolbar();
                    chosen = new ArrayList<>();
                    notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(0);
                }
            });
        }

        int[] textColorAttr = new int[]{R.attr.font};
        TypedArray ta = obtainStyledAttributes(textColorAttr);
        int textColor = ta.getColor(0, Color.BLACK);

        public void updateToolbar() {
            mToolbar.setTitle(
                    getResources().getQuantityString(R.plurals.reorder_selected, chosen.size(),
                            chosen.size()));
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holderB, final int position) {
            if (holderB instanceof ViewHolder) {
                final ViewHolder holder = (ViewHolder) holderB;
                final Feed origPos = items.get(position);
                holder.text.setText(origPos.name);

                if (chosen.contains(origPos)) {
                    holder.itemView.setBackgroundColor(
                            Palette.getDarkerColor(holder.text.getCurrentTextColor()));
                    holder.text.setTextColor(Color.WHITE);
                } else {
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                    holder.text.setTextColor(textColor);
                }

                MainActivity.getImageLoader(ReorderSubreddits.this).displayImage(origPos.icon, ((ImageView) holder.icon));
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!isMultiple) {
                            isMultiple = true;
                            chosen = new ArrayList<>();
                            chosen.add(origPos);

                            doNewToolbar();
                            holder.itemView.setBackgroundColor(
                                    Palette.getDarkerColor(Palette.getDefaultAccent()));
                            holder.text.setTextColor(Color.WHITE);
                        } else if (chosen.contains(origPos)) {
                            holder.itemView.setBackgroundColor(Color.TRANSPARENT);

                            //set the color of the text back to what it should be
                            holder.text.setTextColor(textColor);

                            chosen.remove(origPos);

                            if (chosen.isEmpty()) {
                                isMultiple = false;
                                doOldToolbar();
                            }
                        } else {
                            chosen.add(origPos);
                            holder.itemView.setBackgroundColor(
                                    Palette.getDarkerColor(Palette.getDefaultAccent()));
                            holder.text.setTextColor(textColor);
                            updateToolbar();
                        }
                        return true;
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isMultiple) {
                            new AlertDialogWrapper.Builder(ReorderSubreddits.this).setItems(
                                    new CharSequence[]{
                                            getString(R.string.reorder_move),
                                            getString(R.string.btn_delete)
                                    }, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 1) {
                                                AlertDialogWrapper.Builder b =
                                                        new AlertDialogWrapper.Builder(
                                                                ReorderSubreddits.this).setTitle(
                                                                R.string.reorder_remove_title)
                                                                .setPositiveButton(
                                                                        R.string.btn_remove,
                                                                        new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(
                                                                                    DialogInterface dialog,
                                                                                    int which) {
                                                                                subs.remove(
                                                                                        items.get(
                                                                                                position));
                                                                                adapter.notifyItemRemoved(
                                                                                        position);
                                                                            }
                                                                        })
                                                                .setNegativeButton(
                                                                        R.string.btn_cancel,
                                                                        new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(
                                                                                    DialogInterface dialog,
                                                                                    int which) {

                                                                            }
                                                                        });

                                                b.show();
                                            } else {
                                                Feed s = items.get(holder.getAdapterPosition());
                                                int index = subs.indexOf(s);
                                                subs.remove(index);
                                                subs.add(0, s);

                                                notifyItemMoved(holder.getAdapterPosition(), 0);
                                                recyclerView.smoothScrollToPosition(0);
                                            }
                                        }
                                    }).show();
                        } else {
                            if (chosen.contains(origPos)) {
                                holder.itemView.setBackgroundColor(Color.TRANSPARENT);

                                //set the color of the text back to what it should be
                                int[] textColorAttr = new int[]{R.attr.font};
                                TypedArray ta = obtainStyledAttributes(textColorAttr);
                                holder.text.setTextColor(ta.getColor(0, Color.BLACK));
                                ta.recycle();

                                chosen.remove(origPos);
                                updateToolbar();

                                if (chosen.isEmpty()) {
                                    isMultiple = false;
                                    doOldToolbar();
                                }
                            } else {
                                chosen.add(origPos);
                                holder.itemView.setBackgroundColor(
                                        Palette.getDarkerColor(Palette.getDefaultAccent()));
                                holder.text.setTextColor(Color.WHITE);
                                updateToolbar();
                            }
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return items.size() + 1;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            final TextView text;
            final ImageView icon;

            public ViewHolder(View itemView) {
                super(itemView);
                text = (TextView) itemView.findViewById(R.id.name);
                icon = (ImageView) itemView.findViewById(R.id.icon);
            }
        }

        public class SpacerViewHolder extends RecyclerView.ViewHolder {
            public SpacerViewHolder(View itemView) {
                super(itemView);
                itemView.findViewById(R.id.height)
                        .setLayoutParams(
                                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        MainActivity.dpToPxVertical(88)));

            }
        }

    }

    private class ParseFeedTask extends AsyncTask<String, Void, Feed> {

        String url;

        @Override
        protected Feed doInBackground(String... input) {
            url = input[0];
            Feed f = UserFeeds.doAddFeed(url);
            return f;
        }

        @Override
        protected void onPostExecute(final Feed feed) {
            if (feed != null) {
                new AlertDialogWrapper.Builder(ReorderSubreddits.this).setTitle("Feed added successfully!").setPositiveButton("Ok!", null).show();
                Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealmOrUpdate(feed);
                    }
                }, new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        doAddSub(feed);
                    }
                });
            } else {
                new SearchSiteTask().execute(url);
            }
        }
    }

    private class SearchSiteTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String urlBase = strings[0];
            Document doc = null;
            try {
                String url = new URL(urlBase).toString();
                doc = Jsoup.connect(url).get();
                Elements links = doc.select("link[type=application/rss+xml]");

                if (links.size() > 0) {
                    String rss_url = links.get(0).attr("abs:href").toString();
                    return rss_url;
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String feed) {
            if (feed == null) {
                new AlertDialogWrapper.Builder(ReorderSubreddits.this).setTitle("Error adding feed! Make sure you have entered the URL correctly").setPositiveButton("Ok!", null).show();
            } else {
                new ParseFeedTask().execute(feed);
            }
        }
    }
}
