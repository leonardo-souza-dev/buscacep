package com.leonardoserra.buscacepcommapa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leonardoserra.buscacepcommapa.bean.Endereco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoricoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        ListView historicoListView = (ListView) findViewById(R.id.historicoListView);
        HistoricoAdapter historicoAdapter = new HistoricoAdapter(this, R.layout.hist_row);
        historicoListView.setAdapter(historicoAdapter);

        List<Endereco> historico = obterHistorico();

        for (final Endereco entry : historico) {
            historicoAdapter.add(entry);
        }
    }

    private List<Endereco> obterHistorico(){

        List<Endereco> historico = new ArrayList<>();

        SharedPreferences sp = getSharedPreferences("cepleo", MODE_PRIVATE);
        String historicoSp = sp.getString("historico", null);

        if (historicoSp == null) {
            Log.i("BUSCACEPLOG", "historico nulo");
        } else {

            Gson gson = new Gson();
            historico = gson.fromJson(historicoSp, new TypeToken<ArrayList<Endereco>>() {
            }.getType());

            Collections.reverse(historico);
        }

        if (historico.size() > 5)
            historico = historico.subList(0, 5);

        return historico;

    }
}
