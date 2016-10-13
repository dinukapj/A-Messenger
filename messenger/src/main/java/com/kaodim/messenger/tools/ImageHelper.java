package com.kaodim.messenger.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Kanskiy on 12/10/2016.
 */

public class ImageHelper {
    public static void setupImageOrientation(String photoPath) throws IllegalArgumentException, IOException {
        ExifInterface ei = new ExifInterface(photoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotateImage(90,photoPath);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotateImage(180,photoPath);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotateImage(270,photoPath);
                break;
        }
    }
    private static void rotateImage(int degree, String imagePath){

        if(degree<=0){
            return;
        }
        try{
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = false;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inDither = true;
            Bitmap b= BitmapFactory.decodeFile(imagePath,opts);
            Matrix matrix = new Matrix();
            if(b.getWidth()>b.getHeight()){
                matrix.setRotate(degree);
                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(),
                        matrix, true);
            }
            FileOutputStream fOut = new FileOutputStream(imagePath);
            String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            String imageType = imageName.substring(imageName.lastIndexOf(".") + 1);

            FileOutputStream out = new FileOutputStream(imagePath);
            if (imageType.equalsIgnoreCase("png")) {
                b.compress(Bitmap.CompressFormat.PNG, 100, out);
            }else if (imageType.equalsIgnoreCase("jpeg")|| imageType.equalsIgnoreCase("jpg")) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
            fOut.flush();
            fOut.close();

            b.recycle();
        }catch (Exception e){
            e.printStackTrace();
        }
        return;
    }
}
