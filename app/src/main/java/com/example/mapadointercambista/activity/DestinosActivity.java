package com.example.mapadointercambista.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.adapter.DestinoAdapter;
import com.example.mapadointercambista.model.Destino;
import com.example.mapadointercambista.model.GridSpacingItemDecoration;
import com.example.mapadointercambista.model.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class DestinosActivity extends AppCompatActivity {

    RecyclerView listaTodosDestinos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_destinos);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        listaTodosDestinos = findViewById(R.id.listaTodosDestinos);

        listaTodosDestinos.setLayoutManager(new GridLayoutManager(this, 2));

        int spacing = 24;
        listaTodosDestinos.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        List<Destino> destinos = new ArrayList<>();

        destinos.add(new Destino("Inglaterra - Oxford", R.drawable.inglaterra, 4.8f, 120));
        destinos.add(new Destino("Japão - Kyoto", R.drawable.japao, 4.9f, 210));
        destinos.add(new Destino("Alemanha - Berlim", R.drawable.alemanha, 4.7f, 98));
        destinos.add(new Destino("Canadá - Toronto", R.drawable.toronto, 5f, 5));
        destinos.add(new Destino("Espanha - Barcelona", R.drawable.barcelona, 4f, 3));
        destinos.add(new Destino("Austrália - Sydney", R.drawable.boston, 4f, 8));
        destinos.add(new Destino("Portugal - Lisboa", R.drawable.boston, 5f, 5));


        DestinoAdapter adapter = new DestinoAdapter(destinos);

        listaTodosDestinos.setAdapter(adapter);

        // Barra Inferior

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_mundo);
    }
}