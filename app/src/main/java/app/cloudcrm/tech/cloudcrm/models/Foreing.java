package app.cloudcrm.tech.cloudcrm.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by gustavojunior on 26/10/17.
 */


@DatabaseTable(tableName = "foreing")
public class Foreing {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private String nome;
    @DatabaseField
    private int foreingId;
    @DatabaseField
    private int formId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String data) {
        this.nome = data;
    }

    public int getForeingId() {
        return foreingId;
    }

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public void setForeingId(int foreingId) {
        this.foreingId = foreingId;
    }

}
