package com.example.ian.pong;

import android.graphics.RectF;
import java.util.Random;

public class Ball {

    private RectF mRect;

    //variables to control speed of ball in x and y direction
    static float mXVelocity = 0;
    static float mYVelocity = 0;
    //why changed it to static?
    //User pauses app, instance destroyed. On creation, it gets reset. Trying to solve that
    //So, in constructor, checks if 0 i.e never instantiated. Thus sets it. Otherwise, user was already playing.

    //variables to set ball size
    private float mBallWidth;
    private float mBallHeight;

    //own code
    //getter for ball's mXVelocity
    //No longer needed
    /*
    public float getmXVelocity(){

        //returning absolute value
        return Math.abs(mXVelocity);

    }*/

    //Ball constructor method. Will set size of ball relative to width of screen passed in constructor.
    //Speed set relative to height of screen, also passed in constructor
    public Ball (int screenX, int screenY){

        //make the mBall size relative to the screen resolution
        mBallWidth = 50;//screenX/100;
        mBallHeight = mBallWidth;//basically a square ball

        //start the ball travelling straight up at a speed of quarter the screen height per second
        if (mYVelocity==0 && mXVelocity==0){
            mYVelocity = screenY/4;
            mXVelocity = mYVelocity;
        }

        //initialize the RectF that represents the ball
        mRect = new RectF();
    }

    //method to help us get hold of the ball out of the class.
    //Give access to mRect
    public RectF getRect(){
        return mRect;
    }

    //update method of the ball. Set to be called once every frame of the game. Updates top and left values of the ball based on velocity
    /*
    divided by number of frames per second the device is managing to run the game at. Then, other points of mRect are updated relative
    to top left and size of the ball.

    Dividing velocity by fps ensures the same ball movement rate in all devices despite varying fps
     */

    //Change the position each frame
    public void update (long fps){
        mRect.left = mRect.left + (mXVelocity/fps);
        mRect.top = mRect.top + (mYVelocity/fps);//changed to mYVelocity
        //I guess this is setting the top left side

        //now set the other positions relative to ball size and top left
        mRect.right = mRect.left + mBallWidth;
        mRect.bottom = mRect.top + mBallHeight;
    }

    //reverse the vertical heading
    public void reverseYVelocity(){
        mYVelocity = -mYVelocity;
    }

    //reverse the horizontal heading
    public void reverseXVelocity(){
        mXVelocity = -mXVelocity;
    }

    //simply randomly reverses the x velocity
    public void setRandomXVelocity(){
        Random generator = new Random();
        //float answer = generator.nextInt(2);//previously int
        boolean reverse = generator.nextBoolean();

        if (reverse){//previously equal, i.e answer == 0
            reverseXVelocity();
        }
        //mXVelocity = answer * 100;
        //reverseXVelocity();
    }

    //Speed up by 10%
    //A score of 25 is quite tough on this setting
    public void increaseVelocity(){
        mXVelocity = mXVelocity + (mXVelocity/10);
        mYVelocity = mYVelocity + (mYVelocity/10);
    }

    //clear obstacle in Y axis
    public void clearObstacleY(float y){
        mRect.bottom = y;
        mRect.top = y - mBallHeight;
    }

    //clear obstacles in X axis
    public void clearObstacleX(float x){
        mRect.left = x;
        mRect.right = x + mBallHeight;
    }

    //called when out of lives, thus game restarted
    public void reset (int x, int y){
        mRect.left = x/2;//starting it at the middle
        mRect.top = y - 100;//trying to set it right above the paddle
        mRect.right = x/2 + mBallWidth;
        mRect.bottom = y - 100 + mBallHeight;//new code

        //reset the ball velocity
        mYVelocity = y/4;
        mXVelocity = mYVelocity;
    }
}
