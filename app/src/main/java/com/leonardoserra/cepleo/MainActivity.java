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

                //URL url = new URL("http://cep.republicavirtual.com.br/web_cep.php?cep=" + termoBusca + "&formato=jsonp");
                URL url = new URL("http://maps.google.com/maps/api/geocode/json?address=" + termoBusca +"&sensor=false");
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                if (connection.getResponseCode() == 200) {
                    BufferedReader stream =
                            new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String linha = "";
                    StringBuilder resposta = new StringBuilder();

                    while ((linha = stream.readLine()) != null) {
                        resposta.append(linha);
                    }

                    connection.disconnect();

                    return resposta.toString();
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
                JSONObject json = new JSONObject(s);
//                String tipoLogradouro = json.getString("tipo_logradouro");
//                String logradouro = tipoLogradouro + " " +  json.getString("logradouro");
//                String cidade = json.getString("cidade");

//                String uf = json.getString("uf");
                JSONArray results = json.getJSONArray("results");
                JSONObject asd = results.getJSONObject(0);
                JSONArray qwe = (JSONArray) asd.get("address_components");
                String bairro = qwe.getJSONObject(1).get("long_name").toString();
                String cidade = qwe.getJSONObject(2).get("long_name").toString();
                String uf = qwe.getJSONObject(3).get("short_name").toString();
//                edtLogradouro.setText(logradouro);
                edtBairro.setText(bairro);
                edtCidade.setText(cidade);
                edtUf.setText(uf);

                JSONObject qwe2 = (JSONObject) asd.get("geometry");
                gLat = (double)asd.getJSONObject("geometry").getJSONObject("location").get("lat");
                gLng = (double)asd.getJSONObject("geometry").getJSONObject("location").get("lng");

                atualizarMapa(gLat,gLng);
                // String bairro2 = qwe2.getJSONObject(1).get("lat").toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private double gLat, gLng;
    }
}
