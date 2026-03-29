package com.example.mapadointercambista.activity.destinos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.adapter.destino.DestinoAdapter;
import com.example.mapadointercambista.model.destino.Destino;
import com.example.mapadointercambista.model.destino.DestinoRepository;
import com.example.mapadointercambista.model.destino.DestinoStorage;
import com.example.mapadointercambista.model.destino.FavoritosStorage;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.ui.decoration.GridSpacingItemDecoration;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DestinosActivity extends AppCompatActivity {

    private RecyclerView listaTodosDestinos;
    private DestinoAdapter adapter;

    private final List<Destino> destinosOriginais = new ArrayList<>();
    private final List<Destino> destinosExibidos = new ArrayList<>();

    private TextView textoResultadosDestinos;

    private String filtroIdiomaSelecionado = "Todos";
    private String filtroContinenteSelecionado = "Todos";
    private String filtroPaisSelecionado = "Todos";
    private String filtroOrdenacaoSelecionado = "Melhor nota";
    private boolean somenteFavoritos = false;
    private String textoBuscaAtual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_destinos);

        aplicarModoImersivo();

        listaTodosDestinos = findViewById(R.id.listaTodosDestinos);
        listaTodosDestinos.setLayoutManager(new GridLayoutManager(this, 2));
        listaTodosDestinos.setHasFixedSize(true);
        listaTodosDestinos.setItemViewCacheSize(10);

        int spacing = 24;
        listaTodosDestinos.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        adapter = new DestinoAdapter(destinosExibidos);
        listaTodosDestinos.setAdapter(adapter);

        textoResultadosDestinos = findViewById(R.id.textoResultadosDestinos);

        ImageView iconeFiltro = findViewById(R.id.iconeFiltroDestinos);
        iconeFiltro.setOnClickListener(v -> abrirBottomSheetFiltros());

        EditText barraPesquisa = findViewById(R.id.barraPesquisa);
        barraPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textoBuscaAtual = s != null ? s.toString() : "";
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        carregarDestinos();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_mundo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarModoImersivo();
        sincronizarDestinos();
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

    private void carregarDestinos() {
        DestinoStorage destinoStorage = new DestinoStorage(this);
        List<Destino> destinos = destinoStorage.carregarDestinos();

        boolean precisaRecriar = destinos.isEmpty();

        if (!precisaRecriar) {
            for (Destino destino : destinos) {
                boolean continenteInvalido = destino.getContinente() == null || destino.getContinente().trim().isEmpty();
                boolean imagemInvalida = destino.getImagemNome() == null || destino.getImagemNome().trim().isEmpty();

                boolean imagemAntigaErrada =
                        "destino_sydney".equals(destino.getId()) && !"australia".equals(destino.getImagemNome());

                boolean imagemAntigaLisboa =
                        "destino_lisboa".equals(destino.getId()) && !"lisboa".equals(destino.getImagemNome());

                if (continenteInvalido || imagemInvalida || imagemAntigaErrada || imagemAntigaLisboa) {
                    precisaRecriar = true;
                    break;
                }
            }
        }

        if (precisaRecriar) {
            destinos = DestinoRepository.getDestinos();
            destinoStorage.salvarDestinos(destinos);
        }

        destinosOriginais.clear();
        destinosOriginais.addAll(destinos);

        aplicarFiltros();
    }

    private void sincronizarDestinos() {
        DestinoStorage destinoStorage = new DestinoStorage(this);
        List<Destino> atualizados = destinoStorage.carregarDestinos();

        boolean precisaRecriar = atualizados.isEmpty();

        if (!precisaRecriar) {
            for (Destino destino : atualizados) {
                boolean continenteInvalido = destino.getContinente() == null || destino.getContinente().trim().isEmpty();
                boolean imagemInvalida = destino.getImagemNome() == null || destino.getImagemNome().trim().isEmpty();

                boolean imagemAntigaErrada =
                        "destino_sydney".equals(destino.getId()) && !"australia".equals(destino.getImagemNome());

                boolean imagemAntigaLisboa =
                        "destino_lisboa".equals(destino.getId()) && !"lisboa".equals(destino.getImagemNome());

                if (continenteInvalido || imagemInvalida || imagemAntigaErrada || imagemAntigaLisboa) {
                    precisaRecriar = true;
                    break;
                }
            }
        }

        if (precisaRecriar) {
            atualizados = DestinoRepository.getDestinos();
            destinoStorage.salvarDestinos(atualizados);
        }

        if (atualizados.size() == destinosOriginais.size()) {
            boolean mudou = false;

            for (int i = 0; i < atualizados.size(); i++) {
                Destino antigo = destinosOriginais.get(i);
                Destino novo = atualizados.get(i);

                if (!antigo.getId().equals(novo.getId())
                        || antigo.getNota() != novo.getNota()
                        || antigo.getAvaliacoes() != novo.getAvaliacoes()
                        || (antigo.getContinente() == null && novo.getContinente() != null)
                        || (antigo.getContinente() != null && !antigo.getContinente().equals(novo.getContinente()))
                        || (antigo.getImagemNome() == null && novo.getImagemNome() != null)
                        || (antigo.getImagemNome() != null && !antigo.getImagemNome().equals(novo.getImagemNome()))) {
                    mudou = true;
                    break;
                }
            }

            if (!mudou) {
                return;
            }
        }

        destinosOriginais.clear();
        destinosOriginais.addAll(atualizados);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        String busca = textoBuscaAtual != null ? textoBuscaAtual.trim().toLowerCase() : "";

        SessionManager sessionManager = new SessionManager(this);
        FavoritosStorage favoritosStorage = new FavoritosStorage(this);
        Set<String> favoritos = sessionManager.estaLogado()
                ? favoritosStorage.carregarFavoritos(sessionManager.getEmailUsuario())
                : Collections.emptySet();

        destinosExibidos.clear();

        for (Destino destino : destinosOriginais) {
            String nome = destino.getNome() != null ? destino.getNome().toLowerCase() : "";
            String pais = destino.getPais() != null ? destino.getPais().toLowerCase() : "";
            String idioma = destino.getIdioma() != null ? destino.getIdioma().toLowerCase() : "";
            String continente = destino.getContinente() != null ? destino.getContinente().toLowerCase() : "";

            boolean correspondeBusca = busca.isEmpty()
                    || nome.contains(busca)
                    || pais.contains(busca)
                    || idioma.contains(busca)
                    || continente.contains(busca);

            boolean correspondeIdioma = filtroIdiomaSelecionado.equals("Todos")
                    || idioma.equalsIgnoreCase(filtroIdiomaSelecionado);

            boolean correspondeContinente = filtroContinenteSelecionado.equals("Todos")
                    || continente.equalsIgnoreCase(filtroContinenteSelecionado);

            boolean correspondePais = filtroPaisSelecionado.equals("Todos")
                    || pais.equalsIgnoreCase(filtroPaisSelecionado);

            boolean correspondeFavorito = !somenteFavoritos || favoritos.contains(destino.getId());

            if (correspondeBusca && correspondeIdioma && correspondeContinente
                    && correspondePais && correspondeFavorito) {
                destinosExibidos.add(destino);
            }
        }

        switch (filtroOrdenacaoSelecionado) {
            case "Mais avaliações":
                Collections.sort(destinosExibidos, (a, b) -> Integer.compare(b.getAvaliacoes(), a.getAvaliacoes()));
                break;

            case "Melhor nota":
            default:
                Collections.sort(destinosExibidos, (a, b) -> Float.compare(b.getNota(), a.getNota()));
                break;
        }

        adapter.notifyDataSetChanged();
        atualizarResultados();
    }

    private void atualizarResultados() {
        int quantidade = destinosExibidos.size();
        textoResultadosDestinos.setText(
                quantidade == 1 ? "1 resultado" : quantidade + " resultados"
        );
    }

    private void limparFiltros() {
        filtroIdiomaSelecionado = "Todos";
        filtroContinenteSelecionado = "Todos";
        filtroPaisSelecionado = "Todos";
        filtroOrdenacaoSelecionado = "Melhor nota";
        somenteFavoritos = false;
        textoBuscaAtual = "";

        EditText barraPesquisa = findViewById(R.id.barraPesquisa);
        barraPesquisa.setText("");

        aplicarFiltros();
    }

    private void abrirBottomSheetFiltros() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_filtros_destinos, null);
        dialog.setContentView(view);

        Spinner spinnerIdioma = view.findViewById(R.id.spinnerFiltroIdioma);
        Spinner spinnerContinente = view.findViewById(R.id.spinnerFiltroContinente);
        Spinner spinnerPais = view.findViewById(R.id.spinnerFiltroPais);
        Spinner spinnerOrdenacao = view.findViewById(R.id.spinnerFiltroOrdenacao);
        CheckBox checkSomenteFavoritos = view.findViewById(R.id.checkSomenteFavoritos);

        List<String> idiomas = criarListaIdiomas();
        List<String> continentes = criarListaContinentes();
        List<String> paises = criarListaPaises();
        List<String> ordenacoes = criarListaOrdenacoes();

        spinnerIdioma.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, idiomas));
        spinnerContinente.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, continentes));
        spinnerPais.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, paises));
        spinnerOrdenacao.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ordenacoes));

        spinnerIdioma.setSelection(Math.max(0, idiomas.indexOf(filtroIdiomaSelecionado)));
        spinnerContinente.setSelection(Math.max(0, continentes.indexOf(filtroContinenteSelecionado)));
        spinnerPais.setSelection(Math.max(0, paises.indexOf(filtroPaisSelecionado)));
        spinnerOrdenacao.setSelection(Math.max(0, ordenacoes.indexOf(filtroOrdenacaoSelecionado)));
        checkSomenteFavoritos.setChecked(somenteFavoritos);

        view.findViewById(R.id.botaoLimparFiltrosBottomSheet).setOnClickListener(v -> {
            limparFiltros();
            dialog.dismiss();
        });

        view.findViewById(R.id.botaoAplicarFiltrosBottomSheet).setOnClickListener(v -> {
            filtroIdiomaSelecionado = spinnerIdioma.getSelectedItem().toString();
            filtroContinenteSelecionado = spinnerContinente.getSelectedItem().toString();
            filtroPaisSelecionado = spinnerPais.getSelectedItem().toString();
            filtroOrdenacaoSelecionado = spinnerOrdenacao.getSelectedItem().toString();
            somenteFavoritos = checkSomenteFavoritos.isChecked();

            aplicarFiltros();
            dialog.dismiss();
        });

        dialog.show();
    }

    private List<String> criarListaIdiomas() {
        Set<String> unicos = new LinkedHashSet<>();
        unicos.add("Todos");

        for (Destino destino : destinosOriginais) {
            if (destino.getIdioma() != null && !destino.getIdioma().trim().isEmpty()) {
                unicos.add(destino.getIdioma());
            }
        }

        return new ArrayList<>(unicos);
    }

    private List<String> criarListaContinentes() {
        Set<String> unicos = new LinkedHashSet<>();
        unicos.add("Todos");

        for (Destino destino : destinosOriginais) {
            if (destino.getContinente() != null && !destino.getContinente().trim().isEmpty()) {
                unicos.add(destino.getContinente());
            }
        }

        return new ArrayList<>(unicos);
    }

    private List<String> criarListaPaises() {
        Set<String> unicos = new LinkedHashSet<>();
        unicos.add("Todos");

        for (Destino destino : destinosOriginais) {
            if (destino.getPais() != null && !destino.getPais().trim().isEmpty()) {
                unicos.add(destino.getPais());
            }
        }

        return new ArrayList<>(unicos);
    }

    private List<String> criarListaOrdenacoes() {
        List<String> lista = new ArrayList<>();
        lista.add("Melhor nota");
        lista.add("Mais avaliações");
        return lista;
    }
}