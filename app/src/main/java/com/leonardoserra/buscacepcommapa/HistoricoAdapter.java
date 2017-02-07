package com.leonardoserra.buscacepcommapa;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.leonardoserra.buscacepcommapa.bean.Endereco;

public final class HistoricoAdapter extends ArrayAdapter<Endereco> {

    private final int layout;
    private Context context;

    public HistoricoAdapter(final Context pContext, final int pLayout) {
        super(pContext, 0);

        this.context = pContext;
        this.layout = pLayout;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        final View view = getWorkingView(convertView);
        final Endereco entry = getItem(position);

        if (layout == R.layout.hist_row) {
            final ViewHolderSimpleRow viewHolderSimpleRow = getViewHolder(view);
            setElements(viewHolderSimpleRow, entry);
        }

        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vaiParaActivity(entry);
            }
        });

        return view;
    }

    private void vaiParaActivity(Endereco endereco) {

        Intent intent = new Intent(context, PrincipalActivity.class);

        Bundle b = new Bundle();
        b.putString("historico_endereco", new Gson().toJson(endereco));

        intent.putExtras(b);

        context.startActivity(intent);
    }

    private void setElements(ViewHolderSimpleRow viewHolderSimpleRow, Endereco pEntry) {
        viewHolderSimpleRow.histCepRow.setText(pEntry.getCep());

        String resultado;
        if (pEntry.getResultado().isEmpty()){
            resultado = "CEP não encontrado";
            viewHolderSimpleRow.histResultadoRow.setTextColor(Color.RED);
        } else {
            resultado = pEntry.getResultado();
            viewHolderSimpleRow.histResultadoRow.setTextColor(Color.BLACK);
        }
        viewHolderSimpleRow.histResultadoRow.setText(resultado);
    }

    private View getWorkingView(final View convertView) {
        View workingView;

        if (null == convertView) {
            final Context context = getContext();
            final LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            workingView = inflater.inflate(layout, null);
        } else {
            workingView = convertView;
        }

        return workingView;
    }

    private ViewHolderSimpleRow getViewHolder(final View workingView) {

        ViewHolderSimpleRow viewBase = null;

        if (layout == R.layout.hist_row) {
            ViewHolderSimpleRow viewHolderSimpleRow = new ViewHolderSimpleRow();
            viewHolderSimpleRow.histCepRow = (TextView) workingView.findViewById(R.id.hist_cep_row);
            viewHolderSimpleRow.histResultadoRow = (TextView) workingView.findViewById(R.id.hist_resultado_row);

            workingView.setTag(viewHolderSimpleRow);
            viewBase = viewHolderSimpleRow;
        }

        return viewBase;
    }

    private static class ViewHolderSimpleRow {
        public TextView histCepRow;
        public TextView histResultadoRow;
    }
}
