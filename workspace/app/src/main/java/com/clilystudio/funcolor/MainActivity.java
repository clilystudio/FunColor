package com.clilystudio.funcolor;

import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final String TAG = "MainActivity";
    private static final int UPDATE_COLOR = 1;
    private static final int DELAY = 1000;
    private static final int PERIOD = 60;
    private static final int ALPHA = 255;
    BackgroundSound mBackgroundSound;
    private MediaPlayer mMediaPlayer;
    private TimerHandle mHandler = new TimerHandle(this);
    private Timer mTimer;
    private TimerTask mTimerTask;
    private TextView mContentView;
    private int mHue = 160;
    private int mSaturation = 0;
    private int mLightness = 0;
    private Point mSize = new Point();
    private GestureDetector mGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindowManager().getDefaultDisplay().getSize(mSize);
        Log.e(TAG, "width=" + mSize.x + " height=" + mSize.y);
        setContentView(R.layout.activity_main);
        mContentView = (TextView) findViewById(R.id.tv_title);
        if (mContentView != null) {
            mContentView.setOnTouchListener(this);
            setContentColor();
        }
        mGestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.e(TAG, "velocityX=" + velocityX + " velocityY=" + velocityY );
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBackgroundSound = new BackgroundSound();
        mBackgroundSound.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBackgroundSound.cancel(true);
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer = null;
        }
        stopTimer();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_MENU && super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            startTimer();
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            stopTimer();
            return true;
        }
        return false;
    }

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    mLightness += 2;
                    mLightness = mLightness % 480;
                    Message message = Message.obtain(mHandler, UPDATE_COLOR);
                    mHandler.sendMessage(message);
                }
            };
        }
        mTimer.schedule(mTimerTask, DELAY, PERIOD);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    private void setContentColor() {
        int red;
        int green;
        int blue;
        float hue = mHue / 240.0f;
        float saturation = mSaturation <= 240 ? mSaturation / 240.0f : (240 - mSaturation % 240) / 240.0f;
        float lightness = mLightness <= 240 ? mLightness / 240.0f : (240 - mLightness % 240) / 240.0f;
        if (mSaturation == 0) {
            red = green = blue = Math.round(lightness * 255.0f);
        } else {
            float q = lightness < 0.5 ? lightness * (1.0f + saturation) : lightness + saturation - lightness * saturation;
            float p = 2.0f * lightness - q;
            red = hue2rgb(p, q, hue + 1.0f / 3.0f);
            green = hue2rgb(p, q, hue);
            blue = hue2rgb(p, q, hue - 1.0f / 3.0f);
        }
        mContentView.setBackgroundColor(Color.argb(ALPHA, red, green, blue));
        int bright = (int) (255.0 * (1.0 - lightness));
        mContentView.setTextColor(Color.argb(200, bright, bright, bright));
        mContentView.invalidate();
    }

    private int hue2rgb(float p, float q, float t) {
        if (t < 0) {
            t += 1.0;
        }
        if (t > 1) {
            t -= 1.0;
        }
        float color;
        if (t * 6.0 < 1.0) {
            color = p + (q - p) * 6.0f * t;
        } else if (t * 2.0 < 1.0) {
            color = q;
        } else if (t * 3.0 < 2.0) {
            color = p + (q - p) * (4.0f - t * 6.0f);
        } else {
            color = p;
        }
        return Math.round(color * 255.0f);
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

    static class TimerHandle extends Handler {
        WeakReference<MainActivity> mMainActivity;

        TimerHandle(MainActivity activity) {
            mMainActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            MainActivity mainActivity = mMainActivity.get();
            switch (msg.what) {
                case UPDATE_COLOR:
                    mainActivity.setContentColor();
                    break;
                default:
                    break;
            }
        }
    }
}
