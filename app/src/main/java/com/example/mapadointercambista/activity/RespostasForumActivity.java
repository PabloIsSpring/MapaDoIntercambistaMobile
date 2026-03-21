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
import com.example.mapadointercambista.model.PostForum;
import com.example.mapadointercambista.model.RespostaForum;
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

        PostForum post = (PostForum) getIntent().getSerializableExtra("postSelecionado");

        ImageView fotoPerfil = findViewById(R.id.fotoPerfilPostOriginal);
        TextView nome = findViewById(R.id.nomeUsuarioPostOriginal);
        TextView tempo = findViewById(R.id.tempoPostOriginal);
        TextView mensagem = findViewById(R.id.mensagemPostOriginal);

        if (post != null) {
            fotoPerfil.setImageResource(post.getFotoPerfil());
            nome.setText(post.getUsuario());
            tempo.setText(post.getTempoPostagem());
            mensagem.setText(post.getMensagem());
        }

        listaRespostas = findViewById(R.id.listaRespostas);
        listaRespostas.setLayoutManager(new LinearLayoutManager(this));

        List<RespostaForum> respostas = new ArrayList<>();

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

        respostas.add(new RespostaForum(
                "Fernanda",
                "Quero testar depois.",
                R.drawable.logo,
                "há 25min",
                3,
                0,
                1,
                true,
                false
        ));

        respostas.add(new RespostaForum(
                "João",
                "A ideia do app está muito boa.",
                R.drawable.logo,
                "há 10min",
                5,
                0,
                0,
                true,
                false
        ));
        RespostaForumAdapter adapter = new RespostaForumAdapter(respostas);
        listaRespostas.setAdapter(adapter);

        findViewById(R.id.cardResponder).setOnClickListener(v ->
                Toast.makeText(this, "Você não entrou em sua conta", Toast.LENGTH_SHORT).show()
        );

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setSelectedItemId(R.id.nav_forum);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                finish();
                return true;
            }

            if (itemId == R.id.nav_forum) {
                return true;
            }

            if (itemId == R.id.nav_mundo) {
                finish();
                return true;
            }

            if (itemId == R.id.nav_perfil) {
                Toast.makeText(this, "Tela de login em desenvolvimento", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });
    }
}