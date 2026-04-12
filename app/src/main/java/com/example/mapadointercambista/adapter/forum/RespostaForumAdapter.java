package com.example.mapadointercambista.adapter.forum;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.InputFilter;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mapadointercambista.R;
import com.example.mapadointercambista.model.forum.ForumStorage;
import com.example.mapadointercambista.model.forum.PostForum;
import com.example.mapadointercambista.model.forum.RespostaForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.util.AnimationUtils;
import com.example.mapadointercambista.util.AvatarUtils;
import com.example.mapadointercambista.util.ForumLimits;
import com.example.mapadointercambista.util.InputSecurityUtils;
import com.example.mapadointercambista.util.TimeUtils;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RespostaForumAdapter extends RecyclerView.Adapter<RespostaForumAdapter.ViewHolder> {

    private static final int MAX_NIVEL_VISUAL = 2;
    private static final int INDENT_DP = 10;
    private final Context context;
    private final String postId;
    private final List<RespostaForum> lista;
    private final SessionManager sessionManager;
    private final ForumStorage forumStorage;
    private final LruCache<String, Bitmap> avatarCache = new LruCache<>(60);
    private int dpToPxInt(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    public RespostaForumAdapter(Context context, String postId, List<RespostaForum> lista, boolean usuarioLogado) {
        this.context = context;
        this.postId = postId;
        this.lista = lista;
        this.sessionManager = new SessionManager(context);
        this.forumStorage = new ForumStorage(context);
        setHasStableIds(true);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout containerResposta;
        LinearLayout blocoResposta;
        View linhaThread;

        ShapeableImageView fotoPerfil;
        TextView nomeUsuario;
        TextView textoBadgeVoceResposta;
        TextView tempo;
        TextView mensagem;

        LinearLayout botaoLike;
        LinearLayout botaoDislike;
        LinearLayout botaoResponder;

        TextView textoLikes;
        TextView textoDislikes;
        TextView textoToggleRespostas;
        ImageView botaoOpcoesResposta;
        ImageView iconeLikeResposta;
        ImageView iconeDislikeResposta;

        public ViewHolder(View itemView) {
            super(itemView);

            containerResposta = itemView.findViewById(R.id.containerResposta);
            blocoResposta = itemView.findViewById(R.id.blocoResposta);
            linhaThread = itemView.findViewById(R.id.linhaThread);

            fotoPerfil = itemView.findViewById(R.id.fotoPerfilResposta);
            nomeUsuario = itemView.findViewById(R.id.nomeUsuarioResposta);
            textoBadgeVoceResposta = itemView.findViewById(R.id.textoBadgeVoceResposta);
            tempo = itemView.findViewById(R.id.textoTempoResposta);
            mensagem = itemView.findViewById(R.id.textoMensagemResposta);

            botaoLike = itemView.findViewById(R.id.botaoLikeResposta);
            botaoDislike = itemView.findViewById(R.id.botaoDislikeResposta);
            botaoResponder = itemView.findViewById(R.id.botaoResponderResposta);

            textoLikes = itemView.findViewById(R.id.textoLikesResposta);
            textoDislikes = itemView.findViewById(R.id.textoDislikesResposta);
            textoToggleRespostas = itemView.findViewById(R.id.textoToggleRespostas);
            botaoOpcoesResposta = itemView.findViewById(R.id.botaoOpcoesResposta);
            iconeLikeResposta = itemView.findViewById(R.id.iconeLikeResposta);
            iconeDislikeResposta = itemView.findViewById(R.id.iconeDislikeResposta);
        }
    }

    @Override
    public long getItemId(int position) {
        String id = lista.get(position).getId();
        return id != null ? id.hashCode() : position;
    }

    @NonNull
    @Override
    public RespostaForumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resposta_forum, parent, false);

        AnimationUtils.applyPressAnimation(view);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RespostaForumAdapter.ViewHolder holder, int position) {
        RespostaForum resposta = lista.get(position);

        if (!resposta.isVisivel()) {
            holder.itemView.setVisibility(View.GONE);
            RecyclerView.LayoutParams hiddenParams = new RecyclerView.LayoutParams(0, 0);
            holder.itemView.setLayoutParams(hiddenParams);
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            RecyclerView.LayoutParams visibleParams = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
            );
            holder.itemView.setLayoutParams(visibleParams);
        }

        aplicarAvatar(holder.fotoPerfil, resposta.getAutorFotoUri(), resposta.getAutorNome());

        holder.nomeUsuario.setText(textoSeguro(resposta.getAutorNome(), "Usuário"));
        holder.tempo.setText(resposta.getTempoPostagem());
        holder.mensagem.setText(textoSeguro(resposta.getMensagem(), ""));
        holder.mensagem.setAlpha(resposta.getNivel() >= 3 ? 0.96f : 1f);

        holder.textoLikes.setText(String.valueOf(resposta.getLikes()));
        holder.textoDislikes.setText(String.valueOf(resposta.getDislikes()));

        boolean usuarioLogadoAgora = usuarioEstaLogado();
        boolean ehAutor = usuarioLogadoAgora
                && sessionManager.getEmailUsuario().equals(resposta.getAutorEmail());

        holder.textoBadgeVoceResposta.setVisibility(ehAutor ? View.VISIBLE : View.GONE);
        holder.botaoOpcoesResposta.setVisibility(ehAutor ? View.VISIBLE : View.GONE);
        holder.botaoOpcoesResposta.setOnClickListener(v -> abrirMenuResposta(v, resposta));

        AnimationUtils.applyPressAnimation(holder.botaoLike);
        AnimationUtils.applyPressAnimation(holder.botaoDislike);
        AnimationUtils.applyPressAnimation(holder.botaoResponder);

        if (holder.botaoOpcoesResposta != null) {
            AnimationUtils.applyPressAnimation(holder.botaoOpcoesResposta);
        }

        if (holder.textoToggleRespostas != null) {
            AnimationUtils.applyPressAnimation(holder.textoToggleRespostas);
        }

        aplicarIndentacao(holder, resposta.getNivel());
        atualizarEstadoVisualReacoes(holder, resposta);

        holder.botaoLike.setOnClickListener(v -> {
            if (!usuarioEstaLogado()) {
                Toast.makeText(context, "Entre em uma conta para interagir.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!v.isEnabled()) return;
            v.setEnabled(false);

            AnimationUtils.playBounce(holder.botaoLike);

            boolean sucesso = forumStorage.toggleLikeResposta(
                    postId,
                    resposta.getId(),
                    sessionManager.getEmailUsuario()
            );

            if (sucesso) {
                int posicao = holder.getAdapterPosition();
                RespostaForum atualizada = buscarRespostaAtualizada(resposta.getId());

                if (posicao != RecyclerView.NO_POSITION && atualizada != null) {
                    lista.set(posicao, atualizada);
                    notifyItemChanged(posicao);
                }
            }

            v.postDelayed(() -> v.setEnabled(true), 250);
        });

        holder.botaoDislike.setOnClickListener(v -> {
            if (!usuarioEstaLogado()) {
                Toast.makeText(context, "Entre em uma conta para interagir.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!v.isEnabled()) return;
            v.setEnabled(false);

            AnimationUtils.playBounce(holder.botaoDislike);

            boolean sucesso = forumStorage.toggleDislikeResposta(
                    postId,
                    resposta.getId(),
                    sessionManager.getEmailUsuario()
            );

            if (sucesso) {
                int posicao = holder.getAdapterPosition();
                RespostaForum atualizada = buscarRespostaAtualizada(resposta.getId());

                if (posicao != RecyclerView.NO_POSITION && atualizada != null) {
                    lista.set(posicao, atualizada);
                    notifyItemChanged(posicao);
                }
            }

            v.postDelayed(() -> v.setEnabled(true), 250);
        });

        holder.botaoResponder.setOnClickListener(v -> {
            AnimationUtils.playBounce(v);
            abrirDialogNovaRespostaFilha(resposta);
        });

        if (resposta.isTemRespostas()) {
            holder.textoToggleRespostas.setVisibility(View.VISIBLE);
            int totalFilhasDiretas = contarFilhasDiretas(position, resposta.getNivel());
            boolean filhasVisiveis = temFilhasVisiveis(position, resposta.getNivel());

            if (filhasVisiveis) {
                holder.textoToggleRespostas.setText(
                        totalFilhasDiretas == 1
                                ? "Ocultar 1 resposta"
                                : "Ocultar " + totalFilhasDiretas + " respostas"
                );
            } else {
                holder.textoToggleRespostas.setText(
                        totalFilhasDiretas == 1
                                ? "Ver 1 resposta"
                                : "Ver " + totalFilhasDiretas + " respostas"
                );
            }

            holder.textoToggleRespostas.setOnClickListener(v -> {
                AnimationUtils.playBounce(v);
                alternarRespostasFilhas(position, resposta.getNivel());
                notifyDataSetChanged();
            });
        } else {
            holder.textoToggleRespostas.setVisibility(View.GONE);
            holder.textoToggleRespostas.setOnClickListener(null);
        }
    }

    private boolean usuarioEstaLogado() {
        return sessionManager.estaLogado()
                && sessionManager.getEmailUsuario() != null
                && !sessionManager.getEmailUsuario().trim().isEmpty();
    }

    private void aplicarIndentacao(ViewHolder holder, int nivelReal) {
        int nivelVisual = Math.min(Math.max(nivelReal, 0), MAX_NIVEL_VISUAL);
        int margemEsquerda = dpToPx(nivelVisual * INDENT_DP);

        MarginLayoutParams params = (MarginLayoutParams) holder.containerResposta.getLayoutParams();
        params.setMarginStart(margemEsquerda);
        params.bottomMargin = dpToPx(10);
        holder.containerResposta.setLayoutParams(params);

        if (nivelReal == 0) {
            holder.linhaThread.setVisibility(View.INVISIBLE);
        } else {
            holder.linhaThread.setVisibility(View.VISIBLE);

            if (nivelReal == 1) {
                holder.linhaThread.setAlpha(0.18f);
            } else if (nivelReal == 2) {
                holder.linhaThread.setAlpha(0.12f);
            } else {
                holder.linhaThread.setAlpha(0.08f);
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    private RespostaForum buscarRespostaAtualizada(String respostaId) {
        PostForum postAtualizado = forumStorage.buscarPostPorId(postId);
        if (postAtualizado == null || postAtualizado.getRespostas() == null) {
            return null;
        }

        for (RespostaForum resposta : postAtualizado.getRespostas()) {
            if (respostaId.equals(resposta.getId())) {
                return resposta;
            }
        }

        return null;
    }

    private void aplicarAvatar(ShapeableImageView imageView, String fotoUri, String nomeAutor) {
        imageView.setImageTintList(null);

        if (fotoUri != null && !fotoUri.isEmpty()) {
            Glide.with(context)
                    .load(Uri.parse(fotoUri))
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .circleCrop()
                    .into(imageView);
            return;
        }

        String chaveCache = nomeAutor != null && !nomeAutor.trim().isEmpty()
                ? nomeAutor.trim().toLowerCase()
                : "avatar_padrao_resposta";

        Bitmap avatar = avatarCache.get(chaveCache);

        if (avatar == null) {
            avatar = AvatarUtils.criarAvatarComInicial(context, nomeAutor, 72);
            if (avatar != null) {
                avatarCache.put(chaveCache, avatar);
            }
        }

        if (avatar != null) {
            imageView.setImageBitmap(avatar);
        } else {
            imageView.setImageResource(R.drawable.ic_user);
        }
    }

    private void atualizarEstadoVisualReacoes(ViewHolder holder, RespostaForum resposta) {
        if (!usuarioEstaLogado()) {
            holder.iconeLikeResposta.setAlpha(1f);
            holder.iconeDislikeResposta.setAlpha(1f);
            holder.textoLikes.setAlpha(1f);
            holder.textoDislikes.setAlpha(1f);
            holder.iconeLikeResposta.setColorFilter(ContextCompat.getColor(context, R.color.green_like));
            holder.iconeDislikeResposta.setColorFilter(ContextCompat.getColor(context, R.color.red_dislike));
            return;
        }

        boolean curtiu = resposta.usuarioCurtiu(sessionManager.getEmailUsuario());
        boolean descurtiu = resposta.usuarioDescurtiu(sessionManager.getEmailUsuario());

        holder.iconeLikeResposta.setAlpha(curtiu ? 1f : 0.60f);
        holder.textoLikes.setAlpha(curtiu ? 1f : 0.80f);
        holder.iconeDislikeResposta.setAlpha(descurtiu ? 1f : 0.60f);
        holder.textoDislikes.setAlpha(descurtiu ? 1f : 0.80f);

        holder.iconeLikeResposta.setColorFilter(ContextCompat.getColor(
                context,
                curtiu ? R.color.green_like_active : R.color.green_like
        ));

        holder.iconeDislikeResposta.setColorFilter(ContextCompat.getColor(
                context,
                descurtiu ? R.color.red_dislike_active : R.color.red_dislike
        ));
    }

    private void abrirDialogNovaRespostaFilha(RespostaForum respostaPai) {
        if (!usuarioEstaLogado()) {
            Toast.makeText(context, "Entre em uma conta para interagir.", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(context);
        input.setHint("Digite sua resposta");
        input.setMinLines(3);
        input.setPadding(dpToPxInt(16), dpToPxInt(14), dpToPxInt(16), dpToPxInt(14));
        input.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(ForumLimits.MAX_RESPOSTA)
        });

        new AlertDialog.Builder(context)
                .setTitle("Responder comentário")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Responder", (dialog, which) -> {
                    String texto = InputSecurityUtils.sanitizeUserText(
                            input.getText().toString()
                    );

                    if (InputSecurityUtils.isNullOrBlank(texto)) {
                        Toast.makeText(context, "Digite uma resposta.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (InputSecurityUtils.containsSuspiciousPattern(texto)) {
                        Toast.makeText(context, "Conteúdo inválido detectado.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    RespostaForum novaResposta = new RespostaForum(
                            sessionManager.getNomeUsuario(),
                            sessionManager.getEmailUsuario(),
                            sessionManager.getFotoUsuario(),
                            texto,
                            TimeUtils.agora(),
                            respostaPai.getNivel() + 1,
                            true,
                            false
                    );

                    boolean sucesso = forumStorage.adicionarRespostaFilha(
                            postId,
                            respostaPai.getId(),
                            novaResposta
                    );

                    if (sucesso) {
                        Toast.makeText(context, "Resposta enviada com sucesso.", Toast.LENGTH_SHORT).show();
                        atualizarLista();
                    } else {
                        Toast.makeText(context, "Não foi possível responder agora.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void abrirMenuResposta(View anchor, RespostaForum resposta) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.getMenu().add("Editar");
        popupMenu.getMenu().add("Excluir");

        popupMenu.setOnMenuItemClickListener(item -> {
            String titulo = item.getTitle().toString();

            if ("Editar".equals(titulo)) {
                abrirDialogEditarResposta(resposta);
                return true;
            }

            if ("Excluir".equals(titulo)) {
                confirmarExclusaoResposta(resposta);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void abrirDialogEditarResposta(RespostaForum resposta) {
        EditText input = new EditText(context);
        input.setText(textoSeguro(resposta.getMensagem(), ""));
        input.setMinLines(3);
        input.setPadding(dpToPxInt(16), dpToPxInt(14), dpToPxInt(16), dpToPxInt(14));
        input.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(ForumLimits.MAX_RESPOSTA)
        });

        new AlertDialog.Builder(context)
                .setTitle("Editar resposta")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novaMensagem = InputSecurityUtils.sanitizeUserText(
                            input.getText().toString()
                    );

                    if (InputSecurityUtils.isNullOrBlank(novaMensagem)) {
                        Toast.makeText(context, "Digite uma mensagem.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (InputSecurityUtils.containsSuspiciousPattern(novaMensagem)) {
                        Toast.makeText(context, "Conteúdo inválido detectado.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean sucesso = forumStorage.editarResposta(postId, resposta.getId(), novaMensagem);

                    if (sucesso) {
                        Toast.makeText(context, "Resposta editada com sucesso.", Toast.LENGTH_SHORT).show();
                        atualizarLista();
                    } else {
                        Toast.makeText(context, "Não foi possível editar a resposta.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void confirmarExclusaoResposta(RespostaForum resposta) {
        new AlertDialog.Builder(context)
                .setTitle("Excluir resposta")
                .setMessage("Tem certeza que deseja excluir esta resposta?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    boolean sucesso = forumStorage.excluirResposta(postId, resposta.getId());

                    if (sucesso) {
                        Toast.makeText(context, "Resposta excluída com sucesso.", Toast.LENGTH_SHORT).show();
                        atualizarLista();
                    } else {
                        Toast.makeText(context, "Não foi possível excluir a resposta.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private boolean temFilhasVisiveis(int posicaoPai, int nivelPai) {
        for (int i = posicaoPai + 1; i < lista.size(); i++) {
            RespostaForum atual = lista.get(i);

            if (atual.getNivel() <= nivelPai) {
                break;
            }

            if (atual.getNivel() == nivelPai + 1) {
                return atual.isVisivel();
            }
        }

        return false;
    }

    private void atualizarLista() {
        PostForum postAtualizado = forumStorage.buscarPostPorId(postId);
        if (postAtualizado != null) {
            atualizarDadosPreservandoVisibilidade(postAtualizado.getRespostas());
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
    }

    public void atualizarDadosPreservandoVisibilidade(List<RespostaForum> novasRespostas) {
        Map<String, Boolean> mapaVisibilidade = new HashMap<>();
        for (RespostaForum resposta : lista) {
            mapaVisibilidade.put(resposta.getId(), resposta.isVisivel());
        }

        lista.clear();

        if (novasRespostas != null) {
            for (RespostaForum resposta : novasRespostas) {
                Boolean visivelAnterior = mapaVisibilidade.get(resposta.getId());
                if (visivelAnterior != null) {
                    resposta.setVisivel(visivelAnterior);
                }
                lista.add(resposta);
            }
        }

        notifyDataSetChanged();
    }

    private int contarFilhasDiretas(int posicaoPai, int nivelPai) {
        int total = 0;

        for (int i = posicaoPai + 1; i < lista.size(); i++) {
            RespostaForum atual = lista.get(i);

            if (atual.getNivel() <= nivelPai) {
                break;
            }

            if (atual.getNivel() == nivelPai + 1) {
                total++;
            }
        }

        return total;
    }

    public void atualizarDados(List<RespostaForum> novasRespostas) {
        lista.clear();
        if (novasRespostas != null) {
            lista.addAll(novasRespostas);
        }
        notifyDataSetChanged();
    }

    private String textoSeguro(String valor, String fallback) {
        String texto = InputSecurityUtils.sanitizeUserText(valor);
        return texto.isEmpty() ? fallback : texto;
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }
}