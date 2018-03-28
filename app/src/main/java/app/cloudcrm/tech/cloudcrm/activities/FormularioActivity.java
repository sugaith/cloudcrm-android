package app.cloudcrm.tech.cloudcrm.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItem;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.airbrake.AirbrakeNotifier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.cloudcrm.tech.cloudcrm.BuildConfig;
import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMAPI;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMActivity;
import app.cloudcrm.tech.cloudcrm.classes.OnFinishListener;
import app.cloudcrm.tech.cloudcrm.models.Formulario;
import app.cloudcrm.tech.cloudcrm.forms.FormularioAdapter;
import app.cloudcrm.tech.cloudcrm.misc.Validador;
import app.cloudcrm.tech.cloudcrm.models.Entry;
import app.cloudcrm.tech.cloudcrm.forms.Campo;

public class FormularioActivity extends CloudCRMActivity {

    public static int TYPE_FLOAT_RESULT = 9999;

    public static int TYPE_PAYMENT_RESULT = 9595;

    public static int TYPE_CUSTOM_RESULT = 9422;

    public static int TYPE_POLYGON_RESULT = 9878;

    TextView textViewVersion;

    ListView listViewFormulario;

    GridView gridViewFormulario;

    ArrayList<Formulario> formularioArrayList;

    Formulario form;

    int entryId = 1;

    int formId;

    FormularioAdapter adapter;

    Entry myEntry;

    long timeStart;

    long timeEnd;

    long timeElapsed;

    EditText editText;

    boolean is_signup = false;

    static boolean skipAsk = false;

    boolean finishOnSave = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_formulario, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Log.d("button_cliqued",String.valueOf(item.getItemId()));

        switch (item.getItemId()){

            case android.R.id.home:
                //onBackPressed();
                notSaveNewBack();
                finish();
            break;

            case R.id.saveFormulario:

                save(true);


            break;

            case R.id.sendEmail:
                View view = getLayoutInflater().inflate(R.layout.alert_send_email, null);
                final EditText editTextEmail = (EditText) view.findViewById(R.id.editTextEmail);
                final EditText editTextNome = (EditText) view.findViewById(R.id.editTextNome);
                final Button buttonSendEmail = (Button) view.findViewById(R.id.buttonSendEmail);
                final Button buttonCancelEmail = (Button) view.findViewById(R.id.buttonCancelEmail);

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                timeEnd = System.currentTimeMillis();

                timeElapsed = timeEnd - timeStart;

                try {
                    form.getData().put("time_elapsed", timeElapsed);
                    form.getData().put("time_start", timeStart);
                    form.getData().put("time_end", timeEnd);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                editTextEmail.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {


                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        if(!Validador.isValidEmail(editTextEmail.getText().toString())){
                            editTextEmail.setError(getString(R.string.enter_valid_email));
                        }else{
                            editTextEmail.setError(null);
                        }

                    }
                });

                final Dialog dialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .show();

                dialog.setCancelable(false);

                buttonCancelEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                buttonSendEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String email = editTextEmail.getText().toString();
                        String nome = editTextNome.getText().toString();

                        //Log.d("FORMU.EM", email);

                        if(email.equals("")){

                            //editTextEmail.setError(getString(R.string.email_required));

                            editTextEmail.setError(getString(R.string.enter_valid_email));

                            return;

                        }

                        try {

                            JSONObject jsonObject = new JSONObject();

                            jsonObject.put("email", email);

                            jsonObject.put("nome", nome);

                            form.getData().put("send_to", jsonObject);

                            //Log.d("CLOUD_CRM_E", form.getData().toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Log.d("FORMU.EM", e.getMessage());
                        }

                        myEntry.setJson(form.getData().toString());

                        try{

                            //Log.d("FORMU.EM", "Update");

                            if(myEntry.getId() == 0){
                                App.getEntries().create(myEntry);
                            }

                            App.getEntries().update(myEntry);

                            //Log.d("FORMU.EM", "Send");

                            myEntry.send(new CloudCRMAPI.OnFinish() {
                                             @Override
                                             public void onSuccess(final JSONObject result) {

                                                 FormularioActivity.this.runOnUiThread(new Runnable() {
                                                     @Override
                                                     public void run() {

                                                         Toast.makeText(FormularioActivity.this, R.string.email_sent, Toast.LENGTH_SHORT).show();

                                                         try{
                                                             JSONObject j = new JSONObject(myEntry.getJson());
                                                             if(j.has("send_to")){
                                                                 j.remove("send_to");
                                                             }
                                                             myEntry.setJson(j.toString());
                                                             if(!result.isNull("id")) {
                                                                 myEntry.setRemoteId(result.getInt("id"));
                                                             }
                                                             App.getEntries().update(myEntry);
                                                         }catch (Exception e){
                                                             e.printStackTrace();
                                                         }

                                                     }
                                                 });

                                             }

                                             @Override
                                             public void onError(Exception e) {

                                             }
                                         });

                        }catch (Exception e){

                            e.printStackTrace();

                            //Log.d("FORMU.EM", e.toString());

                        }

                        Intent it = new Intent("com.cloudcrm.cloudcrmsender");

                        it.putExtra("entryId", entryId);

                        sendBroadcast(it);

                        dialog.dismiss();



                    }
                });

                editTextEmail.requestFocus();

                imm.showSoftInputFromInputMethod(editTextEmail.getWindowToken(), InputMethodManager.SHOW_FORCED);

                imm.showSoftInput(editTextEmail, InputMethodManager.SHOW_FORCED);

            break;
        }

        return true;
    }

    public void save(boolean exit) {

        try{

            if(form.getConfig().has("disable_edit")){

                if(form.getConfig().getBoolean("disable_edit")){

                    return;

                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        //Log.d(TAG, "Save called(exit="+String.valueOf(exit)+")");

        boolean isOk = true;

        if(!skipAsk)

            adapter.notifyDataSetChanged();

        String camps = "";

        try {

            for(Campo campo: form.campos){

                if(campo.isRequired()){

                    if(form.getData().has(campo.getId())){

                        if(form.getData().getString(campo.getId()).equals("")){

                            isOk = false;

                        }else{

                            // Do something

                        }

                    }else{

                        isOk = false;

                    }

                }

            }

            if((!isOk)&&(!skipAsk)){

                Dialog dialog = new AlertDialog.Builder(FormularioActivity.this)
                        .setTitle("Campos requeridos nao prenchidos")
                        .setMessage("Tem campos obrigatorios sem precher")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

                return;

            }

            timeEnd = System.currentTimeMillis();

            timeElapsed = timeEnd - timeStart;

            form.getData().put("time_elapsed", timeElapsed);
            form.getData().put("time_start", timeStart);
            form.getData().put("time_end", timeEnd);

            myEntry.setJson(form.getObject().toString());

            //Log.d("FormularioAct", form.getObject().toString());

            myEntry.setSent(0);

            //Log.d("CHECK_ENTRIES", "EntryId:"+String.valueOf(entryId));

            if (entryId == -1) {

                //Log.d("FORMS.CREATE", "Created");

                //Log.d("CHECK_ENTRIES", "New Form Id: "+String.valueOf(formId));

                myEntry.setFormId(formId);

                App.getEntries().create(myEntry);

                entryId = myEntry.getId();

                myEntry.setSent(0);

                //Log.d("FORMS.CREATE", "New id:"+String.valueOf(entryId));

            } else {

                //Log.d("CHECK_ENTRIES", "Updating");

                //Log.d("FORMS.UPDATE", "Updated");

                if(!skipAsk) {

                    myEntry.setSent(0);

                }else{

                    myEntry.setSent(0);

                }

                App.getEntries().update(myEntry);

            }


        }catch (Exception e){

            //Log.d("FORMS.EXCEPTION", e.getMessage());

        }

        if(!skipAsk) {

            if(exit) {

                //Log.d(TAG, "finishOnSave = true");

                finish();

            }

        }

        skipAsk = false;

        finishOnSave = false;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Log.d(TAG, "OnDestroy() called");

        //App.setServiceActive(true);

    }


    @Override
    protected void onPause() {

        super.onPause();

        //save(false);

    }

    @Override
    protected void onStop() {

        super.onStop();

        //save(false);
    }

    @Override
    public void finish() {

        super.finish();
        //save(false);
        //Log.d(TAG, "FINISH() called;");

    }

    protected void notSaveNewBack(){

        entryId = getIntent().getIntExtra("entryId", -1);
        if(entryId == -1) {
            try {
                App.getEntries().delete(myEntry);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        setContentView(R.layout.activity_formulario);

        App.setActionBarColor(this);

        timeStart = System.currentTimeMillis();

        App.setServiceActive(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listViewFormulario = (ListView) findViewById(R.id.listViewFormulario);

        gridViewFormulario = (GridView) findViewById(R.id.gridViewFormulario);

        gridViewFormulario.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        editText = (EditText) findViewById(R.id.editText);





        listViewFormulario.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //hide KB

                if(view == null){
                    //Log.d("FA_EXCEOTION", "abslistview = null");
                }

                //Log.d("FA_EXCEPTION", "onScrollStateChanged");

                try {

                    if (SCROLL_STATE_TOUCH_SCROLL == scrollState) {


                        //Log.d("FA_EXCEPTION", "requestFocus");
                        editText.requestFocus();

                        View v  = getCurrentFocus();

                        if(v == null){
                            //Log.d("FA_EXCEPTION", "v == null");
                            v = editText;
                        }

                        //Log.d("FA_EXCEPTION", "Create imm");
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        //Log.d("FA_EXCEPTION", "Hide KBD");
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        //Log.d("FA_EXCEPTION", "clearFocus();");
                        editText.clearFocus();

                    }

                }catch (Exception e){

                    e.printStackTrace();

                    //Log.d("FA_EXCEPTION", e.getMessage());

                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) { }
        });


        textViewVersion = (TextView) findViewById(R.id.textViewVersion);

        textViewVersion.setText(BuildConfig.VERSION_NAME);

        formId = getIntent().getIntExtra("formId", -1);

        if(formId == -1){

            //Log.d(TAG, "formId ======== -1 finish");

            finish();
        }

        entryId = getIntent().getIntExtra("entryId", -1);

        is_signup = getIntent().getBooleanExtra("isSignUp", false);

        if(entryId == -1){

            //Log.d("NEW_ENTRY", "Entry");

            myEntry = new Entry();

            myEntry.setJson("{}");

            myEntry.setStatus("A");

            myEntry.setUserId(App.getCurrentUser().getRemoteId());

            try {

                myEntry.setSent(0);

                myEntry.setFormId(formId);

                App.getEntries().create(myEntry);

                entryId = myEntry.getId();


            } catch (SQLException e) {

                e.printStackTrace();

                //Log.d("NEW_ENTRY", e.getMessage());

            }

        }else {

            try {

                myEntry = App.getEntries().queryForId(entryId);

            } catch (SQLException e) {

                e.printStackTrace();

                //Log.d("Formulario.Err", e.getMessage());

            }

        }


        Intent it = new Intent();

        it.putExtra("entryId", myEntry.getId());

        setResult(RESULT_OK, it);


        try {

            form = App.formularios.queryForEq("remoteId", formId).get(0);

            App.LogUserAction("ENTER_FORM", form.getNome()+" -> "+ (entryId ==-1 ? "create" : "update"));

            form.generateCamposFromJson();

            setTitle(form.getNome());

            form.setData(new JSONObject(myEntry.getJson()));

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        adapter = new FormularioAdapter(this, form);

        adapter.setMyEntry(myEntry);

        listViewFormulario.setAdapter(adapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        //Log.d("CLOUD_FORE", String.valueOf(requestCode));

        if((requestCode == TYPE_FLOAT_RESULT)&&(resultCode == RESULT_OK)) {

            String prop = data.getStringExtra("field");

            double value = data.getDoubleExtra("value", 0);
            try {
                adapter.formularioData.put(prop, value);

                saveNow();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if((requestCode == TYPE_PAYMENT_RESULT)&&(resultCode == RESULT_OK)) {
          String prop = data.getStringExtra("field");

          String value = data.getStringExtra("value");

          try {
              adapter.formularioData.put(prop, value);

              saveNow();

          } catch (JSONException e) {
              e.printStackTrace();
          }

      }else if((requestCode == TYPE_CUSTOM_RESULT)&&(resultCode == RESULT_OK)) {

        String prop = data.getStringExtra("field");
        String value = data.getStringExtra("value");

        try {
            adapter.formularioData.put(prop, value);
            saveNow();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }else if(requestCode == PickImageActivity.PICK_IMAGE){

            if(resultCode == RESULT_OK){

                try {
                    adapter.formularioData.put(data.getStringExtra(PickImageActivity.EXTRA_FIELD),
                            data.getStringExtra(PickImageActivity.EXTRA_FILENAME));
                    saveNow();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }else if(requestCode == 8999){

            if(resultCode == RESULT_OK){

                try {

                    adapter.formularioData.put(data.getStringExtra("field"), data.getIntExtra("entryId", 0));

                    saveNow();

                    //Log.d("CLOUD_FOREING", data.getStringExtra("field")+" -> "+String.valueOf(data.getIntExtra("entryId", 0)));

                }catch (Exception e){

                    e.printStackTrace();

                }

            }

        }else if(requestCode == TYPE_POLYGON_RESULT){

            if(resultCode == RESULT_OK) {

                String prop = data.getStringExtra("field");

                String value = data.getStringExtra("value");

                if (value.isEmpty()) {
                    value = "{}";
                }

                try {

                    adapter.formularioData.put(prop, value);

                    saveNow();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }else if(requestCode == 543){

            if(resultCode == RESULT_OK) {

                String prop = data.getStringExtra("field");

                String value = data.getStringExtra("value");

                if (value.isEmpty()) {
                    value = "";
                }

                try {

                    adapter.formularioData.put(prop, value);

                    saveNow();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

        adapter.notifyDataSetChanged();

    }


    @Override
    protected void onResume() {

        super.onResume();

        adapter.notifyDataSetChanged();

        editText.requestFocus();

    }

    public void saveNow(){

        //Log.d(TAG, "SaveNow()");

        //save(false);

    }

    @Override
    public void onBackPressed() {

        //Log.d(TAG, "onBackPressed ....");


        if(myEntry == null){
            //Log.d(TAG, "myEntry is null..");

            myEntry = new Entry();
            finish();
            return;
        }

        if(is_signup){

            findViewById(R.id.saveFormulario).callOnClick();

            return;

        }

        StringBuilder stringBuilder = new StringBuilder();

        try {

            boolean ok = true;

            if(form.getData().toString().equals("{}")){
                ok = false;
            }else {

                JSONArray names = form.getData().names();


                for (int k = 0; k < names.length(); k++) {

                    try {

                        String key = names.getString(k);

                        if(key.startsWith("time_")) continue;

                        if (form.getData().has(key)) {

                            stringBuilder.append(form.getData().getString(key));

                        }

                    }catch (Exception e){

                        e.printStackTrace();

                    }

                }

            }

            //Log.d("TESTEX", stringBuilder.toString());

            if(stringBuilder.toString().trim().equals("")){

                ok = false;

            }

            if(!ok){

                //Log.d(TAG, "Not Ok");

                App.getEntries().delete(myEntry);

                finish();

                App.setServiceActive(true);

                return;
            }

        }catch (Exception e){

            //Log.d(TAG, "Except"+e.toString());

            e.printStackTrace();

        }

        //Log.d("EXIT_FORM", form.getData().toString());
        try {
            //Log.d("EXIT_FORM", new JSONObject(myEntry.getJson()).toString());
        } catch (Exception e) {
            e.printStackTrace();
            //Log.d(TAG, "Exs: "+e.toString());
        }

        try {
            if(form.getData().toString().equals(new JSONObject(myEntry.getJson()).toString())){

                //Log.d(TAG, "Case 1");

                finish();

            }else{

                /*Dialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Atencao")
                        .setMessage("Deseja sair sem salvar as modificacoes?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();*/

                //Log.d(TAG, "Case 2");

                finish();

            }
        } catch (JSONException e) {
            e.printStackTrace();

            //Log.d(TAG, "Exception -> "+e.toString());

            finish();
        }


        App.setServiceActive(true);

    }

    private class CustomScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            // do nothing
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (SCROLL_STATE_TOUCH_SCROLL == scrollState) {
                View currentFocus = getCurrentFocus();
                if (currentFocus != null) {

                }
            }
        }

    }



}
