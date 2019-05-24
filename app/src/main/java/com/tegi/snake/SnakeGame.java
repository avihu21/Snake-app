package com.tegi.snake;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

class SnakeGame extends SurfaceView implements Runnable {

    //objects for the game loop/thread
    private Thread mThread = null;

    //contrl pausing between updates
    private long mNextFrameTime;

    //is the game currently playing and or paused?
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;

    //fro playing sound effects
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrash_ID = -1;

    //the size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;

    //how many points does the player have
    private int mScore;

    //objects for drawing
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;

    //a snake
    private Snake mSnake;

    //an apple
    private Apple mApple;

    //this is the constructor method that gets called
    //from SnakeActivity
    public SnakeGame(Context context,Point size){
        super(context);

        //work out how many pixels each block is
        int blockSize = size.x/NUM_BLOCKS_WIDE;
        //how many blocks of the same size will fit into the hight
        mNumBlocksHigh = size.y/blockSize;

        //initialize the SoundPool
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

            mSP = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build();

        }else{
            mSP = new SoundPool(5,AudioManager.STREAM_MUSIC,0);
        }
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            //prepare the sounds in memory
            descriptor = assetManager.openFd("get_apple.ogg");
            mEat_ID = mSP.load(descriptor,0);

            descriptor = assetManager.openFd("snake_death.ogg");
            mCrash_ID = mSP.load(descriptor,0);
        }catch (IOException e){
            //Error
        }

        //initialize the drawing objects
        mSurfaceHolder = getHolder();
        mPaint = new Paint();

        //call the constructors of our two game objects
        mApple = new Apple(context,new Point(NUM_BLOCKS_WIDE,mNumBlocksHigh),blockSize);

        mSnake = new Snake(context,new Point(NUM_BLOCKS_WIDE,mNumBlocksHigh),blockSize);

    }

    //called to start a new game
    public void newGame(){
        //reset the snake
        mSnake.reset(NUM_BLOCKS_WIDE,mNumBlocksHigh);

        //get the apple ready for dinner
        mApple.spawn();

        //reset the mScore
        mScore = 0;

        //setup mNextFrameTime so an update can be triggered
        mNextFrameTime = System.currentTimeMillis();

    }

    //handles the game loop
    @Override
    public void run(){
        while (mPlaying){
            if(!mPaused){
                //update 10 times a second
                if(updateRequired()){
                    update();
                }
            }

            draw();
        }
    }

    //check to see if it is time for an update
    public boolean updateRequired(){
        //run at 10 frames per second
        final long TARGET_FPS = 10;
        //there are 1000 miliseconds in a second
        final long MILLIS_PER_SECONDS = 1000;

        //are we due to update the frame
        if(mNextFrameTime <= System.currentTimeMillis()){
            //tenth of a second has passed

            //setuup when the next update will be triggered
            mNextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECONDS/TARGET_FPS;

            //return true so that the update and draw methods are executed
            return true;
        }

        return false;
    }

    //update all the game objects
    public void update(){
        //move the snake
        mSnake.move();

        //did the head of the snake eat the apple?
        if (mSnake.checkDinner(mApple.getLocation())){
            
            mApple.spawn();

            //add to mScore
            mScore = mScore+1;

            //play a sound
            mSP.play(mEat_ID,1,1,0,0,1);
        }

        //did the snake die?
        if (mSnake.detectDeath()){
            //pause the game ready to start again
            mSP.play(mCrash_ID,1,1,0,0,1);

            mPaused = true;
        }
    }

    //do all the drawing
    public void draw(){
        //get a lock on the mCanvas
        if (mSurfaceHolder.getSurface().isValid()){
            mCanvas = mSurfaceHolder.lockCanvas();

            //fill the screen with color
            mCanvas.drawColor(Color.argb(255,26,128,182));

            //set the size and color of the mPaint for the text
            mPaint.setColor(Color.argb(255,255,255,255));
            mPaint.setTextSize(120);

            //draw the score
            mCanvas.drawText("" + mScore,20,120,mPaint);

            //draw the apple and the snake
            mApple.draw(mCanvas,mPaint);
            mSnake.draw(mCanvas,mPaint);

            //draw some text while paused
            if(mPaused){

                //set the size and color of mPaintfor the text
                mPaint.setColor(Color.argb(255,255,255,255));
                mPaint.setTextSize(250);

                //draw the massage
                
                mCanvas.drawText("Tap To Play!",200,700,mPaint);
            }
            //unlock the canvas to show graphics for this frame
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        switch (motionEvent.getAction()&MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_UP:
                if(mPaused){
                    mPaused = false;
                    newGame();

                    //dont want to process snake
                    //direction for the tap
                    return true;
                }

                //let the snake class handle the input
                mSnake.switchHeading(motionEvent);
                break;
                default:
                    break;
        }
        return true;
    }

    //stop the thread
    public void pause(){
        mPlaying = false;
        try{
            mThread.join();
        }catch (InterruptedException e){
            //Error
        }
    }

    //start the thread
    public void resume(){
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }
}
