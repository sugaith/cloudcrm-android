package app.cloudcrm.tech.cloudcrm.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by albertomiranda on 6/12/17.
 */
@DatabaseTable(tableName = "cidade")
public class Cidade {

    @DatabaseField
    String nome;

    public String getNome() {
        return nome;
    }
}
