package khattak.nauman.RssFeed.AppleRssFeedViewer;

import android.app.Application;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by Muhammad NaumanTariq on 11-Sep-17.
 */

public class ParseApplications {

    private static final String TAG = "ParseApplications";

    private ArrayList<FeedEntry> applications;

    public ParseApplications() {
        this.applications = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications;
    }

    public boolean parse(String XMLData) {
        boolean status = true; //if an exception is thrown then this will be set to false, true means everything is going fine
        FeedEntry currentRecord = null;
        boolean inEntry = false; //used to indicate whether we are inside Entry or not
        String textValue = ""; //used to store the text from starting tag to closing tag of a particular tag

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(XMLData));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
//                        Log.d(TAG, "parse: Starting tag for "+tagName);
                        if ("entry".equalsIgnoreCase(tagName)) {
                            inEntry = true;
                            currentRecord = new FeedEntry();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
//                        Log.d(TAG, "parse: Ending tag for "+tagName);
                        if (inEntry) {
                            if ("entry".equalsIgnoreCase(tagName)) {
                                applications.add(currentRecord);//Gives error {'Variable' currentRecord might not have been initialized.} if initially, on line 30, its not initialized to null. Android studion doesnt know about xml therefore it connot know that one cannot reach END_TAG without going through START_TAG
                                inEntry = false;
                            } else if ("name".equalsIgnoreCase(tagName)) {
                                currentRecord.setName(textValue);
                            } else if ("artist".equalsIgnoreCase(tagName)) {
                                currentRecord.setArtist(textValue);
                            } else if ("releaseDate".equalsIgnoreCase(tagName)) {
                                currentRecord.setReleaseDate(textValue);
                            } else if ("summary".equalsIgnoreCase(tagName)) {
                                currentRecord.setSummary(textValue);
                            } else if ("image".equalsIgnoreCase(tagName)) {
                                currentRecord.setImageURL(textValue);
                            }
                        }
                        break;
                    default:
                        //Nothing else to do
                }
                eventType = xpp.next();
            }
//            for (FeedEntry app : applications) {
//                Log.d(TAG, "**************");
//                Log.d(TAG, app.toString());
//            }

        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }

        return status;
    }

}
