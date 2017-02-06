package com.leonardoserra.buscacepcommapa.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.widget.Toast;

import com.leonardoserra.buscacepcommapa.API.GitHubService;
import com.leonardoserra.buscacepcommapa.R;
import com.leonardoserra.buscacepcommapa.bean.AddressComponent;
import com.leonardoserra.buscacepcommapa.bean.MapsGoogle;
import com.leonardoserra.buscacepcommapa.bean.Result;
import com.leonardoserra.buscacepcommapa.net.ServiceGenerator;
import com.leonardoserra.buscacepcommapa.vm.PrincipalViewModelo;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyModel {

    private final Double LAT_PADRAO = 40.0;
    private final Double LNG_PADRAO = 40.0;
    private GitHubService git;
    private PrincipalViewModelo viewModel;
    private Context context;
    private String cep;
    private Boolean faltaInternet;

    public MyModel(PrincipalViewModelo viewModel, Context contextoPassado) {
        this.viewModel = viewModel;
        this.git = ServiceGenerator.createService(GitHubService.class);
        context = contextoPassado;
    }

    public void busca(String cepPesquisado) {

        if (!cepPesquisado.equals("")) {
            cep = cepPesquisado;
            getMapsGoogle(cepPesquisado);
            getRepublica(cepPesquisado);
        }
    }

    public void atualizaMapaInicial(double lat, double lng) {

        viewModel.setMapa(lat, lng, 1.0f);
    }

    private void getMapsGoogle(String cepPesquisado) {
        Call<MapsGoogle> call = git.obterMapsGoogle(cepPesquisado);
        call.enqueue(new Callback<MapsGoogle>() {
            @Override
            public void onResponse(Response<MapsGoogle> response) {
                MapsGoogle model = response.body();

                if (model == null) {
                    //404 or the response cannot be converted to MapsGoogle.
                    ResponseBody responseBody = response.errorBody();
                    if (responseBody != null) {
                        try {
                            viewModel.setBairro2("responseBody = " + responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        viewModel.setBairro2("responseBody  = null");
                    }
                } else {
                    //200
                    Result result = model.getResults().get(0);
                    List<AddressComponent> addressComponent = result.address_components;

                    if (addressComponent.get(1).types != null
                            && addressComponent.get(1).types.size() > 1
                            && addressComponent.get(1).types.get(1).equals("sublocality")) {
                        viewModel.setBairro2(addressComponent.get(1).long_name);
                        viewModel.setCidade2(addressComponent.get(2).long_name);
                        viewModel.setUf2(addressComponent.get(3).short_name);
                    }
                    if (addressComponent.get(2).types != null
                            && addressComponent.get(2).types.size() > 2
                            && addressComponent.get(2).types.get(2).contains("sublocality")) {
                        viewModel.setBairro2(addressComponent.get(2).long_name);
                        viewModel.setCidade2(addressComponent.get(3).long_name);
                        viewModel.setUf2(addressComponent.get(4).short_name);
                    }
                    viewModel.setLat2(result.geometry.location.lat);
                    viewModel.setLng2(result.geometry.location.lng);
                    viewModel.setMapa(result.geometry.location.lat, result.geometry.location.lng, 17.0f);
                }
                viewModel.setPb2(false);
            }

            @Override
            public void onFailure(Throwable t) {
                viewModel.setBairro2("t = " + t.getMessage());
                viewModel.setPb2(false);
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

            HttpURLConnection connection1 = null;
            StringBuilder resposta = new StringBuilder();

            try {
                String termoBusca = params[0].trim().replace(",", "").replace("-", "").replace(".", "");

                URL url1 = new URL("http://cep.republicavirtual.com.br/web_cep.php?cep=" + termoBusca + "&formato=jsonp");
                connection1 = (HttpURLConnection) url1.openConnection();
                connection1.setRequestMethod("GET");
                connection1.setRequestProperty("Accept", "application/json");
                connection1.setConnectTimeout(5000);

                if (connection1.getResponseCode() == 200) {
                    faltaInternet = false;

                    BufferedReader stream1 = new BufferedReader(new InputStreamReader(connection1.getInputStream()));

                    String linha;
                    while ((linha = stream1.readLine()) != null) {
                        resposta.append(linha);
                    }
                }

            } catch (UnknownHostException | ConnectException e) {
                faltaInternet = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection1 != null)
                    connection1.disconnect();
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

                        viewModel.setCep2(cep);
                        viewModel.setLat2(LAT_PADRAO);
                        viewModel.setLng2(LNG_PADRAO);

                        //cepNaoEncontrado = true;
                        //insereEnderecoNoHistorico();

                        Vibrator vs = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        vs.vibrate(1000);

                        return;
                    }

                    String tipoLogradouro = resultadoJson.getString("tipo_logradouro");
                    String logradouro = tipoLogradouro + " " + resultadoJson.getString("logradouro");

                    viewModel.setCep2(cep);
                    viewModel.setLogradouro2(logradouro);

                    //api2get = true;
                    //insereEnderecoNoHistorico();
                    //populaCampos(endereco);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
