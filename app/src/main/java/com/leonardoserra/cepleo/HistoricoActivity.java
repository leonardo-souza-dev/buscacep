package com.leonardoserra.cepleo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

        //inverte ordem
        List<String> list = Arrays.asList(historicoArray);
        Collections.reverse(list);
        historicoArray = (String[]) list.toArray();

        listAdapter = new ArrayAdapter<String>(this, R.layout.hist_row, historicoArray);
        lstView.setAdapter(listAdapter);

        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
