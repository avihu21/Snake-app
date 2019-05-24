package com.tegi.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.Random;

class Apple {
    //the location of the apple on the grid
    //not in pixels
    private Point mLocation = new Point();

    //the range of values we can choose from
    //to spawn an apple
    private Point mSpawnRange;
    private int mSize;

    //an image to represent the apple
    private Bitmap mBitmapApple;


    //set up the apple in the constructor
    Apple(Context context,Point sr ,int s){
        //make a note of the passed in spaen range
        mSpawnRange = sr;

        //make a note of the size of an apple
        mSize = s;

        //hide the apple off-screen location until the game starts
        mLocation.x = -10;

        //load the image to the bitmap
        mBitmapApple = BitmapFactory.decodeResource(context.getResources(),R.drawable.apple);

        //resize the bitmap
        mBitmapApple = Bitmap.createScaledBitmap(mBitmapApple,s,s,false);
    }

    //this is called every time an apple is eaten
    void spawn(){
        //choose two random values and place the apple
        Random random = new Random();
        mLocation.x = random.nextInt(mSpawnRange.x)+1;
        mLocation.y = random.nextInt(mSpawnRange.y-1)+1;
    }

    //let SnakeGame know where the apple is
    //SnakeGame can share this with snake
    Point getLocation(){
        return mLocation;
    }

    //draw the apple
    void draw(Canvas canvas,Paint paint){
        canvas.drawBitmap(mBitmapApple,mLocation.x*mSize,mLocation.y*mSize,paint);
    }
}
