package com.clilystudio.funcolor;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    BackgroundSound mBackgroundSound = new BackgroundSound();
    private MediaPlayer mMediaPlayer;
    private TextView mContentView;
    private int mHue = 64;
    private int mSaturation = 146;
    private int mLightness = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContentView = (TextView) findViewById(R.id.tv_title);
        if (mContentView != null) {
//            contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

            mContentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            mContentView.setBackgroundColor(getColorFromHSL());
            mContentView.invalidate();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBackgroundSound.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackgroundSound.cancel(true);
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            mMediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    private int getColorFromHSL() {
        double r, g, b;
        double h, s, l;

        h = mHue / 240.0;
        s = mSaturation / 240.0;
        l = mLightness / 240.0;


        if (mSaturation == 0) {
            r = g = b = l;
        } else {
            double q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            double p = 2 * l - q;
            r = hue2rgb(p, q, h + 1.0 / 3.0);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 1.0 / 3.0);
        }
        int argb = Color.argb(255, (int) (r * 255), (int) (g * 255), (int) (b * 255));
        return argb;
    }

    private double hue2rgb(double p, double q, double t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1.0 / 6.0) return p + (q - p) * 6.0 * t;
        if (t < 1.0 / 2.0) return q;
        if (t < 2.0 / 3.0) return p + (q - p) * (2.0 / 3.0 - t) * 6.0;
        return p;
    }

    public class BackgroundSound extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (mMediaPlayer == null) {
                mMediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.bg);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.setVolume(100, 100);
                mMediaPlayer.start();
            } else {
                mMediaPlayer.start();
            }
            return null;
        }
    }
}
