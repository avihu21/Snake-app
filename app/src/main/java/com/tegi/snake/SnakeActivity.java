package com.tegi.snake;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Point;
import android.view.Display;

public class SnakeActivity extends Activity {

    //declare an instance of snake game
    SnakeGame mSnakeGame;

    //set the game up
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get the pixel dimensions of the screen
        Display display = getWindowManager().getDefaultDisplay();

        //initialize the result into a point object
        Point size = new Point();
        display.getSize(size);

        //create a new instance of the SnakeEngine class
        mSnakeGame = new SnakeGame(this,size);

        //make snakeEngine the view of the activity
        setContentView(mSnakeGame);
    }

    //start the thread in snakeEngine
    @Override
    protected void onResume(){
        super.onResume();
        mSnakeGame.resume();
    }

    //stop the thread in snakeEngine
    @Override
    protected void onPause(){
        super.onPause();
        mSnakeGame.pause();
    }
}
