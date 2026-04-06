package com.example.mapadointercambista.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.auth.LoginActivity;
import com.example.mapadointercambista.activity.destinos.DestinosActivity;
import com.example.mapadointercambista.activity.forum.ForumActivity;
import com.example.mapadointercambista.adapter.destino.DestinoAdapter;
import com.example.mapadointercambista.adapter.forum.PostForumAdapter;
import com.example.mapadointercambista.adapter.home.CarrosselAdapter;
import com.example.mapadointercambista.model.destino.Destino;
import com.example.mapadointercambista.model.destino.DestinoRepository;
import com.example.mapadointercambista.model.destino.DestinoStorage;
import com.example.mapadointercambista.model.destino.FavoritosStorage;
import com.example.mapadointercambista.model.forum.ForumRepository;
import com.example.mapadointercambista.model.forum.ForumStorage;
import com.example.mapadointercambista.model.forum.PostForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.util.InputSecurityUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final long AUTO_SLIDE_DELAY_MS = 4500L;
    private static final int LIMITE_DESTINOS_HOME = 3;
    private static final int LIMITE_POSTS_HOME = 3;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final List<Destino> destinosHome = new ArrayList<>();
    private final List<PostForum> postsHome = new ArrayList<>();
    private final List<Destino> favoritosHome = new ArrayList<>();

    private final List<Destino> todosDestinosHome = new ArrayList<>();
    private final List<PostForum> todosPostsHome = new ArrayList<>();
    private final List<Destino> todosFavoritosHome = new ArrayList<>();

    private Runnable autoSlideRunnable;
    private ViewPager2 carrossel;
    private RecyclerView listaDestinos;
    private RecyclerView listaForum;
    private RecyclerView listaFavoritosHome;

    private DestinoAdapter adapterDestinos;
    private PostForumAdapter adapterForum;
    private DestinoAdapter adapterFavoritos;

    private boolean dadosCarregados = false;
    private LinearLayout indicadorCarrossel;
    private int totalBanners = 0;
    private LinearLayout secaoFavoritosHome;
    private LinearLayout secaoDestinosHome;
    private LinearLayout secaoForumHome;
    private TextView textoResumoFavoritosHome;

    private TextView verTodosDestinos;
    private TextView verTodosForum;
    private EditText barraPesquisa;

    private SessionManager sessionManager;
    private FavoritosStorage favoritosStorage;

    private String textoBuscaAtual = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        if (sessionManager.sessaoApiExpirada()) {
            sessionManager.logout();
            Toast.makeText(this, "Sua sessão expirou. Faça login novamente.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        favoritosStorage = new FavoritosStorage(this);

        aplicarModoImersivo();
        configurarInsets();
        inicializarViews();
        configurarCarrossel();
        configurarListas();
        configurarBusca();
        configurarAcoes();
        carregarDadosIniciais();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_home);
    }

    private void configurarInsets() {
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    0
            );

            return insets;
        });
    }

    private void inicializarViews() {
        carrossel = findViewById(R.id.carrossel);
        listaDestinos = findViewById(R.id.listaDestinos);
        listaForum = findViewById(R.id.listaForum);
        listaFavoritosHome = findViewById(R.id.listaFavoritosHome);
        indicadorCarrossel = findViewById(R.id.indicadorCarrossel);
        secaoFavoritosHome = findViewById(R.id.secaoFavoritosHome);
        textoResumoFavoritosHome = findViewById(R.id.textoResumoFavoritosHome);
        barraPesquisa = findViewById(R.id.barraPesquisa);
        verTodosDestinos = findViewById(R.id.verTodosDestinos);
        verTodosForum = findViewById(R.id.verTodosForum);

        secaoDestinosHome = (LinearLayout) listaDestinos.getParent();
        secaoForumHome = (LinearLayout) listaForum.getParent();
    }

    private void configurarCarrossel() {
        int[] imagens = {
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3
        };

        CarrosselAdapter adapterCarrossel = new CarrosselAdapter(imagens);
        carrossel.setAdapter(adapterCarrossel);
        totalBanners = imagens.length;
        configurarIndicadores(totalBanners);
        atualizarIndicador(0);

        carrossel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                atualizarIndicador(position);
            }
        });

        carrossel.setOffscreenPageLimit(1);

        ImageView setaEsquerda = findViewById(R.id.setaEsquerda);
        ImageView setaDireita = findViewById(R.id.setaDireita);

        setaDireita.setOnClickListener(v -> avancarCarrossel());
        setaEsquerda.setOnClickListener(v -> voltarCarrossel());

        autoSlideRunnable = () -> {
            if (carrossel == null || carrossel.getAdapter() == null) return;

            int total = carrossel.getAdapter().getItemCount();
            if (total <= 1) return;

            int proximo = (carrossel.getCurrentItem() + 1) % total;
            carrossel.setCurrentItem(proximo, true);

            handler.postDelayed(autoSlideRunnable, AUTO_SLIDE_DELAY_MS);
        };
    }

    private void configurarIndicadores(int quantidade) {
        if (indicadorCarrossel == null) return;

        indicadorCarrossel.removeAllViews();

        for (int i = 0; i < quantidade; i++) {
            View indicador = new View(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 8);
            params.setMargins(6, 0, 6, 0);
            indicador.setLayoutParams(params);
            indicador.setBackgroundResource(R.drawable.bg_indicador_carrossel_inativo);

            indicadorCarrossel.addView(indicador);
        }
    }

    private void atualizarIndicador(int posicaoAtiva) {
        if (indicadorCarrossel == null) return;

        for (int i = 0; i < indicadorCarrossel.getChildCount(); i++) {
            View indicador = indicadorCarrossel.getChildAt(i);
            indicador.setBackgroundResource(
                    i == posicaoAtiva
                            ? R.drawable.bg_indicador_carrossel_ativo
                            : R.drawable.bg_indicador_carrossel_inativo
            );
        }
    }

    private void configurarBusca() {
        barraPesquisa.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                aplicarModoImersivo();
            }
        });

        barraPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBuscaAtual = InputSecurityUtils.sanitizeUserText(
                        s != null ? s.toString() : ""
                );
                aplicarBuscaHome();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void configurarAcoes() {
        verTodosDestinos.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, DestinosActivity.class)));

        verTodosForum.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ForumActivity.class)));
    }

    private void configurarListas() {
        listaFavoritosHome.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        listaFavoritosHome.setHasFixedSize(true);
        listaFavoritosHome.setItemViewCacheSize(6);

        adapterFavoritos = new DestinoAdapter(favoritosHome);
        listaFavoritosHome.setAdapter(adapterFavoritos);

        listaDestinos.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        listaDestinos.setHasFixedSize(true);
        listaDestinos.setItemViewCacheSize(6);

        adapterDestinos = new DestinoAdapter(destinosHome);
        listaDestinos.setAdapter(adapterDestinos);

        listaForum.setLayoutManager(new LinearLayoutManager(this));
        listaForum.setHasFixedSize(false);
        listaForum.setItemViewCacheSize(8);
        listaForum.setItemAnimator(null);

        adapterForum = new PostForumAdapter(this, postsHome, false);
        listaForum.setAdapter(adapterForum);
    }

    private void carregarDadosIniciais() {
        if (dadosCarregados) return;

        carregarDestinosHomeBase();
        carregarForumHomeBase();
        carregarFavoritosHomeBase();
        aplicarBuscaHome();

        dadosCarregados = true;
    }

    private void carregarDestinosHomeBase() {
        DestinoStorage destinoStorage = new DestinoStorage(this);
        List<Destino> destinos = destinoStorage.carregarDestinos();

        if (destinos.isEmpty()) {
            destinos = DestinoRepository.getDestinos();
            destinoStorage.salvarDestinos(destinos);
        }

        todosDestinosHome.clear();

        int limite = Math.min(destinos.size(), LIMITE_DESTINOS_HOME);
        for (int i = 0; i < limite; i++) {
            todosDestinosHome.add(destinos.get(i));
        }
    }

    private void carregarForumHomeBase() {
        ForumStorage forumStorage = new ForumStorage(this);
        List<PostForum> posts = forumStorage.carregarPosts();

        if (posts.isEmpty()) {
            posts = ForumRepository.criarPostsIniciais();
            forumStorage.salvarPosts(posts);
        }

        todosPostsHome.clear();

        int limite = Math.min(posts.size(), LIMITE_POSTS_HOME);
        for (int i = 0; i < limite; i++) {
            todosPostsHome.add(posts.get(i));
        }
    }

    private void carregarFavoritosHomeBase() {
        todosFavoritosHome.clear();

        if (!sessionManager.estaLogado()) {
            secaoFavoritosHome.setVisibility(View.GONE);
            return;
        }

        List<Destino> todosDestinos = new DestinoStorage(this).carregarDestinos();

        if (todosDestinos.isEmpty()) {
            todosDestinos = DestinoRepository.getDestinos();
            new DestinoStorage(this).salvarDestinos(todosDestinos);
        }

        java.util.Set<String> favoritosIds =
                favoritosStorage.carregarFavoritos(sessionManager.getEmailUsuario());

        for (Destino destino : todosDestinos) {
            if (favoritosIds.contains(destino.getId())) {
                todosFavoritosHome.add(destino);
            }
        }
    }

    private void aplicarBuscaHome() {
        String busca = textoBuscaAtual != null
                ? textoBuscaAtual.trim().toLowerCase()
                : "";

        favoritosHome.clear();
        destinosHome.clear();
        postsHome.clear();

        if (busca.isEmpty()) {
            favoritosHome.addAll(todosFavoritosHome);
            destinosHome.addAll(todosDestinosHome);
            postsHome.addAll(todosPostsHome);
        } else {
            for (Destino destino : todosFavoritosHome) {
                if (destinoCorrespondeBusca(destino, busca)) {
                    favoritosHome.add(destino);
                }
            }

            for (Destino destino : todosDestinosHome) {
                if (destinoCorrespondeBusca(destino, busca)) {
                    destinosHome.add(destino);
                }
            }

            for (PostForum post : todosPostsHome) {
                if (postCorrespondeBusca(post, busca)) {
                    postsHome.add(post);
                }
            }
        }

        atualizarSecoesHome(busca.isEmpty());

        adapterFavoritos.notifyDataSetChanged();
        adapterDestinos.notifyDataSetChanged();
        adapterForum.notifyDataSetChanged();
    }

    private boolean destinoCorrespondeBusca(Destino destino, String busca) {
        if (destino == null) return false;

        String nome = textoSeguro(destino.getNome());
        String pais = textoSeguro(destino.getPais());
        String idioma = textoSeguro(destino.getIdioma());
        String continente = textoSeguro(destino.getContinente());

        return nome.contains(busca)
                || pais.contains(busca)
                || idioma.contains(busca)
                || continente.contains(busca);
    }

    private boolean postCorrespondeBusca(PostForum post, String busca) {
        if (post == null) return false;

        String autor = textoSeguro(post.getAutorNome());
        String titulo = textoSeguro(post.getTitulo());
        String mensagem = textoSeguro(post.getMensagem());

        return autor.contains(busca)
                || titulo.contains(busca)
                || mensagem.contains(busca);
    }

    private void atualizarSecoesHome(boolean buscaVazia) {
        if (!sessionManager.estaLogado() || todosFavoritosHome.isEmpty()) {
            secaoFavoritosHome.setVisibility(View.GONE);
        } else {
            secaoFavoritosHome.setVisibility(favoritosHome.isEmpty() && !buscaVazia ? View.GONE : View.VISIBLE);

            if (favoritosHome.isEmpty() && !buscaVazia) {
                textoResumoFavoritosHome.setText("Nenhum favorito encontrado");
            } else {
                textoResumoFavoritosHome.setText(
                        favoritosHome.size() == 1
                                ? "1 destino salvo por você"
                                : favoritosHome.size() + " destinos salvos por você"
                );
            }
        }

        secaoDestinosHome.setVisibility(destinosHome.isEmpty() ? View.GONE : View.VISIBLE);
        secaoForumHome.setVisibility(postsHome.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void avancarCarrossel() {
        if (carrossel.getAdapter() == null) return;

        int total = carrossel.getAdapter().getItemCount();
        if (total == 0) return;

        int atual = carrossel.getCurrentItem();
        carrossel.setCurrentItem((atual + 1) % total, true);
    }

    private void voltarCarrossel() {
        if (carrossel.getAdapter() == null) return;

        int total = carrossel.getAdapter().getItemCount();
        if (total == 0) return;

        int atual = carrossel.getCurrentItem();
        int anterior = (atual - 1 + total) % total;
        carrossel.setCurrentItem(anterior, true);
    }

    private void aplicarModoImersivo() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    private String textoSeguro(String valor) {
        return InputSecurityUtils.sanitizeUserText(
                valor != null ? valor.toLowerCase() : ""
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        aplicarModoImersivo();

        if (autoSlideRunnable != null && carrossel != null
                && carrossel.getAdapter() != null
                && carrossel.getAdapter().getItemCount() > 1) {
            handler.postDelayed(autoSlideRunnable, AUTO_SLIDE_DELAY_MS);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        carregarDestinosHomeBase();
        carregarForumHomeBase();
        carregarFavoritosHomeBase();
        aplicarBuscaHome();
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarModoImersivo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (autoSlideRunnable != null) {
            handler.removeCallbacks(autoSlideRunnable);
        }
    }
}