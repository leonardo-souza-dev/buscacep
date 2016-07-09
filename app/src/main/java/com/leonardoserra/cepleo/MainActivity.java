package com.leonardoserra.cepleo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private EditText edtCep;
    private EditText edtLogradouro;
    private EditText edtComplemento;
    private EditText edtBairro;
    private EditText edtCidade;
    private EditText edtUf;
    private static final String LOG_TAG = "EXAMPLO_PROGRESSO";
    private GoogleMap mMap;
    //static final LatLng HAMBURG = new LatLng(53.558, 9.927);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
//        Marker hamburg = mMap.addMarker(new MarkerOptions().position(HAMBURG).title("Hamburg"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 15));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

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

        atualizarMapa(40,40);
    }

    private void atualizarMapa(double lat, double lng) {
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapsActivity activity = new MapsActivity();
        activity.setLatLng(lat, lng);
        map.getMapAsync(activity);
    }

    public void buscar(View view) {
        String lCep = edtCep.getText().toString();

        SharedPreferences sp = getSharedPreferences("cepleo", MODE_PRIVATE);
        String historicoStr = sp.getString("historico", null);
        SharedPreferences.Editor e = sp.edit();
        if (historicoStr == null) {
            e.putString("historico", lCep + ";");
        } else {
            e.putString("historico", historicoStr + lCep + ";");
        }
        e.commit();

        BuscarCepTask task = new BuscarCepTask();
        task.execute(lCep);
    }

    public void limpaPlaceHolder(View view) {
        edtCep = (EditText)findViewById(R.id.edtCep);
        edtCep.setText("");
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
            progress = ProgressDialog.show(MainActivity.this, "aguarde", "buscando cep");
        }

        @Override
        protected String doInBackground(String... params) {

            if (params.length < 1 || params[0] == "" || params[0] == null)
                Toast.makeText(MainActivity.this, "Insira um CEP", Toast.LENGTH_LONG).show();

            try {
                String termoBusca = params[0].trim().replace(",", "").replace("-", "").replace(".", "");


                HttpURLConnection connection2 =
                        (HttpURLConnection)new URL("http://cep.republicavirtual.com.br/web_cep.php?cep=" +
                                termoBusca + "&formato=jsonp").openConnection();
                connection2.setRequestMethod("GET");
                connection2.setRequestProperty("Accept", "application/json");

                HttpURLConnection connection1 =
                        (HttpURLConnection)new URL("http://maps.google.com/maps/api/geocode/json?address=" +
                                termoBusca +"&sensor=false").openConnection();
                connection1.setRequestMethod("GET");
                connection1.setRequestProperty("Accept", "application/json");

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
                Toast.makeText(MainActivity.this, "Erro ao buscar o Cep", Toast.LENGTH_LONG).show();

            try {
                String[] jsons = s.split(Pattern.quote("|"));

                JSONObject json1 = new JSONObject(jsons[1]);
                String tipoLogradouro = json1.getString("tipo_logradouro");
                String logradouro = tipoLogradouro + " " +  json1.getString("logradouro");
                String bairro1 = json1.getString("bairro");
                String cidade1 = json1.getString("cidade");
                String uf1 = json1.getString("uf");

                JSONObject json2 = new JSONObject(jsons[0]);
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

                gLat = (double)root.getJSONObject("geometry").getJSONObject("location").get("lat");
                gLng = (double)root.getJSONObject("geometry").getJSONObject("location").get("lng");

                atualizarMapa(gLat,gLng);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private double gLat, gLng;
    }
}
