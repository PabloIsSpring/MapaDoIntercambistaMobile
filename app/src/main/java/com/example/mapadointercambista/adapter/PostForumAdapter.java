package com.example.mapadointercambista.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.model.PostForum;

import java.util.List;

public class PostForumAdapter extends RecyclerView.Adapter<PostForumAdapter.ViewHolder>{

    private List<PostForum> lista;

    public PostForumAdapter(List<PostForum> lista){
        this.lista = lista;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView fotoPerfil;
        TextView usuario;
        TextView mensagem;
        RatingBar rating;
        TextView reviews;

        public ViewHolder(View itemView){
            super(itemView);

            fotoPerfil = itemView.findViewById(R.id.fotoPerfil);
            usuario = itemView.findViewById(R.id.nomeUsuario);
            mensagem = itemView.findViewById(R.id.textoMensagem);
            rating = itemView.findViewById(R.id.ratingPost);
            reviews = itemView.findViewById(R.id.reviewsPost);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_forum, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){

        PostForum post = lista.get(position);

        holder.usuario.setText(post.getUsuario());
        holder.mensagem.setText(post.getMensagem());
        holder.fotoPerfil.setImageResource(post.getFotoPerfil());
        holder.rating.setRating(post.getAvaliacao());

        holder.reviews.setText(post.getAvaliacao() + " (" + post.getReviews() + ")");
    }

    @Override
    public int getItemCount(){
        return lista.size();
    }
}