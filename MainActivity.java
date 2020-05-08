package com.example.democv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;


import org.opencv.android.OpenCVLoader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button_open, button_gray, button_circle;

    Bitmap bitmap;

    static {
        if(!OpenCVLoader.initDebug()){
            Log.e("OpenCV", "init unsuccesfull");
        }
        else
            Log.e("OpenCV", "success");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        button_open = findViewById(R.id.button_open);
        button_gray = findViewById(R.id.button_gray);
        button_circle = findViewById(R.id.button_circle);

        button_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 0);
            }
        });

        button_gray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //harris h = new harris();
                //BitmapDrawable drawable = (BitmapDrawable) iv.getDrawable();
                //Bitmap rgb = drawable.getBitmap();
                Bitmap rgb = Bitmap.createBitmap(bitmap);
                //rgb = Bitmap.createScaledBitmap(rgb,200, 200, true);

                if(rgb == null)
                {
                    Toast.makeText(getApplicationContext(), "LOAD AN IMAGE!!!", Toast.LENGTH_LONG).show();
                    return;
                }

                kinks k = new kinks(rgb);
                k.run_spline();
                Bitmap green = k.getSp(); //k.getCorner();



                //imageView.setImageDrawable(null);
                /*ColorMatrix m = new ColorMatrix();
                m.setSaturation(0);

                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(m);
                imageView.setColorFilter(filter);*/


                imageView.setImageBitmap(null);
                imageView.setImageBitmap(green);

                Toast.makeText(getApplicationContext(), "Done!!!", Toast.LENGTH_LONG).show();
                //bitmap = green;

            }
        });

        button_circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //harris h = new harris();
                //BitmapDrawable drawable = (BitmapDrawable) iv.getDrawable();
                //Bitmap rgb = drawable.getBitmap();
                Bitmap rgb = Bitmap.createBitmap(bitmap);
                //rgb = Bitmap.createScaledBitmap(rgb,200, 200, true);

                if(rgb == null)
                {
                    Toast.makeText(getApplicationContext(), "LOAD AN IMAGE!!!", Toast.LENGTH_LONG).show();
                    return;
                }

                kinks k = new kinks(rgb);
                k.run_circle();
                Bitmap green = k.getSp(); //k.getCorner();

                imageView.setImageBitmap(null);
                imageView.setImageBitmap(green);

                Toast.makeText(getApplicationContext(), "Done!!!", Toast.LENGTH_LONG).show();
                //bitmap = green;

            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data)
    {
        super.onActivityResult(reqCode, resCode, data);

        switch (reqCode) {
            case 0:
                if (resCode == RESULT_OK) {
                    Uri imageUri = data.getData();

                    Bitmap bmp = null;

                    try {
                        bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    bmp = Bitmap.createScaledBitmap(bmp,400, 400, true);

                    imageView.setImageBitmap(bmp);

                    bitmap = Bitmap.createBitmap(bmp);

                }
        }
    }
}
