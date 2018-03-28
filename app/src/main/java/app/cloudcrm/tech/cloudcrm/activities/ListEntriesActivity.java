package app.cloudcrm.tech.cloudcrm.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.loopj.android.airbrake.AirbrakeNotifier;

import junit.framework.Test;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.cloudcrm.tech.cloudcrm.BuildConfig;
import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMAPI;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMActivity;
import app.cloudcrm.tech.cloudcrm.classes.ImagePicker;
import app.cloudcrm.tech.cloudcrm.forms.Campo;
import app.cloudcrm.tech.cloudcrm.forms.FormularioAdapter;
import app.cloudcrm.tech.cloudcrm.models.Foreing;
import app.cloudcrm.tech.cloudcrm.models.Formulario;
import app.cloudcrm.tech.cloudcrm.models.Entry;

import static android.view.View.GONE;

public class ListEntriesActivity extends CloudCRMActivity {

    ListView listView;

    TextView textViewVersion;

    EditText editTextSearchBox;

    TextView textViewNoResults;

    AdapterEntries adapterEntries;

    FloatingActionButton floatingActionButton;

    TextView qmap_2;

    ArrayList<Entry> entries;
    public Boolean allowScrollLoad = true;
    public static Integer DEFAULT_OFFSET = 0;
    public static Integer DEFAULT_LIMIT  = 300;

    ArrayList<ListEntry> listEntries;
    ArrayList<ListEntry> listEntriesOriginal;

    Formulario formulario;

    Entry delEntry;

    int formId;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_lista_entries, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case android.R.id.home:
                //Log.d("voltou_log","voltou");
                onBackPressed();

                break;

            case R.id.action_new_entry:

                /*Intent it = new Intent(ListEntriesActivity.this, FormularioActivity.class);
                it.putExtra("formId", formId);
                it.putExtra("entryId", -1);
                startActivity(it);*/

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                if(editTextSearchBox.getVisibility() == View.VISIBLE) {
                    imm.hideSoftInputFromInputMethod(editTextSearchBox.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(editTextSearchBox.getWindowToken(), 0);
                    editTextSearchBox.setVisibility(GONE);
                    getEntries(DEFAULT_OFFSET,DEFAULT_LIMIT);
                    updateEntries(entries,true);
                    allowScrollLoad = true;
                }else{
                    editTextSearchBox.setVisibility(View.VISIBLE);
                    editTextSearchBox.requestFocus();
                    imm.showSoftInput(editTextSearchBox, InputMethodManager.SHOW_IMPLICIT);
                    getEntries(DEFAULT_OFFSET,Integer.parseInt(String.valueOf(TotalEntries())));
                    updateEntries(entries,true);
                    allowScrollLoad = false;
                }



                break;

        }

        return true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        setContentView(R.layout.activity_list_entries);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fabNewEntry);

        editTextSearchBox = (EditText) findViewById(R.id.editTextSearchBox);

        textViewVersion = (TextView) findViewById(R.id.textViewVersion);

        qmap_2 = (TextView) findViewById(R.id.qmap_2);

        textViewVersion.setText(BuildConfig.VERSION_NAME);

        textViewNoResults = (TextView) findViewById(R.id.textViewNoResults);
        TextView adicionar = (TextView) findViewById(R.id.qmap_2);
        editTextSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {


                ArrayList<ListEntry> tempEntries = new ArrayList<ListEntry>();

                listEntries.clear();

                tempEntries.clear();

                for(ListEntry entry : listEntriesOriginal){

                    if(entry.getSearch().toUpperCase().contains(s.toString().toUpperCase())){

                        tempEntries.add(entry);

                    }

                }

                if(tempEntries.size()==0){

                    textViewNoResults.setVisibility(View.VISIBLE);

                }else{

                    textViewNoResults.setVisibility(GONE);

                }

                listEntries = tempEntries;

                adapterEntries.notifyDataSetChanged();

            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                App.LogUserAction("PLUS_BUTTON", "Create new entry to form: "+String.valueOf(formId));

                Intent it = new Intent(ListEntriesActivity.this, FormularioActivity.class);

                it.putExtra("formId", formId);

                //Log.d("CHECK_ENTRIES", "passFormId: "+String.valueOf(formId));

                it.putExtra("entryId", -1);

                startActivityForResult(it, 111);

            }
        });

        if(getIntent().getBooleanExtra("chooser", false)){
            App.setActionBarColorHex(this, "#666666");
            floatingActionButton.setVisibility(GONE);
            adicionar.setVisibility(GONE);
            qmap_2.setVisibility(GONE);
        }else{
            App.setActionBarColor(this);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        formId = getIntent().getIntExtra("formId", -1);

        listEntries = new ArrayList<ListEntry>();

        listEntriesOriginal = new ArrayList<ListEntry>();

        adapterEntries = new AdapterEntries(this);

        if(formId == -1){

            finish();

        }

        listView = (ListView) findViewById(R.id.listViewEntries);

        listView.setAdapter(adapterEntries);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                delEntry = listEntries.get(position).getEntry();

                try{

                    if(formulario.getConfig().getBoolean("disable_edit")){

                        return false;

                    }

                }catch (Exception e){

                }


                try {

                    delEntry = App.getEntries().queryForId(delEntry.getId());

                }catch (Exception e){
                    e.printStackTrace();
                }

                new AlertDialog.Builder(ListEntriesActivity.this)
                        .setTitle("Delete entry")
                        .setMessage("Do you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {


                                    delEntry.setStatus(Entry.STATUS_DELETED);

                                    delEntry.setSent(0);

                                    JSONObject jsonObjectE = new JSONObject(delEntry.getJson());

                                    jsonObjectE.put("status", Entry.STATUS_DELETED);

                                    delEntry.setJson(jsonObjectE.toString());

                                    App.getEntries().update(delEntry);

                                    onResume();

                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).create().show();

                return true;
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int preLast;
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }
            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                switch(absListView.getId()){
                    case R.id.listViewEntries:
                        final int lastItem = i + i1;
                        if(lastItem == i2){
                            if(preLast!=lastItem){
                                if(listView.getAdapter().getCount() != TotalEntries() && allowScrollLoad == true){
                                    getEntries(listView.getAdapter().getCount(), DEFAULT_LIMIT);
                                    updateEntries(entries,false);
                                }
                                //Log.d("Last", "Last");
                                preLast = lastItem;
                            }
                        }
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Intent intent = new Intent();

                if(editTextSearchBox.getVisibility() == View.VISIBLE) {

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromInputMethod(editTextSearchBox.getWindowToken(), 0);

                    imm.hideSoftInputFromWindow(editTextSearchBox.getWindowToken(), 0);

                    editTextSearchBox.setVisibility(GONE);

                }

                Intent it = new Intent(ListEntriesActivity.this, FormularioActivity.class);

                it.putExtra("formId", formId);

                Intent intentReturn = new Intent();

                try {

                    it.putExtra("entryId", listEntries.get(position).getEntry().getId());

                    if(listEntries.get(position).getEntry().getRemoteId() == 0) {

                        int remoteId = listEntries.get(position).getEntry().getRemoteId();

                        Entry e = App.getEntries().queryForId(listEntries.get(position).getEntry().getId());

                        intentReturn.putExtra("entryId", e.getRemoteId());

                    }else{

                        intentReturn.putExtra("entryId", listEntries.get(position).getEntry().getRemoteId());

                    }

                }catch (Exception e){

                    e.printStackTrace();

                    return;

                }

                if(getIntent().getBooleanExtra("chooser", false)){

                    intentReturn.putExtra("field", getIntent().getStringExtra("field"));

                    setResult(RESULT_OK, intentReturn);

                    finish();

                    return;

                }

                startActivityForResult(it, 124);

            }
        });


    }
    protected long TotalEntries(){
        try{
            return App.getEntries().queryBuilder().where()
                    .eq("formId",  formId)
                    .and()
                    .eq("status", Entry.STATUS_NORMAL)
                    .and()
                    .eq("userId", App.getCurrentUser().getRemoteId()).countOf();
        }catch (SQLException e){
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        listEntries.clear();

        editTextSearchBox.setText("");

        if(!getIntent().getBooleanExtra("chooser", false)) {

            App.setServiceActive(true);

            //Log.d("TESTING", "serviceWillBeActived()");

        }

        try{


            //Log.d("TESTIN.F", "FORM:"+String.valueOf(formId));

            formulario = App.formularios.queryForEq("remoteId", formId).get(0);

            App.LogUserAction("LIST_ENTRIES", formulario.getNome());

            if(getIntent().getBooleanExtra("chooser", false)){

                setTitle(getString(R.string.select_option));

                floatingActionButton.setVisibility(GONE);

                qmap_2.setVisibility(GONE);

            } else {

                setTitle(formulario.getNome());

                try {

                    if (formulario.getConfig().has("disable_add")){
                        if(formulario.getConfig().getBoolean("disable_add")){
                            floatingActionButton.setVisibility(GONE);
                            qmap_2.setVisibility(GONE);
                        }
                    }

                }catch (Exception er){

                }

            }

            getEntries(DEFAULT_OFFSET,DEFAULT_LIMIT);
            updateEntries(entries,true);

        }catch (Exception e){

            //Log.d("FATAL_ERROR", e.getMessage());

            e.printStackTrace();

        }
    }

    class AdapterEntries extends ArrayAdapter<Entry>{

        public AdapterEntries(Context context) {
            super(context, R.layout.entry_layout);
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

                viewHolder.radioButton = (RadioButton) convertView.findViewById(R.id.radioButton);

                //viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);

                viewHolder.status = (ImageView) convertView.findViewById(R.id.status);

                if (getIntent().getBooleanExtra("chooser", false)) {

                    viewHolder.radioButton.setVisibility(View.VISIBLE);

                    //viewHolder.imageView.setVisibility(View.INVISIBLE);

                } else {

                    viewHolder.radioButton.setVisibility(View.INVISIBLE);

                    //viewHolder.imageView.setVisibility(View.INVISIBLE);

                }


                convertView.setTag(viewHolder);

            } else {

                viewHolder = (ViewHolder) convertView.getTag();

            }

            try {

                Collections.sort(listEntries, new Comparator<ListEntry>() {
                    @Override
                    public int compare(ListEntry a1, ListEntry a2) {
                        // String implements Comparable
                        return (a1.getTitle().toUpperCase()).compareTo(a2.getTitle().toUpperCase());
                    }
                });

                ListEntry entry = listEntries.get(position);

                viewHolder.title.setText(entry.getTitle().toUpperCase());

                if (entry.getSent() == 1) {

                    viewHolder.status.setImageBitmap(BitmapFactory.decodeResource(ListEntriesActivity.this.getResources(), R.drawable.ic_sent));

                } else {


                    viewHolder.status.setImageBitmap(BitmapFactory.decodeResource(ListEntriesActivity.this.getResources(), R.drawable.ic_not_sent));


                }

                final String fileName = Environment.getExternalStorageDirectory()+File.separator+".cloudcrm"+ File.separator+"cache"+File.separator+String.valueOf(formulario.getRemoteId())+"_picture_file1_"+String.valueOf(entry.getId())+".jpg";

                final ViewHolder v = viewHolder;

                File f = new File(fileName);
                /*
                if(f.exists()) {

                    new Thread() {

                        @Override
                        public void run() {

                            try {

                                Bitmap b = BitmapFactory.decodeFile(fileName);

                                final Bitmap x = ImagePicker.HDBitmap(b, 60);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        v.imageView.setImageBitmap(x);
                                    }
                                });

                            } catch (Exception e) {

                                e.printStackTrace();

                            }

                        }
                    }.start();

                }else{

                    //viewHolder.imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.cliente));

                }

                */

                viewHolder.subtitle.setText(entry.getSubtitle().toUpperCase());

                if (getIntent().getIntExtra("selected", 0) == entry.getRemoteId()) {

                    viewHolder.radioButton.setChecked(true);

                    viewHolder.title.setTextColor(getResources().getColor(R.color.colorPrimary));

                    //convertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));

                } else {

                    viewHolder.radioButton.setChecked(false);

                    viewHolder.title.setTextColor(getResources().getColor(android.R.color.black));

                    //convertView.setBackgroundColor(getResources().getColor(android.R.color.transparent, ListEntriesActivity.this.getTheme());

                }

            } catch (Exception e) {

                e.printStackTrace();

            }

            return convertView;
        }

        class ViewHolder{

            TextView title;

            TextView subtitle;

            RadioButton radioButton;

            //ImageView imageView;

            ImageView status;

        }
    }


    class ListEntry implements Comparable<ListEntry>{

        String title;

        String subtitle;

        String search;

        public int getSent() {
            return sent;
        }

        public void setSent(int sent) {
            this.sent = sent;
        }

        int sent = 0;

        public String getSearch() {
            return search;
        }

        public void setSearch(String search) {
            this.search = search;
        }

        int id;

        int remoteId;

        int formId;

        Entry entry;

        String thumbnail = "";

        String type = "picture";

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getFormId() {
            return formId;
        }

        public void setFormId(int formId) {
            this.formId = formId;
        }

        public Entry getEntry() {
            return entry;
        }

        public void setEntry(Entry entry) {
            this.entry = entry;
        }

        @Override
        public int compareTo(ListEntry o) {
            return getTitle().compareTo(o.getTitle());
        }

        public int getRemoteId() {
            return remoteId;
        }

        public void setRemoteId(int remoteId) {
            this.remoteId = remoteId;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public String getThumbnail() {
            return thumbnail;
        }
    }

    void updateEntries(ArrayList<Entry> mEntries, Boolean clear) {

        formulario.generateCamposFromJson();

        ArrayList<Campo> campos = formulario.getCampos();

        String titleField = "";
        String subTitleField = "";

        boolean isTitleForeing = false;

        boolean isSubTitleForeing = false;

        try {

            formulario.setTitle(formulario.getConfig().getString("text1"));
            formulario.setSubtitle(formulario.getConfig().getString("text2"));

        } catch (Exception e) {
            e.printStackTrace();
        }


        try {

            for (Campo campo : campos) {

                if (formulario.getTitle().equals(campo.getId())) {

                    titleField = campo.getNome();

                    //Log.d("REGTEST", titleField);

                    if (titleField.trim().matches(Formulario.IS_FOREING_PATTERN)) {

                        isTitleForeing = true;

                    }

                }

                if (formulario.getSubtitle().equals(campo.getId())) {

                    subTitleField = campo.getNome();

                    //Log.d("REGTEST", subTitleField);

                    if (subTitleField.trim().matches(Formulario.IS_FOREING_PATTERN)) {

                        isSubTitleForeing = true;

                    }

                }

            }

            //Log.d("REGTEST", "isTitleForeing(" + titleField + "):" + String.valueOf(isTitleForeing));
            //Log.d("REGTEST", "isSubTitleForeing(" + subTitleField + "):" + String.valueOf(isSubTitleForeing));

        } catch (Exception e) {

            e.printStackTrace();
        }

        try {

            if(clear){
                //Log.d("append_status",String.valueOf(clear));
                listEntries.clear();
                listEntriesOriginal.clear();
            }

            for (Entry entry : mEntries) {

                JSONObject temp = new JSONObject(entry.getJson());

                String title = getResources().getString(android.R.string.untitled);

                String subtitle = getResources().getString(android.R.string.untitled);

                ListEntry listEntry = new ListEntry();

                listEntry.setEntry(entry);

                listEntry.setFormId(entry.getFormId());

                listEntry.setRemoteId(entry.getRemoteId());

                listEntry.setId(entry.getId());

                listEntry.setSent(entry.getSent());

                try {

                    listEntry.setThumbnail(temp.getString("file1"));

                }catch (Exception e){

                    e.printStackTrace();

                }

                if (temp.has(formulario.getTitle())) {

                    title = temp.getString(formulario.getTitle());

                    if (isTitleForeing) {


                        String t = getValueFromForm(formulario.getTitle(), title);

                        if (!t.equals("-"))
                            title = t;//-




                    }
                    if(formulario.getTitle().contains("payment")){
                        title = getPayment(title);
                    }

                }

                if (temp.has(formulario.getSubtitle())) {

                    subtitle = temp.getString(formulario.getSubtitle());

                    if (isSubTitleForeing) {

                        String s = getValueFromForm(formulario.getSubtitle(), subtitle);

                        if (!s.equals("-"))
                            subtitle = s;
                    }

                    if(formulario.getSubtitle().contains("payment")){
                        subtitle = getPayment(subtitle);
                    }
                }

                listEntry.setTitle(title);

                listEntry.setSubtitle(subtitle);

                listEntry.setSearch(temp.toString());

                listEntries.add(listEntry);

                listEntriesOriginal.add(listEntry);

            }

            adapterEntries.notifyDataSetChanged();

        } catch (Exception e) {

            e.printStackTrace();

            //Log.d("EXCEPTION:", e.toString());

        }


    }
    private String getPayment(String text){
        String resultado = text;
        JSONObject titulo = null;
        if(text.contains("authorization_code")){
            try {
                titulo = new JSONObject(text);
                String mostra = titulo.getString("v2");
                String status = getBaseContext().getString(getBaseContext().getResources().getIdentifier(titulo.getString("status"), "string", getBaseContext().getPackageName()));
                if(titulo.has("estornado")){
                    status = "Estornado";
                }
                resultado = status + " #" + titulo.getString("authorization_code") + " - " + titulo.getString("parcelas") + "x DE " + mostra;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return resultado;
    }

    private String getValueFromForm(String field, String entryId) {

        int entryIdInt = -1;

        try {

            entryIdInt = Integer.parseInt(entryId);

        }catch (Exception e){

            e.printStackTrace();

             //Log.d("CLOUD_CRM_FOR", e.getMessage());

            return "-";

        }

        //Log.d("CLOUD_CRM_FOR", "getValueFromForm('"+field+"', '"+entryId+"')");

        int foreingFormId = 0;

        JSONArray campos = null;

        if(formId>0){

            try {

                campos = new JSONArray(formulario.getCamposJson());

            }catch (JSONException e){

                e.printStackTrace();

                //Log.d("CLOUD_CRM_FOR", "EXCEPTION: "+e.getMessage());

                return "-";
            }

            for(int i = 0; i < campos.length(); i++){

                try {

                    JSONObject jo = campos.getJSONObject(i);

                    String label = jo.getString("nome");
                    String xfield = jo.getString("id");

                    //Log.d("CLOUD_CRM_FOR", label+"????");

                    if(xfield.equals(field)){

                        foreingFormId = FormularioAdapter.getFormIdFromLabel(label);

                        //Log.d("CLOUD_CRM_FOR", "LABEL IS: "+label);

                    }

                }catch (Exception e){

                    //Log.d("CLOUD_CRM_FOR", e.getMessage());

                    e.printStackTrace();

                    return "-";
                }

            }

            //Log.d("CLOUD_CRM_FOR", "FOREING FORM: "+String.valueOf(foreingFormId));

            try {

                String result;
                boolean foreing_online = false;

                try {
                    foreing_online = formulario.getConfig().getBoolean("online");

                } catch (Exception e) {
                    e.getStackTrace();
                }

                if(foreing_online == true) {

                    Foreing entry = App.foreing.queryBuilder()
                            .where()
                            .eq("foreingId", entryIdInt)
                            .and()
                            .eq("formId", foreingFormId).query().get(0);



                    result = entry.getNome();


                } else {

                    Entry entry = App.getEntries().queryBuilder()
                            .where()
                            .eq("remoteId", entryIdInt)
                            .and()
                            .eq("formId", foreingFormId).query().get(0);

                    Formulario f = App.formularios.queryForEq("remoteId", foreingFormId).get(0);

                    //Log.d("CLOUD_CRM_FOR_F", f.getNome());

                    JSONObject config = f.getConfig();

                    //Log.d("CLOUD_CRM_FOR_C", "TITLE: " + config.getString("text1"));

                    JSONObject jsonObject = new JSONObject(entry.getJson());

                    //Log.d("CLOUD_CRM_FOR", "FIELD IS:" + jsonObject.getString(config.getString("text1")));

                    result = jsonObject.getString(config.getString("text1"));

                    //Log.d("CLOUD_CRM_FOR", "RESULT IS:" + result);

                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();

                //Log.d("CLOUD_CRM_FOR_E", "Error 11115:"+e.toString());
            }

        }

        return "-";

    }


    public void checkAll(){

        String tag = "ENTCHECK";

        //Log.d(tag, "checkAll()");

        ArrayList<Entry> mentries = new ArrayList<Entry>();

        try {

            String sql = "select id, length(json) from `entries` where formId = '" + String.valueOf(formId) + "';";

            //Log.d(tag, "SQL:"+sql);

            List<String[]> all = App.getEntries().queryRaw(sql).getResults();

            for(String[] row: all){

                //Log.d(tag, "Id: "+row[0]+" size: "+row[1]);

                mentries.add(App.getEntries().queryForId(Integer.parseInt(row[0])));

            }

        }catch (Exception e){

            e.printStackTrace();

            //Log.d(tag, e.toString());

        }

    }

    public void getEntries(Integer offset, Integer limit){

        String xTAG = "GET_ENTRIES";

        try {

            List<String[]> ss = App.getEntries().queryRaw("select id, status, userId, formId from `entries`;").getResults();

            for(String[] x: ss){

                //Log.d(xTAG, "ID: "+x[0]+" Status:"+x[1]+" UserID: "+x[2]+" FormID:"+x[3]);

            }

            entries = new ArrayList<>();

            //Log.d(xTAG, "Check form: " + String.valueOf(formulario.getRemoteId()));

            QueryBuilder queryBuilder = App.getEntries().queryBuilder();
            if (getIntent().getBooleanExtra("chooser", false)) {
                queryBuilder
                        .where()
                        .eq("formId", formId)
                        .and()
                        .eq("status", Entry.STATUS_NORMAL)
                        .and()
                        .gt("remoteId", 0)
                        .and()
                        .eq("userId", App.getCurrentUser().getRemoteId())
                ;
            }else{
                queryBuilder
                        .where()
                        .eq("formId", formId)
                        .and()
                        .eq("status", Entry.STATUS_NORMAL)
                        .and()
                        .eq("userId", App.getCurrentUser().getRemoteId())
                ;
            }
            entries = new ArrayList<Entry>(queryBuilder.offset(offset).limit(limit).query()); /*
            CloseableIterator<Entry> iterator = App.getEntries().iterator(queryBuilder.prepare());
            //Log.d(xTAG, "TRY");
            try {
                // get the raw results which can be cast under Android
                AndroidDatabaseResults results =
                        (AndroidDatabaseResults) iterator.getRawResults();
                Cursor cursor = results.getRawCursor();
                //Log.d(xTAG, "SQL:-> "+queryBuilder.prepare().toString());
                //Log.d(xTAG, "Cursor-iterate: " + String.valueOf(results.getCount()));
                String[] columns = cursor.getColumnNames();
                for (String column : columns) {
                    //Log.d(xTAG, "Name: " + column);
                }
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    //Log.d(xTAG, "start()");
                    //for(int i = 0; i < columns.length; i++) {
                    //    //Log.d("ENTCHECK", String.valueOf(i)+") "+columns[i]+" -> " + String.valueOf(cursor.getString(i)));
                    //}
                    Entry entry = new Entry();
                    entry.setId(cursor.getInt(2));
                    entry.setFormId(cursor.getInt(1));
                    entry.setJson(cursor.getString(3));
                    entry.setRemoteId(cursor.getInt(4));
                    entry.setSent(cursor.getInt(5));
                    entry.setStatus(cursor.getString(6));
                    entry.setUserId(cursor.getInt(10));
                    //Log.d(xTAG, "add(entry);");
                    entries.add(entry);
                }
            } finally {
                iterator.closeQuietly();
            }*/

            if (entries.size() == 0) {

                textViewNoResults.setVisibility(View.VISIBLE);

            } else {

                textViewNoResults.setVisibility(GONE);

                for (Entry e : entries) {

                    //Log.d(xTAG, "EntryId: " + String.valueOf(e.getRemoteId()));

                }

            }

        }catch (Exception e){

            e.printStackTrace();

            //Log.d(xTAG, e.toString());

        }
        return;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        try{

            if(formulario.getConfig().getBoolean("disable_edit")){
                Toast.makeText(this, "Nada salvo", Toast.LENGTH_SHORT).show();
                return;
            }

        }catch (Exception el){

        }

        if(resultCode == RESULT_OK){

            int id = data.getIntExtra("entryId", 0);


            try {

                final Entry myEntry = App.getEntries().queryForId(id);

                //Log.d("AUTO_SAVE", "Saving entry!");

                myEntry.send(new CloudCRMAPI.OnFinish() {
                    @Override
                    public void onSuccess(final JSONObject result) {

                        //Log.d("AUTO_SAVE", "success");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(ListEntriesActivity.this, R.string.uploaded, Toast.LENGTH_SHORT).show();
                                getEntries(DEFAULT_OFFSET,DEFAULT_LIMIT);
                                    updateEntries(entries, true);
                                    adapterEntries.notifyDataSetChanged();

                            }
                        });

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

            }catch (Exception e){

                e.printStackTrace();

            }



        }

    }
}