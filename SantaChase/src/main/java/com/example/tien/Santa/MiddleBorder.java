package com.example.tien.Santa;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by tien on 06/01/2016.
 */


public class MiddleBorder extends GameObject{
    private Bitmap image;

    public MiddleBorder(Bitmap res, int x, int y, int h)
    {
        height = 100;
        width = 20;

        this.x = x ;
        this.y = y;

        dx = GamePanel.MOVESPEED;




        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }
    public void update()
    {
        x+=dx;
    }
    public void draw(Canvas canvas)
    {
        try{canvas.drawBitmap(image,x,y,null);}catch(Exception e){};
    }

}