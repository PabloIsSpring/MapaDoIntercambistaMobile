package com.example.mapadointercambista.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.model.RespostaForum;

import java.util.List;

public class RespostaForumAdapter extends RecyclerView.Adapter<RespostaForumAdapter.ViewHolder> {

    private final List<RespostaForum> lista;
    private final boolean usuarioLogado;

    public RespostaForumAdapter(List<RespostaForum> lista, boolean usuarioLogado) {
        this.lista = lista;
        this.usuarioLogado = usuarioLogado;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout containerResposta;
        LinearLayout blocoResposta;
        View linhaThread;

        ImageView fotoPerfil;
        TextView nomeUsuario;
        TextView tempo;
        TextView mensagem;

        LinearLayout botaoLike;
        LinearLayout botaoDislike;
        LinearLayout botaoResponder;

        ImageView iconeLike;
        ImageView iconeDislike;
        TextView textoLikes;
        TextView textoDislikes;



        public ViewHolder(View itemView) {
            super(itemView);

            containerResposta = itemView.findViewById(R.id.containerResposta);
            blocoResposta = itemView.findViewById(R.id.blocoResposta);
            linhaThread = itemView.findViewById(R.id.linhaThread);

            fotoPerfil = itemView.findViewById(R.id.fotoPerfilResposta);
            nomeUsuario = itemView.findViewById(R.id.nomeUsuarioResposta);
            tempo = itemView.findViewById(R.id.textoTempoResposta);
            mensagem = itemView.findViewById(R.id.textoMensagemResposta);

            botaoLike = itemView.findViewById(R.id.botaoLikeResposta);
            botaoDislike = itemView.findViewById(R.id.botaoDislikeResposta);
            botaoResponder = itemView.findViewById(R.id.botaoResponderResposta);

            iconeLike = itemView.findViewById(R.id.iconeLikeResposta);
            iconeDislike = itemView.findViewById(R.id.iconeDislikeResposta);
            textoLikes = itemView.findViewById(R.id.textoLikesResposta);
            textoDislikes = itemView.findViewById(R.id.textoDislikesResposta);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resposta_forum, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RespostaForum resposta = lista.get(position);

        if (!resposta.isVisivel()) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
            ));
        }

        holder.fotoPerfil.setImageResource(resposta.getFotoPerfil());
        holder.nomeUsuario.setText(resposta.getUsuario());
        holder.tempo.setText(resposta.getTempoPostagem());
        holder.mensagem.setText(resposta.getMensagem());
        holder.textoLikes.setText(String.valueOf(resposta.getLikes()));
        holder.textoDislikes.setText(String.valueOf(resposta.getDislikes()));

        int margemEsquerda = resposta.getNivel() * 36;
        MarginLayoutParams params = (MarginLayoutParams) holder.containerResposta.getLayoutParams();
        params.setMarginStart(margemEsquerda);
        holder.containerResposta.setLayoutParams(params);

        if (resposta.getNivel() == 0) {
            holder.linhaThread.setVisibility(View.INVISIBLE);
        } else {
            holder.linhaThread.setVisibility(View.VISIBLE);
        }

        holder.botaoLike.setOnClickListener(v -> {
            if (!usuarioLogado) {
                Toast.makeText(v.getContext(), "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "Like realizado", Toast.LENGTH_SHORT).show();
            }
        });

        holder.botaoDislike.setOnClickListener(v -> {
            if (!usuarioLogado) {
                Toast.makeText(v.getContext(), "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "Dislike realizado", Toast.LENGTH_SHORT).show();
            }
        });

        holder.botaoResponder.setOnClickListener(v -> {
            if (!usuarioLogado) {
                Toast.makeText(v.getContext(), "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "Responder será implementado depois", Toast.LENGTH_SHORT).show();
            }
        });

        if (resposta.isTemRespostas()) {
            holder.blocoResposta.setOnClickListener(v ->
                    alternarRespostasFilhas(position, resposta.getNivel())
            );
        } else {
            holder.blocoResposta.setOnClickListener(null);
        }
    }

    private void alternarRespostasFilhas(int posicaoPai, int nivelPai) {
        boolean mostrar = false;

        for (int i = posicaoPai + 1; i < lista.size(); i++) {
            RespostaForum atual = lista.get(i);

            if (atual.getNivel() <= nivelPai) {
                break;
            }

            if (atual.getNivel() == nivelPai + 1) {
                mostrar = !atual.isVisivel();
                break;
            }
        }

        for (int i = posicaoPai + 1; i < lista.size(); i++) {
            RespostaForum atual = lista.get(i);

            if (atual.getNivel() <= nivelPai) {
                break;
            }

            if (atual.getNivel() == nivelPai + 1) {
                atual.setVisivel(mostrar);
            } else if (!mostrar) {
                atual.setVisivel(false);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}