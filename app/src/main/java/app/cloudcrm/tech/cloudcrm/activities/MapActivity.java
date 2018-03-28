package app.cloudcrm.tech.cloudcrm.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;
import com.loopj.android.airbrake.AirbrakeNotifier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.cloudcrm.tech.cloudcrm.R;
import app.cloudcrm.tech.cloudcrm.classes.App;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "MAP_ACT";

    // IO

    JSONObject jsonObject = new JSONObject();

    String stringJson = "{}";

    // Interface

    GoogleMap gMap;

    Polygon polygon = null;

    double area;

    TextView textView;

    FrameLayout frameLayout;

    PolygonOptions polygonOptions = null;

    ArrayList<LatLng> latLngArrayList = new ArrayList<LatLng>();

    ArrayList<Marker> markers = new ArrayList<>();

    PlaceAutocompleteFragment autocompleteFragment;

    GoogleApiClient googleApiClient;

    Button button;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_map, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menuScreen){

            take();

        }else {

            finish();

        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AirbrakeNotifier.register(this, "c9c2e69d0fc6ec95ed03f201aa124902");
        setContentView(R.layout.activity_map);

        button = (Button) findViewById(R.id.buttonSave);

        button.setOnClickListener(this);

        App.setActionBarColor(this);

        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentManager nfragmentManager = getFragmentManager();

        autocompleteFragment = (PlaceAutocompleteFragment) nfragmentManager.findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());

                gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(place.getLatLng(), 13, 0, 0)));

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textView = (TextView) findViewById(R.id.textView);

        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

        String title = (!getIntent().getStringExtra("label").isEmpty()) ? getIntent().getStringExtra("label") : getString(R.string.create_poligon);

        setTitle(title);

        if(fragmentManager == null){

            //Log.d(TAG, "fragmentManager is null");

        }

        SupportMapFragment mapFragment = (SupportMapFragment)fragmentManager.findFragmentById(R.id.map);

        if(mapFragment == null){

            //Log.d(TAG, "Error null");

        }else {

            mapFragment.getMapAsync(this);

        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.setOnMapClickListener(this);

        gMap = googleMap;

        gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        loadFromIntent();

        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

                markers.get(markers.indexOf(marker)).setPosition(marker.getPosition());

                update();

            }
        });

    }

    @Override
    public void onMapClick(LatLng latLng) {

        Marker marker = gMap.addMarker(new MarkerOptions()
         //       .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker))
        .position(latLng)
        .title("T")
        .visible(true)
        .draggable(true)
        );


        markers.add(marker);

        update();

    }

    public void update(){

        polygonOptions = new PolygonOptions();

        polygonOptions.strokeColor(R.color.colorMapStroke);

        polygonOptions.fillColor(R.color.colorMapFill);

        List<LatLng> lngList = new ArrayList<>();

        for(Marker marker: markers){
            polygonOptions.add(marker.getPosition());
            lngList.add(marker.getPosition());
        }

        if(polygon!=null)
            polygon.remove();

        polygon = gMap.addPolygon(polygonOptions);

        double ar = SphericalUtil.computeArea(polygonOptions.getPoints());

        if(ar == 0){

            ar = 1;

        }

        DecimalFormat df2 = new DecimalFormat("#,###,###,##0.00");

        area = ar/10000;

        //Log.d(TAG, "Area: "+String.valueOf(area)+" ha.");

        textView.setText("Area: "+df2.format(area)+" ha.");

        if(area > 0){

            frameLayout.setVisibility(View.VISIBLE);

        }else{

            frameLayout.setVisibility(View.GONE);

        }


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Log.d(TAG, connectionResult.getErrorMessage());
    }

    public String getDefault(){

        JSONObject jsonObjectTemp = new JSONObject();

        JSONArray jsonArray = new JSONArray();

        try {
            jsonArray.put(new JSONObject("{lat:1,lng:1}"));
            jsonArray.put(new JSONObject("{lat:1,lng:2}"));
            jsonArray.put(new JSONObject("{lat:2,lng:1}"));
            jsonArray.put(new JSONObject("{lat:2,lng:2}"));
            jsonObjectTemp.put("polygon", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  jsonObjectTemp.toString();

    }

    public String generateResult(){

        JSONObject jsonObjectTemp = new JSONObject();

        JSONArray polygon = new JSONArray();

        try {
            jsonObjectTemp.put("area", area);
            jsonObjectTemp.put("zoom", gMap.getCameraPosition().zoom);
            jsonObjectTemp.put("camera_lat", gMap.getCameraPosition().target.latitude);
            jsonObjectTemp.put("camera_lng", gMap.getCameraPosition().target.longitude);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{

            for(Marker mk: markers){

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("lat", mk.getPosition().latitude);

                jsonObject.put("lng", mk.getPosition().longitude);

                polygon.put(jsonObject);

            }

            jsonObjectTemp.put("polygon", polygon);

        }catch (Exception e){
            e.printStackTrace();
        }

        //Log.d(TAG, "Result:"+jsonObjectTemp.toString());

        return jsonObjectTemp.toString();

    }

    public void loadFromIntent(){

        stringJson = getIntent().getStringExtra("value");

        if(stringJson == null){

            // item is null

            //Log.d(TAG, "json_object is null");

        }else if(stringJson.isEmpty()){

            //Log.d(TAG, "json_object is empty");

        }else{

            //Log.d(TAG, "json_object is '"+stringJson+"'");

            try{

                jsonObject = new JSONObject(stringJson);

                if(jsonObject.has("hectarea")){

                    area = jsonObject.getDouble("hectarea");

                }else{

                    area = 0;

                }


                double camera_lat = 0;
                double camera_lng = 0;
                double camera_zoom = 5;

                if(jsonObject.has("zoom")){

                    camera_zoom = jsonObject.getDouble("zoom");

                }

                if(jsonObject.has("camera_lng")){
                    camera_lng = jsonObject.getDouble("camera_lng");
                }

                if(jsonObject.has("camera_lat")){
                    camera_lat = jsonObject.getDouble("camera_lat");
                }

                gMap.moveCamera(
                        CameraUpdateFactory
                                .newCameraPosition(
                                        new CameraPosition(
                                                new LatLng(camera_lat, camera_lng),
                                                (float) camera_zoom, 0, 0)));

                if(jsonObject.has("polygon")){

                    try{

                        JSONArray jsonArray = jsonObject.getJSONArray("polygon");

                        if(jsonArray.length()>0){



                        }

                        LatLngBounds.Builder builder = LatLngBounds.builder();

                        for(int i = 0; i < jsonArray.length(); i++){

                            try {

                                JSONObject jsonObjectTest = jsonArray.getJSONObject(i);

                                LatLng latLng = new LatLng(jsonObjectTest.getDouble("lat"), jsonObjectTest.getDouble("lng"));

                                builder.include(latLng);

                                Marker marker = gMap.addMarker(new MarkerOptions()
                                     //   .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker))
                                        .position(latLng)
                                        .title("T")
                                        .visible(true)
                                        .draggable(true)
                                );

                                markers.add(marker);

                            }catch (Exception e){
                                e.printStackTrace();

                                //Log.d(TAG, e.getMessage());

                            }

                        }

                        update();

                    }catch (Exception e){

                        //Log.d(TAG, "error parsing polygon attr: "+e.toString());

                    }

                }

            }catch (Exception e){
                e.printStackTrace();

                //Log.d(TAG, "error parsing json_object");

            }

        }
    }

    @Override
    public void onClick(View view) {

        Intent data = new Intent();

        data.putExtra("value", generateResult());

        data.putExtra("field", getIntent().getStringExtra("field"));

        setResult(RESULT_OK, data);

        finish();

    }

    private void take() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        //Log.d("SCREEN", "Init()");

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/test.jpg";

            //Log.d("SCREEN", "Dir:"+mPath);

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);

            v1.buildDrawingCache(true);

            v1.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY));
            v1.layout(0, 0, 600, 600);

            Bitmap bmp = v1.getDrawingCache();

            if(bmp == null){

                //Log.d("SCREENX", "BTMP is null");

            }

            Bitmap bitmap = Bitmap.createBitmap(bmp);
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            //Log.d("SCREEN", "Finished");

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
            //Log.d("SCREEN", e.toString());

        }
    }
}
