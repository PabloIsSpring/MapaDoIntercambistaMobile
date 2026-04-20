package com.example.mapadointercambista.activity.forum;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.mapadointercambista.util.AnimationUtils;
import com.example.mapadointercambista.util.InputSecurityUtils;
import com.example.mapadointercambista.util.TransitionHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForumActivity extends AppCompatActivity {

    private static final String PREF_FORUM_UI = "forum_ui";
    private static final String KEY_ORDENACAO = "ordenacao";
    private static final int MAX_BUSCA = 80;

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
    private View containerVazioForum;

    private ActivityResultLauncher<Intent> launcherNovoPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum);

        aplicarModoImersivo();

        forumStorage = new ForumStorage(this);
        sessionManager = new SessionManager(this);

        configurarLauncherNovoPost();

        listaTodosForuns = findViewById(R.id.listaTodosForuns);
        listaTodosForuns.setLayoutManager(new LinearLayoutManager(this));
        listaTodosForuns.setHasFixedSize(false);
        listaTodosForuns.setItemViewCacheSize(10);
        listaTodosForuns.setItemAnimator(null);

        adapter = new PostForumAdapter(this, postsExibidos, true);
        listaTodosForuns.setAdapter(adapter);

        textoResultadosForum = findViewById(R.id.textoResultadosForum);
        containerVazioForum = findViewById(R.id.containerVazioForum);
        textoVazioForum = findViewById(R.id.textoVazioForum);

        EditText barraPesquisa = findViewById(R.id.barraPesquisaForum);
        ImageView iconeFiltro = findViewById(R.id.iconeFiltroForum);

        barraPesquisa.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(MAX_BUSCA)
        });

        AnimationUtils.applyPressAnimation(barraPesquisa);
        AnimationUtils.applyPressAnimation(iconeFiltro);

        criterioOrdenacao = getSharedPreferences(PREF_FORUM_UI, MODE_PRIVATE)
                .getString(KEY_ORDENACAO, "recentes");

        barraPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBusca = InputSecurityUtils.sanitizeSearchText(
                        s != null ? s.toString() : "",
                        MAX_BUSCA
                );
                aplicarBuscaEOrdenacao();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        iconeFiltro.setOnClickListener(v -> {
            AnimationUtils.playBounce(v);
            abrirBottomSheetFiltrosForum();
        });

        carregarPostsIniciais();

        View botaoNovoForum = findViewById(R.id.botaoNovoForum);
        AnimationUtils.applyPressAnimation(botaoNovoForum);

        botaoNovoForum.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em uma conta para publicar.", Toast.LENGTH_SHORT).show();
                return;
            }

            AnimationUtils.playBounce(v);

            Intent intent = new Intent(this, NovaPublicacaoActivity.class);
            launcherNovoPost.launch(intent);
            TransitionHelper.slideForward(this);
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            aplicarModoImersivo();
        }
    }

    private void configurarLauncherNovoPost() {
        launcherNovoPost = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        sincronizarPosts();
                    }
                }
        );
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

                if (!textoSeguro(antigo.getId()).equals(textoSeguro(novo.getId()))
                        || antigo.getLikes() != novo.getLikes()
                        || antigo.getDislikes() != novo.getDislikes()
                        || antigo.getQuantidadeRespostas() != novo.getQuantidadeRespostas()
                        || !textoSeguro(antigo.getTitulo()).equals(textoSeguro(novo.getTitulo()))
                        || !textoSeguro(antigo.getMensagem()).equals(textoSeguro(novo.getMensagem()))
                        || !textoSeguro(antigo.getImagemUri()).equals(textoSeguro(novo.getImagemUri()))) {
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
        String busca = InputSecurityUtils.sanitizeSearchText(textoBusca, MAX_BUSCA).toLowerCase();

        for (PostForum post : postsOriginais) {
            String autor = textoSeguro(post.getAutorNome()).toLowerCase();
            String titulo = textoSeguro(post.getTitulo()).toLowerCase();
            String mensagem = textoSeguro(post.getMensagem()).toLowerCase();

            if (busca.isEmpty()
                    || autor.contains(busca)
                    || titulo.contains(busca)
                    || mensagem.contains(busca)) {
                filtrados.add(post);
            }
        }

        ordenarLista(filtrados);

        postsExibidos.clear();
        postsExibidos.addAll(filtrados);
        adapter.notifyDataSetChanged();

        int quantidade = filtrados.size();
        textoResultadosForum.setText(
                quantidade == 1
                        ? "1 publicação encontrada"
                        : quantidade + " publicações encontradas"
        );

        boolean vazio = quantidade == 0;
        containerVazioForum.setVisibility(vazio ? View.VISIBLE : View.GONE);
        listaTodosForuns.setVisibility(vazio ? View.GONE : View.VISIBLE);

        if (vazio) {
            textoVazioForum.setText(busca.isEmpty()
                    ? "Ainda não existem publicações no fórum."
                    : "Nenhuma publicação encontrada.");
        }
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

    private void abrirBottomSheetFiltrosForum() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_filtros_forum, null);
        dialog.setContentView(view);

        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(android.R.color.transparent);
        }

        RadioButton radioRecentes = view.findViewById(R.id.radioRecentesForum);
        RadioButton radioCurtidos = view.findViewById(R.id.radioCurtidosForum);
        RadioButton radioRespondidos = view.findViewById(R.id.radioRespondidosForum);

        View.OnClickListener selecionarUnico = v -> {
            radioRecentes.setChecked(v == radioRecentes);
            radioCurtidos.setChecked(v == radioCurtidos);
            radioRespondidos.setChecked(v == radioRespondidos);
        };

        radioRecentes.setOnClickListener(selecionarUnico);
        radioCurtidos.setOnClickListener(selecionarUnico);
        radioRespondidos.setOnClickListener(selecionarUnico);

        switch (criterioOrdenacao) {
            case "curtidos":
                radioCurtidos.setChecked(true);
                break;
            case "respondidos":
                radioRespondidos.setChecked(true);
                break;
            case "recentes":
            default:
                radioRecentes.setChecked(true);
                break;
        }

        view.findViewById(R.id.botaoFecharFiltrosForum).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.botaoAplicarFiltrosForum).setOnClickListener(v -> {
            if (radioCurtidos.isChecked()) {
                criterioOrdenacao = "curtidos";
            } else if (radioRespondidos.isChecked()) {
                criterioOrdenacao = "respondidos";
            } else {
                criterioOrdenacao = "recentes";
            }

            getSharedPreferences(PREF_FORUM_UI, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_ORDENACAO, criterioOrdenacao)
                    .apply();

            aplicarBuscaEOrdenacao();
            dialog.dismiss();
        });

        dialog.show();
    }

    private String textoSeguro(String valor) {
        return InputSecurityUtils.sanitizeUserText(valor);
    }
}