package app.cloudcrm.tech.cloudcrm.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.loopj.android.airbrake.AirbrakeNotifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMActivity;
import app.cloudcrm.tech.cloudcrm.classes.ImagePicker;

public class PickImageActivity extends CloudCRMActivity {

    public static String TAG = "PickImageActivity";

    public static int FROM_GALLERY = 0;

    public static int FROM_CAMERA = 1;

    public static int REQUEST_GALLERY = 96;

    public static int REQUEST_CAMERA = 45;

    public static int PICK_IMAGE = 9990;

    public static String EXTRA_FILENAME = "fileName";

    public static String EXTRA_FIELD = "field";

    public static String EXTRA_WHICH = "which";

    ImageView imageView;

    String fileName = "";

    int which = FROM_GALLERY;

    File file = null;

    Uri uri;

    String fieldName;

    int requestCode = REQUEST_GALLERY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        //Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_pick_image);

        setTitle(getResources().getString(R.string.action_tirar_foto));

        imageView = (ImageView) findViewById(R.id.imageView);

        fileName = getIntent().getStringExtra(EXTRA_FILENAME);

        which = getIntent().getIntExtra(EXTRA_WHICH, FROM_GALLERY);

        fieldName = getIntent().getStringExtra(EXTRA_FIELD);

        file = new File(fileName);

        if(file.exists()){

            //Log.d(TAG, file.getAbsolutePath()+" exists");

            if(file.delete()){
                //Log.d(TAG, fileName+" deleted");
            }

        }

        uri = Uri.fromFile(file);

        //Log.d(TAG, "Uri: "+uri.toString());

        onPickImage();

    }

    @Override
    public void onBackPressed() {

        setResult(RESULT_CANCELED);

        finish();

    }

    private static final int PICK_IMAGE_ID = 234; // the number doesn't matter

    public void onPickImage() {
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(this);
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PICK_IMAGE_ID:

                if(resultCode!=RESULT_OK){
                    setResult(RESULT_CANCELED);
                    finish();
                    return;
                }

                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                //imageView.setImageBitmap(bitmap);

                try {

                    FileOutputStream fOut = new FileOutputStream(file);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                    fOut.flush();

                    fOut.close();

                    Intent it = new Intent();

                    it.putExtra(EXTRA_FIELD, fieldName);

                    it.putExtra(EXTRA_FILENAME, fileName);

                    setResult(RESULT_OK, it);

                    finish();

                }catch (Exception e){

                    e.printStackTrace();

                }

                // TODO use bitmap
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

}
