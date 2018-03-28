package app.cloudcrm.tech.cloudcrm.daos;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

import app.cloudcrm.tech.cloudcrm.models.Usuario;

/**
 * Created by Alberto on 22/6/2016.
 */
public class UsuarioDAO extends BaseDaoImpl<Usuario, Integer> {

    public UsuarioDAO(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Usuario.class);

        setConnectionSource(connectionSource);

        initialize();

    }
}
