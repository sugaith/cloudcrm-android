package app.cloudcrm.tech.cloudcrm.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.airbrake.AirbrakeNotifier;

import java.sql.SQLException;

import app.cloudcrm.tech.cloudcrm.BuildConfig;
import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.models.Formulario;

public class BrowserActivity extends AppCompatActivity {

    WebView webView;

    ProgressBar progressBar;

    TextView textViewVersion;

    String url;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_browser, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){

            onBackPressed();

        }else if(item.getItemId() == R.id.action_refresh){

            //webView.clearCache(true);

            webView.reload();

            progressBar.setVisibility(View.VISIBLE);

            progressBar.setProgress(0);

        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        setContentView(R.layout.activity_browser);

        url = getIntent().getStringExtra("url");

        if(!url.startsWith("http")){
                url = App.getPublicUrl() + url + "&nomenu=1&__token=" + App.getUserToken();
        }

        App.LogUserAction("BROWSE", url);

        //Log.d("BROWSER_ACTIVITY", url);

        App.setActionBarColor(this);

        textViewVersion = (TextView) findViewById(R.id.textViewVersion);

        textViewVersion.setText(BuildConfig.VERSION_NAME);

        int formId = getIntent().getIntExtra("formId", 0);
        if(formId == 0){
            finish();
            return;
        }

        try {
            Formulario formulario = App.formularios.queryForEq("remoteId", formId).get(0);

            setTitle(formulario.getNome());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webView = (WebView) findViewById(R.id.webView);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        webView.setWebViewClient(new WebViewClient(){
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {

                //Log.d("SSL_ERROR", error.toString());

                Dialog alert = new AlertDialog.Builder(BrowserActivity.this)
                    .setTitle("SSL Error")
                        .setMessage("There was an error while verifying the SSL Certificate. Do you want to proceed?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.proceed();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).create();

                alert.show();

                // Ignore SSL certificate errors
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                progressBar.setVisibility(View.VISIBLE);

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

                view.setVisibility(View.GONE);

            }
        });

        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                progressBar.setProgress(newProgress*2);

            }
        });

        webView.getSettings().setJavaScriptEnabled(true);

        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        String appCachePath = this.getCacheDir().getAbsolutePath();

        webView.getSettings().setAppCachePath(appCachePath);

        webView.getSettings().setAppCacheEnabled(false);

        webView.getSettings().setAllowFileAccess(false);

        webView.getSettings().setDomStorageEnabled(false);

        //Log.d("BROWSER_ACT", url);

        webView.loadUrl(url);

    }

    @Override
    public void onBackPressed() {

        if(webView.canGoBack()){
            webView.goBack();
        }else{
            finish();
        }

    }
}
