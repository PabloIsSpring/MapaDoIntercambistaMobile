package com.example.mapadointercambista.activity.destinos;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.adapter.destino.AvaliacaoDestinoAdapter;
import com.example.mapadointercambista.model.destino.AvaliacaoDestino;
import com.example.mapadointercambista.model.destino.Destino;
import com.example.mapadointercambista.model.destino.DestinoStorage;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.util.TimeUtils;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class DetalheDestinoActivity extends AppCompatActivity {

    private DestinoStorage destinoStorage;
    private SessionManager sessionManager;
    private Destino destinoAtual;

    private ImageView imagem;
    private ImageView botaoVoltar;
    private TextView nome;
    private TextView pais;
    private TextView idioma;
    private TextView moeda;
    private TextView descricao;
    private TextView textoResumoAvaliacoes;
    private RecyclerView listaAvaliacoes;
    private TextView textoVerMaisDescricao;
    private boolean descricaoExpandida = false;
    private String descricaoCompleta = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_destino);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        destinoStorage = new DestinoStorage(this);
        sessionManager = new SessionManager(this);

        Destino destinoRecebido = (Destino) getIntent().getSerializableExtra("destino");
        if (destinoRecebido != null) {
            destinoAtual = destinoStorage.buscarDestinoPorId(destinoRecebido.getId());

            if (destinoAtual == null) {
                destinoAtual = destinoRecebido;
            }
        }

        imagem = findViewById(R.id.imagemDestino);
        botaoVoltar = findViewById(R.id.botaoVoltarDestino);
        nome = findViewById(R.id.nomeDestino);
        pais = findViewById(R.id.textoPais);
        idioma = findViewById(R.id.textoIdioma);
        moeda = findViewById(R.id.textoMoeda);
        descricao = findViewById(R.id.textoDescricao);
        textoResumoAvaliacoes = findViewById(R.id.textoResumoAvaliacoesDestino);
        listaAvaliacoes = findViewById(R.id.listaAvaliacoesDestino);
        textoVerMaisDescricao = findViewById(R.id.textoVerMaisDescricao);

        listaAvaliacoes.setLayoutManager(new LinearLayoutManager(this));

        botaoVoltar.setOnClickListener(v -> finish());

        findViewById(R.id.botaoAvaliarDestino).setOnClickListener(v -> abrirDialogNovaAvaliacao());

        preencherCabecalho();
        carregarAvaliacoes();

        textoVerMaisDescricao.setOnClickListener(v -> {
            descricaoExpandida = !descricaoExpandida;

            if (descricaoExpandida) {
                descricao.setMaxLines(Integer.MAX_VALUE);
                textoVerMaisDescricao.setText("Ver menos");
            } else {
                descricao.setMaxLines(4);
                textoVerMaisDescricao.setText("Ver mais");
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_mundo);

        findViewById(R.id.containerAgencia1).setOnClickListener(v ->
                Toast.makeText(this, "Explore Abroad", Toast.LENGTH_SHORT).show());

        findViewById(R.id.containerAgencia2).setOnClickListener(v ->
                Toast.makeText(this, "Journey Hub", Toast.LENGTH_SHORT).show());

        findViewById(R.id.containerAgencia3).setOnClickListener(v ->
                Toast.makeText(this, "Gateway Exchange", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (destinoAtual != null) {
            Destino atualizado = destinoStorage.buscarDestinoPorId(destinoAtual.getId());
            if (atualizado != null) {
                destinoAtual = atualizado;
            }
        }

        preencherCabecalho();
        carregarAvaliacoes();
    }

    private void preencherCabecalho() {
        if (destinoAtual == null) {
            return;
        }

        imagem.setImageResource(destinoAtual.getImagem());
        nome.setText(destinoAtual.getNome());
        pais.setText(destinoAtual.getPais());
        idioma.setText(destinoAtual.getIdioma());
        moeda.setText(destinoAtual.getMoeda());
        descricaoCompleta = destinoAtual.getDescricao();
        descricao.setText(descricaoCompleta);

        descricao.post(() -> {
            if (descricao.getLineCount() > 4) {
                descricao.setMaxLines(4);
                textoVerMaisDescricao.setVisibility(View.VISIBLE);
                textoVerMaisDescricao.setText("Ver mais");
            } else {
                textoVerMaisDescricao.setVisibility(View.GONE);
            }
        });

        int quantidade = destinoAtual.getListaAvaliacoes() != null ? destinoAtual.getListaAvaliacoes().size() : 0;
        String notaMedia = String.format(java.util.Locale.US, "%.1f", destinoAtual.getNota());

        if (quantidade == 1) {
            textoResumoAvaliacoes.setText(notaMedia + " • 1 avaliação");
        } else {
            textoResumoAvaliacoes.setText(notaMedia + " • " + quantidade + " avaliações");
        }
    }

    private void carregarAvaliacoes() {
        List<AvaliacaoDestino> avaliacoes = new ArrayList<>();

        if (destinoAtual != null && destinoAtual.getListaAvaliacoes() != null) {
            avaliacoes = destinoAtual.getListaAvaliacoes();
        }

        AvaliacaoDestinoAdapter adapter = new AvaliacaoDestinoAdapter(
                this,
                destinoAtual != null ? destinoAtual.getId() : "",
                destinoAtual != null ? destinoAtual.getAgencias() : new ArrayList<>(),
                avaliacoes
        );

        listaAvaliacoes.setAdapter(adapter);
    }

    private void abrirDialogNovaAvaliacao() {
        if (!sessionManager.estaLogado()) {
            Toast.makeText(this, "Entre em sua conta para avaliar", Toast.LENGTH_SHORT).show();
            return;
        }

        if (destinoAtual == null) {
            Toast.makeText(this, "Destino não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_avaliacao_destino, null);

        EditText inputMensagem = view.findViewById(R.id.inputMensagemAvaliacao);
        RatingBar ratingBar = view.findViewById(R.id.ratingNovaAvaliacao);
        Spinner spinnerAgencia = view.findViewById(R.id.spinnerAgenciaAvaliacao);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                destinoAtual.getAgencias()
        );
        spinnerAgencia.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Nova avaliação")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Publicar", (dialog, which) -> {
                    String mensagem = inputMensagem.getText().toString().trim();
                    float nota = ratingBar.getRating();
                    String agencia = spinnerAgencia.getSelectedItem().toString();

                    if (mensagem.isEmpty()) {
                        Toast.makeText(this, "Digite sua avaliação", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (nota <= 0) {
                        Toast.makeText(this, "Selecione uma nota em estrelas", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AvaliacaoDestino novaAvaliacao = new AvaliacaoDestino(
                            sessionManager.getNomeUsuario(),
                            sessionManager.getEmailUsuario(),
                            sessionManager.getFotoUsuario(),
                            mensagem,
                            nota,
                            agencia,
                            TimeUtils.agora()
                    );

                    boolean sucesso = destinoStorage.adicionarAvaliacao(destinoAtual.getId(), novaAvaliacao);

                    if (sucesso) {
                        Destino atualizado = destinoStorage.buscarDestinoPorId(destinoAtual.getId());
                        if (atualizado != null) {
                            destinoAtual = atualizado;
                        }

                        preencherCabecalho();
                        carregarAvaliacoes();

                        Toast.makeText(this, "Avaliação publicada com sucesso", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro ao publicar avaliação", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}