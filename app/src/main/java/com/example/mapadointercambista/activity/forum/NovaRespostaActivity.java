package com.example.mapadointercambista.activity.forum;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.model.forum.ForumStorage;
import com.example.mapadointercambista.model.forum.RespostaForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.util.TimeUtils;
import com.google.android.material.button.MaterialButton;

public class NovaRespostaActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "post_id";

    private SessionManager sessionManager;
    private ForumStorage forumStorage;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nova_resposta);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        sessionManager = new SessionManager(this);
        forumStorage = new ForumStorage(this);
        postId = getIntent().getStringExtra(EXTRA_POST_ID);

        ImageView botaoVoltar = findViewById(R.id.botaoVoltarNovaResposta);
        EditText inputMensagem = findViewById(R.id.inputMensagemNovaResposta);
        MaterialButton botaoResponder = findViewById(R.id.botaoPublicarNovaResposta);

        botaoVoltar.setOnClickListener(v -> finish());

        botaoResponder.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em sua conta para responder", Toast.LENGTH_SHORT).show();
                return;
            }

            String mensagem = inputMensagem.getText().toString().trim();

            if (mensagem.isEmpty()) {
                inputMensagem.setError("Digite uma resposta");
                inputMensagem.requestFocus();
                return;
            }

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
                Toast.makeText(this, "Resposta publicada com sucesso", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Erro ao publicar resposta", Toast.LENGTH_SHORT).show();
            }
        });
    }
}