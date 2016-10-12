package com.leonardoserra.buscacepcommapa;

public class Endereco {

    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
    private Double lat;
    private Double lng;

    public Endereco(String pCep, String pLogradouro, String pBairro, String pCidade, String pUf, Double pLat, Double pLng) {
        this.cep = pCep;
        this.logradouro = pLogradouro;
        this.bairro = pBairro;
        this.cidade = pCidade;
        this.uf = pUf;
        this.lat = pLat;
        this.lng = pLng;
    }

    public String getResultado() {
        return this.logradouro + ", " + this.bairro + ", " + this.cidade + " - " + this.uf;
    }


    public String getCep() {
        return this.cep;
    }

    public String getLogradouro() {
        return this.logradouro;
    }

    public String getBairro() {
        return this.bairro;
    }

    public String getCidade() {
        return this.cidade;
    }

    public String getUf() {
        return this.uf;
    }

    public Double getLat() {
        return this.lat;
    }

    public Double getLng() {
        return this.lng;
    }
}
