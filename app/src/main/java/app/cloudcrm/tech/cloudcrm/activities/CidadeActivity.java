package app.cloudcrm.tech.cloudcrm.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;

import com.loopj.android.airbrake.AirbrakeNotifier;

import java.util.ArrayList;
import java.util.List;

import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.CidadesDatabase;
import app.cloudcrm.tech.cloudcrm.models.Cidade;

public class CidadeActivity extends AppCompatActivity {

    ArrayAdapter<String> arrayAdapter = null;

    ListView listView;

    EditText editText;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        setContentView(R.layout.activity_cidade);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1){
            @NonNull
            @Override
            public Filter getFilter() {
                return super.getFilter();
            }

        };

        listView = (ListView) findViewById(R.id.listView);

        editText = (EditText) findViewById(R.id.search);


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                arrayAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        try{

            String []cidades = getResources().getStringArray(R.array.cidades_br);

            for(String cidade: cidades){

                arrayAdapter.add(cidade);

            }

        }catch (Exception e){

            //Log.d("DB2_LOG", e.getMessage());

        }

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent bundle = new Intent();

                bundle.putExtra("value", arrayAdapter.getItem(i));

                bundle.putExtra("field", getIntent().getStringExtra("field"));

                setResult(RESULT_OK, bundle);

                finish();
            }
        });

    }


    class MyAdapter extends ArrayAdapter<String>{

        public MyAdapter(@NonNull Context context, @LayoutRes int resource) {

            super(context, resource);

        }

        @NonNull
        @Override
        public Filter getFilter() {
            return super.getFilter();
        }

        class ItemFilter extends Filter{

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            }
        }
    }
}
