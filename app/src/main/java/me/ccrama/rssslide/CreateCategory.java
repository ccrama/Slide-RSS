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

package me.ccrama.rssslide;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles creation of Multireddits.
 */
public class CreateCategory extends BaseActivityAnim {
/* todo
    private ArrayList<UserFeeds.Feed> feeds;
    private boolean delete = false;
    private CustomAdapter adapter;
    private EditText title;
    private RecyclerView recyclerView;
    private String input;
    private String old;
    public static final String EXTRA_MULTI = "multi";

    //Shows a dialog with all feeds and allows the user to select which ones to include in the Category
    private String[] all;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overrideRedditSwipeAnywhere();

        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_createcategory);
        setupAppBar(R.id.toolbar, "", true, true);

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDialog();
            }
        });
        title = (EditText) findViewById(R.id.name);

     feeds = new ArrayList<>();
        if (getIntent().hasExtra(EXTRA_MULTI)) {
            String multi = getIntent().getExtras().getString(EXTRA_MULTI);
            old = multi;
            title.setText(multi.replace("%20", " "));
            for (String c : UserFeeds.categories) {
                if (c.split("<<")[0].equals(multi)) {
                    for (UserFeeds.Feed feed : c.feeds) {
                        feeds.add(feed);
                    }
                }
            }
        }
        recyclerView = (RecyclerView) findViewById(R.id.subslist);

        adapter = new CustomAdapter(feeds);
        //  adapter.setHasStableIds(true);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void showSelectDialog() {

        // List of all subreddits of the multi
        List<String> sorted = new ArrayList<>();
        List<String> multiSubs = new ArrayList<>();
        multiSubs.addAll(subs);
        sorted.addAll(subs);

        //Add all user subs that aren't already on the list
        for (String s : UserSubscriptions.sort(UserSubscriptions.getSubscriptions(this))) {
            if (!sorted.contains(s)) sorted.add(s);
        }

        //Array of all subs
        all = new String[sorted.size()];
        //Contains which subreddits are checked
        boolean[] checked = new boolean[all.length];


        //Remove special subreddits from list and store it in "all"
        int i = 0;
        for (String s : sorted) {
            if (!s.equals("all") && !s.equals("frontpage") && !s.contains("+") && !s.contains(".") && !s.contains("/m/")) {
                all[i] = s;
                i++;
            }
        }

        //Remove empty entries & store which subreddits are checked
        List<String> list = new ArrayList<>();
        i = 0;
        for (String s : all) {
            if (s != null && !s.isEmpty()) {
                list.add(s);
                if (multiSubs.contains(s)) {
                    checked[i] = true;
                }
                i++;
            }
        }

        //Convert List back to Array
        all = list.toArray(new String[list.size()]);

        final ArrayList<String> toCheck = new ArrayList<>();
        toCheck.addAll(subs);
        new AlertDialogWrapper.Builder(this)
                .setMultiChoiceItems(all, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (!isChecked) {
                            toCheck.remove(all[which]);
                        } else {
                            toCheck.add(all[which]);
                        }
                        Log.v(LogUtil.getTag(), "Done with " + all[which]);
                    }
                })
                .setTitle(R.string.multireddit_selector)
                .setPositiveButton(getString(R.string.btn_add).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        subs = toCheck;
                        adapter = new CustomAdapter(subs);
                        recyclerView.setAdapter(adapter);

                    }
                })
                .setNegativeButton(R.string.reorder_add_subreddit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new MaterialDialog.Builder(CreateCategory.this)
                                .title(R.string.reorder_add_subreddit)
                                .inputRangeRes(2, 21, R.color.md_red_500)
                                .alwaysCallInputCallback()
                                .input(getString(R.string.reorder_subreddit_name), null, false, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence raw) {
                                        input = raw.toString().replaceAll("\\s", ""); //remove whitespace from input
                                    }
                                })
                                .positiveText(R.string.btn_add)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(MaterialDialog dialog, DialogAction which) {
                                        new AsyncGetSubreddit().execute(input);
                                    }
                                })
                                .negativeText(R.string.btn_cancel)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(MaterialDialog dialog, DialogAction which) {

                                    }
                                }).show();
                    }
                })
                .show();
    }

    private class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {
        @Override
        public void onPostExecute(Subreddit subreddit) {
            if (subreddit != null || input.equalsIgnoreCase("friends") || input.equalsIgnoreCase("mod")) {
                subs.add(input);
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(subs.size());
            }
        }

        @Override
        protected Subreddit doInBackground(final String... params) {
            try {
                if (subs.contains(params[0])) return null;
                return Authentication.reddit.getSubreddit(params[0]);
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new AlertDialogWrapper.Builder(CreateCategory.this)
                                    .setTitle(R.string.subreddit_err)
                                    .setMessage(getString(R.string.subreddit_err_msg, params[0]))
                                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();

                                        }
                                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {

                                }
                            }).show();
                        } catch (Exception ignored) {

                        }
                    }
                });

                return null;
            }
        }
    }


    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private final ArrayList<UserFeeds.Feed> items;

        public CustomAdapter(ArrayList<UserFeeds.Feed> items) {
            this.items = items;
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subforsublist, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            final UserFeeds.Feed origPos = items.get(position);
            holder.text.setText(origPos.name);

            ((ImageView) holder.itemView.findViewById(R.id.color)).setImageDrawable(null);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialogWrapper.Builder(CreateCategory.this).setTitle(R.string.really_remove_subreddit_title)
                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    subs.remove(origPos);
                                    adapter = new CustomAdapter(subs);
                                    recyclerView.setAdapter(adapter);
                                }
                            })
                            .setNegativeButton(R.string.btn_no, null)
                            .show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            final TextView text;

            public ViewHolder(View itemView) {
                super(itemView);

                text = (TextView) itemView.findViewById(R.id.name);


            }
        }
    }


    public class SaveMulti extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String multiName = title.getText().toString().replace(" ", "").replace("-", "_");
                Pattern validName = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9_]{2,20}$");
                Matcher m = validName.matcher(multiName);

                if (!m.matches()) {
                    Log.v(LogUtil.getTag(), "Invalid multi name");
                    throw new IllegalArgumentException(multiName);
                }
                if (delete) {
                    Log.v(LogUtil.getTag(), "Deleting");
                    new MultiRedditManager(Authentication.reddit).delete(old);
                } else {
                    if (old != null && !old.isEmpty() && !old.replace(" ", "").equals(multiName)) {
                        Log.v(LogUtil.getTag(), "Renaming");
                        new MultiRedditManager(Authentication.reddit).rename(old, multiName);
                    }
                    Log.v(LogUtil.getTag(), "Create or Update, Name: " + multiName);
                    new MultiRedditManager(Authentication.reddit).createOrUpdate(new MultiRedditUpdateRequest.Builder(Authentication.name, multiName).subreddits(subs).build());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v(LogUtil.getTag(), "Update Subreddits");
                            new UserSubscriptions.SyncMultireddits(CreateCategory.this).execute();
                        }
                    });
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Context context = getApplicationContext();
                        CharSequence text = getString(R.string.multi_saved_successfully);
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                });
            } catch (final NetworkException | ApiException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialogWrapper.Builder(CreateCategory.this)
                                .setTitle(R.string.err_title)
                                .setMessage(e instanceof ApiException ? getString(R.string.misc_err) + ": " + ((ApiException) e).getExplanation() + "\n" + getString(R.string.misc_retry) : getString(R.string.misc_err))
                                .setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                }).create().show();
                    }
                });
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialogWrapper.Builder(CreateCategory.this)
                                .setTitle(R.string.multireddit_invalid_name)
                                .setMessage(R.string.multireddit_invalid_name_msg)
                                .setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                }).create().show();
                    }
                });
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_multi, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                new AlertDialogWrapper.Builder(CreateCategory.this)
                        .setTitle(getString(R.string.delete_multireddit_title, title.getText().toString()))
                        .setMessage(R.string.cannot_be_undone)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new MaterialDialog.Builder(CreateCategory.this)
                                        .title(R.string.deleting)
                                        .progress(true, 100)
                                        .content(R.string.misc_please_wait)
                                        .cancelable(false)
                                        .show();

                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        try {
                                            new MultiRedditManager(Authentication.reddit).delete(old);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    new UserSubscriptions.SyncMultireddits(CreateCategory.this).execute();
                                                }
                                            });

                                        } catch (final Exception e) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    new AlertDialogWrapper.Builder(CreateCategory.this)
                                                            .setTitle(R.string.err_title)
                                                            .setMessage(e instanceof ApiException ? getString(R.string.misc_err) + ": " + ((ApiException) e).getExplanation() + "\n" + getString(R.string.misc_retry) : getString(R.string.misc_err))
                                                            .setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    finish();
                                                                }
                                                            }).create().show();
                                                }
                                            });
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }
                                }.execute();
                            }
                        }).setNegativeButton(R.string.btn_cancel, null).show();
                return true;
            case R.id.save:
                if (title.getText().toString().isEmpty()) {
                    new AlertDialogWrapper.Builder(CreateCategory.this)
                            .setTitle(R.string.multireddit_title_empty)
                            .setMessage(R.string.multireddit_title_empty_msg)
                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    title.requestFocus();
                                }
                            }).show();
                } else if (subs.isEmpty()) {
                    new AlertDialogWrapper.Builder(CreateCategory.this)
                            .setTitle(R.string.multireddit_no_subs)
                            .setMessage(R.string.multireddit_no_subs_msg)
                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                } else {
                    new SaveMulti().execute();
                }
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }
*/
}
