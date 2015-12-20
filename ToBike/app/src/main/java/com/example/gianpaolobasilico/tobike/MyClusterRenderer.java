package com.example.gianpaolobasilico.tobike;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by AjejeBrazorf on 19/12/15.
 */


public class MyClusterRenderer extends DefaultClusterRenderer<mMarkerPostazione> {


    public MyClusterRenderer(Context context, GoogleMap map, ClusterManager<mMarkerPostazione> clusterManager) {
        super(context, map, clusterManager);
    }

    protected void onBeforeClusterItemRendered(mMarkerPostazione item,
                                               MarkerOptions markerOptions) {
        markerOptions.icon(item.getMarker().getIcon());
        markerOptions.title(item.getmTitle());

    }

    @Override
    public void setOnClusterClickListener(ClusterManager.OnClusterClickListener<mMarkerPostazione> listener) {
        super.setOnClusterClickListener(listener);
    }


}
