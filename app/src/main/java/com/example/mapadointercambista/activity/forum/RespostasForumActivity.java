package com.example.mapadointercambista.activity.forum;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.adapter.forum.RespostaForumAdapter;
import com.example.mapadointercambista.util.AvatarUtils;
import com.example.mapadointercambista.model.forum.ForumStorage;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.model.forum.PostForum;
import com.example.mapadointercambista.model.forum.RespostaForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.util.TimeUtils;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_respostas_forum);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        forumStorage = new ForumStorage(this);
        sessionManager = new SessionManager(this);

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

        listaRespostas = findViewById(R.id.listaRespostas);
        listaRespostas.setLayoutManager(new LinearLayoutManager(this));

        iconeLikePostOriginal = findViewById(R.id.iconeLikePostOriginal);
        iconeDislikePostOriginal = findViewById(R.id.iconeDislikePostOriginal);

        textoBadgeVocePostOriginal = findViewById(R.id.textoBadgeVocePostOriginal);



        PostForum postRecebido = (PostForum) getIntent().getSerializableExtra("postSelecionado");

        if (postRecebido != null) {
            postAtual = forumStorage.buscarPostPorId(postRecebido.getId());
            if (postAtual == null) {
                postAtual = postRecebido;
            }
        }

        configurarCaixaResponder();
        configurarAcoesPostOriginal();
        preencherPostOriginal();
        carregarRespostas();

        findViewById(R.id.botaoResponderAcao).setOnClickListener(v -> abrirDialogNovaResposta());
        findViewById(R.id.cardResponder).setOnClickListener(v -> abrirDialogNovaResposta());
        findViewById(R.id.botaoVoltarRespostas).setOnClickListener(v -> finish());
        findViewById(R.id.botaoResponderAcao).setOnClickListener(v -> abrirTelaNovaResposta());
        findViewById(R.id.cardResponder).setOnClickListener(v -> abrirTelaNovaResposta());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_forum);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (postAtual != null) {
            PostForum recarregado = forumStorage.buscarPostPorId(postAtual.getId());
            if (recarregado != null) {
                postAtual = recarregado;
            }
        }

        configurarCaixaResponder();
        configurarAcoesPostOriginal();
        preencherPostOriginal();
        carregarRespostas();
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
            if (fotoUsuario != null && !fotoUsuario.isEmpty()) {
                iconeAvatarResponder.setImageURI(Uri.parse(fotoUsuario));
                iconeAvatarResponder.setImageTintList(null);
            } else {
                Bitmap avatar = AvatarUtils.criarAvatarComInicial(this, sessionManager.getNomeUsuario(), 120);
                iconeAvatarResponder.setImageBitmap(avatar);
                iconeAvatarResponder.setImageTintList(null);
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

            if (postAtual == null) {
                return;
            }

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

            if (postAtual == null) {
                return;
            }

            animarClique(botaoDislikePostOriginal);
            boolean sucesso = forumStorage.toggleDislikePost(postAtual.getId(), sessionManager.getEmailUsuario());

            if (sucesso) {
                recarregarPostAtual();
            }
        });

        boolean ehAutor = postAtual != null
                && sessionManager.estaLogado()
                && sessionManager.getEmailUsuario().equals(postAtual.getAutorEmail());

        botaoOpcoesPostOriginal.setVisibility(ehAutor ? View.VISIBLE : View.GONE);

        botaoOpcoesPostOriginal.setOnClickListener(v -> {
            if (postAtual == null) {
                return;
            }

            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenu().add("Editar");
            popupMenu.getMenu().add("Excluir");

            popupMenu.setOnMenuItemClickListener(item -> {
                String titulo = item.getTitle().toString();

                if (titulo.equals("Editar")) {
                    abrirDialogEditarPostOriginal();
                    return true;
                }

                if (titulo.equals("Excluir")) {
                    confirmarExclusaoPostOriginal();
                    return true;
                }

                return false;
            });

            popupMenu.show();
        });
    }

    private void atualizarEstadoVisualPostOriginal() {
        if (postAtual == null) {
            return;
        }

        if (!sessionManager.estaLogado()) {
            iconeLikePostOriginal.setAlpha(1f);
            iconeDislikePostOriginal.setAlpha(1f);
            textoLikes.setAlpha(1f);
            textoDislikes.setAlpha(1f);
            return;
        }

        boolean curtiu = postAtual.usuarioCurtiu(sessionManager.getEmailUsuario());
        boolean descurtiu = postAtual.usuarioDescurtiu(sessionManager.getEmailUsuario());

        iconeLikePostOriginal.setAlpha(curtiu ? 1f : 0.55f);
        textoLikes.setAlpha(curtiu ? 1f : 0.75f);

        iconeDislikePostOriginal.setAlpha(descurtiu ? 1f : 0.55f);
        textoDislikes.setAlpha(descurtiu ? 1f : 0.75f);
    }

    private void preencherPostOriginal() {
        if (postAtual == null) {
            return;
        }

        String fotoUri = postAtual.getAutorFotoUri();
        aplicarAvatar(fotoPerfilPostOriginal, postAtual.getAutorFotoUri(), postAtual.getAutorNome());

        boolean ehAutor = postAtual != null
                && sessionManager.estaLogado()
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
        List<RespostaForum> respostas = new ArrayList<>();

        if (postAtual != null && postAtual.getRespostas() != null) {
            respostas = postAtual.getRespostas();
        }

        RespostaForumAdapter adapter = new RespostaForumAdapter(
                this,
                postAtual != null ? postAtual.getId() : "",
                respostas,
                sessionManager.estaLogado()
        );

        listaRespostas.setAdapter(adapter);
    }

    private void abrirDialogNovaResposta() {
        if (!sessionManager.estaLogado()) {
            Toast.makeText(this, "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (postAtual == null) {
            Toast.makeText(this, "Post não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(this);
        input.setHint("Digite sua resposta");
        input.setMinLines(3);
        input.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
                .setTitle("Nova resposta")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Responder", (dialog, which) -> {
                    String texto = input.getText().toString().trim();

                    if (texto.isEmpty()) {
                        Toast.makeText(this, "Digite uma resposta", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    RespostaForum novaResposta = new RespostaForum(
                            sessionManager.getNomeUsuario(),
                            sessionManager.getEmailUsuario(),
                            sessionManager.getFotoUsuario(),
                            texto,
                            TimeUtils.agora(),
                            0,
                            true,
                            false
                    );

                    boolean sucesso = forumStorage.adicionarResposta(postAtual.getId(), novaResposta);

                    if (sucesso) {
                        recarregarPostAtual();
                        Toast.makeText(this, "Resposta publicada com sucesso", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro ao publicar resposta", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void abrirDialogEditarPostOriginal() {
        EditText input = new EditText(this);
        input.setText(postAtual.getMensagem());
        input.setMinLines(3);
        input.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
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
        new AlertDialog.Builder(this)
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
        if (postAtual != null) {
            PostForum atualizado = forumStorage.buscarPostPorId(postAtual.getId());
            if (atualizado != null) {
                postAtual = atualizado;
            }
        }

        configurarAcoesPostOriginal();
        preencherPostOriginal();
        carregarRespostas();
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
            Bitmap avatar = AvatarUtils.criarAvatarComInicial(this, nomeAutor, 120);
            imageView.setImageBitmap(avatar);
            imageView.setImageTintList(null);
        }
    }
}