package me.ccrama.rssslide;

/**
 * Created by Carlos on 3/16/2017.
 */

import android.text.format.Time;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses generic Atom feeds.
 *
 * <p>Given an InputStream representation of a feed, it returns a List of entries,
 * where each list element represents a single entry (post) in the XML feed.
 *
 * <p>An example of an Atom feed can be found at:
 * http://en.wikipedia.org/w/index.php?title=Atom_(standard)&oldid=560239173#Example_of_an_Atom_1.0_feed
 */
public class FeedInfoParser {

    // Constants indicting XML element names that we're interested in
    private static final int TAG_ICON = 1;
    private static final int TAG_TITLE = 2;
    private static final int TAG_PUBLISHED = 3;
    private static final int TAG_LINK = 4;
    private static final int TAG_SUMMARY = 5;
    private static final int TAG_CONTENT = 6;
    private static final int TAG_IMAGE = 7;

    // We don't use XML namespaces
    private static final String ns = null;

    /** Parse an Atom feed, returning a collection of Entry objects.
     *
     * @param in Atom feed, as a stream.
     * @return List of {@link com.example.android.basicsyncadapter.net.FeedParser.Entry} objects.
     * @throws XmlPullParserException on error parsing feed.
     * @throws IOException on I/O error.
     */
    public Feed parse(InputStream in)
            throws XmlPullParserException, IOException, ParseException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    /**
     * Decode a feed attached to an XmlPullParser.
     *
     * @param parser Incoming XMl
     * @return List of {@link com.example.android.basicsyncadapter.net.FeedParser.Entry} objects.
     * @throws XmlPullParserException on error parsing feed.
     * @throws IOException on I/O error.
     */
    private Feed readFeed(XmlPullParser parser)
            throws XmlPullParserException, IOException, ParseException {

        // Search for <feed> tags. These wrap the beginning/end of an Atom document.
        //
        // Example:
        // <?xml version="1.0" encoding="utf-8"?>
        // <feed xmlns="http://www.w3.org/2005/Atom">
        // ...
        // </feed>
        Feed f = new Feed();
        while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {

            String name = parser.getName();
            Log.v("Feed", "Feed found " + name);

            // Starts by looking for the <entry> tag. This tag repeates inside of <feed> for each
            // article in the feed.
            //
            // Example:
            // <entry>
            //   <title>Article title</title>
            //   <link rel="alternate" type="text/html" href="http://example.com/article/1234"/>
            //   <link rel="edit" href="http://example.com/admin/article/1234"/>
            //   <id>urn:uuid:218AC159-7F68-4CC6-873F-22AE6017390D</id>
            //   <published>2003-06-27T12:00:00Z</published>
            //   <updated>2003-06-28T12:00:00Z</updated>
            //   <summary>Article summary goes here.</summary>
            //   <author>
            //     <name>Rick Deckard</name>
            //     <email>deckard@example.com</email>
            //   </author>
            // </entry>
            if (name != null && name.equals("icon")) {
                f.icon = readTag(parser, TAG_ICON);
            } else if (name != null && name.equals("title")) {
                f.name = readTag(parser, TAG_TITLE);
            }

            parser.next();
        }
        return f;
    }

    /**
     * Process an incoming tag and read the selected value from it.
     */
    private String readTag(XmlPullParser parser, int tagType)
            throws IOException, XmlPullParserException {
        String tag = null;
        String endTag = null;

        switch (tagType) {
            case TAG_ICON:
                return readBasicTag(parser, "icon");
            case TAG_TITLE:
                return readBasicTag(parser, "title");
            case TAG_SUMMARY:
                return readBasicTag(parser, "description");
            case TAG_IMAGE:
                return null; //todo
            case TAG_PUBLISHED:
                return readBasicTag(parser, "published");
            case TAG_LINK:
                return readAlternateLink(parser);
            default:
                throw new IllegalArgumentException("Unknown tag type: " + tagType);
        }
    }

    /**
     * Reads the body of a basic XML tag, which is guaranteed not to contain any nested elements.
     *
     * <p>You probably want to call readTag().
     *
     * @param parser Current parser object
     * @param tag XML element tag name to parse
     * @return Body of the specified tag
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readBasicTag(XmlPullParser parser, String tag)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String result = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return result;
    }

    /**
     * Processes link tags in the feed.
     */
    private String readAlternateLink(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        String link = null;
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String tag = parser.getName();
        String relType = parser.getAttributeValue(null, "rel");
        if (relType != null && relType.equals("alternate")) {
            link = parser.getAttributeValue(null, "href");
        }
        while (true) {
            if (parser.next() == XmlPullParser.END_TAG) break;
            // Intentionally break; consumes any remaining sub-tags.
        }
        return link;
    }

    /**
     * For the tags title and summary, extracts their text values.
     */
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = null;
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    /**
     * Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
     * if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
     * finds the matching END_TAG (as indicated by the value of "depth" being 0).
     */
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    /**
     * This class represents a single entry (post) in the XML feed.
     *
     * <p>It includes the data members "title," "link," and "summary."
     */
    public static class Entry {
        public String id;
        public final String title;
        public final String link;
        public final long published;
        public final String image;
        public final String summary;

        Entry(String id, String title, String link, String summary, String image, long published) {
            this.id = id;
            this.title = title;
            this.link = link;
            this.published = published;
            this.image = image;
            this.summary = summary;
        }
    }
}