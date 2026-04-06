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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.adapter.destino.AvaliacaoDestinoAdapter;
import com.example.mapadointercambista.model.destino.AvaliacaoDestino;
import com.example.mapadointercambista.model.destino.Destino;
import com.example.mapadointercambista.model.destino.DestinoStorage;
import com.example.mapadointercambista.model.destino.FavoritosStorage;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.util.InputSecurityUtils;
import com.example.mapadointercambista.util.ImageUtils;
import com.example.mapadointercambista.util.TimeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private final List<AvaliacaoDestino> avaliacoesExibidas = new ArrayList<>();
    private AvaliacaoDestinoAdapter avaliacaoAdapter;
    private int ultimoHashDestino = -1;

    private ImageView botaoFavoritoDestino;
    private FavoritosStorage favoritosStorage;
    private MaterialButton botaoAvaliarDestino;

    private boolean alterandoFavorito = false;
    private boolean publicandoAvaliacao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhe_destino);

        aplicarModoImersivo();

        destinoStorage = new DestinoStorage(this);
        sessionManager = new SessionManager(this);
        favoritosStorage = new FavoritosStorage(this);

        Destino destinoRecebido = (Destino) getIntent().getSerializableExtra("destino");
        if (destinoRecebido != null && destinoRecebido.getId() != null) {
            destinoAtual = destinoStorage.buscarDestinoPorId(destinoRecebido.getId());
            if (destinoAtual == null) {
                destinoAtual = destinoRecebido;
            }
        }

        if (destinoAtual == null) {
            Toast.makeText(this, "Destino não encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
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
        botaoFavoritoDestino = findViewById(R.id.botaoFavoritoDestino);
        botaoAvaliarDestino = findViewById(R.id.botaoAvaliarDestino);

        listaAvaliacoes.setLayoutManager(new LinearLayoutManager(this));
        listaAvaliacoes.setHasFixedSize(false);
        listaAvaliacoes.setItemViewCacheSize(8);
        listaAvaliacoes.setItemAnimator(null);

        avaliacaoAdapter = new AvaliacaoDestinoAdapter(
                this,
                destinoAtual.getId(),
                destinoAtual.getAgencias() != null ? destinoAtual.getAgencias() : new ArrayList<>(),
                avaliacoesExibidas
        );
        listaAvaliacoes.setAdapter(avaliacaoAdapter);

        botaoVoltar.setOnClickListener(v -> finish());

        botaoFavoritoDestino.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em sua conta para favoritar.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (destinoAtual == null || alterandoFavorito) {
                return;
            }

            alterandoFavorito = true;
            v.setEnabled(false);

            boolean agoraFavorito = favoritosStorage.toggleFavorito(
                    sessionManager.getEmailUsuario(),
                    destinoAtual.getId()
            );

            atualizarBotaoFavorito(agoraFavorito);

            Toast.makeText(
                    this,
                    agoraFavorito ? "Destino adicionado aos favoritos." : "Destino removido dos favoritos.",
                    Toast.LENGTH_SHORT
            ).show();

            v.postDelayed(() -> {
                alterandoFavorito = false;
                v.setEnabled(true);
            }, 250);
        });

        botaoAvaliarDestino.setOnClickListener(v -> abrirDialogNovaAvaliacao());

        preencherCabecalho();
        carregarAvaliacoes();
        ultimoHashDestino = calcularHashDestinoAtual();

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
        aplicarModoImersivo();

        if (destinoAtual != null) {
            Destino atualizado = destinoStorage.buscarDestinoPorId(destinoAtual.getId());
            if (atualizado != null) {
                destinoAtual = atualizado;
            }
        }

        int hashAtual = calcularHashDestinoAtual();
        if (hashAtual != ultimoHashDestino) {
            preencherCabecalho();
            carregarAvaliacoes();
            ultimoHashDestino = hashAtual;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            aplicarModoImersivo();
        }
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

    private void atualizarBotaoFavorito(boolean favorito) {
        botaoFavoritoDestino.setColorFilter(ContextCompat.getColor(
                this,
                favorito ? R.color.favorite_active : R.color.white
        ));
    }

    private int calcularHashDestinoAtual() {
        if (destinoAtual == null) return -1;

        int hash = destinoAtual.getId() != null ? destinoAtual.getId().hashCode() : 0;
        hash = 31 * hash + Float.floatToIntBits(destinoAtual.getNota());
        hash = 31 * hash + destinoAtual.getAvaliacoes();
        hash = 31 * hash + (destinoAtual.getDescricao() != null ? destinoAtual.getDescricao().hashCode() : 0);
        return hash;
    }

    private void preencherCabecalho() {
        if (destinoAtual == null) {
            return;
        }

        int drawableId = ImageUtils.getDrawableId(this, destinoAtual.getImagemNome());
        imagem.setImageResource(drawableId != 0 ? drawableId : R.drawable.ic_world);

        nome.setText(textoSeguro(destinoAtual.getNome(), "Destino"));
        pais.setText(textoSeguro(destinoAtual.getPais(), "-"));
        idioma.setText(textoSeguro(destinoAtual.getIdioma(), "-"));
        moeda.setText(textoSeguro(destinoAtual.getMoeda(), "-"));

        descricaoCompleta = textoSeguro(destinoAtual.getDescricao(), "");
        descricao.setText(descricaoCompleta);

        descricao.post(() -> {
            if (descricao.getLineCount() > 4) {
                descricao.setMaxLines(descricaoExpandida ? Integer.MAX_VALUE : 4);
                textoVerMaisDescricao.setVisibility(View.VISIBLE);
                textoVerMaisDescricao.setText(descricaoExpandida ? "Ver menos" : "Ver mais");
            } else {
                textoVerMaisDescricao.setVisibility(View.GONE);
            }
        });

        int quantidade = destinoAtual.getListaAvaliacoes() != null ? destinoAtual.getListaAvaliacoes().size() : 0;
        String notaMedia = String.format(Locale.getDefault(), "%.1f", destinoAtual.getNota());

        textoResumoAvaliacoes.setText(
                quantidade == 1
                        ? notaMedia + " • 1 avaliação"
                        : notaMedia + " • " + quantidade + " avaliações"
        );

        if (sessionManager.estaLogado()) {
            boolean favorito = favoritosStorage.isFavorito(
                    sessionManager.getEmailUsuario(),
                    destinoAtual.getId()
            );
            atualizarBotaoFavorito(favorito);
        } else {
            atualizarBotaoFavorito(false);
        }
    }

    private void carregarAvaliacoes() {
        avaliacoesExibidas.clear();

        if (destinoAtual != null && destinoAtual.getListaAvaliacoes() != null) {
            avaliacoesExibidas.addAll(destinoAtual.getListaAvaliacoes());
        }

        avaliacaoAdapter.notifyDataSetChanged();
    }

    private void abrirDialogNovaAvaliacao() {
        if (!sessionManager.estaLogado()) {
            Toast.makeText(this, "Entre em sua conta para avaliar.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (destinoAtual == null || publicandoAvaliacao) {
            Toast.makeText(this, "Destino não encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_avaliacao_destino, null);

        EditText inputMensagem = view.findViewById(R.id.inputMensagemAvaliacao);
        RatingBar ratingBar = view.findViewById(R.id.ratingNovaAvaliacao);
        Spinner spinnerAgencia = view.findViewById(R.id.spinnerAgenciaAvaliacao);

        List<String> agencias = destinoAtual.getAgencias() != null
                ? destinoAtual.getAgencias()
                : new ArrayList<>();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                agencias
        );
        spinnerAgencia.setAdapter(spinnerAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Nova avaliação")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Publicar", (dialog, which) -> {
                    if (publicandoAvaliacao) {
                        return;
                    }

                    String mensagem = InputSecurityUtils.sanitizeUserText(
                            inputMensagem.getText().toString()
                    );
                    float nota = ratingBar.getRating();
                    String agencia = spinnerAgencia.getSelectedItem() != null
                            ? spinnerAgencia.getSelectedItem().toString()
                            : "";

                    if (InputSecurityUtils.isNullOrBlank(mensagem)) {
                        Toast.makeText(this, "Digite sua avaliação.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (InputSecurityUtils.containsSuspiciousPattern(mensagem)) {
                        Toast.makeText(this, "Conteúdo inválido detectado.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (nota <= 0) {
                        Toast.makeText(this, "Selecione uma nota em estrelas.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (InputSecurityUtils.isNullOrBlank(agencia)) {
                        Toast.makeText(this, "Selecione uma agência.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    publicandoAvaliacao = true;
                    botaoAvaliarDestino.setEnabled(false);

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

                        Toast.makeText(this, "Avaliação publicada com sucesso.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro ao publicar avaliação.", Toast.LENGTH_SHORT).show();
                    }

                    botaoAvaliarDestino.postDelayed(() -> {
                        publicandoAvaliacao = false;
                        botaoAvaliarDestino.setEnabled(true);
                    }, 300);
                })
                .show();
    }

    private String textoSeguro(String valor, String fallback) {
        String texto = InputSecurityUtils.sanitizeUserText(valor != null ? valor : "");
        return texto.isEmpty() ? fallback : texto;
    }
}