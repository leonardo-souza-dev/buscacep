package com.leonardoserra.buscacepcommapa.vm;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.v4.app.FragmentManager;
import com.leonardoserra.buscacepcommapa.BR;
import com.google.android.gms.maps.SupportMapFragment;
import com.leonardoserra.buscacepcommapa.MapsActivity;

public class MainViewModel extends BaseObservable {

    private boolean progressBar;
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
    private Double lat;
    private Double lng;

    private FragmentManager fm;
    private MapsActivity mapsActivity;
    private int idMapa;

    public MainViewModel(FragmentManager fm, MapsActivity mapsActivity, int idMapa) {
        this.mapsActivity = mapsActivity;
        this.fm = fm;
        this.idMapa = idMapa;
    }

    @Bindable
    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
        notifyPropertyChanged(BR.cep);
    }

    @Bindable
    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
        notifyPropertyChanged(BR.logradouro);
    }

    @Bindable
    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
        notifyPropertyChanged(BR.bairro);
    }

    @Bindable
    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
        notifyPropertyChanged(BR.cidade);
    }

    @Bindable
    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
        notifyPropertyChanged(BR.uf);
    }

    @Bindable
    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
        notifyPropertyChanged(BR.lat);
    }

    @Bindable
    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
        notifyPropertyChanged(BR.lng);
    }

    @Bindable
    public boolean isProgressBar() {
        return progressBar;
    }

    public void setProgressBar(boolean progressBar) {
        this.progressBar = progressBar;
        notifyPropertyChanged(BR.progressBar);
    }

    public void setMapa(Double lat, Double lng) {
        SupportMapFragment map = (SupportMapFragment) fm.findFragmentById(idMapa);

        mapsActivity.inicializa(lat, lng, 17.0f, this.cep);

        map.getMapAsync(mapsActivity);
    }

    public void setMapaInicial() {
        SupportMapFragment map = (SupportMapFragment) fm.findFragmentById(idMapa);

        Double LAT_PADRAO = 40.0;
        Double LNG_PADRAO = 40.0;
        mapsActivity.inicializa(LAT_PADRAO, LNG_PADRAO, 1.0f, this.cep);

        map.getMapAsync(mapsActivity);
    }
}
