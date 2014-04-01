package com.golden.owaranai;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.golden.owaranai.twitter.StatusItem;

import java.io.InputStream;
import java.util.List;

import twitter4j.Status;

/**
 * Created by soomin on 3/23/2014.
 */
public class TimelineAdapter extends BaseAdapter {

    private Activity activity;

    private List<StatusItem> mStatuses;

    private static LayoutInflater inflater = null;

    public TimelineAdapter(Activity a) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        int count = 0;
        if (mStatuses != null) {
            count = mStatuses.size();
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.tweet_row, null);
        }

        if (mStatuses != null) {
            // Get all the references to the layouts in the row
            ImageView avatarImage = (ImageView) vi.findViewById(R.id.avatar);
            TextView userName = (TextView) vi.findViewById(R.id.username);
            TextView displayName = (TextView) vi.findViewById(R.id.displayname);
            TextView time = (TextView) vi.findViewById(R.id.time);
            TextView tweet = (TextView) vi.findViewById(R.id.tweet);

            // Set all the references to the layouts in the row
            new DownloadImageTask(avatarImage).execute(((Status) mStatuses.get(position).status).getUser().getProfileImageURL());
            tweet.setText(((Status)mStatuses.get(position).status).getText());
            displayName.setText(((Status)mStatuses.get(position).status).getUser().getName());
            userName.setText("@" + ((Status)mStatuses.get(position).status).getUser().getScreenName());
            time.setText(((Status)mStatuses.get(position).status).getCreatedAt().toString());
        }

        return vi;
    }

    public void setStatuses(List<StatusItem> data) {
        mStatuses = data;
        System.out.println("Set the list of statuses to adapter.");
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
