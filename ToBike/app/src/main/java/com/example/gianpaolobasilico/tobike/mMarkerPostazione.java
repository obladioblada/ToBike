package com.example.gianpaolobasilico.tobike;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by ajejeBrazorf on 19/12/15.
 */
public class mMarkerPostazione implements ClusterItem {

    private final LatLng mPosition;
    private final int mBikes;
    private final String mTitle;

    public String getmTitle() {
        return mTitle;
    }

    public mMarkerPostazione(double lat, double lng, String mTitle, int bikes) {
        this.mTitle = mTitle;
        this.mPosition = new LatLng(lat, lng);
        this.mBikes = bikes;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public int getmBikes(){
        return mBikes;
    }

    public MarkerOptions getMarker() {
        MarkerOptions mo= new MarkerOptions().position(getPosition());
        mo.title(mTitle);
        if(mBikes<4){
            //     mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red));
            mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }else{
            if(mBikes<6){
                //         mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_yellow));
                mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            }
            else
            {
                //   mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_green));
                mo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }
        }

        return mo;
    }



}