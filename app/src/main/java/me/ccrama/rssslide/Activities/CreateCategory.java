package me.ccrama.rssslide.Activities;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import me.ccrama.rssslide.BaseApplication;
import me.ccrama.rssslide.Palette;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.Realm.Category;
import me.ccrama.rssslide.Realm.Feed;
import me.ccrama.rssslide.Realm.FeedWrapper;
import me.ccrama.rssslide.UserFeeds;

/**
 * This class handles creation of Categories.
 */
public class CreateCategory extends BaseActivityAnim {
    private ArrayList<FeedWrapper> feeds;
    private FeedWrapper base;
    private CustomAdapter adapter;
    private EditText title;
    private RecyclerView recyclerView;
    public static final String EXTRA_MULTI = "multi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_createcategory);
        setupAppBar(R.id.toolbar, "", true, true);

        title = (EditText) findViewById(R.id.name);

        feeds = new ArrayList<>();
        if (getIntent().hasExtra(EXTRA_MULTI)) {
            String multi = getIntent().getExtras().getString(EXTRA_MULTI);
            title.setText(multi.replace("%20", " "));
            title.setEnabled(false);
            base = Realm.getDefaultInstance().where(Category.class).equalTo("name", multi).findFirst();
            for (Feed f : base.getFeeds()) {
                feeds.add(f);
            }
        }

        recyclerView = (RecyclerView) findViewById(R.id.subslist);

        ArrayList<FeedWrapper> allFeeds = new ArrayList<>();
        for (FeedWrapper w : UserFeeds.getAllUserFeeds()) {
            if (w instanceof Feed) {
                allFeeds.add(w);
            }
        }
        adapter = new CustomAdapter(allFeeds);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private final ArrayList<FeedWrapper> items;

        public CustomAdapter(ArrayList<FeedWrapper> items) {
            this.items = items;
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subforsublistcreate, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            final FeedWrapper origPos = items.get(position);
            final TextView t = ((TextView) holder.itemView.findViewById(R.id.name));
            t.setText(origPos.getTitle());

            ImageView back = (ImageView) holder.itemView.findViewById(R.id.icon);
            back.setImageDrawable(null);
            if (origPos.getIcon() != null && !origPos.getIcon().isEmpty()) {
                back.setBackgroundDrawable(null);
                BaseApplication.getImageLoader(CreateCategory.this).displayImage(origPos.getIcon(), back);
            } else {
                back.setBackgroundResource(R.drawable.circle);
                back.getBackground().setColorFilter(Palette.getColor(origPos.getTitle()), PorterDuff.Mode.MULTIPLY);
            }

            AppCompatCheckBox box = ((AppCompatCheckBox) holder.itemView.findViewById(R.id.selected));
            for (FeedWrapper f : feeds) {
                if (f.getTitle().equals(origPos.getTitle())) {
                    box.setChecked(true);
                    break;
                }
            }
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        feeds.add(origPos);
                    } else {
                        int index = 0;
                        for (FeedWrapper f : feeds) {
                            if (f.getTitle().equals(origPos.getTitle())) {

                                break;
                            }
                            index += 1;
                        }
                        feeds.remove(index);
                    }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (base == null) {
                    base = new Category();
                    ((Category) base).setName(title.getText().toString());
                }
                ((Category) base).feeds = new RealmList<>();
                for (FeedWrapper w : feeds) {
                    if (w instanceof Feed)
                        ((Category) base).feeds.add(((Feed) w));
                }
                realm.insertOrUpdate((Category) base);
            }
        });
    }

}
