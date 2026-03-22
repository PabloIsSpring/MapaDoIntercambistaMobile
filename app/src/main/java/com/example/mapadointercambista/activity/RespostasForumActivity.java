package com.example.mapadointercambista.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.adapter.RespostaForumAdapter;
import com.example.mapadointercambista.model.ForumStorage;
import com.example.mapadointercambista.model.NavigationHelper;
import com.example.mapadointercambista.model.PostForum;
import com.example.mapadointercambista.model.RespostaForum;
import com.example.mapadointercambista.model.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class RespostasForumActivity extends AppCompatActivity {

    RecyclerView listaRespostas;

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

        SessionManager sessionManager = new SessionManager(this);

        ImageView fotoPerfil = findViewById(R.id.fotoPerfilPostOriginal);
        TextView nome = findViewById(R.id.nomeUsuarioPostOriginal);
        TextView tempo = findViewById(R.id.tempoPostOriginal);
        TextView mensagem = findViewById(R.id.mensagemPostOriginal);

        TextView textoLikes = findViewById(R.id.textoLikesPostOriginal);
        TextView textoDislikes = findViewById(R.id.textoDislikesPostOriginal);
        TextView textoRespostas = findViewById(R.id.textoQuantidadeRespostasPostOriginal);


        PostForum post = (PostForum) getIntent().getSerializableExtra("postSelecionado");

        if (post != null) {
            fotoPerfil.setImageResource(post.getFotoPerfil());
            nome.setText(post.getUsuario());
            tempo.setText("· " + post.getTempoPostagem());
            mensagem.setText(post.getMensagem());

            textoLikes.setText(String.valueOf(post.getLikes()));
            textoDislikes.setText(String.valueOf(post.getDislikes()));
            textoRespostas.setText(post.getQuantidadeRespostas() + " respostas");
        }


        TextView textoResponder = findViewById(R.id.textoResponderBloqueado);

        if (sessionManager.estaLogado()) {
            textoResponder.setText("Postar sua resposta");
        } else {
            textoResponder.setText("Entre em sua conta para responder");
        }

        findViewById(R.id.botaoResponderAcao).setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tela de responder será implementada depois", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.cardResponder).setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Você não entrou em sua conta", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tela de responder será implementada depois", Toast.LENGTH_SHORT).show();
            }
        });

        listaRespostas = findViewById(R.id.listaRespostas);
        listaRespostas.setLayoutManager(new LinearLayoutManager(this));

        ForumStorage forumStorage = new ForumStorage(this);
        List<RespostaForum> respostas = forumStorage.carregarRespostas();

        if (respostas.isEmpty()) {
            respostas = new ArrayList<>();

            respostas.add(new RespostaForum(
                    "Carlos",
                    "Também achei muito bom.",
                    R.drawable.logo,
                    "há 1h",
                    8,
                    1,
                    0,
                    true,
                    true
            ));

            respostas.add(new RespostaForum(
                    "Amanda",
                    "Concordo com você!",
                    R.drawable.logo,
                    "há 50min",
                    4,
                    0,
                    1,
                    true,
                    true
            ));

            respostas.add(new RespostaForum(
                    "Marcos",
                    "Eu também testei e curti bastante.",
                    R.drawable.logo,
                    "há 35min",
                    6,
                    1,
                    2,
                    true,
                    false
            ));

            forumStorage.salvarRespostas(respostas);
        }

        RespostaForumAdapter adapter =
                new RespostaForumAdapter(respostas, sessionManager.estaLogado());

        listaRespostas.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_forum);
    }
}