package com.example.mapadointercambista.activity.forum;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mapadointercambista.R;
import com.example.mapadointercambista.adapter.forum.RespostaForumAdapter;
import com.example.mapadointercambista.model.forum.ForumStorage;
import com.example.mapadointercambista.model.forum.PostForum;
import com.example.mapadointercambista.model.forum.RespostaForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.util.AvatarUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class RespostasForumActivity extends AppCompatActivity {

    private RecyclerView listaRespostas;
    private ForumStorage forumStorage;
    private SessionManager sessionManager;
    private PostForum postAtual;

    private ShapeableImageView fotoPerfilPostOriginal;
    private ShapeableImageView iconeAvatarResponder;
    private TextView nome;
    private TextView tempo;
    private TextView mensagem;
    private TextView textoLikes;
    private TextView textoDislikes;
    private TextView textoRespostas;
    private TextView textoResponder;

    private LinearLayout botaoLikePostOriginal;
    private LinearLayout botaoDislikePostOriginal;
    private ImageView botaoOpcoesPostOriginal;
    private ImageView iconeLikePostOriginal;
    private ImageView iconeDislikePostOriginal;
    private TextView textoBadgeVocePostOriginal;

    private RespostaForumAdapter adapter;
    private final List<RespostaForum> respostasExibidas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_respostas_forum);

        aplicarModoImersivo();

        forumStorage = new ForumStorage(this);
        sessionManager = new SessionManager(this);

        inicializarViews();
        inicializarPost();
        configurarLista();
        configurarCaixaResponder();
        configurarAcoesPostOriginal();
        preencherPostOriginal();
        carregarRespostas();

        findViewById(R.id.botaoResponderAcao).setOnClickListener(v -> abrirTelaNovaResposta());
        findViewById(R.id.cardResponder).setOnClickListener(v -> abrirTelaNovaResposta());
        findViewById(R.id.botaoVoltarRespostas).setOnClickListener(v -> finish());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_forum);
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarModoImersivo();
        sincronizarPostAtual();
        configurarCaixaResponder();
        configurarAcoesPostOriginal();
        preencherPostOriginal();
        carregarRespostas();
    }

    private void aplicarModoImersivo() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void inicializarViews() {
        fotoPerfilPostOriginal = findViewById(R.id.fotoPerfilPostOriginal);
        iconeAvatarResponder = findViewById(R.id.iconeAvatarResponder);

        nome = findViewById(R.id.nomeUsuarioPostOriginal);
        tempo = findViewById(R.id.tempoPostOriginal);
        mensagem = findViewById(R.id.mensagemPostOriginal);

        textoLikes = findViewById(R.id.textoLikesPostOriginal);
        textoDislikes = findViewById(R.id.textoDislikesPostOriginal);
        textoRespostas = findViewById(R.id.textoQuantidadeRespostasPostOriginal);
        textoResponder = findViewById(R.id.textoResponderBloqueado);

        botaoLikePostOriginal = findViewById(R.id.botaoLikePostOriginal);
        botaoDislikePostOriginal = findViewById(R.id.botaoDislikePostOriginal);
        botaoOpcoesPostOriginal = findViewById(R.id.botaoOpcoesPostOriginal);

        iconeLikePostOriginal = findViewById(R.id.iconeLikePostOriginal);
        iconeDislikePostOriginal = findViewById(R.id.iconeDislikePostOriginal);

        textoBadgeVocePostOriginal = findViewById(R.id.textoBadgeVocePostOriginal);
        listaRespostas = findViewById(R.id.listaRespostas);
    }

    private void inicializarPost() {
        PostForum postRecebido = (PostForum) getIntent().getSerializableExtra("postSelecionado");

        if (postRecebido != null) {
            postAtual = forumStorage.buscarPostPorId(postRecebido.getId());
            if (postAtual == null) {
                postAtual = postRecebido;
            }
        }
    }

    private void configurarLista() {
        listaRespostas.setLayoutManager(new LinearLayoutManager(this));
        listaRespostas.setHasFixedSize(false);
        listaRespostas.setItemViewCacheSize(12);

        adapter = new RespostaForumAdapter(
                this,
                postAtual != null ? postAtual.getId() : "",
                respostasExibidas,
                sessionManager.estaLogado()
        );

        listaRespostas.setAdapter(adapter);
    }

    private void sincronizarPostAtual() {
        if (postAtual != null) {
            PostForum recarregado = forumStorage.buscarPostPorId(postAtual.getId());
            if (recarregado != null) {
                postAtual = recarregado;
            }
        }
    }

    private void abrirTelaNovaResposta() {
        if (!sessionManager.estaLogado()) {
            Toast.makeText(this, "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (postAtual == null) {
            Toast.makeText(this, "Post não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        android.content.Intent intent = new android.content.Intent(this, NovaRespostaActivity.class);
        intent.putExtra(NovaRespostaActivity.EXTRA_POST_ID, postAtual.getId());
        startActivity(intent);
    }

    private void configurarCaixaResponder() {
        if (sessionManager.estaLogado()) {
            textoResponder.setText("Postar sua resposta");

            String fotoUsuario = sessionManager.getFotoUsuario();
            iconeAvatarResponder.setImageTintList(null);

            if (fotoUsuario != null && !fotoUsuario.isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(fotoUsuario))
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user)
                        .circleCrop()
                        .into(iconeAvatarResponder);
            } else {
                Bitmap avatar = AvatarUtils.criarAvatarComInicial(this, sessionManager.getNomeUsuario(), 72);
                iconeAvatarResponder.setImageBitmap(avatar);
            }
        } else {
            textoResponder.setText("Entre em sua conta para responder");
            iconeAvatarResponder.setImageResource(R.drawable.ic_user);
            iconeAvatarResponder.setImageTintList(
                    ContextCompat.getColorStateList(this, android.R.color.white)
            );
        }
    }

    private void configurarAcoesPostOriginal() {
        botaoLikePostOriginal.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
                return;
            }

            if (postAtual == null) return;

            animarClique(botaoLikePostOriginal);
            boolean sucesso = forumStorage.toggleLikePost(postAtual.getId(), sessionManager.getEmailUsuario());

            if (sucesso) {
                recarregarPostAtual();
            }
        });

        botaoDislikePostOriginal.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
                return;
            }

            if (postAtual == null) return;

            animarClique(botaoDislikePostOriginal);
            boolean sucesso = forumStorage.toggleDislikePost(postAtual.getId(), sessionManager.getEmailUsuario());

            if (sucesso) {
                recarregarPostAtual();
            }
        });

        boolean ehAutor = postAtual != null
                && sessionManager.estaLogado()
                && sessionManager.getEmailUsuario() != null
                && sessionManager.getEmailUsuario().equals(postAtual.getAutorEmail());

        botaoOpcoesPostOriginal.setVisibility(ehAutor ? View.VISIBLE : View.GONE);

        botaoOpcoesPostOriginal.setOnClickListener(v -> {
            if (postAtual == null) return;

            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenu().add("Editar");
            popupMenu.getMenu().add("Excluir");

            popupMenu.setOnMenuItemClickListener(item -> {
                String titulo = item.getTitle().toString();

                if ("Editar".equals(titulo)) {
                    abrirDialogEditarPostOriginal();
                    return true;
                }

                if ("Excluir".equals(titulo)) {
                    confirmarExclusaoPostOriginal();
                    return true;
                }

                return false;
            });

            popupMenu.show();
        });
    }

    private void atualizarEstadoVisualPostOriginal() {
        if (postAtual == null) return;

        if (!sessionManager.estaLogado() || sessionManager.getEmailUsuario() == null) {
            iconeLikePostOriginal.setAlpha(1f);
            iconeDislikePostOriginal.setAlpha(1f);
            textoLikes.setAlpha(1f);
            textoDislikes.setAlpha(1f);

            iconeLikePostOriginal.setColorFilter(ContextCompat.getColor(this, R.color.green_like));
            iconeDislikePostOriginal.setColorFilter(ContextCompat.getColor(this, R.color.red_dislike));
            return;
        }

        boolean curtiu = postAtual.usuarioCurtiu(sessionManager.getEmailUsuario());
        boolean descurtiu = postAtual.usuarioDescurtiu(sessionManager.getEmailUsuario());

        iconeLikePostOriginal.setAlpha(curtiu ? 1f : 0.60f);
        textoLikes.setAlpha(curtiu ? 1f : 0.80f);
        iconeDislikePostOriginal.setAlpha(descurtiu ? 1f : 0.60f);
        textoDislikes.setAlpha(descurtiu ? 1f : 0.80f);

        iconeLikePostOriginal.setColorFilter(ContextCompat.getColor(
                this, curtiu ? R.color.green_like_active : R.color.green_like
        ));

        iconeDislikePostOriginal.setColorFilter(ContextCompat.getColor(
                this, descurtiu ? R.color.red_dislike_active : R.color.red_dislike
        ));
    }

    private void preencherPostOriginal() {
        if (postAtual == null) return;

        aplicarAvatar(fotoPerfilPostOriginal, postAtual.getAutorFotoUri(), postAtual.getAutorNome());

        boolean ehAutor = sessionManager.estaLogado()
                && sessionManager.getEmailUsuario() != null
                && sessionManager.getEmailUsuario().equals(postAtual.getAutorEmail());

        textoBadgeVocePostOriginal.setVisibility(ehAutor ? View.VISIBLE : View.GONE);

        nome.setText(postAtual.getAutorNome());
        tempo.setText("· " + postAtual.getTempoPostagem());
        mensagem.setText(postAtual.getMensagem());
        textoLikes.setText(String.valueOf(postAtual.getLikes()));
        textoDislikes.setText(String.valueOf(postAtual.getDislikes()));
        textoRespostas.setText(postAtual.getQuantidadeRespostas() + " respostas");

        atualizarEstadoVisualPostOriginal();
    }

    private void carregarRespostas() {
        respostasExibidas.clear();

        if (postAtual != null && postAtual.getRespostas() != null) {
            respostasExibidas.addAll(postAtual.getRespostas());
        }

        adapter.notifyDataSetChanged();
    }

    private void abrirDialogEditarPostOriginal() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(postAtual.getMensagem());
        input.setMinLines(3);
        input.setPadding(40, 30, 40, 30);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Editar post")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novaMensagem = input.getText().toString().trim();

                    if (novaMensagem.isEmpty()) {
                        Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean sucesso = forumStorage.editarPost(postAtual.getId(), novaMensagem);

                    if (sucesso) {
                        recarregarPostAtual();
                        Toast.makeText(this, "Post editado com sucesso", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro ao editar post", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void confirmarExclusaoPostOriginal() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Excluir post")
                .setMessage("Tem certeza que deseja excluir esta publicação?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    boolean sucesso = forumStorage.excluirPost(postAtual.getId());

                    if (sucesso) {
                        Toast.makeText(this, "Post excluído com sucesso", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erro ao excluir post", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void recarregarPostAtual() {
        sincronizarPostAtual();
        configurarAcoesPostOriginal();
        preencherPostOriginal();
        carregarRespostas();
    }

    private void animarClique(View view) {
        ObjectAnimator diminuirX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.96f, 1f);
        ObjectAnimator diminuirY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.96f, 1f);
        diminuirX.setDuration(120);
        diminuirY.setDuration(120);
        diminuirX.start();
        diminuirY.start();
    }

    private void aplicarAvatar(ShapeableImageView imageView, String fotoUri, String nomeAutor) {
        imageView.setImageTintList(null);

        if (fotoUri != null && !fotoUri.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(fotoUri))
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .circleCrop()
                    .into(imageView);
        } else {
            Bitmap avatar = AvatarUtils.criarAvatarComInicial(this, nomeAutor, 72);
            imageView.setImageBitmap(avatar);
        }
    }
}