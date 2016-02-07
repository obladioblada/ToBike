package com.example.gianpaolobasilico.tobike;


//do NOT delete this two lines, is where I take the sh*t out of the markers! :D
// https://developers.google.com/maps/documentation/android-api/utility/marker-clustering?hl=en

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener{

    private GoogleMap mMap;
    private String jsonResponse;
    private List stazioni;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private TextView fermata;
    private TextView numbicilibere;
    private TextView numbicioccupate;
    private Switch mode;
    private TextView textmode;

    // Create an instance of GoogleAPIClient.
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Double myLatitude;
    private Double myLongitude;
    private LocationManager locationManager;
    private FloatingActionButton myLocation;
    private FloatingActionButton navigation;
    private Circle myLocationCircle;
    private CircleOptions myLocationCircleOptions;
    private String[] navigation_items;
    private int[] icon_list;
    private LocationRequest mLocationRequest;
    private List<String> suggestions;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> acAdapter;
    private String station_to_reach;
    private Boolean is_ready;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private Marker mPositionMarker;
    private MarkerOptions mPositionMarkerOption;
    String url = "http://api.citybik.es/to-bike.json";
    // Declare a variable for the cluster manager.
    private ClusterManager<mMarkerPostazione> mClusterManager;
    private MyClusterRenderer myrend;
    //code to start speech
    private static final int REQUEST_CODE = 1234;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("lifecycle","oncreate");
        setContentView(R.layout.activity_maps);
        is_ready=false;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        stazioni= new ArrayList<MarkerOptions>();
        myLocationCircleOptions=new CircleOptions();
        myLocationCircleOptions.radius(10);
        myLocationCircleOptions.fillColor(Color.YELLOW);
        myLocationCircleOptions.strokeColor(Color.BLUE);
        myLocationCircleOptions.strokeWidth(2);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .enableAutoManage(this, this)
                    .build();
            //as soon as map is connected
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        mPositionMarkerOption = new MarkerOptions();
        mPositionMarkerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.ncflat));
        mPositionMarkerOption.anchor(0.5f, 0.5f);
        mPositionMarkerOption.flat(true);

        //handlign slidingUp panel
        slidingUpPanelLayout=(SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        fermata = (TextView)findViewById(R.id.fermata);
        numbicilibere = (TextView)findViewById(R.id.numbicilibere);
        numbicioccupate = (TextView)findViewById(R.id.numbicioccupate);
        // false=available station mode
        // true= available bike mode
        textmode=(TextView)findViewById(R.id.textMode);
        mode=(Switch)findViewById(R.id.switchMode);
        mode.setChecked(true);
        mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               changeColorMode(isChecked);
            }
        });

        //set location floating action button;
        myLocation=(FloatingActionButton)findViewById(R.id.position);
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
                if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                    buildAlertMessageNoGps();
                }
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    myLatitude=mLastLocation.getLatitude();
                    myLongitude=mLastLocation.getLongitude();
                    if(mPositionMarker!=null)
                        mPositionMarker.remove();
                    mPositionMarkerOption.position(new LatLng(myLatitude,myLongitude));
                    mPositionMarker=mMap.addMarker(mPositionMarkerOption);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLatitude,myLongitude),16));
                }

            }
        });

        //handling navigation floating button
        navigation =(FloatingActionButton)findViewById(R.id.navigation);
        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findRoute();
            }
        });
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList=(ListView)findViewById(R.id.drawer_items);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_18dp);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mDrawerLayout.setDrawerListener(drawerToggle);

        navigation_items= new String[]{getString(R.string.login),getString(R.string.preferred),getString(R.string.setting),
                getString(R.string.about)};
        icon_list = new int[]{R.drawable.login,R.drawable.ic_add_location_black_24dp,R.drawable.ic_settings_black_24dp,
                R.drawable.ic_person_black_24dp};

        MAdapterList mAdapter=new MAdapterList(this, navigation_items,icon_list);
        mDrawerList.setAdapter(mAdapter);
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

        //handling autocomplete
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        autoCompleteTextView=(AutoCompleteTextView)findViewById(R.id.autocomplete);
        suggestions=new ArrayList<String>();
        acAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,suggestions);
        autoCompleteTextView.setDropDownBackgroundResource(R.color.white);
        autoCompleteTextView.setDropDownVerticalOffset(25);
        autoCompleteTextView.setDropDownWidth(displaymetrics.widthPixels);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.performCompletion();
        //handling click on suggestion
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                station_to_reach = acAdapter.getItem(position);
            }
        });

        //handling click on search button keyboard
        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_SEARCH)
                {   //implementare che al click del tasto di ricerca la camera si deve spostare sulla postazione
                    // e deve spuntare lo sliding in basso col numero di fermate
                    autoCompleteTextView.clearFocus();
                    station_to_reach=autoCompleteTextView.getText().toString();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    for (Marker m:mClusterManager.getMarkerCollection().getMarkers()) {
                        if( station_to_reach.equals(m.getTitle())){
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 16));
                            fermata.setText(m.getTitle());
                            numbicilibere.setText(String.valueOf(myrend.getClusterItem(m).getmFree()));
                            numbicioccupate.setText(String.valueOf(myrend.getClusterItem(m).getmBikes()));
                            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            Log.i("fermata", m.getTitle());
                            autoCompleteTextView.setText("");
                            return true;}
                          }

                }
                return false;}

        });

    }



    private void findRoute() {

    }

    //-------------change color markers with respect to mode state-------------------
    private void changeColorMode(boolean isChecked) {

        for (Marker m:mClusterManager.getMarkerCollection().getMarkers()) {
            int tocheck;
            int bikered=R.drawable.redm;
            int bikegreen=R.drawable.greenm;
            int stationred=R.drawable.ic_add_location_black_24dp;
            int stationgreen=R.drawable.ic_directions_bike_black_24dp;

            //check color for bike
            if(isChecked){
                tocheck=myrend.getClusterItem(m).getmBikes();
                if(tocheck<=2){
                    m.setIcon(BitmapDescriptorFactory.fromResource(bikered));
                }else{
                    if(tocheck<=4){
                        m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.yellowm));
                    }
                    else {
                        m.setIcon(BitmapDescriptorFactory.fromResource(bikegreen));
                    }
                }
            }
            //check color for statiomn
            else{
                tocheck=myrend.getClusterItem(m).getmFree();
                if(tocheck<=2){
                    m.setIcon(BitmapDescriptorFactory.fromResource(bikered));
                }else{
                    if(tocheck<=4){
                        m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.yellowm));
                    }
                    else {
                        m.setIcon(BitmapDescriptorFactory.fromResource(bikegreen));
                    }

            }





        }
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.i("lifecycle", "onstart");
    }

    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        Log.i("lifecycle", "onstop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected())
            stopLocationUpdates();
        Log.i("lifecycle","onpause");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.i("lifecycle", "onresume");
        }
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
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                fermata.setText(mMarkerPostazione.getmTitle());
                numbicioccupate.setText(String.valueOf(mMarkerPostazione.getmBikes()));
                numbicilibere.setText(String.valueOf(mMarkerPostazione.getmFree()));
                getMap().animateCamera(CameraUpdateFactory.newLatLng(mMarkerPostazione.getPosition()));
                return true;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
         drawerToggle.syncState();
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
            case  R.id.voicesearch:
                startVoiceRecognitionActivity();
        }

        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    private void startVoiceRecognitionActivity() {
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "find your station");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String station_said=new String();
            station_said=matches.get(0);
            station_to_reach=station_said;
            autoCompleteTextView.setText(station_said);
            for (Marker m:mClusterManager.getMarkerCollection().getMarkers()) {
                if( station_said.equals(m.getTitle())){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 16));
                    fermata.setText(m.getTitle());
                    numbicioccupate.setText(String.valueOf(myrend.getClusterItem(m).getmBikes()));
                    numbicioccupate.setText(String.valueOf(myrend.getClusterItem(m).getmFree()));
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    Log.i("fermata", m.getTitle());
                    }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.0704900,7.6868200),12));
        setUpClusterer();
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        doReq();
        autoCompleteTextView.setAdapter(acAdapter);
        is_ready=true;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i("ciao", "ciao");
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });
     }

    public void doReq(){
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Parsing json array response
                            // loop through each json object
                            jsonResponse = "";
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject person = (JSONObject) response
                                        .get(i);

                                String id = person.getString("id");
                                String name = person.getString("name");
                                suggestions.add(name);
                                double lat = person.getDouble("lat");
                                lat=lat/1e6;
                                double lng = person.getDouble("lng");
                                lng=lng/1e6;
                                int bikes = person.getInt("bikes");
                                int free = person.getInt("free");
                                String timestamp=person.getString("timestamp");

                                jsonResponse += "id: " + id + "\n\n";
                                jsonResponse += "name: " + name + "\n\n";
                                jsonResponse += "lat: " + lat + "\n\n";
                                jsonResponse += "lng: " + lng + "\n\n";
                                jsonResponse += "bikes: " + bikes + "\n\n\n";
                                jsonResponse += "free: " + free + "\n\n";
                                jsonResponse += "timestamp: " + timestamp + "\n\n";

                                mMarkerPostazione am= new mMarkerPostazione(lat,lng,name,bikes,free);
                                mClusterManager.addItem(am);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            myLatitude=mLastLocation.getLatitude();
             myLongitude=mLastLocation.getLongitude();
            if(mPositionMarker!=null)
                mPositionMarker.remove();
               mPositionMarkerOption.position(new LatLng(myLatitude, myLongitude));
              mPositionMarker=mMap.addMarker(mPositionMarkerOption);
             mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLatitude, myLongitude),16));
             createLocationRequest();
             startLocationUpdates();}
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(R.string.nogps);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_add_location_black_24dp);
        builder.setTitle("Attivazione GPS");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateUI();
    }

    private void updateUI() {
        myLatitude=mLastLocation.getLatitude();
        myLongitude=mLastLocation.getLongitude();
        if(mPositionMarker!=null)
            mPositionMarker.setPosition(new LatLng(myLatitude, myLongitude));

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private class MAdapterList extends BaseAdapter {

        private final Context context;
        private final String[] navigation_items;
        private final int[] item_icons;


        public MAdapterList(Context context, String[] navigation_items, int[] item_icon) {
            this.context = context;
            this.navigation_items = navigation_items;
            this.item_icons = item_icon;
        }

        @Override
        public int getCount() {
            return navigation_items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater= (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.drawer_list_item,parent,false);
            TextView text = (TextView)rowView.findViewById(R.id.textViewItem);
            ImageView icon=(ImageView)rowView.findViewById(R.id.icon_item);
            text.setText(navigation_items[position]);
            icon.setImageResource(item_icons[position]);
            return rowView;
        }
    }


    //handling  the navigation drawer's click
    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItemNavigation(position);
        }
    }

    private void selectItemNavigation(int position) {
        switch (position){
            //login
            case 0:Intent login=new Intent(this,LoginActivity.class);
                   startActivity(login);break;
            //preferiti
            case 1:break;
            //impostazioni
            case 2:
                Intent setting=new Intent(this,SettingActivity.class);
                startActivity(setting);
                break;
            //chi siamo
            case 3:
                Intent about=new Intent(this,AboutActivity.class);
                startActivity(about);break;
        }
    }


}
