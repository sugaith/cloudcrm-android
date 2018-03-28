package app.cloudcrm.tech.cloudcrm.classes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.daos.CidadeDAO;

/**
 * Created by albertomiranda on 6/12/17.
 */

public class CidadesDatabase extends OrmLiteSqliteOpenHelper {

    Context context;

    ConnectionSource connectionSource;

    SQLiteDatabase db;

    public CidadeDAO cidades;

    static String databaseName = Environment.getExternalStorageDirectory()+ File.separator+".cloudcrm"+File.separator+"cidades.db";

    static final int VERSION = 1122;

    public CidadesDatabase(Context context) {

        super(context, databaseName, null, VERSION);

        //Log.d("DB2_LOG", "Create cidades database");

        this.context = context;

        /*if(!new File(databaseName).exists()){
            //Log.d("DB2_LOG", "Doesnt exists");
            try {
                copy(new File(databaseName));
            } catch (IOException e) {
                //Log.d("DB2_LOG", "Not loaded:"+e.getMessage());
                e.printStackTrace();
            }
        }*/

        this.connectionSource = getConnectionSource();

        this.db = this.getWritableDatabase();

        //Log.d("DB2_LOG", "Type:"+this.connectionSource.getDatabaseType().getDatabaseName());


        try {

           // db.execSQL("select * from cidade");

            //Log.d("DB2_LOG", "Create dao");

            cidades = new CidadeDAO(connectionSource);

            //Log.d("DB2_LOG", "Create dao ok");

        } catch (Exception e) {

            //Log.d("DB2_LOG", "Exception x:"+e.getMessage());

            e.printStackTrace();

        }

    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {

        //Log.d("DB2_LOG", "Create ...");

        try {

            copy(new File(databaseName));
        } catch (IOException e) {
            //Log.d("DB2_LOG", "Exception: "+e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

        //Log.d("DB2_LOG", "Upgrade ...");

        onCreate(database, connectionSource);
    }


    public void copy(File dst) throws IOException {

        InputStream in = context.getResources().openRawResource(R.raw.mydb);

        //Log.d("DB2_LOG", "Copy to: "+dst.getAbsolutePath());

        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } catch (Exception e) {
                //Log.d("DB2_LOG", "Cant write:"+e.getMessage());
            } finally {
                out.close();
            }
        }catch (Exception e){
            //Log.d("DB2_LOG", "Err.."+e.getMessage());
        } finally {
            in.close();
        }
    }
}
