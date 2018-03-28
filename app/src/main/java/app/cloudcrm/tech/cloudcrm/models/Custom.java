package app.cloudcrm.tech.cloudcrm.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by gustavojunior on 26/05/17.
 */


@DatabaseTable(tableName = "custon")
public class Custom {
        @DatabaseField(generatedId = true)
        private int id;
        @DatabaseField
        private String data;
        @DatabaseField
        private int entryId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

}
