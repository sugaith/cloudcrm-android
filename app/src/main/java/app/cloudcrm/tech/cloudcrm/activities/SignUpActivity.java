package app.cloudcrm.tech.cloudcrm.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.airbrake.AirbrakeNotifier;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.TimeUnit;

import app.cloudcrm.tech.cloudcrm.BuildConfig;
import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.misc.Validador;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {

    EditText editTextName;

    EditText editTextEmail;

    EditText editTextDocumento;

    EditText editTextPassword;

    Button buttonCreateAccount;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getString(R.string.sign_up_form_title));

        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage(getString(R.string.sign_up_progress));

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextDocumento = (EditText) findViewById(R.id.editTextDocumento);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonCreateAccount = (Button) findViewById(R.id.buttonCreateAccount);

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!validateForm()){
                    return;
                }
                progressDialog.show();

                JSONObject jsonObjectRequest = new JSONObject();

                try {

                    jsonObjectRequest.put("owner", BuildConfig.COMPANY_ID);

                    jsonObjectRequest.put("name", editTextName.getText().toString());
                    jsonObjectRequest.put("email", editTextEmail.getText().toString());
                    jsonObjectRequest.put("documento", editTextDocumento.getText().toString());
                    jsonObjectRequest.put("password", editTextPassword.getText().toString());

                    signUp(jsonObjectRequest);

                } catch (JSONException e) {

                    e.printStackTrace();

                    progressDialog.dismiss();

                }

            }
        });

    }

    public void signUp(final JSONObject signUpObject){


        new Thread(){

            @Override
            public void run() {
                super.run();

                String mURL = BuildConfig.SERVER_URL +
                        "action=sign"
                        ;

                try {
                    signUpObject.put("api_key", BuildConfig.API_KEY);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*
                try {
                    //Log.d("OkHttp.REQ", signUpObject.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                */
                //Log.d("OkHttp.URL", mURL);

                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .readTimeout(1200, TimeUnit.SECONDS)
                        .writeTimeout(1200, TimeUnit.SECONDS)
                        .build()
                        ;

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("json_data", "json_data",
                                RequestBody.create(MediaType.parse("application/json"), signUpObject.toString())).build();

                Request request = new Request.Builder()
                        .url(mURL)
                        .post(requestBody)
                        .build();

                try {

                    final Response response = okHttpClient.newCall(request).execute();

                    final String result = response.body().string();

                    //Log.d("RESPONSE.ENT", result);

                    //Log.d("HTTP_SING", result);

                    final JSONObject responseObject = new JSONObject(result);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();

                            try {
                                if(responseObject.getInt("id") > 0){

                                    Intent it = new Intent();

                                    it.putExtra("login", signUpObject.getString("email"));
                                    it.putExtra("password", signUpObject.getString("password"));

                                    setResult(RESULT_OK, it);

                                    finish();

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                } catch (Exception e) {


                    e.printStackTrace();

                    //Log.d("OkHttp.E", e.getMessage());

                }

            }

        }.start();
    }

    public boolean validateForm(){

        boolean isValid = false;

        if(editTextName.getText().toString().length() < 5){

            editTextName.setError(getString(R.string.please_name));

            editTextName.requestFocus();

            return false;

        }

        if(!Validador.isValidEmail(editTextEmail.getText().toString())){

            editTextEmail.setError(getString(R.string.please_email));

            editTextEmail.requestFocus();

            return false;

        }

        if(editTextDocumento.getText().toString().length() < 5){

            editTextDocumento.setError(getString(R.string.please_number));

            editTextDocumento.requestFocus();

            return false;

        }

        if(editTextPassword.getText().toString().length() < 5){

            editTextPassword.setError(getString(R.string.please_password));

            editTextPassword.requestFocus();

            return false;

        }

        return true;

    }

}
