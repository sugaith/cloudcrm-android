package app.cloudcrm.tech.cloudcrm.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import app.cloudcrm.tech.cloudcrm.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMAPI;
import app.cloudcrm.tech.cloudcrm.models.Formulario;


import me.pagar.mposandroid.MposPaymentResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import me.pagar.mposandroid.Mpos;
import me.pagar.mposandroid.MposListener;
import me.pagar.mposandroid.EmvApplication;
import me.pagar.mposandroid.PaymentMethod;



/**
 * Created by gustavojunior on 08/05/17.
 */

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {

    private Button pay_button;
    public Integer total = 0;
    private Formulario formulario;
    private JSONArray payment;
    public SeekBar seekBar;
    private ProgressDialog dialog;
    public Spinner forma_pago;
    private String[] arraySpinner;
    public TextView valor_final;
    public TextView mycloudcoin;
    Button buttonBack;
    JSONObject loginResultObject;
    private Integer ac_response_code = 0;
    private String card_emv_response = null;
    private static String TAG = "BLUETOOTH_LOG";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Log.d("menu_click",item.toString());

        switch (item.getItemId()) {

            case android.R.id.home:

                onBackPressed();

                break;

        }

        //pay_button.performClick();
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.setActionBarColor(this);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        setContentView(R.layout.activity_payment);
        int formId = getIntent().getIntExtra("formId", 0);



        try {
            formulario = App.formularios.queryForEq("remoteId", formId).get(0);
            setTitle(formulario.getNome());

            payment = formulario.getPayment();

            //Log.d("TAXAS",payment.toString());

        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }


        buttonBack = (Button) findViewById(R.id.buttonBack);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        seekBar = (SeekBar)  findViewById(R.id.seekBar);
        final EditText valor = (EditText) findViewById(R.id.valor);

        valor.setText(String.valueOf(getIntent().getDoubleExtra("valor",0)));

        valor.setVisibility(View.GONE);
        //Log.d("valor_mostra",String.valueOf(getIntent().getDoubleExtra("valor",0)));

        //Log.d("string_mostra",getIntent().getStringExtra("idforeing"));
        this.arraySpinner = new String[] {
                "Crédito", "Débito"
        };
        forma_pago = (Spinner) findViewById(R.id.forma_pago);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, arraySpinner);
        forma_pago.setAdapter(adapter);


        valor_final = (TextView) findViewById(R.id.valor_final);
        mycloudcoin = (TextView) findViewById(R.id.mycloudcoin);

        Double aux_cloud = getIntent().getDoubleExtra("valor",0) / 1800;

        mycloudcoin.setText(String.valueOf(aux_cloud));
        pay_button = (Button) findViewById(R.id.buttonPay);

        pay_button.setOnClickListener(this);
        seekBar.setProgress(0);
        seekBar.setMax(11);
        final TextView textView5 = (TextView) findViewById(R.id.textView5);
        final TextView seekBarValue = (TextView) findViewById(R.id.seekbarvalue);

        forma_pago.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getSelectedItem() == "Débito") {
                    seekBar.setVisibility(View.GONE);
                    seekBarValue.setText("DÉBITO");
                    textView5.setVisibility(View.GONE);
                }else{

                    seekBarValue.setText("1x");
                    seekBar.setVisibility(View.VISIBLE);
                    textView5.setVisibility(View.VISIBLE);
                }
                seekBar.setProgress(0);
                valor.setText(valor.getText());

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        progress = progress + 1;
                        seekBarValue.setText(String.valueOf(progress) + 'x');


                        if (Double.parseDouble(valor.getText().toString()) > 0) {
                            valor.setText(valor.getText());
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
        });


        valor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    try {
                        JSONObject payment_object = payment.getJSONObject(0);
                        Double valor_campo = getValor(Double.parseDouble(valor.getText().toString()),seekBar,payment_object);
                        //Log.d("valor_calculado",valor_campo.toString());
                        int progresso = seekBar.getProgress() + 1;

                        //Double acrescimo =  (Double.parseDouble(valor.getText().toString()) / 0.892) - Double.parseDouble(valor.getText().toString());
                        //acrescimo = acrescimo * (progresso - 1);
                        Double y = 0.00;
                        //if(progresso > 0) {
                            //y = Math.round((valor_campo + acrescimo) * 100.0) / 100.0;
                        //}else {
                        y = Math.round(valor_campo * 100.0) / 100.0;

                        //}
                        //Log.d("log_y",y.toString());
                        Double aux = y * 100;
                        total = aux.intValue();




                        //Log.d("log_aux",total.toString());


                        if(progresso > 0 && forma_pago.getSelectedItem() == "Crédito")
                            y = (y / progresso);


                        DecimalFormat valor_final_mostra = new DecimalFormat("##,##0.00");
                        valor_final_mostra.setRoundingMode(RoundingMode.DOWN);
                        valor_final.setText(valor_final_mostra.format(y));
                    } catch (Exception e) {

                        //Log.d("error_log", e.toString());
                       e.getStackTrace();
                    }
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });

    }

    public Double getValor(Double val, SeekBar seekBar, JSONObject paym) throws JSONException {
        Double Result = 0.00;

        int progresso = seekBar.getProgress() + 1;

        if (forma_pago.getSelectedItem() == "Débito") {
            Result = val / Double.parseDouble(paym.getJSONObject("taxas").getString("taxa1"));
        }else if(forma_pago.getSelectedItem() == "Crédito") {

            if (progresso == 1) {
                Result = val / Double.parseDouble(paym.getJSONObject("taxas").getString("taxa2"));
            } else if (progresso > 1 && progresso <= 6) {
                Result = val / Double.parseDouble(paym.getJSONObject("taxas").getString("taxa3"));
            } else if (progresso > 6 && progresso <= 12) {
                Result = val / Double.parseDouble(paym.getJSONObject("taxas").getString("taxa4"));
            }

            if(progresso > 1) {

                Double centagem = (1 - (0.018 * progresso));
                Result = Result / centagem;

            }
        }
        return Result;
    }

    public void pay(View view) throws Exception {

        dialog = ProgressDialog.show(this, "",
                "Conectando ao MPÓS...", true);

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        BluetoothDevice[] devices = adapter.getBondedDevices().toArray(new BluetoothDevice[0]);

        BluetoothDevice device = null;

        for(int i = 0; i<devices.length; i++){
            BluetoothDevice dev = null;
            dev = devices[i];
            if(dev.getName().startsWith("PAX-")){
                device = dev;
            }
        }

        if(device == null) {
            dialog.hide();
            throw new Exception("bluetooth nao esta ligado",null);
        }


        JSONObject payment_object = payment.getJSONObject(0);
        String ek = payment_object.getString("key");
            final Mpos mpos = new Mpos(device, ek, this);


            mpos.addListener(new MposListener() {

                @Override
                public void bluetoothConnected() {
                    // Inicializar operações no pinpad

                    mpos.initialize();
                    //Log.d(TAG,"CONNECTED");
                    showMessage(R.string.BluetoothConnected);

                }
                @Override
                public void bluetoothDisconnected() {
                    //Log.d(TAG,"BLUETOOTH DISCONNECTED");
                    showMessage(R.string.BluetoothDisconnected);
                    hideMessage();

                }

                @Override
                public void receiveInitialization() {
                    //Log.d("Abecs", "receive initialization!");
                    mpos.displayText(getString(R.string.wait));
                    try {
                        mpos.downloadEMVTablesToDevice(true);
                    } catch (Exception e) {
                        //Log.d("Abecs", "Got error in initialization and table update " + e.getMessage());
                    }
                }



                @Override
                public void receiveClose() {
                    showMessage(R.string.BluetoothDisconnected);
                    //Log.d(TAG,"FECHOU CONEXAO ");
                    mpos.closeConnection();

                }

                @Override
                public void receiveNotification(String notification) {
                    //Log.d(TAG,"RECEBEU NOTIFICAÇÃO");
                    showMessage(notification);
                }

                @Override
                public void receiveOperationCompleted() {
                    //Log.d(TAG,"OPERACAO COMPLETADA");
                }

                @Override
                public void receiveOperationCancelled() {

                    //Log.d(TAG,"OPERCAO CANCELADA");
                    mpos.displayText(getString(R.string.OperationCancelled));
                    showMessage(R.string.OperationCancelled);
                    mpos.closeConnection();
                }



                public void errorInternet(String text){
                    showDialog(null,text);
                    mpos.cancelOperation();
                    mpos.close(getString(R.string.BluetoothDisconnected));

                }

                @Override
                public void receiveCardHash(String cardHash, MposPaymentResult mposPaymentResult) {
                    mpos.displayText(getString(R.string.wait));
                    //Log.d("Abecs", "Card Hash is " + cardHash);
                    //Log.d("Abecs", "Card Brand is " + mposPaymentResult.cardBrand);
                    //Log.d("Abecs", "FD = " + mposPaymentResult.cardFirstDigits + " LD = " + mposPaymentResult.cardLastDigits);
                    //Log.d("Abecs", "ONL = " + mposPaymentResult.isOnline);

                    // Gerar transação com a API Pagar.me...

                    JSONObject transactionPostFormData = new JSONObject();

                    try {
                        transactionPostFormData.put("valor", total);
                        transactionPostFormData.put("valor_limpo", getIntent().getDoubleExtra("valor",0));
                        transactionPostFormData.put("parcelas", (seekBar.getProgress() + 1));
                        transactionPostFormData.put("api_key", BuildConfig.API_KEY);
                        transactionPostFormData.put("cardHash", cardHash);
                        transactionPostFormData.put("nome", getIntent().getStringExtra("nome"));
                        transactionPostFormData.put("documento", getIntent().getStringExtra("documento"));

                        //transactionPostFormData.put("formIdForeing", getIntent().getStringExtra("formIdForeing"));
                        //Log.d("DATA_PAYMENT",transactionPostFormData.toString());
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                    CloudCRMAPI cloudCRMAPI = new CloudCRMAPI("transaction", transactionPostFormData.toString());
                    cloudCRMAPI.makeCall(new CloudCRMAPI.OnFinish() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            loginResultObject = result;
                            //Log.d("DATA_PAYMENT",loginResultObject.toString());

                            if(loginResultObject.has("acquirer_response_code")){
                                try {
                                    ac_response_code = loginResultObject.getInt("acquirer_response_code");
                                    card_emv_response = loginResultObject.getString("card_emv_response");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if(ac_response_code > 0){
                                    mpos.finishTransaction(true, ac_response_code, card_emv_response);
                                }else {
                                    mpos.finishTransaction(false, 0, null);
                                }
                            }else{

                                //Log.d("DATA_PAYMENT",result.toString());
                                //mpos.cancelOperation();
                                try {
                                    errorInternet(loginResultObject.getString("error"));
                                } catch (JSONException t) {
                                    errorInternet(getString(R.string.aviso_internet));
                                }
                            }
                        }
                        @Override
                        public void onError(Exception e) {
                            //Log.d("Ops!", e.toString());
                            errorInternet(getString(R.string.aviso_internet));
                        }
                    });

                }
                @Override
                public void receiveTableUpdated(boolean loaded) {
                    //Log.d("Abecs", "received table updated loaded = " + loaded);

                    Integer metodo = 0;

                    if(forma_pago.getSelectedItem() == "Débito") {
                        metodo = PaymentMethod.DebitCard;
                    }else{
                        metodo = PaymentMethod.CreditCard;
                    }

                    EmvApplication visa = new EmvApplication(metodo, "visa");
                    ArrayList<EmvApplication> l = new ArrayList<EmvApplication>();
                    l.add(visa);
                    EmvApplication master = new EmvApplication(metodo, "mastercard");
                    l.add(master);
                    mpos.payAmount(total, l, metodo);
                }

                @Override
                public void receiveFinishTransaction() {
                    hideMessage();
                    //Log.d(TAG,"transação finalizada");
                    try {
                        if(loginResultObject.getString("status").contains("paid")) {
                            mpos.close(getString(R.string.paymentAproved) + loginResultObject.getString("id"));
                            JSONObject dados = new JSONObject();
                            dados.put("valor",loginResultObject.getString("valor"));
                            dados.put("parcelas",loginResultObject.getString("parcelas"));
                            dados.put("authorization_code",loginResultObject.getString("authorization_code"));
                            dados.put("status",loginResultObject.getString("status"));
                            dados.put("id",loginResultObject.getString("id"));
                            dados.put("v2",valor_final.getText());
                            dados.put("valor_limpo",getIntent().getDoubleExtra("valor",0));
                            salvar(dados);
                        }else{
                            mpos.close(getString(R.string.paymentRecused) + loginResultObject.getString("id"));
                            showDialog(null,getString(R.string.paymentRecused) + " - " + getString(R.string.recusado_aviso));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        errorInternet(getString(R.string.Error));
                    }
                }



                @Override
                public void bluetoothErrored(int error) {
                    //Log.d(TAG,"ocorreu um error no bluetooth "+error);
                    hideMessage();
                    //Log.d(getString(R.string.BluetoothDisconnected),"MPÓS CODIGO : "+error);
                }

                @Override
                public void receiveError(int error) {
                    //Log.d(TAG,"error no pinpad "+error);
                    hideMessage();
                    showDialog(getString(R.string.OperationCancelled),"MPÓS CODIGO : "+error);
                    mpos.closeConnection();
                }




            });

            mpos.openConnection();
    }

    public void salvar(final JSONObject tr){
        final Intent it = new Intent();
        it.putExtra("field", getIntent().getStringExtra("field"));
        it.putExtra("value", tr.toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run(){
                try {
                    new AlertDialog.Builder(PaymentActivity.this)
                            .setTitle("ATENÇÃO").setMessage(getString(R.string.paymentAproved) +  tr.getString("id")).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setResult(RESULT_OK, it);
                            finish();
                        }
                    }).show();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });


    }

    public void showMessage(int resId, Object... args)
    {
        showMessage(getString(resId), args);
    }

    public void showMessage(String message, Object... args)
    {
        String formattedMessage = String.format(message, args);
        showMessage(formattedMessage);
    }

    public void hideMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
    }

    public void showDialog(final String title, final String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(PaymentActivity.this)
                        .setTitle(title).setMessage(text).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });
    }







    public void showMessage(int resId)
    {
        showMessage(getString(resId));
    }

    public void showMessage(String message)
    {
        uiShowMessage(message);
    }

    private void uiShowMessage(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.setMessage(message);
            }
        });
    }




    @Override
    public void onClick(View view) {

        if(total > 0){

            try {
                pay(view);
            }catch (Exception e){
                new AlertDialog.Builder(this)
                        .setTitle("MPÓS BLUETOOTH").setMessage(R.string.MPOS_BLUETOOTH).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
                e.printStackTrace();
            }

        }else{
            new AlertDialog.Builder(this)
                    .setTitle("ATENÇÃO").setMessage(R.string.aviso_validacao).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
        }
    }
}
