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
import com.example.mapadointercambista.model.forum.RespostaForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.util.InputSecurityUtils;
import com.example.mapadointercambista.util.TimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class NovaRespostaActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "post_id";
    private static final int MAX_RESPOSTA = 300;

    private SessionManager sessionManager;
    private ForumStorage forumStorage;
    private String postId;

    private EditText inputMensagem;
    private TextView textoContadorMensagem;
    private MaterialButton botaoResponder;

    private boolean respondendo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nova_resposta);

        aplicarModoImersivo();

        sessionManager = new SessionManager(this);
        forumStorage = new ForumStorage(this);
        postId = getIntent().getStringExtra(EXTRA_POST_ID);

        if (InputSecurityUtils.isNullOrBlank(postId)) {
            Toast.makeText(this, "Publicação inválida.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        configurarLimites();
        configurarContadores();
        configurarEventos();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_forum);
    }

    private void initViews() {
        ImageView botaoVoltar = findViewById(R.id.botaoVoltarNovaResposta);
        inputMensagem = findViewById(R.id.inputMensagemNovaResposta);
        textoContadorMensagem = findViewById(R.id.textoContadorMensagemNovaResposta);
        botaoResponder = findViewById(R.id.botaoPublicarNovaResposta);

        botaoVoltar.setOnClickListener(v -> finish());
    }

    private void configurarLimites() {
        inputMensagem.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_RESPOSTA)});
    }

    private void configurarContadores() {
        textoContadorMensagem.setText("0/" + MAX_RESPOSTA);

        inputMensagem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoContadorMensagem.setText(s.length() + "/" + MAX_RESPOSTA);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void configurarEventos() {
        botaoResponder.setOnClickListener(v -> publicarResposta());
    }

    private void publicarResposta() {
        if (respondendo) {
            return;
        }

        if (!sessionManager.estaLogado()) {
            Toast.makeText(this, "Entre em sua conta para responder.", Toast.LENGTH_SHORT).show();
            return;
        }

        String mensagem = InputSecurityUtils.sanitizeUserText(inputMensagem.getText().toString());

        if (InputSecurityUtils.isNullOrBlank(mensagem)) {
            inputMensagem.setError("Digite uma resposta.");
            inputMensagem.requestFocus();
            return;
        }

        if (InputSecurityUtils.exceedsMaxLength(mensagem, MAX_RESPOSTA)) {
            inputMensagem.setError("Resposta muito longa.");
            inputMensagem.requestFocus();
            return;
        }

        if (InputSecurityUtils.containsSuspiciousPattern(mensagem)) {
            Toast.makeText(this, "Conteúdo inválido detectado.", Toast.LENGTH_SHORT).show();
            return;
        }

        setRespondendo(true);

        try {
            RespostaForum novaResposta = new RespostaForum(
                    sessionManager.getNomeUsuario(),
                    sessionManager.getEmailUsuario(),
                    sessionManager.getFotoUsuario(),
                    mensagem,
                    TimeUtils.agora(),
                    0,
                    true,
                    false
            );

            boolean sucesso = forumStorage.adicionarResposta(postId, novaResposta);

            if (sucesso) {
                Toast.makeText(this, "Resposta publicada com sucesso.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Não foi possível publicar a resposta.", Toast.LENGTH_SHORT).show();
                setRespondendo(false);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao responder no momento.", Toast.LENGTH_SHORT).show();
            setRespondendo(false);
        }
    }

    private void setRespondendo(boolean respondendo) {
        this.respondendo = respondendo;
        botaoResponder.setEnabled(!respondendo);
        botaoResponder.setText(respondendo ? "Respondendo..." : "Responder");
        inputMensagem.setEnabled(!respondendo);
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
}