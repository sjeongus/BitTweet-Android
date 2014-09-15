package org.bittweet.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.koushikdutta.ion.Ion;

import org.bittweet.android.R;
import org.bittweet.android.ui.util.ExtendedViewPager;
import org.bittweet.android.ui.util.TouchImageView;

public class ImageViewerActivity extends FragmentActivity {
    /**
     * Step 1: Download and set up v4 support library: http://developer.android.com/tools/support-library/setup.html
     * Step 2: Create ExtendedViewPager wrapper which calls TouchImageView.canScrollHorizontallyFroyo
     * Step 3: ExtendedViewPager is a custom view and must be referred to by its full package name in XML
     * Step 4: Write TouchImageAdapter, located below
     * Step 5. The ViewPager in the XML should be ExtendedViewPager
     */

    private String[] media;
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageviewer);

        media = getIntent().getStringArrayExtra("MEDIA");
        int position = getIntent().getIntExtra("POSITION", 0);

        activity = ImageViewerActivity.this;

        ExtendedViewPager mViewPager = (ExtendedViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new TouchImageAdapter());
        mViewPager.setCurrentItem(position);
    }

    public class TouchImageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return media.length;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            TouchImageView img = new TouchImageView(container.getContext());
            Ion.with(img).error(R.drawable.image_error).animateGif(true).load(media[position]);
            container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.finish();
                }
            });
            return img;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }
}
