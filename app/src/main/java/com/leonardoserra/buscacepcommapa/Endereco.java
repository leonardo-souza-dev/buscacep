package com.leonardoserra.buscacepcommapa;

public class Endereco {

    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
    private Double lat;
    private Double lng;

    public Endereco(){}
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
        if (!this.logradouro.isEmpty() && !this.bairro.isEmpty() && !this.cidade.isEmpty() && !this.uf.isEmpty()) {
            return this.logradouro + ", " + this.bairro + ", " + this.cidade + " - " + this.uf;
        } else {
            return "";
        }
    }

    public String getCep() {
        return this.cep;
    }

    public void setCep(String cep){
        this.cep = cep;
    }

    public String getLogradouro() {
        return this.logradouro;
    }

    public void setLogradouro(String logradouro){
        this.logradouro = logradouro;
    }

    public String getBairro() {
        return this.bairro;
    }

    public void setBairro(String bairro){
        this.bairro = bairro;
    }

    public String getCidade() {
        return this.cidade;
    }

    public void setCidade(String cidade){
        this.cidade = cidade;
    }

    public String getUf() {
        return this.uf;
    }

    public void setUf(String uf){
        this.uf = uf;
    }

    public Double getLat() {
        return this.lat;
    }

    public void setLat(Double lat){
        this.lat = lat;
    }

    public Double getLng() {
        return this.lng;
    }

    public void setLng(Double lng){
        this.lng = lng;
    }
}
