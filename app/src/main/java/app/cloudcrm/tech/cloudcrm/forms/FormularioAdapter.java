package app.cloudcrm.tech.cloudcrm.forms;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Array;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


import app.cloudcrm.tech.cloudcrm.BuildConfig;
import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.activities.CidadeActivity;
import app.cloudcrm.tech.cloudcrm.activities.CustomActivity;
import app.cloudcrm.tech.cloudcrm.activities.ForeingActivity;
import app.cloudcrm.tech.cloudcrm.activities.GalleryActivity;
import app.cloudcrm.tech.cloudcrm.activities.InputDecimalActivity;
import app.cloudcrm.tech.cloudcrm.activities.InputDecimalPaymentActivity;
import app.cloudcrm.tech.cloudcrm.activities.ListEntriesActivity;
import app.cloudcrm.tech.cloudcrm.activities.MapActivity;
import app.cloudcrm.tech.cloudcrm.activities.PaymentActivity;
import app.cloudcrm.tech.cloudcrm.activities.PhoneActivity;
import app.cloudcrm.tech.cloudcrm.activities.PickImageActivity;
import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.activities.FormularioActivity;
import app.cloudcrm.tech.cloudcrm.classes.CloudCRMAPI;
import app.cloudcrm.tech.cloudcrm.classes.ImagePicker;
import app.cloudcrm.tech.cloudcrm.misc.Validador;
import app.cloudcrm.tech.cloudcrm.models.Custom;
import app.cloudcrm.tech.cloudcrm.models.Entry;
import app.cloudcrm.tech.cloudcrm.models.EntryFiles;
import app.cloudcrm.tech.cloudcrm.models.Foreing;
import app.cloudcrm.tech.cloudcrm.models.Formulario;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;


import java.io.IOException;
import me.pagar.mposandroid.Mpos;

import static app.cloudcrm.tech.cloudcrm.activities.PhoneActivity.format;
import static app.cloudcrm.tech.cloudcrm.classes.CloudCRMActivity.getInstance;

/**
 *
 * Created by Alberto on 14/6/2016.
 *
 */

public class FormularioAdapter extends ArrayAdapter<Campo>{

    Formulario formulario;

    LayoutInflater mInflater;

    public JSONObject formularioData;

    Entry myEntry = null;

    public static String thumbnailFolder = Environment.getExternalStorageDirectory()+File.separator+".cloudcrm"+File.separator+"cache"+File.separator;

    public void setMyEntry(Entry myEntry) {
        this.myEntry = myEntry;
    }

    @Override
    public Context getContext() {
        return super.getContext();
    }

    public FormularioAdapter(Context context, Formulario formulario) {

        super(context, R.layout.formularios_text_item);

        this.formulario = formulario;

        try{

            File dir = new File(thumbnailFolder);

            dir.mkdirs();

        }catch (Exception e){

            e.printStackTrace();

        }

        try {

            this.formularioData = formulario.getObject();

        }catch (Exception e){

            e.printStackTrace();

        }

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        BaseViewHolder viewHolder = null;

        Campo atual = null;

        try {

            atual = formulario.campos.get(position);

            if (atual == null) {

                //Log.d("FORMU.ATUAL", "atual is null");

                atual = new Campo("teste", "Teste", Formulario.getTipoByNome(FormularioAdapterConstants.TYPE_BOOLEAN));

            }

        } catch (Exception e) {

            //Log.d("ERROR_LOG", e.getMessage());

        }

        String tipo = FormularioAdapterConstants.TYPE_HEADING;

        try {

            tipo = atual.getTipo().getTipo();

        } catch (Exception e) {

            //Log.d("FORMU.ADAPTER", e.getMessage());

            tipo = FormularioAdapterConstants.TYPE_HEADING;

        }




        switch (tipo) {
            case FormularioAdapterConstants.TYPE_TEXT:
                viewHolder = new ViewHolderText();
                break;
            case FormularioAdapterConstants.TYPE_INT:
                viewHolder = new ViewHolderInt();
                break;
            case Formulario.TYPE_FLOAT:
                viewHolder = new ViewHolderFloat();
                break;
            case Formulario.TYPE_BIT:
                viewHolder = new ViewHolderBoolean();
                break;
            case Formulario.TYPE_PICTURE:
                viewHolder = new ViewHolderPicture();
                break;
            case Formulario.TYPE_SELECT:
                viewHolder = new ViewHolderSelect();
                break;
            case Formulario.TYPE_SIGNPAD:
                viewHolder = new ViewHolderSignPad();
                break;
            case Formulario.TYPE_GPS:
                viewHolder = new ViewHolderGPS();
                break;
            case Formulario.TYPE_DATE:
                viewHolder = new ViewHolderDate();
                break;
            case Formulario.TYPE_HEADING:
                viewHolder = new BaseViewHolder();
                break;
            case Formulario.TYPE_SAVE_N_SEND:
                viewHolder  = new ViewHolderSaveSend();
                break;
            case Formulario.TYPE_FOREING:
                viewHolder  = new ViewHolderForeing();
                break;
            case Formulario.TYPE_CIDADE:
                viewHolder = new ViewHolderTextCidade();
                break;
            case Formulario.TYPE_CPF:
                viewHolder = new ViewHolderCPF();
                break;
            case Formulario.TYPE_EMAIL:
                viewHolder = new ViewHolderEmail();
                break;
            case Formulario.TYPE_PHONE:
                viewHolder = new ViewHolderPhone();
                break;
            case Formulario.TYPE_POLYGON:
                viewHolder = new ViewHolderPolygon();
                break;
            case Formulario.TYPE_PAYMENT:
                viewHolder = new ViewHolderPayment();
                break;
            case Formulario.TYPE_CUSTOM:
                viewHolder = new ViewHolderCustom();
                break;
            default:
                viewHolder = new BaseViewHolder();
                break;
        }


            if (convertView == null) {

            //Log.d("FORMULARIO_TIPO", tipo);

            //Log.d("FORMULARIO_TIPO", atual.getNome());

            try {


                //Log.d("FA_EXCEPTION", "beforeRender");

                convertView = mInflater.inflate(atual.getTipo().getResource(), parent, false);

                viewHolder.bindUI(convertView);

                convertView.setTag(viewHolder);

            }catch (Exception e){

                e.printStackTrace();

                //Log.d("FA_EXCEPTION", e.getMessage());


            }

        } else {

            viewHolder = (BaseViewHolder) convertView.getTag();

        }

        viewHolder.loadData(atual);

        return convertView;

    }

    @Override
    public int getViewTypeCount() {
        return Formulario.tipos.size();
    }

    @Override
    public int getItemViewType(int position) {

        Campo campo = formulario.campos.get(position);

        //Log.d("FORMU.CAMPO", campo.getNome());

        if (campo == null) return 0;

        Tipo tp = campo.getTipo();

        if (tp == null) {

            return 0;

        } else {

            return tp.getId();

        }
    }

    @Override
    public int getCount() {
        if(formulario!=null) {
            return formulario.campos.size();
        }else{
            return 0;
        }
    }

    @Override
    public Filter getFilter() {
        return super.getFilter();
    }

    @Override
    public Campo getItem(int position) {
        return formulario.campos.get(position);
    }


    /**
     *  BaseViewHolder
     */

    class BaseViewHolder {

        public TextView label;

        public Object getValue() {
            return null;
        }

        public TextView getLabel() {
            return label;
        }

        public void setLabel(TextView label) {
            this.label = label;
        }

        public BaseViewHolder(){

        }

        public void bindUI(View convertView) {

            label = (TextView) convertView.findViewById(R.id.label);

        }

        public void loadData(Campo campo) {

            String exp = "\\[[0-9]+\\]";

            String stringToSet = campo.getNome().replaceAll(exp, "");

            //Log.d("REGEXP", stringToSet+" => "+campo.getNome());

            if(label != null) {

                label.setText(stringToSet);

                if(campo.getTipo().getTipo() != FormularioAdapterConstants.TYPE_HEADING)

                    label.setTextColor(getContext().getResources().getColor(R.color.colorGray));

                if (campo.isRequired()) {

                    //Log.d("REQUIRED_FIELDS", campo.getNome());

                    label.setText(stringToSet + " (*)");

                    try {
                        if(formularioData.has(campo.getId())) {
                            if (formularioData.get(campo.getId()).equals("")) {

                                label.setTextColor(getContext().getResources().getColor(R.color.colorDanger));

                            }else{

                                label.setTextColor(getContext().getResources().getColor(R.color.colorSuccess));

                            }
                        }else{
                            label.setTextColor(getContext().getResources().getColor(R.color.colorDanger));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                    //Log.d("REQUIRED_FIELDS-", campo.getNome());

                }

            }

        }

    }

    /**
     *  All ViewHolders
     * --------------------------------------------------------------------------------------------
     *  ViewHolderText
     *
     */

    class ViewHolderText extends BaseViewHolder{

        EditText editText;

        @Override
        public void bindUI(View convertView) {

            super.bindUI(convertView);

            editText = (EditText) convertView.findViewById(R.id.editText);

            editText.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    Campo camp = (Campo) v.getTag();

                    v.setEnabled(!camp.isReadOnly());

                }
            });

            editText.addTextChangedListener(new CustomTextWatcher(editText) {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    //Log.d("FORMU.afterChanged", String.valueOf(s));

                    Campo camp = (Campo) getEditText().getTag();

                    if (camp != null) {

                        //Log.d("FORMU.PUT", "SETTING: " + camp.getNome() + " -> " + String.valueOf(s));

                        try {

                            String valor = String.valueOf(s);

                            formularioData.put(camp.getId(), valor);

                            //((FormularioActivity)(getContext())).saveNow();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {

                        //Log.d("FORMU.ISNULL", "camp is null");

                    }

                }
            });

        }

        @Override
        public void loadData(Campo campo) {

            super.loadData(campo);

            if (campo == null) {

                //Log.d("FORMU.NULL", "Campo == null");

            }

            editText.setEnabled(!campo.isReadOnly());

            editText.setTag(campo);

            //Log.d("FORMU.JSON", formularioData.toString());

            //Log.d("FORMU.IF", "if(formularioData.has('" + campo.getId() + "'))");

            if (formularioData.has(campo.getId())) {

                try {


                    String val = formularioData.getString(campo.getId());

                    if(val.toUpperCase().equals("NULL")){
                        val = "";
                    }
                    editText.setText(val);



                } catch (JSONException e) {

                    //Log.d("FORMU", e.getMessage());

                    e.printStackTrace();

                }

            } else {


                editText.setText("");

                try {

                    formularioData.put(campo.getId(), "");

                    //Log.d("FORMU", "Error");

                } catch (Exception e) {


                }

            }

        }
    }

    class ViewHolderNull extends BaseViewHolder {

        @Override
        public void bindUI(View convertView) {

        }

        @Override
        public void loadData(Campo campo) {



        }
    }

    class ViewHolderFloat extends BaseViewHolder {

        Button button;

        @Override
        public void bindUI(View convertView) {

            super.bindUI(convertView);

            button = (Button) convertView.findViewById(R.id.button);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Campo campo = (Campo) v.getTag();

                    Intent it = new Intent(getContext(), InputDecimalActivity.class);

                    it.putExtra("field", campo.getId());

                    it.putExtra("formId", formulario.getRemoteId());

                    it.putExtra("label", campo.getNome());


                    try {
                        it.putExtra("value", formularioData.getDouble(campo.getId()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ((Activity)getContext()).startActivityForResult(it, FormularioActivity.TYPE_FLOAT_RESULT);

                }
            });


        }

        @Override
        public void loadData(Campo campo) {

            super.loadData(campo);

            if (campo == null) {

                //Log.d("FORMU.NULL", "Campo == null");

            }

            button.setTag(campo);

            button.setText(getContext().getString(R.string.digite_valor));

            String format = getFormatFromField(campo.getId());

            //Log.d("FORMATE", format);

            //Log.d("FORMU.JSON", formularioData.toString());

            //Log.d("FORMU.IF", "if(formularioData.has('" + campo.getId() + "'))");



            if (formularioData.has(campo.getId())) {

                try {

                    Double val = formularioData.getDouble(campo.getId());

                    DecimalFormat df2 = new DecimalFormat(format);

                    button.setText(df2.format(val));

                    /*

                    JSONArray payment = formulario.getPayment();
                    //Log.d("FORMU.PAYMENT", payment.toString());
                    */




                } catch (JSONException e) {

                    //Log.d("FORMU", e.getMessage());

                    e.printStackTrace();

                }

            } else {

                DecimalFormat df2 = new DecimalFormat(format);

                button.setText(df2.format(0));

            }

        }

        private String getFormatFromField(String id) {

            try {

                JSONObject config = formulario.getConfig();

                if (config.has("fields")) {

                    JSONObject o = config.getJSONObject("fields");

                    if(o.has(id)){

                        return o.getJSONObject(id).getString("decimalFormat");

                    }else{

                        //Log.d("FORMATE", "!ID: "+id);

                    }

                }else{

                    //Log.d("FORMATE", "ELSE");

                    //Log.d("FORMATE", config.toString());

                }

            }catch (Exception e){

                e.printStackTrace();

                //Log.d("FORMATE", "Errro: "+e.toString());

            }

            return  "#,###,###,##0.00" ;
        }
    }

    class ViewHolderPolygon extends BaseViewHolder {

        Button button;

        @Override
        public void bindUI(View convertView) {

            super.bindUI(convertView);

            button = (Button) convertView.findViewById(R.id.button);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Campo campo = (Campo) v.getTag();

                    Intent it = new Intent(getContext(), MapActivity.class);

                    it.putExtra("field", campo.getId());

                    it.putExtra("formId", formulario.getRemoteId());

                    it.putExtra("label", campo.getNome());

                    try {
                        it.putExtra("value", formularioData.getString(campo.getId()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //Log.d("MAP_ACT", "Lauch Intent");

                    ((Activity)getContext()).startActivityForResult(it, FormularioActivity.TYPE_POLYGON_RESULT);
                }
            });


        }

        @Override
        public void loadData(Campo campo) {

            super.loadData(campo);

            if (campo == null) {

                //Log.d("FORMU.NULL", "Campo == null");

            }

            button.setTag(campo);

            button.setText(getContext().getString(R.string.digite_valor));

            String format = getFormatFromField(campo.getId());

            //Log.d("FORMATE", format);

            //Log.d("FORMU.JSON", formularioData.toString());

            //Log.d("FORMU.IF", "if(formularioData.has('" + campo.getId() + "'))");

            if (formularioData.has(campo.getId())) {

                try {

                    JSONObject jsonObject = new JSONObject(formularioData.getString(campo.getId()));

                    Double val = jsonObject.getDouble("area");

                    DecimalFormat df2 = new DecimalFormat(format);

                    button.setText(df2.format(val)+" ha.");

                } catch (Exception e) {

                    button.setText(R.string.wo_polygon);

                    //Log.d("FORMU", e.getMessage());

                    e.printStackTrace();

                }

            } else {

                button.setText(R.string.wo_polygon);

            }

        }

        private String getFormatFromField(String id) {

            try {

                JSONObject config = formulario.getConfig();

                if (config.has("fields")) {

                    JSONObject o = config.getJSONObject("fields");

                    if(o.has(id)){

                        return o.getJSONObject(id).getString("decimalFormat");

                    }else{

                        //Log.d("FORMATE", "!ID: "+id);

                    }

                }else{

                    //Log.d("FORMATE", "ELSE");

                    //Log.d("FORMATE", config.toString());

                }

            }catch (Exception e){

                e.printStackTrace();

                //Log.d("FORMATE", "Errro: "+e.toString());

            }

            return  "#,###,###,##0.00" ;
        }
    }

    class ViewHolderEmail extends BaseViewHolder {

        EditText editText;

        Button button;

        @Override
        public void bindUI(View convertView) {

            super.bindUI(convertView);

            editText = (EditText) convertView.findViewById(R.id.editText);

            button = (Button) convertView.findViewById(R.id.buttonValid);

            /*editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    //Log.d("FA_EXCEPTION", "v.setEnabled();");

                    Campo camp = (Campo) v.getTag();

                    v.setEnabled(!camp.isReadOnly());

                }
            });*/

            editText.addTextChangedListener(new CustomTextWatcher(editText) {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    //Log.d("FA_EXCEPTION", String.valueOf(s));

                    Campo camp = (Campo) getEditText().getTag();

                    if (camp != null) {
                        // -- test

                        ////Log.d("FORMU.PUT", "SETTING: " + camp.getNome() + " -> " + String.valueOf(s));

                        try {

                            String valor = String.valueOf(s);

                            formularioData.put(camp.getId(), valor);

                            //((FormularioActivity)(getContext())).saveNow();

                            if(!Validador.isValidEmail(valor)){

                                button.setVisibility(View.VISIBLE);

                            }else{

                                button.setVisibility(View.INVISIBLE);

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {

                        //Log.d("FORMU.ISNULL", "camp is null");

                    }

                }
            });

        }

        @Override
        public void loadData(Campo campo) {

            super.loadData(campo);

            if (campo == null) {

                //Log.d("FORMU.NULL", "Campo == null");

            }

            editText.setEnabled(!campo.isReadOnly());


            editText.setTag(campo);

            //Log.d("FORMU.JSON", formularioData.toString());

            //Log.d("FORMU.IF", "if(formularioData.has('" + campo.getId() + "'))");

            if (formularioData.has(campo.getId())) {

                try {


                    String val = formularioData.getString(campo.getId());

                    if(val.toUpperCase().equals("NULL")){
                        val = "";
                    }
                    editText.setText(val);



                } catch (JSONException e) {

                    //Log.d("FORMU", e.getMessage());

                    e.printStackTrace();

                }

            } else {


                editText.setText("");

                try {

                    formularioData.put(campo.getId(), "");

                    //Log.d("FORMU", "Error");

                } catch (Exception e) {

                    e.printStackTrace();

                }

            }

        }
    }

    class ViewHolderCPF extends BaseViewHolder {

        EditText editText;

        Button button;

        @Override
        public void bindUI(View convertView) {

            super.bindUI(convertView);

            editText = (EditText) convertView.findViewById(R.id.editText);

            button = (Button) convertView.findViewById(R.id.buttonValid);

            editText.setInputType(InputType.TYPE_CLASS_NUMBER);

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    Campo camp = (Campo) v.getTag();

                    v.setEnabled(!camp.isReadOnly());

                }
            });

            editText.addTextChangedListener(new CustomTextWatcher(editText) {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    //Log.d("FORMU.afterChanged", String.valueOf(s));

                    Campo camp = (Campo) getEditText().getTag();

                    if (camp != null) {

                        //Log.d("FORMU.PUT", "SETTING: " + camp.getNome() + " -> " + String.valueOf(s));

                        try {

                            String valor = String.valueOf(s);

                            if((Validador.isValidCNPJ(valor))||(Validador.isValidCPF(valor))){
                                //getEditText().setError(null);
                                button.setVisibility(View.INVISIBLE);
                            }else{
                                button.setVisibility(View.VISIBLE);
                            }

                            formularioData.put(camp.getId(), valor);

                            //((FormularioActivity)(getContext())).saveNow();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {

                        //Log.d("FORMU.ISNULL", "camp is null");

                    }

                }
            });

        }

        @Override
        public void loadData(Campo campo) {

            super.loadData(campo);

            if (campo == null) {

                //Log.d("FORMU.NULL", "Campo == null");

            }

            editText.setEnabled(!campo.isReadOnly());


            editText.setTag(campo);

            //Log.d("FORMU.JSON", formularioData.toString());

            //Log.d("FORMU.IF", "if(formularioData.has('" + campo.getId() + "'))");

            if (formularioData.has(campo.getId())) {

                try {


                    String val = formularioData.getString(campo.getId());

                    if(val.toUpperCase().equals("NULL")){
                        val = "";
                    }
                    editText.setText(val);



                } catch (JSONException e) {

                    //Log.d("FORMU", e.getMessage());

                    e.printStackTrace();

                }

            } else {


                editText.setText("");

                try {

                    formularioData.put(campo.getId(), "");

                    //Log.d("FORMU", "Error");

                } catch (Exception e) {


                }

            }

        }
    }

    class ViewHolderTextCidade extends BaseViewHolder{

        AutoCompleteTextView editText;

        Button button;

        @Override
        public void bindUI(View convertView) {

            super.bindUI(convertView);

            editText = (AutoCompleteTextView) convertView.findViewById(R.id.editText);

            button = (Button) convertView.findViewById(R.id.button);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent it = new Intent((Activity)getContext(),CidadeActivity.class);

                    Campo camp = (Campo) view.getTag();

                    it.putExtra("field", camp.getId());

                    try {
                        it.putExtra("value", formularioData.getString(camp.getId()));
                    } catch (JSONException e) {

                        it.putExtra("value", "");
                    }

                    ((Activity)getContext()).startActivityForResult(it, 543);

                }
            });

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    Campo camp = (Campo) v.getTag();

                    if(camp!=null) {

                        v.setEnabled(!camp.isReadOnly());

                    }

                }
            });

            editText.addTextChangedListener(new CustomTextWatcher(editText) {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    //Log.d("FORMU.afterChanged", String.valueOf(s));

                    Campo camp = (Campo) getEditText().getTag();

                    if (camp != null) {

                        //Log.d("FORMU.PUT", "SETTING: " + camp.getNome() + " -> " + String.valueOf(s));

                        try {
                            formularioData.put(camp.getId(), String.valueOf(s));

                           // ((FormularioActivity)(getContext())).saveNow();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {

                        //Log.d("FORMU.ISNULL", "camp is null");

                    }

                }
            });

        }

        @Override
        public void loadData(Campo campo) {

            super.loadData(campo);

            if (campo == null) {

                //Log.d("FORMU.NULL", "Campo == null");

                return;

            }

            editText.setEnabled(!campo.isReadOnly());

            String[] opcoes = new String[]{};

            String tpInput = "";

            try {

                tpInput = campo.getNome().split("\\(")[1].replace(")", "").toUpperCase();
                String tpName = campo.getNome().split("\\(")[0];

                label.setText(tpName);

            }catch (Exception e){

                //Log.d("FORMU.EXCEPTION", e.getMessage());

                e.printStackTrace();

            }

            switch (tpInput) {

                case "CIDADE_BR":

                    opcoes = getContext().getResources().getStringArray(R.array.cidades_br);

                break;

                case "UF_BR":

                    opcoes = getContext().getResources().getStringArray(R.array.uf_br);

                break;
                case "CULTURA":

                    opcoes = getContext().getResources().getStringArray(R.array.mculturas);

                break;

                default:

                    if(tpInput.toUpperCase().startsWith("FORM:")){

                        //Log.d("AUTO_COMPLETE", "Hast : form:");

                        String strFormId = tpInput.replace("FORM:", "");

                        int intFormId = Integer.parseInt(strFormId.trim());

                        try {

                            Formulario formulario = App.formularios.queryForEq("remoteId", intFormId).get(0);

                            ArrayList<Entry> entries = new ArrayList<>(App.getEntries().queryForEq("formId", intFormId));

                            ArrayList<String> strEntries = new ArrayList<String>();

                            for(Entry entry : entries){

                                JSONObject entryData = new JSONObject();

                                String str = "";


                                try {

                                    entryData = new JSONObject(entry.getJson());

                                    str = entryData.getString(formulario.getTitle()).trim().toUpperCase();

                                }catch (Exception e){

                                    e.printStackTrace();

                                }

                                if(!str.equals("")) {

                                    //Log.d("AUTO_COMPLETE", "Str: "+str);

                                    strEntries.add(str);

                                }

                            }

                            String[] optTemp = new String[strEntries.size()];

                            opcoes = strEntries.toArray(optTemp);

                        } catch (Exception e) {
                            e.printStackTrace();



                        }

                    }

                break;

            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, opcoes);

            editText.setAdapter(adapter);

            //Log.d("FORMU.EXCEPTION", campo.getNome());

            editText.setTag(campo);

            button.setTag(campo);

            if (formularioData.has(campo.getId())) {

                try {


                    String val = formularioData.getString(campo.getId());

                    if(val.toUpperCase().equals("NULL")){
                        val = "";
                    }
                    editText.setText(val);

                    button.setText(val);

                    if(val.equals("")){
                        button.setText("- Escolha cidade -");
                    }



                } catch (JSONException e) {

                    //Log.d("FORMU", e.getMessage());

                    e.printStackTrace();

                }

            } else {


                button.setText("- Escolha cidade -");

                editText.setText("");

                try {

                    formularioData.put(campo.getId(), "");

                } catch (Exception e) {

                    e.printStackTrace();

                }

            }

        }
    }

    class ViewHolderInt extends BaseViewHolder{

        EditText editText = null;

        @Override
        public void bindUI(View convertView) {

            editText = (EditText) convertView.findViewById(R.id.editTextNumber);

            super.bindUI(convertView);

            editText.addTextChangedListener(new CustomTextWatcher(editText) {
                @Override
                public void afterTextChanged(Editable s) {
                    super.afterTextChanged(s);

                    Campo campo = (Campo) getEditText().getTag();

                    //Log.d("CCRM.EXCP", "After changed!");

                    try {
                        formularioData.put(campo.getId(), getEditText().getText().toString());

                        //Log.d("CCRM.EXCP", "Storing to '"+getEditText().getText().toString()+"': "+campo.getId());

                        //((FormularioActivity)(getContext())).saveNow();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        //Log.d("CCRM.EXCP", e.getMessage());
                    }

                }
            });

        }

        @Override
        public void loadData(Campo campo) {

            super.loadData(campo);

            editText.setTag(campo);

            if (formularioData.has(campo.getId())) {

                try {

                    String val = formularioData.getString(campo.getId());

                    if(val.toUpperCase().equals("NULL")){
                        //Log.d("CCRM.EXCP", "Val is null");
                        val = "";
                    }

                    //Log.d("CCRM.EXCP", "HAS:"+campo.getId());

                    editText.setText(val);

                } catch (Exception e) {

                    e.printStackTrace();

                    //Log.d("CCRM.EXCP", e.getMessage());

                    editText.setText("");

                }

            } else {

                editText.setText("");

            }


        }
    }

    class ViewHolderBoolean extends BaseViewHolder {

        CheckBox checkBox = null;

        Switch aSwitch;

        @Override
        public void loadData(Campo campo) {

            super.loadData(campo);

            aSwitch.setTag(campo);

            checkBox.setTag(campo);

            checkBox.setText(campo.getNome());

            aSwitch.setChecked(false);

            checkBox.setChecked(false);

            if (formularioData.has(campo.getId())) {

                try {
                    aSwitch.setChecked(formularioData.getInt(campo.getId())==1);
                    checkBox.setChecked(formularioData.getInt(campo.getId())==1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {

                try {

                    formularioData.put(campo.getId(), 0);

                } catch (Exception e) {

                    e.printStackTrace();

                }

                //Log.d("FORMS.NOT_FOUND_ID", "not found " + campo.getNome());

            }

        }

        @Override
        public void bindUI(View convertView) {

            super.bindUI(convertView);

            aSwitch = (Switch) convertView.findViewById(R.id.switch1);

            aSwitch.setVisibility(View.INVISIBLE);

            checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

            label.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBox.callOnClick();
                }
            });

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Campo temp = (Campo) v.getTag();

                    //Switch c = (Switch) v;

                    CheckBox c = (CheckBox)v;

                    //Log.d("FORMS.BOOLEAN", "Has been changed " + temp.getNome());

                    try {
                        int val = 0;

                        if(c.isChecked()){
                            val = 1;
                        }

                        formularioData.put(temp.getId(), val);

                        //((FormularioActivity)getContext()).saveNow();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

        }
    }

    class ViewHolderDate extends BaseViewHolder{

        EditText editText = null;
        Button dateButton;

        @Override
        public void bindUI(View convertView) {

            super.bindUI(convertView);

            dateButton = (Button) convertView.findViewById(R.id.dateButton);

            dateButton.setText(R.string.completar_data);

            dateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Campo campo = (Campo) v.getTag();

                    if(campo == null){
                        return;
                    }

                    String valor = "";

                    Calendar cal = Calendar.getInstance();

                    String current = String.valueOf(cal.get(Calendar.YEAR))+"-"
                            +String.valueOf(cal.get(Calendar.MONTH))+"-"+
                            String.valueOf(cal.get(Calendar.DAY_OF_MONTH));

                    try {
                        if(formularioData.has(campo.getId())) {
                            valor = formularioData.getString(campo.getId());
                        }else{
                            valor = current;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                        valor = current;

                    }

                    if(valor.equals("")){
                        valor = current;
                    }

                    final String val = valor;

                    Calendar setCalendar = Calendar.getInstance();

                    String[] values = val.split("-");

                    int year = 2016;

                    int month = 6;

                    int day = 28;

                    try{

                        year = Integer.valueOf(values[0]);
                        month = Integer.valueOf(values[1]);
                        day = Integer.valueOf(values[2]);

                        if(val.equals("0000-00-00")){
                            throw new Exception("Invalid date");
                        }

                        if(val.equals("00-00-0000")){
                            throw new Exception("Invalid date");
                        }

                        if(year < 1500){
                            throw new Exception("Invalid date");
                        }

                    }catch (Exception e){

                        year = cal.get(Calendar.YEAR);

                        day = cal.get(Calendar.DAY_OF_MONTH);

                        month = cal.get(Calendar.MONTH);

                    }

                    DatePickerDialog dtx = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                            Calendar calendar = new GregorianCalendar(year,
                                    monthOfYear,
                                    dayOfMonth,
                                    0,
                                    0);

                            DateFormat dateFormat = SimpleDateFormat.getDateInstance(0, Locale.FRENCH);


                            long time = calendar.getTimeInMillis();

                            Date dt = new Date(time);

                            DateFormat dtf = DateFormat.getDateInstance();

                            String sim = new SimpleDateFormat("dd-MM-yyyy").format(dt);
                            String sim_db = new SimpleDateFormat("yyyy-MM-dd").format(dt);

                            dateButton.setText(sim);

                            try {
                                formularioData.put(campo.getId(), sim_db);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, year, month, day);

                    dtx.show();


                }

            });

        }

        @Override
        public void loadData(Campo campo) {

            super.loadData(campo);

            dateButton.setTag(campo);

            dateButton.setText(R.string.sel_data);

            try {

                String val = "";

                val = formularioData.getString(campo.getId());

                if(val.toUpperCase().equals("NULL")){

                    val = getContext().getResources().getString(R.string.sel_data);

                }

                if(val.equals("0000-00-00")){

                    val = getContext().getResources().getString(R.string.sel_data);

                }

                dateButton.setText(val);

            } catch (JSONException e) {
                e.printStackTrace();

                dateButton.setText(R.string.completar_data);
            }

        }
    }

    class ViewHolderSelect extends BaseViewHolder{

        Spinner spinner;

        Button button;

        ArrayAdapter<String> spinnerAdapter;

        @Override
        public void loadData(Campo campo) {
            super.loadData(campo);

            spinnerAdapter.clear();

            String label = campo.getNome();

            //Log.d("CLOUD_ERR", "PRINT_R");

            for (String opt : campo.getOptions()) {

                if (opt.trim().equals("")) continue;

                //Log.d("ADDDED_OPTIONS", opt);

                spinnerAdapter.add(opt);

            }



            spinner.setTag(campo);

            button.setTag(campo);

            button.setText(getContext().getResources().getString(R.string.selecionar));

            try {
                if (formularioData.has(campo.getId())) {
                    button.setText(formularioData.getString(campo.getId()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void bindUI(final View convertView) {

            spinner = (Spinner) convertView.findViewById(R.id.spinner);

            spinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);

            spinner.setAdapter(spinnerAdapter);

            super.bindUI(convertView);

            button = (Button) convertView.findViewById(R.id.button);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Campo campo = (Campo) v.getTag();

                    new AlertDialog.Builder(getContext())
                            .setTitle(campo.getNome())
                            .setAdapter(spinnerAdapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    button.setText(spinnerAdapter.getItem(which));
                                    try{

                                       // ((FormularioActivity)(getContext())).saveNow();

                                        formularioData.put(campo.getId(), spinnerAdapter.getItem(which));

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }).create().show();

                }
            });

            /*spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    Campo atual;

                    try {
                        atual = (Campo) parent.getTag();
                    } catch (Exception e) {
                        return;
                    }
                    if (atual != null) {
                        try {
                            formularioData.put(atual.getId(), spinnerAdapter.getItem(position));

                            //Log.d("SELECTED", spinnerAdapter.getItem(position));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });*/

        }

    }

    class ViewHolderForeing extends BaseViewHolder{

        String TAG = "CLOUD_FOREING";

        Spinner spinner;

        Button button;

        MapAdapter spinnerAdapter;

        @Override
        public void loadData(Campo campo) {
            super.loadData(campo);

            spinnerAdapter.clear();
            boolean foreing_online = false;
            try {
                foreing_online = formulario.getConfig().getBoolean("online");

            } catch (Exception e) {
                e.getStackTrace();
            }
            String label = campo.getNome();

            //Log.d(TAG, "PRINT_R");

            int formId = getFormIdFromLabel(label);

            button.setTag(campo);

            button.setText(getContext().getResources().getString(R.string.selecionar));


            try {
                formularioData.put("formIdForeing",String.valueOf(getFormIdFromLabel(label)));

                //Log.d("formIdForeing",String.valueOf(getFormIdFromLabel(label)));
            }catch (JSONException e){
                e.printStackTrace();
            }


            if (formId > 0) {

                //Log.d(TAG, "LOAD ITEMS:");

                try {

                    Formulario formulario = App.formularios.queryForEq("remoteId", formId).get(0);

                    //Log.d(TAG, String.valueOf(formularioData.getInt(campo.getId())));

                    if(foreing_online == true) {
                        ArrayList<Foreing> entries = new ArrayList<>(App.foreing.queryBuilder()
                                .where()
                                .eq("formId", formId)
                                .and()
                                .eq("foreingId", formularioData.getInt(campo.getId())).query());

                        button.setText(entries.get(0).getNome());

                    } else {
                        ArrayList<Entry> entries = new ArrayList<>(App.getEntries().queryBuilder()
                                .where()
                                .eq("formId", formId)
                                .and()
                                .eq("remoteId", formularioData.getInt(campo.getId()))
                                .and()
                                .eq("status", "A").query());

                        JSONObject config = formulario.getConfig();

                        //for (Entry e : entries) {

                        //MapItem mapItem = new MapItem();

                        JSONObject json = new JSONObject(entries.get(0).getJson());

                        button.setText(json.getString(config.getString("text1")));

                        //Log.d(TAG, String.valueOf(entries.get(0).getRemoteId()));

                        //Log.d(TAG, json.getString(config.getString("text1")));

                        //Log.d(TAG, json.toString(2));
                    }

                        //button.setText();

                    /*}

                        try {

                            JSONObject json = new JSONObject(e.getJson());

                            mapItem.setDisplay(json.getString(config.getString("text1")));

                            spinnerAdapter.add(mapItem);

                        } catch (JSONException e1) {

                            e1.printStackTrace();

                            //Log.d(TAG, e1.getMessage());

                        }

                        //Log.d(TAG, String.valueOf(mapItem.getValue()) + "->" + mapItem.getDisplay());

                    }*/


                } catch (Exception e) {

                    e.printStackTrace();

                    //Log.d(TAG, e.getMessage());
                }

            }


            /*spinner.setTag(campo);


            //Log.d(TAG, "LOAD VALUE");

            try {

                //Log.d(TAG, "formularioData has "+campo.getId());

                if (formularioData.has(campo.getId())) {

                    //button.setText(formularioData.getString(campo.getId()));

                    //Log.d(TAG, "is formId > 0?");

                    if (formId > 0) {

                        //Log.d(TAG, "yes, it is: "+String.valueOf(formularioData.getString(campo.getId())));

                        CharSequence chr = spinnerAdapter.getItemByVal(formularioData.getString(campo.getId())).display;

                        if (chr != null) {
                            button.setText(chr);
                        }else{

                            button.setText(getContext().getResources().getString(R.string.selecionar));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();

                //Log.d(TAG, e.getMessage());

            }*/

        }

        @Override
        public void bindUI(View convertView) {

            spinner = (Spinner) convertView.findViewById(R.id.spinner);

            spinnerAdapter = new MapAdapter(getContext());

            spinner.setAdapter(spinnerAdapter);

            super.bindUI(convertView);

            button = (Button) convertView.findViewById(R.id.button);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean foreing_online = false;
                    try {
                        foreing_online = formulario.getConfig().getBoolean("online");

                    } catch (Exception e) {
                        e.getStackTrace();
                    }

                    final Campo campo = (Campo) v.getTag();

                    if(foreing_online == true) {

                        Intent it = new Intent(getContext(), ForeingActivity.class);
                        it.putExtra("field", campo.getId());

                        it.putExtra("formId", getFormIdFromLabel(campo.getNome()));

                        ((Activity) getContext()).startActivityForResult(it, 8999);

                    }else {


                        Intent it = new Intent(getContext(), ListEntriesActivity.class);

                        it.putExtra("chooser", true);

                        try {
                            it.putExtra("selected", formularioData.getInt(campo.getId()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        it.putExtra("field", campo.getId());

                        it.putExtra("formId", getFormIdFromLabel(campo.getNome()));

                        ((Activity) getContext()).startActivityForResult(it, 8999);
                    }
                }
            });

        }

    }

    public static int getFormIdFromLabel(String label) {

        int formId = 0;

        Pattern p = Pattern.compile("\\(FORM:[0-9]+]\\)");

        String[] labelSplited = label.split("\\[");

        for (int i = 0; i < labelSplited.length; i++) {

            //Log.d("CLOUD_ERR", labelSplited[i]);

        }

        try {

            String tempFormId = labelSplited[1].replace("]", "").trim();

            formId = Integer.parseInt(tempFormId);

        } catch (Exception e) {

            //Log.d("CLOUD_ERR", label);

            //Log.d("CLOUD_ERR", e.getMessage());

            e.printStackTrace();

            return 0;

        }
        return formId;
    }

    class ViewHolderPicture extends BaseViewHolder{

        ImageView imageView;

        ImageView delete;

        Button button;

        Button buttonImageDownload;

        ProgressBar progressBar;

        TextView placeholder;

        LinearLayout downloadProgress;

        boolean downloading = false;

        public static final String PTAG = "PICTURE_FIELD";

        public static final int RADIUS = 5;

        public static final int THUMB_SIZE = 250;

        @Override
        public void loadData(final Campo campo) {

            super.loadData(campo);

            try {

                if (formularioData.has(campo.getId())) {


                    String val = formularioData.getString(campo.getId());

                    //Log.d(PTAG, campo.getId()+" --> "+val);

                    if(val.equals("")){

                        buttonImageDownload.setVisibility(View.INVISIBLE);

                        delete.setVisibility(View.INVISIBLE);

                        placeholder.setVisibility(View.VISIBLE);

                    }else{

                        placeholder.setVisibility(View.INVISIBLE);

                        delete.setVisibility(View.VISIBLE);

                    }

                }else{

                    placeholder.setVisibility(View.VISIBLE);

                    buttonImageDownload.setVisibility(View.INVISIBLE);

                    delete.setVisibility(View.INVISIBLE);

                }


            }catch (Exception e){

                //Log.d(PTAG, e.getMessage());

            }

            button.setTag(campo);

            buttonImageDownload.setTag(campo);

            buttonImageDownload.callOnClick();

            imageView.setImageBitmap(null);

            imageView.setTag(campo);

            delete.setTag(campo);

            if (formularioData.has(campo.getId())) {

                try {

                    String fName = "";

                    if (formularioData.has(campo.getId())) {

                        fName = formularioData.getString(campo.getId());

                        //Log.d("IMG_FNAME", fName);

                    }

                    if((!fName.startsWith("data"))&&(!fName.equals(""))){

                        File f = new File(fName);

                        if (f.exists()) {

                            buttonImageDownload.setVisibility(View.GONE);

                            formularioData.put(campo.getId(), f.getAbsolutePath());

                            try {

                                new Thread() {

                                    Bitmap t = null;

                                    Bitmap b = null;

                                    @Override
                                    public void run() {
                                        super.run();

                                        try {

                                            t = BitmapFactory.decodeFile(formularioData.getString(campo.getId()));

                                            b = ImagePicker.HDBitmap(t, THUMB_SIZE, RADIUS);

                                            ((Activity) getContext()).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    imageView.setImageBitmap(b);

                                                    placeholder.setVisibility(View.INVISIBLE);

                                                }
                                            });

                                        } catch (Exception e) {

                                            e.printStackTrace();

                                        }

                                    }
                                }.start();

                            } catch (Exception e) {

                                e.printStackTrace();

                                //Log.d("IMG_EXT", e.getMessage());

                            }

                        }else{

                            //Log.d("IMG_NOTEX", fName);

                        }

                    } else {

                        imageView.setImageBitmap(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.cliente));

                        buttonImageDownload.setVisibility(View.GONE);

                    }

                } catch (JSONException e) {

                    e.printStackTrace();

                }

            }else{

                buttonImageDownload.setVisibility(View.GONE);

            }

        }

        @Override
        public void bindUI(final View convertView) {

            super.bindUI(convertView);

            imageView = (ImageView) convertView.findViewById(R.id.imageView);

            delete = (ImageView) convertView.findViewById(R.id.delete);

            button = (Button) convertView.findViewById(R.id.button);

            buttonImageDownload = (Button) convertView.findViewById(R.id.buttonImageDownload);

            buttonImageDownload.setVisibility(View.GONE);

            progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            placeholder = (TextView) convertView.findViewById(R.id.placeholder);

            downloadProgress = (LinearLayout) convertView.findViewById(R.id.downloading);

            downloadProgress.setVisibility(View.INVISIBLE);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final Campo campo = (Campo) view.getTag();

                    final File fl = new File(thumbnailFolder+File.separator+String.valueOf(formulario.getRemoteId())+"_"+String.valueOf(campo.getTipo().getTipo())+"_"+campo.getId()+"_"+String.valueOf(myEntry.getId())+".jpg");

                    if(fl.exists()){

                        try{

                            if(fl.delete()) {

                                ((Activity) getContext()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        notifyDataSetChanged();

                                    }
                                });

                            }

                        }catch (Exception e){

                            e.printStackTrace();

                        }

                    }

                }
            });

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Campo camp = (Campo) v.getTag();

                    try {

                        String data = formularioData.getString(camp.getId());

                        final File fl = new File(thumbnailFolder+File.separator+String.valueOf(formulario.getRemoteId())+"_"+String.valueOf(camp.getTipo().getTipo())+"_"+camp.getId()+"_"+String.valueOf(myEntry.getId())+".jpg");

                        if(!fl.exists()){
                            return;
                        }

                        openGallery(fl.getAbsolutePath());

                    }catch (Exception e){

                        e.printStackTrace();

                    }


                }
            });


            buttonImageDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                final Campo campo = (Campo) v.getTag();

                downloadProgress.setVisibility(View.INVISIBLE);

                final File fl = new File(thumbnailFolder+String.valueOf(formulario.getRemoteId())+"_"+String.valueOf(campo.getTipo().getTipo())+"_"+campo.getId()+"_"+String.valueOf(myEntry.getId())+".jpg");

                new Thread(){
                    @Override
                    public void run() {

                        try {

                            String data = "";

                            try {

                                //Log.d("IMG_DATA", "data = formularioData.getString(campo.getId());");

                                data = formularioData.getString(campo.getId());

                            }catch (Exception e){

                                e.printStackTrace();

                            }


                            if(data.equals("")){

                                //Log.d("IMG_DEBUG", "data is empty");

                                return;
                            }

                            if(data.startsWith("https://")){

                                //Log.d("IMG_DEBUG", data);

                                if(fl.exists()){

                                    //Log.d("IMG_DEBUG", "File: "+fl.getAbsolutePath()+" exists and DATA: "+data);

                                    //Log.d("IMG_DEBUG", "Decoding");

                                    final Bitmap bmp = BitmapFactory.decodeFile(fl.getAbsolutePath());

                                    final Bitmap b = ImagePicker.HDBitmap(bmp, THUMB_SIZE, RADIUS);

                                    ((Activity)getContext()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            imageView.setImageBitmap(b);

                                            //Log.d("IMG_DEBUG", "Bitmap setted");

                                            buttonImageDownload.setVisibility(View.INVISIBLE);

                                            placeholder.setVisibility(View.INVISIBLE);

                                        }
                                    });

                                    //Log.d("IMG_DEBUG", "FROM CACHE");

                                    return;

                                }else{

                                    ((Activity)getContext()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            placeholder.setVisibility(View.VISIBLE);

                                        }
                                    });


                                }

                                //Log.d("IMG_DEBUG", "FROM WEB");

                                data = data+"&__token="+App.getCurrentUser().getUserToken();

                                Boolean hd = App.getSharedPreferences(getContext()).getBoolean("hd_images", false);

                                if(hd){

                                    data+="&hd=1";

                                }

                                if(downloading){

                                    //Log.d("IMG_DEBUG", "Already downloading: "+campo.getId());

                                    ((Activity)getContext()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            downloadProgress.setVisibility(View.VISIBLE);

                                        }
                                    });

                                    return;
                                }else{


                                    ((Activity)getContext()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            downloadProgress.setVisibility(View.GONE);

                                        }
                                    });

                                }

                                downloading = true;

                                ((Activity)getContext()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        downloadProgress.setVisibility(View.VISIBLE);

                                        placeholder.setText(R.string.wait_for_image);

                                    }
                                });

                                final OkHttpClient client = new OkHttpClient.Builder()
                                        .writeTimeout(21, TimeUnit.SECONDS)
                                        .readTimeout(30, TimeUnit.SECONDS).build();

                                final OkHttpClient client2 = new OkHttpClient.Builder()
                                        .writeTimeout(21, TimeUnit.SECONDS)
                                        .readTimeout(30, TimeUnit.SECONDS)
                                        .addNetworkInterceptor(new Interceptor() {
                                            @Override public Response intercept(Interceptor.Chain chain) throws IOException {
                                                Response originalResponse = chain.proceed(chain.request());
                                                return originalResponse.newBuilder()
                                                        .body(new ProgressResponseBody(originalResponse.body(), new ProgressListe(campo) {
                                                            @Override
                                                            public void update(final long bytesRead, final long contentLength, boolean done) {
                                                                ((Activity) getContext()).runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {

                                                                        progressBar.setIndeterminate(false);

                                                                        int max = 100;

                                                                        int progress = Math.round(bytesRead/(contentLength/100));

                                                                        progressBar.setMax(max);

                                                                        progressBar.setProgress(progress);

                                                                        //Log.d("PROGRESS_B", String.valueOf(progress)+"%");

                                                                    }
                                                                });
                                                            }
                                                        }))
                                                        .build();
                                            }
                                        }).build();

                                final Request request = new Request.Builder()
                                        .url(data).build();

                                final Request requestHead = new Request.Builder()
                                        .url(data).head().build();

                                final String test = data;

                                new Thread(){

                                    @Override
                                    public void run() {

                                        Response header = null;

                                        try {

                                            header = client.newCall(request).execute();
                                            // OKHTTP put the length from the header here even though the body is empty
                                            final long size = header.body().contentLength();
                                            header.close();

                                            //Log.d("HTTP_HEADER_LEN", "URL:"+test);

                                            //Log.d("HTTP_HEADER_LEN", "SIZE:"+App.readableFileSize(size));

                                            ((Activity) getContext()).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    placeholder.setText(App.readableFileSize(size));

                                                    placeholder.setVisibility(View.VISIBLE);

                                                }
                                            });

                                        } catch (IOException e) {

                                            if (header!=null) {

                                                header.close();

                                            }

                                            e.printStackTrace();

                                        }

                                        try{

                                            //Log.d("IMG_DEBUG_D", "start downloading "+campo.getId());

                                            Response resp = client2.newCall(request).execute();

                                            InputStream inputStream = resp.body().byteStream();

                                            BufferedInputStream input = new BufferedInputStream(inputStream);

                                            OutputStream output = new FileOutputStream(fl.getAbsolutePath());

                                            byte[] data = new byte[1024];

                                            long total = 0;

                                            int count;

                                            //Log.d("IMG_DEBUG", "Escrevendo..");

                                            while ((count = input.read(data)) != -1) {
                                                total += count;
                                                output.write(data, 0, count);
                                            }

                                            /*String responseString = resp.body().string();

                                            //Log.d("IMG_DEBUG_D", "executed --> "+campo.getId());

                                            responseString = responseString.replace("data:image/jpeg;base64,", "");

                                            responseString = responseString.replace("data:image/png;base64,", "");

                                            //Log.d("DOWNLOAD_IMG", responseString);

                                            //formularioData.put(campo.getId(), responseString);

                                            FileOutputStream fos = new FileOutputStream(fl);

                                            byte[] decodedString = null;*/


                                            //Log.d("IMG_DEBUG_PUT_TO", campo.getId());


                                            ((Activity)(getContext())).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    notifyDataSetChanged();

                                                    downloadProgress.setVisibility(View.INVISIBLE);

                                                    downloading = false;

                                                }
                                            });


                                        }catch(Exception e){

                                            e.printStackTrace();

                                            ((Activity)getContext()).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    buttonImageDownload.setVisibility(View.VISIBLE);
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    downloading = false;
                                                }
                                            });

                                        }

                                        downloading = false;

                                    }

                                }.start();


                            }else {

                                //Log.d("IMG_RESTORE", "Unknown type");

                                buttonImageDownload.setVisibility(View.GONE);

                                placeholder.setVisibility(View.VISIBLE);

                                return;

                            }


                            /*fl.createNewFile();

                            FileOutputStream fos = new FileOutputStream(fl);

                            byte[] decodedString = Base64.decode(realData, Base64.DEFAULT);

                            fos.write(decodedString);

                            fos.flush();

                            fos.close();

                            //Log.d("IMG_PUT_TO", campo.getId());

                            final Bitmap bmp = BitmapFactory.decodeFile(fl.getAbsolutePath());

                            //Log.d("BMP_PROC_X", "Size: "+String.valueOf(bmp.getWidth())+"x"+String.valueOf(bmp.getHeight()));

                            final Bitmap b = ImagePicker.HDBitmap(bmp, THUMB_SIZE, RADIUS);

                            ((Activity)getContext()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    imageView.setImageBitmap(b);

                                    buttonImageDownload.setVisibility(View.INVISIBLE);

                                    placeholder.setVisibility(View.INVISIBLE);

                                }
                            });*/


                        } catch (Exception e) {

                            e.printStackTrace();

                            //Log.d("IMG_EXCEPTION", e.getMessage());

                        }

                        //Log.d("IMG_END","loading");


                    }
                }.start();

                }
            });

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Campo camp = (Campo) v.getTag();

                    String fileName = thumbnailFolder+File.separator+String.valueOf(formulario.getRemoteId())+"_"+String.valueOf(camp.getTipo().getTipo())+"_"+camp.getId()+"_"+String.valueOf(myEntry.getId())+".jpg";

                    final File f = new File(fileName);

                    Intent picker = new Intent(getContext(), PickImageActivity.class);

                    picker.putExtra(PickImageActivity.EXTRA_FILENAME, f.getAbsoluteFile().toString());

                    picker.putExtra(PickImageActivity.EXTRA_FIELD, camp.getId());

                    picker.putExtra(PickImageActivity.EXTRA_WHICH, 0);

                    ((Activity)getContext()).startActivityForResult(picker, PickImageActivity.PICK_IMAGE);

                }
            });

        }

        private class ProgressResponseBody extends ResponseBody {

            private final ResponseBody responseBody;
            private final ProgressListener progressListener;
            private BufferedSource bufferedSource;

            public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
                this.responseBody = responseBody;
                this.progressListener = progressListener;
            }

            @Override public MediaType contentType() {
                return responseBody.contentType();
            }

            @Override public long contentLength() {
                return responseBody.contentLength();
            }

            @Override public BufferedSource source() {
                if (bufferedSource == null) {
                    bufferedSource = Okio.buffer(source(responseBody.source()));
                }
                return bufferedSource;
            }

            private Source source(Source source) {
                return new ForwardingSource(source) {
                    long totalBytesRead = 0L;

                    @Override public long read(Buffer sink, long byteCount) throws IOException {
                        long bytesRead = super.read(sink, byteCount);
                        // read() returns the number of bytes read, or -1 if this source is exhausted.
                        totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                        progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                        return bytesRead;
                    }
                };
            }
        }


        class ProgressListe implements ProgressListener{

            Campo campo;

            ProgressListe(Campo campo){

                this.campo = campo;

            }

            @Override
            public void update(long bytesRead, long contentLength, boolean done) {

            }
        }

    }


    class ViewHolderPhone extends BaseViewHolder{

        AutoCompleteTextView editText;

        Button button;

        @Override
        public void bindUI(View convertView) {

            super.bindUI(convertView);

            editText = (AutoCompleteTextView) convertView.findViewById(R.id.editText);

            button = (Button) convertView.findViewById(R.id.button);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent it = new Intent((Activity)getContext(),PhoneActivity.class);

                    Campo camp = (Campo) view.getTag();

                    it.putExtra("title", camp.getNome());

                    it.putExtra("field", camp.getId());

                    try {
                        it.putExtra("value", formularioData.getString(camp.getId()));
                    } catch (JSONException e) {

                        it.putExtra("value", "");
                    }

                    ((Activity)getContext()).startActivityForResult(it, 543);

                }
            });

        }

        @Override
        public void loadData(Campo campo) {

            super.loadData(campo);

            if (campo == null) {

                //Log.d("FORMU.NULL", "Campo == null");

                return;

            }

            button.setEnabled(!campo.isReadOnly());
            button.setClickable(!campo.isReadOnly());

            String[] opcoes = new String[]{};

            String tpInput = "";

            try {

                tpInput = campo.getNome().split("\\(")[1].replace(")", "").toUpperCase();
                String tpName = campo.getNome().split("\\(")[0];

                label.setText(tpName);

            }catch (Exception e){

                //Log.d("FORMU.EXCEPTION", e.getMessage());

                e.printStackTrace();

            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, opcoes);

            editText.setAdapter(adapter);

            //Log.d("FORMU.EXCEPTION", campo.getNome());

            editText.setTag(campo);

            button.setTag(campo);

            if (formularioData.has(campo.getId())) {

                try {


                    String val = formularioData.getString(campo.getId());

                    if(val.toUpperCase().equals("NULL")){

                        val = "";

                    }

                    button.setText(format(val));

                    if(val.equals("")){
                        button.setText("Digitar numero");
                    }



                } catch (JSONException e) {

                    //Log.d("FORMU", e.getMessage());

                    e.printStackTrace();

                }

            } else {


                button.setText("Digitar numero");

                editText.setText("");

                try {

                    formularioData.put(campo.getId(), "");

                } catch (Exception e) {

                    e.printStackTrace();

                }

            }

        }
    }

    class ViewHolderSignPad extends BaseViewHolder{

        SignaturePad signaturePad;
        Button clearButton;
        String fName;

        @Override
        public void loadData(final Campo campo) {
            super.loadData(campo);

            signaturePad.setTag(campo);

            clearButton.setTag(campo);

            signaturePad.clear();

            if (formularioData.has(campo.getId())) {

                fName = "";

                try {

                    fName = formularioData.getString(campo.getId());

                    //Log.d("SIGN_FILE_DATA", fName);

                    if(fName.equals("")) return;

                    new Thread(){

                        public void run() {

                            //Log.d("SIGN_FILE_LOAD", "run();");

                            try {

                                String value = formularioData.getString(campo.getId()).replace("data:image/jpeg;base64,", "").replace("data:image/png;base64,", "");

                                //Log.d("SIGN_FILE_LOAD", value);

                                byte[] decoded = Base64.decode(value, Base64.DEFAULT);

                                final Bitmap b = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

                                ((Activity)getContext()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        //Log.d("SIGN_FILE_LOAD", "runUIThread();");

                                        try {

                                            signaturePad.setSignatureBitmap(b);

                                        }catch (Exception e){

                                            e.printStackTrace();

                                        }
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                    }.start();

                    //Log.d("SIGN_FILE","End loading");

                } catch (Exception e) {

                    //Log.d("SIGN_PAD_EXCEPTION", e.getMessage());

                }

            }

            //Log.d("SIGNPAD", "loadData");

        }

        @Override
        public void bindUI(View convertView) {

            super.bindUI(convertView);

            //Log.d("SIGNPAD", "bindUI");

            signaturePad = (SignaturePad) convertView.findViewById(R.id.signature_pad);

            clearButton = (Button) convertView.findViewById(R.id.clearButton);

            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                Campo campo = (Campo) v.getTag();

                signaturePad.clear();

                try {
                    formularioData.put(campo.getId(), "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                }
            });

            signaturePad.setOnSignedListener(new CustomSignatureListener(signaturePad) {
                @Override
                public void onSigned() {
                    super.onSigned();

                    final Campo campo = (Campo) signaturePad.getTag();


                    try {

                        new Thread(){
                            @Override
                            public void run() {

                            Bitmap bmp = signaturePad.getSignatureBitmap();

                            try {

                                formularioData.put(campo.getId(), App.getEncoded64ImageStringFromBitmap(bmp));

                                /*((FormularioActivity)(getContext())).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((FormularioActivity)(getContext())).saveNow();
                                    }
                                });*/

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            }
                        }.start();

                    } catch (Exception e) {

                        //Log.d("SIGN_PAD_EXCEPTION", e.getMessage());

                    }
                }
            });


        }
    }

    class ViewHolderGPS extends BaseViewHolder{

        Button buttonGps;
        GoogleApiClient client;
        View.OnClickListener myOnClickListener;

        @Override
        public void loadData(Campo campo) {
            super.loadData(campo);

            buttonGps.setTag(campo);

            buttonGps.setText(getContext().getResources().getString(R.string.gps_default_text));

            if (formularioData.has(campo.getId())) {

                try {
                    buttonGps.setText("GPS:" + formularioData.getString(campo.getId()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

        @Override
        public void bindUI(View convertView) {
            super.bindUI(convertView);

            buttonGps = (Button) convertView.findViewById(R.id.buttonGps);

            buttonGps.setText(getContext().getResources().getString(R.string.gps_default_text));

            myOnClickListener= new View.OnClickListener() {

                @Override
                public void onClick(final View v) {

                    final Campo campo = (Campo) v.getTag();

                    ((Button)v).setText("Capturando..");

                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getContext(), "Aguarde..", Toast.LENGTH_SHORT).show();
                        }
                    });

                    client = new GoogleApiClient.Builder(getContext())
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {

                                Toast.makeText(getContext(), getContext().getResources().getString(R.string.loading), Toast.LENGTH_LONG).show();

                                LocationRequest request = LocationRequest.create();

                                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                request.setInterval(1);
                                request.setFastestInterval(1);

                                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }

                                LocationServices.FusedLocationApi.requestLocationUpdates(client, request, new com.google.android.gms.location.LocationListener() {

                                    @Override
                                    public void onLocationChanged(Location location) {

                                    Toast.makeText(getContext(),
                                        "Latitude:" + String.valueOf(location.getLatitude()) + "\n" +
                                        "Longitude:" + String.valueOf(location.getLongitude())
                                        , Toast.LENGTH_LONG).show();

                                    LocationServices.FusedLocationApi.removeLocationUpdates(client, this);

                                    try {
                                        formularioData.put(campo.getId(), String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude()));

                                        ((Button)v).setText(formularioData.getString(campo.getId()));

                                        v.setOnClickListener(myOnClickListener);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                    }
                                });

                            }

                            @Override
                            public void onConnectionSuspended(int i) {

                            }
                        })
                        .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(ConnectionResult connectionResult) {
                                Toast.makeText(getContext(), "Failed to connect to google", Toast.LENGTH_LONG).show();
                            }
                        })
                        .build();

                    client.connect();

                }
            };

            buttonGps.setOnClickListener(myOnClickListener);

        }
    }



    class ViewHolderPayment extends BaseViewHolder{

        Button buttonPay;
        JSONObject config;
        String text1 = "";
        String cpf1 = "";
        @Override
        public void bindUI(View convertView) {

            super.bindUI(convertView);
            buttonPay = (Button) convertView.findViewById(R.id.buttonPay);
            config = formulario.getConfig();



            buttonPay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                         text1 = config.getString("text1");
                    }catch (JSONException e){
                        e.printStackTrace();
                    }


                    if (formularioData.has(text1)) {

                        final Campo campo = (Campo) v.getTag();
                        Boolean existe = false;
                        try {
                            JSONObject jsonObject = new JSONObject(formularioData.getString(campo.getId()));
                            if(jsonObject.getString("parcelas") != ""){
                                existe = true;
                            }
                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                        if (existe == false) {

                            Intent it = new Intent(getContext(), InputDecimalPaymentActivity.class);

                            it.putExtra("field", campo.getId());

                            it.putExtra("formId", formulario.getRemoteId());

                            it.putExtra("label", campo.getNome());

                            try {
                                it.putExtra("nome", formularioData.getString(text1));

                                if (formularioData.has("cpf1")) {
                                    cpf1 = formularioData.getString("cpf1");
                                }

                                it.putExtra("documento", cpf1);

                                //it.putExtra("formIdForeing", formularioData.getString("formIdForeing"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                it.putExtra("value", formularioData.getString(campo.getId()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            ((Activity) getContext()).startActivityForResult(it, FormularioActivity.TYPE_PAYMENT_RESULT);
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(getInstance());

                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case DialogInterface.BUTTON_POSITIVE:
                                            JSONObject transactionPostFormData = new JSONObject();
                                            try {
                                                JSONObject jsonObject = new JSONObject(formularioData.getString(campo.getId()));
                                                transactionPostFormData.put("api_key", BuildConfig.API_KEY);
                                                transactionPostFormData.put("id", jsonObject.getString("id"));

                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                            CloudCRMAPI cloudCRMAPI = new CloudCRMAPI("estorno", transactionPostFormData.toString());
                                            cloudCRMAPI.makeCall(new CloudCRMAPI.OnFinish() {
                                                @Override
                                                public void onSuccess(final JSONObject result) {
                                                    JSONObject loginResultObject = result;

                                                        getInstance().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                JSONObject loginResultObject = result;
                                                                try {
                                                                    new android.support.v7.app.AlertDialog.Builder(getContext())
                                                                            .setTitle("ATENÇÃO").setMessage(loginResultObject.getString("error")).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                                            dialogInterface.dismiss();

                                                                        }
                                                                    }).show();
                                                                } catch (Exception e) {
                                                                    new android.support.v7.app.AlertDialog.Builder(getContext())
                                                                            .setTitle("ATENÇÃO").setMessage(R.string.aviso_estorno_sucesso).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                            try {
                                                                                JSONObject jsonObject = new JSONObject(formularioData.getString(campo.getId()));
                                                                                jsonObject.put("estornado",true);
                                                                                formularioData.put(campo.getId(), jsonObject);
                                                                                buttonPay.setText("Estornado # " + jsonObject.getString("parcelas") + "X DE " + jsonObject.getString("v2"));
                                                                            } catch (Exception e) {
                                                                                e.getStackTrace();
                                                                            }


                                                                        }
                                                                    }).show();
                                                                }

                                                            }
                                                        });

                                                }
                                                @Override
                                                public void onError(final Exception e) {

                                                    getInstance().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            new android.support.v7.app.AlertDialog.Builder(getContext())
                                                                    .setTitle("ATENÇÃO").setMessage(e.getMessage()).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                    dialogInterface.dismiss();
                                                                }
                                                            }).show();
                                                        }
                                                    });
                                                }
                                            });
                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            dialog.dismiss();
                                            break;
                                    }
                                }
                            };


                            builder.setTitle("ATENÇÃO").setMessage(R.string.aviso_estorno).setPositiveButton("Yes", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener).setCancelable(false).show();



                        }

                    }else{
                        new android.support.v7.app.AlertDialog.Builder(getContext())
                                .setTitle("ATENÇÃO").setMessage(R.string.aviso_cliente).setPositiveButton("ok", new DialogInterface.OnClickListener() {
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
        public void loadData(Campo campo) {

            super.loadData(campo);

            if (campo == null) {

                //Log.d("FORMU.NULL", "Campo == null");

            }

            buttonPay.setTag(campo);
            buttonPay.setText(getContext().getString(R.string.payment_default_text));
            buttonPay.setEnabled(true);

            //Log.d("FORMU.IF", "if(formularioData.has('" + campo.getId() + "'))");
            buttonPay.setText("REALIZAR PAGAMENTO");

            if (formularioData.has(campo.getId())) {
                try {
                    JSONObject jsonObject = new JSONObject(formularioData.getString(campo.getId()));

                        //Log.d("dados_form_payment", formularioData.getString(campo.getId()));
                        String val = jsonObject.getString("v2");
                        String status = getContext().getString(getContext().getResources().getIdentifier(jsonObject.getString("status"), "string", getContext().getPackageName()));
                        if(jsonObject.has("estornado")) {
                            status = "estornado";
                            buttonPay.setEnabled(false);
                        }
                        buttonPay.setText(status + "# " + jsonObject.getString("parcelas") + "X DE " + val);


                } catch (JSONException e) {
                    //Log.d("FORMU", e.getMessage());
                    e.printStackTrace();
                }
            }

        }

    }




    class ViewHolderCustom extends BaseViewHolder{

        Button buttonCus;



        @Override
        public void loadData(Campo campo) {

            super.loadData(campo);

            if (campo == null) {

                //Log.d("FORMU.NULL", "Campo == null");

            }

            buttonCus.setTag(campo);

            //Log.d("FORMU.JSON", formularioData.toString());
            //Log.d("FORMU.IF", "if(formularioData.has('" + campo.getId() + "'))");
            buttonCus.setText("0 Item(s)");
            JSONObject dataCustom = null;
            if (formularioData.has(campo.getId())) {

                try {
                    dataCustom = new JSONObject(formularioData.getString(campo.getId()));

                    buttonCus.setText(String.valueOf(dataCustom.getJSONArray("dados").length()) + " Item(s)");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void bindUI(View convertView) {

            super.bindUI(convertView);
            buttonCus = (Button) convertView.findViewById(R.id.buttonCus);


            buttonCus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                        Campo campo = (Campo) v.getTag();

                        String label = campo.getNome();

                        int formId = getFormIdFromLabel(label);
                        //Log.d("formId_custom",String.valueOf(formId));
                        try {
                            Intent it = new Intent(getContext(),CustomActivity.class);
                            it.putExtra("field", campo.getId());
                            it.putExtra("formId", formulario.getRemoteId());
                            if (formularioData.has(campo.getId())) {
                                it.putExtra("data",formularioData.getString(campo.getId()));
                            }
                            it.putExtra("CustomId",formId);
                            ((Activity) getContext()).startActivityForResult(it, FormularioActivity.TYPE_CUSTOM_RESULT);

                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                }
            });


        }




    }


    public class ViewHolderSaveSend extends BaseViewHolder{
        Button buttonSave;
        Button buttonSend;
        @Override
        public void bindUI(View convertView) {
            buttonSave = (Button) convertView.findViewById(R.id.buttonSalvar);
            buttonSend = (Button) convertView.findViewById(R.id.buttonSend);
            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                ((Activity)getContext()).findViewById(R.id.saveFormulario).callOnClick();
                }
            });
            buttonSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                ((Activity)getContext()).findViewById(R.id.sendEmail).callOnClick();
                }
            });
        }
    }

    public class MapItem{

        String display;

        String value;

        public void setValue(String value) {
            this.value = value;
        }

        public void setDisplay(String display) {
            this.display = display;
        }

        public String getValue() {
            return value;
        }

        public String getDisplay() {
            return display;
        }
    }

    public class MapAdapter extends ArrayAdapter<MapItem>{


        public MapAdapter(Context context, int resource) {
            super(context, resource);
        }

        public MapAdapter(Context context) {
            super(context, R.layout.entry_layout);
        }

        public MapItem getItemByVal(String value){

            MapItem mapItem = new MapItem();

            for(int i = 0; i < getCount(); i++){

                MapItem tmp = getItem(i);

                if(tmp.value.equals(value)){

                    return tmp;

                }

            }

            return mapItem;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){

                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = layoutInflater.inflate(R.layout.entry_layout, parent, false);
            }

            TextView tv = (TextView) convertView.findViewById(R.id.titleTextView);

            tv.setText(getItem(position).display);

            return convertView;
        }


    }

    public void openGallery(String defaultFile){

        //Log.d("GALLERY", "openGallery");

        final Intent it = new Intent(getContext(), GalleryActivity.class);

        it.putExtra("DEFAULT_FILE", defaultFile);

        ArrayList<String> files = new ArrayList<>();

        ArrayList<String> titles = new ArrayList<>();

        try{


            for(Campo campo: formulario.campos){

                if(!campo.getTipo().getTipo().equals(Formulario.TYPE_PICTURE)){
                    continue;
                }

                String fileName = thumbnailFolder+String.valueOf(formulario.getRemoteId())+"_picture_"+campo.getId()+"_"+String.valueOf(myEntry.getId())+".jpg";

                File f = new File(fileName);

                if(f.exists()){

                    files.add(f.getAbsolutePath());

                    titles.add(campo.getNome());

                }

            }

            String[] fs = new String[files.size()];

            String[] ts = new String[titles.size()];

            files.toArray(fs);

            titles.toArray(ts);

            it.putExtra("FILES", fs);

            it.putExtra("TITLES", ts);

            ((Activity)getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    getContext().startActivity(it);

                }
            });

        }catch (Exception e){

            e.printStackTrace();

        }

    }


    interface ProgressListener {
        void update(long bytesRead, long contentLength, boolean done);
    }

}
