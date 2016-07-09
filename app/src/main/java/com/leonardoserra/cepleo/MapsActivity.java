package com.leonardoserra.cepleo;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double gLat, gLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void setLatLng(double lat, double lng){
        this.gLat = lat;
        this.gLng = lng;
    }

    private Marker m;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        MarkerOptions ultimoMarker = new MarkerOptions();

        mMap = googleMap;

        LatLng pos = new LatLng(gLat, gLng);
        ultimoMarker.position(pos).title("Marker");
        m = mMap.addMarker(ultimoMarker);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(gLat, gLng), 20.0f));
    }
}