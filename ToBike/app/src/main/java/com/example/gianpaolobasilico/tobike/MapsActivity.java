package com.example.gianpaolobasilico.tobike;


//do NOT delete this two lines, is where I take the sh*t out of the markers! :D
// https://developers.google.com/maps/documentation/android-api/utility/marker-clustering?hl=en

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private ProgressDialog pDialog;
    private String jsonResponse;
    private List stazioni;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    // Create an instance of GoogleAPIClient.
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Double myLatitude;
    private Double myLongitude;
    private FloatingActionButton myLocation;
    private FloatingActionButton TakeMeTo;
    private Circle myLocationCircle;
    private CircleOptions myLocationCircleOptions;
    private String[] navigation_items;


    String url = "http://api.citybik.es/to-bike.json";
    // Declare a variable for the cluster manager.
    private ClusterManager<mMarkerPostazione> mClusterManager;
    private MyClusterRenderer myrend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        stazioni= new ArrayList<MarkerOptions>();
        myLocationCircleOptions=new CircleOptions();
        myLocationCircleOptions.radius(10);
        myLocationCircleOptions.fillColor(Color.YELLOW);
        myLocationCircleOptions.strokeColor(Color.BLUE);
        myLocationCircleOptions.strokeWidth(2);

        //set floating action button;
        myLocation=(FloatingActionButton)findViewById(R.id.position);
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    myLatitude=mLastLocation.getLatitude();
                    myLongitude=mLastLocation.getLongitude();
                    myLocationCircleOptions.center(new LatLng(myLatitude,myLongitude));
                    myLocationCircleOptions.radius(10);
                    myLocationCircleOptions.fillColor(Color.YELLOW);
                    myLocationCircleOptions.strokeColor(Color.BLUE);
                    myLocationCircleOptions.strokeWidth(2);
                    myLocationCircle= mMap.addCircle(myLocationCircleOptions);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLatitude, myLongitude),16));
                }

            }

        });

        TakeMeTo=(FloatingActionButton)findViewById(R.id.navigation);

        //navigationDrawer
        //navigationDrawerm
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //get toolbar from xml and set it as actionbar
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(drawerToggle);
        navigation_items= new String[]{"Preferiti","Impostazioni","cazzabibbolo","chi siamo", "obladiObada"};
        mDrawerList=(ListView)findViewById(R.id.drawer_items);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item,R.id.textViewList,navigation_items));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_closed) {

           // Called when a drawer has settled in a completely closed state.
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            //Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            //as soon as map is connected
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }


    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;}

    private void setUpClusterer() {
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<mMarkerPostazione>(this, getMap());
        myrend=new MyClusterRenderer(this, getMap(),mClusterManager);
        getMap().setOnCameraChangeListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        mClusterManager.setRenderer(myrend);
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<mMarkerPostazione>() {
            @Override
            public boolean onClusterClick(Cluster<mMarkerPostazione> cluster) {
                getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), getMap().getCameraPosition().zoom + 1));
                return true;
            }
        });
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<mMarkerPostazione>() {
            @Override
            public boolean onClusterItemClick(mMarkerPostazione mMarkerPostazione) {
                TextView bici = (TextView) findViewById(R.id.nrBici);
                bici.setText("a " + mMarkerPostazione.getmTitle() + ": " + String.valueOf(mMarkerPostazione.getmBikes()) + " bici");
                getMap().animateCamera(CameraUpdateFactory.newLatLng(mMarkerPostazione.getPosition()));
                return true;
            }
        });
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }


    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpClusterer();
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        doReq();
        LatLng torino = new LatLng(45.0585363,7.6882472);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(torino));

    }

    public void doReq(){
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) { Log.d("ciao",response.toString());
                        try {
                            // Parsing json array response
                            // loop through each json object
                            jsonResponse = "";
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject person = (JSONObject) response
                                        .get(i);

                                String id = person.getString("id");
                                String name = person.getString("name");
                                double lat = person.getDouble("lat");
                                lat=lat/1e6;
                                double lng = person.getDouble("lng");
                                lng=lng/1e6;
                                int bikes = person.getInt("bikes");
                                String free = person.getString("free");
                                String timestamp=person.getString("timestamp");

                                jsonResponse += "id: " + id + "\n\n";
                                jsonResponse += "name: " + name + "\n\n";
                                jsonResponse += "lat: " + lat + "\n\n";
                                jsonResponse += "lng: " + lng + "\n\n";
                                jsonResponse += "bikes: " + bikes + "\n\n\n";
                                jsonResponse += "free: " + free + "\n\n";
                                jsonResponse += "timestamp: " + timestamp + "\n\n";

                                mMarkerPostazione am= new mMarkerPostazione(lat,lng,name,bikes);
                                mClusterManager.addItem(am);
                            }
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }

                        hidepDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {   Log.d("ciao", error.toString());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                hidepDialog();
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(20000,1,1.0f));
        MySingleton.getInstance(this).addToRequestQueue(req);
    }

    public GoogleMap getMap() {
        return mMap;}


    @Override
    public void onConnected(Bundle bundle) {
        //as soon as map is connected
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            myLatitude=mLastLocation.getLatitude();
            myLongitude=mLastLocation.getLongitude();}
        myLocationCircleOptions.center(new LatLng(myLatitude, myLongitude));

        if(myLocationCircle!=null)
             myLocationCircle.remove();
        myLocationCircle= mMap.addCircle(myLocationCircleOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLatitude, myLongitude),16));

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }



    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    }
}
