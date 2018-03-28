package app.cloudcrm.tech.cloudcrm.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.loopj.android.airbrake.AirbrakeNotifier;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.cloudcrm.tech.cloudcrm.BuildConfig;
import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RelatorioActivity extends AppCompatActivity {

    StringBuilder HTML;

    WebView webView;

    Button fechar;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        fechar.callOnClick();

        return true;

    }

    @Override
    public void onBackPressed() {

        fechar.callOnClick();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        setContentView(R.layout.activity_relatorio);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webView = (WebView) findViewById(R.id.webView);

        fechar = (Button) findViewById(R.id.fechar);

        App.exportDB(this, "allowski@gmail.com", "AutoBackup: "+String.valueOf(App.getCurrentUser().getRemoteId()), "Backup concluido");

        fechar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(RelatorioActivity.this)
                        .setTitle("Cancelar Backup")
                        .setMessage("Deseja interromper o backup?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                dialogInterface.dismiss();

                            }
                        }).create().show();

            }
        });

        File dbFile = App.getDatabaseFile();

        HTML = new StringBuilder();

        setTitle("Backup");

        HTML.append("<!doctype html><html><head><meta charset='UTF8'></head><body>");

        HTML.append("<h1>Backup geral</h1>" +
                "<p>Estamos copiando os seus dados para um lugar seguro. Aguarde uns instantes, n&atilde;o desconecte seu celular.</p>" +
                "<table style='width:100%;'>");

        String db = App.getDbHelper().getDatabaseName();

        float size = (float)dbFile.length()/(1024f*1024f);

        HTML.append("<tr><th>USUARIO: </th><td>"+App.getCurrentUser().getUserName()+"</td></tr>");
        HTML.append("<tr><th>LOGIN: </th><td>"+App.getCurrentUser().getEmail()+"</td></tr>");
        HTML.append("<tr><th>EMPRESA: </th><td>"+App.getCurrentUser().getCompanyName()+"</td></tr>");
        HTML.append("<tr><th>ID USUARIO: </th><td>"+String.valueOf(App.getCurrentUser().getRemoteId())+"</td></tr>");
        HTML.append("<tr><th>NOME BANCO: </th><td>"+db+"</td></tr>");

        HTML.append("<tr><th>TAMANHO: </th><td>"+String.valueOf(size)+" MB</td></tr>");

        HTML.append("</table>");

        HTML.append("<table style='width:100%;'>");

        HTML.append("<tr><th>Formulario</th><th>Qtde</th><th>Enviado</th></tr>");

        try{

            List<String[]> resultado = App.getEntries().queryRaw("select formularios.nome, count(entries.id), sum(entries.sent) from entries " +
                    " inner join formularios on formularios.remoteId = entries.formId GROUP BY formularios.nome").getResults();

            for(String[] formulario: resultado){

                HTML.append("<tr>");
                HTML.append("<td>"+formulario[0]+"</td>");
                HTML.append("<td>"+formulario[1]+"</td>");
                HTML.append("<td>"+formulario[2]+"</td>");
                HTML.append("</tr>");

            }

            HTML.append("<tr><th colspan='3' style='color:red;'>Aguarde...</th></tr>");

            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .writeTimeout(1000, TimeUnit.MINUTES)
                    .readTimeout(1000, TimeUnit.MINUTES)
                    .build();

            JSONObject jsonObjectData = new JSONObject();

            try {
                jsonObjectData.put("api_key", BuildConfig.API_KEY);
                jsonObjectData.put("token", App.getCurrentUser().getUserToken());
                jsonObjectData.put("relatorio", HTML.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("json_data", "json_data",
                            RequestBody.create(MediaType.parse("application/json"), jsonObjectData.toString()))
                    .addFormDataPart("file.db", "file.db",
                            RequestBody.create(MediaType.parse("text/csv"), dbFile)).build();

            final Request request = new Request.Builder()
                    .url(BuildConfig.SERVER_URL+"action=relatorio")
                    .post(requestBody)
                    .build();

            new Thread(){
                @Override
                public void run() {

                    try{

                        Response response = okHttpClient.newCall(request).execute();

                        String resp = response.body().string();

                        JSONObject respo = new JSONObject(resp);

                        HTML.append(respo.getString("log"));

                        HTML.append("</table><h1>Backup concluido!</h1></body></html>");


                        //Log.d("RELATORIO", respo.getString("log"));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                webView.loadData(HTML.toString(), "text/html", "UTF-8");

                                SharedPreferences sharedPreferences = getSharedPreferences("startup", 0);

                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                editor.putBoolean("init", true);

                                editor.apply();

                                editor.commit();

                                fechar.setText("Prosseguir");

                                fechar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        finish();
                                    }
                                });

                            }
                        });

                    }catch (Exception e){

                        e.printStackTrace();

                    }

                }
            }.start();




        }catch (Exception e){

            e.printStackTrace();

            //Log.d("RELATORIO", e.getMessage());

        }

        webView.loadData(HTML.toString(), "text/html", "UTF-8");

    }
}
