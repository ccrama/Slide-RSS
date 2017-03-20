package me.ccrama.rssslide.Views;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.devspark.robototextview.widget.RobotoTextView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ccrama.rssslide.Palette;
import me.ccrama.rssslide.R;
import me.ccrama.rssslide.SettingValues;
import me.ccrama.rssslide.Util.LinkUtil;
import me.ccrama.rssslide.Util.TextViewLinkHandler;

/**
 * Created by carlo_000 on 1/11/2016.
 */
public class SpoilerRobotoTextView extends RobotoTextView implements ClickableText {
    private static final Pattern              htmlSpoilerPattern  =
            Pattern.compile("<a href=\"([#/](?:spoiler|sp|s))\">([^<]*)</a>");

    public SpoilerRobotoTextView(Context context) {
        super(context);
        setLineSpacing(0, 1.1f);
    }

    public SpoilerRobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLineSpacing(0, 1.1f);
    }

    public SpoilerRobotoTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLineSpacing(0, 1.1f);
    }

    private static SpannableStringBuilder removeNewlines(SpannableStringBuilder s) {
        int start = 0;
        int end = s.length();
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return (SpannableStringBuilder) s.subSequence(start, end);
    }

    /**
     * Set the text from html. Handles formatting spoilers, links etc. <p/> The text must be valid
     * html.
     *
     * @param text html text
     */
    public void setTextHtml(CharSequence text) {
        setTextHtml(text, "");
    }

    /**
     * Set the text from html. Handles formatting spoilers, links etc. <p/> The text must be valid
     * html.
     *
     * @param baseText  html text
     * @param subreddit the subreddit to theme
     */
    public void setTextHtml(CharSequence baseText, String subreddit) {
        String text = wrapAlternateSpoilers(saveEmotesFromDestruction(baseText.toString().trim()));
        SpannableStringBuilder builder = (SpannableStringBuilder) Html.fromHtml(text);

        replaceQuoteSpans(
                builder); //replace the <blockquote> blue line with something more colorful

        if (text.contains("<a")) {
            setEmoteSpans(builder); //for emote enabled subreddits
        }
        if (text.contains("[")) {
            setCodeFont(builder);
        }
        if (text.contains("[[d[")) {
            setStrikethrough(builder);
        }
        if (text.contains("[[h[")) {
            setHighlight(builder, subreddit);
        }
        if (subreddit != null && !subreddit.isEmpty()) {
            setMovementMethod(new TextViewLinkHandler(this, subreddit, builder));
            setFocusable(false);
            setClickable(false);
            if (subreddit.equals("FORCE_LINK_CLICK")) {
                setLongClickable(false);
            }

        }

        builder = removeNewlines(builder);

        builder.append(" ");

        super.setText(builder, TextView.BufferType.SPANNABLE);
    }


    /**
     * Replaces the blue line produced by <blockquote>s with something more visible
     *
     * @param spannable parsed comment text #fromHtml
     */
    private void replaceQuoteSpans(Spannable spannable) {
        QuoteSpan[] quoteSpans = spannable.getSpans(0, spannable.length(), QuoteSpan.class);

        for (QuoteSpan quoteSpan : quoteSpans) {
            final int start = spannable.getSpanStart(quoteSpan);
            final int end = spannable.getSpanEnd(quoteSpan);
            final int flags = spannable.getSpanFlags(quoteSpan);

            spannable.removeSpan(quoteSpan);

            //If the theme is Light or Sepia, use a darker blue; otherwise, use a lighter blue
            final int barColor =
                    (SettingValues.currentTheme == 1 || SettingValues.currentTheme == 5)
                            ? ContextCompat.getColor(getContext(), R.color.md_blue_600)
                            : ContextCompat.getColor(getContext(), R.color.md_blue_400);

            final int BAR_WIDTH = 4;
            final int GAP = 5;

            spannable.setSpan(new CustomQuoteSpan(Color.TRANSPARENT, //background color
                            barColor, //bar color
                            BAR_WIDTH, //bar width
                            GAP), //bar + text gap
                    start, end, flags);
        }
    }

    private String wrapAlternateSpoilers(String html) {
        Matcher htmlSpoilerMatcher = htmlSpoilerPattern.matcher(html);
        while (htmlSpoilerMatcher.find()) {
            String newPiece = htmlSpoilerMatcher.group();
            String inner = "<a href=\"/spoiler\">spoiler&lt; [[s[ "
                    + newPiece.substring(newPiece.indexOf(">") + 1,
                    newPiece.indexOf("<", newPiece.indexOf(">")))
                    + "]s]]</a>";
            html = html.replace(htmlSpoilerMatcher.group(), inner);
        }
        return html;
    }

    private String saveEmotesFromDestruction(String html) {
        //Emotes often have no spoiler caption, and therefore are converted to empty anchors. Html.fromHtml removes anchors with zero length node text. Find zero length anchors that start with "/" and add "." to them.
        Pattern htmlEmotePattern = Pattern.compile("<a href=\"/.*\"></a>");
        Matcher htmlEmoteMatcher = htmlEmotePattern.matcher(html);
        while (htmlEmoteMatcher.find()) {
            String newPiece = htmlEmoteMatcher.group();
            //Ignore empty tags marked with sp.
            if (!htmlEmoteMatcher.group().contains("href=\"/sp\"")) {
                newPiece = newPiece.replace("></a", ">.</a");
                html = html.replace(htmlEmoteMatcher.group(), newPiece);
            }
        }
        return html;
    }

    private void setEmoteSpans(SpannableStringBuilder builder) {
        for (URLSpan span : builder.getSpans(0, builder.length(), URLSpan.class)) {
                setLargeLinks(builder, span);
            File emoteDir = new File(Environment.getExternalStorageDirectory(), "RedditEmotes");
            File emoteFile = new File(emoteDir, span.getURL().replace("/", "").replaceAll("-.*", "")
                    + ".png"); //BPM uses "-" to add dynamics for emotes in browser. Fall back to original here if exists.
            boolean startsWithSlash = span.getURL().startsWith("/");
            boolean hasOnlyOneSlash = StringUtils.countMatches(span.getURL(), "/") == 1;

            if (emoteDir.exists() && startsWithSlash && hasOnlyOneSlash && emoteFile.exists()) {
                //We've got an emote match
                int start = builder.getSpanStart(span);
                int end = builder.getSpanEnd(span);
                CharSequence textCovers = builder.subSequence(start, end);

                //Make sure bitmap loaded works well with screen density.
                BitmapFactory.Options options = new BitmapFactory.Options();
                DisplayMetrics metrics = new DisplayMetrics();
                ((WindowManager) getContext().getSystemService(
                        Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
                options.inDensity = 240;
                options.inScreenDensity = metrics.densityDpi;
                options.inScaled = true;

                //Since emotes are not directly attached to included text, add extra character to attach image to.
                builder.removeSpan(span);
                if (builder.subSequence(start, end).charAt(0) != '.') {
                    builder.insert(start, ".");
                }
                Bitmap emoteBitmap = BitmapFactory.decodeFile(emoteFile.getAbsolutePath(), options);
                builder.setSpan(new ImageSpan(getContext(), emoteBitmap), start, start + 1,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                //Check if url span has length. If it does, it's a spoiler/caption
                if (textCovers.length() > 1) {
                    builder.setSpan(new URLSpan("/sp"), start + 1, end + 1,
                            Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    builder.setSpan(new StyleSpan(Typeface.ITALIC), start + 1, end + 1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                builder.append("\n"); //Newline to fix text wrapping issues
            }
        }
    }


    private void setLargeLinks(SpannableStringBuilder builder, URLSpan span) {
        builder.setSpan(new RelativeSizeSpan(1.3f), builder.getSpanStart(span),
                builder.getSpanEnd(span), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void setStrikethrough(SpannableStringBuilder builder) {
        final int offset = "[[d[".length(); // == "]d]]".length()

        int start = -1;
        int end;

        for (int i = 0; i < builder.length() - 3; i++) {
            if (builder.charAt(i) == '['
                    && builder.charAt(i + 1) == '['
                    && builder.charAt(i + 2) == 'd'
                    && builder.charAt(i + 3) == '[') {
                start = i + offset;
            } else if (builder.charAt(i) == ']'
                    && builder.charAt(i + 1) == 'd'
                    && builder.charAt(i + 2) == ']'
                    && builder.charAt(i + 3) == ']') {
                end = i;
                builder.setSpan(new StrikethroughSpan(), start, end,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                builder.delete(end, end + offset);
                builder.delete(start - offset, start);
                i -= offset + (end - start); // length of text
            }
        }
    }

    private void setHighlight(SpannableStringBuilder builder, String subreddit) {
        final int offset = "[[h[".length(); // == "]h]]".length()

        int start = -1;
        int end;
        for (int i = 0; i < builder.length() - 4; i++) {
            if (builder.charAt(i) == '['
                    && builder.charAt(i + 1) == '['
                    && builder.charAt(i + 2) == 'h'
                    && builder.charAt(i + 3) == '[') {
                start = i + offset;
            } else if (builder.charAt(i) == ']'
                    && builder.charAt(i + 1) == 'h'
                    && builder.charAt(i + 2) == ']'
                    && builder.charAt(i + 3) == ']') {
                end = i;
                builder.setSpan(new BackgroundColorSpan(Palette.getColor(subreddit)), start, end,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                builder.delete(end, end + offset);
                builder.delete(start - offset, start);
                i -= offset + (end - start); // length of text
            }
        }
    }

    @Override
    public void onLinkClick(String url, int xOffset, String subreddit, URLSpan span) {
        if (url == null) {
            ((View) getParent()).callOnClick();
            return;
        }

        Context context = getContext();
        Activity activity = null;
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else if (context instanceof android.support.v7.view.ContextThemeWrapper) {
            activity =
                    (Activity) ((android.support.v7.view.ContextThemeWrapper) context).getBaseContext();
        } else if (context instanceof ContextWrapper) {
            Context context1 = ((ContextWrapper) context).getBaseContext();
            if (context1 instanceof Activity) {
                activity = (Activity) context1;
            } else if (context1 instanceof ContextWrapper) {
                Context context2 = ((ContextWrapper) context1).getBaseContext();
                if (context2 instanceof Activity) {
                    activity = (Activity) context2;
                } else if (context2 instanceof ContextWrapper) {
                    activity =
                            (Activity) ((android.support.v7.view.ContextThemeWrapper) context2).getBaseContext();
                }
            }
        } else {
            throw new RuntimeException("Could not find activity from context:" + context);
        }
        LinkUtil.openUrl(url, Palette.getColor(subreddit), activity);
    }

    @Override
    public void onLinkLongClick(final String baseUrl, MotionEvent event) {
        if (baseUrl == null) {
            return;
        }
        final String url = StringEscapeUtils.unescapeHtml4(baseUrl);

        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        Activity activity = null;
        final Context context = getContext();
        if (context instanceof Activity) {
            activity = (Activity) context;
        } else if (context instanceof android.support.v7.view.ContextThemeWrapper) {
            activity =
                    (Activity) ((android.support.v7.view.ContextThemeWrapper) context).getBaseContext();
        } else if (context instanceof ContextWrapper) {
            Context context1 = ((ContextWrapper) context).getBaseContext();
            if (context1 instanceof Activity) {
                activity = (Activity) context1;
            } else if (context1 instanceof ContextWrapper) {
                Context context2 = ((ContextWrapper) context1).getBaseContext();
                if (context2 instanceof Activity) {
                    activity = (Activity) context2;
                } else if (context2 instanceof ContextWrapper) {
                    activity =
                            (Activity) ((android.support.v7.view.ContextThemeWrapper) context2).getBaseContext();
                }
            }
        } else {
            throw new RuntimeException("Could not find activity from context:" + context);
        }

        if (activity != null && !activity.isFinishing()) {

                BottomSheet.Builder b = new BottomSheet.Builder(activity).title(url).grid();
                int[] attrs = new int[]{R.attr.tint};
                TypedArray ta = getContext().obtainStyledAttributes(attrs);

                int color = ta.getColor(0, Color.WHITE);
                Drawable open = getResources().getDrawable(R.drawable.ic_open_in_browser);
                open.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                Drawable share = getResources().getDrawable(R.drawable.ic_share);
                share.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                Drawable copy = getResources().getDrawable(R.drawable.ic_content_copy);
                copy.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

                ta.recycle();

            /* todo make work
                b.sheet(R.id.open_link, open,
                        getResources().getString(R.string.submission_link_extern));
                b.sheet(R.id.share_link, share, getResources().getString(R.string.share_link));
                b.sheet(R.id.copy_link, copy,
                        getResources().getString(R.string.submission_link_copy));
                final Activity finalActivity = activity;
                b.listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.open_link:
                                LinkUtil.openExternally(url, context, false);
                                break;
                            case R.id.share_link:
                                MainActivity.defaultShareText("", url, finalActivity);
                                break;
                            case R.id.copy_link:
                                ClipboardManager clipboard =
                                        (ClipboardManager) finalActivity.getSystemService(
                                                Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Link", url);
                                clipboard.setPrimaryClip(clip);
                                Toast.makeText(finalActivity, R.string.submission_link_copied,
                                        Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }).show();*/
        }
    }

    /**
     * Sets the styling for string with code segments. <p/> The general process is to search for
     * <code>[[&lt;[</code> and <code>]&gt;]]</code> tokens to find the code fragments within the
     * escaped text. A <code>Spannable</code> is created which which breaks up the origin sequence
     * into non-code and code fragments, and applies a monospace font to the code fragments.
     *
     * @param sequence the Spannable generated from Html.fromHtml
     * @return the message with monospace font applied to code fragments
     */
    private SpannableStringBuilder setCodeFont(SpannableStringBuilder sequence) {
        int start = 0;
        int end = 0;
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.charAt(i) == '[' && i < sequence.length() - 3) {
                if (sequence.charAt(i + 1) == '['
                        && sequence.charAt(i + 2) == '<'
                        && sequence.charAt(i + 3) == '[') {
                    start = i;
                }
            } else if (sequence.charAt(i) == ']' && i < sequence.length() - 3) {
                if (sequence.charAt(i + 1) == '>'
                        && sequence.charAt(i + 2) == ']'
                        && sequence.charAt(i + 3) == ']') {
                    end = i;
                }
            }

            if (end > start) {
                sequence.delete(end, end + 4);
                sequence.delete(start, start + 4);
                sequence.setSpan(new TypefaceSpan("monospace"), start, end - 4,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                start = 0;
                end = 0;
                i = i - 4; // move back to compensate for removal of [[<[
            }
        }

        return sequence;
    }


}
