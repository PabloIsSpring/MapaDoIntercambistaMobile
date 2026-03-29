package com.example.mapadointercambista.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.example.mapadointercambista.activity.destinos.DestinosActivity;
import com.example.mapadointercambista.activity.forum.ForumActivity;
import com.example.mapadointercambista.adapter.destino.DestinoAdapter;
import com.example.mapadointercambista.adapter.forum.PostForumAdapter;
import com.example.mapadointercambista.adapter.home.CarrosselAdapter;
import com.example.mapadointercambista.model.destino.Destino;
import com.example.mapadointercambista.model.destino.DestinoRepository;
import com.example.mapadointercambista.model.destino.DestinoStorage;
import com.example.mapadointercambista.model.forum.ForumRepository;
import com.example.mapadointercambista.model.forum.ForumStorage;
import com.example.mapadointercambista.model.forum.PostForum;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final long AUTO_SLIDE_DELAY_MS = 4500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Destino> destinosHome = new ArrayList<>();
    private final List<PostForum> postsHome = new ArrayList<>();

    private Runnable autoSlideRunnable;
    private ViewPager2 carrossel;
    private RecyclerView listaDestinos;
    private RecyclerView listaForum;

    private DestinoAdapter adapterDestinos;
    private PostForumAdapter adapterForum;

    private boolean dadosCarregados = false;
    private LinearLayout indicadorCarrossel;
    private int totalBanners = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        aplicarModoImersivo();
        configurarInsets();
        inicializarViews();
        indicadorCarrossel = findViewById(R.id.indicadorCarrossel);
        configurarCarrossel();
        configurarAcoes();
        configurarListas();
        carregarDadosIniciais();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_home);
    }

    private void configurarInsets() {
        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void inicializarViews() {
        carrossel = findViewById(R.id.carrossel);
        listaDestinos = findViewById(R.id.listaDestinos);
        listaForum = findViewById(R.id.listaForum);
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

    private void configurarAcoes() {
        TextView verTodosDestinos = findViewById(R.id.verTodosDestinos);
        TextView verTodosForum = findViewById(R.id.verTodosForum);

        verTodosDestinos.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, DestinosActivity.class)));

        verTodosForum.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ForumActivity.class)));
    }

    private void configurarListas() {
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

        adapterForum = new PostForumAdapter(this, postsHome, false);
        listaForum.setAdapter(adapterForum);
    }

    private void carregarDadosIniciais() {
        if (dadosCarregados) return;

        carregarDestinosHome();
        carregarForumHome();
        dadosCarregados = true;
    }

    private void carregarDestinosHome() {
        DestinoStorage destinoStorage = new DestinoStorage(this);
        List<Destino> destinos = destinoStorage.carregarDestinos();

        if (destinos.isEmpty()) {
            destinos = DestinoRepository.getDestinos();
            destinoStorage.salvarDestinos(destinos);
        }

        destinosHome.clear();
        destinosHome.addAll(destinos.size() > 3 ? destinos.subList(0, 3) : destinos);
        adapterDestinos.notifyDataSetChanged();
    }

    private void carregarForumHome() {
        ForumStorage forumStorage = new ForumStorage(this);
        List<PostForum> posts = forumStorage.carregarPosts();

        if (posts.isEmpty()) {
            posts = ForumRepository.criarPostsIniciais();
            forumStorage.salvarPosts(posts);
        }

        postsHome.clear();
        postsHome.addAll(posts);
        adapterForum.notifyDataSetChanged();
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