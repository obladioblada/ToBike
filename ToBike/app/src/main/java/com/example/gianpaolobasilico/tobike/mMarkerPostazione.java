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
    private final int mFree;



    public mMarkerPostazione(double lat, double lng, String mTitle, int bikes,int free) {
        this.mTitle = mTitle;
        this.mPosition = new LatLng(lat, lng);
        this.mBikes = bikes;
        this.mFree=free;
    }

    public String getmTitle() {
        return mTitle;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }



    public int getmBikes(){
        return mBikes;
    }

    public int getmFree(){
        return mFree;
    }

    public MarkerOptions getMarker() {
        MarkerOptions mo= new MarkerOptions().position(getPosition());
        mo.title(mTitle);
        mo.flat(true);
        //


        if(mBikes<4){
            mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.redm));
        }else{
            if(mBikes<6){
                mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.yellowm));
            }
            else
            {mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.greenm));
            }
        }

        return mo;
    }




}