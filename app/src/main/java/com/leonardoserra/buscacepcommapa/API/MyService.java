package com.leonardoserra.buscacepcommapa.API;

import com.leonardoserra.buscacepcommapa.bean.MapsGoogle;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface MyService {

    @GET("/maps/api/geocode/json?sensor=false")
    Call<MapsGoogle> obterMapsGoogle(@Query("address") String cep);
}
