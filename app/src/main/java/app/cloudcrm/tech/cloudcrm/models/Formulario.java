package app.cloudcrm.tech.cloudcrm.models;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import app.cloudcrm.tech.cloudcrm.classes.App;
import app.cloudcrm.tech.cloudcrm.forms.Campo;
import app.cloudcrm.tech.cloudcrm.forms.FormularioAdapterConstants;
import app.cloudcrm.tech.cloudcrm.forms.Tipo;

/**
 * Created by Alberto on 14/6/2016.
 */
@DatabaseTable(tableName = "formularios")
public class Formulario extends FormularioAdapterConstants{

    @DatabaseField(generatedId = true)
    int id = 0;
    @DatabaseField
    int remoteId = 0;
    @DatabaseField
    String nome;
    @DatabaseField
    String title;
    @DatabaseField
    String subtitle;
    @DatabaseField
    String camposJson;
    @DatabaseField
    int ownerId;
    @DatabaseField
    String url;
    @DatabaseField
    String config;
    @DatabaseField
    String payment;
    // Static

    public final static String IS_FOREING_PATTERN = ".*\\[\\d+\\].*";

    public JSONObject getConfig(){
        try{

            //Log.d("FORM_CONFIG", "Config:"+config);

            if(config == null) config = "{}";

            return new JSONObject(config);
        }catch (Exception e){
            e.printStackTrace();
            return new JSONObject();
        }
    }

    public void setConfig(JSONObject config){

        //Log.d("FORM_CONFIG", "setConfig:"+config.toString());

        this.config = config.toString();

    }


    public JSONArray getPayment(){
        try{

            //Log.d("FORM_PAYMENT", "payment:"+payment);

            if(payment == null) payment = "{}";

            return new JSONArray(payment);
        }catch (Exception e){
            e.printStackTrace();
            return new JSONArray();
        }
    }

    public void setPayment(JSONArray payment){

        //Log.d("FORM_PAYMENT", "setPayment:"+payment.toString());

        this.payment = payment.toString();

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<Campo> campos;

    JSONObject data;

    public Formulario(){

        campos = new ArrayList<Campo>();

        data = new JSONObject();

    }

    public Formulario(int id, String nome) {

        this.id = id;

        this.nome = nome;

        campos = new ArrayList<Campo>();

        data = new JSONObject();

    }

    public Campo addCampo(String id, String nome, String tipo){

        //Log.d("ADDCAMPO", "ID -> "+String.valueOf(id));

        //Log.d("ADDCAMPO", "NOME -> " + nome);

        //Log.d("ADDCAMPO", "TIPO -> " + tipo);

        Campo campo = new Campo(id, nome, getTipoByNome(tipo));

        campos.add(campo);

        return campo;

    }

    public static ArrayList<Formulario> createFromJson(String json){

        ArrayList<Formulario> formularios = new ArrayList<Formulario>();

        try {

            JSONObject todosOsFormularios = new JSONObject(json);

            JSONArray jsonFormulariosArray = todosOsFormularios.getJSONArray("forms");



            for (int c = 0; c < jsonFormulariosArray.length(); c++) {

                Formulario newFormulario = new Formulario();

                try {


                    JSONObject obj = jsonFormulariosArray.getJSONObject(c);

                    //Log.d("FORM_CONFIG", newFormulario.getConfig().toString());

                    List<Formulario> fs = App.formularios.queryForEq("remoteId", obj.getInt("id"));

                    if(fs.size()>0){

                        newFormulario = fs.get(0);

                        if(obj.getBoolean("delete")){

                            App.formularios.delete(newFormulario);

                            continue;

                        }

                    }

                    if(obj.getBoolean("delete"))

                        continue;

                    if(obj.getInt("visible") == 0){

                        //Log.d("RECEIVER.REMO", newFormulario.getNome());

                        App.formularios.delete(newFormulario);

                        continue;

                    }

                    if(obj.has("config")){

                        newFormulario.setConfig(obj.getJSONObject("config"));

                    }else {

                        //Log.d("FORM_CONFIG", "no(config)");

                        newFormulario.setConfig(new JSONObject());

                    }

                    if(todosOsFormularios.has("payment")){

                        newFormulario.setPayment(todosOsFormularios.getJSONArray("payment"));

                    }

                    newFormulario.setRemoteId(obj.getInt("id"));

                    newFormulario.setNome(obj.getString("nome"));

                    JSONArray jCampos = obj.getJSONArray("campos");

                    newFormulario.setTitle(obj.getString("title"));

                    newFormulario.setSubtitle(obj.getString("subtitle"));

                    newFormulario.setOwnerId(obj.getInt("ownerId"));

                    newFormulario.setCamposJson(jCampos.toString());





                    if(obj.has("url")){

                        newFormulario.setUrl(obj.getString("url"));

                    }else{

                        newFormulario.setUrl("");

                    }

                    if(newFormulario.getId()>0){

                        App.formularios.update(newFormulario);

                    }else{

                        App.formularios.create(newFormulario);

                    }

                    newFormulario.generateCamposFromJson();


                } catch (Exception e) {

                }

                formularios.add(newFormulario);

            }



        }catch(Exception ex){

        }

        return formularios;

    }

    public void generateCamposFromJson() {

        try {

            JSONArray jCampos = new JSONArray(camposJson);

            for (int i = 0; i < jCampos.length(); i++) {

                try {

                    JSONObject jCampo = jCampos.getJSONObject(i);

                    Campo temp = new Campo(jCampo.getString("id"), jCampo.getString("nome"), getTipoByNome(jCampo.getString("tipo")));

                    try {

                        temp.setHidden(jCampo.getBoolean("hidden"));

                        temp.setRequired(jCampo.getBoolean("required"));

                        temp.setReadOnly(jCampo.getBoolean("readonly"));

                    }catch (Exception e){

                        e.printStackTrace();

                        //Log.d("REQUIRED->", e.getMessage());

                    }

                    if(jCampo.getString("tipo").equals(TYPE_SELECT)){

                        JSONArray opts = jCampo.getJSONArray("options");

                        temp.addOption("");

                        for(int t = 0; t < opts.length(); t++){

                            //Log.d("OPTIONS", opts.getString(t));

                            temp.addOption(opts.getString(t));

                        }

                    }

                    campos.add(temp);

                } catch (Exception e) {

                    e.printStackTrace();

                }

            }

        }catch (Exception e){

            e.printStackTrace();

        }

        Campo save = new Campo();

        save.setId("save");

        save.setNome("none");

        save.setReadOnly(true);

        save.setHidden(false);

        save.setRequired(false);

        save.setTipo(getTipoByNome(TYPE_SAVE_N_SEND));

        campos.add(save);

    }


    public static Tipo getTipoByNome(String nome){

        for(Tipo tipo : tipos){

            if(tipo.getTipo().equals(nome)){
                return tipo;
            }

        }

        return null;

    }

    public JSONObject getObject(){

        return data;

    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

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

    public int getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(int remoteId) {
        this.remoteId = remoteId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCamposJson() {
        return camposJson;
    }

    public void setCamposJson(String camposJson) {
        this.camposJson = camposJson;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public ArrayList<Campo> getCampos() {
        return campos;
    }
}
