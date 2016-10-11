package com.leonardoserra.buscacepcommapa;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private EditText cepEditText;
    private EditText logradouroEditText;
    private EditText bairroEditText;
    private EditText cidadeEditText;
    private EditText ufEditText;
    private SupportMapFragment map;
    private String historicoPesquisa;
    private boolean faltaInternet = false;
    private String cep;
    private ScrollView resultadoScrollView;
    private RelativeLayout layoutTop;
    private TextView localidadeTextView;
    private TextWatcher tw = new TextWatcher() {
        private CepDigitadoListener cepDigitadoListener = new CepDigitadoListener();

        public void afterTextChanged(Editable s) {
            Log.d("after", s.toString());
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d("before", s.toString());
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Integer tamanhoTextoAnterior = cepDigitadoListener.obterTamanhoTextoAnterior();

            //int qtd = s.length();
            //String novoCaracter = s.toString().substring(qtd - 1, qtd);
            //cepDigitadoListener.addCaracter(novoCaracter);
            String localidade = cepDigitadoListener.getLocalidade(s.toString());

            localidadeTextView.setText(localidade);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        localidadeTextView = (TextView) findViewById(R.id.localidadeTextView);

        cepEditText = (EditText) findViewById(R.id.cepEditText);
        cepEditText.addTextChangedListener(tw);
        logradouroEditText = (EditText) findViewById(R.id.logradouroEditText);
        bairroEditText = (EditText) findViewById(R.id.bairroEditText);
        cidadeEditText = (EditText) findViewById(R.id.cidadeEditText);
        ufEditText = (EditText) findViewById(R.id.ufEditText);
        resultadoScrollView = (ScrollView) findViewById(R.id.resultadoScrollView);
        layoutTop = (RelativeLayout) findViewById(R.id.layout_top);

        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        Bundle bundle = getIntent() != null ? getIntent().getExtras() : null;
        historicoPesquisa = bundle != null ? bundle.getString("cep_historico") : null;

        if (historicoPesquisa == null)
            atualizarMapa(40, 40, true);
        else
            buscar(null);



        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0,70,0,0);
        layoutTop.setLayoutParams(params);
    }

    private void atualizarMapa(double lat, double lng, boolean setMapaInicial) {
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapsActivity activity = new MapsActivity();

        if (!setMapaInicial)
            activity.inicializa(lat, lng, cep);
        else
            activity.setMapaInicial();

        map.getMapAsync(activity);
    }

    public void buscar(View view) {

        if (historicoPesquisa != null) {
            cep = historicoPesquisa;
            cepEditText.setText(cep);
        } else
            cep = cepEditText.getText().toString();

        historicoPesquisa = null;

        SharedPreferences sp = getSharedPreferences("cepleo", MODE_PRIVATE);
        String historicoStr = sp.getString("historico", null);
        SharedPreferences.Editor e = sp.edit();

        if (historicoStr == null) {
            e.putString("historico", cep + ";");
        } else {
            e.putString("historico", historicoStr + cep + ";");
        }

        e.commit();

        BuscarCepTask task = new BuscarCepTask();
        task.execute(cep);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_historico) {
            Intent i = new Intent(this, HistoricoActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class CepDigitadoListener {

        private String localidade = "";

        private ArrayList<String> cepDigitado = new ArrayList<>();

        public Integer obterTamanhoTextoAnterior() {
            return cepDigitado.size();
        }

        public String getLocalidade(String texto) {
            String localidade;

            String primeiroCaracter = texto.substring(0, 1);
            String segundoCaracter = texto.length() > 1 ? texto.substring(1, 2) : "";
            String terceiroCaracter = texto.length() > 2 ? texto.substring(2, 3) : "";
            switch (primeiroCaracter) {
                case "0":
                    localidade = "Grande São Paulo";
                    switch (segundoCaracter) {
                        case "1":
                            localidade = "Centro (Sé e República)/ Bom Retiro/ Vila Buarque e Sumaré/ Consolação/ Jardins/ Liberdade";
                            switch (terceiroCaracter) {
                                case "0":
                                    localidade = "Centro (Sé e República)";
                                    break;
                                case "1":
                                    localidade = "Bom Retiro";
                                    break;
                                case "2":
                                    localidade = "Vila Buarque e Sumaré";
                                    break;
                                case "3":
                                    localidade = "Consolação";
                                    break;
                                case "4":
                                    localidade = "Jardins";
                                    break;
                                case "5":
                                    localidade = "Liberdade";
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case "2":
                            localidade = "Santana e Vila Guilherme, Vila Maria, Jaçanã e Tucuruvi, Tremembé, Mandaqui, Casa Verde, Cachoeirinha, Limão, Brasilândia, Freguesia do Ó";
                            switch (terceiroCaracter) {
                                case "0":
                                    localidade = "Santana e Vila Guilherme";
                                    break;
                                case "1":
                                    localidade = "Vila Maria";
                                    break;
                                case "2":
                                    localidade = "Jaçanã e Tucuruvi";
                                    break;
                                case "3":
                                    localidade = "Tremembé";
                                    break;
                                case "4":
                                    localidade = "Mandaqui";
                                    break;
                                case "5":
                                    localidade = "Casa Verde";
                                    break;
                                case "6":
                                    localidade = "Cachoeirinha";
                                    break;
                                case "7":
                                    localidade = "Limão";
                                    break;
                                case "8":
                                    localidade = "Brasilândia";
                                    break;
                                case "9":
                                    localidade = "Freguesia do Ó";
                                    break;
                                default:
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case "1":
                    localidade = "Interior e litoral de São Paulo";
                    break;
                case "2":
                    localidade = "Rio de Janeiro";
                    break;
                case "3":
                    localidade = "Minas Gerais";
                    break;
                case "4":
                    localidade = "Bahia e Sergipe";
                    break;
                case "5":
                    localidade = "Pernambuco, Alagoas, Paraíba e Rio Grande do Norte";
                    break;
                case "6":
                    localidade = "Ceará, Piauí, Maranhão, Pará, Amapá, Amazonas, Acre e Roraima";
                    break;
                case "7":
                    localidade = "Distrito Federal, Goiás, Rondônia, Tocantins, Mato Grosso e Mato Grosso do Sul";
                    break;
                case "8":
                    localidade = "Paraná e Santa Catarina";
                    break;
                case "9":
                    localidade = "Rio Grande do Sul";
                    break;
                default:
                    localidade = "";
                    break;
            }
            return localidade;
        }

        private String obterSubRegiao(ArrayList<String> cepDigitado) {
            String localidadeIndice1;
            switch (cepDigitado.get(1)) {
                case "1":
                    localidadeIndice1 = "Centro (Sé e República)/ Bom Retiro/ Vila Buarque e Sumaré/ Consolação/ Jardins/ Liberdade";
                    break;
                default:
                    localidadeIndice1 = "x1";
                    break;
            }
            return localidadeIndice1;
        }

        private String obterSetor(ArrayList<String> cepDigitado) {
            String localidadeIndice2;
            switch (cepDigitado.get(2)) {
                case "1":
                    localidadeIndice2 = "Bom Retiro";
                    break;
                case "2":
                    localidadeIndice2 = "Vila Buarque e Sumaré";
                    break;
                case "3":
                    localidadeIndice2 = "Consolação";
                    break;
                case "4":
                    localidadeIndice2 = "Jardins";
                    break;
                case "5":
                    localidadeIndice2 = "Liberdade";
                    break;
                default:
                    localidadeIndice2 = "x2";
                    break;
            }
            return localidadeIndice2;
        }

    }

    private class BuscarCepTask extends AsyncTask<String, Integer, String> {

        private ProgressDialog progress;

        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(MainActivity.this, getString(R.string.aguarde), getString(R.string.buscando_cep));
        }

        @Override
        protected String doInBackground(String... params) {

            if (params.length < 1 || params[0] == null || params[0].equals(""))
                Toast.makeText(MainActivity.this, R.string.insira_um_cep, Toast.LENGTH_LONG).show();

            HttpURLConnection connection1 = null;
            HttpURLConnection connection2 = null;
            StringBuilder resposta = new StringBuilder();

            try {
                String termoBusca = params[0].trim().replace(",", "").replace("-", "").replace(".", "");

                URL url1 = new URL("http://cep.republicavirtual.com.br/web_cep.php?cep=" + termoBusca + "&formato=jsonp");
                connection1 = (HttpURLConnection) url1.openConnection();
                connection1.setRequestMethod("GET");
                connection1.setRequestProperty("Accept", "application/json");

                String url2 = "http://maps.google.com/maps/api/geocode/json?address=" + termoBusca + "&sensor=false";
                connection2 = (HttpURLConnection) new URL(url2).openConnection();
                connection2.setRequestMethod("GET");
                connection2.setRequestProperty("Accept", "application/json");

                if (connection1.getResponseCode() == 200 && connection2.getResponseCode() == 200) {
                    faltaInternet = false;

                    BufferedReader stream1 = new BufferedReader(new InputStreamReader(connection1.getInputStream()));
                    BufferedReader stream2 = new BufferedReader(new InputStreamReader(connection2.getInputStream()));

                    String linha;
                    while ((linha = stream1.readLine()) != null) {
                        resposta.append(linha);
                    }
                    resposta.append('|');
                    while ((linha = stream2.readLine()) != null) {
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
                if (connection2 != null)
                    connection2.disconnect();
            }

            return resposta.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            progress.dismiss();

            if (faltaInternet) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.parece_que_nao_ha), Toast.LENGTH_LONG).show();
                return;
            }

            String[] jsons = new String[0];
            if (s == null)
                Toast.makeText(MainActivity.this, R.string.erro_ao_buscar_cep, Toast.LENGTH_LONG).show();
            else {
                jsons = s.split(Pattern.quote("|"));
            }

            try {
                JSONObject resultadoJson = new JSONObject(jsons[0]);
                JSONObject statusJson = new JSONObject(jsons[1]);

                String resultado = resultadoJson.getString("resultado");
                String status = statusJson.getString("status");

                if (resultado.equals("0") || status.equals("ZERO_RESULTS")) {
                    Toast.makeText(MainActivity.this, R.string.cep_nao_encontrado,Toast.LENGTH_LONG).show();

                    logradouroEditText.setText("");
                    bairroEditText.setText("");
                    cidadeEditText.setText("");
                    ufEditText.setText("");

                    Vibrator vs = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    vs.vibrate(1000);

                    return;
                }

                String tipoLogradouro = resultadoJson.getString("tipo_logradouro");
                String logradouro = tipoLogradouro + " " + resultadoJson.getString("logradouro");
                String bairro1 = resultadoJson.getString("bairro");
                String cidade1 = resultadoJson.getString("cidade");
                String uf1 = resultadoJson.getString("uf");

                JSONArray results = statusJson.getJSONArray("results");
                JSONObject root = results.getJSONObject(0);
                JSONArray addressComponents = (JSONArray) root.get("address_components");
                String bairro2 = addressComponents.getJSONObject(1).get("long_name").toString();
                String cidade2 = addressComponents.getJSONObject(2).get("long_name").toString();
                String uf2 = addressComponents.getJSONObject(3).get("short_name").toString();

                resultadoScrollView.setVisibility(View.VISIBLE);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0,0,0,0);
                layoutTop.setLayoutParams(params);

                logradouroEditText.setText(logradouro);
                bairroEditText.setText(bairro1 == null ? bairro2 : bairro1);
                cidadeEditText.setText(cidade1 == null ? cidade2 : cidade1);
                ufEditText.setText(uf1 == null ? uf2 : uf1);

                double lat = (double)root.getJSONObject("geometry").getJSONObject("location").get("lat");
                double lng = (double)root.getJSONObject("geometry").getJSONObject("location").get("lng");



                atualizarMapa(lat, lng, false);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
