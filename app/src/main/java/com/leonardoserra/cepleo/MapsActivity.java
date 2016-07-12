package com.leonardoserra.cepleo;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double gLat, gLng;
    private MarkerOptions gMarkerOptions;
    private LatLng gPos;
    private float gZoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void inicializa(double lat, double lng, String cep){
        gLat = lat;
        gLng = lng;
        gZoom = 18.0f;
        gMarkerOptions = new MarkerOptions();
        gPos = new LatLng(gLat, gLng);
        gMarkerOptions.position(gPos).title(cep);
    }

    public void setMapaInicial() {
        gZoom = 1.0f;
        gSetMapaInicial = true;
    }

    private boolean gSetMapaInicial = false;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();

        mMap = googleMap;

        if (!gSetMapaInicial) {
            mMap.addMarker(gMarkerOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gPos, gZoom));
        }

    }
}