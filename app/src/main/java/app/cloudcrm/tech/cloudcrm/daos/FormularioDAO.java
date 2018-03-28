package app.cloudcrm.tech.cloudcrm.daos;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

import app.cloudcrm.tech.cloudcrm.models.Formulario;

/**
 * Created by Alberto on 17/6/2016.
 */
public class FormularioDAO extends BaseDaoImpl<Formulario, Integer> {
    public FormularioDAO(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Formulario.class);
    }
}
