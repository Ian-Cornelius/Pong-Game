package com.example.ian.pong;

/*
This is the game's engine'

I bet the rectangles start with the top left position, then other points provided relative to it
 */

import android.graphics.RectF;

public class Paddle {

    //RectF is an object that holds four coordinates, just what we need
    private RectF mRect;

    //How long and high our mPaddle will be
    private float mLength;
    private float mHeight;

    //x is the far left of the rectangle, which forms our mPaddle
    private float mXCoord;

    //y is the top coordinate
    private float mYCoord;

    //This will hold the pixel per second speed the mPaddle will travel with
    //Changed to static cause of pause problems
    static float mPaddleSpeed = 0;

    //which ways can the mPaddle move
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    //is the mPaddle moving and in which direction
    private int mPaddleMoving = STOPPED;

    //the screen length and width in pixels
    private int mScreenX;
    private int mScreenY;

    //paddle constructor method. Will pass in screen width and height here.
    public Paddle (int x, int y){

        mScreenX = x;
        mScreenY = y;

        //paddle length 1/8 of screen length
        mLength = mScreenX/5;
        //reset to 1/5

        //paddle height 1/25 screen height
        mHeight = 50;
        //changed it to 50

        //start the mPaddle in roughly the screen center
        //mXCoord and mYCoord represent the top left corner of the rectangle
        mXCoord = mScreenX/2;
        mYCoord = mScreenY - 100;//had to raise y by 100 coz screen not full size in android 8.1

        //creating the mPaddle
        mRect = new RectF(mXCoord,mYCoord,mXCoord + mLength,mYCoord + mHeight);

        //How fast is the mPaddle in pixels per second
        if(mPaddleSpeed==0){
            mPaddleSpeed = mScreenX/3;
        }
        //cover entire screen in 1 second
        //Now changed to 1/3 of the screen per second

    }

    //public getter to return RectF to show paddle location
    //this is the getter method that makes the rectangle defining our paddle visible to the GameView class
    public RectF getRect(){
        return mRect;
    }

    //used to change whether the paddle is moving to the left, right or nowhere
    public void setMovementState(int state){
        mPaddleMoving = state;
    }

    //update method to make sure the paddle doesn't disappear off the screen
    //will be called from update in GameView
    //determines if the paddle needs to move and changes the coordinates
    //contained in mRect if necessary
    public void update (long fps){

        if (mPaddleMoving == LEFT){
            mXCoord = mXCoord - mPaddleSpeed/fps;
        }
        if (mPaddleMoving == RIGHT){
            mXCoord = mXCoord + mPaddleSpeed/fps;
        }

        //make sure its not leaving the screen
        if (mRect.left < 0){
            mXCoord = 0;
        }

        if(mRect.right > mScreenX){

            mXCoord = mScreenX - //The width of the paddle
                    (mRect.right - mRect.left);
        }

        //update the paddle graphics
        mRect.left = mXCoord;
        mRect.right = mXCoord + mLength;
    }

    //Increases paddle speed relative to ball speed
    public void increaseVelocity(){

        if (GameView.mScore>1){

            //check if paddle speed is negative or positive
            if(mPaddleSpeed<0){
                mPaddleSpeed += -Math.abs(Ball.mXVelocity/8);
            }else{

                mPaddleSpeed += Math.abs(Ball.mXVelocity/8);
            }
        }
        //paddle speed always plus 1/4 of ball speed
    }

    //Called when out of lives, starting a new game
    public void reset (int x, int y){
        mRect.left = x/2;//starting it at the middle
        mRect.top = y - 100;//trying to set it right above the paddle
        mRect.right = mRect.left + mLength;
        mRect.bottom = mRect.top + mHeight;//new code

        //reset paddle speed
        mPaddleSpeed = mScreenX/3;
    }
}
