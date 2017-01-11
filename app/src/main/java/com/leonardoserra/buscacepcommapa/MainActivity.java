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
import android.util.Log;
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
import com.leonardoserra.buscacepcommapa.models.MapsGoogle;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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

    private MapsGoogle mapsGoogle;
    private Endereco endereco = new Endereco();

    public void buscar(View view) {

        cepPesquisado = cepEditText.getText().toString();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        if (!cepPesquisado.equals("")) {

            String url = MapsGoogleService.BASE_URL;

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            MapsGoogleService service = retrofit.create(MapsGoogleService.class);

            Call<MapsGoogle> requestMapsGoogle = service.obterMapsGoogle(cepPesquisado);
            requestMapsGoogle.enqueue(new Callback<MapsGoogle>() {
                @Override
                public void onResponse(Call<MapsGoogle> call, Response<MapsGoogle> response) {
                    if (!response.isSuccess()){
                        Log.i("LOGECP", "ERRO:" + response.code());
                    }
                    else {
                        mapsGoogle = response.body();

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


                        bairroEditText.setText(endereco.getBairro());
                        cidadeEditText.setText(endereco.getCidade());
                        ufEditText.setText(endereco.getUf());

                        api1get = true;
                        insereEnderecoNoHistorico();
                        atualizarMapa(endereco.getLat(), endereco.getLng(), false);
                    }
                }

                @Override
                public void onFailure(Call<MapsGoogle> call, Throwable t) {
                    Log.e("LOGCEP", "ERRO: " + t.getMessage());
                }
            });

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

    private boolean api1get = false;
    private boolean api2get = false;
    private boolean cepNaoEncontrado = false;

    private void insereEnderecoNoHistorico() {
        if (cepNaoEncontrado || (api1get && api2get)) {
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

            api1get = false;
            api2get = false;
        }
    }

    private void populaCampos(Endereco endereco) {

        cepEditText.setText(endereco.getCep());
        bairroEditText.setText(endereco.getBairro());
        cidadeEditText.setText(endereco.getCidade());
        ufEditText.setText(endereco.getUf());

        logradouroEditText.setText(endereco.getLogradouro());

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
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.parece_que_nao_ha), Toast.LENGTH_LONG).show();
                return;
            }

            if (s == null)
                Toast.makeText(MainActivity.this, R.string.erro_ao_buscar_cep, Toast.LENGTH_LONG).show();
            else {

                try {
                    JSONObject resultadoJson = new JSONObject(s);

                    String resultado = resultadoJson.getString("resultado");

                    if (resultado.equals("0")) {
                        Toast.makeText(MainActivity.this, R.string.cep_nao_encontrado, Toast.LENGTH_LONG).show();

                        endereco.setCep(cepPesquisado);
                        endereco.setLat(LAT_PADRAO);
                        endereco.setLng(LNG_PADRAO);

                        cepNaoEncontrado = true;
                        insereEnderecoNoHistorico();
                        populaCampos(endereco);

                        Vibrator vs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vs.vibrate(1000);

                        return;
                    }

                    String tipoLogradouro = resultadoJson.getString("tipo_logradouro");
                    String logradouro = tipoLogradouro + " " + resultadoJson.getString("logradouro");

                    endereco.setCep(cepPesquisado);
                    endereco.setLogradouro(logradouro);

                    api2get = true;
                    insereEnderecoNoHistorico();
                    populaCampos(endereco);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
