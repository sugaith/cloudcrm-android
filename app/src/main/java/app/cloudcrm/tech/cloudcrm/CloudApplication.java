package app.cloudcrm.tech.cloudcrm;

import android.app.Application;
import android.util.Log;

/**
 * Created by Alberto on 10/6/2016.
 */
public class CloudApplication extends Application {

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        //Log.d("CloudApp", "onLowMemory called..");

    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        //Log.d("CloudApp", "onTrimMemory called .. level:"+String.valueOf(level));

    }
}
