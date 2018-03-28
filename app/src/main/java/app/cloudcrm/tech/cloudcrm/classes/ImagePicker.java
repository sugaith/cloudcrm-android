package app.cloudcrm.tech.cloudcrm.classes;

/**
 * Created by Alberto on 10/9/2016.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import app.cloudcrm.tech.cloudcrm.R;


/**
 * Author: Mario Velasco Casquero
 * Date: 08/09/2015
 * Email: m3ario@gmail.com
 */
public class ImagePicker {

    private static final int DEFAULT_MIN_WIDTH_QUALITY = 600;        // min pixels
    private static final String TAG = "ImagePicker";
    private static final String TEMP_IMAGE_NAME = "tempImage";

    public static int minWidthQuality = DEFAULT_MIN_WIDTH_QUALITY;


    public static Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(context)));
        intentList = addIntentsToList(context, intentList, pickIntent);
        intentList = addIntentsToList(context, intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    context.getString(R.string.action_tirar_foto));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
            //Log.d(TAG, "Intent: " + intent.getAction() + " package: " + packageName);
        }
        return list;
    }


    public static Bitmap getImageFromResult(Context context, int resultCode,
                                            Intent imageReturnedIntent) {
        //Log.d(TAG, "getImageFromResult, resultCode: " + resultCode);
        Bitmap bm = null;
        File imageFile = getTempFile(context);
        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage;
            boolean isCamera = (imageReturnedIntent == null ||
                    imageReturnedIntent.getData() == null  ||
                    imageReturnedIntent.getData().toString().contains(imageFile.toString()));
            if (isCamera) {     /** CAMERA **/
                selectedImage = Uri.fromFile(imageFile);
            } else {            /** ALBUM **/
                selectedImage = imageReturnedIntent.getData();
            }
            //Log.d(TAG, "selectedImage: " + selectedImage);


            try{

                bm = HDBitmap(MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedImage), MAX_WIDTH);

            }catch (Exception e){

                e.printStackTrace();

            }
            //int rotation = getRotation(context, selectedImage, isCamera);
            //bm = rotate(bm, rotation);
        }
        return bm;
    }


    private static File getTempFile(Context context) {
        File imageFile = new File(Environment.getExternalStorageDirectory()+File.separator+"temp.jpg");
        imageFile.getParentFile().mkdirs();
        return imageFile;
    }

    private static Bitmap decodeBitmap(Context context, Uri theUri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        int maxWith = 800;

        int maxHeight = 400;

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap actuallyUsableBitmap = BitmapFactory.decodeFileDescriptor(
                fileDescriptor.getFileDescriptor(), null, options);
        /*
        //Log.d(TAG, options.inSampleSize + " sample method bitmap ... " +
                actuallyUsableBitmap.getWidth() + " " + actuallyUsableBitmap.getHeight());
        */

        return actuallyUsableBitmap;
    }

    /**
     * Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
     **/
    private static Bitmap getImageResized(Context context, Uri selectedImage) {
        Bitmap bm = null;
        int[] sampleSizes = new int[]{1, 2, 3, 4, 5};
        int i = 0;
        do {
            bm = decodeBitmap(context, selectedImage, sampleSizes[i]);
            //Log.d(TAG, "resizer: new bitmap width = " + bm.getWidth());
            i++;
        } while (bm.getWidth() < minWidthQuality && i < sampleSizes.length);
        return bm;
    }


    private static int getRotation(Context context, Uri imageUri, boolean isCamera) {
        int rotation;
        if (isCamera) {
            rotation = getRotationFromCamera(context, imageUri);
        } else {
            rotation = getRotationFromGallery(context, imageUri);
        }
        //Log.d(TAG, "Image rotation: " + rotation);
        return rotation;
    }

    private static int getRotationFromCamera(Context context, Uri imageFile) {
        int rotate = 0;
        try {

            context.getContentResolver().notifyChange(imageFile, null);
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static int getRotationFromGallery(Context context, Uri imageUri) {
        int result = 0;
        String[] columns = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
                result = cursor.getInt(orientationColumnIndex);
            }
        } catch (Exception e) {
            //Do nothing
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }//End of try-catch block
        return result;
    }


    private static Bitmap rotate(Bitmap bm, int rotation) {
        /*if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, false);
        }*/
        return bm;
    }

    private static final int HORIZONTAL = 1;

    private static final int VERTICAL = 2;

    private static final int SQUARE = 3;

    private static final float MAX_WIDTH = 1280;


    public static Bitmap HDBitmap(Bitmap bm, float max) {

        return HDBitmap(bm, max, 0);

    }
    public static Bitmap HDBitmap(Bitmap bm, float max, int radius){

        if(max == 0){

            max = MAX_WIDTH;

        }


        int orientation = VERTICAL;

        float newWidth = 0;

        float newHeight = 0;

        float actualWidth = bm.getWidth();

        float actualHeight = bm.getHeight();


        //Log.d("BMP_PROC", "H: Actual size: "+String.valueOf(actualWidth)+"x"+String.valueOf(actualHeight));

        if(actualWidth > actualHeight){

            orientation = HORIZONTAL;

        }else if(actualHeight == actualWidth){

            orientation = SQUARE;

        }

        if(orientation == VERTICAL){

            newHeight = max; // 1000 * 900 / 100

            float ratio = (newHeight < actualHeight) ? newHeight/actualHeight : actualHeight/newHeight;

            newWidth = (actualWidth * ratio);

            //Log.d("BMP_PROC", "V: Ratio: "+String.valueOf(ratio)+" New size: "+String.valueOf(newWidth)+"x"+String.valueOf(newHeight));

        }else if(orientation == HORIZONTAL){

            newWidth = max;

            float ratio = (newWidth < actualWidth) ? newWidth/actualWidth : actualWidth/newWidth;

            newHeight = (actualHeight*ratio);

            //Log.d("BMP_PROC", "H: New size: "+String.valueOf(newWidth)+"x"+String.valueOf(newHeight));

        }else{

            newWidth = MAX_WIDTH;

            newHeight = MAX_WIDTH;

        }

        Bitmap bmp = Bitmap.createScaledBitmap(bm, Math.round(newWidth), Math.round(newHeight), true);

        if(radius > 0) {

            return getRoundedCornerBitmap(bmp, radius);

        }else{

            return bmp;

        }

    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {

        float width = bitmap.getWidth();
        float ratio = 9f/16.5f;

        int height = Math.round(width * ratio);

        int top = 100;

        if(bitmap.getHeight()>height) {

            top = -1 * Math.round((bitmap.getHeight() - height) / 2);

        }

        Bitmap temp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas te = new Canvas(temp);

        te.drawBitmap(bitmap, 0, top, null);

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), height);
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(temp, rect, rect, paint);

        return output;
    }

}
