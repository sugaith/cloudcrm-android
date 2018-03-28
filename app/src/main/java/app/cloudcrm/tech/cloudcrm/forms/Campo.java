package app.cloudcrm.tech.cloudcrm.forms;

import android.view.View;

import java.util.ArrayList;

/**
 * Created by Alberto on 14/6/2016.
 */
public class Campo{

    String id = "none";

    String nome;

    Tipo tipo;

    ArrayList<String> options;

    boolean hidden = false;

    boolean required = false;

    boolean readOnly = false;

    boolean visible = false;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Campo() {

    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Campo(String id, String nome, Tipo tipo) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.options = new ArrayList<String>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public void addOption(String option){

        options.add(option);

    }

}
