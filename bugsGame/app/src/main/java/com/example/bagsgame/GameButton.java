package com.example.bagsgame;

import android.graphics.Bitmap;

public class GameButton {
    //картинка
    public Bitmap image;
    //координаты левого верхнего угла
    public int X;
    public int Y;

    //Конструктор
    //x,y - центр кнопки
    public GameButton(Bitmap img,int CenterX,int CenterY)
    {
        X = CenterX-img.getWidth()/2;
        Y = CenterY-img.getHeight()/2;
        image = img;
    }


    //проверка, попадает ли точка в кнопку
    public boolean isOnButton(float x,float y)
    {
        if(     X<x &&
                X+image.getWidth()>x &&
                Y<y &&
                Y+image.getHeight()>y)
            return true;
        else
            return false;
    }

}
