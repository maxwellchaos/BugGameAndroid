package com.example.bagsgame;

import android.graphics.Canvas;
import android.view.SurfaceHolder;


//этот поток отвечает за отрисовку
public class DrawThread extends Thread{
    private SurfaceHolder surfaceHolder;
    private GameView gameView;
    private boolean running;
    public static Canvas canvas;

    public DrawThread(SurfaceHolder surfaceHolder, GameView gameView)
    {
        super();
        this.gameView = gameView;
        this.surfaceHolder = surfaceHolder;

    }

    public  void setRunning(boolean isRunning)
    {
        running = isRunning;
    }
    @Override
    public void run()
    {
        //пока игра запущена
        while(running)
        {
            canvas = null;
            try
            {
                //Захватить канву
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder)
                {
                    //нарисоваться
                    this.gameView.draw(canvas);
                    //проверить на проигрыш
                    this.gameView.FinishTest();
                }
            }
            catch (Exception e) {
            }
            finally {
                if(canvas != null)
                {
                    try
                    {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

