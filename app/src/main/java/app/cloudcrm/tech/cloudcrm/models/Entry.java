package app.cloudcrm.tech.cloudcrm.models;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import app.cloudcrm.tech.cloudcrm.BuildConfig;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMAPI;
import app.cloudcrm.tech.cloudcrm.classes.OnFinishListener;
import app.cloudcrm.tech.cloudcrm.forms.Campo;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Alberto on 17/6/2016.
 */
@DatabaseTable(tableName = "entries")
public class Entry {

    public static String STATUS_NORMAL = "A";
    public static String STATUS_DELETED = "C";
    public static String STATUS_DELETED_PERM = "X";

    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField
    int remoteId;
    @DatabaseField
    int formId;
    @DatabaseField
    int timestampCreated;
    @DatabaseField
    int timestampModified;
    @DatabaseField
    int sent = 0;
    @DatabaseField
    int userId = 0;
    @DatabaseField
    boolean deleted;
    @DatabaseField
    int timestampDeleted;
    @DatabaseField
    String json;
    @DatabaseField
    String status = "A";

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Entry() {

        this.userId = App.getCurrentUser().getRemoteId();

    }

    public static boolean isSending() {
        return sending;
    }

    public static void setSending(boolean sending) {
        Entry.sending = sending;
    }

    static  boolean sending = false;

    public Entry getThis(){
        return this;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(int remoteId) {
        this.remoteId = remoteId;
    }

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public int getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(int timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    public int getTimestampModified() {
        return timestampModified;
    }

    public void setTimestampModified(int timestampModified) {
        this.timestampModified = timestampModified;
    }

    public int getSent() {
        return sent;
    }

    public void setSent(int sent) {
        this.sent = sent;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getTimestampDeleted() {
        return timestampDeleted;
    }

    public void setTimestampDeleted(int timestampDeleted) {
        this.timestampDeleted = timestampDeleted;
    }

    /**
     * @param onFinish
     *
     * This function sends the entry to the REST API
     *
     */

    public void send(final CloudCRMAPI.OnFinish onFinish){

        if(isSending())
        {
            //Log.d("Uploaded", "Already sending..");
            return;
        }

        setSending(true);

        final JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("data", new JSONObject(getJson()));

            //Log.d("DataSend",jsonObject.getJSONObject("data").toString());
            jsonObject.put("formId", getFormId());
            jsonObject.put("entryId", getRemoteId());
            jsonObject.put("userId", getUserId());

            //Log.d("Uploaded", jsonObject.toString());

        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        try {

            JSONArray keys = jsonObject.getJSONObject("data").names();

            for (int c = 0; c < keys.length(); c++) {

                try {

                    //Log.d("IMG_DEBUG_UPLOAD_IMAGE", "CHECK:" + keys.getString(c));

                    String value = jsonObject.getJSONObject("data").getString(keys.getString(c));

                    File f = new File(value);

                    if (f.exists()) {

                        //Log.d("IMG_DEBUG_UPLOAD_IMAGE", "FILE_EXIST: " + f.getAbsolutePath());

                        Bitmap b = BitmapFactory.decodeFile(f.getAbsolutePath());

                        //Log.d("IMG_DEBUG_DECODE", "ImageSize: "+String.valueOf(b.getWidth())+"x"+String.valueOf(b.getHeight()));

                        //f.delete();

                        if (b != null) {

                            //Log.d("IMG_DEBUG_UPLOAD_IMAGE", "FILE: " + f.getAbsolutePath() + " KEY: " + keys.getString(c));

                            jsonObject.getJSONObject("data").put(keys.getString(c), App.getEncoded64ImageStringFromBitmap(b));

                        }

                    }

                } catch (Exception e) {

                    e.printStackTrace();

                }

            }

        }catch (Exception e){

            e.printStackTrace();

        }

        new CloudCRMAPI("upload_entries", jsonObject.toString())
        .makeCall(new CloudCRMAPI.OnFinish() {
            @Override
            public void onSuccess(JSONObject responseObject)
            {


                String mTAG = "UPLOADED_OK";

                try
                {

                    //Log.d(mTAG, responseObject.toString());

                    try {

                        setRemoteId(responseObject.getInt("id"));
                    }catch (Exception e){
                        setRemoteId(0);
                    }

                    setSent(1);

                    Formulario formulario = App.formularios.queryForEq("remoteId", formId).get(0);

                    formulario.generateCamposFromJson();

                    ArrayList<Campo> campos = formulario.getCampos();

                    //Log.d(mTAG, "for()");

                    try{

                        for(Campo campo: campos){

                            if(!campo.getTipo().getTipo().equals(Formulario.TYPE_PICTURE)){
                                continue;
                            }

                            try {

                                if(!jsonObject.getJSONObject("data").has(campo.getId())) continue;

                                String val = jsonObject.getJSONObject("data").getString(campo.getId());

                                if(val.startsWith("https://")){

                                    continue;

                                }

                                //Log.d(mTAG, "Check campo:"+campo.getId());

                                if(val.startsWith("data:")){

                                    String url = "https://cloud.cloudcrm.tech/?action=image&format=1&table=form_"+
                                            String.valueOf(formulario.getRemoteId())+"&field="+campo.getId()+"&id="+String.valueOf(getRemoteId());


                                    //Log.d(mTAG, "URL:"+url);

                                    jsonObject.getJSONObject("data").put(campo.getId(), url);


                                }



                            }catch (Exception e){

                                e.printStackTrace();

                                //Log.d(mTAG, e.toString());

                            }


                        }

                    }catch (Exception e){

                        e.printStackTrace();

                        //Log.d(mTAG, e.toString());


                    }


                    if (jsonObject.has("send_to")) {
                        jsonObject.remove("send_to");
                    }

                    setJson(jsonObject.getJSONObject("data").toString());
                    //Log.d("enviar_email", jsonObject.toString());
                    //Log.d(mTAG, jsonObject.toString());

                    App.getEntries().update(getThis());

                    //Log.d(mTAG, "Save");


                    setSending(false);

                    onFinish.onSuccess(jsonObject);


                }catch (Exception e){

                    //Log.d(mTAG, e.getMessage());

                    onError(e);
                }
            }
            @Override
            public void onError(Exception e) {
                setSending(false);
                onFinish.onError(e);
            }
        });

    }

    public static void getAll(final Context context, final int formId, final OnFinishListener onFinishListener){

        new Thread(){

            @Override
            public void run() {

                super.run();

                String responseText = null;

                JSONObject jsonObjectData = new JSONObject();

                //Log.d("DEBUG_ENTRIES", "FORM_ID:"+String.valueOf(formId));

                try {
                    jsonObjectData.put("formId", formId);
                    jsonObjectData.put("api_key", BuildConfig.API_KEY);
                    jsonObjectData.put("token", App.getCurrentUser().getUserToken());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("json_data", "json_data",
                                RequestBody.create(MediaType.parse("application/json"), jsonObjectData.toString())).build();

                //Log.d("HRESPONSE", jsonObjectData.toString());

                String mURL = BuildConfig.SERVER_URL +
                        "action=get_entries_optimized";

                //Log.d("OkHttp", mURL);

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .readTimeout(500, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .build();


                Request request = new Request.Builder()
                        .url(mURL)
                        .addHeader("Atest", "testing")
                        .post(requestBody)
                        .build();

                try {

                    //Log.d("OkHttp", "Execute();");

                    Response response = okHttpClient.newCall(request).execute();

                    responseText = response.body().string();

                    //Log.d("DEBUG_ENTRIES", "new JSONObject: "+String.valueOf(responseText.length()));

                    JSONObject result = new JSONObject(responseText);

                    checkNDelete(result);

                    //Log.d("HRESPONSE", responseText);

                    JSONArray jsonArray = result.getJSONArray("lista");

                    for(int i = 0; i < jsonArray.length(); i++){

                        try {

                            //Log.d("DEBUG_ENTRIES", "getJSONObject");

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            int remoteId = jsonObject.getInt("id");

                            int id = 0;

                            Entry entry = new Entry();

                            //Log.d("DEBUG_ENTRIES", "queryBuilder()");

                            ArrayList<Entry> entries = null;

                            try {

                                //entries = new ArrayList<Entry>(App.getEntries().queryBuilder()
                                //        .where()
                                //        .eq("remoteId", remoteId)
                                //       .and()
                                //        .eq("formId", formId)
                                //       .query());



                                String sql = "select id, json from `entries` where remoteId = "+String.valueOf(remoteId)+" and formId = "+String.valueOf(formId)+";";

                                List<String[]> test = App.getEntries().queryRaw(sql).getResults();

                                //Log.d("DEBUG_ENTRIES", sql);

                                id = Integer.parseInt(test.get(0)[0]);

                                if(id > 0){

                                    //Log.d("DEBUG_ENTRIES", "Local id = "+String.valueOf(id));

                                    //Log.d("DEBUG_ENTRIES", "Size: "+test.get(0)[1]);

                                    entry = App.getEntries().queryForId(id);

                                    //Log.d("DEBUG_ENTRIES", "Entry fetched");

                                }

                            }catch (Exception e){

                                //Log.d("DEBUG_ENTRIES", "Has an exception: "+e.toString());

                                //e.printStackTrace(); return ;

                                entries = new ArrayList<Entry>();

                            }

                            //Log.d("DEBUG_ENTRIES", "after query builder");

                            if (id > 0) {

                                //entry = entries.get(0);

                                if (entry.getSent() == 0)
                                    continue;

                            }

                            entry.setRemoteId(remoteId);

                            entry.setSent(1);

                            entry.setJson(jsonObject.toString());

                            ////Log.d("DEBUG_ENTRIES", "Size: "+String.valueOf(entry.s));

                            entry.setFormId(formId);

                            Integer user_creator = jsonObject.getInt("user_creator");
                            
                            if(result.getBoolean("mostra_todos")){
                                //Log.d("DEBUG_ENTRIES", "mostrar todos e true");
                                user_creator = App.getCurrentUser().getRemoteId();
                            }
                            entry.setUserId(user_creator);

                            //Log.d("DEBUG_ENTRIES", "UserId:"+String.valueOf(user_creator));

                            if (entry.getId() > 0) {

                                //Log.d("OkHttp", "Update");

                                //Log.d("DEBUG_ENTRIES", "UPDATE ENTRY");

                                App.getEntries().update(entry);

                            } else {

                                //Log.d("OkHttp", "Create");

                                //Log.d("DEBUG_ENTRIES", "CREATE ENTRY");

                                App.getEntries().create(entry);

                            }

                            //Log.d("DEBUG_ENTRIES", "AFter create or update");

                        }catch (Exception e){

                            //Log.d("ERROR_TEST", e.getMessage());

                            //Log.d("DEBUG_ENTRIES", "EXCEPTION: "+e.toString());

                        }


                    }


                } catch (Exception e) {

                    e.printStackTrace();

                    //Log.d("OkHttp", e.getMessage());

                }

                final String finalResponseText = responseText;

                onFinishListener.onFinish(finalResponseText);

            }
        }.start();

    }

    private static void checkNDelete(JSONObject result) {

        if(result.has("deleted")){

            try{

                JSONArray jsonArrayDeleted = result.getJSONArray("deleted");

                for(int i = 0; i < jsonArrayDeleted.length(); i++){

                    JSONObject jo = jsonArrayDeleted.getJSONObject(i);

                    //Log.d("DELETER", String.valueOf(jo.getInt("id")));

                    try {
                        Entry entry = App.getEntries().queryForEq("remoteId", jo.getInt("id")).get(0);
                        if (entry.getStatus().equals("A")) {
                            entry.setStatus("I");
                            App.getEntries().update(entry);
                            //Log.d("DELETER", "Deleted");
                        }
                    }catch (IndexOutOfBoundsException ex){
                        //Log.d("DELETER","not_found");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }

            }catch (Exception e){

                e.printStackTrace();

            }

        }else{

            //Log.d("DELETED", "else");

        }
    }

    static int currentFormulario = 0;

    static OnFinishListener theOnFinishListener;

    public static void getAllData(final Context context, OnFinishListener onFinishListener){

        if(onFinishListener != null){

            theOnFinishListener = onFinishListener;

            //Log.d("ONFINISH", "Not null");

        }

        try {

            ArrayList<Formulario> formularios = new ArrayList<Formulario>(App.formularios.queryForAll());


            if(currentFormulario>=formularios.size()){

                theOnFinishListener.onFinish("full");

                currentFormulario = 0;

                return;

            }

            Formulario formulario = formularios.get(currentFormulario);

            getAll(context, formulario.getRemoteId(), new OnFinishListener(){

                @Override
                public void onFinish(String response) {

                    currentFormulario++;

                    getAllData(context, null);

                }

            });



        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    static int currentEntry = 0;

    static OnFinishListener entryOnFinishListener;

    public static void uploadAll(final Context context, OnFinishListener onFinishListener) throws Exception {

        if(onFinishListener != null){

            entryOnFinishListener = onFinishListener;

            currentEntry = 0;

        }

        try{

            ArrayList<Entry> entries = new ArrayList<Entry>(App.getEntries().queryBuilder()
            .where()
                    .eq("remoteId", 0)
                    .or()
                    .eq("sent", 0)
                    .query()
            );

            ////Log.d("Uploaded", entries.toString());

            if(entries.size() == 0){

                //Log.d("Uploaded", "none");

                return;

            }

            if(currentEntry >= entries.size()){

                //Log.d("Uploaded", "End of");

                entryOnFinishListener.onFinish("ok");

                return;

            }

            final Entry current = entries.get(currentEntry);

            //Log.d("Uploaded", current.toString());

            current.send(new CloudCRMAPI.OnFinish() {
                @Override
                public void onSuccess(JSONObject result) {

                    currentEntry++;

                    //Log.d("Uploaded", "This one: "+String.valueOf(current.getRemoteId())+" -> "+String.valueOf(current.getFormId()));
                    try {
                        uploadAll(context, null);
                    }catch (Exception e){
                        e.getStackTrace();
                    }

                }

                @Override
                public void onError(Exception e) {
                    //Log.d("Uploaded", e.getMessage());
                }
            });

        }catch (Exception e){

            e.printStackTrace();

        }

    }

    public static void updateForms(Context context, final OnFinishListener onFinishListener) {

        new Thread() {

            @Override
            public void run() {

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .readTimeout(120, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .build();

                RequestBody requestBody = null;

                try{

                    JSONObject requestObject = new JSONObject();

                    requestObject.put("api_key", BuildConfig.API_KEY);

                    requestObject.put("token", App.getUserToken());

                    requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("json_data", "json_data",
                                    RequestBody.create(MediaType.parse("application/json"), requestObject.toString())).build();

                }catch (Exception e){

                    e.printStackTrace();

                }

                Request request = new Request.Builder()
                        .url(BuildConfig.SERVER_URL+"action=get_forms")
                        .post(requestBody)
                        .build();

                try {


                    Response response = okHttpClient.newCall(request).execute();

                    String result = response.body().string();

                    //Log.d("HRESPONSE", result);

                    Formulario.createFromJson(result);

                    onFinishListener.onFinish("onFinish");

                    try{

                        //Log.d("HRESPONSE", new JSONObject(result).toString(2));

                    }catch (Exception e){

                        e.printStackTrace();

                    }

                } catch (Exception e) {

                    e.printStackTrace();

                    onFinishListener.onFinish("Error");

                }

            }

        }.start();

    }

    public static ArrayList<Entry> getEntries(int formularioId){

        String xTAG = "GET_ENTRIES";

        ArrayList<Entry> entries = new ArrayList<>();

        try {

            //Log.d(xTAG, "Check form: " + String.valueOf(formularioId));

            QueryBuilder queryBuilder = App.getEntries()
                    .queryBuilder();

            queryBuilder.limit(1500L)
                    .where()
                    .eq("formId", formularioId)
                    .and()
                    .eq("status", Entry.STATUS_NORMAL)
                    .and()
                    .eq("userId", App.getCurrentUser().getRemoteId())
            ;

            CloseableIterator<Entry> iterator = App.getEntries().iterator(queryBuilder.prepare());

            try {
                // get the raw results which can be cast under Android
                AndroidDatabaseResults results =
                        (AndroidDatabaseResults) iterator.getRawResults();
                Cursor cursor = results.getRawCursor();

                //Log.d(xTAG, queryBuilder.prepare().toString());

                //Log.d(xTAG, "Cursor-iterate: " + String.valueOf(results.getCount()));

                String[] columns = cursor.getColumnNames();

                for (String column : columns) {
                    //Log.d(xTAG, "Name: " + column);
                }

                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                    //for(int i = 0; i < columns.length; i++) {

                    //    //Log.d("ENTCHECK", String.valueOf(i)+") "+columns[i]+" -> " + String.valueOf(cursor.getString(i)));

                    //}

                    Entry entry = new Entry();

                    entry.setId(cursor.getInt(2));

                    entry.setFormId(cursor.getInt(1));

                    entry.setJson(cursor.getString(3));

                    entry.setRemoteId(cursor.getInt(4));

                    //Log.d(xTAG, "add(entry);");

                    entries.add(entry);

                    cursor.moveToNext();

                }

            } finally {
                iterator.closeQuietly();
            }


        }catch (Exception e){

            e.printStackTrace();

        }
        return entries;
    }


    public static ArrayList<Entry> getEntry(int entryId){

        String xTAG = "GET_ENTRY";

        ArrayList<Entry> entries = new ArrayList<>();

        try {

            //Log.d(xTAG, "Check form: " + String.valueOf(entryId));

            QueryBuilder queryBuilder = App.getEntries()
                    .queryBuilder();

            queryBuilder.limit(1500L)
                    .where()
                    .eq("id", entryId)
                    .and()
                    .eq("status", Entry.STATUS_NORMAL)
                    .and()
                    .eq("userId", App.getCurrentUser().getRemoteId())
            ;

            CloseableIterator<Entry> iterator = App.getEntries().iterator(queryBuilder.prepare());

            try {
                // get the raw results which can be cast under Android
                AndroidDatabaseResults results =
                        (AndroidDatabaseResults) iterator.getRawResults();
                Cursor cursor = results.getRawCursor();

                //Log.d(xTAG, queryBuilder.prepare().toString());

                //Log.d(xTAG, "Cursor-iterate: " + String.valueOf(results.getCount()));

                String[] columns = cursor.getColumnNames();

                for (String column : columns) {
                    //Log.d(xTAG, "Name: " + column);
                }

                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                    //for(int i = 0; i < columns.length; i++) {

                    //    //Log.d("ENTCHECK", String.valueOf(i)+") "+columns[i]+" -> " + String.valueOf(cursor.getString(i)));

                    //}

                    Entry entry = new Entry();

                    entry.setId(cursor.getInt(2));

                    entry.setFormId(cursor.getInt(1));

                    entry.setJson(cursor.getString(3));

                    entry.setRemoteId(cursor.getInt(4));

                    //Log.d(xTAG, "add(entry);");

                    entries.add(entry);

                    cursor.moveToNext();

                }

            } finally {
                iterator.closeQuietly();
            }


        }catch (Exception e){

            e.printStackTrace();

        }
        return entries;
    }

}
