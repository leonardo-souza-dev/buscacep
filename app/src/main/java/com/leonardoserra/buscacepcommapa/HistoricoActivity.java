package com.leonardoserra.buscacepcommapa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;

public class HistoricoActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> listAdapter;
    private HistoricoAdapter movieAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        listView = (ListView) findViewById(R.id.historicoListView);//
        movieAdapter = new HistoricoAdapter(this, R.layout.hist_row);//
        listView.setAdapter(movieAdapter);//

        ArrayList<Endereco> historicoArray = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences("cepleo", MODE_PRIVATE);
        String historicoSp = sp.getString("historico", null);
        if (historicoSp != null) {
            Gson gson = new Gson();
            historicoArray = gson.fromJson(historicoSp, new TypeToken<ArrayList<Endereco>>() {
            }.getType());
        }

        //inverte ordem
        Collections.reverse(historicoArray);

        for (final Endereco entry : historicoArray) {
            movieAdapter.add(entry);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String clicado = parent.getItemAtPosition(position).toString();
                Intent intent = new Intent(HistoricoActivity.this, MainActivity.class);
                intent.putExtra("cep_historico", clicado);
                startActivity(intent);

            }
        });
    }
}
