package com.example.mapadointercambista.activity.forum;

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
import com.example.mapadointercambista.model.forum.PostForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForumActivity extends AppCompatActivity {

    private static final String PREF_FORUM_UI = "forum_ui";
    private static final String KEY_ORDENACAO = "ordenacao";

    private RecyclerView listaTodosForuns;
    private ForumStorage forumStorage;
    private SessionManager sessionManager;
    private PostForumAdapter adapter;

    private final List<PostForum> postsOriginais = new ArrayList<>();
    private final List<PostForum> postsExibidos = new ArrayList<>();

    private String textoBusca = "";
    private String criterioOrdenacao = "recentes";

    private TextView textoResultadosForum;
    private TextView textoVazioForum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum);

        aplicarModoImersivo();

        forumStorage = new ForumStorage(this);
        sessionManager = new SessionManager(this);

        listaTodosForuns = findViewById(R.id.listaTodosForuns);
        listaTodosForuns.setLayoutManager(new LinearLayoutManager(this));
        listaTodosForuns.setHasFixedSize(false);
        listaTodosForuns.setItemViewCacheSize(10);

        adapter = new PostForumAdapter(this, postsExibidos, true);
        listaTodosForuns.setAdapter(adapter);

        textoResultadosForum = findViewById(R.id.textoResultadosForum);
        textoVazioForum = findViewById(R.id.textoVazioForum);

        EditText barraPesquisa = findViewById(R.id.barraPesquisaForum);
        ImageView iconeFiltro = findViewById(R.id.iconeFiltroForum);

        criterioOrdenacao = getSharedPreferences(PREF_FORUM_UI, MODE_PRIVATE)
                .getString(KEY_ORDENACAO, "recentes");

        barraPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBusca = s != null ? s.toString().trim() : "";
                aplicarBuscaEOrdenacao();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        iconeFiltro.setOnClickListener(this::abrirMenuOrdenacao);

        carregarPostsIniciais();

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
        aplicarModoImersivo();
        sincronizarPosts();
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

    private void carregarPostsIniciais() {
        List<PostForum> posts = forumStorage.carregarPosts();

        if (posts.isEmpty()) {
            posts = ForumRepository.criarPostsIniciais();
            forumStorage.salvarPosts(posts);
        }

        postsOriginais.clear();
        postsOriginais.addAll(posts);
        aplicarBuscaEOrdenacao();
    }

    private void sincronizarPosts() {
        List<PostForum> atualizados = forumStorage.carregarPosts();

        if (atualizados.size() == postsOriginais.size()) {
            boolean mudou = false;

            for (int i = 0; i < atualizados.size(); i++) {
                PostForum antigo = postsOriginais.get(i);
                PostForum novo = atualizados.get(i);

                if (!antigo.getId().equals(novo.getId())
                        || antigo.getLikes() != novo.getLikes()
                        || antigo.getDislikes() != novo.getDislikes()
                        || antigo.getQuantidadeRespostas() != novo.getQuantidadeRespostas()
                        || !antigo.getMensagem().equals(novo.getMensagem())) {
                    mudou = true;
                    break;
                }
            }

            if (!mudou) {
                return;
            }
        }

        postsOriginais.clear();
        postsOriginais.addAll(atualizados);
        aplicarBuscaEOrdenacao();
    }

    private void aplicarBuscaEOrdenacao() {
        List<PostForum> filtrados = new ArrayList<>();
        String busca = textoBusca != null ? textoBusca.toLowerCase() : "";

        for (PostForum post : postsOriginais) {
            String autor = post.getAutorNome() != null ? post.getAutorNome().toLowerCase() : "";
            String mensagem = post.getMensagem() != null ? post.getMensagem().toLowerCase() : "";

            if (busca.isEmpty() || autor.contains(busca) || mensagem.contains(busca)) {
                filtrados.add(post);
            }
        }

        ordenarLista(filtrados);

        postsExibidos.clear();
        postsExibidos.addAll(filtrados);
        adapter.notifyDataSetChanged();

        int quantidade = filtrados.size();
        textoResultadosForum.setText(quantidade == 1 ? "1 resultado" : quantidade + " resultados");
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

            if ("Mais recentes".equals(titulo)) {
                criterioOrdenacao = "recentes";
            } else if ("Mais curtidos".equals(titulo)) {
                criterioOrdenacao = "curtidos";
            } else if ("Mais respondidos".equals(titulo)) {
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
}