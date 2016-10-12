package com.leonardoserra.buscacepcommapa;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private double lat, lng;
    private MarkerOptions markerOptions;
    private LatLng pot;
    private float zoom;
    private boolean setMapaInicial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_fragment);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void inicializa(double lat, double lng, String cep){
        this.lat = lat;
        this.lng = lng;
        zoom = 18.0f;
        markerOptions = new MarkerOptions();
        pot = new LatLng(this.lat, this.lng);
        markerOptions.position(pot).title(cep);
    }

    public void setMapaInicial() {
        zoom = 1.0f;
        setMapaInicial = true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();

        map = googleMap;

        if (!setMapaInicial) {
            map.addMarker(markerOptions);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pot, zoom));
        }
    }
}