package com.example.mapadointercambista.adapter.destino;

import android.content.Intent;
import android.util.SparseArray;
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
import com.example.mapadointercambista.util.ImageUtils;

import java.util.List;
import java.util.Locale;

public class DestinoAdapter extends RecyclerView.Adapter<DestinoAdapter.ViewHolder> {

    private final List<Destino> lista;
    private final SparseArray<Integer> imageCache = new SparseArray<>();

    public DestinoAdapter(List<Destino> lista) {
        this.lista = lista;
        setHasStableIds(true);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagem;
        TextView nome;
        RatingBar rating;
        TextView reviews;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagem = itemView.findViewById(R.id.imagemDestino);
            nome = itemView.findViewById(R.id.textoDestino);
            rating = itemView.findViewById(R.id.ratingDestino);
            reviews = itemView.findViewById(R.id.textoReviews);
        }
    }

    @Override
    public long getItemId(int position) {
        String id = lista.get(position).getId();
        return id != null ? id.hashCode() : position;
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
        holder.rating.setRating(destino.getNota());
        holder.reviews.setText(String.format(
                Locale.getDefault(),
                "%.1f (%d avaliações)",
                destino.getNota(),
                destino.getAvaliacoes()
        ));

        String imagemNome = destino.getImagemNome();
        int cacheKey = imagemNome != null ? imagemNome.hashCode() : 0;

        Integer drawableId = imageCache.get(cacheKey);
        if (drawableId == null) {
            int resolvedId = ImageUtils.getDrawableId(
                    holder.itemView.getContext(),
                    imagemNome
            );

            if (resolvedId == 0) {
                resolvedId = R.drawable.ic_world;
            }

            imageCache.put(cacheKey, resolvedId);
            drawableId = resolvedId;
        }

        holder.imagem.setImageResource(drawableId);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetalheDestinoActivity.class);
            intent.putExtra("destino", destino);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }
}