package app.cloudcrm.tech.cloudcrm.forms;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.sunmi.impl.V1Printer;

import app.cloudcrm.tech.cloudcrm.R;

/**
 * Created by Alberto on 21/6/2016.
 */
public class CustomSignatureListener implements SignaturePad.OnSignedListener{

    SignaturePad signaturePad;

    CustomSignatureListener(SignaturePad signaturePad){

        //Log.d("SIGNPAD", "constructor");

        this.signaturePad = signaturePad;

    }

    @Override
    public void onStartSigning() {

        //Log.d("SIGNPAD", "onStartSigning");

    }

    @Override
    public void onSigned() {

        //Log.d("SIGNPAD", "onSigned");

        Bitmap bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ALPHA_8);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        paint.setColor(Color.BLACK);

        paint.setTextAlign(Paint.Align.CENTER);

        paint.setTextSize(19);

        canvas.drawText("Test", 0, 0, paint);

    }

    @Override
    public void onClear() {

        //Log.d("SIGNPAD", "onClear");

    }

    public SignaturePad getSignaturePad() {
        return signaturePad;
    }

    public void setSignaturePad(SignaturePad signaturePad) {
        this.signaturePad = signaturePad;
    }
}