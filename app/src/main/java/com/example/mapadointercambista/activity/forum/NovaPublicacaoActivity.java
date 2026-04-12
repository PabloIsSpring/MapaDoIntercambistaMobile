package com.example.mapadointercambista.activity.forum;

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
import androidx.appcompat.app.AppCompatActivity;

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

    private static final int MAX_TITULO = 80;
    private static final int MAX_MENSAGEM = 500;

    private SessionManager sessionManager;
    private ForumStorage forumStorage;

    private EditText inputTitulo;
    private EditText inputMensagem;
    private TextView textoContadorTitulo;
    private TextView textoContadorMensagem;
    private MaterialButton botaoPublicar;

    private boolean publicando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nova_publicacao);

        aplicarModoImersivo();

        sessionManager = new SessionManager(this);
        forumStorage = new ForumStorage(this);

        initViews();
        configurarLimites();
        configurarContadores();
        configurarEventos();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_forum);
    }

    private void initViews() {
        ImageView botaoVoltar = findViewById(R.id.botaoVoltarNovaPublicacao);
        inputTitulo = findViewById(R.id.inputTituloNovaPublicacao);
        inputMensagem = findViewById(R.id.inputMensagemNovaPublicacao);
        textoContadorTitulo = findViewById(R.id.textoContadorTituloNovaPublicacao);
        textoContadorMensagem = findViewById(R.id.textoContadorMensagemNovaPublicacao);
        botaoPublicar = findViewById(R.id.botaoPublicarNovaPublicacao);

        botaoVoltar.setOnClickListener(v -> {
            finish();
            TransitionHelper.slideBack(this);
        });
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
        botaoPublicar.setOnClickListener(v -> publicarPost());
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

        if (InputSecurityUtils.isNullOrBlank(titulo)) {
            inputTitulo.setError("Digite um título.");
            inputTitulo.requestFocus();
            return;
        }

        if (InputSecurityUtils.isNullOrBlank(mensagem)) {
            inputMensagem.setError("Digite uma publicação.");
            inputMensagem.requestFocus();
            return;
        }

        if (InputSecurityUtils.exceedsMaxLength(titulo, MAX_TITULO)) {
            inputTitulo.setError("Título muito longo.");
            inputTitulo.requestFocus();
            return;
        }

        if (InputSecurityUtils.exceedsMaxLength(mensagem, MAX_MENSAGEM)) {
            inputMensagem.setError("Texto muito longo.");
            inputMensagem.requestFocus();
            return;
        }

        if (InputSecurityUtils.containsSuspiciousPattern(titulo)
                || InputSecurityUtils.containsSuspiciousPattern(mensagem)) {
            Toast.makeText(this, "Conteúdo inválido detectado.", Toast.LENGTH_SHORT).show();
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

    private void setPublicando(boolean publicando) {
        this.publicando = publicando;
        botaoPublicar.setEnabled(!publicando);
        botaoPublicar.setText(publicando ? "Publicando..." : "Publicar");
        inputTitulo.setEnabled(!publicando);
        inputMensagem.setEnabled(!publicando);
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