package com.example.mapadointercambista.adapter.forum;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.InputFilter;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.mapadointercambista.activity.forum.RespostasForumActivity;
import com.example.mapadointercambista.model.forum.ForumStorage;
import com.example.mapadointercambista.model.forum.PostForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.util.AnimationUtils;
import com.example.mapadointercambista.util.AvatarUtils;
import com.example.mapadointercambista.util.ForumLimits;
import com.example.mapadointercambista.util.InputSecurityUtils;
import com.example.mapadointercambista.util.TransitionHelper;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class PostForumAdapter extends RecyclerView.Adapter<PostForumAdapter.ViewHolder> {

    private final Context context;
    private final List<PostForum> lista;
    private final boolean modoCompleto;
    private final SessionManager sessionManager;
    private final ForumStorage forumStorage;
    private final LruCache<String, Bitmap> avatarCache = new LruCache<>(50);

    public PostForumAdapter(Context context, List<PostForum> lista, boolean modoCompleto) {
        this.context = context;
        this.lista = lista;
        this.modoCompleto = modoCompleto;
        this.sessionManager = new SessionManager(context);
        this.forumStorage = new ForumStorage(context);
        setHasStableIds(true);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView fotoPerfil;
        TextView usuario;
        TextView textoBadgeVocePost;
        TextView textoTituloPost;
        TextView mensagem;
        TextView textoTempoPostagem;
        TextView textoLikes;
        TextView textoDislikes;
        TextView textoRespostas;
        LinearLayout botaoRespostas;
        LinearLayout botaoLikeContainer;
        LinearLayout botaoDislikeContainer;
        View botaoOpcoesPost;
        ImageView iconeLike;
        ImageView iconeDislike;

        public ViewHolder(View itemView) {
            super(itemView);
            fotoPerfil = itemView.findViewById(R.id.fotoPerfil);
            usuario = itemView.findViewById(R.id.nomeUsuario);
            textoBadgeVocePost = itemView.findViewById(R.id.textoBadgeVocePost);
            textoTituloPost = itemView.findViewById(R.id.textoTituloPost);
            mensagem = itemView.findViewById(R.id.textoMensagem);
            textoTempoPostagem = itemView.findViewById(R.id.textoTempoPostagem);
            textoLikes = itemView.findViewById(R.id.textoLikes);
            textoDislikes = itemView.findViewById(R.id.textoDislikes);
            textoRespostas = itemView.findViewById(R.id.textoRespostas);
            botaoRespostas = itemView.findViewById(R.id.botaoRespostas);
            botaoLikeContainer = itemView.findViewById(R.id.botaoLikeContainer);
            botaoDislikeContainer = itemView.findViewById(R.id.botaoDislikeContainer);
            botaoOpcoesPost = itemView.findViewById(R.id.botaoOpcoesPost);
            iconeLike = itemView.findViewById(R.id.iconeLike);
            iconeDislike = itemView.findViewById(R.id.iconeDislike);
        }
    }

    @Override
    public long getItemId(int position) {
        String id = lista.get(position).getId();
        return id != null ? id.hashCode() : position;
    }

    @NonNull
    @Override
    public PostForumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_forum, parent, false);

        AnimationUtils.applyPressAnimation(view);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostForumAdapter.ViewHolder holder, int position) {
        PostForum post = lista.get(position);

        holder.usuario.setText(textoSeguro(post.getAutorNome(), "Usuário"));
        holder.textoTituloPost.setText(textoSeguro(post.getTitulo(), "Sem título"));
        holder.mensagem.setText(textoSeguro(post.getMensagem(), ""));
        holder.textoTempoPostagem.setText("· " + post.getTempoPostagem());
        holder.textoLikes.setText(String.valueOf(post.getLikes()));
        holder.textoDislikes.setText(String.valueOf(post.getDislikes()));
        holder.textoRespostas.setText(post.getQuantidadeRespostas() + " respostas");

        aplicarAvatar(holder.fotoPerfil, post.getAutorFotoUri(), post.getAutorNome());

        boolean ehAutor = sessionManager.estaLogado()
                && sessionManager.getEmailUsuario() != null
                && sessionManager.getEmailUsuario().equals(post.getAutorEmail())
                && modoCompleto;

        holder.textoBadgeVocePost.setVisibility(ehAutor ? View.VISIBLE : View.GONE);
        holder.botaoOpcoesPost.setVisibility(ehAutor ? View.VISIBLE : View.GONE);
        holder.botaoOpcoesPost.setOnClickListener(v -> abrirMenuPost(v, post));

        if (!modoCompleto) {
            holder.textoTituloPost.setMaxLines(2);
            holder.mensagem.setMaxLines(2);
        } else {
            holder.textoTituloPost.setMaxLines(3);
            holder.mensagem.setMaxLines(Integer.MAX_VALUE);
        }

        atualizarEstadoVisualReacoes(holder, post);

        AnimationUtils.applyPressAnimation(holder.botaoLikeContainer);
        AnimationUtils.applyPressAnimation(holder.botaoDislikeContainer);
        AnimationUtils.applyPressAnimation(holder.botaoRespostas);

        if (holder.botaoOpcoesPost != null) {
            AnimationUtils.applyPressAnimation(holder.botaoOpcoesPost);
        }

        holder.botaoLikeContainer.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(context, "Entre em uma conta para interagir.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!v.isEnabled()) return;
            v.setEnabled(false);

            AnimationUtils.playBounce(holder.botaoLikeContainer);

            boolean sucesso = forumStorage.toggleLikePost(post.getId(), sessionManager.getEmailUsuario());
            if (sucesso) {
                int posicao = holder.getAdapterPosition();
                PostForum atualizado = buscarPostAtualizado(post.getId());

                if (posicao != RecyclerView.NO_POSITION && atualizado != null) {
                    lista.set(posicao, atualizado);
                    notifyItemChanged(posicao);
                }
            }

            v.postDelayed(() -> v.setEnabled(true), 250);
        });

        holder.botaoDislikeContainer.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(context, "Entre em uma conta para interagir.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!v.isEnabled()) return;
            v.setEnabled(false);

            AnimationUtils.playBounce(holder.botaoDislikeContainer);

            boolean sucesso = forumStorage.toggleDislikePost(post.getId(), sessionManager.getEmailUsuario());
            if (sucesso) {
                int posicao = holder.getAdapterPosition();
                PostForum atualizado = buscarPostAtualizado(post.getId());

                if (posicao != RecyclerView.NO_POSITION && atualizado != null) {
                    lista.set(posicao, atualizado);
                    notifyItemChanged(posicao);
                }
            }

            v.postDelayed(() -> v.setEnabled(true), 250);
        });

        holder.botaoRespostas.setOnClickListener(v -> {
            AnimationUtils.playBounce(v);

            Intent intent = new Intent(context, RespostasForumActivity.class);
            intent.putExtra("postSelecionado", post);
            context.startActivity(intent);
            if (context instanceof Activity) {
                TransitionHelper.slideForward((Activity) context);
            }
        });
    }

    private int dpToPxInt(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
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
                : "avatar_padrao";

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

    private void atualizarEstadoVisualReacoes(ViewHolder holder, PostForum post) {
        if (!sessionManager.estaLogado() || sessionManager.getEmailUsuario() == null) {
            holder.iconeLike.setAlpha(1f);
            holder.iconeDislike.setAlpha(1f);
            holder.textoLikes.setAlpha(1f);
            holder.textoDislikes.setAlpha(1f);

            holder.iconeLike.setColorFilter(ContextCompat.getColor(context, R.color.green_like));
            holder.iconeDislike.setColorFilter(ContextCompat.getColor(context, R.color.red_dislike));
            return;
        }

        boolean curtiu = post.usuarioCurtiu(sessionManager.getEmailUsuario());
        boolean descurtiu = post.usuarioDescurtiu(sessionManager.getEmailUsuario());

        holder.iconeLike.setAlpha(curtiu ? 1f : 0.60f);
        holder.textoLikes.setAlpha(curtiu ? 1f : 0.80f);
        holder.iconeDislike.setAlpha(descurtiu ? 1f : 0.60f);
        holder.textoDislikes.setAlpha(descurtiu ? 1f : 0.80f);

        holder.iconeLike.setColorFilter(ContextCompat.getColor(
                context,
                curtiu ? R.color.green_like_active : R.color.green_like
        ));

        holder.iconeDislike.setColorFilter(ContextCompat.getColor(
                context,
                descurtiu ? R.color.red_dislike_active : R.color.red_dislike
        ));
    }

    private void abrirMenuPost(View anchor, PostForum post) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.getMenu().add("Editar");
        popupMenu.getMenu().add("Excluir");

        popupMenu.setOnMenuItemClickListener(item -> {
            String titulo = item.getTitle().toString();

            if ("Editar".equals(titulo)) {
                abrirDialogEditarPost(post);
                return true;
            }

            if ("Excluir".equals(titulo)) {
                confirmarExclusaoPost(post);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void abrirDialogEditarPost(PostForum post) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPxInt(16), dpToPxInt(12), dpToPxInt(16), dpToPxInt(8));

        EditText inputTitulo = new EditText(context);
        inputTitulo.setHint("Título");
        inputTitulo.setText(textoSeguro(post.getTitulo(), ""));
        inputTitulo.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(ForumLimits.MAX_TITULO_POST)
        });

        EditText inputMensagem = new EditText(context);
        inputMensagem.setHint("Mensagem");
        inputMensagem.setText(textoSeguro(post.getMensagem(), ""));
        inputMensagem.setMinLines(4);
        inputMensagem.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(ForumLimits.MAX_TEXTO_POST)
        });

        container.addView(inputTitulo);
        container.addView(inputMensagem);

        new AlertDialog.Builder(context)
                .setTitle("Editar post")
                .setView(container)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novoTitulo = InputSecurityUtils.sanitizeUserText(inputTitulo.getText().toString());
                    String novaMensagem = InputSecurityUtils.sanitizeUserText(inputMensagem.getText().toString());

                    if (InputSecurityUtils.isNullOrBlank(novoTitulo)) {
                        Toast.makeText(context, "Digite um título.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (InputSecurityUtils.isNullOrBlank(novaMensagem)) {
                        Toast.makeText(context, "Digite uma mensagem.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean sucesso = forumStorage.editarPost(post.getId(), novoTitulo, novaMensagem);

                    if (sucesso) {
                        Toast.makeText(context, "Post editado com sucesso.", Toast.LENGTH_SHORT).show();
                        atualizarListaCompleta();
                    } else {
                        Toast.makeText(context, "Não foi possível editar o post.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void confirmarExclusaoPost(PostForum post) {
        new AlertDialog.Builder(context)
                .setTitle("Excluir post")
                .setMessage("Tem certeza que deseja excluir esta publicação?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    boolean sucesso = forumStorage.excluirPost(post.getId());

                    if (sucesso) {
                        Toast.makeText(context, "Post excluído com sucesso.", Toast.LENGTH_SHORT).show();
                        atualizarListaCompleta();
                    } else {
                        Toast.makeText(context, "Erro ao excluir post.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private PostForum buscarPostAtualizado(String postId) {
        List<PostForum> posts = forumStorage.carregarPosts();
        if (posts == null) return null;

        for (PostForum post : posts) {
            if (postId.equals(post.getId())) {
                return post;
            }
        }
        return null;
    }

    private void atualizarListaCompleta() {
        List<PostForum> novosPosts = forumStorage.carregarPosts();
        atualizarDados(novosPosts);
    }

    private String textoSeguro(String valor, String fallback) {
        String texto = InputSecurityUtils.sanitizeUserText(valor);
        return texto.isEmpty() ? fallback : texto;
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public void atualizarDados(List<PostForum> novosPosts) {
        lista.clear();
        if (novosPosts != null) {
            lista.addAll(novosPosts);
        }
        notifyDataSetChanged();
    }
}