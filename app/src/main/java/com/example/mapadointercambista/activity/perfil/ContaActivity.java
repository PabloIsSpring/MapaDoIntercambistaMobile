package com.example.mapadointercambista.activity.perfil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.auth.CadastroActivity;
import com.example.mapadointercambista.activity.auth.LoginActivity;
import com.example.mapadointercambista.adapter.destino.DestinoAdapter;
import com.example.mapadointercambista.model.destino.Destino;
import com.example.mapadointercambista.model.destino.DestinoStorage;
import com.example.mapadointercambista.model.destino.FavoritosStorage;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.util.AvatarUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ContaActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private FavoritosStorage favoritosStorage;
    private DestinoStorage destinoStorage;

    private ShapeableImageView imagemPerfilConta;
    private TextView textoNomeUsuarioConta;
    private TextView textoEmailUsuarioConta;
    private TextView textoAlterarFoto;
    private TextView textoResumoFavoritosConta;
    private TextView textoVazioFavoritosConta;
    private TextView textoTituloSecaoConfiguracoes;
    private TextView textoSubtituloHeroConta;

    private RecyclerView listaFavoritosConta;

    private MaterialButton botaoSairConta;
    private MaterialButton botaoEntrarConta;
    private MaterialButton botaoCriarConta;

    private View secaoAcoesVisitante;
    private View secaoAcoesLogado;
    private View cardVisitanteConta;
    private View cardFavoritosConta;
    private View blocoConfiguracoesConta;

    private LinearLayout atalhoFavoritos;
    private LinearLayout atalhoConversas;
    private LinearLayout atalhoEditarPerfil;

    private LinearLayout itemIdioma;
    private LinearLayout itemNotificacoes;
    private LinearLayout itemLocalizacao;
    private LinearLayout itemAcessibilidade;
    private LinearLayout itemPrivacidade;
    private LinearLayout itemSeguranca;
    private LinearLayout itemSuporte;
    private LinearLayout itemTermos;
    private LinearLayout itemAdicionarConta;

    private DestinoAdapter favoritosAdapter;
    private final List<Destino> favoritosExibidos = new ArrayList<>();

    private ActivityResultLauncher<Intent> launcherGaleria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conta);

        sessionManager = new SessionManager(this);
        favoritosStorage = new FavoritosStorage(this);
        destinoStorage = new DestinoStorage(this);

        aplicarModoImersivo();
        configurarLauncherGaleria();
        inicializarViews();
        configurarListaFavoritos();
        configurarAcoes();
        atualizarInterface();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_perfil);
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarModoImersivo();
        atualizarInterface();
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

    private void configurarLauncherGaleria() {
        launcherGaleria = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        return;
                    }

                    Uri uriSelecionada = result.getData().getData();
                    if (uriSelecionada == null) {
                        Toast.makeText(this, "Não foi possível obter a imagem", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        salvarPermissaoUri(uriSelecionada);
                        sessionManager.salvarFotoUsuario(uriSelecionada.toString());
                        preencherDadosUsuario();
                        Toast.makeText(this, "Foto atualizada com sucesso", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao salvar a foto", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void inicializarViews() {
        imagemPerfilConta = findViewById(R.id.imagemPerfilConta);
        textoNomeUsuarioConta = findViewById(R.id.textoNomeUsuarioConta);
        textoEmailUsuarioConta = findViewById(R.id.textoEmailUsuarioConta);
        textoAlterarFoto = findViewById(R.id.textoAlterarFoto);
        textoResumoFavoritosConta = findViewById(R.id.textoResumoFavoritosConta);
        textoVazioFavoritosConta = findViewById(R.id.textoVazioFavoritosConta);
        textoTituloSecaoConfiguracoes = findViewById(R.id.textoTituloSecaoConfiguracoes);
        textoSubtituloHeroConta = findViewById(R.id.textoSubtituloHeroConta);

        listaFavoritosConta = findViewById(R.id.listaFavoritosConta);

        botaoSairConta = findViewById(R.id.botaoSairConta);
        botaoEntrarConta = findViewById(R.id.botaoEntrarConta);
        botaoCriarConta = findViewById(R.id.botaoCriarConta);

        secaoAcoesVisitante = findViewById(R.id.secaoAcoesVisitante);
        secaoAcoesLogado = findViewById(R.id.secaoAcoesLogado);
        cardVisitanteConta = findViewById(R.id.cardVisitanteConta);
        cardFavoritosConta = findViewById(R.id.cardFavoritosConta);
        blocoConfiguracoesConta = findViewById(R.id.blocoConfiguracoesConta);

        atalhoFavoritos = findViewById(R.id.atalhoFavoritos);
        atalhoConversas = findViewById(R.id.atalhoConversas);
        atalhoEditarPerfil = findViewById(R.id.atalhoEditarPerfil);

        itemIdioma = findViewById(R.id.itemIdioma);
        itemNotificacoes = findViewById(R.id.itemNotificacoes);
        itemLocalizacao = findViewById(R.id.itemLocalizacao);
        itemAcessibilidade = findViewById(R.id.itemAcessibilidade);
        itemPrivacidade = findViewById(R.id.itemPrivacidade);
        itemSeguranca = findViewById(R.id.itemSeguranca);
        itemSuporte = findViewById(R.id.itemSuporte);
        itemTermos = findViewById(R.id.itemTermos);
        itemAdicionarConta = findViewById(R.id.itemAdicionarConta);
    }

    private void configurarAcoes() {
        imagemPerfilConta.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em uma conta para alterar a foto", Toast.LENGTH_SHORT).show();
                return;
            }
            selecionarFoto();
        });

        textoAlterarFoto.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em uma conta para alterar a foto", Toast.LENGTH_SHORT).show();
                return;
            }
            selecionarFoto();
        });

        botaoEntrarConta.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        botaoCriarConta.setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class))
        );

        botaoSairConta.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Você saiu da conta", Toast.LENGTH_SHORT).show();
            atualizarInterface();
        });

        atalhoFavoritos.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em uma conta para ver seus favoritos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (favoritosExibidos.isEmpty()) {
                Toast.makeText(this, "Você ainda não favoritou nenhum destino", Toast.LENGTH_SHORT).show();
            } else {
                listaFavoritosConta.smoothScrollToPosition(0);
                Toast.makeText(this, "Seus favoritos estão logo abaixo", Toast.LENGTH_SHORT).show();
            }
        });

        atalhoConversas.setOnClickListener(v ->
                Toast.makeText(this, "Suas conversas ficarão disponíveis em breve", Toast.LENGTH_SHORT).show()
        );

        atalhoEditarPerfil.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em uma conta para editar seu perfil", Toast.LENGTH_SHORT).show();
                return;
            }
            selecionarFoto();
        });

        itemIdioma.setOnClickListener(v ->
                Toast.makeText(this, "Idioma: Português - Brasil", Toast.LENGTH_SHORT).show()
        );

        itemNotificacoes.setOnClickListener(v ->
                Toast.makeText(this, "Configuração de notificações em breve", Toast.LENGTH_SHORT).show()
        );

        itemLocalizacao.setOnClickListener(v ->
                Toast.makeText(this, "Configuração de localização em breve", Toast.LENGTH_SHORT).show()
        );

        itemAcessibilidade.setOnClickListener(v ->
                Toast.makeText(this, "Opções de acessibilidade em breve", Toast.LENGTH_SHORT).show()
        );

        itemPrivacidade.setOnClickListener(v ->
                Toast.makeText(this, "Políticas de privacidade em breve", Toast.LENGTH_SHORT).show()
        );

        itemSeguranca.setOnClickListener(v ->
                Toast.makeText(this, "Configurações de segurança em breve", Toast.LENGTH_SHORT).show()
        );

        itemSuporte.setOnClickListener(v ->
                Toast.makeText(this, "Central de suporte em breve", Toast.LENGTH_SHORT).show()
        );

        itemTermos.setOnClickListener(v ->
                Toast.makeText(this, "Termos de uso em breve", Toast.LENGTH_SHORT).show()
        );

        itemAdicionarConta.setOnClickListener(v ->
                Toast.makeText(this, "Suporte para múltiplas contas em breve", Toast.LENGTH_SHORT).show()
        );
    }

    private void configurarListaFavoritos() {
        listaFavoritosConta.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        listaFavoritosConta.setHasFixedSize(true);
        listaFavoritosConta.setItemViewCacheSize(6);
        listaFavoritosConta.setItemAnimator(null);

        favoritosAdapter = new DestinoAdapter(favoritosExibidos);
        listaFavoritosConta.setAdapter(favoritosAdapter);
    }

    private void atualizarInterface() {
        if (sessionManager.sessaoApiExpirada()) {
            sessionManager.logout();
        }

        if (sessionManager.estaLogado()) {
            aplicarEstadoLogado();
        } else {
            aplicarEstadoVisitante();
        }
    }

    private void aplicarEstadoLogado() {
        secaoAcoesVisitante.setVisibility(View.GONE);
        secaoAcoesLogado.setVisibility(View.VISIBLE);
        cardVisitanteConta.setVisibility(View.GONE);
        cardFavoritosConta.setVisibility(View.VISIBLE);
        blocoConfiguracoesConta.setVisibility(View.VISIBLE);

        textoTituloSecaoConfiguracoes.setText("Configurações");
        textoSubtituloHeroConta.setText("Carteirinha do Intercambista");

        preencherDadosUsuario();
        carregarFavoritos();
    }

    private void aplicarEstadoVisitante() {
        secaoAcoesVisitante.setVisibility(View.VISIBLE);
        secaoAcoesLogado.setVisibility(View.GONE);
        cardVisitanteConta.setVisibility(View.VISIBLE);
        cardFavoritosConta.setVisibility(View.GONE);
        blocoConfiguracoesConta.setVisibility(View.VISIBLE);

        textoNomeUsuarioConta.setText("Visitante");
        textoEmailUsuarioConta.setText("Explore o app livremente e entre em uma conta para interagir");
        textoAlterarFoto.setText("Faça login para personalizar seu perfil");
        textoSubtituloHeroConta.setText("Mapa do Intercambista");
        textoTituloSecaoConfiguracoes.setText("Explorar e configurações");

        Glide.with(this).clear(imagemPerfilConta);
        imagemPerfilConta.setImageDrawable(null);
        imagemPerfilConta.setImageTintList(null);

        Bitmap avatar = AvatarUtils.criarAvatarComInicial(this, "Visitante", 120);
        imagemPerfilConta.setImageBitmap(avatar);

        favoritosExibidos.clear();
        favoritosAdapter.notifyDataSetChanged();
    }

    private void selecionarFoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        launcherGaleria.launch(intent);
    }

    private void salvarPermissaoUri(Uri uri) {
        try {
            final int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(uri, flags);
        } catch (SecurityException ignored) {
        }
    }

    private void preencherDadosUsuario() {
        String nome = sessionManager.getNomeUsuario();
        String email = sessionManager.getEmailUsuario();
        String fotoUri = sessionManager.getFotoUsuario();

        textoNomeUsuarioConta.setText(nome != null && !nome.isEmpty() ? nome : "Usuário");
        textoEmailUsuarioConta.setText(email != null ? email : "");
        textoAlterarFoto.setText("Toque para alterar a foto");

        Glide.with(this).clear(imagemPerfilConta);
        imagemPerfilConta.setImageDrawable(null);
        imagemPerfilConta.setImageTintList(null);

        if (fotoUri != null && !fotoUri.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(fotoUri))
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .override(240, 240)
                    .thumbnail(0.25f)
                    .circleCrop()
                    .dontAnimate()
                    .into(imagemPerfilConta);
        } else {
            Bitmap avatar = AvatarUtils.criarAvatarComInicial(this, nome, 120);
            imagemPerfilConta.setImageBitmap(avatar);
        }
    }

    private void carregarFavoritos() {
        favoritosExibidos.clear();

        String emailUsuario = sessionManager.getEmailUsuario();
        Set<String> favoritosIds = favoritosStorage.carregarFavoritos(emailUsuario);
        List<Destino> todosDestinos = destinoStorage.carregarDestinos();

        for (Destino destino : todosDestinos) {
            if (favoritosIds.contains(destino.getId())) {
                favoritosExibidos.add(destino);
            }
        }

        favoritosAdapter.notifyItemRangeChanged(0, favoritosExibidos.size());

        int quantidade = favoritosExibidos.size();

        if (quantidade == 0) {
            textoResumoFavoritosConta.setText("Seus destinos favoritos aparecerão aqui");
            textoVazioFavoritosConta.setVisibility(View.VISIBLE);
            listaFavoritosConta.setVisibility(View.GONE);
        } else {
            textoResumoFavoritosConta.setText(
                    quantidade == 1 ? "1 destino favoritado" : quantidade + " destinos favoritados"
            );
            textoVazioFavoritosConta.setVisibility(View.GONE);
            listaFavoritosConta.setVisibility(View.VISIBLE);
        }
    }
}