package app.cloudcrm.tech.cloudcrm.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.airbrake.AirbrakeNotifier;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import app.cloudcrm.tech.cloudcrm.BuildConfig;
import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMAPI;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMActivity;
import app.cloudcrm.tech.cloudcrm.models.Usuario;
import app.cloudcrm.tech.cloudcrm.misc.Settings;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends CloudCRMActivity {

    Button buttonLogin;

    Button buttonSignUp;

    EditText editTextLogin;

    EditText editTextPassword;

    TextView textViewVersion;

    JSONObject LoginResultObject;

    Boolean LoggedOrNot = false;

    ProgressDialog LoginProgressDialog;

    Intent openItentAfterLogin;

    Usuario user;

    boolean launchDefaultForm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        App.LogUserAction("LOGIN_FORM", "Enter Login form");

        try {
            user = App.usuarios.queryForAll().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_login);

        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonSignUp = (Button) findViewById(R.id.buttonSignUp);

        if(BuildConfig.ENABLE_SIGN_UP){
            buttonSignUp.setVisibility(View.VISIBLE);

            buttonSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivityForResult(it, 1);
                }
            });

        }else{
            buttonSignUp.setVisibility(View.INVISIBLE);
        }

        textViewVersion = (TextView) findViewById(R.id.textViewVersion);

        textViewVersion.setText(String.valueOf(BuildConfig.VERSION_NAME));

        createFolderIfNExist();

        if(user == null){

            //Log("No Login");

        }else {

            //Log("Is Logged, proceed");

            openItentAfterLogin = new Intent(LoginActivity.this, ListFormulariosActivity.class);

            startActivity(openItentAfterLogin);

            finish();

        }

        editTextLogin = (EditText) findViewById(R.id.login);

        editTextPassword = (EditText) findViewById(R.id.password);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoginProgressDialog = new ProgressDialog(LoginActivity.this);

                LoginProgressDialog.setMessage(getString(R.string.login_in));

                LoginProgressDialog.setCancelable(false);

                LoginProgressDialog.show();

                Login();

            }
        });


    }

    private void failedLogin() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                LoginProgressDialog.dismiss();

                AlertDialog builder = new AlertDialog.Builder(LoginActivity.this).setTitle(R.string.usuario_nao_valido)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });


    }

    private void createFolderIfNExist(){

        try {

            File mDir = new File(Environment.getExternalStorageDirectory()+
                    File.separator+".cloudcrm"+File.separator);

            boolean isCreated = mDir.mkdirs();

        }catch (Exception e){

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();

        }
    }

    private void Login(){

        try {

            JSONObject LoginPostFormData = new JSONObject();

            LoginPostFormData.put("password", editTextPassword.getText().toString());

            LoginPostFormData.put("email", editTextLogin.getText().toString());

            LoginPostFormData.put("api_key", BuildConfig.API_KEY);

            CloudCRMAPI cloudCRMAPI = new CloudCRMAPI("login", LoginPostFormData.toString());

            cloudCRMAPI.makeCall(new CloudCRMAPI.OnFinish() {
                @Override
                public void onSuccess(JSONObject result) {

                    processLogin(result);

                    App.LogUserAction("LOGIN_OK", "User Logged in");

                }

                @Override
                public void onError(Exception e) {

                    App.LogUserAction("LOGIN_ERR", "User error Login");

                    failedLogin();

                }
            });



        }catch (JSONException e){

            e.printStackTrace();

        }

    }

    public void processLogin(JSONObject loginResultObject){

        try{

            if (loginResultObject.has("logged_in")) {

                LoggedOrNot = loginResultObject.getBoolean("logged_in");

                if (LoggedOrNot) {

                    App.usuarios.delete(App.usuarios.queryForAll());

                    //Log("Logged in!");

                    openItentAfterLogin = new Intent(LoginActivity.this, ListFormulariosActivity.class);

                    //Log.d("AFTER_LOGIN", String.valueOf(launchDefaultForm));

                    final Usuario currentUser = new Usuario();

                    currentUser.setCompanyName(loginResultObject.getString("companyName"));
                    currentUser.setCompanyColor(loginResultObject.getString("companyColor"));
                    currentUser.setRemoteId(loginResultObject.getInt("id"));
                    currentUser.setUserName(loginResultObject.getString("name"));
                    currentUser.setUserToken(loginResultObject.getString("user_token"));
                    currentUser.setEmail(loginResultObject.getString("email"));
                    currentUser.setOwnerId(loginResultObject.getInt("ownerId"));
                    currentUser.setLogged(true);

                    String LogoFileName = Environment.getExternalStorageDirectory()+File.separator+".cloudcrm"+File.separator+"logo-"+String.valueOf(currentUser.getOwnerId())+".png";

                    File LogoFile = new File(LogoFileName);

                    LogoFile.delete();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            LoginProgressDialog.setMessage(getString(R.string.downloading_assets));

                        }
                    });

                    App.usuarios.create(currentUser);

                    new CloudCRMAPI("get_logo", "{}")
                        .saveTo(LogoFileName)
                        .makeCall(new CloudCRMAPI.OnFinish() {
                        @Override
                        public void onSuccess(JSONObject result) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(openItentAfterLogin);
                                    if(BuildConfig.COMPANY_ID<1) {
                                        App.createCustomShortcut(LoginActivity.this);
                                    }
                                    finish();
                                }
                            });

                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "Nao foi possivel obter a Logo", Toast.LENGTH_LONG).show();
                                    startActivity(openItentAfterLogin);
                                    finish();
                                }
                            });
                        }
                    });

                }else{

                    //Log("Not Logged in!");

                    failedLogin();

                }

            }else{

                //Log("Not Logged in (no Logged_in field in JSON)!");

                failedLogin();

            }

        }catch (Exception ex){

            LoggedOrNot = false;

            //Log.d("CCRM", ex.getMessage());

            failedLogin();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            String username = data.getStringExtra("login");

            String passwd = data.getStringExtra("password");

            editTextLogin.setText(username);

            editTextPassword.setText(passwd);

            launchDefaultForm = true;

            buttonLogin.callOnClick();

        }

    }
}
