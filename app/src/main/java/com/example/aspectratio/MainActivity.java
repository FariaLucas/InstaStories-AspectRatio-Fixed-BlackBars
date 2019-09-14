package com.example.aspectratio;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final int IMAGE_GALLEY_REQUEST = 20;
    FrameImage frameImage;
    Button addPhotoBtn, saveBtn;
    Switch switchGallery;
    TextView galleryTogle;
    ImageView picture;
    Bitmap newBitmap, resized;
    Display display;
    Point size;
    int screenWidth, screenHeight;
    boolean openGallery = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        runApp();
    }

    void runApp() {
        switchGallery = findViewById(R.id.gallerySwitch);
        galleryTogle = findViewById(R.id.galleyText);
        addPhotoBtn = findViewById(R.id.addPhoto);
        saveBtn = findViewById(R.id.save);
        picture = findViewById(R.id.imgPicture);
        frameImage = new FrameImage();
        display = getWindowManager().getDefaultDisplay();
        size = new Point();

        display.getRealSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        Toast.makeText(this, "Screen Size: " + screenWidth + "x" + screenHeight, Toast.LENGTH_LONG).show();

        addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent((Intent.ACTION_PICK));
                File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                String path = pictureDirectory.getPath();
                Uri data = Uri.parse(path);
                photoPickerIntent.setDataAndType(data, "image/*");
                startActivityForResult(photoPickerIntent, IMAGE_GALLEY_REQUEST);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmapToGallery(newBitmap);
            }
        });

        switchGallery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                openGallery = isChecked;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLEY_REQUEST) {
                Uri imageUri = data.getData();
                InputStream inputStream;

                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);

                    AddBlackBars(imgBitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void AddBlackBars(Bitmap img) {

        int imageHeight = img.getHeight(), imageWidth = img.getWidth();
        int positionTop = 200;

        Toast.makeText(this, "Normal Image- " + imageWidth + "x" + imageHeight, Toast.LENGTH_LONG).show();

        frameImage.loadImage("CenterImage1440.png", this.getAssets());
        resized = Bitmap.createScaledBitmap(img, 1440, 2560, true);


        newBitmap = Bitmap.createBitmap(frameImage.bitmap.getWidth(), frameImage.bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(frameImage.bitmap, 0, 0, null);      //Places de Frame in the (0,0) of the image

        canvas.drawBitmap(resized, 0, positionTop, null);

        addPhotoBtn.setText("Choose Another");
        saveBtn.setVisibility(View.VISIBLE);
        switchGallery.setVisibility(View.VISIBLE);
        galleryTogle.setVisibility(View.VISIBLE);

        picture.setImageBitmap(newBitmap);
    }

    private void saveBitmapToGallery(Bitmap bitmap){

        if(!StorageHelper.isExternalStorageReadableAndWritable()){
            Toast.makeText(this, "Gallery not Available", Toast.LENGTH_LONG).show();
            return;
        }

        String filename = getFilename() + ".png";
        String url = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, filename, "AspectRatio fix for Instagram stories");
        Toast.makeText(this, "Image saved in 'Pictures' folder", Toast.LENGTH_LONG).show();

        if(url==null){
            Toast.makeText(this, "Didn't Save", Toast.LENGTH_LONG).show();
            return;
        }

        if (openGallery){
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.parse(url),"image/*");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            Intent intent = Intent.createChooser(target, "Open File");
            startActivity(intent);
        }
    }

    private String getFilename(){
        return "AspectRatio- " + System.currentTimeMillis();
    }
}