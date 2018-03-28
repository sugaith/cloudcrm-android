package app.cloudcrm.tech.cloudcrm.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.loopj.android.airbrake.AirbrakeNotifier;
import java.sql.SQLException;

import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.classes.FloatEditText;
import app.cloudcrm.tech.cloudcrm.models.Formulario;

public class InputDecimalPaymentActivity extends AppCompatActivity {

    FloatEditText floatEditText = null;

    GridView gridViewKeyBoard;

    Button buttonSave;

    Button buttonBack;

    TextView label;

    ArrayAdapter<String> arrayAdapter;

    String[] keys;

    Formulario formulario;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_input_decimal_activity, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menuSalvar){

            buttonSave.callOnClick();

        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        setContentView(R.layout.activity_input_decimal);

        App.setActionBarColor(this);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        keys = getResources().getStringArray(R.array.keyboard);

        gridViewKeyBoard = (GridView) findViewById(R.id.gridView);

        floatEditText = (FloatEditText) findViewById(R.id.floatEditText);

        floatEditText.setMaxVal(1000000000000.0d);

        floatEditText.setDecimalPlaces(2);

        label = (TextView) findViewById(R.id.label);

        label.setText("Valor");

        buttonSave = (Button) findViewById(R.id.buttonSave);

        buttonSave.setText("Prosseguir");
        buttonBack = (Button) findViewById(R.id.buttonBack);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        arrayAdapter = new ArrayAdapter<String>(this, R.layout.keyboard_button_item, keys);

        gridViewKeyBoard.setNumColumns(3);

        gridViewKeyBoard.setAdapter(arrayAdapter);

        int formId = getIntent().getIntExtra("formId", 0);

        if(formId == 0){
            finish();
            return;
        }

        App.setActionBarColor(this);

        try {
            formulario = App.formularios.queryForEq("remoteId", formId).get(0);

            setTitle(formulario.getNome());

        } catch (SQLException e) {
            e.printStackTrace();
            finish();
            return;
        }



        floatEditText.setDecimalFormatString("##,##0.00");


        gridViewKeyBoard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(keys[position].equals("<")){
                    floatEditText.backspace();
                }else {
                    floatEditText.appendText(keys[position]);
                }
                floatEditText.requestFocus();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(floatEditText.getFloatValue() > 0){
                    Intent it = new Intent(InputDecimalPaymentActivity.this,PaymentActivity.class);

                    it.putExtra("formId", getIntent().getIntExtra("formId",0));
                    it.putExtra("label", getIntent().getStringExtra("label"));
                    it.putExtra("field",getIntent().getStringExtra("field"));
                    it.putExtra("nome",getIntent().getStringExtra("nome"));
                    it.putExtra("documento",getIntent().getStringExtra("documento"));
                    //it.putExtra("formIdForeing",getIntent().getStringExtra("formIdForeing"));
                    double valor = (double) Math.round(floatEditText.getFloatValue() * 100) / 100;
                    it.putExtra("valor",valor);

                    startActivityForResult(it, 10);

                }else{
                    new AlertDialog.Builder(InputDecimalPaymentActivity.this)
                            .setTitle("ATENÇÃO").setMessage(R.string.aviso_validacao).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
                }



            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==10){
             getIntent().putExtra("field", data.getStringExtra("field"));
            getIntent().putExtra("value", data.getStringExtra("value"));
            setResult(RESULT_OK, getIntent());
            finish();
        }
    }
}
