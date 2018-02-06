package khattak.nauman.RssFeed.AppleRssFeedViewer;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listApps;
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit = 10;
    private String feedCachedUrl = null;
    public static final String STATE_URL = "feedUrl";
    public static final String STATE_LIMIT = "feedLimit";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = (ListView) findViewById(R.id.xmlListView);
//        listApps.setClickable(true);
//        listApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });

        if (savedInstanceState != null) {
            feedUrl = savedInstanceState.getString(STATE_URL);
            feedLimit = savedInstanceState.getInt(STATE_LIMIT);
        }
        enableHttpResponseCache();
        downloadUrl(String.format(feedUrl, feedLimit));
    }

    private void enableHttpResponseCache() {
        try {
            Log.d(TAG, "enableHttpResponseCache: inside try block");
            long httpCacheSize = 5 * 1024 * 1024; // 5 MiB
            File httpCacheDir = new File(getCacheDir(), "http");
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File.class, long.class)
                    .invoke(null, httpCacheDir, httpCacheSize);
            Log.d(TAG, "enableHttpResponseCache: leaving try block");
        } catch (Exception httpResponseCacheNotAvailable) {
            Log.d(TAG, "HTTP response cache is unavailable.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        if (feedLimit == 10) {
            menu.findItem(R.id.mnu10).setChecked(true);
        } else {
            menu.findItem(R.id.mnu25).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.mnuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;

            case R.id.mnuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;

            case R.id.mnuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;

            case R.id.mnu10:
            case R.id.mnu25:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;// 35 - currentLimit = required, 35-10=25, 35-25=10
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " setting feed limit to " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " feed limit unchanged");
                }
                break;

            case R.id.mnuRefresh:
                feedCachedUrl = null;
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        downloadUrl(String.format(feedUrl, feedLimit));//String.format will replace %d with feedLimit value in URL
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL, feedUrl);
        outState.putInt(STATE_LIMIT, feedLimit);
        super.onSaveInstanceState(outState);
    }


    private void downloadUrl(String feedUrl) {
        if (!feedUrl.equalsIgnoreCase(feedCachedUrl)) {//if url isn't changed it won't download same url again
            Log.d(TAG, "downloadUrl: starting Asynctask");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedUrl);
            feedCachedUrl = feedUrl;
            Log.d(TAG, "downloadUrl: done");
        } else {
            Log.d(TAG, "downloadUrl: URl not changed");
        }

    }

    private class DownloadData extends AsyncTask<String, Void, String> {

        private static final String TAG = "DownloadData";

        //the return value of doInBackground(String... strings) is passed as an argument to onPostExecute(String s)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is " + s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(//Mainactivity.this -> context
//                    MainActivity.this, R.layout.list_item, parseApplications.getApplications());
//            listApps.setAdapter(arrayAdapter);

            //MainActivity.this is used to invoke context bcz an Activity or AppCompatActivity is a context
            FeedAdapter<FeedEntry> feedAdapter = new FeedAdapter<>(MainActivity.this, R.layout.list_record, parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);
        }

        @Override
        protected String doInBackground(String... strings) {//You can pass multiple urls
            Log.d(TAG, "doInBackground: starts with " + strings[0]);
            String rssFeed = downloadXML(strings[0]);//we are fetching data from first url. In our case we have only one url.
            if (rssFeed == null) {
                Log.e(TAG, "doInBackground: Error downloading");
            } /*else{
                ParseApplications parseApplications = new ParseApplications();
                parseApplications.parse(rssFeed);
            }*/
            return rssFeed;
        }

        private String downloadXML(String urlpath) {
            StringBuilder xmlResult = new StringBuilder();

            try {

                URL url = new URL(urlpath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //Here we downcasted URL.openConnection() to HttpURLConnection
                connection.setUseCaches(true);
//                connection.addRequestProperty("Cache-Control", "max-age=0");
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: The response code was " + response);

//                long currentTime = System.currentTimeMillis();
//                long expires = connection.getHeaderFieldDate("Expires", currentTime);
//                long lastModified = connection.getHeaderFieldDate("Last-Modified", currentTime);
//                long lastUpdateTime = 0;
//
//                // lastUpdateTime represents when the cache was last updated.
//                if (lastModified < lastUpdateTime) {
//                    // Skip update
//                } else {
//                    // Parse update
//                    lastUpdateTime = lastModified;
//                }

//                InputStream inputStream = connection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader reader = new BufferedReader(inputStreamReader);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500];
                while (true) {
                    charsRead = reader.read(inputBuffer);
                    if (charsRead < 0) {
                        break;
                    }
                    if (charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                reader.close();

                return xmlResult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: MalformedURLException: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IO Exception: " + e.getMessage());
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXML: SecurityException: " + e.getMessage());
            } catch (NullPointerException e) {
                Log.e(TAG, "downloadXML: NullPointerException: " + e.getMessage());
            }

            return null;
        }
    }
}


















