package com.leonardoserra.buscacepcommapa.vm;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.maps.SupportMapFragment;
//import com.leonardoserra.buscacepcommapa.BR;
import com.leonardoserra.buscacepcommapa.MapsActivity;

public class MainViewModel extends BaseObservable {

    private boolean pb;
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
    private Double lat;
    private Double lng;

    private boolean setMapaInicial;
    private FragmentManager fm;
    private MapsActivity mapsActivity;
    private int idMapa;

    public MainViewModel(FragmentManager fm, boolean setMapaInicial, MapsActivity mapsActivity, int idMapa) {
        this.mapsActivity = mapsActivity;
        this.setMapaInicial = setMapaInicial;
        this.fm = fm;
        this.idMapa = idMapa;
    }

    @Bindable
    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
        //notifyPropertyChanged(BR.cep);
    }

    @Bindable
    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
        //notifyPropertyChanged(BR.logradouro);
    }

    @Bindable
    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
        //notifyPropertyChanged(BR.bairro);
    }

    @Bindable
    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
        //notifyPropertyChanged(BR.cidade);
    }

    @Bindable
    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
        //notifyPropertyChanged(BR.uf);
    }

    @Bindable
    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
        //notifyPropertyChanged(BR.lat);
    }

    @Bindable
    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
        //notifyPropertyChanged(BR.lng);
    }

    @Bindable
    public boolean isPb() {
        return pb;
    }

    public void setPb(boolean pb) {
        this.pb = pb;
        //notifyPropertyChanged(BR.pb);
    }

    public void setMapa(Double lat, Double lng, float zoom) {
        SupportMapFragment map = (SupportMapFragment) fm.findFragmentById(idMapa);

        //if (!setMapaInicial)
        mapsActivity.inicializa(lat, lng, zoom, this.cep);
        //else
        //mapsActivity.setMapaInicial();

        map.getMapAsync(mapsActivity);

    }
/*
    private Context context = null;
    private Endereco endereco = new Endereco();
    private boolean faltaInternet = false;
    private final Double LAT_PADRAO = 40.0;
    private final Double LNG_PADRAO = 40.0;
    private String cep;
    public MainViewModel(Context contextPassado){
        context = contextPassado;
    }

    public Endereco getEnderecoMapsGoogle(String cepPesquisado){

        cep = cepPesquisado;

        if (!cepPesquisado.equals("")) {

            getRetrofit();

            return endereco;

        } else{
            return null;
        }
    }

    public Endereco getEnderecoRepublica(String cepPesquisado){

        if (!cepPesquisado.equals("")) {

            getRepublica();

            return endereco;

        } else{
            return null;
        }
    }

    private void getRetrofit(){

        MyService service = retrofit.create(MyService.class);

        Call<MapsGoogle> requestMapsGoogle = service.obterMapsGoogle(cep);

        requestMapsGoogle.enqueue(new Callback<MapsGoogle>() {

            @Override
            public void onResponse(Call<MapsGoogle> call, Response<MapsGoogle> response) {

                if (!response.isSuccess()){
                    Log.i("LOGECP", "ERRO:" + response.code());
                }
                else {
                    MapsGoogle mapsGoogle = response.body();

                    if (mapsGoogle.results.get(0).address_components.get(1).types != null
                            && mapsGoogle.results.get(0).address_components.get(1).types.size() > 1
                            && mapsGoogle.results.get(0).address_components.get(1).types.get(1).equals("sublocality")) {
                        endereco.setBairro(mapsGoogle.results.get(0).address_components.get(1).long_name);
                        endereco.setCidade(mapsGoogle.results.get(0).address_components.get(2).long_name);
                        endereco.setUf(mapsGoogle.results.get(0).address_components.get(3).short_name);
                    }
                    if (mapsGoogle.results.get(0).address_components.get(2).types != null
                            && mapsGoogle.results.get(0).address_components.get(2).types.size() > 2
                            && mapsGoogle.results.get(0).address_components.get(2).types.get(2).contains("sublocality")) {
                        endereco.setBairro(mapsGoogle.results.get(0).address_components.get(2).long_name);
                        endereco.setCidade(mapsGoogle.results.get(0).address_components.get(3).long_name);
                        endereco.setUf(mapsGoogle.results.get(0).address_components.get(4).short_name);
                    }
                    endereco.setLat(mapsGoogle.results.get(0).geometry.location.lat);
                    endereco.setLng(mapsGoogle.results.get(0).geometry.location.lng);

                    //insereEnderecoNoHistorico();
                }
            }

            @Override
            public void onFailure(Call<MapsGoogle> call, Throwable t) {
                Log.e("LOGCEP", "ERRO: " + t.getMessage());
            }
        });
    }

    private void getRepublica(){

        MainViewModel.BuscarCepTask task = new MainViewModel.BuscarCepTask();
        task.execute(cep);
    }
*/
//    private void mergeEndereco() {
//        if (api1get && api2get) {
//            String historicoSp = sp.getString("historico", null);
//            ArrayList<Endereco> enderecos = new ArrayList<>();
//
//            if (historicoSp != null) {
//                Gson gson = new Gson();
//                enderecos = gson.fromJson(historicoSp, new TypeToken<ArrayList<Endereco>>() {
//                }.getType());
//            }
//            enderecos.add(endereco);
//            editor.putString("historico", new Gson().toJson(enderecos));
//
//            editor.apply();
//
//            api1get = false;
//            api2get = false;
//        }
//    }
/*

*/
}
