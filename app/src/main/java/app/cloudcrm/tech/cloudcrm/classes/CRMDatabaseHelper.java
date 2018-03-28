package app.cloudcrm.tech.cloudcrm.classes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.File;

import app.cloudcrm.tech.cloudcrm.models.Formulario;
import app.cloudcrm.tech.cloudcrm.models.Entry;
import app.cloudcrm.tech.cloudcrm.models.EntryFiles;
import app.cloudcrm.tech.cloudcrm.models.Usuario;

/**
 * Created by Alberto on 6/6/2016.
 *
 */
public class CRMDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String databasename = "cloudcrm.db";
    //private static final String databasename = Environment.getExternalStorageDirectory() + File.separator + "cloudcrm.db";

    private static final int databaseVersion = 900;

    private Context context;

    public CRMDatabaseHelper(Context context){

        super(context, databasename, null, databaseVersion);

        this.context = context;

    }

    public CRMDatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {

        super(context, databaseName, factory, databaseVersion);

        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {

        try {

            TableUtils.createTable(connectionSource, Entry.class);
            TableUtils.createTable(connectionSource, EntryFiles.class);
            TableUtils.createTable(connectionSource, Formulario.class);
            TableUtils.createTable(connectionSource, Usuario.class);

        }catch (Exception e){

            //Log.d("DATABASE_ERROR", e.getMessage());

        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

        try {

            TableUtils.dropTable(connectionSource, Entry.class, true);

            TableUtils.createTable(connectionSource, Entry.class);


        }catch (Exception e) {

            e.printStackTrace();

        }


        try{ TableUtils.dropTable(connectionSource, Formulario.class, true);
        }catch (Exception e) {}

        try{ TableUtils.createTable(connectionSource, Formulario.class);
        }catch (Exception e) {}

        try{ TableUtils.dropTable(connectionSource, EntryFiles.class, true);
        }catch (Exception e) {}

        try{ TableUtils.createTable(connectionSource, EntryFiles.class);
        }catch (Exception e) {}

        try{ TableUtils.dropTable(connectionSource, Usuario.class, true);
        }catch (Exception e) {}

        try{ TableUtils.createTable(connectionSource, Usuario.class);
        }catch (Exception e) {}

    }
}
