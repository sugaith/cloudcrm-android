package app.cloudcrm.tech.cloudcrm.daos;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

import app.cloudcrm.tech.cloudcrm.models.EntryFiles;

/**
 *
 *          Created by Alberto on 23/6/2016.
 *
 */
public class EntryFilesDAO extends BaseDaoImpl<EntryFiles, Integer> {

    public EntryFilesDAO(ConnectionSource connectionSource) throws SQLException {

        super(connectionSource, EntryFiles.class);

        setConnectionSource(connectionSource);

        initialize();

    }

}
