package com.example.mapadointercambista.adapter.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;

public class CarrosselAdapter extends RecyclerView.Adapter<CarrosselAdapter.ViewHolder> {

    private final int[] imagens;
    private final String[] titulos = {
            "Seu próximo intercâmbio começa aqui",
            "Descubra novos destinos",
            "Encontre sua agência ideal"
    };

    private final String[] subtitulos = {
            "Confira programas incríveis para sua jornada internacional",
            "Explore países bem avaliados pela comunidade",
            "Veja experiências reais antes de escolher"
    };

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
        holder.titulo.setText(titulos[position]);
        holder.subtitulo.setText(subtitulos[position]);

        holder.botao.setOnClickListener(v -> {
            // Pode abrir a tela de destinos depois
        });
    }

    @Override
    public int getItemCount() {
        return imagens != null ? imagens.length : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagem;
        TextView titulo;
        TextView subtitulo;
        Button botao;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagem = itemView.findViewById(R.id.imagemBanner);
            titulo = itemView.findViewById(R.id.tituloBanner);
            subtitulo = itemView.findViewById(R.id.subtituloBanner);
            botao = itemView.findViewById(R.id.botaoBanner);
        }
    }
}