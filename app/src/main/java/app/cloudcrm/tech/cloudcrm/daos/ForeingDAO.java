package app.cloudcrm.tech.cloudcrm.daos;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import app.cloudcrm.tech.cloudcrm.models.Foreing;

/**
 * Created by gustavojunior on 26/10/17.
 */

public class ForeingDAO extends BaseDaoImpl<Foreing, Integer> {

    public ForeingDAO(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Foreing.class);

        setConnectionSource(connectionSource);

        initialize();

    }
}
