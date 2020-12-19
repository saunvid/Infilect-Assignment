package com.saunvid.infilectassignment.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.FlashMode;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.saunvid.infilectassignment.fragment.CapturedImageDialogFragment;
import com.saunvid.infilectassignment.listener.OnCapturedImageListener;
import com.saunvid.infilectassignment.R;
import com.saunvid.infilectassignment.helper.ImageCompressorHelper;
import com.saunvid.infilectassignment.helper.Util;

import java.io.File;
import java.util.concurrent.Executors;

public class CameraXActivity extends AppCompatActivity {

    PreviewConfig previewConfig;
    private static final String TAG = "AndroidCameraApi";
    private FloatingActionButton takePictureButton;
    private TextureView textureView;
    private ImageCapture imageCapture;
    private FloatingActionButton flashToggle, cancelFab, chngCameraFab;
    private ProgressBar cameraProgress;
    public static final int CAMERA_CAPTURE_REQUEST_CODE = 9990;
    public static final String EXTRA_FRONT_CAMERA = "EXTRA_FRONT_CAMERA";
    private boolean frontCamera = false;
    public static final int PERMISSION_REQUEST_CODE = 1;
    private Preview preview;
    private boolean isImageCaptured = false;
    private CameraX.LensFacing lensFacing;

    public static void openCameraActivity(Context context, boolean frontCamera) {
        Intent intent = new Intent(context, CameraXActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_FRONT_CAMERA, frontCamera);
        intent.putExtras(bundle);
        ((Activity) context).startActivityForResult(intent, CAMERA_CAPTURE_REQUEST_CODE);
    }

    public static void openCameraActivity(Fragment fragment, boolean frontCamera) {
        Intent intent = new Intent(fragment.getContext(), CameraXActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_FRONT_CAMERA, frontCamera);
        intent.putExtras(bundle);
        fragment.startActivityForResult(intent, CAMERA_CAPTURE_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null) {
            frontCamera = getIntent().getExtras().getBoolean(EXTRA_FRONT_CAMERA, false);
        }
        setContentView(R.layout.activity_camera_x);
        initView();
    }

    private void initView() {
        cancelFab = findViewById(R.id.fab_cancel);
        cameraProgress = findViewById(R.id.progress_camera_x);
        flashToggle = findViewById(R.id.imgBtn_flashLight);
        chngCameraFab = findViewById(R.id.imgBtn_ChgCamera);
        textureView = findViewById(R.id.texture);
        takePictureButton = findViewById(R.id.btn_takepicture);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinatorLayout), (v, insets) -> {
            //inset from top
            ViewGroup.MarginLayoutParams fabLayoutParams = (ViewGroup.MarginLayoutParams) cancelFab.getLayoutParams();
            fabLayoutParams.topMargin = insets.getSystemWindowInsetTop() + Util.getValueInDP(CameraXActivity.this, 16);

            //inset from top
            ViewGroup.MarginLayoutParams flashToggleLayoutParams = (ViewGroup.MarginLayoutParams) flashToggle.getLayoutParams();
            flashToggleLayoutParams.topMargin = insets.getSystemWindowInsetTop() + Util.getValueInDP(CameraXActivity.this, 16);
//inset from top
            ViewGroup.MarginLayoutParams chngCamToggleLayoutParams = (ViewGroup.MarginLayoutParams) chngCameraFab.getLayoutParams();
            chngCamToggleLayoutParams.topMargin = insets.getSystemWindowInsetTop() + Util.getValueInDP(CameraXActivity.this, 16);

            //inset from bottom
            ViewGroup.MarginLayoutParams takePictureButtonLayoutParams = (ViewGroup.MarginLayoutParams) takePictureButton.getLayoutParams();
            takePictureButtonLayoutParams.bottomMargin = insets.getSystemWindowInsetBottom() + Util.getValueInDP(CameraXActivity.this, 16);
            return insets.consumeSystemWindowInsets();
        });
        setUpListner();
        if (!Util.isPermissionGranted(this, Manifest.permission.CAMERA) || !Util.isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            return;
        }
        textureView.post(() ->
                startCamera()
        );

    }

    @SuppressLint("RestrictedApi")
    private void changeCamera() {
        /*lensFacing = lensFacing == CameraX.LensFacing.FRONT ? CameraX.LensFacing.BACK : CameraX.LensFacing.FRONT;
        try {
            // Only bind use cases if we can query a camera with this orientation
            CameraX.getCameraWithLensFacing(lensFacing);
            bindCameraUseCases();
        } catch (CameraInfoUnavailableException e) {
            // Do nothing
        }*/
        frontCamera = frontCamera == frontCamera ? !frontCamera : frontCamera;
        CameraX.unbindAll();
        startCamera();
    }

    private void bindCameraUseCases() {
        // Make sure that there are no other use cases bound to CameraX
        CameraX.unbindAll();

        PreviewConfig previewConfig = new PreviewConfig.Builder().
                setLensFacing(lensFacing)
                .build();
        Preview preview = new Preview(previewConfig);

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setLensFacing(lensFacing)
                .build();
        imageCapture = new ImageCapture(imageCaptureConfig);

        // Apply declared configs to CameraX using the same lifecycle owner
        CameraX.bindToLifecycle(this, preview, imageCapture);
    }


    private void startCamera() {
        // pull the metrics from our TextureView
        DisplayMetrics metrics = new DisplayMetrics();
        textureView.getDisplay().getRealMetrics(metrics);
        // define the screen size
        Size screenSize = new Size(metrics.widthPixels, metrics.heightPixels);
        Log.i("WIDTH", metrics.widthPixels + "");
        Log.i("HEIGHT", metrics.heightPixels + "");
        // Create configuration object for the viewfinder use case
        previewConfig = new PreviewConfig.Builder()
                .setTargetResolution(screenSize)
                /*.setTargetAspectRatio(AspectRatio.RATIO_16_9)*/
                .setLensFacing(frontCamera ? CameraX.LensFacing.FRONT : CameraX.LensFacing.BACK)
                .build();
        lensFacing = previewConfig.getLensFacing();
        // Build the viewfinder use case
        preview = new Preview(previewConfig);

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(@NonNull Preview.PreviewOutput output) {
                // To update the SurfaceTexture, we have to remove it and re-add it
                ViewGroup parent = (ViewGroup) textureView.getParent();
                parent.removeView(textureView);
                parent.addView(textureView, 0);
                textureView.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setLensFacing(frontCamera ? CameraX.LensFacing.FRONT : CameraX.LensFacing.BACK)
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build();
        imageCapture = new ImageCapture(imageCaptureConfig);
        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, imageCapture);
    }

    private void updateTransform() {
        Matrix matrix = new Matrix();

        // Compute the center of the view finder
        float centerX = textureView.getWidth() / 2f;
        float centerY = textureView.getHeight() / 2f;

        // Correct preview output to account for display rotation
        int rotationDegree = 90;
        switch (textureView.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotationDegree = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegree = 90;
                break;
            case Surface.ROTATION_180:
                rotationDegree = 180;
                break;
            case Surface.ROTATION_270:
                rotationDegree = 270;
                break;
        }
        matrix.postRotate(-rotationDegree, centerX, centerY);

        // Finally, apply transformations to our TextureView
        textureView.setTransform(matrix);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllRequiredPermissionGranted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        //when user permanently click don't ask again
                        Snackbar.make(findViewById(R.id.coordinatorLayout), "We need both camera and storage permission, please enable them manually", Snackbar.LENGTH_INDEFINITE).setAnchorView(takePictureButton).setAction("Settings", v -> Util.openAppSetting(v.getContext())).show();
                        return;
                    }
                    isAllRequiredPermissionGranted = false;
                }
            }

            if (isAllRequiredPermissionGranted) {
                //if all permission is granted
                textureView.post(() ->
                        startCamera());
            } else {
                Snackbar.make(findViewById(R.id.coordinatorLayout), "We need both camera and storage permission, please enable them manually", Snackbar.LENGTH_INDEFINITE).setAnchorView(takePictureButton).setAction("Settings", v -> Util.openAppSetting(v.getContext())).show();
            }
        }
    }

    private void setUpListner() {
        chngCameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCamera();
            }
        });
        cancelFab.setOnClickListener(view -> finish());
        flashToggle.setOnClickListener(view -> {
            flashToggle.setSelected(!flashToggle.isSelected());
            imageCapture.setFlashMode(flashToggle.isSelected() ? FlashMode.ON : FlashMode.OFF);
        });
        takePictureButton.setOnClickListener(view -> {
            if (!Util.isPermissionGranted(this, Manifest.permission.CAMERA) || !Util.isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
            if (!isImageCaptured) {
                try {
                    //CameraX.unbind(preview);
                    cameraProgress.setVisibility(View.VISIBLE);
                    File file = Util.createFile(CameraXActivity.this, "cameraX");
                    isImageCaptured = true;
                    imageCapture.takePicture(file, Executors.newSingleThreadExecutor(), new ImageCapture.OnImageSavedListener() {
                        @Override
                        public void onImageSaved(@NonNull final File file) {
                            isImageCaptured = false;
                            textureView.post(() -> {
                                cameraProgress.setVisibility(View.GONE);
                                //compressing image
                                String compressedImageFilePath = new ImageCompressorHelper(CameraXActivity.this).compressImage(file);
                                CapturedImageDialogFragment capturedImageDialogFragment = CapturedImageDialogFragment.createCapturedImageDialog(compressedImageFilePath);
                                capturedImageDialogFragment.setOnCapturedImageListener(new OnCapturedImageListener() {
                                    @Override
                                    public void onSaveClick(String file1) {
                                        Intent intent = new Intent();
                                        Bundle bundle = new Bundle();
                                        if (file1 == null || TextUtils.isEmpty(file1)) {
                                            bundle.putString("imagePath", null);
                                        } else {
                                            bundle.putString("imagePath", file1);
                                        }
                                        intent.putExtras(bundle);
                                        setResult(RESULT_OK, intent);
                                        CameraXActivity.this.finish();
                                    }

                                    @Override
                                    public void onRetryClick() {
                                        //CameraX.bindToLifecycle(CameraXActivity.this, preview);

                                    }
                                });
                                capturedImageDialogFragment.show(getSupportFragmentManager(), capturedImageDialogFragment.getTag());

                            });
                        }

                        @Override
                        public void onError(@NonNull ImageCapture.ImageCaptureError imageCaptureError, @NonNull final String message, @Nullable Throwable cause) {
                            isImageCaptured = false;
                            textureView.post(() -> {
                                // CameraX.bindToLifecycle(CameraXActivity.this, preview);
                                cameraProgress.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            });
                        }

                    });
                } catch (Exception e) {
                    isImageCaptured = false;
                }
            }
        });

        textureView.addOnLayoutChangeListener((view, i, i1, i2, i3, i4, i5, i6, i7) -> {

        });
    }


    @Override
    protected void onDestroy() {
        CameraX.unbindAll();
        super.onDestroy();
    }

}
