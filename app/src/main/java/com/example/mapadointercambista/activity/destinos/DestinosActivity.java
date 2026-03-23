package com.example.mapadointercambista.activity.destinos;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.adapter.destino.DestinoAdapter;
import com.example.mapadointercambista.model.destino.Destino;
import com.example.mapadointercambista.model.destino.DestinoRepository;
import com.example.mapadointercambista.model.destino.DestinoStorage;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.ui.decoration.GridSpacingItemDecoration;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        DestinoStorage destinoStorage = new DestinoStorage(this);
        List<Destino> destinos = destinoStorage.carregarDestinos();

        if (destinos.isEmpty()) {
            destinos = DestinoRepository.getDestinos();
            destinoStorage.salvarDestinos(destinos);
        }

        DestinoAdapter adapter = new DestinoAdapter(destinos);
        listaTodosDestinos.setAdapter(adapter);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_mundo);
    }
}