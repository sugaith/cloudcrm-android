package app.cloudcrm.tech.cloudcrm.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.table.TableUtils;
import com.loopj.android.airbrake.AirbrakeNotifier;

import org.json.JSONObject;

import java.io.File;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

import app.cloudcrm.tech.cloudcrm.BuildConfig;
import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMAPI;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMActivity;
import app.cloudcrm.tech.cloudcrm.classes.NumberFormater;
import app.cloudcrm.tech.cloudcrm.classes.NumberFormaterManager;
import app.cloudcrm.tech.cloudcrm.classes.OnFinishListener;
import app.cloudcrm.tech.cloudcrm.models.Formulario;
import app.cloudcrm.tech.cloudcrm.models.Entry;
import app.cloudcrm.tech.cloudcrm.models.Usuario;
import app.cloudcrm.tech.cloudcrm.misc.Settings;
import app.cloudcrm.tech.cloudcrm.services.CloudCRMNotificationService;
import app.cloudcrm.tech.cloudcrm.services.CloudCRMSendService;

public class ListFormulariosActivity extends CloudCRMActivity{

    ListView listViewFormularios;

    ImageView imageViewLogo;

    ArrayAdapter<String> arrayAdapter;

    ArrayList<Formulario> formularios;

    ArrayList<Usuario> usuarios;

    Usuario usuario;

    TextView textViewVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        float numero =  21.0f;

        /*Entry.'All(this, 313, new OnFinishListener() {
            @Override
            public void onFinish(String response) {
                //Log.d("DEBUG_ENTRIES", "Finished");
            }
        });*/

        //Log.d("TEST", "REST---");

        //Log.d("_TOKEN_", App.getCurrentUser().getUserToken());

        //Log.d("_TOKEN_", BuildConfig.API_KEY);

        App.fixDatabase();

        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        setContentView(R.layout.activity_list_formularios);

        SharedPreferences sharedPreferences = getSharedPreferences("startup", 0);

        if(!sharedPreferences.getBoolean("init", false)) {

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putBoolean("init", true);

            editor.apply();

            editor.commit();

            Intent it = new Intent(this, RelatorioActivity.class);

            /* startActivity(it); */

        }

        textViewVersion = (TextView) findViewById(R.id.textViewVersion);

        textViewVersion.setText(BuildConfig.VERSION_NAME);

        App.setActionBarColor(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(getIntent().getBooleanExtra("open-light-box", false)){

            //Log.d(CloudCRMNotificationService.TAG, "Open Lightbox");

            //Log.d(CloudCRMNotificationService.TAG, "Title: "+getIntent().getStringExtra("title"));

            //Log.d(CloudCRMNotificationService.TAG, "Content: "+getIntent().getStringExtra("content"));

            //Log.d(CloudCRMNotificationService.TAG, getIntent().getExtras().toString());

            new AlertDialog.Builder(this)
                    .setTitle(getIntent().getStringExtra("title"))
                    .setMessage(getIntent().getStringExtra("content"))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .create()
                    .show();

        }else{

            sendBroadcast(new Intent(CloudCRMNotificationService.ACTION));

        }

        try {

            CloudCRMSendService cloudCRMSendService = new CloudCRMSendService(this);

            cloudCRMSendService.setAlarm();

        }catch (Exception e){

            e.printStackTrace();

        }

        imageViewLogo = (ImageView) findViewById(R.id.imageViewLogo);

        imageViewLogo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    Entry.uploadAll(ListFormulariosActivity.this, new OnFinishListener() {
                        @Override
                        public void onFinish(String response) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ListFormulariosActivity.this, "Subido", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }catch (Exception e){
                   e.getStackTrace();
                }

                return true;
            }
        });

        setTitle(App.getCurrentUser().getCompanyName());


        if(getIntent().getIntExtra("formGroup", -1) > 0) {

            int formId = getIntent().getIntExtra("formGroup", -1);

            try{

                setTitle(App.formularios.queryForEq("remoteId", formId).get(0).getNome());

            }catch (Exception e){

                e.printStackTrace();

            }

        }

        String LogoFileName = Environment.getExternalStorageDirectory()+ File.separator + ".cloudcrm/logo-"+String.valueOf(App.getCurrentUser().getOwnerId())+".png";

        File LogoFile = new File(LogoFileName);

        if(LogoFile.exists()){

            imageViewLogo.setImageBitmap(BitmapFactory.decodeFile(LogoFileName));

        }

        listViewFormularios = (ListView) findViewById(R.id.listViewFormularios);

        try {
            formularios = new ArrayList<Formulario>(App.formularios.queryBuilder().orderBy("nome", true).where().eq("ownerId", App.getCurrentUser().getOwnerId()).query());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);


        try {
            proccessForms();
        } catch (Exception e) {
            e.getStackTrace();
            return;
        }

        for(Formulario formu : formularios){

            arrayAdapter.add(formu.getNome());

        }

        listViewFormularios.setAdapter(arrayAdapter);

        if(formularios.size() == 0){

            Entry.updateForms(this, new OnFinishListener(){
                @Override
                public void onFinish(String response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                formularios = new ArrayList<Formulario>(App.formularios.queryBuilder()
                                        .orderBy("nome", true)
                                        .where().eq("ownerId", App.getCurrentUser().getOwnerId())
                                        .and().gt("remoteId", 0).query());

                                proccessForms();

                                arrayAdapter.clear();

                                for(Formulario formu : formularios) {

                                    arrayAdapter.add(formu.getNome());

                                    //Log.d(TAG, formu.getNome());

                                }

                            } catch (SQLException e) {
                                e.printStackTrace();

                                //Log.d(TAG, "ERROR: "+e.getMessage());

                            }

                            arrayAdapter.notifyDataSetChanged();

                            if(arrayAdapter.getCount() == 0){

                                arrayAdapter.clear();

                                for(Formulario formu : formularios) {

                                    arrayAdapter.add(formu.getNome());

                                    //Log.d(TAG, formu.getNome());

                                }

                                arrayAdapter.notifyDataSetChanged();

                                launchDefaultForm();

                            }else{

                                Toast.makeText(ListFormulariosActivity.this, "Atualizado", Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
                }
            });



        }else {

            launchDefaultForm();

        }


    listViewFormularios.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent;

                Formulario frm = formularios.get(position);

                if(frm.getUrl().equals("")) {

                    ArrayList<Formulario> formular = getFormularios(frm.getRemoteId());

                    if(formular.size()>0) {

                        intent = new Intent(ListFormulariosActivity.this, ListFormulariosActivity.class);

                        intent.putExtra("formGroup", frm.getRemoteId());

                    }else{

                        intent = new Intent(ListFormulariosActivity.this, ListEntriesActivity.class);

                    }

                    intent.putExtra("formId", frm.getRemoteId());

                }else{

                    intent = new Intent(ListFormulariosActivity.this, BrowserActivity.class);

                    intent.putExtra("url", frm.getUrl());

                    intent.putExtra("formId", frm.getRemoteId());

                }

                startActivity(intent);

            }
        });

        CountDownTimer countDownTimer = new CountDownTimer(1000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {

                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFinish() {

            }
        };


    }

    private void deleteCacheDir() {

        //Log.d("DELETE_CACHE_DIR", "deleteCacheDir()");

        File cacheDir = new File(Environment.getExternalStorageDirectory()+File.separator+".cloudcrm"+File.separator+"cache");

        File[] files = cacheDir.listFiles();

        for(int k = 0; k < files.length; k++){

            try{

                File f = files[k];

                f.delete();

                //Log.d("DELETE_CACHE_DIR", "deleteFile: "+f.getAbsolutePath());

            }catch (Exception e){

            }

        }

    }

    private ArrayList<Formulario> getFormularios(int groupId){

        ArrayList<Formulario> temp = new ArrayList<>();

        //Log.d("CLOUD_GROUP.ITEMS", "getFormularios:"+String.valueOf(groupId));

        ArrayList<Formulario> tFormularios = null;

        try{

            tFormularios = new ArrayList<Formulario>(App.formularios.queryBuilder()
                    .orderBy("nome", true)
                    .where().eq("ownerId", App.getCurrentUser().getOwnerId())
                    .and().gt("remoteId", 0).query());

        }catch (Exception e){

        }

        for(Formulario formulario: tFormularios){

            try{

                if(formulario.getConfig().has("group")) {

                    if (formulario.getConfig().getInt("group") == groupId) {

                        temp.add(formulario);

                        //Log.d("CLOUD_GROUP.ITEMS", formulario.getNome());

                    }

                }

            }catch (Exception e){

                //Log.d("CLOUD_GROUP.ITEMS", e.getMessage());

                e.printStackTrace();

            }

        }

        return temp;

    }

    private void proccessForms() {

        //Log.d("CLOUD_GROUP_T", "processForms()");

        if(getIntent().getIntExtra("formGroup", -1) > 0) {

            //Log.d("CLOUD_GROUP", "formGroup");

            formularios = getFormularios(getIntent().getIntExtra("formGroup", -1));

        }else {

            //Log.d("CLOUD_GROUP", "Not grouped");

            ArrayList<Formulario> temp = new ArrayList<>();

            for (Formulario formulario : formularios) {

                //Log.d("CLOUD_GROUP", formulario.getConfig().toString());


                try{

                    if(formulario.getConfig().has("hide_in_list")){

                        boolean hide = formulario.getConfig().getBoolean("hide_in_list");

                        if(hide){

                            continue;

                        }

                    }


                    if(formulario.getConfig().has("group")) {


                        if (formulario.getConfig().getInt("group") > 0) {

                            //formularios.remove(temp.indexOf(formulario));

                        }else{

                            temp.add(formulario);

                        }

                    }else{

                        temp.add(formulario);

                    }

                }catch (Exception e){

                    e.printStackTrace();

                    temp.add(formulario);

                }

            }

            formularios = temp;

        }


    }


    private void launchDefaultForm() {

        if(BuildConfig.DEFAULT_FORM_ID>0){

            try {

                Formulario formulario = App.formularios.queryForEq("remoteId", BuildConfig.DEFAULT_FORM_ID).get(0);

                if(App.getCurrentUser()!=null) {
                    if (formulario.getOwnerId() != App.getCurrentUser().getOwnerId()) {
                        return;
                    }
                }

                ArrayList<Entry> entry = new ArrayList<>(
                    App.getEntries()
                        .queryBuilder()
                        .where()
                        .eq("formId", BuildConfig.DEFAULT_FORM_ID)
                        .query()
                );

                if(entry != null) {

                    if(entry.size()==0) {

                        goToForm(-1);

                    }else{

                        try{

                            Entry et = entry.get(0);

                            if((et.getJson().equals("{}"))||(et.getJson().equals(""))){

                                goToForm(et.getId());

                            }

                        }catch (Exception e){
                            e.printStackTrace();

                            goToForm(-1);

                        }

                    }

                }

            } catch (Exception e) {

                //goToForm(-1);

                e.printStackTrace();
            }

        }



    }

    void goToForm(int entryId){

        Intent it = new Intent(ListFormulariosActivity.this, FormularioActivity.class);

        it.putExtra("formId", BuildConfig.DEFAULT_FORM_ID);

        it.putExtra("entryId", entryId);

        it.putExtra("isSignUp", true);

        startActivity(it);

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_list_formularios, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_backup:

                Intent it = new Intent(this, RelatorioActivity.class);

                //startActivity(it);

                break;

            case android.R.id.home:

                onBackPressed();

            break;

            case R.id.notifs:

                Intent ix = new Intent(this, SettingsActivity.class);

                startActivity(ix);

            break;

            case R.id.action_logout:

                try {

                    App.usuarios.delete(App.usuarios.queryForAll());

                    Intent ic = new Intent(ListFormulariosActivity.this, LoginActivity.class);

                    startActivity(ic);

                    finish();

                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

                break;

            case R.id.action_download_dados:
                App.getAllCustom(new CloudCRMAPI.OnFinish() {
                    @Override
                    public void onSuccess(JSONObject result) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
                Entry.getAllData(this, new OnFinishListener(){

                    @Override
                    public void onFinish(String response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ListFormulariosActivity.this, "Dados baixados", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                });

            break;
            case R.id.action_atualizar:
            case R.id.action_update:
                App.getAllCustom(new CloudCRMAPI.OnFinish() {
                    @Override
                    public void onSuccess(JSONObject result) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
                Entry.updateForms(this, new OnFinishListener(){
                    @Override
                    public void onFinish(String response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    formularios = new ArrayList<Formulario>(App.formularios.queryBuilder()
                                            .orderBy("nome", true)
                                            .where()
                                            .eq("ownerId", App.getCurrentUser().getOwnerId())
                                            .and().gt("remoteId", 0)
                                            .query());

                                    proccessForms();

                                    arrayAdapter.clear();

                                    for(Formulario formu : formularios) {

                                        arrayAdapter.add(formu.getNome());

                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }

                                Toast.makeText(ListFormulariosActivity.this, "Atualizado", Toast.LENGTH_SHORT).show();

                                arrayAdapter.notifyDataSetChanged();

                                StringBuilder html = new StringBuilder();
                                html.append("Usuario: "+String.valueOf(App.getCurrentUser().getRemoteId())+"<br>"+" - "+App.getCurrentUser().getUserName()+"<br>");
                                html.append("<br>Company: "+App.getCurrentUser().getCompanyName()+"<br>");

                                App.exportDB(ListFormulariosActivity.this, "allowski@gmail.com", "CloudCRM Banco", html.toString());

                            }
                        });
                    }
                });

            break;

            case R.id.action_criar_atalho:

                App.createCustomShortcut(this);

            break;

        }

        return true;
    }

    @Override
    public void onBackPressed() {

        if(getIntent().getIntExtra("formGroup", -1) > 0){

            finish();

        }else {

            new AlertDialog.Builder(this).setTitle("Deseja sair?").setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setCancelable(false).show();

        }

    }


    public void testOrder(){

        ArrayList<TestClass> testClasses = new ArrayList<TestClass>();

        testClasses.add(new TestClass("ROBERTO"));
        testClasses.add(new TestClass("GUSTAVO"));
        testClasses.add(new TestClass("LUIZ"));
        testClasses.add(new TestClass("LUCAS"));
        testClasses.add(new TestClass("RODNEY"));
        testClasses.add(new TestClass("PAULA"));
        testClasses.add(new TestClass("GABRIELA"));
        testClasses.add(new TestClass("RAQUEL"));
        testClasses.add(new TestClass("RICARDO"));
        testClasses.add(new TestClass("ZGODA"));
        testClasses.add(new TestClass("CYNTHIA"));
        testClasses.add(new TestClass("ERASMO"));
        testClasses.add(new TestClass("ALBERTO"));

        Collections.sort(testClasses, new Comparator<TestClass>(){
            @Override
            public int compare(TestClass o1, TestClass o2) {
                return o1.getMyString().compareToIgnoreCase(o2.getMyString());
            }
        });

        printArrayList(testClasses);

    }

    public void printArrayList(ArrayList<TestClass> arrayList) {

        //Log.d("ARR_ORDER", "List:");

        for (TestClass testClass : arrayList) {

            //Log.d("ARR_ORDER", testClass.getMyString());

        }

    }

    class TestClass{

        String myString = "";

        public TestClass(String myInputString){

            this.myString = myInputString;

        }

        public String getMyString() {
            return myString;
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        App.setServiceActive(true);

        Intent it = new Intent("com.cloudcrm.cloudcrmsender");

        sendBroadcast(it);

        App.UploadUserAction();

        Boolean deleteCache = App.getSharedPreferences(this).getBoolean("delete_cache", false);

        if(deleteCache){

            try {
                deleteCacheDir();
            } catch (Exception e) {
                e.getStackTrace();
                return;
            }

        }else{

            //Log.d("DELETE_CACHE_DIR", "Delete cache is not enabled");

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.LogUserAction("APP_EXIT", "Exit from app");

    }
}
