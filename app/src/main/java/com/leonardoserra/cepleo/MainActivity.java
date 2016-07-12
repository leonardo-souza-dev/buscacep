package com.leonardoserra.cepleo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private EditText edtCep;
    private EditText edtLogradouro;
    private EditText edtBairro;
    private EditText edtCidade;
    private EditText edtUf;
    private String gCepHistorico;

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

        edtCep = (EditText)findViewById(R.id.edtCep);
        edtLogradouro = (EditText)findViewById(R.id.edtLogradouro);
        edtBairro = (EditText)findViewById(R.id.edtBairro);
        edtCidade = (EditText)findViewById(R.id.edtCidade);
        edtUf = (EditText)findViewById(R.id.edtUf);

        Bundle b = getIntent() != null ? getIntent().getExtras() : null;
        gCepHistorico = b != null ? b.getString("cep_historico") : null;

        if (gCepHistorico == null)
            atualizarMapa(40,40);
        else
            buscar(null);
    }

    private void atualizarMapa(double lat, double lng) {
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapsActivity activity = new MapsActivity();
        activity.inicializa(lat, lng, gCep);
        map.getMapAsync(activity);
    }

    private String gCep;

    public void buscar(View view) {

        if (gCepHistorico != null) {
            gCep = gCepHistorico;
            edtCep.setText(gCep);
        }
        else
            gCep = edtCep.getText().toString();

        gCepHistorico = null;

        SharedPreferences sp = getSharedPreferences("cepleo", MODE_PRIVATE);
        String historicoStr = sp.getString("historico", null);
        SharedPreferences.Editor e = sp.edit();
        if (historicoStr == null) {
            e.putString("historico", gCep + ";");
        } else {
            e.putString("historico", historicoStr + gCep + ";");
        }
        e.commit();

        BuscarCepTask task = new BuscarCepTask();
        task.execute(gCep);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_historico) {
            Intent i = new Intent(this, HistoricoActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class BuscarCepTask extends AsyncTask<String, Integer, String> {

        private ProgressDialog progress;

        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(MainActivity.this, getString(R.string.aguarde), getString(R.string.buscando_cep));
        }

        @Override
        protected String doInBackground(String... params) {

            if (params.length < 1 || params[0] == "" || params[0] == null)
                Toast.makeText(MainActivity.this, R.string.insira_um_cep, Toast.LENGTH_LONG).show();

            try {
                String termoBusca = params[0].trim().replace(",", "").replace("-", "").replace(".", "");

                HttpURLConnection connection1 =
                        (HttpURLConnection)new URL("http://cep.republicavirtual.com.br/web_cep.php?cep=" +
                                termoBusca + "&formato=jsonp").openConnection();
                connection1.setRequestMethod("GET");
                connection1.setRequestProperty("Accept", "application/json");

                HttpURLConnection connection2 =
                        (HttpURLConnection)new URL("http://maps.google.com/maps/api/geocode/json?address=" +
                                termoBusca +"&sensor=false").openConnection();
                connection2.setRequestMethod("GET");
                connection2.setRequestProperty("Accept", "application/json");

                if (connection1.getResponseCode() == 200 && connection2.getResponseCode() == 200) {
                    BufferedReader stream1 = new BufferedReader(new InputStreamReader(connection1.getInputStream()));
                    BufferedReader stream2 = new BufferedReader(new InputStreamReader(connection2.getInputStream()));

                    String linha = "";
                    StringBuilder resposta = new StringBuilder();
                    while ((linha = stream1.readLine()) != null) {
                        resposta.append(linha);
                    }
                    resposta.append('|');
                    while ((linha = stream2.readLine()) != null) {
                        resposta.append(linha);
                    }
                    connection1.disconnect();
                    connection2.disconnect();

                    String respostaStr = resposta.toString();

                    return respostaStr;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progress.dismiss();

            if (s == null)
                Toast.makeText(MainActivity.this, R.string.erro_ao_buscar_cep, Toast.LENGTH_LONG).show();

            try {
                String[] jsons = s.split(Pattern.quote("|"));

                JSONObject json1 = new JSONObject(jsons[0]);
                JSONObject json2 = new JSONObject(jsons[1]);

                String resultado1 = json1.getString("resultado");
                String resultado2 = json2.getString("status");

                if (resultado1.equals("0") || resultado2.equals("ZERO_RESULTS")) {
                    Toast.makeText(MainActivity.this, R.string.cep_nao_encontrado,Toast.LENGTH_LONG).show();

                    Vibrator vs = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    vs.vibrate(1000);

                    return;
                }

                String tipoLogradouro = json1.getString("tipo_logradouro");
                String logradouro = tipoLogradouro + " " +  json1.getString("logradouro");
                String bairro1 = json1.getString("bairro");
                String cidade1 = json1.getString("cidade");
                String uf1 = json1.getString("uf");


                JSONArray results = json2.getJSONArray("results");
                JSONObject root = results.getJSONObject(0);
                JSONArray addressComponents = (JSONArray) root.get("address_components");
                String bairro2 = addressComponents.getJSONObject(1).get("long_name").toString();
                String cidade2 = addressComponents.getJSONObject(2).get("long_name").toString();
                String uf2 = addressComponents.getJSONObject(3).get("short_name").toString();

                edtLogradouro.setText(logradouro);
                edtBairro.setText(bairro1 == null ? bairro2 : bairro1);
                edtCidade.setText(cidade1 == null ? cidade2 : cidade1);
                edtUf.setText(uf1 == null ? uf2 : uf1);

                double lat = (double)root.getJSONObject("geometry").getJSONObject("location").get("lat");
                double lng = (double)root.getJSONObject("geometry").getJSONObject("location").get("lng");

                atualizarMapa(lat, lng);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
