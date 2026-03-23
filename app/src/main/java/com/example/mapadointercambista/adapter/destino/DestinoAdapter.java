package com.example.mapadointercambista.adapter.destino;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.destinos.DetalheDestinoActivity;
import com.example.mapadointercambista.model.destino.Destino;

import java.util.List;

public class DestinoAdapter extends RecyclerView.Adapter<DestinoAdapter.ViewHolder> {

    private List<Destino> lista;

    public DestinoAdapter(List<Destino> lista) {
        this.lista = lista;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imagem;
        TextView nome;
        RatingBar rating;
        TextView reviews;

        public ViewHolder(View itemView) {
            super(itemView);

            imagem = itemView.findViewById(R.id.imagemDestino);
            nome = itemView.findViewById(R.id.textoDestino);
            rating = itemView.findViewById(R.id.ratingDestino);
            reviews = itemView.findViewById(R.id.textoReviews);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_destino, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Destino destino = lista.get(position);

        holder.nome.setText(destino.getNome());
        holder.imagem.setImageResource(destino.getImagem());
        holder.rating.setRating(destino.getNota());
        holder.reviews.setText(destino.getNota() + " (" + destino.getAvaliacoes() + " avaliações)");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetalheDestinoActivity.class);
            intent.putExtra("destino", destino);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}