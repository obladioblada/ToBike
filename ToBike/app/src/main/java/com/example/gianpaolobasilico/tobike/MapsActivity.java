package com.example.gianpaolobasilico.tobike;


//do NOT delete this two lines, is where I take the sh*t out of the markers! :D
// https://developers.google.com/maps/documentation/android-api/utility/marker-clustering?hl=en

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Double myLatitude;
    private Double myLongitude;
    private LocationManager locationManager;
    private FloatingActionButton myLocation;
    private FloatingActionButton navigation;
    private FloatingActionButton clear;
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
    private JSONArray routes;
    private JSONArray steps;
    private JSONObject leg;
    private String polyline="";
    private  PolylineOptions lineOptions ;
    private List<Polyline> polylineList;
    private List<ArrayList<LatLng>> listasegmenti;
    private LatLng lastD;
    private  String stationRequest = "http://api.citybik.es/to-bike.json";
    private String directionRequest = "https://maps.googleapis.com/maps/api/directions/json?";
    private double disttoDirection;
    private int rateRequest;
    private LatLng destination_position;
    private long tempoTrascorso;
    private long tStart;
    private ArrayList<LatLng> pointsPath;
    List<mMarkerPostazione> postazioni;
    private boolean pathRequested;
    private LatLng mypositionSaved;




    //gestione invio dati
    //0 Direzione, 1 Fermata, 2 Distanza
    private String[] btData;
    private int contatoreBtData;






    /**___------------------------------------------*/
    private ListView devices;
    private ArrayAdapter arrayAdapter;
    private Button cerca;
    BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT=4321;
    private BroadcastReceiver mReceiver;
    ArrayList<BluetoothDevice> avaibleDevices;
    Context context;
    private String[] listtextdev;
    private int iDev=0;
    private boolean stationrequestDone;

    private boolean startnavigation;


    /**------------------------------------------*/

    // Declare a variable for the cluster manager.
    private ClusterManager<mMarkerPostazione> mClusterManager;
    private MyClusterRenderer myrend;
    //code to start speech
    private static final int REQUEST_CODE_TO_SPEECH = 1234;
    private static final int REQUEST_CODE_TO_SETTING=1992;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Stringhe per il salvataggio degli stati
     */
    static final String STATE_MODE = "stateMode";
    static final String POSITION_MAPPA = "PositionMappa";


    /**
     * Stringhe per illa richiesta della direzione
     */
    private static final String KEY="&key=AIzaSyAbOWOtYsr1-uDMrC6eC4Ycy1XVEWP1P-g";
    //MODE : fare decidere all'utente se andare a piedi o in macchina(??)
    private static final String MODE="&mode=walking";
    private static final String AVOID="&avoid=highways";
    private int indexPositions;
    private ConnectThread connectThread;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        startnavigation=false;
        if(savedInstanceState==null)
        {   Intent i=new Intent(this,SplashActivity.class);
            startActivity(i);
            stationrequestDone=false;
        }
        Log.i("lifecycle","oncreate");
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        indexPositions=0;
        pathRequested=false;
        postazioni=new ArrayList<>();
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
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);}
        mPositionMarkerOption = new MarkerOptions();
        mPositionMarkerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.ncflat));
        mPositionMarkerOption.anchor(0.5f, 0.5f);
        mPositionMarkerOption.flat(true);
        devices=(ListView)findViewById(R.id.listaDevices);
        //handling slidingUp panel
        slidingUpPanelLayout=(SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        fermata = (TextView)findViewById(R.id.fermata);
        numbicilibere = (TextView)findViewById(R.id.numbicilibere);
        numbicioccupate = (TextView)findViewById(R.id.numbicioccupate);
        mode=(Switch)findViewById(R.id.switchMode);
        /**
         * false=available station mode
         * true= available bike mode
         *
         * L'applicazione inizia di default in BIKE MODE
         * **/
        mode.setChecked(true);
        mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    /** metodo che cambia il colore dei markers delle postazione
                     * in base alla modalità dell'applicazione
                     *
                     */
                      changeColorMode(isChecked);
                    if(mClusterManager!=null) {
                        for (Marker m : mClusterManager.getMarkerCollection().getMarkers()) {
                            myrend.getClusterItem(m).setState(isChecked);
                        }
                    }
                }
        });


        btData= new String[3];
        btData[0]="direzione";
        btData[1]="stazione";
        btData[2]="distanza";
        contatoreBtData=0;

        //set location floating action button;
        myLocation=(FloatingActionButton)findViewById(R.id.position);
        navigation =(FloatingActionButton)findViewById(R.id.navigation);
        navigation.setEnabled(false);
        navigation.setClickable(false);
        clear=(FloatingActionButton)findViewById(R.id.clear);
        clear.setVisibility(View.GONE);
        clear.hide();
        myLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                 /**controllo se gps abilitato*/
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
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLatitude,myLongitude),16),500,null);
                        }

                }
        });
         // 5 minuti 300000
        rateRequest=300000;

        /**
         * gestione floating button relativo alla navigazione
         */

        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findRoute();
                navigation.setVisibility(View.INVISIBLE);
                clear.setVisibility(View.VISIBLE);
                clear.setEnabled(false);
                startnavigation = true;

            }
        });
        /**
         * gestione floating button relativo alla navigazione
         */

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pointsPath.clear();
                clearMap();
                clear.setVisibility(v.INVISIBLE);
                navigation.setVisibility(View.VISIBLE);
                startnavigation=false;
                btData[0]="direzione";
                btData[1]="stazione";
                btData[2]="distanza";

            }
        });
        polylineList =new ArrayList<>();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList=(ListView)findViewById(R.id.drawer_items);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_18dp);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mDrawerLayout.setDrawerListener(drawerToggle);
        /**
         * creazione della lista da inserire nella navigation drawer
         */
        navigation_items= new String[]{getString(R.string.connecting),getString(R.string.login),getString(R.string.preferred), getString(R.string.about)};
        icon_list = new int[]{R.drawable.ic_bluetooth_black_24dp,R.drawable.login,R.drawable.ic_add_location_black_24dp, R.drawable.ic_person_black_24dp};
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


        HandlerThread btDataThread = new HandlerThread("HandlerThread");
        btDataThread.start();
        final   Handler handler = new Handler(btDataThread.getLooper());
        Runnable r= new Runnable() {
            @Override
            public void run() {
               if(startnavigation)
                   getDirection();
              //  if (btData!=null); Log.i("data",btData[contatoreBtData]);
                if( connectThread!=null){
                    if (connectThread.BtConnected()) {
                        connectThread.sendData(btData[contatoreBtData]);
                        Log.i("data",btData[contatoreBtData]);
                    }
                }

                Log.i("data i","contatore "+contatoreBtData);
                contatoreBtData++;
                if(contatoreBtData==3) contatoreBtData=0;
                handler.postDelayed(this, 1000);
            }
        };


        handler.post(r);





        /**
         * gestione autocompleteText view
         */
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
                {   /**
                    al click del tasto di ricerca la camera si sposta sulla postazione spunta lo sliding in basso col numero di fermate
                    */
                    autoCompleteTextView.clearFocus();
                    station_to_reach=autoCompleteTextView.getText().toString();
                    Log.i("toreach",station_to_reach);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    for (Marker m:mClusterManager.getMarkerCollection().getMarkers()) {
                            if( station_to_reach.equals(m.getTitle())){
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 16),500,null);
                                fermata.setText(m.getTitle());
                                numbicilibere.setText(String.valueOf(myrend.getClusterItem(m).getmFree()));
                                numbicioccupate.setText(String.valueOf(myrend.getClusterItem(m).getmBikes()));
                                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                navigation.setEnabled(true);
                                navigation.setClickable(true);
                                Log.i("fermata", m.getTitle());
                                autoCompleteTextView.setText("");
                                return true;
                            }
                    }

                }
                return false;
            }

        });

    }

    //metodo per cancellare percorso dalla mappa
    public void clearMap(){
        for (Polyline p:polylineList) {
            p.remove();
        }
        polylineList.clear();
        if(mPositionMarker!=null&&myLatitude!=null&&myLongitude!=null)
            mPositionMarker.remove();
        mPositionMarkerOption.position(new LatLng(myLatitude,myLongitude));
        mPositionMarker=mMap.addMarker(mPositionMarkerOption);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //mi salvo modalità applicazione
        outState.putBoolean(STATE_MODE,mode.isChecked());
        //mi salvo la posizione della camera (mappa)
        if(mMap!=null)
            outState.putParcelable(POSITION_MAPPA,mMap.getCameraPosition().target);
    if(pointsPath!=null) {
        if (pointsPath.size() > 0) {
            pathRequested = true;
            outState.putParcelableArrayList("pointsPath", pointsPath);
            pointsPath = outState.getParcelableArrayList("pointsPath");
            Log.i("babba save", "misura del percorso  " + pointsPath.size());
            outState.putBoolean("pathRequested", pathRequested);
        }

     }
        if(myLatitude!=null&&myLocation!=null)
        outState.putParcelable("myPosition",new LatLng(myLatitude,myLongitude));
        outState.putParcelableArrayList("postazioni", (ArrayList<? extends Parcelable>) postazioni);
         outState.putBoolean("stationRequestDone",stationrequestDone);
        outState.putString("fermata",station_to_reach);
        outState.putString("bici libere",numbicilibere.getText().toString());
        outState.putString("bicioccupate",numbicioccupate.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //resetto modalità applicazione

        mode.setChecked(savedInstanceState.getBoolean(STATE_MODE));
        stationrequestDone=savedInstanceState.getBoolean("stationRequestDone");
        if(mClusterManager!=null) {
            for (Marker m : mClusterManager.getMarkerCollection().getMarkers()) {
                myrend.getClusterItem(m).setState(mode.isChecked());
            }

        }
        for (mMarkerPostazione m:postazioni) {
            m.setState(mode.isChecked());}
        if(pointsPath!=null) pointsPath.clear();
        pointsPath=savedInstanceState.getParcelableArrayList("pointsPath");
        if(pointsPath!=null) Log.i("babba restore","misura del percorso  "+pointsPath.size());
        mypositionSaved =savedInstanceState.getParcelable("myPosition");
        if(savedInstanceState.getBoolean("pathRequested")==true)
        {   navigation.setVisibility(View.INVISIBLE);
            clear.setVisibility(View.VISIBLE);
            clear.setEnabled(true);
        }
            postazioni.clear();
            postazioni=savedInstanceState.getParcelableArrayList("postazioni");
            fermata.setText(savedInstanceState.getString("fermata"));
            station_to_reach=savedInstanceState.getString("fermata");
            numbicilibere.setText(savedInstanceState.getString("bici libere"));
            numbicioccupate.setText(savedInstanceState.getString("bicioccupate"));
    }

    /**
     costruzione stringa richiesta http per la direzione
     */
    private void findRoute() {
        String origin="origin=";
        String destination="destination=";

        for (Marker m:mClusterManager.getMarkerCollection().getMarkers()) {
            if(station_to_reach!=null)
                if( station_to_reach.equals(m.getTitle())){
                    directionRequest = "http://www.yournavigation.org/api/1.0/gosmore.php?format=geojson";
                    directionRequest+="&flat=";
                    directionRequest+=myLatitude;
                    directionRequest+="&flon=";
                    directionRequest+=myLongitude;
                    directionRequest+="&tlat=";
                    destination_position=myrend.getClusterItem(m).getPosition();
                    directionRequest+=destination_position.latitude;
                    directionRequest+="&tlon=";
                    directionRequest+=destination_position.longitude;
                    directionRequest+="&fast=1&v=bicycle&layer=cn&geometry=1&distance=gc&instructions=1&lang=it";
                    doDirectionRequest(directionRequest);
                    Log.i("station",directionRequest);
                    if( connectThread!=null){
                        if (connectThread.BtConnected()) {
                            connectThread.sendData("i" + station_to_reach);
                            Log.i("data station","i" + station_to_reach);
                        }
                    }
                }
        }

        tStart = System.currentTimeMillis();
    }
    /**
     metodo per la gestione del percorso da-a
     */

    public void doDirectionRequest(String directionRequest){
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, directionRequest, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i("station response ", response.toString());
                    clearMap();
                    List<List<LatLng>> list=new ArrayList<>();
                    lineOptions = new PolylineOptions();
                    JSONArray coord = response.getJSONArray ("coordinates");
                    DrawPath drawPath =new DrawPath(response);
                    drawPath.execute();

                    String cc[] = response.getJSONObject ("properties").toString().split("<br>");
                    Log.i("direction size",String.valueOf(cc.length));
                    clear.setEnabled(true);
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
                error.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Error: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(20000,1,1.0f));
        MySingleton.getInstance(this).addToRequestQueue(jsonObjReq);
    }


   private void clearmarkermap(){
        mClusterManager.clearItems();
       /* for (Marker m:mClusterManager.getMarkerCollection().getMarkers()) {
            m.remove();
        } */
    }

    private void creaSegmenti(LatLng[] points) {
        listasegmenti=new ArrayList<>();
        for(int i=0;i<points.length-1;i++){
            ArrayList<LatLng> segment=new ArrayList<>();
            segment.add(points[i]);
            segment.add(points[i+1]);
            listasegmenti.add(segment);
           /** if(i==0)
            {
            MarkerOptions mo=new MarkerOptions();
            mo.position(points[i]);
            mMap.addMarker(mo);

            }
            if(i==points.length-2){
                MarkerOptions mo=new MarkerOptions();
                mo.position(points[i+1]);
                mMap.addMarker(mo);
            }*/
        }
    }

    private boolean isBetweenSegment (LatLng P,ArrayList<LatLng>segmento){
        LatLng A=segmento.get(0);
        LatLng B=segmento.get(1);
        LatLng AP= new LatLng(P.latitude-A.latitude,P.longitude-A.longitude);
        LatLng AB= new LatLng(B.latitude-A.latitude,B.longitude-A.longitude);
        LatLng BP= new LatLng(P.latitude-B.latitude,P.longitude-B.longitude);
        LatLng BA= new LatLng(A.latitude-B.latitude,A.longitude-B.longitude);

        if(ps(AP,AB)<0){
            //sono a sinistra di A
            return false;
            }
          else if (ps(BP,BA)<0)
                {//sono a destra di B
                    return false;
                }
            else
                {//sono in mezzo tra A e B

                return true;
                }




    }
    //calcola prodotto scalare tra due vettori
    private double ps(LatLng x,LatLng y){
        return x.latitude*y.latitude+x.longitude*y.longitude;
    }

    private double pv(LatLng U,LatLng V){return U.latitude*V.longitude-U.longitude*V.latitude;}

    private double modulo(LatLng AB){
        return Math.sqrt(  (Math.pow(AB.latitude,2)) + (Math.pow(AB.longitude,2)) ) ;
    }

    //calcolo distanza della posizione da tutti i segmenti del mio percorso
    private int getSegmento() {
        LatLng myPosition=new LatLng(myLatitude,myLongitude);
        double modAB;
        double modAP;
        double cosAlfa;
        double modAD;
        double cosBeta;
        double latD;
        double sinBeta;
        double longD;
        LatLng proiezioneSegmento=null;
        LatLng proiezioneSegmentoPiuVicino=null;
        int i=0;
        double distmin=1000000000;
        double dist=0;
        int index=0;

        for (ArrayList<LatLng> s:listasegmenti) {
            if(isBetweenSegment(myPosition,s)){
                //calcolo distanza dal vettore
                LatLng A=s.get(0);
                LatLng B=s.get(1);
                LatLng AP= new LatLng(myPosition.latitude-A.latitude,myPosition.longitude-A.longitude);
                LatLng AB= new LatLng(B.latitude-A.latitude,B.longitude-A.longitude);
                 modAB=modulo(AB);
                 modAP=modulo(AP);
                 double prodMod=modAB*modAP;
                 if (prodMod==0) prodMod=1;
                 cosAlfa=ps(AP,AB)/prodMod;
                 modAD=modAP*cosAlfa;
                 if (modAB==0) modAB=1;
                 latD=A.latitude+AB.latitude*modAD/modAB;
                 longD=A.longitude+AB.longitude*modAD/modAB;
                 //cosBeta=ps(AB,new LatLng(1,0))/modAB;
                 //latD=A.latitude+(modAD*cosBeta);
                 //sinBeta=Math.abs(Math.sqrt(1-Math.pow(cosBeta,2)));
                 //longD=A.longitude+(modAD*sinBeta);
                 proiezioneSegmento= new LatLng(latD,longD);
                //vettore DP
                 LatLng DP= new LatLng(myPosition.latitude-proiezioneSegmento.latitude,myPosition.longitude-proiezioneSegmento.longitude);
                 Log.i("D lat",String.valueOf(latD));
                 Log.i("D long",String.valueOf(longD));
                 dist=calcoloDistanza(myPosition,proiezioneSegmento);
                //calcolo distanza con formula
                //proiezioneSegmento B, myPosition A
                if (dist<distmin && dist<50) {
                        proiezioneSegmentoPiuVicino=new LatLng(proiezioneSegmento.latitude,proiezioneSegmento.longitude);
                        distmin = dist;
                        index=i;
                        Log.i("distanza",String.valueOf(dist));
                    //calcolare distanza dal posizione alla fine del segmento
                       disttoDirection=calcoloDistanza(proiezioneSegmentoPiuVicino,B);
                       Log.i("DtoNewDirection","DtoNewDirection : "+disttoDirection);
                }


            }


            i++;
        }
        Log.i("distanzafinale",String.valueOf(distmin));
       /** MarkerOptions mo=new MarkerOptions();
        mo.position(listasegmenti.get(index).get(0));
        mMap.addMarker(mo);
        MarkerOptions m1=new MarkerOptions();
        m1.position(listasegmenti.get(index).get(1));
        mMap.addMarker(m1);**/
        return index;
    }

    private  double calcoloDistanza(LatLng A,LatLng B){
        //calcolo distanza in metri con formula
        // p1 = (minlon, minlat) //longitudine e latitudine in radianti
        //  p2 = (maxlon, maxlat) //longitudine e latitudine in radianti
        // dist = arccos( sin(minlat) * sin(maxlat) + cos(minlat) * cos(maxlat) * cos(maxlon – minlon) ) * 6371*1000
        double distanza;
        double R=6371;
        if(A!=null&B!=null)
        {          distanza=R*((Math.acos(Math.sin(Math.toRadians(B.latitude))*Math.sin(Math.toRadians(A.latitude))+Math.cos(Math.toRadians(B.latitude))*Math.cos(Math.toRadians(A.latitude))*Math.cos(Math.toRadians(A.longitude)-Math.toRadians(B.longitude)))))*1000;
        return distanza;}
        else return 1000000;
    }

    private void changeColorMode(boolean isChecked) {
        if (mMap != null) {
            mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

            for (mMarkerPostazione m: postazioni) {
                m.setState(isChecked);
            }
            Log.i("onmapready len p=", " "+postazioni.size());
            Log.i("onmapready change ic=", " "+mode.isChecked());
            mClusterManager.clearItems();
            mClusterManager.addItems(postazioni);
            for (Marker m : mClusterManager.getMarkerCollection().getMarkers()) {

                int tocheck;
                int bikered = R.drawable.markersbikered;
                int bikegreen = R.drawable.markersbikegreen;
                int bikeyellow=R.drawable.markersbikeyellow;

                int stationred = R.drawable.markersstationred;
                int stationgreen = R.drawable.markersstationgreen;
                int stationyellow=R.drawable.markersstationyellow;
                //check color for bike
                if (isChecked) {
                    tocheck = myrend.getClusterItem(m).getmBikes();
                    if (tocheck <= 2) {
                        m.setIcon(BitmapDescriptorFactory.fromResource(bikered));
                    } else {
                        if (tocheck <= 4) {
                            m.setIcon(BitmapDescriptorFactory.fromResource(bikeyellow));
                        } else {
                            m.setIcon(BitmapDescriptorFactory.fromResource(bikegreen));
                        }
                    }
                }
                //check color for station
                else {
                    tocheck = myrend.getClusterItem(m).getmFree();
                    if (tocheck <= 2) {
                        m.setIcon(BitmapDescriptorFactory.fromResource(stationred));
                    } else {
                        if (tocheck <= 4) {
                            m.setIcon(BitmapDescriptorFactory.fromResource(stationyellow));
                        } else {
                            m.setIcon(BitmapDescriptorFactory.fromResource(stationgreen));
                        }

                    }


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
        }
        Log.i("lifecycle", "onresume");
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
                getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), getMap().getCameraPosition().zoom + 1),500,null);
                return true;
            }
        });
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<mMarkerPostazione>() {
            @Override
            public boolean onClusterItemClick(mMarkerPostazione mMarkerPostazione) {
                station_to_reach=mMarkerPostazione.getmTitle();
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                fermata.setText(mMarkerPostazione.getmTitle());
                numbicioccupate.setText(String.valueOf(mMarkerPostazione.getmBikes()));
                numbicilibere.setText(String.valueOf(mMarkerPostazione.getmFree()));
                navigation.setEnabled(true);
                navigation.setClickable(true);
                getMap().animateCamera(CameraUpdateFactory.newLatLng(mMarkerPostazione.getPosition()),500,null);
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

    /**
     * metodo per il riconoscimento vocale
     */
    private void startVoiceRecognitionActivity() {
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "find your station");
        startActivityForResult(intent, REQUEST_CODE_TO_SPEECH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TO_SPEECH && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            station_to_reach=matches.get(0);;
            autoCompleteTextView.setText(station_to_reach);
            for (Marker m:mClusterManager.getMarkerCollection().getMarkers()) {
                    if( station_to_reach.equals(m.getTitle())){
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.getPosition(), 16),500,null);
                        fermata.setText(m.getTitle());
                        numbicioccupate.setText(String.valueOf(myrend.getClusterItem(m).getmBikes()));
                        numbicilibere.setText(String.valueOf(myrend.getClusterItem(m).getmFree()));
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        Log.i("fermata", m.getTitle());
                    }
            }
        }

        if (requestCode == REQUEST_CODE_TO_SETTING && resultCode == RESULT_OK){
            Intent returnedObjFromConnectThread = (Intent) data.getSerializableExtra("ConnectThread");
            connectThread=data.getParcelableExtra("connectThread");
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.i("onmapready, is cheched=", " "+mode.isChecked());
        setUpClusterer();
        changeColorMode(mode.isChecked());
        if(mClusterManager!=null) {
            for (Marker m : mClusterManager.getMarkerCollection().getMarkers()) {
                myrend.getClusterItem(m).setState(mode.isChecked());
            }
        }
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        if(!stationrequestDone) doStationRequest();
        autoCompleteTextView.setAdapter(acAdapter);
        is_ready=true;
        if(pointsPath!=null) putPathOnMap();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                navigation.setEnabled(false);
                navigation.setClickable(false);
            }
        });
     }

   public void update(){
       long tEnd = System.currentTimeMillis();
       long tDelta = tEnd - tStart;
       if(tDelta>=rateRequest)
           stationRequestUpdate();
       Log.i("update","update stazioni");
   }

    public void stationRequestUpdate(){
       tStart= System.currentTimeMillis();;
        JsonArrayRequest req = new JsonArrayRequest(stationRequest,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Parsing json array response
                            // loop through each json object
                            jsonResponse = "";
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject station = (JSONObject) response
                                        .get(i);

                                String id = station.getString("id");
                                String name = station.getString("name");

                                double lat = station.getDouble("lat");
                                lat = lat / 1e6;
                                double lng = station.getDouble("lng");
                                lng = lng / 1e6;
                                int bikes = station.getInt("bikes");
                                int free = station.getInt("free");
                                String timestamp = station.getString("timestamp");
                                //aggiorno il numero di bici e postazioni libere
                                for (Marker m : mClusterManager.getMarkerCollection().getMarkers()) {
                                    if(myrend.getClusterItem(m).getmTitle().equals(name)){
                                          myrend.getClusterItem(m).setmBikes(bikes);
                                          myrend.getClusterItem(m).setmFree(free);
                                    //controllo se la posizione di arrivo è quale  nel caso se le bici sono zero ricalcolo percorso
                                        if(name.equals(station_to_reach)&&myrend.getClusterItem(m).getmFree()==0){
                                            changeStation();
                                        }



                                         }
                                }
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

    public void doStationRequest(){
        stationrequestDone=true;
        Log.i("richiesta stazioni","richiesta stazioni partita");
        JsonArrayRequest req = new JsonArrayRequest(stationRequest,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        DrawStation drawStation=new DrawStation(response);
                        drawStation.execute();

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



    private class DrawStation extends AsyncTask<Void,Void,List<mMarkerPostazione>> {
        JSONArray res;

        public DrawStation(JSONArray res){
            this.res=res;
        }

        protected List<mMarkerPostazione> doInBackground(Void... voids)  {

            try {
                // Parsing json array response
                // loop through each json object
                jsonResponse = "";
                for (int i = 0; i < res.length(); i++) {

                    JSONObject station = (JSONObject) res
                            .get(i);

                    String id = station.getString("id");
                    String name = station.getString("name");
                    suggestions.add(name);
                    double lat = station.getDouble("lat");
                    lat=lat/1e6;
                    double lng = station.getDouble("lng");
                    lng=lng/1e6;
                    int bikes = station.getInt("bikes");
                    int free = station.getInt("free");
                    String timestamp=station.getString("timestamp");

                    jsonResponse += "id: " + id + "\n\n";
                    jsonResponse += "name: " + name + "\n\n";
                    jsonResponse += "lat: " + lat + "\n\n";
                    jsonResponse += "lng: " + lng + "\n\n";
                    jsonResponse += "bikes: " + bikes + "\n\n\n";
                    jsonResponse += "free: " + free + "\n\n";
                    jsonResponse += "timestamp: " + timestamp + "\n\n";
                    mMarkerPostazione am= new mMarkerPostazione(lat,lng,name,bikes,free);
                    Log.i("ciao",""+postazioni.size());
                    postazioni.add(am);

                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }



            return postazioni;
        }



        protected void onPostExecute(List<mMarkerPostazione> postazioni){
            mClusterManager.addItems(postazioni);
            if(!pathRequested) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45.0704900,7.6868200),14),500,null);
            else mMap.animateCamera(CameraUpdateFactory.newLatLng(mypositionSaved),500,null);

        }
    }



    private class DrawPath extends AsyncTask<Void, Void, List<LatLng>> {
        JSONObject response;

        public DrawPath(JSONObject response)
        {   this.response=response;
        }
        protected List<LatLng> doInBackground(Void... voids) {
            try { pointsPath=new ArrayList<LatLng>();
                // Parse JSON
                JSONArray coord = response.getJSONArray ("coordinates");
                for(int i=0;i<coord.length();i++) {
                    String c = String.valueOf(coord.get(i));
                    c = c.substring(1);
                    c = c.replaceAll("]", "");
                    String cc[] = c.split(",");
                    LatLng ll = new LatLng(Double.parseDouble(cc[1]), Double.parseDouble(cc[0]));
                    pointsPath.add(ll);
                }
            } catch (Exception e) {
                e.printStackTrace();
              //  Toast.makeText(getApplicationContext(),
                        //"Error: " + e.getMessage(),
                       // Toast.LENGTH_LONG).show();
            }
            return pointsPath;
        }


        @Override
        protected void onPostExecute(List<LatLng> pointsPath) {
            super.onPostExecute(pointsPath);
            putPathOnMap();
            if(mMap!=null) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLatitude,myLongitude),16),1000,null);

        }


    }

    public void putPathOnMap(){
        if(pointsPath!=null) {
            if (pointsPath.size() > 0) {
                pathRequested = true;
                LatLng[] pointsArray = pointsPath.toArray(new LatLng[pointsPath.size()]);
                Log.i("babba polyline", "lunghezza " + pointsArray.length);
                // Draw Points on MapView


                polylineList.add(mMap.addPolyline(new PolylineOptions()
                        .add(pointsArray)
                        .color(Color.parseColor("#3bb2d0"))
                        .width(5)));

                creaSegmenti(pointsArray);
            }


            Log.i("points size", String.valueOf(pointsPath.size()));
        }

    }

    private void changeStation() {
        //trovate la stazione piu vicina a quella a cui dovrei andare e ricalcolare percorso
        double near_station=100000000;
        LatLng near_Statio_position;
        String near_Station_name="";
        double d;

        for (Marker m : mClusterManager.getMarkerCollection().getMarkers()){
            d=calcoloDistanza(destination_position,m.getPosition());
            if(d<near_station && myrend.getClusterItem(m).getmFree()>0)
              {   near_Statio_position=m.getPosition();
                  near_Station_name=myrend.getClusterItem(m).getmTitle();
                  near_station=d;}
        }
        station_to_reach=near_Station_name;
        btData[1]="s"+station_to_reach;
        findRoute();


    }

    public GoogleMap getMap() {
        return mMap;}

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("lifecycle", "onconnected");
        //as soon as map is connected
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            myLatitude=mLastLocation.getLatitude();
             myLongitude=mLastLocation.getLongitude();
            if(mPositionMarker!=null)
                mPositionMarker.remove();
               mPositionMarkerOption.position(new LatLng(myLatitude, myLongitude));
              mPositionMarker=mMap.addMarker(mPositionMarkerOption);
            if(bundle!=null)
                mMap.moveCamera(CameraUpdateFactory.newLatLng((LatLng)bundle.getParcelable(POSITION_MAPPA)));
             //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLatitude, myLongitude),16));
             createLocationRequest();
             startLocationUpdates();}
    }


    /**
     * costruzione del messaggio per la mancanza del gps nel telefono
     * */

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
        Log.i("svolta","getDirection");
        if(listasegmenti!=null) {
            indexPositions=getSegmento();
            Log.i("prova D", listasegmenti.get(indexPositions).toString() );
        }
        update();
    }

    private void getDirection() {
//Da inviare ad Arduino
//0 dx
//1 sx
//2 dritto
//3 arrivo
        double coseno;
        double seno;
        double angolo;
        if(disttoDirection>100){
            //continua drittto
            btData[0]="d2";
        }else {
            //dato il segmento corrente ed il successivo mi calcolo la direzione  di svolta
            if (listasegmenti != null) {
                ArrayList<LatLng> currentSegment = listasegmenti.get(indexPositions);
                if(indexPositions==listasegmenti.size()-1){
                    Log.i("svolta","stai arrivando, mancano solo "+disttoDirection+"m");
                    if( connectThread!=null) {

                        if (connectThread.BtConnected()) {
                            connectThread.sendData("f");
                            Log.i("data","f");
                            btData[2] = "p" + disttoDirection;
                        }
                    }
                }else {
                    ArrayList<LatLng> nextSegemnt = listasegmenti.get(indexPositions + 1);
                    LatLng currentVector = new LatLng(currentSegment.get(0).latitude - currentSegment.get(1).latitude, currentSegment.get(0).longitude - currentSegment.get(1).longitude);
                    LatLng nextVector = new LatLng(nextSegemnt.get(1).latitude - nextSegemnt.get(0).latitude, nextSegemnt.get(1).longitude - nextSegemnt.get(0).longitude);
                    //calcolo prodotto scalare tra i due segmenti per capire l'angolazione di svolta
                    double promod=modulo(currentVector) * modulo(nextVector);
                    if(promod==0)  promod=1;
                     coseno = ps(currentVector, nextVector) /promod;
                    if(promod==0) promod=1;
                     seno = pv(nextVector,currentVector)/promod;
                    Log.i("svolta", " seno " + seno + " " + " coseno  " + coseno);
                    Log.i("svolta tra ",disttoDirection +"  m");
                    btData[2]="p"+disttoDirection;
                    if (seno > 0 && coseno<0) {
                                //svolto a qualsiasi destra


                                if (seno < 0.2) {
                                    if(connectThread!=null){
                                       if (connectThread.BtConnected()) {
                                           btData[0]="d2";
                                           Log.i("svolta", "vai dritto");
                                         }
                                    }
                                    Log.i("svolta", "vai dritto");
                            }
                                if (seno > 0.2 && seno < 0.5) {
                                   if( connectThread!=null){
                                    if (connectThread.BtConnected()) {
                                        btData[0]="d0";
                                    }
                                   }
                                    Log.i("svolta", "tra " + disttoDirection + " m svolta leggermente a destra");
                                }
                                if (seno > 0.5) {
                                    if(connectThread!=null){
                                        if (connectThread.BtConnected()) {
                                            btData[0]="d0";
                                        }
                                    }
                                    Log.i("svolta", "tra " + disttoDirection + " m svolta a destra");
                                }
                    }
                    if(seno>0 && coseno > 0)
                    {   if(connectThread!=null){
                            if (connectThread.BtConnected()&&connectThread!=null) {
                                btData[0]="d0";
                            }
                         }
                         Log.i("svolta", "tra " + disttoDirection + " m svolta a destra");

                    }
                    if (seno < 0 && coseno<0) {
                                //svolto a qualsiasi sinistra
                                if (seno > -0.2) {
                                        if(connectThread!=null){
                                        if (connectThread.BtConnected()) {
                                            btData[0]="d2";
                                        }
                                    }
                                    Log.i("svolta", "vai dritto");
                                }
                                if (seno > -0.5 && seno < -0.2) {
                                    if(connectThread!=null){
                                        if (connectThread.BtConnected()) {
                                            btData[0]="d1";
                                         }
                                    }
                                    Log.i("svolta", "tra " + disttoDirection + " m svolta leggermente a sinistra");
                                }
                                if (seno < -0.5) {
                                    if(connectThread!=null){
                                        if (connectThread.BtConnected()) {
                                            btData[0]="d1";
                                        }
                                    }
                                    Log.i("svolta", "tra" + disttoDirection + "m svolta  a sinistra");
                                }
                    }
                    if(seno<0 && coseno>0){
                            if(connectThread!=null){
                                if (connectThread.BtConnected()) {
                                    btData[0]="d1";
                                }
                            }
                        Log.i("svolta", "tra" + disttoDirection + "m svolta  a sinistra");

                    }


                }
            }
        }


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

    /**
     * adapter per la creazione della lista nella navigation drawer
     * */
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
           
            case 0:ConnesioneBluetooth();
                  break;
                //login
            case 1:Intent login=new Intent(this,LoginActivity.class);
                  startActivity(login);break;
            //preferiti
            case 2:break;

            //chi siamo
            case  3: Intent about=new Intent(this,AboutActivity.class);
                startActivity(about);break;
        }
    }

    private void ConnesioneBluetooth() {
        arrayAdapter=new ArrayAdapter(this,R.layout.listdevices);
        mDrawerLayout.closeDrawers();
        final ArrayList<String> nameDevice=new ArrayList<>();
        if(!bluetoothAdapter.isEnabled())
        {   Intent enablebtIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enablebtIntent,REQUEST_ENABLE_BT);
        }
          bluetoothAdapter.startDiscovery();
          avaibleDevices=new ArrayList<>();
          mReceiver=new BroadcastReceiver() {
              @Override
              public void onReceive(Context context, Intent intent) {
                  String action=intent.getAction();
                  if(BluetoothDevice.ACTION_FOUND.equals(action)){
                      BluetoothDevice bluetoothDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                      avaibleDevices.add(bluetoothDevice);
                      nameDevice.add(bluetoothDevice.getName()+"\n"+bluetoothDevice.getAddress());
                      arrayAdapter.clear();
                     // arrayAdapter.add(bluetoothDevice.getName()+"\n"+bluetoothDevice.getAddress());
                      arrayAdapter.addAll(nameDevice);
                      Log.i("Connessione ricerca ","found "+bluetoothDevice.getName());
                  }
              }
          };
        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver,filter);
        Dialog d=onCreateDialog();
        d.show();

    }


    public Dialog onCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Device List");
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("onclick dialog","cliccato-> "+String.valueOf(which));
                UUID devUUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                Log.i("Connessione attempt ","tryng to connect to"+avaibleDevices.get(which).getName());
                connectThread =new ConnectThread(avaibleDevices.get(which),devUUID,bluetoothAdapter);
                connectThread.run();
                Log.i("Connessione attempt ","after run");
            }
        });

       builder.setCustomTitle(LayoutInflater.from(this).inflate(R.layout.dialoglayout, null));

        return builder.create();
    }

}

