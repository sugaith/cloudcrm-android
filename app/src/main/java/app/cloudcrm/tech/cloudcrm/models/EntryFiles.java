package app.cloudcrm.tech.cloudcrm.models;

import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import app.cloudcrm.tech.cloudcrm.classes.App;

/**
 * Created by Alberto on 23/6/2016.
 */
@DatabaseTable(tableName = "entryfiles")
public class EntryFiles {

    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField
    int remoteId;
    @DatabaseField
    int formId;
    @DatabaseField
    int entryId;
    @DatabaseField
    String field;
    @DatabaseField
    String filePath;
    @DatabaseField
    boolean uploaded = false;

    static String TAG = "EntryFiles";

    public int getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(int remoteId) {
        this.remoteId = remoteId;
    }

    public static String getTAG() {
        return TAG;
    }

    public static void setTAG(String TAG) {
        EntryFiles.TAG = TAG;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public String readFile(){

        File file = new File(filePath);

        if(file.exists()){

            try {

                DataInputStream dis = new DataInputStream(new FileInputStream(file));

                byte[] fileContent =  new byte[(int) file.length()];

                dis.readFully(fileContent);

                String result  = Base64.encodeToString(fileContent, Base64.DEFAULT);

                ////Log.d(TAG, result);

                return result;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return "";

    }


    public static String createFile(int formId, int entryId, String field){

        EntryFiles file = null;

        boolean isCreating = false;

        try{

            file = App.files.queryBuilder().where().eq("formId", formId).and().eq("entryId", entryId).and().eq("field", field).queryForFirst();

            if(file == null){
                isCreating = true;
                file = new EntryFiles();
            }

        }catch (Exception e){

            isCreating = true;

            file = new EntryFiles();

        }

        file.setFormId(formId);

        //Log.d("SendingFile", "IsCreating " + String.valueOf(isCreating));
        //Log.d("SendingFile, EntryId:", String.valueOf(entryId));
        //Log.d("SendingFile, formId:", String.valueOf(formId));
        //Log.d("SendingFile, field:", String.valueOf(field));
        //Log.d("SendingFile, EntryId:", String.valueOf(file.getEntryId()));
        //Log.d("SendingFile, FileId:", String.valueOf(file.getId()));

        file.setEntryId(entryId);

        file.setField(field);

        file.setUploaded(false);

        file.setFilePath(Environment.getExternalStorageDirectory() +
                        File.separator +
                        ".cloudcrm" +
                        File.separator +
                        String.valueOf(formId) + "_" +
                        String.valueOf(entryId) + "_" +
                        field + ".jpg"

        );

        File f = new File(file.getFilePath());

        try {
            if(isCreating) {
                App.files.create(file);
            }else{
                App.files.update(file);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Log.d(TAG, file.getFilePath());

        return file.getFilePath();

    }


}
