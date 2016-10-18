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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

import br.com.jansenfelipe.androidmask.MaskEditTextChangedListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final Double LAT_PADRAO = 40.0;
    private final Double LNG_PADRAO = 40.0;
    private EditText cepEditText;
    private EditText logradouroEditText;
    private EditText bairroEditText;
    private EditText cidadeEditText;
    private EditText ufEditText;
    private ScrollView resultadoScrollView;
    private boolean faltaInternet = false;
    private String cepPesquisado;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

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


        sp = getSharedPreferences("cepleo", MODE_PRIVATE);
        editor = sp.edit();

        resultadoScrollView = (ScrollView) findViewById(R.id.resultadoScrollView);
        cepEditText = (EditText) findViewById(R.id.cepEditText);
        logradouroEditText = (EditText) findViewById(R.id.logradouroEditText);
        bairroEditText = (EditText) findViewById(R.id.bairroEditText);
        cidadeEditText = (EditText) findViewById(R.id.cidadeEditText);
        ufEditText = (EditText) findViewById(R.id.ufEditText);

        MaskEditTextChangedListener mascaraCep = new MaskEditTextChangedListener("#####-###", cepEditText);
        cepEditText.addTextChangedListener(mascaraCep);
        cepEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // TODO do something
                    handled = true;
                    buscar(v);
                }
                return handled;
            }
        });

        Bundle bundle = getIntent() != null ? getIntent().getExtras() : null;
        String historicoEndereco = bundle != null ? bundle.getString("historico_endereco") : null;

        if (historicoEndereco == null)
            atualizarMapa(LAT_PADRAO, LNG_PADRAO, true);
        else {
            Gson gson = new Gson();
            populaCampos(gson.fromJson(historicoEndereco, Endereco.class));
            resultadoScrollView.requestFocus();
        }
    }

    private void atualizarMapa(double lat, double lng, boolean setMapaInicial) {
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapsActivity activity = new MapsActivity();

        if (!setMapaInicial)
            activity.inicializa(lat, lng, cepPesquisado);
        else
            activity.setMapaInicial();

        map.getMapAsync(activity);
    }

    public void vaiAoTopo(View view) {
        resultadoScrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    public void buscar(View view) {

        cepPesquisado = cepEditText.getText().toString();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        if (!cepPesquisado.equals("")) {
            BuscarCepTask task = new BuscarCepTask();
            task.execute(cepPesquisado);
        }
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

    private void insereEnderecoNoHistorico(Endereco endereco) {

        String historicoSp = sp.getString("historico", null);
        ArrayList<Endereco> enderecos = new ArrayList<>();

        if (historicoSp != null) {
            Gson gson = new Gson();
            enderecos = gson.fromJson(historicoSp, new TypeToken<ArrayList<Endereco>>() {
            }.getType());
        }
        enderecos.add(endereco);
        editor.putString("historico", new Gson().toJson(enderecos));

        editor.apply();
    }

    private void populaCampos(Endereco endereco) {

        cepEditText.setText(endereco.getCep());

        logradouroEditText.setText(endereco.getLogradouro());
        bairroEditText.setText(endereco.getBairro());
        cidadeEditText.setText(endereco.getCidade());
        ufEditText.setText(endereco.getUf());

        atualizarMapa(endereco.getLat(), endereco.getLng(), false);
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
                connection1.setConnectTimeout(5000);

                String url2 = "http://maps.google.com/maps/api/geocode/json?address=" + termoBusca + "&sensor=false";
                connection2 = (HttpURLConnection) new URL(url2).openConnection();
                connection2.setRequestMethod("GET");
                connection2.setRequestProperty("Accept", "application/json");
                connection2.setConnectTimeout(5000);


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

                    Endereco endereco = new Endereco(cepPesquisado, "", "", "", "", LAT_PADRAO, LNG_PADRAO);

                    insereEnderecoNoHistorico(endereco);
                    populaCampos(endereco);

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

                String bairro = bairro1 == null ? bairro2 : bairro1;
                String cidade = cidade1 == null ? cidade2 : cidade1;
                String uf = uf1 == null ? uf2 : uf1;

                //String historicoResultado = logradouro + ", " + bairro + ", " + cidade + " - " + uf;

                double lat = (double)root.getJSONObject("geometry").getJSONObject("location").get("lat");
                double lng = (double)root.getJSONObject("geometry").getJSONObject("location").get("lng");

                Endereco endereco = new Endereco(cepPesquisado, logradouro, bairro, cidade, uf, lat, lng);

                insereEnderecoNoHistorico(endereco);
                populaCampos(endereco);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
