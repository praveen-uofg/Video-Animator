package com.github.videoanimatior;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    private MediaPlayer mediaPlayer;

    private TextureView textureView;
    private TextureView textureView2;
    private CardView textureView2Container;

    private Surface mSurface1;
    private Surface mSurface2;
    private Surface mActiveSurface;

    private Handler handler;
    private Runnable runnable;

    AnimatorSet set;
    Animator anim;
    float hypotenuse;
    float pixelDensity;
    private Animation alphaAppear, alphaDisappear;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.txtview);
        textureView2 = findViewById(R.id.txtview2);
        textureView2Container = findViewById(R.id.textureViewContainer2);

        textureView2Container.setVisibility(View.GONE);
        //textureView2Container.setAlpha(0f);
        textureView2.setVisibility(View.GONE);
        initMediaPlayer();


        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.e(TAG, "first surface created!");
                mSurface1=new Surface(surface);
                mActiveSurface = mSurface1;
                mediaPlayer.prepareAsync();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.e(TAG, "first surface destroyed!");
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                //Log.e(TAG, "first surface updated!");

            }
        });
        textureView2.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.e(TAG, "second surface created!");
                mSurface2 = new Surface(surface);
                mActiveSurface = mSurface2;
                mediaPlayer.setSurface(mActiveSurface);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.e(TAG, "second surface destroyed!");
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                //Log.e(TAG, "second surface updated!");

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(runnable);
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videoplayback));
            mediaPlayer.setOnPreparedListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.setSurface(mActiveSurface);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.start();

            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runnable = this;
                    createCircular();
                    handler.postDelayed(runnable, 5000);
                }
            }, 5000);

        }
    }

    private void switchViews() {
        if (mediaPlayer == null) {
            return;
        }
        if (mActiveSurface == mSurface1) {
            mActiveSurface = mSurface2;
            textureView2Container.setVisibility(View.VISIBLE);
            textureView.setVisibility(View.GONE);
        } else {
            mActiveSurface = mSurface1;
            textureView.setVisibility(View.VISIBLE);
            textureView2Container.setVisibility(View.GONE);
        }
        mediaPlayer.setSurface(mActiveSurface);
    }

    private void calculateHypotenuse() {
        int x = textureView.getWidth() / 2;
        int y = textureView.getHeight() / 2;
        hypotenuse = (float) Math.hypot(x, y);

        pixelDensity = getResources().getDisplayMetrics().density;

        alphaAppear = AnimationUtils.loadAnimation(this, R.anim.alpha_anim);
        alphaDisappear = AnimationUtils.loadAnimation(this, R.anim.alpha_disappear);
    }

    public void createCircular() {
        if (hypotenuse == 0.0f) {
            calculateHypotenuse();
        }
        if (mActiveSurface == mSurface1) {
            mActiveSurface = mSurface2;
            Log.e(TAG, "createCircular() active surface: First");
            anim = ViewAnimationUtils.createCircularReveal(textureView, textureView.getWidth() / 2, textureView.getHeight() / 2, hypotenuse,100 * pixelDensity);
            anim.setDuration(350);
        } else {
            mActiveSurface = mSurface1;
            Log.e(TAG, "createCircular() active surface: Second : widht = " + textureView.getWidth() / 2 + " | height : " + textureView.getHeight() / 2);
            anim = ViewAnimationUtils.createCircularReveal(textureView, textureView.getWidth() / 2, textureView.getHeight() / 2,100 * pixelDensity, hypotenuse);
            anim.setDuration(350);

        }
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mActiveSurface == mSurface2) {
                    textureView2Container.setVisibility(View.VISIBLE);
                    textureView2Container.startAnimation(alphaAppear);
                    //textureView2Container.setElevation(0f);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //switchViews();
                if (mActiveSurface == mSurface2) {
                    //textureView2Container.setElevation(convertDIPToPixels(4));
                    textureView2.setVisibility(View.VISIBLE);
                    mediaPlayer.setSurface(mActiveSurface);
                    textureView.setVisibility(View.GONE);

                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        if (mActiveSurface == mSurface1) {
            //mActiveSurface = mSurface1;
            textureView2.setVisibility(View.GONE);
            textureView2Container.setVisibility(View.GONE);
            textureView.setVisibility(View.VISIBLE);
            mediaPlayer.setSurface(mActiveSurface);
        }
        anim.start();
    }

    public int convertDIPToPixels(float dip) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, displayMetrics);
    }
}
