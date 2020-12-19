package com.saunvid.infilectassignment.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.saunvid.infilectassignment.activities.CameraXActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraHelper {
    private Context context;
    public static final int CAPTURE_IMAGE_CAMERA_REQUEST_CODE = 1000;
    private OnImageSaveListener onImageSaveListener;
    private String imagePath;
    private Fragment fragment;

    public static final int PERMISSION_REQUEST = 1;
    //code for using camerax lib
    private boolean useCameraX = true;

    public void setUseCameraX(boolean useCameraX) {
        this.useCameraX = useCameraX;
    }

    public CameraHelper(Fragment fragment) {
        this.context = fragment.getContext();
        this.fragment = fragment;

    }

    public CameraHelper(Context context) {
        this.context = context;
        //sPrefManager = new SPrefManager(context);
    }


    public void openCamera(boolean frontCamera, OnImageSaveListener onImageSaveListener) {
        //setUseCameraX(sPrefManager.getDefaultCamera().equals("In-built App Camera"));
        //view is taken to show the animation effect
        this.onImageSaveListener = onImageSaveListener;
        //use CameraX code from now
        if (useCameraX) {

            if (fragment == null) {

                CameraXActivity.openCameraActivity(context, frontCamera);
            } else {
                CameraXActivity.openCameraActivity(fragment, frontCamera);
            }

        }
        //else use legacy method
        else {
            if (!Util.isPermissionGranted(context, Manifest.permission.CAMERA) && !Util.isPermissionGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA, Manifest.permission.CAMERA}, PERMISSION_REQUEST);
                return;
            }
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (frontCamera) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
                    cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                    cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                } else {
                    cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                }
            }
            if (cameraIntent.resolveActivity(context.getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (Exception e) {
                    if (onImageSaveListener != null) {
                        onImageSaveListener.onFailure(CameraError.FILE_IO_ERROR, "Unable to create file");
                    }
                }
                if (photoFile != null) {
                    Uri photoUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", photoFile);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    cameraIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    cameraIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    if (fragment != null) {
                        fragment.startActivityForResult(cameraIntent, CAPTURE_IMAGE_CAMERA_REQUEST_CODE);
                    } else {
                        ((Activity) context).startActivityForResult(cameraIntent, CAPTURE_IMAGE_CAMERA_REQUEST_CODE);
                    }
                    frontCamera = false;
                }
            } else {
                if (onImageSaveListener != null) {
                    onImageSaveListener.onFailure(CameraError.NO_CAMERA_FOUND_ERROR, "No camera application found");
                }
            }
        }

    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        imagePath = image.getAbsolutePath();
        return image;
    }


    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ((requestCode == CAPTURE_IMAGE_CAMERA_REQUEST_CODE || requestCode == CameraXActivity.CAMERA_CAPTURE_REQUEST_CODE) && resultCode == Activity.RESULT_OK) {
            if (onImageSaveListener != null) {
                //normalizeImage();
                if (useCameraX) {
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            String imagePath = bundle.getString("imagePath");
                            if (imagePath != null) {
                                onImageSaveListener.onImageSave(imagePath);
                            } else {
                                onImageSaveListener.onFailure(CameraError.UNKNOWN_ERROR, "Sorry we are not able to click an image");
                            }
                        }
                    }
                } else {
                    String compressedImageFilePath = new ImageCompressorHelper(context).compressImage(new File(imagePath));
                    Log.i("COMPRESSED IMAGE PATH", compressedImageFilePath);
                    onImageSaveListener.onImageSave(compressedImageFilePath);
                }
                onImageSaveListener = null;
            }
        }
    }


    private Bitmap rotateImage(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;

            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
        rotatedBitmap.recycle();
        return bitmap;
    }

    private void saveBitmapToFile(Bitmap croppedImage) {
        OutputStream outputStream = null;
        try {
            outputStream = context.getContentResolver().openOutputStream(Uri.parse(imagePath));
            croppedImage.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            croppedImage.recycle();
        }

    }

    public interface OnImageSaveListener {
        void onImageSave(String path);

        void onFailure(CameraError cameraError, String errorMessage);
    }


    public enum CameraError {
        NO_CAMERA_FOUND_ERROR,
        FILE_IO_ERROR,
        UNKNOWN_ERROR
    }

    public void destroy() {
        if (fragment != null) {
            fragment = null;
        }
        if (context != null) {
            context = null;
        }
    }


    /*public static void showUseCameraXCamera(Context context, FragmentManager fragmentManager) {
        CustomBottomSheetDialog customBottomSheetDialogUseCameraX = CustomBottomSheetDialog.createDialog("No camera application found", "Use in built camera application", "Use", "Cancel");
        customBottomSheetDialogUseCameraX.setOnDialogButtonClickListener(new CustomBottomSheetDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(CustomBottomSheetDialog customBottomSheetDialog) {
                new SPrefManager(context).removeDefaultCamera();
                customBottomSheetDialog.dismiss();
            }

            @Override
            public void onNegativeClick(CustomBottomSheetDialog customBottomSheetDialog) {
                customBottomSheetDialog.dismiss();
            }
        });
        customBottomSheetDialogUseCameraX.show(fragmentManager, customBottomSheetDialogUseCameraX.getTag());
    }*/
}
