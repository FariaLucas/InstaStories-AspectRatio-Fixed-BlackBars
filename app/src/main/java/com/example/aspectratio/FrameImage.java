package com.example.aspectratio;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;

public class FrameImage {
    Bitmap bitmap;

    public void loadImage(String path, AssetManager manager){
        try{
            InputStream inputStream = manager.open(path);
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}