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
        setLatLng(lat, lng);
        setTituloMarcador(cep);
    }

    private void setTituloMarcador(String t) {
        gMarkerOptions = new MarkerOptions();
        gPos = new LatLng(gLat, gLng);
        gMarkerOptions.position(gPos).title(t);
    }

    private void setLatLng(double lat, double lng){
        this.gLat = lat;
        this.gLng = lng;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();

        mMap = googleMap;
        mMap.addMarker(gMarkerOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gPos, 10.0f));
    }
}