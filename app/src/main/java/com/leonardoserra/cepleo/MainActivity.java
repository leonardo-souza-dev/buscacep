package com.leonardoserra.cepleo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText edtCep;
    private EditText edtLogradouro;
    private EditText edtComplemento;
    private EditText edtBairrp;
    private EditText edtCidade;
    private EditText edtUf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtCep = (EditText)findViewById(R.id.edtCep);

        edtLogradouro = (EditText)findViewById(R.id.edtLogradouro);
        edtBairrp = (EditText)findViewById(R.id.edtBairro);
        edtCidade = (EditText)findViewById(R.id.edtCidade);
        edtUf = (EditText)findViewById(R.id.edtUf);
    }

    public void buscar(View view) {
        BuscarCepTask task = new BuscarCepTask();
        task.execute(edtCep.getText().toString());
    }

    private class BuscarCepTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            if (params.length < 1 || params[0] == "" || params[0] == null)
                Toast.makeText(MainActivity.this, "Insira um CEP", Toast.LENGTH_LONG).show();

            try {
                String termoBusca = params[0].trim().replace(",", "").replace("-", "").replace(".", "");

                URL url = new URL("http://cep.republicavirtual.com.br/web_cep.php?cep=" + termoBusca + "&formato=jsonp");
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
            if (s == null)
                Toast.makeText(MainActivity.this, "Erro ao buscar o Cep", Toast.LENGTH_LONG).show();

            try {
                JSONObject json = new JSONObject(s);
                String tipoLogradouro = json.getString("tipo_logradouro");
                String logradouro = tipoLogradouro + " " +  json.getString("logradouro");
                String cidade = json.getString("cidade");
                String bairro = json.getString("bairro");
                String uf = json.getString("uf");

                edtLogradouro.setText(logradouro);
                edtBairrp.setText(bairro);
                edtCidade.setText(cidade);
                edtUf.setText(uf);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
