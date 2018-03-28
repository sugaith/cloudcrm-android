package app.cloudcrm.tech.cloudcrm.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Alberto on 22/6/2016.
 */
@DatabaseTable(tableName = "Usuario")
public class Usuario {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private int remoteId;
    @DatabaseField
    private String userToken;
    @DatabaseField
    private String userName;
    @DatabaseField
    private String companyName;
    @DatabaseField
    private String companyColor;
    @DatabaseField
    private String email;
    @DatabaseField
    private int ownerId;
    @DatabaseField
    private boolean logged = false;

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(int remoteId) {
        this.remoteId = remoteId;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getCompanyColor() {
        return companyColor;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setCompanyColor(String companyColor) {
        this.companyColor = companyColor;
    }
}
