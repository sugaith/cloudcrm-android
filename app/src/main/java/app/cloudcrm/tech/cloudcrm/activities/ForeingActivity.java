package app.cloudcrm.tech.cloudcrm.activities;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.loopj.android.airbrake.AirbrakeNotifier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import app.cloudcrm.tech.cloudcrm.BuildConfig;
import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMAPI;
import app.cloudcrm.tech.cloudcrm.classes.ImagePicker;
import app.cloudcrm.tech.cloudcrm.models.Foreing;
import app.cloudcrm.tech.cloudcrm.models.Formulario;

import static android.view.View.GONE;

/**
 * Created by gustavojunior on 26/05/17.
 */

public class ForeingActivity extends AppCompatActivity {

    ListView listView;

    ArrayList<Foreing> listEntries = new ArrayList<Foreing>();;

    AdapterEntries adapterEntries;

    TextView textViewVersion;
    EditText editTextSearchBox;
    TextView textViewNoResults;
    ProgressBar loading;
    FloatingActionButton floatingActionButton;

    JSONObject foreingLoad;

    int formId;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_lista_entries, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_new_entry:

                break;

        }
        return true;
    }


    private void salvar(){
        /*
        //Log.d("lista_brack", "Name: " + dataSave);
        Intent it = new Intent();
        it.putExtra("field", getIntent().getStringExtra("field"));
        it.putExtra("value", dataSave);
        setResult(RESULT_OK, it);
        finish();
        */
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
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }


        if(!isNetworkAvailable() ){
            new AlertDialog.Builder(this)
                    .setTitle("SEM INTERNET").setMessage("É necessario estar conectado na internet para utilizar esta opção").setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    finish();
                    return;
                }
            }).show();
        }

        setContentView(R.layout.activity_list_entries);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fabNewEntry);
        editTextSearchBox = (EditText) findViewById(R.id.editTextSearchBox);
        textViewVersion = (TextView) findViewById(R.id.textViewVersion);
        textViewNoResults = (TextView) findViewById(R.id.textViewNoResults);
        loading = (ProgressBar) findViewById(R.id.progressBar);


        TextView adicionar = (TextView) findViewById(R.id.qmap_2);
        adicionar.setVisibility(GONE);
        floatingActionButton.setVisibility(GONE);
        textViewVersion.setVisibility(GONE);
        editTextSearchBox.setVisibility(View.VISIBLE);
        editTextSearchBox.setHint("Buscando Por...");
        editTextSearchBox.requestFocus();

        formId = getIntent().getIntExtra("formId", 0);
        foreingLoad = new JSONObject();

        try {
            foreingLoad.put("api_key", BuildConfig.API_KEY);
            foreingLoad.put("formId", this.formId);
            //Log.d("DATA_PAYMENT", foreingLoad.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapterEntries = new AdapterEntries(this, listEntries);
        listView = (ListView) findViewById(R.id.listViewEntries);
        listView.setAdapter(adapterEntries);




        editTextSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    foreingLoad.put("busca", s.toString());
                } catch (Exception e) {
                    e.getStackTrace();
                    return;
                }
                listEntries.clear();
                if(s.length() % 2 == 0) {
                    textViewNoResults.setVisibility(GONE);
                    loading.setVisibility(View.VISIBLE);
                    CloudCRMAPI cloudCRMAPI = new CloudCRMAPI("foreingload", foreingLoad.toString());
                    cloudCRMAPI.makeCall(new CloudCRMAPI.OnFinish() {
                        @Override
                        public void onSuccess(final JSONObject result) {

                            try {

                                JSONArray resultado = result.getJSONArray("resultado");
                                listEntries.clear();

                                for (int i = 0; i < resultado.length(); i++) {
                                    Foreing foreings = new Foreing();
                                    foreings.setForeingId(resultado.getJSONObject(i).getInt("id"));
                                    foreings.setFormId(formId);
                                    foreings.setNome(resultado.getJSONObject(i).getString("nome"));

                                    listEntries.add(foreings);
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapterEntries.notifyDataSetChanged();
                                        loading.setVisibility(GONE);

                                        if(result.length() == 0){
                                            textViewNoResults.setVisibility(View.VISIBLE);
                                        }

                                    }
                                });


                            } catch (Exception e) {
                                e.getStackTrace();
                            }


                        }


                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
                adapterEntries.notifyDataSetChanged();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Intent intent = new Intent();

                Intent it = new Intent(ForeingActivity.this, FormularioActivity.class);

                it.putExtra("formId", formId);

                Intent intentReturn = new Intent();

                try {

                    it.putExtra("entryId", listEntries.get(position).getForeingId());

                    intentReturn.putExtra("entryId", listEntries.get(position).getForeingId());

                    //Log.d("entryID_foreing", String.valueOf(listEntries.get(position).getForeingId()));

                    List<Foreing> foreing = App.foreing.queryBuilder()
                            .where()
                            .eq("formId", listEntries.get(position).getFormId())
                            .and()
                            .eq("foreingId", listEntries.get(position).getForeingId())
                            .and()
                            .eq("nome", listEntries.get(position).getNome()).query();


                    if(foreing.isEmpty() == true){
                        App.foreing.create(listEntries.get(position));
                    }

                }catch (Exception e){

                    e.printStackTrace();

                    return;

                }

                intentReturn.putExtra("field", getIntent().getStringExtra("field"));

                //Log.d("passou",  getIntent().getStringExtra("field"));

                setResult(RESULT_OK, intentReturn);

                finish();

                return;


                //startActivityForResult(it, 124);

            }
        });
    }


    class AdapterEntries extends ArrayAdapter<Foreing>{

        public AdapterEntries(Context context, ArrayList<Foreing> foreings) {
            super(context, R.layout.entry_layout, foreings);
        }

        @Override
        public int getCount() {
            return listEntries.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = new ViewHolder();

            if (convertView == null) {

                convertView = getLayoutInflater().inflate(R.layout.entry_layout, parent, false);

                viewHolder.title = (TextView) convertView.findViewById(R.id.titleTextView);

                viewHolder.subtitle = (TextView) convertView.findViewById(R.id.subtitleTextView);

                viewHolder.subtitle.setVisibility(GONE);

                viewHolder.radioButton = (RadioButton) convertView.findViewById(R.id.radioButton);

                viewHolder.radioButton.setVisibility(GONE);

                //viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);

                //viewHolder.imageView.setVisibility(GONE);

                viewHolder.status = (ImageView) convertView.findViewById(R.id.status);

                viewHolder.status.setVisibility(GONE);


                convertView.setTag(viewHolder);

            } else {

                viewHolder = (ViewHolder) convertView.getTag();

            }

            try {

                Foreing foreing = getItem(position);



                viewHolder.title.setText(foreing.getNome().toUpperCase());

                //viewHolder.subtitle.setText(String.valueOf(foreing.getId()));

            } catch (Exception e) {

                e.printStackTrace();

            }

            return convertView;
        }

        class ViewHolder{

            TextView title;

            TextView subtitle;

            RadioButton radioButton;

            ImageView imageView;

            ImageView status;

        }
    }



}

