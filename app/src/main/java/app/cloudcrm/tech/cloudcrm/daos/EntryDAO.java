package app.cloudcrm.tech.cloudcrm.daos;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

import app.cloudcrm.tech.cloudcrm.models.Entry;

/**
 * Created by Alberto on 17/6/2016.
 */
public class EntryDAO extends BaseDaoImpl<Entry, Integer> {
    public EntryDAO(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Entry.class);
    }
}
