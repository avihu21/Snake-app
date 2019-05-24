package com.tegi.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

import java.util.ArrayList;

class Snake {

    //the location in the grid of all the segments
    private ArrayList<Point> segmentLocation;

    //how big is each segment of the snake
    private int mSegmentSize;

    //how big is the entire grid
    private Point mMoveRange;

    //where is the center of the screen
    //horizontally in pixels
    private int halfWayPoint;

    //for tracking movement heading
    private enum Heading{
        UP,RIGHT,DOWN,LEFT
    }

    //start by heading to the right
    private Heading heading = Heading.RIGHT;

    //a bitmap for each direction the head can face
    private Bitmap mBitmapHeadRight;
    private Bitmap mBitmapHeadLeft;
    private Bitmap mBitmapHeadUp;
    private Bitmap mBitmapHeadDown;

    //a bitmap for the body
    private Bitmap mBitmapBody;


    //the constructor
    Snake(Context context,Point mr,int ss) {
        //initialize our ArrayList
        segmentLocation = new ArrayList<>();

        //initialize the segment size and movment
        //range from the passed in parameters
        mSegmentSize = ss;
        mMoveRange = mr;

        //create and scale the bitmaps
        mBitmapHeadRight = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);

        //create 3 more versions of the
        //head for different headings
        mBitmapHeadLeft = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);

        mBitmapHeadUp = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);

        mBitmapHeadDown = BitmapFactory.decodeResource(context.getResources(), R.drawable.head);

        //modify the bitmap to face the snake head
        //in the correct direction
        mBitmapHeadRight = Bitmap.createScaledBitmap(mBitmapHeadRight, ss, ss, false);

        //a matrix for scaling
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);

        mBitmapHeadLeft = Bitmap.createBitmap(mBitmapHeadRight, 0, 0, ss, ss, matrix, true);

        //a matrix for rotating
        matrix.preRotate(-90);
        mBitmapHeadUp = Bitmap.createBitmap(mBitmapHeadRight, 0, 0, ss, ss, matrix, true);

        //matrix operations are cumlative
        //so rotate by 180 to face down
        matrix.preRotate(180);
        mBitmapHeadDown = Bitmap.createBitmap(mBitmapHeadRight, 0, 0, ss, ss, matrix, true);

        //create and scale the body
        mBitmapBody = BitmapFactory.decodeResource(context.getResources(), R.drawable.body);

        mBitmapBody = Bitmap.createScaledBitmap(mBitmapBody, ss, ss, false);

        //the halfway point across the screen in pixels
        //used to detect which side of screen was pressed
        halfWayPoint = mr.x * ss / 2;

    }

    //get the snake ready for a new game
    void reset(int w,int h){
        //reset the heading
        heading = Heading.RIGHT;

        //delete the old contents of ArrayList
        segmentLocation.clear();

        //start with a single snake segment
        segmentLocation.add(new Point(w/2,h/2));
    }

    void move(){
        //move the body
        //start at the back and move it
        //to the position of the segment in front of it
        for (int i = segmentLocation.size() -1;i > 0; i--){
            //make it the same value as the next segment
            //going forwardsd towards the head
            segmentLocation.get(i).x = segmentLocation.get(i-1).x;

            segmentLocation.get(i).y = segmentLocation.get(i-1).y;
        }

        //move the head in the appropriate heading
        //get the exiting head position
        Point p = segmentLocation.get(0);

        //move it appropriatly
        switch (heading){
            case UP:
                p.y--;
                break;

            case RIGHT:
                p.x++;
                break;

            case DOWN:
                p.y++;
                break;

            case LEFT:
                p.x--;
                break;

        }

        //insert the adjusted point back into position 0
        segmentLocation.set(0,p);
    }

    boolean detectDeath(){
        //has the snake died
        boolean dead = false;

        //hit any of the screen edges
        if (segmentLocation.get(0).x == -1 || segmentLocation.get(0).x > mMoveRange.x || segmentLocation.get(0).y == -1 || segmentLocation.get(0).y > mMoveRange.y){
            dead =true;
        }

        //eaten itself?
        for (int i = segmentLocation.size() -1;i > 0;i--){
            //have any of the sections collided with the head
            if (segmentLocation.get(0).x == segmentLocation.get(i).x && segmentLocation.get(0).y == segmentLocation.get(i).y){
                dead = true;
            }
        }
        return dead;
    }

    boolean checkDinner(Point I){
        if (segmentLocation.get(0).x == I.x && segmentLocation.get(0).y == I.y){
            /*
                add a new point to the list
                located off screen
                this is ok because on the next call to
                move it will take the position of
                the segment in front of it
            */
            segmentLocation.add(new Point(-10,-10));

            return true;
        }

        return  false;
    }

    void draw (Canvas canvas, Paint paint){
        //dont run this code if ArrayList has nothing in it
        if (!segmentLocation.isEmpty()){
            //all the code from this method goes here

            //draw the head
            switch (heading){
                case RIGHT:
                    canvas.drawBitmap(mBitmapHeadRight,segmentLocation.get(0).x*mSegmentSize,segmentLocation.get(0).y*mSegmentSize,paint);
                    break;

                case LEFT:
                    canvas.drawBitmap(mBitmapHeadLeft,segmentLocation.get(0).x*mSegmentSize,segmentLocation.get(0).y*mSegmentSize,paint);
                    break;

                case UP:
                    canvas.drawBitmap(mBitmapHeadUp,segmentLocation.get(0).x*mSegmentSize,segmentLocation.get(0).y*mSegmentSize,paint);
                    break;

                case DOWN:
                    canvas.drawBitmap(mBitmapHeadDown,segmentLocation.get(0).x*mSegmentSize,segmentLocation.get(0).y*mSegmentSize,paint);
                    break;
            }

            //draw the snake body one block at a time
            for (int i = 1;i < segmentLocation.size();i++){
                canvas.drawBitmap(mBitmapBody,segmentLocation.get(i).x*mSegmentSize,segmentLocation.get(i).y*mSegmentSize,paint);
            }
        }
    }

    //handle changing direction
    void switchHeading(MotionEvent motionEvent){

        //is the tap on the right hand side?
        if(motionEvent.getX() >= halfWayPoint){
            switch (heading){
                //rotate right
                case UP:
                    heading = heading.RIGHT;
                    break;
                case RIGHT:
                    heading = heading.DOWN;
                    break;
                case DOWN:
                    heading = heading.LEFT;
                    break;
                case LEFT:
                    heading = heading.UP;
                    break;
            }
        } else {
            //rotate left
            switch (heading){
                case UP:
                    heading = heading.LEFT;
                    break;
                case LEFT:
                    heading = heading.DOWN;
                    break;
                case DOWN:
                    heading = heading.RIGHT;
                    break;
                case RIGHT:
                    heading = heading.UP;
                    break;
            }
        }
    }



}
