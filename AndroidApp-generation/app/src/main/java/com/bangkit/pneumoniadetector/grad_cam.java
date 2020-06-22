package com.bangkit.pneumoniadetector;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.ByteArrayOutputStream;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;

public class grad_cam extends AppCompatActivity {
    private ImageView mimageView;
    private Button backButton;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    TextView textView;
    String yup = new String("ini dari java utk py");

    // allows user to go back to activity to select a different image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grad_cam);
        mimageView = findViewById(R.id.imageView);
        backButton = (Button)findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(grad_cam.this, camera_get.class);
                startActivity(i);
            }
        });

//        // "context" must be an Activity, Service or Application object from your app.
//        if (! Python.isStarted()) {
//            Python.start(new AndroidPlatform(this));
//        }
//
//        Python py = Python.getInstance();
//        PyObject pyf = py.getModule("myscript");
//        PyObject obj = pyf.callAttr("test",yup);
//
//        textView = findViewById(R.id.text);
//        textView.setText(obj.toString());
    }

    public void takePicture(View view){

        Intent imageTakeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imageTakeIntent.resolveActivity(getPackageManager())!= null)
        {
            startActivityForResult(imageTakeIntent,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent data) {
        super.onActivityResult(requestcode, resultcode, data);
        if (requestcode == REQUEST_IMAGE_CAPTURE && resultcode == RESULT_OK) {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data"); //get bitmap image

            //convert bitmap image to bytearray
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            //initialize the python
            if (!Python.isStarted()) {
                Python.start(new AndroidPlatform(this));
            }
            //send bytearray and get back bytearray from py
            Python py = Python.getInstance();
            PyObject pyf = py.getModule("myscript");
            byte[] img_arr = pyf.callAttr("pass_image", byteArray).toJava(byte[].class);

            //convert byte array to bitmap image
            Bitmap bmp = BitmapFactory.decodeByteArray(img_arr, 0, img_arr.length);
            Bitmap mutableBitmap = null;
            if (bmp != null) {
                mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
            }

            //set the image
            mimageView = findViewById(R.id.imageView);
            mimageView.setImageBitmap(mutableBitmap);


        }

    }
}
