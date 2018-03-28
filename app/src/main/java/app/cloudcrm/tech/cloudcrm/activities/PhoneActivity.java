package app.cloudcrm.tech.cloudcrm.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.loopj.android.airbrake.AirbrakeNotifier;

import java.util.ArrayList;
import java.util.regex.Pattern;

import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMActivity;

public class PhoneActivity extends CloudCRMActivity implements View.OnClickListener {

    int []buttons = new int[]{
        R.id.b0,
        R.id.b1,
        R.id.b2,
        R.id.b3,
        R.id.b4,
        R.id.b5,
        R.id.b6,
        R.id.b7,
        R.id.b8,
        R.id.b9,
        R.id.bb,
        R.id.bok,
    };

    String value = "";

    TextView textView;

    Spinner spinner;

    String area = "55";

    ArrayAdapter<String> arrayAdapter;

    ArrayList<Country> countryArrayList;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        setContentView(R.layout.activity_phone);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textView = (TextView) findViewById(R.id.editText);

        spinner = (Spinner) findViewById(R.id.spinner);

        String tempValue = getIntent().getStringExtra("value");

        //Log.d("PHONE_FMT", "Param: "+tempValue);

        if(tempValue.length()>0){
            if(tempValue.length()>3){

                try {

                    String[] vals = tempValue.split("\\|");

                    if(vals.length == 1){

                        //Log.d("PHONE_FMT", "Default 55");

                        area = "55";

                    }else {

                        area = vals[0];
                        value = vals[1];

                        //Log.d("PHONE_FMTS", "Area:"+area+", Value:"+value);

                    }

                }catch (Exception e){

                }
            }
        }

        value = unformat(value);

        textView.setText(format(area+"|"+value));

        setTitle(getIntent().getStringExtra("title"));

        for(int k = 0; k < buttons.length; k++){
            Button b = (Button) findViewById(buttons[k]);
            b.setOnClickListener(this);
        }

        countryArrayList = new ArrayList<>();

        countryArrayList.add(new Country(55, "Brasil"));
        countryArrayList.add(new Country(54, "Argentina"));
        countryArrayList.add(new Country(595, "Paraguay"));
        countryArrayList.add(new Country(591, "Bolivia"));

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        int i = 0;

        //Log.d("PHONE_FMTArea", area);


        spinner.setAdapter(arrayAdapter);

        for(Country country: countryArrayList){
            arrayAdapter.add("(+"+String.valueOf(country.code)+") "+country.getCountryName());
            if(String.valueOf(country.code).equals(area)){
                //Log.d("PHONE_FMTX", "Teste:"+area+" -> "+String.valueOf(country.code)+" -> "+String.valueOf(i));
                spinner.setSelection(i);
                area = String.valueOf(country.getCode());
                textView.setText(format(area+"|"+value));
            }
            i++;
        }

        arrayAdapter.notifyDataSetChanged();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                area = String.valueOf(countryArrayList.get(i).getCode());
                 //Log.d("PHONE_FMTX", area+" -> "+String.valueOf(i));
                textView.setText(format(area+"|"+value));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.bok:

                if(!isValid()){

                    new AlertDialog.Builder(this)
                            .setTitle("Numero nao valido")
                            .setMessage("O numero digitado nao e validado. Digite o numero com o DDD")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create().show();

                }else{

                    Intent result = new Intent();

                    result.putExtra("field", getIntent().getStringExtra("field"));

                    result.putExtra("value", area+"|"+value);

                    setResult(RESULT_OK, result);

                    finish();

                }

            break;
            case R.id.bb:

                try{
                    value = value.substring(0, value.length()-1);
                }catch (Exception e){
                    textView.setText("");
                }

                textView.setText(format(area+"|"+value));

            break;
            default:

                if(value.length()<11) {

                    value = value + ((Button) view).getText().toString();

                    textView.setText(format(area+"|"+value));

                }

            break;
        }

        if(isValid()){
            textView.setTextColor(Color.parseColor("#009909"));
        }else{
            textView.setTextColor(Color.parseColor("#990000"));
            if(value.length()==0){
                textView.setText("+XX (XX) XXXX-XXXX");
                textView.setTextColor(Color.parseColor("#999999"));
            }
        }

    }

    boolean isValid(){
        if(value.length()==0){
            textView.setText("+XX (XX) XXXX-XXXX");
            textView.setTextColor(Color.parseColor("#999999"));
        }
        if(value.length()==11 || value.length() == 10){
            return true;
        }
        return false;
    }

    public static String format(String val){

        //Log.d("PHONE_FMT", "Original: "+val);

        String area = "";

        StringBuilder sb = new StringBuilder();

        try{

            String []fmt = val.split("\\|");

            //Log.d("PHONE_FMT", "Length:"+String.valueOf(fmt.length));

            area = fmt[0];

            val = fmt[1];

            for(String str: fmt){
                //Log.d("PHONE_FMTT", str);
            }

            //Log.d("PHONE_FMT", "Area:"+area);
            //Log.d("PHONE_FMT", "Nmer:"+val);

        }catch (Exception e){

        }

        sb.append("+");

        sb.append(area);

        sb.append(" ");

        for(int i = 0; i < val.length(); i++){

            if(i == 0){
                sb.append("(");
            }else if(i == 2){
                sb.append(") ");
            }else if(i == 6 && val.length() > 6 && val.length() < 11){
                sb.append("-");
            }else if(i == 7 && val.length() == 11){
                sb.append("-");
            }else if(val.length()>11){

            }

            sb.append(val.charAt(i));

        }

        return sb.toString();

    }

    String unformat(String val){
        return val.replace("(", "").replace(")", "").replace(".", "").replace(" ", "").replace("-", "");
    }

    class Country{
        int code;
        String countryName;

        public Country(int code, String countryName) {
            this.code = code;
            this.countryName = countryName;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }
    }



}
