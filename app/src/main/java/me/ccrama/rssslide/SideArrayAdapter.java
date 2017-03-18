package me.ccrama.rssslide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ccrama on 8/17/2015.
 */
public class SideArrayAdapter extends ArrayAdapter<Feed> {
    private final List<Feed> objects;
    private Filter filter;
    public ArrayList<Feed> fitems;
    public ListView parentL;
    public boolean openInSubView = true;

    public SideArrayAdapter(Context context, ArrayList<Feed> objects, ListView view) {
        super(context, 0, objects);
        this.objects = objects;
        filter = new SubFilter();
        fitems = new ArrayList<>(objects);
        parentL = view;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public Filter getFilter() {

        if (filter == null) {
            filter = new SubFilter();
        }
        return filter;
    }

    int height;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (position < fitems.size()) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.subforsublist, parent, false);

            final String sub;
            final String base = fitems.get(position).name;
            sub = fitems.get(position).name;
            final TextView t = ((TextView) convertView.findViewById(R.id.name));
            t.setText(sub);

            if (height == 0) {
                final View finalConvertView = convertView;
                convertView.getViewTreeObserver()
                        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                height = finalConvertView.getHeight();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    finalConvertView.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                } else {
                                    finalConvertView.getViewTreeObserver()
                                            .removeGlobalOnLayoutListener(this);
                                }
                            }
                        });
            }

            final String subreddit = (sub.contains("+") || sub.contains("/m/")) ? sub
                    : SantitizeField.sanitizeString(
                    sub.replace(getContext().getString(R.string.search_goto) + " ", ""));

            ImageView back = (ImageView) convertView.findViewById(R.id.icon);
            back.setImageDrawable(null);
            MainActivity.getImageLoader(getContext()).displayImage(fitems.get(position).icon, back);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (base.startsWith(getContext().getString(R.string.search_goto) + " ")
                            || !((MainActivity) getContext()).usedArray.contains(base)) {
                        try {
                            //Hide the toolbar search UI without an animation because we're starting a new activity
                            if ((SettingValues.subredditSearchMethod
                                    == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                                    || SettingValues.subredditSearchMethod
                                    == Constants.SUBREDDIT_SEARCH_METHOD_BOTH)
                                    && ((MainActivity) getContext()).findViewById(
                                    R.id.toolbar_search).getVisibility() == View.VISIBLE) {
                                ((MainActivity) getContext()).findViewById(
                                        R.id.toolbar_search_suggestions).setVisibility(View.GONE);
                                ((MainActivity) getContext()).findViewById(R.id.toolbar_search)
                                        .setVisibility(View.GONE);
                                ((MainActivity) getContext()).findViewById(
                                        R.id.close_search_toolbar).setVisibility(View.GONE);

                                //Play the exit animations of the search toolbar UI to avoid the animations failing to animate upon the next time
                                //the search toolbar UI is called. Set animation to 0 because the UI is already hidden.
                                ((MainActivity) getContext()).exitAnimationsForToolbarSearch(0,
                                        ((CardView) ((MainActivity) getContext()).findViewById(
                                                R.id.toolbar_search_suggestions)),
                                        ((AutoCompleteTextView) ((MainActivity) getContext()).findViewById(
                                                R.id.toolbar_search)),
                                        ((ImageView) ((MainActivity) getContext()).findViewById(
                                                R.id.close_search_toolbar)));
                                if (SettingValues.single) {
                                    ((MainActivity) getContext()).getSupportActionBar()
                                            .setTitle(((MainActivity) getContext()).selectedFeed.name);
                                } else {
                                    ((MainActivity) getContext()).getSupportActionBar()
                                            .setTitle(
                                                    ((MainActivity) getContext()).tabViewModeTitle);
                                }
                            }
                        } catch (NullPointerException npe) {
                            Log.e(getClass().getName(), npe.getMessage());
                        }
                        Intent inte = new Intent(getContext(), FeedViewSingle.class);
                        inte.putExtra(FeedViewSingle.EXTRA_FEED, subreddit);
                        ((Activity) getContext()).startActivityForResult(inte, 2001);
                    } else {

                        try {
                            //Hide the toolbar search UI with an animation because we're just changing tabs
                            if ((SettingValues.subredditSearchMethod
                                    == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                                    || SettingValues.subredditSearchMethod
                                    == Constants.SUBREDDIT_SEARCH_METHOD_BOTH)
                                    && ((MainActivity) getContext()).findViewById(
                                    R.id.toolbar_search).getVisibility() == View.VISIBLE) {
                                ((MainActivity) getContext()).findViewById(
                                        R.id.close_search_toolbar).performClick();
                            }
                        } catch (NullPointerException npe) {
                            Log.e(getClass().getName(), npe.getMessage());
                        }

                        ((MainActivity) getContext()).pager.setCurrentItem(
                                ((MainActivity) getContext()).usedArray.indexOf(base));
                        ((MainActivity) getContext()).drawerLayout.closeDrawers();
                        if (((MainActivity) getContext()).drawerSearch != null) {
                            ((MainActivity) getContext()).drawerSearch.setText("");
                        }
                    }
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            });
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    try {
                        //Hide the toolbar search UI without an animation because we're starting a new activity
                        if ((SettingValues.subredditSearchMethod
                                == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                                || SettingValues.subredditSearchMethod
                                == Constants.SUBREDDIT_SEARCH_METHOD_BOTH)
                                && ((MainActivity) getContext()).findViewById(R.id.toolbar_search)
                                .getVisibility() == View.VISIBLE) {
                            ((MainActivity) getContext()).findViewById(
                                    R.id.toolbar_search_suggestions).setVisibility(View.GONE);
                            ((MainActivity) getContext()).findViewById(R.id.toolbar_search)
                                    .setVisibility(View.GONE);
                            ((MainActivity) getContext()).findViewById(R.id.close_search_toolbar)
                                    .setVisibility(View.GONE);

                            //Play the exit animations of the search toolbar UI to avoid the animations failing to animate upon the next time
                            //the search toolbar UI is called. Set animation to 0 because the UI is already hidden.
                            ((MainActivity) getContext()).exitAnimationsForToolbarSearch(0,
                                    ((CardView) ((MainActivity) getContext()).findViewById(
                                            R.id.toolbar_search_suggestions)),
                                    ((AutoCompleteTextView) ((MainActivity) getContext()).findViewById(
                                            R.id.toolbar_search)),
                                    ((ImageView) ((MainActivity) getContext()).findViewById(
                                            R.id.close_search_toolbar)));
                            if (SettingValues.single) {
                                ((MainActivity) getContext()).getSupportActionBar()
                                        .setTitle(((MainActivity) getContext()).selectedFeed.name);
                            } else {
                                ((MainActivity) getContext()).getSupportActionBar()
                                        .setTitle(((MainActivity) getContext()).tabViewModeTitle);
                            }
                        }
                    } catch (NullPointerException npe) {
                        Log.e(getClass().getName(), npe.getMessage());
                    }
                    Intent inte = new Intent(getContext(), FeedViewSingle.class);
                    inte.putExtra(FeedViewSingle.EXTRA_FEED, subreddit);
                    ((Activity) getContext()).startActivityForResult(inte, 2001);

                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    return true;
                }
            });
        } else {
            if ((fitems.size() * height) < parentL.getHeight()
                    && (SettingValues.subredditSearchMethod
                    == Constants.SUBREDDIT_SEARCH_METHOD_DRAWER
                    || SettingValues.subredditSearchMethod
                    == Constants.SUBREDDIT_SEARCH_METHOD_BOTH)) {
                convertView =
                        LayoutInflater.from(getContext()).inflate(R.layout.spacer, parent, false);
                ViewGroup.LayoutParams params =
                        convertView.findViewById(R.id.height).getLayoutParams();
                params.height = (parentL.getHeight() - (getCount() - 1) * height);
                convertView.setLayoutParams(params);
            } else {
                convertView =
                        LayoutInflater.from(getContext()).inflate(R.layout.spacer, parent, false);
                ViewGroup.LayoutParams params =
                        convertView.findViewById(R.id.height).getLayoutParams();
                params.height = 0;
                convertView.setLayoutParams(params);
            }
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return fitems.size() + 1;
    }

    private class SubFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            String prefix = constraint.toString().toLowerCase();

            if (prefix == null || prefix.isEmpty()) {
                ArrayList<Feed> list = new ArrayList<>(objects);
                results.values = list;
                results.count = list.size();
            } else {
                openInSubView = true;
                final ArrayList<Feed> list = new ArrayList<>(objects);
                final ArrayList<Feed> nlist = new ArrayList<>();

                for (Feed f : list) {
                    if (StringUtils.containsIgnoreCase(f.name, prefix)) nlist.add(f);
                    if (f.name.equals(prefix)) openInSubView = false;
                }

                results.values = nlist;
                results.count = nlist.size();
            }
            return results;
        }


        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            fitems = (ArrayList<Feed>) results.values;
            clear();
            if (fitems != null) {
                addAll(fitems);
                notifyDataSetChanged();
            }
        }
    }
}