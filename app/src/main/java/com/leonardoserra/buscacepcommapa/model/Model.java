package com.leonardoserra.buscacepcommapa.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leonardoserra.buscacepcommapa.API.MapsGoogleService;
import com.leonardoserra.buscacepcommapa.bean.Endereco;
import com.leonardoserra.buscacepcommapa.R;
import com.leonardoserra.buscacepcommapa.bean.AddressComponent;
import com.leonardoserra.buscacepcommapa.bean.MapsGoogle;
import com.leonardoserra.buscacepcommapa.bean.Result;
import com.leonardoserra.buscacepcommapa.net.ServiceGenerator;
import com.leonardoserra.buscacepcommapa.vm.MainViewModel;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Model {

    private MapsGoogleService mapsGoogleService;
    private MainViewModel viewModel;
    private Context context;
    private String cep;
    private Boolean faltaInternet;
    private boolean api1get = false;
    private boolean api2get = false;
    private boolean cepNaoEncontrado = false;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Endereco endereco = new Endereco();
    private String cepp;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
    private Double lat;
    private Double lng;

    public Model(MainViewModel viewModel, Context contextoPassado, SharedPreferences sp) {
        this.viewModel = viewModel;
        this.mapsGoogleService = ServiceGenerator.createService(MapsGoogleService.class);
        this.context = contextoPassado;
        this.sp = sp;
    }

    public void busca(String cepPesquisado) {

        if (!cepPesquisado.equals("")) {
            cep = cepPesquisado;
            getMapsGoogle(cepPesquisado);
            getRepublica(cepPesquisado);
        }
    }

    private void insereEnderecoNoHistorico() {

        if (cepNaoEncontrado) {

        } else {
            if (api1get && api2get) {
                String historicoSp = sp.getString("historico", null);
                ArrayList<Endereco> enderecos = new ArrayList<>();

                if (historicoSp != null) {
                    Gson gson = new Gson();
                    enderecos = gson.fromJson(historicoSp, new TypeToken<ArrayList<Endereco>>() {
                    }.getType());
                }

                endereco.setCep(cepp);
                endereco.setLogradouro(logradouro);
                endereco.setBairro(bairro);
                endereco.setCidade(cidade);
                endereco.setUf(uf);
                endereco.setLat(lat);
                endereco.setLng(lng);

                enderecos.add(endereco);
                editor = sp.edit();
                editor.putString("historico", new Gson().toJson(enderecos));

                editor.apply();

                api1get = false;
                api2get = false;
            }
        }
    }

    private void getMapsGoogle(String cepPesquisado) {

        Call<MapsGoogle> call = mapsGoogleService.obterMapsGoogle(cepPesquisado);
        call.enqueue(new Callback<MapsGoogle>() {
            @Override
            public void onResponse(Response<MapsGoogle> response) {
                MapsGoogle model = response.body();

                if (model == null) {
                    cepNaoEncontrado = true;

                    ResponseBody responseBody = response.errorBody();
                    if (responseBody != null) {
                        try {
                            Log.e("BUSCACEPLOG", responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("BUSCACEPLOG", "ERRO");
                    }
                } else {

                    Result result = model.getResults().get(0);
                    List<AddressComponent> addressComponent = result.address_components;

                    if (addressComponent.get(1).types != null
                            && addressComponent.get(1).types.size() > 1
                            && addressComponent.get(1).types.get(1).equals("sublocality")) {

                        bairro = addressComponent.get(1).long_name;
                        viewModel.setBairro(bairro);

                        cidade = addressComponent.get(2).long_name;
                        viewModel.setCidade(cidade);

                        uf = addressComponent.get(3).short_name;
                        viewModel.setUf(uf);
                    }
                    if (addressComponent.get(2).types != null
                            && addressComponent.get(2).types.size() > 2
                            && addressComponent.get(2).types.get(2).contains("sublocality")) {

                        bairro = addressComponent.get(2).long_name;
                        viewModel.setBairro(bairro);

                        cidade = addressComponent.get(3).long_name;
                        viewModel.setCidade(cidade);

                        uf = addressComponent.get(4).short_name;
                        viewModel.setUf(uf);
                    }

                    lat = result.geometry.location.lat;
                    viewModel.setLat(lat);

                    lng = result.geometry.location.lng;
                    viewModel.setLng(lng);

                    viewModel.setMapa(lat, lng);
                    api1get = true;
                    insereEnderecoNoHistorico();
                }
                viewModel.setProgressBar(false);
            }

            @Override
            public void onFailure(Throwable t) {
                viewModel.setBairro("t = " + t.getMessage());
                viewModel.setProgressBar(false);
            }
        });
    }

    private void getRepublica(String cepPesquisado) {
        BuscarCepTask task = new BuscarCepTask();
        task.execute(cepPesquisado);
    }

    private class BuscarCepTask extends AsyncTask<String, Integer, String> {

        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(context, context.getString(R.string.aguarde), context.getString(R.string.buscando_cep));
        }

        @Override
        protected String doInBackground(String... params) {

            if (params.length < 1 || params[0] == null || params[0].equals(""))
                Toast.makeText(context, R.string.insira_um_cep, Toast.LENGTH_LONG).show();

            HttpURLConnection connection = null;
            StringBuilder resposta = new StringBuilder();

            try {
                String termoBusca = params[0].trim().replace(",", "").replace("-", "").replace(".", "");

                URL url = new URL("http://cep.republicavirtual.com.br/web_cep.php?cep=" + termoBusca + "&formato=jsonp");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(5000);

                if (connection.getResponseCode() == 200) {
                    faltaInternet = false;

                    BufferedReader stream1 = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String linha;
                    while ((linha = stream1.readLine()) != null) {
                        resposta.append(linha);
                    }
                    api2get = true;
                    insereEnderecoNoHistorico();
                } else {
                    cepNaoEncontrado = true;
                }

            } catch (UnknownHostException | ConnectException e) {
                faltaInternet = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
            }

            return resposta.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            progress.dismiss();

            if (faltaInternet) {
                Toast.makeText(context, context.getResources().getString(R.string.parece_que_nao_ha), Toast.LENGTH_LONG).show();
                return;
            }

            if (s == null)
                Toast.makeText(context, R.string.erro_ao_buscar_cep, Toast.LENGTH_LONG).show();
            else {

                try {
                    JSONObject resultadoJson = new JSONObject(s);

                    String resultado = resultadoJson.getString("resultado");

                    if (resultado.equals("0")) {
                        Toast.makeText(context, R.string.cep_nao_encontrado, Toast.LENGTH_LONG).show();

                        viewModel.setCep(cep);
                        Double LAT_PADRAO = 40.0;
                        viewModel.setLat(LAT_PADRAO);
                        Double LNG_PADRAO = 40.0;
                        viewModel.setLng(LNG_PADRAO);

                        Vibrator vs = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        vs.vibrate(1000);

                        return;
                    }

                    String tipoLogradouro = resultadoJson.getString("tipo_logradouro");
                    String logradouroo = tipoLogradouro + " " + resultadoJson.getString("logradouro");

                    cepp = cep;
                    viewModel.setCep(cepp);
                    logradouro = logradouroo;
                    viewModel.setLogradouro(logradouroo);

                    insereEnderecoNoHistorico();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
