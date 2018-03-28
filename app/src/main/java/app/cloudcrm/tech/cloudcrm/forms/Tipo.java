package app.cloudcrm.tech.cloudcrm.forms;

/**
 * Created by Alberto on 14/6/2016.
 */
public class Tipo{

    int resource;

    int id;

    String tipo;

    public Tipo(int resource, int id, String tipo) {
        this.resource = resource;
        this.id = id;
        this.tipo = tipo;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

}
