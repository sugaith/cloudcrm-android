package app.cloudcrm.tech.cloudcrm.daos;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

import app.cloudcrm.tech.cloudcrm.models.Cidade;
import app.cloudcrm.tech.cloudcrm.models.Custom;

/**
 * Created by albertomiranda on 6/12/17.
 */




public class CidadeDAO extends BaseDaoImpl<Cidade, Integer> {

    public CidadeDAO(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Cidade.class);

        setConnectionSource(connectionSource);

        initialize();

    }
}
