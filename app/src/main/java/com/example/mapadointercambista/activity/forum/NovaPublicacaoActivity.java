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
import com.example.mapadointercambista.model.forum.PostForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.util.TimeUtils;
import com.google.android.material.button.MaterialButton;

public class NovaPublicacaoActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ForumStorage forumStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nova_publicacao);

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

        ImageView botaoVoltar = findViewById(R.id.botaoVoltarNovaPublicacao);
        EditText inputMensagem = findViewById(R.id.inputMensagemNovaPublicacao);
        MaterialButton botaoPublicar = findViewById(R.id.botaoPublicarNovaPublicacao);

        botaoVoltar.setOnClickListener(v -> finish());

        botaoPublicar.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em sua conta para publicar", Toast.LENGTH_SHORT).show();
                return;
            }

            String mensagem = inputMensagem.getText().toString().trim();

            if (mensagem.isEmpty()) {
                inputMensagem.setError("Digite uma publicação");
                inputMensagem.requestFocus();
                return;
            }

            PostForum novoPost = new PostForum(
                    sessionManager.getNomeUsuario(),
                    sessionManager.getEmailUsuario(),
                    sessionManager.getFotoUsuario(),
                    mensagem,
                    TimeUtils.agora()
            );

            forumStorage.adicionarPost(novoPost);
            Toast.makeText(this, "Post publicado com sucesso", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }
}