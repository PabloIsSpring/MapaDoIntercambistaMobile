package com.example.mapadointercambista.adapter.destino;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.util.AvatarUtils;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.util.TimeUtils;
import com.example.mapadointercambista.model.destino.AvaliacaoDestino;
import com.example.mapadointercambista.model.destino.Destino;
import com.example.mapadointercambista.model.destino.DestinoStorage;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class AvaliacaoDestinoAdapter extends RecyclerView.Adapter<AvaliacaoDestinoAdapter.ViewHolder> {

    private final Context context;
    private final String destinoId;
    private final List<String> agenciasDisponiveis;
    private final List<AvaliacaoDestino> lista;
    private final SessionManager sessionManager;
    private final DestinoStorage destinoStorage;

    public AvaliacaoDestinoAdapter(Context context, String destinoId, List<String> agenciasDisponiveis, List<AvaliacaoDestino> lista) {
        this.context = context;
        this.destinoId = destinoId;
        this.agenciasDisponiveis = agenciasDisponiveis;
        this.lista = lista;
        this.sessionManager = new SessionManager(context);
        this.destinoStorage = new DestinoStorage(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView fotoPerfil;
        TextView nomeUsuario;
        TextView textoBadgeVoce;
        TextView tempo;
        TextView textoAgencia;
        RatingBar rating;
        TextView mensagem;
        LinearLayout botaoLike;
        LinearLayout botaoDislike;
        ImageView iconeLike;
        ImageView iconeDislike;
        TextView textoLikes;
        TextView textoDislikes;
        ImageView botaoOpcoes;

        public ViewHolder(View itemView) {
            super(itemView);

            fotoPerfil = itemView.findViewById(R.id.fotoPerfilAvaliacao);
            nomeUsuario = itemView.findViewById(R.id.nomeUsuarioAvaliacao);
            textoBadgeVoce = itemView.findViewById(R.id.textoBadgeVoceAvaliacao);
            tempo = itemView.findViewById(R.id.tempoAvaliacao);
            textoAgencia = itemView.findViewById(R.id.textoAgenciaAvaliacao);
            rating = itemView.findViewById(R.id.ratingAvaliacao);
            mensagem = itemView.findViewById(R.id.mensagemAvaliacao);
            botaoLike = itemView.findViewById(R.id.botaoLikeAvaliacao);
            botaoDislike = itemView.findViewById(R.id.botaoDislikeAvaliacao);
            iconeLike = itemView.findViewById(R.id.iconeLikeAvaliacao);
            iconeDislike = itemView.findViewById(R.id.iconeDislikeAvaliacao);
            textoLikes = itemView.findViewById(R.id.textoLikesAvaliacao);
            textoDislikes = itemView.findViewById(R.id.textoDislikesAvaliacao);
            botaoOpcoes = itemView.findViewById(R.id.botaoOpcoesAvaliacao);
        }
    }

    @NonNull
    @Override
    public AvaliacaoDestinoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_avaliacao_destino, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvaliacaoDestinoAdapter.ViewHolder holder, int position) {
        AvaliacaoDestino avaliacao = lista.get(position);

        aplicarAvatar(holder.fotoPerfil, avaliacao.getAutorFotoUri(), avaliacao.getAutorNome());
        holder.nomeUsuario.setText(avaliacao.getAutorNome());
        holder.tempo.setText("· " + avaliacao.getTempoFormatado());
        holder.rating.setRating(avaliacao.getNotaEstrelas());
        holder.mensagem.setText(avaliacao.getMensagem());
        holder.textoLikes.setText(String.valueOf(avaliacao.getLikes()));
        holder.textoDislikes.setText(String.valueOf(avaliacao.getDislikes()));

        String agencia = avaliacao.getAgenciaEscolhida();
        if (agencia == null || agencia.trim().isEmpty() || agencia.equalsIgnoreCase("Nenhuma agência")) {
            holder.textoAgencia.setText("Sem agência");
        } else {
            holder.textoAgencia.setText(agencia);
        }

        boolean ehAutor = sessionManager.estaLogado()
                && sessionManager.getEmailUsuario().equals(avaliacao.getAutorEmail());

        holder.textoBadgeVoce.setVisibility(ehAutor ? View.VISIBLE : View.GONE);
        holder.botaoOpcoes.setVisibility(ehAutor ? View.VISIBLE : View.GONE);

        holder.botaoOpcoes.setOnClickListener(v -> abrirMenuAvaliacao(v, avaliacao));

        atualizarEstadoVisualReacoes(holder, avaliacao);

        holder.botaoLike.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(context, "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
                return;
            }

            animarClique(holder.botaoLike);

            boolean sucesso = destinoStorage.toggleLikeAvaliacao(
                    destinoId,
                    avaliacao.getId(),
                    sessionManager.getEmailUsuario()
            );

            if (sucesso) {
                atualizarLista();
            }
        });

        holder.botaoDislike.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(context, "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
                return;
            }

            animarClique(holder.botaoDislike);

            boolean sucesso = destinoStorage.toggleDislikeAvaliacao(
                    destinoId,
                    avaliacao.getId(),
                    sessionManager.getEmailUsuario()
            );

            if (sucesso) {
                atualizarLista();
            }
        });
    }

    private void abrirMenuAvaliacao(View anchor, AvaliacaoDestino avaliacao) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.getMenu().add("Editar");
        popupMenu.getMenu().add("Excluir");

        popupMenu.setOnMenuItemClickListener(item -> {
            String titulo = item.getTitle().toString();

            if (titulo.equals("Editar")) {
                abrirDialogEditarAvaliacao(avaliacao);
                return true;
            }

            if (titulo.equals("Excluir")) {
                confirmarExclusaoAvaliacao(avaliacao);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void abrirDialogEditarAvaliacao(AvaliacaoDestino avaliacao) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_avaliacao_destino, null);

        EditText inputMensagem = view.findViewById(R.id.inputMensagemAvaliacao);
        RatingBar ratingBar = view.findViewById(R.id.ratingNovaAvaliacao);
        Spinner spinnerAgencia = view.findViewById(R.id.spinnerAgenciaAvaliacao);

        inputMensagem.setText(avaliacao.getMensagem());
        ratingBar.setRating(avaliacao.getNotaEstrelas());

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                agenciasDisponiveis
        );
        spinnerAgencia.setAdapter(spinnerAdapter);

        int posicaoSelecionada = agenciasDisponiveis.indexOf(avaliacao.getAgenciaEscolhida());
        if (posicaoSelecionada >= 0) {
            spinnerAgencia.setSelection(posicaoSelecionada);
        }

        new AlertDialog.Builder(context)
                .setTitle("Editar avaliação")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String mensagem = inputMensagem.getText().toString().trim();
                    float nota = ratingBar.getRating();
                    String agencia = spinnerAgencia.getSelectedItem().toString();

                    if (mensagem.isEmpty()) {
                        Toast.makeText(context, "Digite sua avaliação", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (nota <= 0) {
                        Toast.makeText(context, "Selecione uma nota em estrelas", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean sucesso = destinoStorage.editarAvaliacao(
                            destinoId,
                            avaliacao.getId(),
                            mensagem,
                            nota,
                            agencia
                    );

                    if (sucesso) {
                        Toast.makeText(context, "Avaliação editada com sucesso", Toast.LENGTH_SHORT).show();
                        atualizarLista();
                    } else {
                        Toast.makeText(context, "Erro ao editar avaliação", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void confirmarExclusaoAvaliacao(AvaliacaoDestino avaliacao) {
        new AlertDialog.Builder(context)
                .setTitle("Excluir avaliação")
                .setMessage("Tem certeza que deseja excluir esta avaliação?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    boolean sucesso = destinoStorage.excluirAvaliacao(destinoId, avaliacao.getId());

                    if (sucesso) {
                        Toast.makeText(context, "Avaliação excluída com sucesso", Toast.LENGTH_SHORT).show();
                        atualizarLista();
                    } else {
                        Toast.makeText(context, "Erro ao excluir avaliação", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void atualizarLista() {
        Destino destinoAtualizado = destinoStorage.buscarDestinoPorId(destinoId);
        if (destinoAtualizado != null && destinoAtualizado.getListaAvaliacoes() != null) {
            lista.clear();
            lista.addAll(destinoAtualizado.getListaAvaliacoes());
            notifyDataSetChanged();
        }
    }

    private void atualizarEstadoVisualReacoes(ViewHolder holder, AvaliacaoDestino avaliacao) {
        if (!sessionManager.estaLogado()) {
            holder.iconeLike.setAlpha(1f);
            holder.iconeDislike.setAlpha(1f);
            holder.textoLikes.setAlpha(1f);
            holder.textoDislikes.setAlpha(1f);
            holder.iconeLike.setColorFilter(ContextCompat.getColor(context, R.color.green_like));
            holder.iconeDislike.setColorFilter(ContextCompat.getColor(context, R.color.red_dislike));
            return;
        }

        boolean curtiu = avaliacao.usuarioCurtiu(sessionManager.getEmailUsuario());
        boolean descurtiu = avaliacao.usuarioDescurtiu(sessionManager.getEmailUsuario());

        holder.iconeLike.setAlpha(curtiu ? 1f : 0.55f);
        holder.textoLikes.setAlpha(curtiu ? 1f : 0.75f);

        holder.iconeDislike.setAlpha(descurtiu ? 1f : 0.55f);
        holder.textoDislikes.setAlpha(descurtiu ? 1f : 0.75f);

        holder.iconeLike.setColorFilter(ContextCompat.getColor(
                context,
                curtiu ? R.color.green_like_active : R.color.green_like
        ));

        holder.iconeDislike.setColorFilter(ContextCompat.getColor(
                context,
                descurtiu ? R.color.red_dislike_active : R.color.red_dislike
        ));
    }

    private void aplicarAvatar(ShapeableImageView imageView, String fotoUri, String nomeAutor) {
        if (fotoUri != null && !fotoUri.isEmpty()) {
            imageView.setImageURI(Uri.parse(fotoUri));
            imageView.setImageTintList(null);
        } else {
            Bitmap avatar = AvatarUtils.criarAvatarComInicial(context, nomeAutor, 120);
            imageView.setImageBitmap(avatar);
            imageView.setImageTintList(null);
        }
    }

    private void animarClique(View view) {
        ObjectAnimator diminuirX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.92f, 1f);
        ObjectAnimator diminuirY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.92f, 1f);
        diminuirX.setDuration(180);
        diminuirY.setDuration(180);
        diminuirX.start();
        diminuirY.start();
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}