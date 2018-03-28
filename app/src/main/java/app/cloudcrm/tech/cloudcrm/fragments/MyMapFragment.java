package app.cloudcrm.tech.cloudcrm.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Alberto on 6/12/2016.
 */

public class MyMapFragment extends SupportMapFragment {

    public MyMapFragment(){
        //Log.d("MAP_ACT", "new MyMapFragment()");
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {

        //Log.d("MAP_ACT", "Test");

        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }
}
