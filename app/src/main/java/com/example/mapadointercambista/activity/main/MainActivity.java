package com.example.mapadointercambista.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.destinos.DestinosActivity;
import com.example.mapadointercambista.activity.forum.ForumActivity;
import com.example.mapadointercambista.adapter.home.CarrosselAdapter;
import com.example.mapadointercambista.adapter.destino.DestinoAdapter;
import com.example.mapadointercambista.adapter.forum.PostForumAdapter;
import com.example.mapadointercambista.model.destino.Destino;
import com.example.mapadointercambista.model.forum.ForumRepository;
import com.example.mapadointercambista.model.forum.ForumStorage;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.model.forum.PostForum;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Handler handler = new Handler();
    Runnable runnable;
    ViewPager2 carrossel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d("Ciclo de vida", "onCreate() chamado");

        carrossel = findViewById(R.id.carrossel);

        int[] imagens = {
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3
        };

        CarrosselAdapter adapter = new CarrosselAdapter(imagens);
        carrossel.setAdapter(adapter);

        ImageView setaEsquerda = findViewById(R.id.setaEsquerda);
        ImageView setaDireita = findViewById(R.id.setaDireita);

        setaDireita.setOnClickListener(v -> {
            int current = carrossel.getCurrentItem();
            int total = carrossel.getAdapter().getItemCount();

            if (current == total - 1) {
                carrossel.setCurrentItem(0);
            } else {
                carrossel.setCurrentItem(current + 1);
            }
        });

        setaEsquerda.setOnClickListener(v -> {
            int current = carrossel.getCurrentItem();
            int total = carrossel.getAdapter().getItemCount();

            if (current == 0) {
                carrossel.setCurrentItem(total - 1);
            } else {
                carrossel.setCurrentItem(current - 1);
            }
        });

        iniciarAutoSlide();

        TextView verTodos = findViewById(R.id.verTodosDestinos);
        verTodos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DestinosActivity.class);
            startActivity(intent);
        });

        RecyclerView listaDestinos = findViewById(R.id.listaDestinos);
        listaDestinos.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        List<Destino> destinos = new ArrayList<>();
        destinos.add(new Destino("Inglaterra - Oxford", R.drawable.inglaterra, 4.8f, 120));
        destinos.add(new Destino("Japão - Kyoto", R.drawable.japao, 4.9f, 210));
        destinos.add(new Destino("Alemanha - Berlim", R.drawable.alemanha, 4.7f, 98));

        DestinoAdapter adapterDestinos = new DestinoAdapter(destinos);
        listaDestinos.setAdapter(adapterDestinos);

        TextView verTodosForum = findViewById(R.id.verTodosForum);
        verTodosForum.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForumActivity.class);
            startActivity(intent);
        });

        RecyclerView listaForum = findViewById(R.id.listaForum);
        listaForum.setLayoutManager(new LinearLayoutManager(this));

        ForumStorage forumStorage = new ForumStorage(this);
        List<PostForum> posts = forumStorage.carregarPosts();

        if (posts.isEmpty()) {
            posts = ForumRepository.criarPostsIniciais();
            forumStorage.salvarPosts(posts);
        }

        PostForumAdapter adapterForum = new PostForumAdapter(this, posts, false);
        listaForum.setAdapter(adapterForum);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_home);
    }

    private void iniciarAutoSlide() {
        runnable = new Runnable() {
            @Override
            public void run() {
                int current = carrossel.getCurrentItem();
                int total = carrossel.getAdapter().getItemCount();

                if (current == total - 1) {
                    carrossel.setCurrentItem(0, false);
                } else {
                    carrossel.setCurrentItem(current + 1, true);
                }

                handler.postDelayed(this, 4000);
            }
        };

        handler.postDelayed(runnable, 4000);
    }

    @Override
    protected void onResume() {
        super.onResume();

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
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}