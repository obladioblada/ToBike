package com.example.gianpaolobasilico.tobike;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by ajejeBrazorf on 19/12/15.
 */
public class mMarkerPostazione implements ClusterItem, Parcelable {

    private  LatLng mPosition;
    private int mBikes;
    private  String mTitle;
    private  int mFree;
    private boolean[] states;

    /**se l'applicazione è in stato bike state=1, visualizzo icone delle bici
     *
     * se l'applicazione è in statostation state=0, visualizzo icone delle stazioni **/
    private boolean state;



    public mMarkerPostazione(double lat, double lng, String mTitle, int bikes,int free) {
        this.mTitle = mTitle;
        this.mPosition = new LatLng(lat, lng);
        this.mBikes = bikes;
        this.mFree=free;
        state=true;
        states=new boolean[1];
        states[0]=state;
    }

    public void setState(boolean state){
        this.state=state;
    }

    public boolean getState(boolean state){
        return  this.state;
    }



    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle){
        this.mTitle=mTitle;
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

    public void setmFree(int mFree) {
        this.mFree = mFree;
    }

    public void setmBikes(int mBikes) {
        this.mBikes = mBikes;
    }

    public MarkerOptions getMarker() {
        MarkerOptions mo= new MarkerOptions().position(getPosition());
        mo.title(mTitle);
        mo.flat(true);
        //stato bici
        if(state) {
            if (mBikes < 4) {
                mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.markersbikered));
            } else {
                if (mBikes < 6) {
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.markersbikeyellow));
                } else {
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.markersbikegreen));
                }
            }

        }
        else{
            //stato stazione
            if (mBikes < 4) {
                mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.markersstationred));
            } else {
                if (mBikes < 6) {
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.markersstationyellow));
                } else {
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.markersstationgreen));
                }
            }

        }


        return mo;

    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getmTitle());
        dest.writeParcelable(getPosition(),0);
        dest.writeInt(getmBikes());
        dest.writeInt(getmFree());
        dest.writeBooleanArray(states);
    }

    public static final Parcelable.Creator<mMarkerPostazione> CREATOR
            = new Parcelable.Creator<mMarkerPostazione>() {
        public mMarkerPostazione createFromParcel(Parcel in) {
            return new mMarkerPostazione(in);
        }

        public mMarkerPostazione[] newArray(int size) {
            return new mMarkerPostazione[size];
        }
    };

    private mMarkerPostazione(Parcel in) {
        mTitle = in.readString();
        mPosition=in.readParcelable(null);
        mBikes=in.readInt();
        mFree=in.readInt();
        in.readBooleanArray(states);
        state=states[0];
    }
}