package com.example.mapadointercambista.activity.forum;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mapadointercambista.R;
import com.example.mapadointercambista.model.forum.ForumStorage;
import com.example.mapadointercambista.model.forum.PostForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.util.InputSecurityUtils;
import com.example.mapadointercambista.util.TimeUtils;
import com.example.mapadointercambista.util.TransitionHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class NovaPublicacaoActivity extends AppCompatActivity {

    public static final String EXTRA_MODO_EDICAO = "modo_edicao";
    public static final String EXTRA_POST_ID = "post_id";

    private static final int MAX_TITULO = 80;
    private static final int MAX_MENSAGEM = 500;
    private static final int MIN_TITULO = 3;
    private static final int MIN_MENSAGEM = 5;

    private SessionManager sessionManager;
    private ForumStorage forumStorage;

    private EditText inputTitulo;
    private EditText inputMensagem;
    private TextView textoContadorTitulo;
    private TextView textoContadorMensagem;
    private TextView textoTituloTela;
    private MaterialButton botaoPublicar;
    private ImageView imagemPreviewPost;
    private View containerImagemPreview;
    private MaterialButton botaoAdicionarImagem;
    private MaterialButton botaoRemoverImagem;

    private boolean publicando = false;
    private String imagemSelecionadaUri = "";

    private boolean modoEdicao = false;
    private String postIdEdicao = null;

    private ActivityResultLauncher<Intent> launcherImagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nova_publicacao);

        aplicarModoImersivo();

        sessionManager = new SessionManager(this);
        forumStorage = new ForumStorage(this);

        lerExtras();
        configurarLauncherImagem();
        initViews();
        configurarLimites();
        configurarContadores();
        configurarEventos();
        preencherModoEdicaoSeNecessario();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_forum);
    }

    private void lerExtras() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        modoEdicao = intent.getBooleanExtra(EXTRA_MODO_EDICAO, false);
        postIdEdicao = intent.getStringExtra(EXTRA_POST_ID);
    }

    private void configurarLauncherImagem() {
        launcherImagem = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        return;
                    }

                    Uri uriSelecionada = result.getData().getData();
                    if (uriSelecionada == null) {
                        Toast.makeText(this, "Não foi possível selecionar a imagem.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        final int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(uriSelecionada, flags);
                    } catch (SecurityException ignored) {
                    }

                    imagemSelecionadaUri = uriSelecionada.toString();
                    atualizarPreviewImagem();
                }
        );
    }

    private void initViews() {
        ImageView botaoVoltar = findViewById(R.id.botaoVoltarNovaPublicacao);
        inputTitulo = findViewById(R.id.inputTituloNovaPublicacao);
        inputMensagem = findViewById(R.id.inputMensagemNovaPublicacao);
        textoContadorTitulo = findViewById(R.id.textoContadorTituloNovaPublicacao);
        textoContadorMensagem = findViewById(R.id.textoContadorMensagemNovaPublicacao);
        botaoPublicar = findViewById(R.id.botaoPublicarNovaPublicacao);
        textoTituloTela = findViewById(R.id.textoTituloTelaNovaPublicacao);

        imagemPreviewPost = findViewById(R.id.imagemPreviewNovaPublicacao);
        containerImagemPreview = findViewById(R.id.containerImagemPreviewNovaPublicacao);
        botaoAdicionarImagem = findViewById(R.id.botaoAdicionarImagemNovaPublicacao);
        botaoRemoverImagem = findViewById(R.id.botaoRemoverImagemNovaPublicacao);

        botaoVoltar.setOnClickListener(v -> {
            finish();
            TransitionHelper.slideBack(this);
        });

        textoTituloTela.setText(modoEdicao ? "Editar publicação" : "Nova publicação");
        botaoPublicar.setText(modoEdicao ? "Salvar alterações" : "Publicar");

        atualizarPreviewImagem();
    }

    private void configurarLimites() {
        inputTitulo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_TITULO)});
        inputMensagem.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_MENSAGEM)});
    }

    private void configurarContadores() {
        textoContadorTitulo.setText("0/" + MAX_TITULO);
        textoContadorMensagem.setText("0/" + MAX_MENSAGEM);

        inputTitulo.addTextChangedListener(new SimpleCounterWatcher(textoContadorTitulo, MAX_TITULO));
        inputMensagem.addTextChangedListener(new SimpleCounterWatcher(textoContadorMensagem, MAX_MENSAGEM));
    }

    private void configurarEventos() {
        botaoPublicar.setOnClickListener(v -> {
            if (modoEdicao) {
                salvarEdicaoPost();
            } else {
                publicarPost();
            }
        });

        botaoAdicionarImagem.setOnClickListener(v -> selecionarImagem());
        botaoRemoverImagem.setOnClickListener(v -> {
            imagemSelecionadaUri = "";
            atualizarPreviewImagem();
        });
    }

    private void preencherModoEdicaoSeNecessario() {
        if (!modoEdicao) {
            return;
        }

        if (InputSecurityUtils.isNullOrBlank(postIdEdicao)) {
            Toast.makeText(this, "Post inválido para edição.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        PostForum post = forumStorage.buscarPostPorId(postIdEdicao);
        if (post == null) {
            Toast.makeText(this, "Post não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String emailLogado = sessionManager.getEmailUsuario();
        if (emailLogado == null || !emailLogado.equalsIgnoreCase(post.getAutorEmail())) {
            Toast.makeText(this, "Você não pode editar este post.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inputTitulo.setText(post.getTitulo());
        inputMensagem.setText(post.getMensagem());
        imagemSelecionadaUri = post.getImagemUri() != null ? post.getImagemUri() : "";
        atualizarPreviewImagem();
    }

    private void selecionarImagem() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        launcherImagem.launch(intent);
    }

    private void atualizarPreviewImagem() {
        boolean temImagem = imagemSelecionadaUri != null && !imagemSelecionadaUri.trim().isEmpty();

        containerImagemPreview.setVisibility(temImagem ? View.VISIBLE : View.GONE);
        botaoRemoverImagem.setVisibility(temImagem ? View.VISIBLE : View.GONE);

        if (temImagem) {
            Glide.with(this)
                    .load(Uri.parse(imagemSelecionadaUri))
                    .centerCrop()
                    .into(imagemPreviewPost);
        } else {
            Glide.with(this).clear(imagemPreviewPost);
        }
    }

    private void publicarPost() {
        if (publicando) {
            return;
        }

        if (!sessionManager.estaLogado()) {
            Toast.makeText(this, "Entre em sua conta para publicar.", Toast.LENGTH_SHORT).show();
            return;
        }

        String titulo = InputSecurityUtils.sanitizeUserText(inputTitulo.getText().toString());
        String mensagem = InputSecurityUtils.sanitizeUserText(inputMensagem.getText().toString());

        if (!validarCampos(titulo, mensagem)) {
            return;
        }

        setPublicando(true);

        try {
            PostForum novoPost = new PostForum(
                    sessionManager.getNomeUsuario(),
                    sessionManager.getEmailUsuario(),
                    sessionManager.getFotoUsuario(),
                    titulo,
                    mensagem,
                    imagemSelecionadaUri,
                    TimeUtils.agora()
            );

            boolean sucesso = forumStorage.adicionarPost(novoPost);

            if (sucesso) {
                Toast.makeText(this, "Post publicado com sucesso.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Não foi possível publicar agora.", Toast.LENGTH_SHORT).show();
                setPublicando(false);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível publicar agora.", Toast.LENGTH_SHORT).show();
            setPublicando(false);
        }
    }

    private void salvarEdicaoPost() {
        if (publicando) {
            return;
        }

        if (!sessionManager.estaLogado()) {
            Toast.makeText(this, "Entre em sua conta para editar.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (InputSecurityUtils.isNullOrBlank(postIdEdicao)) {
            Toast.makeText(this, "Post inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        String titulo = InputSecurityUtils.sanitizeUserText(inputTitulo.getText().toString());
        String mensagem = InputSecurityUtils.sanitizeUserText(inputMensagem.getText().toString());

        if (!validarCampos(titulo, mensagem)) {
            return;
        }

        setPublicando(true);

        try {
            boolean sucesso = forumStorage.editarPost(
                    postIdEdicao,
                    titulo,
                    mensagem,
                    imagemSelecionadaUri
            );

            if (sucesso) {
                Toast.makeText(this, "Post editado com sucesso.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Não foi possível salvar as alterações.", Toast.LENGTH_SHORT).show();
                setPublicando(false);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao editar o post.", Toast.LENGTH_SHORT).show();
            setPublicando(false);
        }
    }

    private boolean validarCampos(String titulo, String mensagem) {
        if (InputSecurityUtils.isNullOrBlank(titulo)) {
            inputTitulo.setError("Digite um título.");
            inputTitulo.requestFocus();
            return false;
        }

        if (InputSecurityUtils.isNullOrBlank(mensagem)) {
            inputMensagem.setError("Digite uma publicação.");
            inputMensagem.requestFocus();
            return false;
        }

        if (titulo.length() < MIN_TITULO) {
            inputTitulo.setError("Digite um título mais completo.");
            inputTitulo.requestFocus();
            return false;
        }

        if (mensagem.length() < MIN_MENSAGEM) {
            inputMensagem.setError("Digite uma publicação mais completa.");
            inputMensagem.requestFocus();
            return false;
        }

        if (InputSecurityUtils.exceedsMaxLength(titulo, MAX_TITULO)) {
            inputTitulo.setError("Título muito longo.");
            inputTitulo.requestFocus();
            return false;
        }

        if (InputSecurityUtils.exceedsMaxLength(mensagem, MAX_MENSAGEM)) {
            inputMensagem.setError("Texto muito longo.");
            inputMensagem.requestFocus();
            return false;
        }

        if (InputSecurityUtils.containsSuspiciousPattern(titulo)
                || InputSecurityUtils.containsSuspiciousPattern(mensagem)) {
            Toast.makeText(this, "Conteúdo inválido detectado.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void setPublicando(boolean publicando) {
        this.publicando = publicando;
        botaoPublicar.setEnabled(!publicando);
        botaoPublicar.setText(publicando
                ? (modoEdicao ? "Salvando..." : "Publicando...")
                : (modoEdicao ? "Salvar alterações" : "Publicar"));
        inputTitulo.setEnabled(!publicando);
        inputMensagem.setEnabled(!publicando);
        botaoAdicionarImagem.setEnabled(!publicando);
        botaoRemoverImagem.setEnabled(!publicando);
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

    @Override
    protected void onResume() {
        super.onResume();
        aplicarModoImersivo();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            aplicarModoImersivo();
        }
    }

    private static class SimpleCounterWatcher implements TextWatcher {

        private final TextView textView;
        private final int limite;

        SimpleCounterWatcher(TextView textView, int limite) {
            this.textView = textView;
            this.limite = limite;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            textView.setText(s.length() + "/" + limite);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}