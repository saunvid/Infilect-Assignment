package com.saunvid.infilectassignment.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageCompressorHelper {
    private int height = 640;
    private int width = 480;
    private Bitmap.CompressFormat imageFormat = Bitmap.CompressFormat.JPEG;
    private int quality = 85;
    private String imageName;
    private Context context;
    private int imageSource;

    public ImageCompressorHelper(Context context) {
        this.context = context;
    }

    public ImageCompressorHelper setHeight(int height) {
        this.height = height;
        return this;
    }

    public ImageCompressorHelper setSource(int imageSource) {
        this.imageSource = imageSource;
        return this;
    }

    public ImageCompressorHelper setWidth(int height) {
        this.height = height;
        return this;
    }

    public ImageCompressorHelper setImageFormat(Bitmap.CompressFormat imageFormat) {
        this.imageFormat = imageFormat;
        return this;
    }

    public ImageCompressorHelper setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }

    public String compressImage(String actualImagePath) {
        File actualImageFile = new File(actualImagePath);
        File file = ImageCompressor.compressImage(actualImageFile, width, height, imageFormat, quality, getFilePath());
        actualImageFile.delete();
        return file.getAbsolutePath();
    }


    public String compressImage(File actualImageFile) {
        File compressImageFile = ImageCompressor.compressImage(actualImageFile, width, height, imageFormat, quality, getFilePath());
        actualImageFile.delete();
        return compressImageFile.getAbsolutePath();
    }

    private String getFilePath() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image;
        try {
            image = new File(storageDir.getAbsoluteFile(), imageFileName + "_compress.jpg");
            return image.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class ImageCompressor {
        public static File compressImage(File imageFile, int width, int height, Bitmap.CompressFormat compressFormat, int quality, String destinationPath) {
            FileOutputStream fileOutputStream = null;
            File file = new File(destinationPath).getParentFile();
            if (!file.exists()) {
                file.mkdir();
            }
            try {
                fileOutputStream = new FileOutputStream(destinationPath);
                decodeBitmapFromFile(imageFile, width, height).compress(compressFormat, quality, fileOutputStream);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return new File(destinationPath);
        }

        public static Bitmap decodeBitmapFromFile(File imageFile, int width, int height) throws Exception {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            //calculate insamplesize
            options.inSampleSize = calculateSampleSize(options, width, height);
            //Decode bitmap with insamplesize set
            options.inJustDecodeBounds = false;

            Bitmap scaledBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            //check the rotation of the image and display it properly
            ExifInterface exif;
            try {
                exif = new ExifInterface(imageFile.getAbsolutePath());
                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 0);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                } else {
                    matrix.postRotate(0);
                }

                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return scaledBitmap;
        }

        static int calculateSampleSize(BitmapFactory.Options options, int width, int height) {
            //raw height and width of image
            int height1 = options.outHeight;
            int width1 = options.outWidth;
            int inSampleSize = 1;
            if (height1 > height || width1 > width) {
                int halfHeight = height1 / 2;
                int halfWidth = width1 / 2;
                while ((halfHeight / inSampleSize) >= height && (halfWidth / inSampleSize) >= width) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }
    }

}