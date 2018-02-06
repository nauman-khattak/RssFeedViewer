package khattak.nauman.RssFeed.AppleRssFeedViewer;

import android.os.AsyncTask;
import java.io.InputStream;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import java.net.URL;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.List;

/**
 * Created by Muhammad NaumanTariq on 18-Sep-17.
 */

public class FeedAdapter<T extends FeedEntry> extends ArrayAdapter {
    private static final String TAG = "FeedAdapter";

    private final int layoutResource;
    private final LayoutInflater layoutInflater;
    private List<T> applications;

    public FeedAdapter(@NonNull Context context, @LayoutRes int resource, List<T> applications) {
        super(context, resource);
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context);
        this.applications = applications;
    }

    @Override
    public int getCount() {
        return applications.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            Log.d(TAG, "getView: called with null convertView");
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            Log.d(TAG, "getView: provided a convertView");
            viewHolder = (ViewHolder) convertView.getTag();
        }
//        TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
//        TextView tvArtist = (TextView) convertView.findViewById(R.id.tvArtist);
//        TextView tvSummary = (TextView) convertView.findViewById(R.id.tvSummary);

        T currentApp = applications.get(position);

        viewHolder.tvName.setText(currentApp.getName());
        viewHolder.tvArtist.setText(currentApp.getArtist());
        viewHolder.tvSummary.setText(currentApp.getSummary());
//        Picasso.with(context).load("http://i.imgur.com/DvpvklR.png").into(imageView);
//        try {
//            URL url = new URL(currentApp.getImageURL());
//            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//            viewHolder.tvImageView.setImageBitmap(bmp);
//        } catch(Exception e){
//            Log.d(TAG, "getView: setImage: **********************************************"+e);
//            e.printStackTrace();
//        }
        DownloadImageTask dit = new DownloadImageTask(viewHolder.tvImageView);
        dit.execute(currentApp.getImageURL());

        return convertView;
    }

    private class ViewHolder {
        final TextView tvName;
        final TextView tvArtist;
        final TextView tvSummary;
        final ImageView tvImageView;

        ViewHolder(View v) {
            this.tvName = v.findViewById(R.id.tvName);
            this.tvArtist = v.findViewById(R.id.tvArtist);
            this.tvSummary = v.findViewById(R.id.tvSummary);
            this.tvImageView = v.findViewById(R.id.tvImageView);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                in.close();
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();

            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

}
