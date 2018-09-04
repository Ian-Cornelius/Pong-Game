package com.example.ian.pong;

/*
Notice. We implement runnable so we have a thread and can override the run method

Thus, runnable apparently allows us to have a thread, and override the run method

I think it is sort of an interface between the running thread and the class it was started from
 */

import android.graphics.Color;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.SoundPool;
import android.content.Context;
import android.os.Build;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.content.res.AssetManager;
import android.content.res.AssetFileDescriptor;
import android.widget.Toast;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.io.IOException;

public class GameView extends SurfaceView implements Runnable {

    //This is our thread
    Thread mGameThread = null;

    //This is new. We need a SurfaceHolder when we use Paint and Canvas in a thread
    //We will see it in action soon.
    //Personally, I think SurfaceHolder shows the thread which surface it should be painting on, in this case, screen
    SurfaceHolder mOurHolder;

    //A boolean which will be set/unset when the game is running or not.
    //Volatile means it can be accessed both inside and outside our thread.
    volatile boolean mPlaying;

    //Game is mPaused at the start
    boolean mPaused = true;

    //A Canvas and a Paint object
    Canvas mCanvas;
    Paint mPaint;

    //This variable tracks the game's frame rate
    long mFPS;

    //the size of the screen in pixels
    int mScreenX;
    int mScreenY;

    //The player's mPaddle
    Paddle mPaddle;

    //A mBall
    Ball mBall;

    //For sound FX;
    SoundPool sp;
    int beep1ID = -1;
    int beep2ID = -1;
    int beep3ID = -1;
    int loseLifeID = -1;
    int explodeID = -1;

    //The mScore
    //Personally set it to static so that I can look it up in paddle class
    static int mScore = 0;

    //Lives
    static int mLives = 3;
    //changed to static to avoid its changing when instance destroyed and recreated, but app not exited.
    //That is when user presses homescreen
    //worked, but sometimes just dropping on score

    /*
    When we call new() on gameView, this custom constructor runs.
     */
    public GameView (Context context, int x, int y){

        /*
        The next line of code asks the SurfaceView class to set up our object
         */
        super(context);

        //Set the screen width and height
        mScreenX = x;
        mScreenY = y;

        //initialize our mHolder and mPaint objects
        mOurHolder = getHolder();
        mPaint = new Paint();

        //a new mPaddle
        mPaddle = new Paddle(mScreenX, mScreenY);
        //Passing in the screen width and height so as to build the graphics appropriately

        //create an mBall
        mBall = new Ball(mScreenX, mScreenY);
        /*
        mBall and mPaddle created because the game has just been launched
         */

        /*
        Instantiate our SoundPool depending on which version of android we are running
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

            sp = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build();
        }
        else{
            sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        try{
            //We now want to play our audio
            //create objects for the two required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            //Load our fx in memory ready for use
            descriptor = assetManager.openFd("beep1.ogg");
            beep1ID = sp.load(descriptor,0);

            descriptor = assetManager.openFd("beep2.ogg");
            beep2ID = sp.load(descriptor, 0);

            descriptor = assetManager.openFd("beep3.ogg");
            beep3ID = sp.load(descriptor, 0);

            descriptor = assetManager.openFd("loseLife.ogg");
            loseLifeID = sp.load(descriptor,0);

            descriptor = assetManager.openFd("explode.ogg");
            explodeID = sp.load(descriptor,0);

        }catch (IOException e){

            //Print an error message to the console
            //Toast.makeText(getContext(), "Failed to load soundfiles", Toast.LENGTH_LONG);
            Log.e("Error", "Failed to load sound files");

        }

        setupAndRestart();//This will start the game
    }

    //setupAndRestart method that will be called when the game starts or after you've lost a life.
    public void setupAndRestart(){

        //Out of lives
        //put the mBall back to the start
        mBall.reset(mScreenX,mScreenY);
        mPaddle.reset(mScreenX, mScreenY);

        //if game over, reset scores and mLives
        if (mLives == 0){
            mScore= 0;
            mLives = 3;
        }
    }

    @Override
    public void run(){

        /*
        This is the code that will be running inside the thread
         */
        while (mPlaying){

            //capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            //update the frame
            if (!mPaused){
                update();//This is the update method of the GameView class
            }

            //draw the frame
            draw();

            /*
            Calculate the fps of this frame.
            We can then use the result to time animations in the update methods
             */
            long timeThisFrame = System.currentTimeMillis() - startFrameTime;

            if(timeThisFrame >= 1){
                //here so as to avoid division by zero
                mFPS = 1000/timeThisFrame;
                //Now understanding why FPS has to do with your processing speed
            }
        }

        //save the current speed of the ball and paddle. Seen inapotea
        //Okay, both lives and speed of the ball - lives increased, speed reduced. Main score remains as it is
        //Change lives to static. So its not related to the instance, until point of reset, lives=0
    }

    //Everything that needs updating goes in here.
    //Movement, collision detection, e.t.c
    public void update(){

        //move the mPaddle if required
        mPaddle.update(mFPS);

        mBall.update(mFPS);

        //Check if the ball and paddle have collided
        if (RectF.intersects(mPaddle.getRect(), mBall.getRect())){

            //set the ball rising in screen
            mBall.setRandomXVelocity();// doesn't work as desired
            //mBall.reverseXVelocity();
            mBall.reverseYVelocity();

            //avoid the ball not getting stuck on the paddle
            mBall.clearObstacleY(mPaddle.getRect().top - 2);//changed from 2 to 50

            //increase game speed
            mBall.increaseVelocity();
            //paddle speed set relative to ball speed
            mPaddle.increaseVelocity();

            //update score. Play beep sound
            mScore++;

            sp.play(beep1ID, 1,1,0,0,1);
        }

        //Bounce the mBall back if it hits the bottom of the screen
        if (mBall.getRect().bottom > mScreenY - 10){

            mBall.reverseYVelocity();//I have chained reset to see if it will help
            mBall.clearObstacleY(mScreenY - 120);//changed from -100 to -120

            //lose a life
            mLives--;
            sp.play(loseLifeID, 1,1,0,0,1);
            //mBall.reset(mScreenX,mScreenY);
            //mPaddle.reset(mScreenX,mScreenY);


            //end the game if out of lives
            if (mLives == 0){
                mPaused = true;
                setupAndRestart();
            }
        }

        //Bounce the mBall back if it hits the top of the screen
        if (mBall.getRect().top < 0){//changed from 0

            mBall.reverseYVelocity();
            mBall.clearObstacleY(100);

            sp.play(beep2ID, 1,1,0,0,1);
        }

        //if the mBall hits the left of the screen, reverse X direction
        if(mBall.getRect().left < 0){

            mBall.reverseXVelocity();
            mBall.clearObstacleX(2);

            sp.play(beep3ID,1,1,0,0,1);
        }

        //if mBall hits the right wall, bounce
        if (mBall.getRect().right > mScreenX){
            mBall.reverseXVelocity();
            mBall.clearObstacleX(mScreenX - 100);//previously 22

            sp.play(beep3ID,1,1,0,0,1);
        }
    }

    //draw the newly updated scene
    public void draw(){

        //make sure our drawing surface is valid or we crash
        if (mOurHolder.getSurface().isValid()){

            //draw everything here, concerning the scene
            //Lock the mCanvas ready to draw
            mCanvas = mOurHolder.lockCanvas();

            //draw the background color
            mCanvas.drawColor(Color.argb(255,26,128,182));

            //choose the brush color for our drawing
            mPaint.setColor(Color.argb(255,255,255,255));

            //draw the mPaddle
            mCanvas.drawRect(mPaddle.getRect(),mPaint);

            //draw the mBall
            mCanvas.drawRect(mBall.getRect(), mPaint);

            //choose the brush color for drawing
            mPaint.setColor(Color.argb(255,255,255,255));

            //draw the mScore
            mPaint.setTextSize(40);
            mCanvas.drawText("Score: " + mScore + " Lives: " + mLives,10,50,mPaint);

            //draw everything to the screen
            mOurHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    //if the activity is paused/stopped, stop our thread
    public void pause(){

        mPlaying = false;
        try{
            mGameThread.join();
        }catch(InterruptedException e){
            Toast.makeText(getContext(),"Error joining thread",Toast.LENGTH_LONG);
        }
    }

    //if the activity starts or restarts, start our thread
    public void resume(){
        mPlaying = true;
        mGameThread = new Thread(this);
        mGameThread.start();
    }

    //SurfaceView implements onTouchListener
    //So we can override this and method and detect screen touches
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        //check whether player touched left/right of screen
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){

            //player has touched the screen
            case MotionEvent.ACTION_DOWN:
                mPaused = false;

                //Is the touch on the right or left?
                if (motionEvent.getX() > mScreenX/2){

                    //touch to the right.
                    //Move paddle to the right
                    mPaddle.setMovementState(mPaddle.RIGHT);
                }
                else{
                    mPaddle.setMovementState(mPaddle.LEFT);
                }
                break;
        }
        return true;
    }


}
