package com.leonardoserra.buscacepcommapa;

import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
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
import android.widget.ScrollView;

import com.google.gson.Gson;
import com.leonardoserra.buscacepcommapa.bean.Endereco;
import com.leonardoserra.buscacepcommapa.databinding.ActivityMainBinding;
import com.leonardoserra.buscacepcommapa.model.Model;
import com.leonardoserra.buscacepcommapa.vm.MainViewModel;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ActivityMainBinding binding = null;
    private MainViewModel viewModel;
    private Model model;
    private ScrollView resultadoScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new MainViewModel(getSupportFragmentManager(), new MapsActivity(), R.id.map);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMod(viewModel);
        model = new Model(viewModel, this, getSharedPreferences("cepleo", MODE_PRIVATE));

        binding.buscarCepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                viewModel.setProgressBar(true);
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

        viewModel.setMapaInicial();

        historico();
    }

    private void historico() {

        resultadoScrollView = binding.resultadoScrollView;

        Bundle bundle = getIntent() != null ? getIntent().getExtras() : null;
        String historicoEndereco = bundle != null ? bundle.getString("historico_endereco") : null;

        if (historicoEndereco == null) {
            viewModel.setMapaInicial();
        }
        else {
            Gson gson = new Gson();
            Endereco endereco = gson.fromJson(historicoEndereco, Endereco.class);

            populaCampos(endereco);
            resultadoScrollView.requestFocus();
        }
    }

    private void populaCampos(Endereco endereco) {

        viewModel.setCep(endereco.getCep());
        viewModel.setLogradouro(endereco.getLogradouro());
        viewModel.setBairro(endereco.getBairro());
        viewModel.setCidade(endereco.getCidade());
        viewModel.setUf(endereco.getUf());

        viewModel.setMapa(endereco.getLat(), endereco.getLng());
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_historico) {
            Intent i = new Intent(this, HistoricoActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = binding.drawerLayout;
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void vaiAoTopo(View view) {
        resultadoScrollView.fullScroll(ScrollView.FOCUS_UP);
    }
}
