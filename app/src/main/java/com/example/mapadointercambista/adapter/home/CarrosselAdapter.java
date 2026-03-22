package com.example.mapadointercambista.adapter.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;

public class CarrosselAdapter extends RecyclerView.Adapter<CarrosselAdapter.ViewHolder> {

    private int[] imagens;

    public CarrosselAdapter(int[] imagens) {
        this.imagens = imagens;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itens_carrossel, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.imagem.setImageResource(imagens[position]);

    }

    @Override
    public int getItemCount() {
        return imagens.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imagem;

        public ViewHolder(View itemView) {
            super(itemView);

            imagem = itemView.findViewById(R.id.imagemBanner);

        }
    }
}