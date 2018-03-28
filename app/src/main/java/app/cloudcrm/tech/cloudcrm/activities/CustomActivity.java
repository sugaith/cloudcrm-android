package app.cloudcrm.tech.cloudcrm.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.airbrake.AirbrakeNotifier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;

import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMAPI;
import app.cloudcrm.tech.cloudcrm.models.Custom;
import app.cloudcrm.tech.cloudcrm.models.Formulario;

/**
 * Created by gustavojunior on 26/05/17.
 */

public class CustomActivity extends AppCompatActivity {


    TextView label;

    Formulario formulario;

    public String dataSave;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_input_decimal_activity, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                //onBackPressed();
                this.salvar();
                break;

            case R.id.menuSalvar:
                //onBackPressed();
                this.salvar();
                break;

        }
        return true;
    }


    private void salvar(){

        //Log.d("lista_brack", "Name: " + dataSave);
        Intent it = new Intent();
        it.putExtra("field", getIntent().getStringExtra("field"));
        it.putExtra("value", dataSave);
        setResult(RESULT_OK, it);
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService( CONNECTIVITY_SERVICE );

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        App.setActionBarColor(this);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){
            e.printStackTrace();
            finish();
            return;
        }

        setContentView(R.layout.activity_custom);
        int formId = getIntent().getIntExtra("formId", 0);

        try {
            formulario = App.formularios.queryForEq("remoteId", formId).get(0);
            setTitle(formulario.getNome());

        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }


        if(getIntent().hasExtra("data")){
            dataSave = getIntent().getStringExtra("data");
            if(dataSave.contains("&#34;")) {
                dataSave = dataSave.replace("&#34;","\"");
            }

            //Log.d("lista_brack_contet", "Name: " + dataSave);
        }


        try {
            WebView webView = (WebView) findViewById(R.id.webView1);
            webView.setWebViewClient(new WebViewClient());
            webView.setWebChromeClient(new WebChromeClient() {
                public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                   /*
                    //Log.d("DEBUG_JS", message + " -- From line "
                            + lineNumber + " of "
                            + sourceID);

                            */
                }
            });
            webView.getSettings().setAppCacheMaxSize( 5 * 1024 * 1024 ); // 5MB
            webView.getSettings().setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );
            webView.getSettings().setAllowFileAccess( true );
            webView.getSettings().setAppCacheEnabled( true );
            webView.getSettings().setJavaScriptEnabled( true );
            webView.addJavascriptInterface(new CustomProxyJs(this,CustomActivity.this), "androidCustomProxyJs");
            webView.getSettings().setCacheMode( WebSettings.LOAD_DEFAULT );

            Integer id = getIntent().getIntExtra("CustomId",0);
            Custom custom = null;
            custom = App.custom.queryForEq("entryId",id).get(0);

            String getData = "";
                if(dataSave ==null){
                    dataSave = "";
                }
                getData = "<script>\n" +
                        "window.getValue = function(){ return androidCustomProxyJs.getValue(); }; \n" +
                        "window.setValue = function(val){ androidCustomProxyJs.setValue(val); }; \n" +
                        "window.closeWindow = function(){ return androidCustomProxyJs.closeActivity(); }; \n"+
                        "</script>\n";



            webView.loadDataWithBaseURL("", getData+custom.getData(), "text/html", "UTF-8", "");

        } catch (Exception e) {
            DownloadCustom();
            return;
        }


    }

    public void DownloadCustom(){
        App.getAllCustom(new CloudCRMAPI.OnFinish() {
            @Override
            public void onSuccess(JSONObject result) {
                    startActivity(getIntent());
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(CustomActivity.this)
                                .setTitle("ATENÇÃO").setMessage("Não foi possivel carregar o Custom. Tentar novamente?")
                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                        return;
                                    }
                                })
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        DownloadCustom();
                                    }
                                }).show();
                    }
                });
            }
        });
    }

    public class CustomProxyJs {

        private Activity activity = null;

        private CustomActivity custom = null;

        public CustomProxyJs(Activity activity,CustomActivity custom) {

            this.activity = activity;

            this.custom = custom;

        }


        @JavascriptInterface
        public void setValue(String data){

            dataSave = data;

        }

        @JavascriptInterface
        public String getValue(){
            return dataSave;
        }


        @JavascriptInterface
        public void closeActivity(){ custom.salvar(); }


    }


}
