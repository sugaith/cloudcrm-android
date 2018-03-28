package app.cloudcrm.tech.cloudcrm.classes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.loopj.android.airbrake.AirbrakeNotifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.cloudcrm.tech.cloudcrm.models.Formulario;

/**
 * Created by Alberto on 6/6/2016.
 *
 */
public class CloudCRMActivity extends AppCompatActivity {

    public static final String TAG = "CloudCRMActivity";

    public static final int REQUEST_CODE = 1;

    String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CONTROL_LOCATION_UPDATES
    };
    private static Activity instance;

    public static Activity getInstance() {
        return instance;
    }


    protected void log(String textToLog){

        //Log.d("CC:"+this.getClass().getSimpleName(), textToLog);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        tests();

        super.onCreate(savedInstanceState);
        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        instance = this;

        App.setContext(this);

        log("Checking permissions");

        checkPermissions();

    }

    private void tests() {

        //Log.d("REGTEST",  String.valueOf("xtest[123]".matches(Formulario.IS_FOREING_PATTERN)));
        //Log.d("REGTEST",  String.valueOf("test-[123]".matches(Formulario.IS_FOREING_PATTERN)));
        //Log.d("REGTEST",  String.valueOf("test -Aaz!@$[123]".matches(Formulario.IS_FOREING_PATTERN)));

    }

    @Override
    protected void onStop() {

        log("CloudCRM");

        super.onStop();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        log("CODE: "+requestCode);

        try {

            log("PERMISSION: " + permissions[requestCode - 1]);


        }catch (Exception e){

            log(e.getMessage());

        }
    }
    protected void checkPermissions(){

        int i = 1;

        boolean isOK = true;

        List<String> stockList = new ArrayList<String>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            for(int c = 0; c < permissions.length; c++){

                int result = ContextCompat.checkSelfPermission(this, permissions[c]);

                //Log.d("PERMISSIONS", "Checking: "+permissions[c]);

                if(result == PackageManager.PERMISSION_GRANTED){

                    //Log.d("PERMISSIONS", "PackageManager.GRANTED");

                }else if(result == PackageManager.PERMISSION_DENIED){

                    //Log.d("PERMISSIONS", "PackageManager.DENIED");

                    isOK = false;

                    stockList.add(permissions[c]);

                }else{

                    //Log.d("PERMISSIONS", "PackageManager.UNKNOWN");

                }



            }

            String[] stockArr = new String[stockList.size()];
            stockArr = stockList.toArray(stockArr);

            if(!isOK) {

                ActivityCompat.requestPermissions(this, stockArr, i);

            }

        }

    }

}
