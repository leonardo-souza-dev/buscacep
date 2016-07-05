package com.leonardoserra.cepleo;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HistoricoActivity extends AppCompatActivity {

    private ListView lstView;
    private ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        lstView = (ListView) findViewById(R.id.listView);

        String[] historicoArray = new String[0];
        SharedPreferences sp = getSharedPreferences("cepleo", MODE_PRIVATE);
        String historicoStr = sp.getString("historico", null);
        if (historicoStr != null) {
            historicoArray = historicoStr.split(";");
        }

        listAdapter = new ArrayAdapter<String>(this, R.layout.hist_row, historicoArray);
        lstView.setAdapter(listAdapter);
    }
}
