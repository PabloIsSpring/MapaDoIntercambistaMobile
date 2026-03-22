package com.example.mapadointercambista.adapter.forum;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.forum.RespostasForumActivity;
import com.example.mapadointercambista.util.AvatarUtils;
import com.example.mapadointercambista.model.forum.ForumStorage;
import com.example.mapadointercambista.model.forum.PostForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class PostForumAdapter extends RecyclerView.Adapter<PostForumAdapter.ViewHolder> {

    private final Context context;
    private final List<PostForum> lista;
    private final boolean modoCompleto;
    private final SessionManager sessionManager;
    private final ForumStorage forumStorage;

    public PostForumAdapter(Context context, List<PostForum> lista, boolean modoCompleto) {
        this.context = context;
        this.lista = lista;
        this.modoCompleto = modoCompleto;
        this.sessionManager = new SessionManager(context);
        this.forumStorage = new ForumStorage(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView fotoPerfil;
        TextView usuario;
        TextView textoBadgeVocePost;
        TextView mensagem;
        TextView textoTempoPostagem;
        TextView textoLikes;
        TextView textoDislikes;
        TextView textoRespostas;
        LinearLayout botaoRespostas;
        LinearLayout botaoLikeContainer;
        LinearLayout botaoDislikeContainer;
        ImageView botaoOpcoesPost;
        ImageView iconeLike;
        ImageView iconeDislike;

        public ViewHolder(View itemView) {
            super(itemView);

            fotoPerfil = itemView.findViewById(R.id.fotoPerfil);
            usuario = itemView.findViewById(R.id.nomeUsuario);
            textoBadgeVocePost = itemView.findViewById(R.id.textoBadgeVocePost);
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

    @NonNull
    @Override
    public PostForumAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_forum, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostForumAdapter.ViewHolder holder, int position) {
        PostForum post = lista.get(position);

        holder.usuario.setText(post.getAutorNome());
        holder.mensagem.setText(post.getMensagem());
        holder.textoTempoPostagem.setText("· " + post.getTempoPostagem());
        holder.textoLikes.setText(String.valueOf(post.getLikes()));
        holder.textoDislikes.setText(String.valueOf(post.getDislikes()));
        holder.textoRespostas.setText(post.getQuantidadeRespostas() + " respostas");

        aplicarAvatar(holder.fotoPerfil, post.getAutorFotoUri(), post.getAutorNome());

        boolean ehAutor = sessionManager.estaLogado()
                && sessionManager.getEmailUsuario().equals(post.getAutorEmail())
                && modoCompleto;

        holder.textoBadgeVocePost.setVisibility(ehAutor ? View.VISIBLE : View.GONE);
        holder.botaoOpcoesPost.setVisibility(ehAutor ? View.VISIBLE : View.GONE);
        holder.botaoOpcoesPost.setOnClickListener(v -> abrirMenuPost(v, post));

        atualizarEstadoVisualReacoes(holder, post);

        holder.botaoLikeContainer.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(context, "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
                return;
            }

            animarClique(holder.botaoLikeContainer);

            boolean sucesso = forumStorage.toggleLikePost(post.getId(), sessionManager.getEmailUsuario());

            if (sucesso) {
                atualizarLista();
            }
        });

        holder.botaoDislikeContainer.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(context, "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
                return;
            }

            animarClique(holder.botaoDislikeContainer);

            boolean sucesso = forumStorage.toggleDislikePost(post.getId(), sessionManager.getEmailUsuario());

            if (sucesso) {
                atualizarLista();
            }
        });

        holder.botaoRespostas.setOnClickListener(v -> {
            Intent intent = new Intent(context, RespostasForumActivity.class);
            intent.putExtra("postSelecionado", post);
            context.startActivity(intent);
        });
    }

    private void animarClique(View view) {
        ObjectAnimator diminuirX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.92f, 1f);
        ObjectAnimator diminuirY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.92f, 1f);
        diminuirX.setDuration(180);
        diminuirY.setDuration(180);
        diminuirX.start();
        diminuirY.start();
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

    private void atualizarEstadoVisualReacoes(ViewHolder holder, PostForum post) {
        if (!sessionManager.estaLogado()) {
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

    private void abrirMenuPost(View anchor, PostForum post) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.getMenu().add("Editar");
        popupMenu.getMenu().add("Excluir");

        popupMenu.setOnMenuItemClickListener(item -> {
            String titulo = item.getTitle().toString();

            if (titulo.equals("Editar")) {
                abrirDialogEditarPost(post);
                return true;
            }

            if (titulo.equals("Excluir")) {
                confirmarExclusaoPost(post);
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void abrirDialogEditarPost(PostForum post) {
        EditText input = new EditText(context);
        input.setText(post.getMensagem());
        input.setMinLines(3);
        input.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(context)
                .setTitle("Editar post")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novaMensagem = input.getText().toString().trim();

                    if (novaMensagem.isEmpty()) {
                        Toast.makeText(context, "Digite uma mensagem", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean sucesso = forumStorage.editarPost(post.getId(), novaMensagem);

                    if (sucesso) {
                        Toast.makeText(context, "Post editado com sucesso", Toast.LENGTH_SHORT).show();
                        atualizarLista();
                    } else {
                        Toast.makeText(context, "Erro ao editar post", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, "Post excluído com sucesso", Toast.LENGTH_SHORT).show();
                        atualizarLista();
                    } else {
                        Toast.makeText(context, "Erro ao excluir post", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void atualizarLista() {
        List<PostForum> novosPosts = forumStorage.carregarPosts();
        lista.clear();
        lista.addAll(novosPosts);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}