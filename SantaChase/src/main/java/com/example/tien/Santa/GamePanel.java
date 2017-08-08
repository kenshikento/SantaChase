package com.example.tien.Santa;

import android.content.Context;
import android.graphics.BitmapFactory;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by tien on 28/12/2015.
 */
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long missileStartTime;
    private Random rand = new Random();
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BotBorder> botborder;
    private int minBorderHeight;
    private int maxBorderHeight;
    private boolean topDown = true;
    private boolean bottomDown = true;
    private boolean newGameCreated = false;
    private boolean firstGame = true;
    // Increase = easier, decrease = harder
    private int progressDenom = 20;
    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best;

    private int snow_flake_count = 10;
    private final List<Drawable> drawables = new ArrayList<Drawable>();
    private int[][] coords;
  //  private final Drawable snow_flake;



    public GamePanel(Context context) {
        super(context);
        // Add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);
        // Make gamePanel focusable so it can handle events
        setFocusable(true);
        //snow_flake = context.getResources().getDrawable(R.drawable.snow_flake);
      //  snow_flake = ContextCompat.getDrawable(getActivity(),R.drawable.snow_flake);
    ///    snow_flake.setBounds(0, 0, snow_flake.getIntrinsicWidth(), snow_flake
       //         .getIntrinsicHeight());
//
    }



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000) {
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
       // bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.snowymountains1));


       // player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
      //  snow_flake = context.getResources().getDrawable(R.drawable.snow_flake);
       // snow_flake = new BitmapFactory.decodeResource(getResources(), R.drawable.snow_flake);
      //  snow_flake.setBounds(0, 0, snow_flake.getIntrinsicWidth(), snow_flake
       //         .getIntrinsicHeight());

        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.dead3), 35, 60, 1);

        smoke = new ArrayList<Smokepuff>();
        missiles = new ArrayList<Missile>();
        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BotBorder>();
        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();
        thread = new MainThread(getHolder(), this);
        // We can safely start the game loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.getPlaying() && newGameCreated && reset) {
                player.setPlaying(true);
                player.setUp(true);
            }
            if (player.getPlaying()){
                if (!started) started = true;
                reset = false;
                player.setUp(true);
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update() {
        if (player.getPlaying()) {
            if (botborder.isEmpty()) {
                player.setPlaying(false);
                return;
            }
            if (topborder.isEmpty()) {
                player.setPlaying(false);
                return;
            }
            bg.update();
            player.update();
            // Calc the threshold of height the border can have based on the score
            // Max and Min border here are updated and the border switched direction when either max or min is met.
            maxBorderHeight = 30+player.getScore()/progressDenom;
            // Cap max border height so that borders can take up only a 1/2 of the screen
            if (maxBorderHeight > HEIGHT/4) maxBorderHeight = HEIGHT/4;
            minBorderHeight = 5+player.getScore()/progressDenom;

            // Check top border collision
            for (int i = 0; i < topborder.size(); i++) {
                if (collision(topborder.get(i), player)) {
                    player.setPlaying(false);
                }
            }
            // Check bottom border collision
            for (int i = 0; i < botborder.size(); i++) {
                if (collision(botborder.get(i), player)) {
                    player.setPlaying(false);
                }
            }

            // Update bottom border
            updateBottomBorder();
            // Update top border
            updateTopBorder();
            // Add missiles on timer
            long missilesElapsed = (System.nanoTime()-missileStartTime)/1000000;
            if (missilesElapsed > (2000-player.getScore()/4)) {
                // First missile always goes down the middle
                if (missiles.size() == 0) {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile), WIDTH+10, HEIGHT/2, 45, 15, player.getScore(), 13));
                } else {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH+10, (int)(rand.nextDouble()*HEIGHT-(maxBorderHeight*2))+maxBorderHeight, 45, 15, player.getScore(), 13));
                }
                missileStartTime = System.nanoTime();
            }
            for (int i = 0; i < missiles.size(); i++) {
                missiles.get(i).update();
                if (collision(missiles.get(i), player)) {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                if (missiles.get(i).getX() < -45) {
                    missiles.remove(i);
                    break;
                }
            }

            // Add smoke puffs on timer
            long elapsed = (System.nanoTime()-smokeStartTime)/1000000;
            if (elapsed > 120) {
                smoke.add(new Smokepuff(player.getX(), player.getY()+40));
                smokeStartTime = System.nanoTime();
            }
            for (int i = 0; i < smoke.size(); i++) {
                smoke.get(i).update();
                if (smoke.get(i).getX() < 10) {
                    smoke.remove(i);
                }
            }
        } else {
            player.resetDY();
            if (!reset) {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion), player.getX(), player.getY()-30, 100, 100, 25);
            }
            explosion.update();
            long resetElapsed = (System.nanoTime()-startReset)/1000000;
            if ((resetElapsed > 2500 && !newGameCreated) || firstGame) {
                if (firstGame) explosion.setPlayedOnce(true);
                newGame();
                firstGame = false;
            }
        }
    }
    public boolean collision (GameObject a, GameObject b) {
        if (Rect.intersects(a.getRectangle(), b.getRectangle())) {
            return true;
        }
        return false;
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            final float scaleFactorX =  (float)getWidth()/WIDTH;
            final float scaleFactorY =  (float)getHeight()/HEIGHT;
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if (!dissapear) player.draw(canvas);
            for (Smokepuff sp: smoke) {
                sp.draw(canvas);
            }
            for (Missile m: missiles) {
                m.draw(canvas);
            }
            for (TopBorder tb: topborder) {
                tb.draw(canvas);
            }
            for (BotBorder bb: botborder) {
                bb.draw(canvas);
            }
            // Draw explosion
            if (started) {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }


    }

    public void updateTopBorder() {
        // Every 50 points insert randomly placed bottom blocks that break the pattern
        if (player.getScore() % 50 == 0) {
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick1),
                    topborder.get(topborder.size()-1).getX()+20, 0, (int)(rand.nextDouble()*maxBorderHeight)+1));
        }
        for (int i = 0; i < topborder.size(); i++) {
            topborder.get(i).update();
            if (topborder.get(i).getX() < -20) {
                topborder.remove(i);
                // Calc topdown which determines the direction the border is moving (up or down)
                if (topborder.get(topborder.size() - 1).getHeight() >= maxBorderHeight) {
                    topDown = false;
                }
                if (topborder.get(topborder.size() - 1).getHeight() <= minBorderHeight) {
                    topDown = true;
                }
                if (topDown) {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick1),
                            topborder.get(topborder.size()-1).getX()+20, 0, topborder.get(topborder.size()-1).getHeight()+1));
                } else {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick1),
                            topborder.get(topborder.size()-1).getX()+20, 0, topborder.get(topborder.size()-1).getHeight()-1));
                }
            }
        }
    }

    public void updateBottomBorder() {
        // Every 40 points insert randomly placed top blocks that break the pattern
        if (player.getScore() % 40 == 0) {
            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick1),
                    botborder.get(botborder.size()-1).getX()+20, (int)(rand.nextDouble()*maxBorderHeight)+HEIGHT-maxBorderHeight));
        }
        for (int i = 0; i < botborder.size(); i++) {
            botborder.get(i).update();
            if (botborder.get(i).getX() < -20) {
                botborder.remove(i);
                if (bottomDown) {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick1),
                            botborder.get(botborder.size()-1).getX()+20, botborder.get(botborder.size()-1).getY()+1));
                } else {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick1),
                            botborder.get(botborder.size()-1).getX()+20, botborder.get(botborder.size()-1).getY()-1));
                }
            }
        }
        // Calc topdown which determines the direction the border is moving (up or down)
        if (botborder.get(botborder.size()-1).getY() <= HEIGHT-maxBorderHeight) {
            bottomDown = true;
        }
        if (botborder.get(botborder.size()-1).getY() >= HEIGHT-minBorderHeight) {
            bottomDown = false;
        }
    }

    public void newGame() {
        dissapear = false;
        topborder.clear();
        botborder.clear();
        missiles.clear();
        smoke.clear();
        minBorderHeight = 5;
        maxBorderHeight = 30;
        player.setY(HEIGHT/2);
        player.resetDY();

        if (player.getScore() > best) {
            best = player.getScore();
        }
        player.resetScore();

        // Creater initial borders
        // Top border
        for (int i = 0; i*20 < WIDTH+40; i++) {
            if (i == 0) {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick1), i*20, 0, 10));
            } else {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick1), i*20, 0, topborder.get(i-1).getHeight()+1));
            }
        }
        // Bottom border
        for (int i = 0; i*20 < WIDTH+40; i++) {
            if (i == 0) {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick1), i*20, HEIGHT-minBorderHeight));
            } else {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick1), i*20, botborder.get(i-1).getY()-1));
            }
        }
        newGameCreated = true;
    }

    public void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + player.getScore()*3, 10, HEIGHT-10, paint);
        canvas.drawText("BEST: " + best*3, WIDTH-215, HEIGHT - 10, paint);
        if (!player.getPlaying() && newGameCreated && reset) {
            Paint paint1 = new Paint();
            paint1.setColor(Color.BLACK);
            paint1.setTextSize(30);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH/2-50, HEIGHT/2, paint1);
            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH/2-50, HEIGHT/2+20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH/2-50, HEIGHT/2+40, paint1);
        }
    }

}
