package com.kaodim.messenger.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.kaodim.messenger.R;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Kanskiy on 13/10/2016.
 */
public class ImageViewerActivity extends BaseBackButtonActivity {
    ViewPager viewPager;
    TextView tvCurrentPosition;
    Animation animSlideDown;
    Animation animSlideUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setTitle("");

        animSlideDown = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);
        animSlideUp = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);
        setContentView(R.layout.activity_image_viewer);
        Intent intent = getIntent();
        int currentPosition = intent.getIntExtra("currentPosition", 0);
        final ArrayList<String> imageUrls = intent.getStringArrayListExtra("photos");

        tvCurrentPosition = (TextView)findViewById(R.id.tvCurrentPosition);
        setTvCurrentPosition(currentPosition,imageUrls.size());
        viewPager  =(ViewPager)findViewById(R.id.viewPager);
        viewPager.setAdapter(new MyPagerAdapter(this, currentPosition, imageUrls, new TapListener() {
            @Override
            public void onTap() {
                //hide or show actionbar

                if (getSupportActionBar().isShowing()) {
                    getSupportActionBar().hide();
                    tvCurrentPosition.startAnimation(animSlideDown);
                } else {
                    getSupportActionBar().show();
                    tvCurrentPosition.startAnimation(animSlideUp);
                }

            }
        }));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                setTvCurrentPosition(position,imageUrls.size());
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setCurrentItem(currentPosition);

    }
    private void setTvCurrentPosition(int current, int max){
        if (max==0 || max==1){
            tvCurrentPosition.setText("");
            return;
        }
        tvCurrentPosition.setText((current+1)+"/"+max);
    }

    interface TapListener{
        void onTap();
    }
    private class MyPagerAdapter extends PagerAdapter {
        LayoutInflater inflater;
        Context mContext;
        ArrayList<String> photos;
        int				object_id;
        TapListener tapListener;

        public MyPagerAdapter(Context context, int object_id, ArrayList<String> photos, TapListener tapListener) {
            this.object_id = object_id;
            this.mContext = context;
            this.photos = photos;
            this.tapListener=tapListener;
        }



        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View itemView = inflater.inflate(R.layout.item_image_viewer, container, false);
            AQuery aq = new AQuery(itemView);
            final PhotoViewAttacher mAttacher = new PhotoViewAttacher( aq.id(R.id.imageView).getImageView());
            aq.id(R.id.imageView).progress(R.id.progressBar).image(photos.get(position), true , true,1080, 0,new BitmapAjaxCallback(){
                @Override
                protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
                    super.callback(url, iv, bm, status);
                    mAttacher.update();
                }
            });
            mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float v, float v1) {
                    tapListener.onTap();
                }
                @Override
                public void onOutsidePhotoTap() {

                }
            });

            (container).addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((RelativeLayout) object);

        }

    }

}