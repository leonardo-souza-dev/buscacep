package com.leonardoserra.buscacepcommapa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leonardoserra.buscacepcommapa.databinding.ActivityMainBinding;
import com.leonardoserra.buscacepcommapa.model.GitHubModel;
import com.leonardoserra.buscacepcommapa.vm.MainViewModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final Double LAT_PADRAO = 40.0;
    private final Double LNG_PADRAO = 40.0;
    ActivityMainBinding binding = null;
    //private EditText cepEditText;
    private ScrollView resultadoScrollView;
    private String cepPesquisado;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private MainViewModel viewModel;
    private GitHubModel model;
    private Endereco endereco = new Endereco();
    private boolean api1get = false;
    private boolean api2get = false;
    private boolean cepNaoEncontrado = false;

    /*private void setElementosVisuais() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new MainViewModel(getSupportFragmentManager(), true, new MapsActivity(), R.id.map);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setModelo(viewModel);

        Toolbar toolbar = binding.toolbarTest;
        this.setSupportActionBar(toolbar);
        //ActionBar actionBar = binding.toolbar;
        //this.setActionBar(toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = binding.navView;
        navigationView.setNavigationItemSelectedListener(this);
        model = new GitHubModel(viewModel, this);

        binding.buscarCepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                viewModel.setPb(true);
                model.busca(binding.cepEditText.getText().toString());
            }
        });


        model.atualizaMapaInicial(40.0, 40.0);

        sp = getSharedPreferences("cepleo", MODE_PRIVATE);
        editor = sp.edit();

        //resultadoScrollView = (ScrollView) findViewById(R.id.resultadoScrollView);
        resultadoScrollView = binding.resultadoScrollView;


        //obtem historico
        Bundle bundle = getIntent() != null ? getIntent().getExtras() : null;
        String historicoEndereco = bundle != null ? bundle.getString("historico_endereco") : null;

        if (historicoEndereco == null)
            atualizarMapa(LAT_PADRAO, LNG_PADRAO, true);
        else {
            Gson gson = new Gson();
            endereco = gson.fromJson(historicoEndereco, Endereco.class);

            atualizarMapa(endereco.getLat(), endereco.getLng(), false);
            resultadoScrollView.requestFocus();
        }
    }

    private void atualizarMapa(double lat, double lng, boolean setMapaInicial) {
        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapsActivity activity = new MapsActivity();

        if (!setMapaInicial)
            activity.inicializa(lat, lng, 17.0f, cepPesquisado);
        else
            activity.setMapaInicial();

        map.getMapAsync(activity);
    }

    public void vaiAoTopo(View view) {
        resultadoScrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_historico) {
            Intent i = new Intent(this, HistoricoActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

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


}
