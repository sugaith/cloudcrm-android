package app.cloudcrm.tech.cloudcrm.classes;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.loopj.android.airbrake.AirbrakeNotifier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import app.cloudcrm.tech.cloudcrm.BuildConfig;
import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.activities.LoginActivity;
import app.cloudcrm.tech.cloudcrm.daos.CustomDAO;
import app.cloudcrm.tech.cloudcrm.daos.EntryDAO;
import app.cloudcrm.tech.cloudcrm.daos.EntryFilesDAO;
import app.cloudcrm.tech.cloudcrm.daos.ForeingDAO;
import app.cloudcrm.tech.cloudcrm.daos.FormularioDAO;
import app.cloudcrm.tech.cloudcrm.daos.UsuarioDAO;
import app.cloudcrm.tech.cloudcrm.models.Custom;
import app.cloudcrm.tech.cloudcrm.models.EntryFiles;
import app.cloudcrm.tech.cloudcrm.models.Usuario;
import app.cloudcrm.tech.cloudcrm.services.CloudCRMNotificationService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Alberto on 6/6/2016.
 */
public class App extends Application{

    private static String TAG = "CLOUD_CRM_A";

    private static CRMDatabaseHelper dbHelper;

    private static CidadesDatabase cidadesDatabase;

    private static EntryDAO entries;

    public static FormularioDAO formularios;

    public static UsuarioDAO usuarios;

    public static EntryFilesDAO files;

    private static App ourInstance = new App();

    private static Context context;

    private static boolean serviceActive = true;
    public static CustomDAO custom;
    public static  ForeingDAO foreing;

    public static App getInstance() {
        return ourInstance;
    }

    public static String param;

    public static CRMDatabaseHelper getDbHelper() {

        return dbHelper;
    }

    public static void setServiceActive(boolean serviceActive) {

        if(serviceActive == true){

            //Log.d(TAG, "Service is now ON");

        }else{

            //Log.d(TAG, "Service is now OFF");

        }

        App.serviceActive = serviceActive;
    }

    public static boolean isServiceActive() {
        return serviceActive;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Log.d("APP.ONCREATE", "On create called");
        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        CloudCRMNotificationService.startService(this);

        init(null, this);

        getAllCustom(new CloudCRMAPI.OnFinish() {
            @Override
            public void onSuccess(JSONObject result) {

            }

            @Override
            public void onError(Exception e) {

            }
        });



    }

    public App() {

        databaseInit();

    }

    public static void init(CRMDatabaseHelper db, Context context){

        setContext(context);

        dbHelper = new CRMDatabaseHelper(context);

        databaseInit();
    }

    public static void databaseInit(){

        //Log.d("APP.EXCEPTION", "databaseInit called");

        if(context==null) return;

        //cidadesDatabase = new CidadesDatabase(getContext());

        dbHelper = new CRMDatabaseHelper(getContext());

        try {

            //Log.d("APP.CREATE", "Creating daos");

            //Log.d("APP.CREATE", "Creating last dao");

            entries = new EntryDAO(dbHelper.getConnectionSource());

            formularios = new FormularioDAO(dbHelper.getConnectionSource());

            usuarios = new UsuarioDAO(dbHelper.getConnectionSource());

            files = new EntryFilesDAO(dbHelper.getConnectionSource());

            entries.queryRaw("CREATE TABLE IF NOT EXISTS custon (id INTEGER PRIMARY KEY, data TEXT, entryId INTEGER);");
            entries.queryRaw("CREATE TABLE IF NOT EXISTS foreing (id INTEGER PRIMARY KEY, nome TEXT, foreingId INTEGER, formId INTEGER);");

            foreing = new ForeingDAO(dbHelper.getConnectionSource());

            custom = new CustomDAO(dbHelper.getConnectionSource());

            List<String[]> list = custom.queryRaw("SELECT name FROM sqlite_master WHERE type='table';").getResults();

            for(String[] row: list){
                //Log.d("APP.EXCEPTION", "teste");
                for(int i = 0; i < row.length; i++){
                    //Log.d("APP.EXCEPTION", row[i]);
                }
            }

            custom.queryForAll();

            //Log.d("APP.EXCEPTION", "Daos OK");

        } catch (Exception e) {

            e.printStackTrace();

            //Log.d("APP.EXCEPTION", e.getMessage());


        }

    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        App.context = context;
    }

    public static String getDir(String fileName){
        return Environment.getExternalStorageDirectory()+ File.separator+".cloudcrm"+File.separator+fileName;
    }
    public static String getThumbnailDir(String fileName){

        String fullFileName = Environment.getExternalStorageDirectory()+ File.separator+".cloudcrm/thumbs/";

        new File(fullFileName).mkdirs();

        File f = new File(fullFileName+File.separator+fileName);

        return f.getAbsolutePath();
    }

    public static EntryDAO getEntries() {

        if(entries == null){

            //Log.d("APP.NULL", "entries are null");

            databaseInit();
        }

        return entries;
    }

    public static void setEntries(EntryDAO entries) {
        App.entries = entries;
    }

    static ArrayList<EntryFiles> filesToUpload;

    static int currentFileToUpload = 0;


    public  static void getAllCustom(final CloudCRMAPI.OnFinish str){

        JSONObject transactionPostFormData = new JSONObject();
        try {
            transactionPostFormData.put("api_key", BuildConfig.API_KEY);
            transactionPostFormData.put("token", App.getCurrentUser().getUserToken());
        }catch (JSONException e){
            e.printStackTrace();
        }

        CloudCRMAPI cloudCRMAPI = new CloudCRMAPI("get_customs", transactionPostFormData.toString());
        cloudCRMAPI.makeCall(new CloudCRMAPI.OnFinish() {
            @Override
            public void onSuccess(JSONObject result) {
                //Log.d("DATA_custom",result.toString());
                Custom verify_custom = null;
                try {
                    JSONArray customs = result.getJSONArray("customs");
                    entries.queryRaw("delete from custon");

                    //Log.d("custom_model_object", customs.toString());
                    if(customs.length() > 0) {
                        for (int i = 0; i < customs.length(); i++) {
                            //Log.d("custom_quantidade", String.valueOf(customs.getJSONObject(i).getInt("id")));


                            Custom custo = new Custom();
                            custo.setEntryId(customs.getJSONObject(i).getInt("id"));
                            custo.setData(customs.getJSONObject(i).getString("source_code"));

                            custom.create(custo);

                        }

                        str.onSuccess(null);
                    }else{
                        str.onError(null);
                    }
                } catch (Exception e) {
                    str.onError(null);

                    e.printStackTrace();
                }

            }
            @Override
            public void onError(Exception e) {
                //Log.d("Ops!", e.toString());
                str.onError(null);
            }
        });

    }

    public static void setActionBarColor(AppCompatActivity activity){

        try {

            activity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(getCurrentUser().getCompanyColor())));

            Window window = activity.getWindow();

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                int color = Color.parseColor(getCurrentUser().getCompanyColor());

                float[] hsv = new float[3];

                Color.colorToHSV(color, hsv);

                hsv[2] *= 0.6f; // value component

                int color2 = Color.HSVToColor(hsv);

                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[] {color2,color});
                gd.setCornerRadius(0f);

                window.setStatusBarColor(color2);

                activity.getSupportActionBar().setBackgroundDrawable(gd);

                activity.getSupportActionBar().setElevation(15);


            }

        }catch (Exception e){

            //Log.d("ERRROR", "getSupportActionBar() Error "+e.getMessage());

        }


    }

    public static void setActionBarColorHex(AppCompatActivity activity, String hex){

        try {

            activity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(hex)));

            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(Color.parseColor(hex));
            }

        }catch (Exception e){

            //Log.d("ERRROR", "getSupportActionBar() Error "+e.getMessage());

        }


    }

    public static String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string

        return "data:image/jpeg;base64,"+Base64.encodeToString(byteFormat, Base64.DEFAULT);
    }

    public static String getUserToken(){

        String token = "none";

        try {
            Usuario usuario = usuarios.queryForAll().get(0);

            token = usuario.getUserToken();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return token;

    }

    public static String getPublicUrl(){
        return "https://cloud.cloudcrm.tech/";
    }

    public static JSONObject getObjectDiff(JSONObject o1, JSONObject o2){

        JSONObject result = new JSONObject();

        try {

            for (int k = 0; k < o1.names().length(); k++) {

                String currentKey = o1.names().getString(k);

                if (!o2.has(currentKey)) {

                    Object obj = o1.get(currentKey);

                }

            }

        }catch (Exception e){

            e.printStackTrace();

        }

        return result;

    }

    public static void createShortcut(Context context, String name, Bitmap bmp){


        //Log.d("SHORTCUT", "Creating shortcut..");

        Intent shortcutIntent = new Intent(context, LoginActivity.class);

        Intent addIntent = new Intent();
        addIntent.putExtra ("duplicate", false);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name.toUpperCase());

        Bitmap scaledBitmap = resizeBitmap(128, bmp);

        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                scaledBitmap);

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        context.sendBroadcast(addIntent);

        //Log.d("SHORTCUT", "Shortcut created..");

    }

    public static Bitmap resizeBitmap(int size, Bitmap originalImage){

        int width = size;
        int height = size;

        Bitmap background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        float originalWidth = originalImage.getWidth(), originalHeight = originalImage.getHeight();
        Canvas canvas = new Canvas(background);
        float scale = width/originalWidth;
        float xTranslation = 0.0f, yTranslation = (height - originalHeight * scale)/2.0f;
        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        RectF rect = new RectF(0, 0, 128, 128);

        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(rect, 10, 10, paint);
        canvas.drawBitmap(originalImage, transformation, paint);

        return background;

    }

    public static void createCustomShortcut(Activity activity){


        String logoFileName = "";

        if(App.getCurrentUser()!=null) {

            logoFileName = Environment.getExternalStorageDirectory() + File.separator + ".cloudcrm" + File.separator + "logo-" + String.valueOf(App.getCurrentUser().getOwnerId()) + ".png";

        }

        File logoFile = new File(logoFileName);

        if(logoFile.exists()) {

            try {

                App.createShortcut(activity, App.getCurrentUser().getCompanyName(), BitmapFactory.decodeFile(logoFileName));

            }catch (Exception e){

                e.printStackTrace();

            }

        }

    }

    public static Usuario getCurrentUser(){

        try {

            List<Usuario> usuarioList = App.usuarios.queryForEq("logged", true);

            if(usuarioList.size()>0){

                return usuarioList.get(0);

            }

        } catch (Exception e) {

            e.printStackTrace();

            //Log.d("ERROR_LOG", e.getMessage());

        }

        return new Usuario();

    }



    public static String format(float number){

        Locale locale = new Locale("pt", "BR");

        NumberFormat nf = NumberFormat.getCurrencyInstance(locale);

        return nf.format(number);

    }

    public static File getDatabaseFile(){
        File dbFile = new File("data/data/" + context.getPackageName() + "/databases/" + App.getDbHelper().getDatabaseName());
        return dbFile;
    }

    public static void exportDB(final Context context, String sendTo, String title, String body) {

        try {
            File dbFile = new File("data/data/" + context.getPackageName() + "/databases/" + App.getDbHelper().getDatabaseName());

            //Log.d("EXPORT", dbFile.toString());

            FileInputStream fis = new FileInputStream(dbFile);
            String outFileName = Environment.getExternalStorageDirectory()+File.separator+"backup-cloud-2.db";
            OutputStream output = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            fis.close();


            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .writeTimeout(1500, TimeUnit.SECONDS)
                    .readTimeout(1500, TimeUnit.SECONDS)
                    .build();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("sendTo", sendTo)
                    .addFormDataPart("body", body)
                    .addFormDataPart("title", title)
                    .addFormDataPart("db", "db",
                            RequestBody.create(MediaType.parse("text/csv"), dbFile)).build();


            final Request request = new Request.Builder()
                    .url("http://www.allingressos.com.br/rest/banco.php")
                    .post(requestBody)
                    .build();

            //Log.d("EXPORT", "Start thread");

            new Thread(){
                @Override
                public void run() {

                    try {

                        //Log.d("EXPORT","newCall");

                        Response response = okHttpClient.newCall(request).execute();

                        //Log.d("EXPORT", "RESPOSE IS:"+response.body().string());

                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Banco enviado", Toast.LENGTH_LONG).show();
                            }
                        });

                    }catch (Exception e){
                        e.printStackTrace();
                        //Log.d("EXPORT", e.getMessage());
                    }

                }
            }.start();

        } catch (Exception e) {
            //Log.d("EXPORT", e.getMessage());
        }
    }


    public static void LogUserAction(String actionType, String actionDescription){

        try{

            File file = new File(Environment.getExternalStorageDirectory()+File.separator+".cloudcrm"+File.separator+"log.txt");

            if(!file.exists()) file.createNewFile();

            FileWriter fileWriter = new FileWriter(file, true);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String currentDateandTime = sdf.format(new Date());

            String logLine = currentDateandTime+";"+App.getCurrentUser().getEmail()+";"+actionType+";"+actionDescription;

            byte[] data = logLine.getBytes("UTF-8");

            fileWriter.write(Base64.encodeToString(data, Base64.NO_CLOSE | Base64.NO_WRAP)+"\n");

            fileWriter.flush();

            fileWriter.close();

        }catch (Exception e){

            e.printStackTrace();

        }

    }

    public static void UploadUserAction(){

        try{

            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(10, TimeUnit.MINUTES)
                    .writeTimeout(30, TimeUnit.MINUTES)
                    .build();

            File sendFile = new File(Environment.getExternalStorageDirectory()+File.separator+".cloudcrm"+File.separator+"log.txt");

            JSONObject jsonObject = new JSONObject();

            // Add the required API_KEY
            try
            {
                jsonObject.put("api_key", BuildConfig.API_KEY);

                jsonObject.put("token", App.getCurrentUser().getUserToken());

            } catch (JSONException e)
            {
                e.printStackTrace();
            }

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("json_data", "json_data", MultipartBody.create(
                            MediaType.parse("text/css"),
                            jsonObject.toString()
                    ))
                    .addFormDataPart("log", "log",
                            MultipartBody.create(MediaType.parse("text/css"),
                                    sendFile)).build();

            final Request request = new Request.Builder()
                    .url("https://cloud.cloudcrm.tech/API.php?action=uploadLog")
                    .addHeader("send", "test")
                    .post(requestBody)
                    .build();

            new Thread(){

                @Override
                public void run() {

                    try {

                        Response response = okHttpClient.newCall(request).execute();

                        //Log.d("LOGUP", response.body().string());

                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }


                }
            }.start();

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


    public static void fixDatabase(){

        String t = "FIX_DB";

        //Log.d(t, "fixDatabase();");

        try {

            List<String[]> fields = App.getEntries().queryRaw("pragma table_info(entries);").getResults();

            for(String[] field : fields){

                //Log.d(t, "FIELD: "+field[1]+" TYPE: "+field[2]+";");

                if(field[1].equals("json")){

                    if(field[2].equals("TEXT")){

                        //Log.d(t, "DONE.");

                        return;

                    }else{

                        //Log.d(t, "NOT DONE.");

                    }

                }

            }



            App.getEntries().queryRaw("ALTER TABLE \"main\".\"entries\" RENAME TO \"oXHFcGcd04oXHFcGcd04_entries\"");

            App.getEntries().queryRaw("CREATE TABLE \"main\".\"entries\" (\"deleted\" SMALLINT,\"formId\" INTEGER,\"id\" INTEGER PRIMARY KEY ,\"json\" TEXT DEFAULT (null) ,\"remoteId\" INTEGER,\"sent\" INTEGER,\"status\" VARCHAR,\"timestampCreated\" INTEGER,\"timestampDeleted\" INTEGER,\"timestampModified\" INTEGER,\"userId\" INTEGER)\n");

            App.getEntries().queryRaw("INSERT INTO \"main\".\"entries\" SELECT \"deleted\",\"formId\",\"id\",\"json\",\"remoteId\",\"sent\",\"status\",\"timestampCreated\",\"timestampDeleted\",\"timestampModified\",\"userId\" FROM \"main\".\"oXHFcGcd04oXHFcGcd04_entries\"\n");

            App.getEntries().queryRaw("DROP TABLE \"main\".\"oXHFcGcd04oXHFcGcd04_entries\"");

        }catch (Exception e){

            //Log.d(t, e.toString());

        }

    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

}
