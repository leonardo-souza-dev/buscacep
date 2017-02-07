package com.leonardoserra.buscacepcommapa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.gson.Gson;
import com.leonardoserra.buscacepcommapa.bean.Endereco;
import com.leonardoserra.buscacepcommapa.databinding.ActivityPrincipalBinding;
import com.leonardoserra.buscacepcommapa.model.MyModel;
import com.leonardoserra.buscacepcommapa.vm.PrincipalViewModelo;

public class PrincipalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ActivityPrincipalBinding binding = null;
    private PrincipalViewModelo viewModel;
    private MyModel model;
    private ScrollView resultadoScrollView;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Endereco endereco = new Endereco();
    private String cepPesquisado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new PrincipalViewModelo(getSupportFragmentManager(), true, new MapsActivity(), R.id.map);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_principal);
        binding.setMod(viewModel);
        model = new MyModel(viewModel, this, getSharedPreferences("cepleo", MODE_PRIVATE));

        binding.buscarCepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                viewModel.setPb2(true);
                model.busca(binding.cepEditText.getText().toString());
            }
        });

        Toolbar toolbar = binding.toolbar;
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //model.atualizaMapaInicial(40.0, 40.0);
        viewModel.setMapaInicial();

        sp = getSharedPreferences("cepleo", MODE_PRIVATE);
        editor = sp.edit();

        resultadoScrollView = binding.resultadoScrollView;


        //obtem historico
        Bundle bundle = getIntent() != null ? getIntent().getExtras() : null;
        String historicoEndereco = bundle != null ? bundle.getString("historico_endereco") : null;

        if (historicoEndereco == null)
            //atualizarMapa(LAT_PADRAO, LNG_PADRAO, true);
            viewModel.setMapaInicial();
        else {
            Gson gson = new Gson();
            endereco = gson.fromJson(historicoEndereco, Endereco.class);

            populaCampos(endereco);
            resultadoScrollView.requestFocus();
        }
    }

    private void populaCampos(Endereco endereco) {

        viewModel.setCep2(endereco.getCep());
        viewModel.setLogradouro2(endereco.getLogradouro());
        viewModel.setBairro2(endereco.getBairro());
        viewModel.setCidade2(endereco.getCidade());
        viewModel.setUf2(endereco.getUf());

        viewModel.setMapa(endereco.getLat(), endereco.getLng());
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



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_historico) {
            Intent i = new Intent(this, HistoricoActivity.class);
            startActivity(i);
        }

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        DrawerLayout drawer = binding.drawerLayout;
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void vaiAoTopo(View view) {
        resultadoScrollView.fullScroll(ScrollView.FOCUS_UP);
    }
}
