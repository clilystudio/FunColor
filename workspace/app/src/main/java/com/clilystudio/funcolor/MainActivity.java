/*
 * Copyright (C) 2016 ClilyStudio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.clilystudio.funcolor;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
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
    private static final int MIN_DISTANCE = 64;
    private static final int MIN_VELOCITY = 240;
    private static final int MAX_VELOCITY = 24000;
    private static final String KEY_HUE = "Hue";
    private static final String KEY_SATURATION = "Saturation";
    private static final String KEY_LIGHTNESS = "Lightness";
    BackgroundSound mBackgroundSound;
    private MediaPlayer mMediaPlayer;
    private TimerHandle mHandler = new TimerHandle(this);
    private Timer mTimer;
    private TimerTask mTimerTask;
    private TextView mContentView;
    private int mHue = 160;
    private int mSaturation = 80;
    private int mLightness = 0;
    private Point mSize = new Point();
    private GestureDetector mGestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPrefs();
        getWindowManager().getDefaultDisplay().getSize(mSize);
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
                float distanceX = Math.abs(e1.getX() - e2.getX());
                float distanceY = Math.abs(e1.getY() - e2.getY());
                if (distanceX > MIN_DISTANCE) {
                    if (Math.abs(velocityX) > MIN_VELOCITY) {
                        distanceX *= (1.0f + Math.abs(velocityX) / MAX_VELOCITY);
                    }
                }
                if (distanceY > MIN_DISTANCE) {
                    if (Math.abs(velocityY) > MIN_VELOCITY) {
                        distanceY *= (1.0f + Math.abs(velocityY) / MAX_VELOCITY);
                    }
                }
                int mOffsetX = (int) (12.0f * distanceX / mSize.x);
                if (velocityX > 0) {
                    mHue += mOffsetX;
                } else {
                    mHue -= mOffsetX;
                }
                if (mHue < 0) {
                    mHue += 240;
                }
                if (mHue >= 240) {
                    mHue = mHue % 240;
                }

                int mOffsetY = (int) (24.0f * distanceY / mSize.y);
                if (velocityY > 0) {
                    mSaturation += mOffsetY;
                } else {
                    mSaturation -= mOffsetY;
                }
                if (mSaturation < 0) {
                    mSaturation += 480;
                }
                if (mSaturation >= 480) {
                    mSaturation = mSaturation % 480;
                }
                setContentColor();
                return false;
            }
        });
    }

    private void loadPrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mHue = prefs.getInt(KEY_HUE, 160);
        mSaturation = prefs.getInt(KEY_SATURATION, 160);
        mLightness = prefs.getInt(KEY_LIGHTNESS, 160);
    }

    private void savePrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_HUE, mHue);
        editor.putInt(KEY_SATURATION, mSaturation);
        editor.putInt(KEY_LIGHTNESS, mLightness);
        editor.apply();
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
        savePrefs();
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
