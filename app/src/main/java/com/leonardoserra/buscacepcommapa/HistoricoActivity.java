package com.leonardoserra.buscacepcommapa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;

public class HistoricoActivity extends AppCompatActivity {

    private ListView historicoListView;
    private HistoricoAdapter historicoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        historicoListView = (ListView) findViewById(R.id.historicoListView);
        historicoAdapter = new HistoricoAdapter(this, R.layout.hist_row);
        historicoListView.setAdapter(historicoAdapter);

        ArrayList<Endereco> historico = obterHistorico();

        for (final Endereco entry : historico) {
            historicoAdapter.add(entry);
        }
    }

    private ArrayList<Endereco> obterHistorico(){

        ArrayList<Endereco> historico = new ArrayList<>();

        SharedPreferences sp = getSharedPreferences("cepleo", MODE_PRIVATE);
        String historicoSp = sp.getString("historico", null);

        if (historicoSp != null) {
            Gson gson = new Gson();
            historico = gson.fromJson(historicoSp, new TypeToken<ArrayList<Endereco>>() {
            }.getType());
        }

        Collections.reverse(historico);

        return historico;

    }
}
