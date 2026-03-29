package com.example.mapadointercambista.activity.perfil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
    private TextView textoNomeCardConta;
    private TextView textoEmailCardConta;
    private TextView textoResumoFavoritosConta;
    private TextView textoVazioFavoritosConta;
    private TextView textoAlterarFoto;

    private RecyclerView listaFavoritosConta;

    private MaterialButton botaoSairConta;
    private MaterialButton botaoEntrarConta;
    private MaterialButton botaoCriarConta;

    private View cardInfoConta;
    private View cardFavoritosConta;
    private View cardVisitanteConta;
    private View secaoAcoesVisitante;
    private View secaoAcoesLogado;

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
        textoNomeCardConta = findViewById(R.id.textoNomeCardConta);
        textoEmailCardConta = findViewById(R.id.textoEmailCardConta);
        textoResumoFavoritosConta = findViewById(R.id.textoResumoFavoritosConta);
        textoVazioFavoritosConta = findViewById(R.id.textoVazioFavoritosConta);
        textoAlterarFoto = findViewById(R.id.textoAlterarFoto);

        listaFavoritosConta = findViewById(R.id.listaFavoritosConta);

        botaoSairConta = findViewById(R.id.botaoSairConta);
        botaoEntrarConta = findViewById(R.id.botaoEntrarConta);
        botaoCriarConta = findViewById(R.id.botaoCriarConta);

        cardInfoConta = findViewById(R.id.cardInfoConta);
        cardFavoritosConta = findViewById(R.id.cardFavoritosConta);
        cardVisitanteConta = findViewById(R.id.cardVisitanteConta);
        secaoAcoesVisitante = findViewById(R.id.secaoAcoesVisitante);
        secaoAcoesLogado = findViewById(R.id.secaoAcoesLogado);
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
        cardInfoConta.setVisibility(View.VISIBLE);
        cardFavoritosConta.setVisibility(View.VISIBLE);
        cardVisitanteConta.setVisibility(View.GONE);

        preencherDadosUsuario();
        carregarFavoritos();
    }

    private void aplicarEstadoVisitante() {
        secaoAcoesVisitante.setVisibility(View.VISIBLE);
        secaoAcoesLogado.setVisibility(View.GONE);
        cardInfoConta.setVisibility(View.GONE);
        cardFavoritosConta.setVisibility(View.GONE);
        cardVisitanteConta.setVisibility(View.VISIBLE);

        textoNomeUsuarioConta.setText("Visitante");
        textoEmailUsuarioConta.setText("Entre em uma conta para interagir, responder e favoritar destinos");
        textoAlterarFoto.setText("Faça login para personalizar seu perfil");

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
        textoNomeCardConta.setText(nome != null && !nome.isEmpty() ? nome : "Usuário");
        textoEmailCardConta.setText(email != null ? email : "");
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

        favoritosAdapter.notifyDataSetChanged();

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