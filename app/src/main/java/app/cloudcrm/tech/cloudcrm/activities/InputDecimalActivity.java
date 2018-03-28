package app.cloudcrm.tech.cloudcrm.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.loopj.android.airbrake.AirbrakeNotifier;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.DecimalFormat;

import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.classes.FloatEditText;
import app.cloudcrm.tech.cloudcrm.classes.NumberFormaterManager;
import app.cloudcrm.tech.cloudcrm.models.Formulario;

public class InputDecimalActivity extends AppCompatActivity {

    FloatEditText floatEditText = null;

    GridView gridViewKeyBoard;

    Button buttonSave;

    Button buttonBack;

    TextView label;

    ArrayAdapter<String> arrayAdapter;

    String[] keys;

    Formulario formulario;

    static FloatEditText.Formater formater = new FloatEditText.Formater(){

        @Override
        public String format(double value) {

            NumberFormaterManager numberFormaterManager = new NumberFormaterManager();

            numberFormaterManager.addFormat("(0###) ###-###");
            numberFormaterManager.addFormat("(0##) ###-###");

            DecimalFormat decimalFormat = new DecimalFormat("##################");

            return numberFormaterManager.format(decimalFormat.format(value));

        }
    };


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

        floatEditText.setDecimalPlaces(0);

        label = (TextView) findViewById(R.id.label);

        label.setText(getIntent().getStringExtra("label"));

        buttonSave = (Button) findViewById(R.id.buttonSave);

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

        JSONObject config = formulario.getConfig();

        //Log.d("FORM_CONFIG", "Form: "+String.valueOf(formulario.getRemoteId())+":"+config.toString());

        try{

            if(config.has("fields")){

                JSONObject o = config.getJSONObject("fields");

                String f = getIntent().getStringExtra("field");

                if(o.has(f)){

                    JSONObject jp = o.getJSONObject(f);

                    if(jp.has("decimalFormat")){

                        floatEditText.setDecimalFormatString(jp.getString("decimalFormat"));

                    }

                    //Log.d("FORMAT", jp.getString("decimalFormat"));

                    if(jp.getString("decimalFormat").equals("phone")) {

                        //Log.d("FORMAT", "Phone formater");

                        floatEditText.setFormater(FloatEditText.phoneFormater);

                    }

                    if(jp.has("decimalPlaces")){

                        floatEditText.setDecimalPlaces(jp.getInt("decimalPlaces"));

                    }

                    if(jp.has("maxVal")){

                        floatEditText.setMaxVal(jp.getDouble("maxVal"));

                    }

                }

            }else{
                //---

            }

            //floatEditText.setFormater(formater);

        }catch (Exception e){

            e.printStackTrace();

        }

        floatEditText.setFloatValue(getIntent().getDoubleExtra("value", 0));


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

                Intent it = new Intent();

                it.putExtra("field", getIntent().getStringExtra("field"));

                it.putExtra("value", floatEditText.getFloatValue());

                setResult(RESULT_OK, it);

                finish();



            }
        });

    }
}
