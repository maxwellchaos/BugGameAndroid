package com.example.bagsgame;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    //информационная строка при старте игры
    //почему-то спецсимвол переноса строки (\n) не срабал
    //поэтому пишу в две переменные
    private String startText = "Tap bugs to collect score.";
    private String startText2 = "If you miss bug you lose score!";

    GameButton ExitButton;
    GameButton StartButton;
    GameButton RestartButton;
    /*
    Bitmap ExitButton = BitmapFactory.decodeResource(getResources(), R.drawable.exit_button);
    Bitmap StartButton = BitmapFactory.decodeResource(getResources(), R.drawable.play_button);
    Bitmap RestartButton = BitmapFactory.decodeResource(getResources(), R.drawable.restart_button);
*/

    private String FinalText;
    //Обратный отсчет до отключения строки
    //считается количество отрисованных кадров
    //на моем компьютере на эмуляторе выглядит нормально
    private int StartTextCountdown = 300;

    //признак конца игры
    boolean RestartMenu = false;

    //показывать ли стартовое меню
    boolean StartMenu = true;

    //очки
    public int score = 0;
    //поток для отрисовки
    private DrawThread thread;

    //размеры экрана
    private int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    private int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    //список жуков
    private ArrayList<Bug> bugs = new ArrayList<Bug>();

    public GameView(MainActivity context) {
        super(context);
        getHolder().addCallback(this);
        thread = new DrawThread(getHolder(),this);
        setFocusable(true);
        //создаю кнопки
        StartButton = new GameButton(BitmapFactory.decodeResource(getResources(),R.drawable.play_button),
                screenWidth/2,screenHeight/3);
        RestartButton = new GameButton(BitmapFactory.decodeResource(getResources(),R.drawable.restart_button),
                screenWidth/2,screenHeight/3);
        ExitButton = new GameButton(BitmapFactory.decodeResource(getResources(),R.drawable.exit_button),
                screenWidth/2,screenHeight/3*2);

    }

    //при касании экрана
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        //обработка меню
        //в отрисовке будут разные кнопки, а здесь все будет работать одинаково
        if(RestartMenu || StartMenu) {
            //кнопка выхода
            if(ExitButton.isOnButton(event.getX(),event.getY()))
                System.exit(0);
            //кнопка игры или рестарта
            if(StartButton.isOnButton(event.getX(),event.getY())||
                    RestartButton.isOnButton(event.getX(),event.getY()))
            {
                //создать и запустить 10 жуков
                for(int i = 0;i<10;i++) {
                    Random rnd = new Random();
                    int angle = rnd.nextInt(360);
                    Bug bug;

                    //выбираю разные картинки жуков
                    //картинка жука зависит от стартового поворота жука
                    //слева бегут одни жуки, справа другие
                    if(angle>180)
                        bug = new Bug(BitmapFactory.decodeResource(getResources(), R.drawable.bug),angle);
                    else
                        bug = new Bug(BitmapFactory.decodeResource(getResources(), R.drawable.bug2),angle);

                    bug.setRunning(true);
                    bug.start();
                    bugs.add(bug);
                }
                //начать игру
                 RestartMenu = false;
                 StartMenu = false;
                 //обнуляю очки
                 score = 0;
            }
            return super.onTouchEvent(event);
        }
        else {
            int bonus = 0;//это бонус за тап по жуку
            for (Bug bug : bugs) {
                //считаю расстояние между тапом и жуком
                float xdist = bug.getCenterX() - event.getX();
                float ydist = bug.getCenterY() - event.getY();
                double distance = Math.sqrt(xdist * xdist + ydist * ydist);

                if (distance < 100) {
                    Random rnd = new Random();
                    bug.Restart(rnd.nextInt(360));
                    bonus++;
                    new Thread() {
                        public void run() {
                            MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.insect_ju);
                            mp.start();
                        }
                    }.start();
                }
                bug.fear(event.getX(),event.getY());
            }
            //это штраф, когда промазали мимо жука
            if (bonus == 0) {
                bonus = -10;
                new Thread() {
                    public void run() {
                        MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.bwv_i);
                        mp.start();
                    }
                }.start();
            }
            //прибавляю бонус к очкам
            score += bonus;
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {


        //запустить поток
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        boolean retry = true;
        //останавливаю жуков
        for(Bug bug : bugs) {
            while (retry) {
                surfaceHolder.lockCanvas();
                try {
                    bug.setRunning(false);
                    bug.join();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                retry = false;
            }
        }
        retry = true;
        //останавливаю отрисовку
        while (retry)
        {
            surfaceHolder.lockCanvas();
            try{
                thread.setRunning(false);
                thread.join();

            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    //игра закончится когда все жуки уйдут с экрана
    public void FinishTest()
    {
        //если сейчас только начало игры - то не проверяем на финиш
        if(StartMenu)
            return;
        boolean onScreen = false;

        //перебираю всех жуков, и если хоть один еще на экране то onScreen будет true
        for(Bug bug : bugs) {
            float x = bug.getCenterX();
            float y = bug.getCenterY();

            if((x>0 && x<screenWidth) &&
                    (y>0 && y<screenHeight))
            {
                onScreen = true;
            }
        }
        //если на экране нет жуков
        if(!onScreen)
        {
            FinalText = "All bugs gone out";
            RestartMenu = true;
        }
        //если слишком сильно ушли в минус
        if(score < -100)
        {
            FinalText = "Too many miss. You lose";
            RestartMenu = true;
        }
    }

    //рисую меню
    public void drawMenu(Canvas canvas)
    {
        if(StartMenu) {

            Paint fontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            fontPaint.setTextSize(70);
            fontPaint.setColor(Color.RED);
            //fontPaint.setStyle(Paint.Style.STROKE);
            canvas.drawText(startText, 100, screenHeight / 7 - 120, fontPaint);
            canvas.drawText(startText2, 100, screenHeight / 7, fontPaint);
            canvas.drawBitmap(StartButton.image,StartButton.X,StartButton.Y,null);
            canvas.drawBitmap(ExitButton.image,ExitButton.X,ExitButton.Y,null);
        }
        if(RestartMenu) {
            Paint fontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            fontPaint.setTextSize(70);
            fontPaint.setColor(Color.RED);
            //fontPaint.setStyle(Paint.Style.STROKE);
            canvas.drawText(FinalText, 100, screenHeight / 7 - 120, fontPaint);
            canvas.drawText("your score: " + String.valueOf(score), 100, screenHeight / 7, fontPaint);
           // canvas.drawBitmap(BitmapFactory.decodeResource());
            canvas.drawBitmap(RestartButton.image,StartButton.X,StartButton.Y,null);
            canvas.drawBitmap(ExitButton.image,ExitButton.X,ExitButton.Y,null);
        }

    }

    //рисую жуков
    public void drawGame(Canvas canvas)
    {
        Paint paintBack = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBack.setColor(Color.WHITE);
        canvas.drawRect(0,0,screenWidth,screenHeight,paintBack);
        for (Bug bug : bugs) {
            bug.draw(canvas);
        }
        Paint fontPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fontPaint.setTextSize(60);
        fontPaint.setColor(Color.RED);
        canvas.drawText(String.valueOf(score), 100, 100, fontPaint);
        if(StartTextCountdown>0)
        {
            StartTextCountdown--;
            canvas.drawText(startText, 100, screenHeight/2-100, fontPaint);
            canvas.drawText(startText2, 100, screenHeight/2, fontPaint);
        }
    }

    //рисование игры
    //здесь происходит выбор что рисовать, меню или саму игру
    @Override
    public void draw(Canvas canvas)
    {
        super.draw(canvas);
        if(canvas !=null) {
            if(StartMenu || RestartMenu) {
                drawMenu(canvas);
            }
            else {
                drawGame(canvas);
            }
        }
    }
}
