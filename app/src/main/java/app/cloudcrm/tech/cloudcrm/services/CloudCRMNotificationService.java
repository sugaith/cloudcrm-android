package app.cloudcrm.tech.cloudcrm.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.activities.ListFormulariosActivity;
import app.cloudcrm.tech.cloudcrm.activities.NotificationActivity;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMAPI;

/**
 * Created by albertomiranda on 23/12/16.
 */

public class CloudCRMNotificationService extends BroadcastReceiver {

    public static String ACTION = "com.cloudcrm.notif";

    public static String TAG = "NOTIF_SERVICE";

    static AlarmManager alarmManager;

    static PendingIntent pendingIntent;

    static Intent intent;

    static boolean isSeted;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction()==null) return;

        if(!App.getSharedPreferences(context).getBoolean("notifications_new_message", true)){

            //Log.d(TAG, "Notifications disabled");

            return;
        }

        //Log.d(TAG, "Received notification: "+intent.getAction());

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || intent.getAction().equals("android.intent.action.MY_PACKAGE_REPLACED"))
        {


            if(context != null) {

                //Log.d(TAG, "startService()");

                startService(context);

            }

        }else{

            if(intent.getAction().equals(ACTION)) {

                //Log.d(TAG, "Waiting for notifications");

                CloudCRMAPI cloudCRMAPI = new CloudCRMAPI("get_notif", "{}");

                final Context context1 = context;

                cloudCRMAPI.makeCall(new CloudCRMAPI.OnFinish() {
                    @Override
                    public void onSuccess(JSONObject result) {

                        if(!result.has("notif")) return;

                        try {

                            JSONArray jsonArray = result.getJSONArray("notif");

                            for(int i = 0; i < jsonArray.length(); i++){

                                JSONObject resul = jsonArray.getJSONObject(i);

                                createNotificationNow(
                                        resul.getInt("id"),
                                        context1,
                                        resul.getString("title"),
                                        resul.getString("content"),
                                        resul.getString("full_content")
                                );

                            }

                            //Log.d(TAG, result.toString(2));

                        }catch (Exception e){

                            e.printStackTrace();

                        }

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

            }

        }

    }

    private static void createNotificationNow(int id, Context context, String title, String message, String fcontent) {

        //Log.d(TAG, "Notify");

        String ring = App.getSharedPreferences(context).getString("notifications_new_message_ringtone", "DEFAULT_SOUND");

        Uri alarmSound = null;

        if(ring.equals("DEFAULT_SOUND")) {

            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        }else{

            alarmSound = Uri.parse(ring);

        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent it = new Intent(context, ListFormulariosActivity.class);

        it.putExtra("open-light-box", true);

        it.putExtra("title", title);

        it.putExtra("message", message);

        it.putExtra("content", fcontent);

        it.putExtra("test", 1);

        it.putExtra("testing", 1);

        //Log.d(TAG, it.getExtras().toString());

        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, it, 0);

        Notification notif =  new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_cloud)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .addAction(android.R.drawable.ic_menu_view, context.getString(R.string.view_notif), pendingIntent)
                .setSound(alarmSound)
                .build()
                ;



        notif.contentIntent = pendingIntent;

        //notificationManager.notify(id, notif);

    }

    public static void startService(Context context){

        if(true) return;

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        intent = new Intent(ACTION);

        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 5000, 1000*60, pendingIntent);

    }

}
