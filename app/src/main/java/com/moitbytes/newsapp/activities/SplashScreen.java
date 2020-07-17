package com.moitbytes.newsapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.moitbytes.newsapp.R;

public class SplashScreen extends AppCompatActivity
{
    //Variables
    private static int SPLASH_SCREEN = 2000;
    Animation topAnim, bottomAnim;
    ImageView image;
    TextView logo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.welcome_screen);

        //Animations
        topAnim = AnimationUtils.loadAnimation(SplashScreen.this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(SplashScreen.this, R.anim.bottom_animation);

        //Hooks
        image = findViewById(R.id.ImageView);
        logo = findViewById(R.id.textView);

        image.setAnimation(topAnim);
        logo.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_SCREEN);


    }
}
