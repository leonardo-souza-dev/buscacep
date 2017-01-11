package com.leonardoserra.buscacepcommapa;

import com.leonardoserra.buscacepcommapa.models.MapsGoogle;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by vava on 1/10/2017.
 */

public interface MapsGoogleService {

    public static final String BASE_URL = "http://maps.google.com.br/";

    @GET("/maps/api/geocode/json?sensor=false")
    Call<MapsGoogle> obterMapsGoogle(@Query("address") String cep);
}
