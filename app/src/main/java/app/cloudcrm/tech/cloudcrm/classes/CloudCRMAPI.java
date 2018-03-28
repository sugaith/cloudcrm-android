package app.cloudcrm.tech.cloudcrm.classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.loopj.android.airbrake.AirbrakeNotifier;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import app.cloudcrm.tech.cloudcrm.BuildConfig;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Alberto on 15/9/2016.
 */
public class CloudCRMAPI {

    static final String TAG = "CloudCRMAPICall";

    static final int WRITE_TIMEOUT = 60000;

    static final int READ_TIMEOUT = 60000;

    static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    String action;

    String jsonString;

    OkHttpClient okHttpClient;

    Request request;

    RequestBody requestBody;

    Response response;

    String responseText;

    int responseStatus;

    OnFinish onFinish;

    String fileName;

    boolean decodeJson = true;

    public CloudCRMAPI saveTo(String fileName){

        this.fileName = fileName;

        decodeJson = false;

        return this;

    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public CloudCRMAPI(String action, String jsonString) {
        this.action = action;
        this.jsonString = jsonString;

        //Log.d(TAG, "New API Call, action="+action);

        //Log.d(TAG, "URL: "+BuildConfig.SERVER_URL);

        JSONObject jsonObject = null;

        try {

            jsonObject = new JSONObject(jsonString);

            jsonObject.put("api_key", BuildConfig.API_KEY);

            //Log.d(TAG, "API Key = "+BuildConfig.API_KEY);

            if(App.getCurrentUser()!=null){

                jsonObject.put("token", App.getCurrentUser().getUserToken());

            }

        }catch (Exception e){

            //Log.d(TAG, e.getMessage());
            AirbrakeNotifier.notify(e);
            e.printStackTrace();

            jsonObject = new JSONObject();

            try {

                jsonObject.put("api_key", BuildConfig.API_KEY);

                if(App.getCurrentUser()!=null){

                    jsonObject.put("token", App.getCurrentUser().getUserToken());

                }

            }catch (Exception el){

                el.printStackTrace();

            }

        }

        try {

            //Log.d(TAG, "API JSON DATA:\n"+jsonObject.toString(3));

        }catch (Exception e){

            e.printStackTrace();

        }

        okHttpClient = new OkHttpClient.Builder()
                .writeTimeout(WRITE_TIMEOUT, TIME_UNIT)
                .readTimeout(READ_TIMEOUT, TIME_UNIT)
                .build();

        requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("json_data", "json_data",
                        RequestBody.create(MediaType.parse("application/json"), jsonObject.toString())).build();

        request = new Request.Builder()
                .addHeader("Application-ID", "Android-App")
                .addHeader("Application-Version", String.valueOf(BuildConfig.VERSION_NAME))
                .url(BuildConfig.SERVER_URL+"action="+action)
                .post(requestBody)
                .build();

    }

    public void makeCall(final OnFinish onFinish){

        this.onFinish = onFinish;

        new Thread(){

            @Override
            public void run() {

                try {

                    response = okHttpClient.newCall(request).execute();
                    if(!response.isSuccessful()){
                        return;
                    }

                    if(decodeJson) {


                        responseText = response.body().string();
                        JSONObject temp = new JSONObject();
                        responseStatus = response.code();



                        try {
                            temp = new JSONObject(responseText);
                            //Log.d(TAG, "API RESPONSE:\n" + new JSONObject(responseText).toString(2));
                        }catch (Exception e){
                            e.printStackTrace();

                            //Log.d(TAG, "RESPONSE: "+responseText);

                        }

                        onFinish.onSuccess(temp);

                    }else{

                        createFile();

                    }

                }catch (Exception e){

                    onFinish.onError(e);

                    e.printStackTrace();

                }

            }
        }.start();

    }

    private void createFile() {

        //Log.d(TAG, "Create File '"+fileName+"'");

        try {

            //Log.d(TAG, "byteStream()");

            InputStream inputStream = response.body().byteStream();

            BufferedInputStream input = new BufferedInputStream(inputStream);
            OutputStream output = new FileOutputStream(fileName);

            byte[] data = new byte[1024];

            long total = 0;

            int count;

            //Log.d(TAG, "Escrevendo..");

            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }

            JSONObject result = new JSONObject();

            result.put("fileName", fileName);

            onFinish.onSuccess(result);

        }catch (Exception e){

            e.printStackTrace();

            onFinish.onError(e);

        }


    }


    public interface OnFinish{

        void onSuccess(JSONObject result);

        void onError(Exception e);

    }

}
