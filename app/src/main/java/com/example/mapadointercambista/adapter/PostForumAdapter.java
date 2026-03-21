package com.example.mapadointercambista.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.RespostasForumActivity;
import com.example.mapadointercambista.model.PostForum;

import java.util.List;

public class PostForumAdapter extends RecyclerView.Adapter<PostForumAdapter.ViewHolder> {

    private List<PostForum> lista;

    public PostForumAdapter(List<PostForum> lista) {
        this.lista = lista;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView fotoPerfil;
        TextView usuario;
        TextView mensagem;
        TextView textoTempoPostagem;

        ImageView iconeLike;
        ImageView iconeDislike;
        TextView textoLikes;
        TextView textoDislikes;

        LinearLayout botaoRespostas;
        TextView textoRespostas;

        public ViewHolder(View itemView) {
            super(itemView);

            fotoPerfil = itemView.findViewById(R.id.fotoPerfil);
            usuario = itemView.findViewById(R.id.nomeUsuario);
            mensagem = itemView.findViewById(R.id.textoMensagem);
            textoTempoPostagem = itemView.findViewById(R.id.textoTempoPostagem);

            iconeLike = itemView.findViewById(R.id.iconeLike);
            iconeDislike = itemView.findViewById(R.id.iconeDislike);
            textoLikes = itemView.findViewById(R.id.textoLikes);
            textoDislikes = itemView.findViewById(R.id.textoDislikes);

            botaoRespostas = itemView.findViewById(R.id.botaoRespostas);
            textoRespostas = itemView.findViewById(R.id.textoRespostas);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_forum, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostForum post = lista.get(position);

        holder.usuario.setText(post.getUsuario());
        holder.mensagem.setText(post.getMensagem());
        holder.fotoPerfil.setImageResource(post.getFotoPerfil());
        holder.textoTempoPostagem.setText(post.getTempoPostagem());

        holder.textoLikes.setText(String.valueOf(post.getLikes()));
        holder.textoDislikes.setText(String.valueOf(post.getDislikes()));
        holder.textoRespostas.setText(post.getQuantidadeRespostas() + " respostas");

        holder.iconeLike.setOnClickListener(v -> {
            Toast.makeText(v.getContext(),
                    "Você não entrou em sua conta",
                    Toast.LENGTH_SHORT).show();
        });

        holder.iconeDislike.setOnClickListener(v -> {
            Toast.makeText(v.getContext(),
                    "Você não entrou em sua conta",
                    Toast.LENGTH_SHORT).show();
        });

        holder.botaoRespostas.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), RespostasForumActivity.class);
            intent.putExtra("postSelecionado", post);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}