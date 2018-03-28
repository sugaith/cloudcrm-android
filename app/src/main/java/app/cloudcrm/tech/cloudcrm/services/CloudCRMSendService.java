package app.cloudcrm.tech.cloudcrm.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONObject;

import java.sql.SQLException;

import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMAPI;
import app.cloudcrm.tech.cloudcrm.classes.OnFinishListener;
import app.cloudcrm.tech.cloudcrm.models.Entry;

/**
 * Created by Alberto on 17/7/2016.
 */
public class CloudCRMSendService extends BroadcastReceiver {

    public static String TAG = "CLOUD_CRM_S";

    Context context;

    @Override
    public void onReceive(final Context context, Intent intent) {

        int entryId = intent.getIntExtra("entryId", -1);

        //Log.d(TAG, "Service is running..");

        if(!App.isServiceActive()){

            //Log.d(TAG, "Service is currently inactive");

            return;
        }

        if(entryId == -1){

            //Log.d(TAG,"mandando tudo");
            try {
                Entry.uploadAll(context, new OnFinishListener() {
                    @Override
                    public void onFinish(String response) {

                        ////Log.d(TAG, response);
                        ////Log.d(TAG, "resposta servidor");

                    /*
                    Entry.getAllData(context, new OnFinishListener() {
                        @Override
                        public void onFinish(String response) {

                        }
                    });
                    */

                    }

                });
            }catch (Exception e){
                e.getStackTrace();
            }


        }else{

            try {

                //Log.d("DataSend",String.valueOf(entryId));
                Entry entry = App.getEntries().queryForId(entryId);
                //Log.d("DataSend",entry.toString());
                entry.send(new CloudCRMAPI.OnFinish()
                {
                    @Override
                    public void onSuccess(JSONObject result) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }

    public  CloudCRMSendService(){

    }

    public CloudCRMSendService(Context context){
        this.context = context;
    }

    public void setAlarm(){

        try {

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, CloudCRMSendService.class);

            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

            am.setRepeating(AlarmManager.RTC_WAKEUP, 5000, 1000 * 60, pi);

        }catch (Exception e){

            //Log.d(TAG, e.getMessage());

        }


    }

}
