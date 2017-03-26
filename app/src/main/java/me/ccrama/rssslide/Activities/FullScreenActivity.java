package me.ccrama.rssslide.Activities;

import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.view.Window;

import me.ccrama.rssslide.Palette;
import me.ccrama.rssslide.R;

/**
 * Created by tomer aka rosenpin on 11/27/15.
 * <p>
 * This Activity allows for fullscreen viewing without the statusbar visible
 */
public class FullScreenActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //TODO something like this getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
        //   WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        overridePendingTransition(R.anim.slide_in, 0);
        setRecentBar(null, Palette.getDefaultColor());


    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        try {
            findViewById(android.R.id.content).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    //   Blurry.with(FullScreenActivity.this).radius(2).sampling(5).animate().color(Color.parseColor("#99000000")).onto((ViewGroup) findViewById(android.R.id.content));
                }
            });
        } catch (Exception ignored) {

        }
        super.onPostCreate(savedInstanceState);
    }
}
