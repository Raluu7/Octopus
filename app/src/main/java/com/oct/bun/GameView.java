package com.oct.bun;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying, isGameOver = false;
    private com.oct.bun.Background background1, background2;
    private Paint paint;
    private com.oct.bun.Flight flight;
    private com.oct.bun.GameActivity activity;
    private com.oct.bun.Bird[] birds;
    private SharedPreferences prefs;
    private Random random;
    private SoundPool soundPool;
    private int sound;
    private List<com.oct.bun.Bullet> bullets;
    private int screenX, screenY, score=0;
    public static float screenRatioX, screenRatioY;

    // public static int k=0;

    public GameView(com.oct.bun.GameActivity activity, int screenX, int screenY){
        super(activity);

        this.activity = activity;

        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);

        //  sound = soundPool.load(activity,R.raw.shoot, 1);
        sound = soundPool.load(activity, R.raw.shoot, 1);
        this.screenX = screenX;
        this.screenY = screenY;
        if(screenX>=1920) //nu
            screenRatioX = 1920f / screenX; //da
        else //nu
            screenRatioX = screenX / 1920f;  //nu

        if(screenY >= 1080)
            screenRatioY = 1080f / screenY;
        else
            screenRatioY = screenY / 1080f;


        background1 = new com.oct.bun.Background(screenX, screenY, getResources());
        background2 = new com.oct.bun.Background(screenX, screenY, getResources());

        flight = new com.oct.bun.Flight(this, screenY, getResources());

        bullets = new ArrayList<>();

        background2.x = screenX;

        paint = new Paint();
        paint.setTextSize(128*screenRatioX);
        paint.setColor(Color.BLACK);
        birds = new com.oct.bun.Bird[4];

        for ( int i=0; i<4; i++){
            com.oct.bun.Bird bird = new com.oct.bun.Bird(getResources());
            birds[i]=bird;

        }

        random = new Random();
    }

    @Override
    public void run() {

        while (isPlaying){

            update ();
            draw();
            sleep ();

        }
    }

    private void update (){

        background1.x -= 10 * screenRatioX;
        background2.x -=  10 * screenRatioX;

        if(background1.x + background1.background.getWidth()< 0){
            background1.x = background2.x + background2.background.getWidth();
        }

        if(background2.x + background2.background.getWidth()< 0){
            background2.x = background1.x + background1.background.getWidth();
        }

        if(flight.isGoingUp)
            flight.y -= 30 * screenRatioY;
        else
            flight.y += 30 * screenRatioY;

        if(flight.y < 0)
            flight.y = 0; //sa vezi daca iese din ecran

        if(flight.y > screenY - flight.height)
            flight.y = screenY - flight.height;

        List<com.oct.bun.Bullet> trash = new ArrayList<>();

        for(com.oct.bun.Bullet bullet : bullets){
            if(bullet.x > screenX)
                trash.add(bullet);

            bullet.x += 50 * screenRatioX;

            for(com.oct.bun.Bird bird: birds){
                if(Rect.intersects(bird.getCollisionShape(), bullet.getCollisionShape())){

                    score++;
                    bird.x = -500;
                    bullet.x = screenX + 500;
                    bird.wasShot = true;

                }
            }

        }
        for (com.oct.bun.Bullet bullet: trash)
            bullets.remove(bullet);

        for(com.oct.bun.Bird bird : birds){
            bird.x -= bird.speed;
            if(bird.x + bird.width < 0){

                if(!bird.wasShot){
                    isGameOver = true;
                    return;
                }
                int bound = (int) (30 * screenRatioX);
                bird.speed = (int) (random.nextInt(bound)*screenRatioX);

                if(bird.speed < 10 * screenRatioX)
                    bird.speed = (int) (10 * screenRatioX);

                bird.x = screenX;
                bird.y = random.nextInt(screenY - bird.height);

                bird.wasShot = false;
            }
            if(Rect.intersects(bird.getCollisionShape(), flight.getCollisionShape())){

                isGameOver = true;
                return;
            }
        }
    }

    private void draw () {
        if(getHolder().getSurface().isValid()){
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);

            for(com.oct.bun.Bird bird : birds)
                canvas.drawBitmap(bird.getBird(), bird.x, bird.y,paint);


            canvas.drawText(score + "", screenX / 2f,  164, paint);

            if(isGameOver){
                isPlaying = false;

                // reclama
                // k++;
                canvas.drawBitmap(flight.getDead(), flight.x, flight.y, paint);
                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();
                waitBeforeExiting();

                return;
            }
            //if(k%10==0)


            canvas.drawBitmap(flight.getflight(), flight.x, flight.y, paint);


            for(com.oct.bun.Bullet bullet : bullets)
                canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y,paint);

            getHolder().unlockCanvasAndPost(canvas);


        }

    }

    private void waitBeforeExiting() {
        try {
            Thread.sleep(3000);
            activity.startActivity(new Intent(activity, MainActivity.class));
            activity.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void saveIfHighScore() {

        if(prefs.getInt("highscore", 0) < score){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highscore", score);
            editor.apply();
        }
    }

    private  void  sleep (){

        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void resume() {

        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }
    public void pause(){

        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < screenX / 2){
                    flight.isGoingUp = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                flight.isGoingUp = false;
                if (event.getX() > screenX / 2)
                    flight.toShoot++;
                break;
        }

        return true;
    }

    public void newBullet() {

        //  if(!prefs.getBoolean("isMute", false))
        //    soundPool.play(sound,1,1,0,0,1);

        if(!prefs.getBoolean("isMute",false))
            soundPool.play(sound,1,1,0,0,1);
        com.oct.bun.Bullet bullet = new com.oct.bun.Bullet((getResources()));
        bullet.x = flight.x + flight.width;
        bullet.y = flight.y + (flight.height / 2);
        bullets.add(bullet);
    }
}
