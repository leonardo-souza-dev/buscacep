package com.leonardoserra.buscacepcommapa.vm;

import android.content.SharedPreferences;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.v4.app.FragmentManager;
import com.leonardoserra.buscacepcommapa.BR;
import com.google.android.gms.maps.SupportMapFragment;
import com.leonardoserra.buscacepcommapa.MapsActivity;

public class PrincipalViewModelo extends BaseObservable {

    private boolean pb2;
    private String cep2;
    private String logradouro2;
    private String bairro2;
    private String cidade2;
    private String uf2;
    private Double lat2;
    private Double lng2;

    private boolean setMapaInicial;
    private FragmentManager fm;
    private MapsActivity mapsActivity;
    private int idMapa;

    public PrincipalViewModelo(FragmentManager fm, boolean setMapaInicial, MapsActivity mapsActivity, int idMapa) {
        this.mapsActivity = mapsActivity;
        this.setMapaInicial = setMapaInicial;
        this.fm = fm;
        this.idMapa = idMapa;
    }

    @Bindable
    public String getCep2() {
        return cep2;
    }

    public void setCep2(String cep) {
        this.cep2 = cep;
        notifyPropertyChanged(BR.cep2);
    }

    @Bindable
    public String getLogradouro2() {
        return logradouro2;
    }

    public void setLogradouro2(String logradouro) {
        this.logradouro2 = logradouro;
        notifyPropertyChanged(BR.logradouro2);
    }

    @Bindable
    public String getBairro2() {
        return bairro2;
    }

    public void setBairro2(String bairro) {
        this.bairro2 = bairro;
        notifyPropertyChanged(BR.bairro2);
    }

    @Bindable
    public String getCidade2() {
        return cidade2;
    }

    public void setCidade2(String cidade) {
        this.cidade2 = cidade;
        notifyPropertyChanged(BR.cidade2);
    }

    @Bindable
    public String getUf2() {
        return uf2;
    }

    public void setUf2(String uf) {
        this.uf2 = uf;
        notifyPropertyChanged(BR.uf2);
    }

    @Bindable
    public Double getLat2() {
        return lat2;
    }

    public void setLat2(Double lat) {
        this.lat2 = lat;
        notifyPropertyChanged(BR.lat2);
    }

    @Bindable
    public Double getLng2() {
        return lng2;
    }

    public void setLng2(Double lng) {
        this.lng2 = lng;
        notifyPropertyChanged(BR.lng2);
    }

    @Bindable
    public boolean isPb2() {
        return pb2;
    }

    public void setPb2(boolean pb) {
        this.pb2 = pb;
        notifyPropertyChanged(BR.pb2);
    }

    public void setMapa(Double lat, Double lng, float zoom) {
        SupportMapFragment map = (SupportMapFragment) fm.findFragmentById(idMapa);

        mapsActivity.inicializa(lat, lng, zoom, this.cep2);

        map.getMapAsync(mapsActivity);
    }
}
