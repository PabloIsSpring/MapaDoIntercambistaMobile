package com.example.mapadointercambista.activity.forum;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import com.example.mapadointercambista.util.AnimationUtils;
import com.example.mapadointercambista.util.AvatarUtils;
import com.example.mapadointercambista.util.ForumLimits;
import com.example.mapadointercambista.util.InputSecurityUtils;
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
    private TextView tituloPostOriginal;
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
    private TextView textoVazioRespostas;
    private RespostaForumAdapter adapter;
    private final List<RespostaForum> respostasExibidas = new ArrayList<>();
    private int ultimoHashPost = -1;

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

        if (postAtual == null) {
            Toast.makeText(this, "Publicação não encontrada.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        configurarLista();
        configurarCaixaResponder();
        configurarAcoesPostOriginal();
        aplicarMicrointeracoesRespostas();
        preencherPostOriginal();
        carregarRespostas();
        ultimoHashPost = calcularHashPostAtual();

        findViewById(R.id.botaoResponderAcao).setOnClickListener(v -> {
            AnimationUtils.playBounce(v);
            abrirTelaNovaResposta();
        });

        findViewById(R.id.cardResponder).setOnClickListener(v -> {
            AnimationUtils.playBounce(v);
            abrirTelaNovaResposta();
        });

        findViewById(R.id.botaoVoltarRespostas).setOnClickListener(v -> {
            AnimationUtils.playBounce(v);
            finish();
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_forum);
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarModoImersivo();

        sincronizarPostAtual();

        int hashAtual = calcularHashPostAtual();
        if (hashAtual != ultimoHashPost) {
            configurarCaixaResponder();
            configurarAcoesPostOriginal();
            preencherPostOriginal();
            carregarRespostas();
            ultimoHashPost = hashAtual;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            aplicarModoImersivo();
        }
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

    private int calcularHashPostAtual() {
        if (postAtual == null) return -1;

        int hash = postAtual.getId() != null ? postAtual.getId().hashCode() : 0;
        hash = 31 * hash + postAtual.getLikes();
        hash = 31 * hash + postAtual.getDislikes();
        hash = 31 * hash + postAtual.getQuantidadeRespostas();
        hash = 31 * hash + (postAtual.getTitulo() != null ? postAtual.getTitulo().hashCode() : 0);
        hash = 31 * hash + (postAtual.getMensagem() != null ? postAtual.getMensagem().hashCode() : 0);
        return hash;
    }

    private void inicializarViews() {
        fotoPerfilPostOriginal = findViewById(R.id.fotoPerfilPostOriginal);
        iconeAvatarResponder = findViewById(R.id.iconeAvatarResponder);

        nome = findViewById(R.id.nomeUsuarioPostOriginal);
        tempo = findViewById(R.id.tempoPostOriginal);
        tituloPostOriginal = findViewById(R.id.tituloPostOriginal);
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

        textoVazioRespostas = findViewById(R.id.textoVazioRespostas);
    }

    private void inicializarPost() {
        PostForum postRecebido = (PostForum) getIntent().getSerializableExtra("postSelecionado");

        if (postRecebido != null && postRecebido.getId() != null) {
            postAtual = forumStorage.buscarPostPorId(postRecebido.getId());
            if (postAtual == null) {
                postAtual = postRecebido;
            }
        }
    }

    private void configurarLista() {
        listaRespostas.setLayoutManager(new LinearLayoutManager(this));
        listaRespostas.setHasFixedSize(false);
        listaRespostas.setNestedScrollingEnabled(false);
        listaRespostas.setItemViewCacheSize(12);
        listaRespostas.setItemAnimator(null);

        adapter = new RespostaForumAdapter(
                this,
                postAtual.getId(),
                respostasExibidas,
                sessionManager.estaLogado()
        );

        listaRespostas.setAdapter(adapter);
    }

    private void sincronizarPostAtual() {
        if (postAtual != null && postAtual.getId() != null) {
            PostForum recarregado = forumStorage.buscarPostPorId(postAtual.getId());
            if (recarregado != null) {
                postAtual = recarregado;
            }
        }
    }

    private void abrirTelaNovaResposta() {
        if (!sessionManager.estaLogado()) {
            Toast.makeText(this, "Entre em uma conta para responder.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (postAtual == null || postAtual.getId() == null) {
            Toast.makeText(this, "Post não encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        android.content.Intent intent = new android.content.Intent(this, NovaRespostaActivity.class);
        intent.putExtra(NovaRespostaActivity.EXTRA_POST_ID, postAtual.getId());
        startActivity(intent);
    }

    private void configurarCaixaResponder() {
        if (sessionManager.estaLogado()) {
            textoResponder.setText("Escreva uma resposta para esta conversa");

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
            textoResponder.setText("Entre em sua conta para participar da conversa");
            iconeAvatarResponder.setImageResource(R.drawable.ic_user);
            iconeAvatarResponder.setImageTintList(
                    ContextCompat.getColorStateList(this, android.R.color.white)
            );
        }
    }

    private void configurarAcoesPostOriginal() {
        botaoLikePostOriginal.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em uma conta para curtir publicações.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (postAtual == null || postAtual.getId() == null || !v.isEnabled()) return;

            v.setEnabled(false);
            AnimationUtils.playBounce(botaoLikePostOriginal);

            boolean sucesso = forumStorage.toggleLikePost(postAtual.getId(), sessionManager.getEmailUsuario());

            if (sucesso) {
                recarregarPostAtual();
            }

            v.postDelayed(() -> v.setEnabled(true), 250);
        });

        botaoDislikePostOriginal.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em uma conta para interagir com publicações.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (postAtual == null || postAtual.getId() == null || !v.isEnabled()) return;

            v.setEnabled(false);
            AnimationUtils.playBounce(botaoDislikePostOriginal);

            boolean sucesso = forumStorage.toggleDislikePost(postAtual.getId(), sessionManager.getEmailUsuario());

            if (sucesso) {
                recarregarPostAtual();
            }

            v.postDelayed(() -> v.setEnabled(true), 250);
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

        nome.setText(textoSeguro(postAtual.getAutorNome(), "Usuário"));
        tempo.setText("· " + postAtual.getTempoPostagem());
        tituloPostOriginal.setText(textoSeguro(postAtual.getTitulo(), "Sem título"));
        mensagem.setText(textoSeguro(postAtual.getMensagem(), ""));
        textoLikes.setText(String.valueOf(postAtual.getLikes()));
        textoDislikes.setText(String.valueOf(postAtual.getDislikes()));
        textoRespostas.setText(postAtual.getQuantidadeRespostas() + " respostas");

        atualizarEstadoVisualPostOriginal();
    }

    private int dpToPxInt(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void aplicarMicrointeracoesRespostas() {
        View botaoVoltar = findViewById(R.id.botaoVoltarRespostas);
        View cardResponder = findViewById(R.id.cardResponder);
        View botaoResponderAcao = findViewById(R.id.botaoResponderAcao);

        AnimationUtils.applyPressAnimation(botaoVoltar);
        AnimationUtils.applyPressAnimation(botaoLikePostOriginal);
        AnimationUtils.applyPressAnimation(botaoDislikePostOriginal);
        AnimationUtils.applyPressAnimation(cardResponder);
        AnimationUtils.applyPressAnimation(botaoResponderAcao);

        if (botaoOpcoesPostOriginal != null) {
            AnimationUtils.applyPressAnimation(botaoOpcoesPostOriginal);
        }
    }

    private void carregarRespostas() {
        if (adapter == null) return;

        if (postAtual != null && postAtual.getRespostas() != null && !postAtual.getRespostas().isEmpty()) {
            textoVazioRespostas.setVisibility(View.GONE);
            listaRespostas.setVisibility(View.VISIBLE);
            adapter.atualizarDadosPreservandoVisibilidade(postAtual.getRespostas());
        } else {
            textoVazioRespostas.setVisibility(View.VISIBLE);
            listaRespostas.setVisibility(View.GONE);
            adapter.atualizarDados(new ArrayList<>());
        }
    }

    private void abrirDialogEditarPostOriginal() {
        if (postAtual == null) return;

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dpToPxInt(16), dpToPxInt(12), dpToPxInt(16), dpToPxInt(8));

        EditText inputTitulo = new EditText(this);
        inputTitulo.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        inputTitulo.setSingleLine(false);
        inputTitulo.setHint("Título");
        inputTitulo.setText(textoSeguro(postAtual.getTitulo(), ""));
        inputTitulo.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(ForumLimits.MAX_TITULO_POST)
        });

        EditText inputMensagem = new EditText(this);
        inputMensagem.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        inputMensagem.setHint("Mensagem");
        inputMensagem.setText(textoSeguro(postAtual.getMensagem(), ""));
        inputMensagem.setMinLines(4);
        inputMensagem.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(ForumLimits.MAX_TEXTO_POST)
        });

        container.addView(inputTitulo);
        container.addView(inputMensagem);

        new AlertDialog.Builder(this)
                .setTitle("Editar post")
                .setView(container)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novoTitulo = InputSecurityUtils.sanitizeUserText(inputTitulo.getText().toString());
                    String novaMensagem = InputSecurityUtils.sanitizeUserText(inputMensagem.getText().toString());

                    if (InputSecurityUtils.isNullOrBlank(novoTitulo)) {
                        Toast.makeText(this, "Digite um título.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (InputSecurityUtils.isNullOrBlank(novaMensagem)) {
                        Toast.makeText(this, "Digite uma mensagem.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (novoTitulo.length() < 3) {
                        Toast.makeText(this, "Digite um título mais completo.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (novaMensagem.length() < 5) {
                        Toast.makeText(this, "Digite uma mensagem mais completa.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (InputSecurityUtils.exceedsMaxLength(novoTitulo, ForumLimits.MAX_TITULO_POST)) {
                        Toast.makeText(this, "Título muito longo.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (InputSecurityUtils.exceedsMaxLength(novaMensagem, ForumLimits.MAX_TEXTO_POST)) {
                        Toast.makeText(this, "Mensagem muito longa.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (InputSecurityUtils.containsSuspiciousPattern(novoTitulo)
                            || InputSecurityUtils.containsSuspiciousPattern(novaMensagem)) {
                        Toast.makeText(this, "Conteúdo inválido detectado.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean sucesso = forumStorage.editarPost(
                            postAtual.getId(),
                            novoTitulo,
                            novaMensagem,
                            postAtual.getImagemUri()
                    );

                    if (sucesso) {
                        recarregarPostAtual();
                        Toast.makeText(this, "Post editado com sucesso.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Não foi possível editar o post.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void confirmarExclusaoPostOriginal() {
        if (postAtual == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Excluir post")
                .setMessage("Tem certeza que deseja excluir esta publicação?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    boolean sucesso = forumStorage.excluirPost(postAtual.getId());

                    if (sucesso) {
                        Toast.makeText(this, "Post excluído com sucesso.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Não foi possível excluir o post.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void recarregarPostAtual() {
        sincronizarPostAtual();
        configurarAcoesPostOriginal();
        preencherPostOriginal();
        carregarRespostas();
        ultimoHashPost = calcularHashPostAtual();
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

    private String textoSeguro(String valor, String fallback) {
        String texto = InputSecurityUtils.sanitizeUserText(valor);
        return texto.isEmpty() ? fallback : texto;
    }
}