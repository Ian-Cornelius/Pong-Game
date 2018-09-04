package com.example.ian.pong;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.graphics.Point;

public class MainActivity extends AppCompatActivity {

    //Gameview will be the view of the game
    //It will also hold the logic of the game
    //and respond to screen touches as well
    GameView gameView;

    //To store speeds
    //static float ballXVelocity;
    //static float ballYVelocity;
    //static float paddleSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get a Display object to access screen details
        Display display = getWindowManager().getDefaultDisplay();

        //Load the resolution into a Point object
        //Needed according to design of the getSize() method of Display
        Point size = new Point();
        display.getSize(size);

        //Initialize gameView and set is as the View
        gameView = new GameView(this, size.x, size.y);
        setContentView(gameView);
    }

    //This method executes when the player starts the game, after pausing from the app
    //say, pressed the home key
    @Override
    protected void onResume(){
        super.onResume();

        //Paddle.mPaddleSpeed = paddleSpeed;
        //Ball.mXVelocity = ballXVelocity;
        //Ball.mYVelocity = ballYVelocity;
        //tell the gameView resume method to execute
        gameView.resume();
        //basically calls the thread running the game to resume playing
    }

    //This method executes when the player quits the game
    @Override
    protected void onPause(){
        super.onPause();;

        //Tell the gameView pause method to execute
        //ballXVelocity = Ball.mXVelocity;
        //ballYVelocity = Ball.mYVelocity;
        //paddleSpeed = Paddle.mPaddleSpeed;
        gameView.pause();
    }
}
