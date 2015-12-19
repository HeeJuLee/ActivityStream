package com.ncsoft.platform.activitystream;

import android.util.Xml;

import com.android.volley.VolleyLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StreamParser {
    private static final String ns = null;

    public List<Entry> parse(String string) throws XmlPullParserException, IOException {

        ByteArrayInputStream in = new ByteArrayInputStream(string.getBytes());

        return parse(in);
    }

    public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
        List<Entry> entries = new ArrayList<Entry>();

        try {
            XmlPullParser parser = Xml.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            parseFeed(parser, entries);
        } finally {
            in.close();
        }

        return entries;
    }

    private void parseFeed(XmlPullParser parser, List<Entry> entries) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "feed");

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if(name.equals("entry")) {
                parseEntry(parser, entries);
            } else {
                skip(parser);
            }
        }
    }

    private void parseEntry(XmlPullParser parser, List<Entry> entries) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "entry");

        Entry entry = new Entry();

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if(name.equals("id"))
                parseId(parser, entry);
            else if(name.equals("author"))
                parseAuthor(parser, entry);
            else if(name.equals("title"))
                parseTitle(parser, entry);
            else if(name.equals("content"))
                parseContent(parser, entry);
            else if(name.equals("updated"))
                parseUpdated(parser, entry);
            else if(name.equals("activity:object"))
                parseActivityObject(parser, entry);
            else if(name.equals("activity:target"))
                parseActivityTarget(parser, entry);
            else
                skip(parser);
        }
        if(entry.getTargetIssueKey() != null)
            entry.setIssueKey(entry.getTargetIssueKey());
        else
            entry.setIssueKey(entry.getObjectIssueKey());
        if(entry.getTargetIssueSummary() != null)
            entry.setIssueSummary(entry.getTargetIssueSummary());
        else
            entry.setIssueSummary(entry.getObjectIssueSummary());

        entries.add(entry);
    }

    private void parseId(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "id");

        entry.setId(readText(parser));

        parser.require(XmlPullParser.END_TAG, ns, "id");
    }

    private void parseAuthor(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "author");

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if(name.equals("name"))
                parseAuthorName(parser, entry);
            else if(name.equals("link"))
                parseAuthorImageLink(parser, entry);
            else if(name.equals("usr:username"))
                parseAuthorId(parser, entry);
            else
                skip(parser);
        }

    }

    private void parseAuthorName(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "name");

        entry.setAuthorName(readText(parser));

        parser.require(XmlPullParser.END_TAG, ns, "name");
    }

    private void parseAuthorImageLink(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");

        String imageHeight = parser.getAttributeValue(null, "media:height");
        if (imageHeight.equals("48")) {
            entry.setAuthorImageLink(parser.getAttributeValue(null, "href"));

            VolleyLog.d(entry.getAuthorImageLink());
        }

        parser.nextTag();
    }

    private void parseAuthorId(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "usr:username");

        entry.setAuthorId(readText(parser));

        parser.require(XmlPullParser.END_TAG, ns, "usr:username");
    }

    private void parseTitle(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");

        entry.setTitle(readText(parser));

        parser.require(XmlPullParser.END_TAG, ns, "title");
    }

    private void parseContent(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "content");

        entry.setContent(readText(parser));

        parser.require(XmlPullParser.END_TAG, ns, "content");
    }

    private void parseUpdated(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "updated");

        entry.setUpdated(readText(parser));

        parser.require(XmlPullParser.END_TAG, ns, "updated");
    }

    private void parseActivityObject(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "activity:object");

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if(name.equals("title"))
                parseActivityObjectTitle(parser, entry);
            else if(name.equals("summary"))
                parseActivityObjectSummary(parser, entry);
            else if(name.equals("link"))
                parseActivityObjectLink(parser, entry);
            else
                skip(parser);
        }
    }

    private void parseActivityObjectTitle(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");

        entry.setObjectIssueKey(readText(parser));
        VolleyLog.d(entry.getObjectIssueKey());

        parser.require(XmlPullParser.END_TAG, ns, "title");
    }

    private void parseActivityObjectSummary(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "summary");

        entry.setObjectIssueSummary(readText(parser));
        VolleyLog.d(entry.getObjectIssueSummary());

        parser.require(XmlPullParser.END_TAG, ns, "summary");
    }

    private void parseActivityObjectLink(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");

        String relType = parser.getAttributeValue(null, "rel");
        if (relType.equals("alternate")) {
            entry.setObjectIssueWebLink(parser.getAttributeValue(null, "href"));
        }

        parser.nextTag();
    }

    private void parseActivityTarget(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "activity:target");

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if(name.equals("title"))
                parseActivityTargetTitle(parser, entry);
            else if(name.equals("summary"))
                parseActivityTargetSummary(parser, entry);
            else if(name.equals("link"))
                parseActivityTargetLink(parser, entry);
            else
                skip(parser);
        }
    }

    private void parseActivityTargetTitle(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");

        entry.setTargetIssueKey(readText(parser));
        VolleyLog.d(entry.getTargetIssueKey());

        parser.require(XmlPullParser.END_TAG, ns, "title");
    }

    private void parseActivityTargetSummary(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "summary");

        entry.setTargetIssueSummary(readText(parser));
        VolleyLog.d(entry.getTargetIssueSummary());

        parser.require(XmlPullParser.END_TAG, ns, "summary");
    }

    private void parseActivityTargetLink(XmlPullParser parser, Entry entry) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");

        String relType = parser.getAttributeValue(null, "rel");
        if (relType.equals("alternate")) {
            entry.setTargetIssueWebLink(parser.getAttributeValue(null, "href"));
            VolleyLog.d(entry.getTargetIssueWebLink());
        }

        parser.nextTag();
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

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

    public static class Entry {
        private String mId;
        private String mAuthorImageLink;
        private String mAuthorName;
        private String mAuthorId;
        private String mUpdated;
        private String mIssueKey;
        private String mIssueSummary;
        private String mObjectIssueKey;
        private String mObjectIssueSummary;
        private String mObjectIssueWebLink;
        private String mTargetIssueKey;
        private String mTargetIssueSummary;
        private String mTargetIssueWebLink;
        private String mTitle;
        private String mContent;


        public String getId() { return mId; }

        public String getAuthorImageLink() {
            return mAuthorImageLink;
        }

        public String getAuthorName() {
            return mAuthorName;
        }

        public String getAuthorId() {
            return mAuthorId;
        }

        public String getUpdated() {
            return mUpdated;
        }

        public String getIssueKey() {
            return mIssueKey;
        }

        public String getIssueSummary() {
            return mIssueSummary;
        }

        public String getObjectIssueKey() {
            return mObjectIssueKey;
        }

        public String getObjectIssueSummary() {
            return mObjectIssueSummary;
        }

        public String getObjectIssueWebLink() {
            return mObjectIssueWebLink;
        }

        public String getTargetIssueKey() {
            return mTargetIssueKey;
        }

        public String getTargetIssueSummary() {
            return mTargetIssueSummary;
        }

        public String getTargetIssueWebLink() {
            return mTargetIssueWebLink;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getContent() {
            if(mContent == null)
                return "";
            return mContent;
        }

        public void setId(String id) {
            this.mId = id;
        }

        public void setAuthorImageLink(String authorImageLink) {
            this.mAuthorImageLink = authorImageLink;
        }

        public void setAuthorName(String authorName) {
            this.mAuthorName = authorName;
        }

        public void setAuthorId(String authorId) {
            this.mAuthorId = authorId;
        }

        public void setUpdated(String updated) {
            VolleyLog.d("Updated: " + updated);
            this.mUpdated = updated;
        }

        public void setObjectIssueKey(String issueKey) {
            this.mObjectIssueKey = issueKey;
        }

        public void setIssueKey(String issueKey) {
            this.mIssueKey = issueKey;
        }

        public void setIssueSummary(String issueSummary) {
            this.mIssueSummary = issueSummary;
        }

        public void setObjectIssueSummary(String issueSummary) {
            this.mObjectIssueSummary = issueSummary;
        }

        public void setObjectIssueWebLink(String issueWebLink) {
            this.mObjectIssueWebLink = issueWebLink;
        }

        public void setTargetIssueKey(String issueKey) {
            this.mTargetIssueKey = issueKey;
        }

        public void setTargetIssueSummary(String issueSummary) {
            this.mTargetIssueSummary = issueSummary;
        }

        public void setTargetIssueWebLink(String issueWebLink) {
            this.mTargetIssueWebLink = issueWebLink;
        }

        public void setTitle(String title) {
            this.mTitle = title;
        }

        public void setContent(String content) {
            this.mContent = content;
        }
    }
}
