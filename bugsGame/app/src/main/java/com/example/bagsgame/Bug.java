package com.example.bagsgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.SurfaceHolder;
import android.content.res.Resources;


//это класс-поток для каждого жука
public class Bug extends Thread{

    private SurfaceHolder surfaceHolder;
    private GameView gameView;
    private boolean running;
    public static Canvas canvas;

    //размеры экрана
    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    //Картинка жука
    private Bitmap image;

    //координаты жука
    //дробные потому, что будет ползать под любыми углами
    private float y = 100;
    private float x = 100;

    private int speed = 20;//скорость жука
    private double speedX;//скорость жука
    private double speedY;//скорость жука
    private int angle;//угол его движения и поворота в градусах

//конструктор


    public Bug(Bitmap bmp,int angle)
    {
        image = bmp;
        Restart(angle);
    }

    //пересчет координат в зависимости от угла.
    //сделаю приватным, потому что будет использоваться только внутри класса
    private void RecalcSpeed()
    {
        double radAngle = Math.toRadians(angle);
        speedX = speed*Math.sin(radAngle);
        speedY = -speed*Math.cos(radAngle);
    }

    //воскрешаем раздавленного жука и задаем ему новое направление
    public void Restart(int angle)
    {
        this.angle = angle;
        RecalcSpeed();

        //ставлю жука в центр и отодвигаю к краю экрана
        //двигаю так, чтобы он шел к центру экрана
        y = screenHeight/2;
        x = screenWidth/2;

        //пока край жука не ушел за край экрана - двигаю жура
        while((x>0 && x<screenWidth-image.getWidth()) &&
              (y>0 && y<screenHeight-image.getHeight()))
        {
            x-=speedX;
            y-=speedY;
        }
    }

    //пошел жук или замри жук
    public  void setRunning(boolean isRunning)
    {
        running = isRunning;
    }

    public float getCenterX() {
        return x+image.getWidth()/2;
    }

    public float getCenterY() {
        return y+image.getHeight()/2;
    }

    public void setXY(float x,float y) {
        this.x = x;
        this.y = y;
    }

    //рисую жука
    public void draw(Canvas canvas)
    {
        Matrix mtr = new Matrix();
        //поворот отностительно центра картинки
        mtr.postRotate(angle,image.getWidth()/2,image.getHeight()/2);
        //рисовать в координатах
        mtr.postTranslate(x,y);

        canvas.drawBitmap(image,mtr,null);
        //canvas.dra
    }

    public void  update()
    {
        //прибавить к координатам координатную скорость
        x+=speedX;
        y+=speedY;

    }

    @Override
    public void run()
    {
        //пока игра запущена
        while(running)
        {
            update();//передвинуть жука

            try {
                sleep(100);//обновление 10 раз за секунду
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //при каждом тапе жуки будут бояться этого тапа и поворачивать в сторону
    //от этого тапа на заданный угол
    public void fear(float TapX,float TapY)
    {
        int deltaAngle = 30;//это унол на который поворачивает жук
        //угол прямой, соединяющей жука и координату тапа
        double trueAngle = Math.atan2(TapX-x,TapY-y);
        //перевожу в градусы
        trueAngle = trueAngle*180/Math.PI;
        //-90 это поправка связанная с углом движения жука
        if(angle - trueAngle -90 >0)
        {
            angle-=deltaAngle;
        }
        else
        {
            angle+=deltaAngle;
        }
        //пересчитаю скорости, чтобы жук не просто повернул картинку, но и пополз в новую сторону
        RecalcSpeed();
    }
}
