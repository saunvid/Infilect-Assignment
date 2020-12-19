package com.saunvid.infilectassignment.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.saunvid.infilectassignment.service.ForegroundService;
import com.saunvid.infilectassignment.R;
import com.saunvid.infilectassignment.helper.CameraHelper;
import com.saunvid.infilectassignment.remote.Api;
import com.saunvid.infilectassignment.remote.ApiListener;
import com.saunvid.infilectassignment.remote.ApiRequest;

import java.io.File;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.saunvid.infilectassignment.remote.ApiRequest.MEDIA_TYPE_JPG;

public class MainActivity extends AppCompatActivity {

    Button btnLaunchCamera, btnViewImgPath;
    private CameraHelper cameraHelper;
    private Uri selectedImage;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnViewImgPath = findViewById(R.id.btnViewImgPath);
        img = findViewById(R.id.img);
        btnLaunchCamera = findViewById(R.id.btnCaptureImage);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHelper = new CameraHelper(MainActivity.this);
                cameraHelper.openCamera(false, new CameraHelper.OnImageSaveListener() {
                    @Override
                    public void onImageSave(String path) {
                        startMyService();
                        //isImgCapture = true;
                        Log.d("PATH", "onImageSave: " + path);
                        selectedImage = Uri.parse(path);
                        //profileImgFile = new File(path);
                        setEditedImage(path);
                        updateImg();
                    }

                    @Override
                    public void onFailure(CameraHelper.CameraError error, String errorMessage) {
                    }
                });
            }
        });

        btnViewImgPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ViewImagePathActivity.class));
            }
        });
    }

    private void setEditedImage(String path) {

        img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        Glide.with(this).load(path)
                //.circleCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                .into(img);


        //updateImg();
    }

    private void updateImg() {
        //stopMyService();
        //call APi to save img to server
        String url = Api.BASE_URL + "Service1.svc/addImg";
        MultipartBody multipartBody = createMultipartForImg();
        new ApiRequest(this).postBody(url, multipartBody, new ApiListener() {
            @Override
            public void onSuccess(String response) {
                Log.d("RESSSS", "onSuccess: " + response);
                stopMyService();

                /*try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String code = jsonResponse.getJSONObject("addImgResult").getString("CODE");

                    if (code.equalsIgnoreCase("200")) {
                    } else {

                    }

                    stopMyService();
                } catch (JSONException e) {
                    e.getMessage();
                }*/

                //onResume();
            }

            @Override
            public void onError(String ERROR, String errorMessage) {

            }
        });
    }


    public void startMyService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Image is being Uploaded...");
        serviceIntent.setAction("ACTION_START_FOREGROUND_SERVICE");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopMyService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.setAction("ACTION_STOP_FOREGROUND_SERVICE");
        stopService(serviceIntent);
    }

    private MultipartBody createMultipartForImg() {
        MultipartBody.Builder multipartBody = new MultipartBody.Builder();

        String name = String.valueOf(System.currentTimeMillis());

        multipartBody.addFormDataPart("img",
                name + ".jpg",
                RequestBody.create(new File(String.valueOf(selectedImage)), MEDIA_TYPE_JPG));

        return multipartBody.build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (cameraHelper != null) {
            cameraHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

}
