package com.oct.bun;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends AppCompatActivity {

    private InterstitialAd mInterstitialAd;
    public static int k=0;
    private boolean isMute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7787182385819156/6994345036");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        Button mMyButton = (Button) findViewById(R.id.mMyButton);

        mMyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                k++;
                if(k%5==0) {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Log.d("TAG", "The interstitial wasn't loaded yet.");

                    }
                }else
                    startActivity(new Intent(MainActivity.this, GameActivity.class));
            }
        });


        // findViewById(R.id.play).setOnClickListener((view) -> {
        //      @Override
        //    public void onClick(View view) {



        //});



        TextView highScoreTxt = findViewById(R.id.highScore_txt);
        SharedPreferences prefs = getSharedPreferences("game", MODE_PRIVATE);
        highScoreTxt.setText("HighScore "+prefs.getInt("highscore",0));


        isMute = prefs.getBoolean("isMute", false);

        //  ImageView.volumeCtrl = findViewById(R.id.volumeCtrl);
        ImageView volumeCtrl = findViewById(R.id.VolumeCtrl);

        if(isMute)
            volumeCtrl.setImageResource(R.drawable.ic_baseline_volume_off_24);
        else
            volumeCtrl.setImageResource(R.drawable.ic_baseline_volume_up_24);

        volumeCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMute = !isMute;
                if(isMute)
                    volumeCtrl.setImageResource(R.drawable.ic_baseline_volume_off_24);
                else
                    volumeCtrl.setImageResource(R.drawable.ic_baseline_volume_up_24);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isMute", isMute);
                editor.apply();
            }
        });
        //}


    }}