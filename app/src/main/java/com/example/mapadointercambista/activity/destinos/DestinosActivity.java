package com.example.mapadointercambista.activity.destinos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.List;

public class DestinosActivity extends AppCompatActivity {

    private RecyclerView listaTodosDestinos;
    private DestinoAdapter adapter;

    private final List<Destino> destinosOriginais = new ArrayList<>();
    private final List<Destino> destinosExibidos = new ArrayList<>();

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
        listaTodosDestinos.setHasFixedSize(true);
        listaTodosDestinos.setItemViewCacheSize(10);

        int spacing = 24;
        listaTodosDestinos.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        adapter = new DestinoAdapter(destinosExibidos);
        listaTodosDestinos.setAdapter(adapter);

        carregarDestinos();

        android.widget.EditText barraPesquisa = findViewById(R.id.barraPesquisa);
        barraPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarBusca(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_mundo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDestinos();
    }

    private void carregarDestinos() {
        DestinoStorage destinoStorage = new DestinoStorage(this);
        List<Destino> destinos = destinoStorage.carregarDestinos();

        if (destinos.isEmpty()) {
            destinos = DestinoRepository.getDestinos();
            destinoStorage.salvarDestinos(destinos);
        }

        destinosOriginais.clear();
        destinosOriginais.addAll(destinos);

        destinosExibidos.clear();
        destinosExibidos.addAll(destinos);

        adapter.notifyDataSetChanged();
    }

    private void aplicarBusca(String texto) {
        String busca = texto != null ? texto.trim().toLowerCase() : "";

        destinosExibidos.clear();

        for (Destino destino : destinosOriginais) {
            String nome = destino.getNome() != null ? destino.getNome().toLowerCase() : "";
            String pais = destino.getPais() != null ? destino.getPais().toLowerCase() : "";
            String idioma = destino.getIdioma() != null ? destino.getIdioma().toLowerCase() : "";

            if (busca.isEmpty()
                    || nome.contains(busca)
                    || pais.contains(busca)
                    || idioma.contains(busca)) {
                destinosExibidos.add(destino);
            }
        }

        adapter.notifyDataSetChanged();
    }
}