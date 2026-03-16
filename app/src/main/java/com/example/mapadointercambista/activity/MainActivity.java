package com.example.mapadointercambista.activity;

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

import com.example.mapadointercambista.adapter.CarrosselAdapter;
import com.example.mapadointercambista.adapter.PostForumAdapter;
import com.example.mapadointercambista.model.Destino;
import com.example.mapadointercambista.adapter.DestinoAdapter;
import com.example.mapadointercambista.R;
import com.example.mapadointercambista.model.PostForum;
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

        if(savedInstanceState != null){
            // Restaura os valores do estado salvo
            int v1 = savedInstanceState.getInt("valor1");
            int v2 = savedInstanceState.getInt("valor1");
        }
        else{
            // Provavelmente inicializa as variáveis com valores
        }

        Log.d("Ciclo de vida", "onCreate()chamado");

        // Carrossel
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

            if(current == total - 1){
                carrossel.setCurrentItem(0);
            }else{
                carrossel.setCurrentItem(current + 1);
            }

        });

        setaEsquerda.setOnClickListener(v -> {

            int current = carrossel.getCurrentItem();
            int total = carrossel.getAdapter().getItemCount();

            if(current == 0){
                carrossel.setCurrentItem(total - 1);
            }else{
                carrossel.setCurrentItem(current - 1);
            }

        });

        iniciarAutoSlide();

        // Ver todos os destinos

        TextView verTodos = findViewById(R.id.verTodosDestinos);

        verTodos.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, DestinosActivity.class);
            startActivity(intent);

        });

        // Cards
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

        // Fóruns
        RecyclerView listaForum = findViewById(R.id.listaForum);

        listaForum.setLayoutManager(
                new LinearLayoutManager(this)
        );

        List<PostForum> posts = new ArrayList<>();

        posts.add(new PostForum(
                "Associação MarkitoLivre",
                "Gostei hein, top demais",
                R.drawable.logo,
                4.5f,
                21
        ));

        posts.add(new PostForum(
                "Juninho Mandelão",
                "Show de bola esse aplicativo!",
                R.drawable.logo,
                5.0f,
                5
        ));

        posts.add(new PostForum(
                "XD",
                "Aplicativo ficou muito bom!",
                R.drawable.logo,
                4.7f,
                15
        ));

        PostForumAdapter adapterForum = new PostForumAdapter(posts);
        listaForum.setAdapter(adapterForum);

        // Barra Inferior

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(item -> {

            if(item.getItemId() == R.id.nav_home){
                return true;
            }

            if(item.getItemId() == R.id.nav_forum){
                return true;
            }

            if(item.getItemId() == R.id.nav_mundo){
                return true;
            }

            if(item.getItemId() == R.id.nav_perfil){
                return true;
            }

            return false;
        });
    }

    private void iniciarAutoSlide(){

        runnable = new Runnable() {
            @Override
            public void run() {

                int current = carrossel.getCurrentItem();
                int total = carrossel.getAdapter().getItemCount();

                if(current == total - 1){

                    // volta para o início sem animação
                    carrossel.setCurrentItem(0, false);

                }else{

                    // avança normal
                    carrossel.setCurrentItem(current + 1, true);

                }

                handler.postDelayed(this, 4000);
            }
        };

        handler.postDelayed(runnable, 4000);
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d("Ciclo de vida", "onStart()chamado");
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d("Ciclo de vida", "onRestart()chamado");
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
    protected void onPause(){
        super.onPause();
        Log.d("Ciclo de vida", "onPause()chamado");
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d("Ciclo de vida", "onStop()chamado");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("Ciclo de vida", "onDestroy()chamado");
    }

    @Override
    public void onSaveInstanceState(Bundle savedIntanceState){
        // Salva o estado atual do jogador
        savedIntanceState.putInt("valor1", 1000); // Forma de declarar variável
        savedIntanceState.putInt("valor2", 55);
        //Invoca a super classe, para que seja possível salvar o estado
        super.onSaveInstanceState(savedIntanceState);
    }
}