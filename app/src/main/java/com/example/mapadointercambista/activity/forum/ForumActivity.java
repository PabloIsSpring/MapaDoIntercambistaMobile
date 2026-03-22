package com.example.mapadointercambista.activity.forum;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.adapter.forum.PostForumAdapter;
import com.example.mapadointercambista.model.forum.ForumRepository;
import com.example.mapadointercambista.model.forum.ForumStorage;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.model.forum.PostForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.util.TimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForumActivity extends AppCompatActivity {

    private RecyclerView listaTodosForuns;
    private ForumStorage forumStorage;
    private SessionManager sessionManager;
    private PostForumAdapter adapter;

    private List<PostForum> postsOriginais = new ArrayList<>();
    private List<PostForum> postsExibidos = new ArrayList<>();

    private String textoBusca = "";
    private String criterioOrdenacao = "recentes";
    private static final String PREF_FORUM_UI = "forum_ui";
    private static final String KEY_ORDENACAO = "ordenacao";
    private TextView textoResultadosForum;
    private TextView textoVazioForum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum);

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

        listaTodosForuns = findViewById(R.id.listaTodosForuns);
        listaTodosForuns.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PostForumAdapter(this, postsExibidos, true);
        listaTodosForuns.setAdapter(adapter);

        textoResultadosForum = findViewById(R.id.textoResultadosForum);
        textoVazioForum = findViewById(R.id.textoVazioForum);

        EditText barraPesquisa = findViewById(R.id.barraPesquisaForum);
        ImageView iconeFiltro = findViewById(R.id.iconeFiltroForum);

        barraPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBusca = s.toString().trim();
                aplicarBuscaEOrdenacao();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        criterioOrdenacao = getSharedPreferences(PREF_FORUM_UI, MODE_PRIVATE)
                .getString(KEY_ORDENACAO, "recentes");

        iconeFiltro.setOnClickListener(v -> abrirMenuOrdenacao(v));

        carregarPosts();

        findViewById(R.id.botaoNovoForum).setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em sua conta para publicar", Toast.LENGTH_SHORT).show();
                return;
            }

            startActivity(new android.content.Intent(this, NovaPublicacaoActivity.class));
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_forum);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarPosts();
    }

    private void carregarPosts() {
        postsOriginais = forumStorage.carregarPosts();

        if (postsOriginais.isEmpty()) {
            postsOriginais = ForumRepository.criarPostsIniciais();
            forumStorage.salvarPosts(postsOriginais);
        }

        aplicarBuscaEOrdenacao();
    }

    private void aplicarBuscaEOrdenacao() {
        List<PostForum> filtrados = new ArrayList<>();

        for (PostForum post : postsOriginais) {
            String autor = post.getAutorNome() != null ? post.getAutorNome().toLowerCase() : "";
            String mensagem = post.getMensagem() != null ? post.getMensagem().toLowerCase() : "";
            String busca = textoBusca.toLowerCase();

            if (busca.isEmpty() || autor.contains(busca) || mensagem.contains(busca)) {
                filtrados.add(post);
            }
        }

        ordenarLista(filtrados);

        postsExibidos.clear();
        postsExibidos.addAll(filtrados);
        adapter.notifyDataSetChanged();

        int quantidade = filtrados.size();
        if (quantidade == 1) {
            textoResultadosForum.setText("1 resultado");
        } else {
            textoResultadosForum.setText(quantidade + " resultados");
        }

        textoVazioForum.setVisibility(quantidade == 0 ? View.VISIBLE : View.GONE);
        listaTodosForuns.setVisibility(quantidade == 0 ? View.GONE : View.VISIBLE);
    }

    private void ordenarLista(List<PostForum> lista) {
        switch (criterioOrdenacao) {
            case "curtidos":
                Collections.sort(lista, (a, b) -> Integer.compare(b.getLikes(), a.getLikes()));
                break;

            case "respondidos":
                Collections.sort(lista, (a, b) -> Integer.compare(b.getQuantidadeRespostas(), a.getQuantidadeRespostas()));
                break;

            case "recentes":
            default:
                Collections.sort(lista, (a, b) -> Long.compare(b.getCriadoEm(), a.getCriadoEm()));
                break;
        }
    }

    private void abrirMenuOrdenacao(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add("Mais recentes");
        popupMenu.getMenu().add("Mais curtidos");
        popupMenu.getMenu().add("Mais respondidos");

        popupMenu.setOnMenuItemClickListener(item -> {
            String titulo = item.getTitle().toString();

            if (titulo.equals("Mais recentes")) {
                criterioOrdenacao = "recentes";
            } else if (titulo.equals("Mais curtidos")) {
                criterioOrdenacao = "curtidos";
            } else if (titulo.equals("Mais respondidos")) {
                criterioOrdenacao = "respondidos";
            }
            getSharedPreferences(PREF_FORUM_UI, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_ORDENACAO, criterioOrdenacao)
                    .apply();

            aplicarBuscaEOrdenacao();
            return true;
        });

        popupMenu.show();
    }

    private void abrirDialogNovoPost() {
        if (!sessionManager.estaLogado()) {
            Toast.makeText(this, "Entre em sua conta para publicar", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = new EditText(this);
        input.setHint("Digite sua publicação");
        input.setMinLines(3);
        input.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
                .setTitle("Novo post")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Publicar", (dialog, which) -> {
                    String mensagem = input.getText().toString().trim();

                    if (mensagem.isEmpty()) {
                        Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show();
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
                    carregarPosts();
                    Toast.makeText(this, "Post publicado com sucesso", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}