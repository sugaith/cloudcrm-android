package app.cloudcrm.tech.cloudcrm.daos;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

import app.cloudcrm.tech.cloudcrm.models.Custom;
import app.cloudcrm.tech.cloudcrm.models.Usuario;

/**
 * Created by gustavojunior on 26/05/17.
 */

public class CustomDAO extends BaseDaoImpl<Custom, Integer> {

    public CustomDAO(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Custom.class);

        setConnectionSource(connectionSource);

        initialize();

    }
}
