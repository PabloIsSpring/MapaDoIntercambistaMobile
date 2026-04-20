package com.example.mapadointercambista.activity.perfil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
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
import com.example.mapadointercambista.dto.request.IntercambistaUpdtRequestDto;
import com.example.mapadointercambista.dto.response.IntercambistaResponseDto;
import com.example.mapadointercambista.model.destino.AvaliacaoDestino;
import com.example.mapadointercambista.model.destino.Destino;
import com.example.mapadointercambista.model.destino.DestinoStorage;
import com.example.mapadointercambista.model.destino.FavoritosStorage;
import com.example.mapadointercambista.model.forum.ForumStorage;
import com.example.mapadointercambista.model.forum.PostForum;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.network.ApiClient;
import com.example.mapadointercambista.network.ApiService;
import com.example.mapadointercambista.util.AnimationUtils;
import com.example.mapadointercambista.util.AvatarUtils;
import com.example.mapadointercambista.util.TransitionHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContaActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private FavoritosStorage favoritosStorage;
    private DestinoStorage destinoStorage;
    private ForumStorage forumStorage;

    private ShapeableImageView imagemPerfilConta;
    private TextView textoNomeUsuarioConta;
    private TextView textoEmailUsuarioConta;
    private TextView textoAlterarFoto;
    private TextView textoResumoFavoritosConta;
    private TextView textoVazioFavoritosConta;
    private TextView textoTituloSecaoConfiguracoes;
    private TextView textoSubtituloHeroConta;

    private TextView textoUsernameConta;
    private TextView textoIdadeConta;

    private RecyclerView listaFavoritosConta;

    private MaterialButton botaoSairConta;
    private MaterialButton botaoEntrarConta;
    private MaterialButton botaoCriarConta;

    private View secaoAcoesVisitante;
    private View secaoAcoesLogado;
    private View cardVisitanteConta;
    private View cardFavoritosConta;
    private View blocoConfiguracoesConta;
    private View cardInformacoesPerfilConta;

    private LinearLayout atalhoFavoritos;
    private LinearLayout atalhoConversas;
    private LinearLayout atalhoEditarPerfil;

    private LinearLayout itemAbrirConfiguracoes;

    private DestinoAdapter favoritosAdapter;
    private final List<Destino> favoritosExibidos = new ArrayList<>();
    private TextView textoNumeroFavoritosConta;
    private TextView textoNumeroPostsConta;
    private TextView textoNumeroAvaliacoesConta;
    private ActivityResultLauncher<Intent> launcherGaleria;

    private boolean sincronizandoPerfil = false;
    private boolean atualizandoUsername = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conta);

        sessionManager = new SessionManager(this);
        favoritosStorage = new FavoritosStorage(this);
        destinoStorage = new DestinoStorage(this);
        forumStorage = new ForumStorage(this);

        aplicarModoImersivo();
        configurarLauncherGaleria();
        inicializarViews();
        configurarListaFavoritos();
        configurarAcoes();
        aplicarMicrointeracoesConta();
        atualizarInterface();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_perfil);
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarModoImersivo();
        atualizarInterface();
        tentarSincronizarPerfilApi();
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

        textoUsernameConta = findViewById(R.id.textoUsernameConta);
        textoIdadeConta = findViewById(R.id.textoIdadeConta);

        listaFavoritosConta = findViewById(R.id.listaFavoritosConta);

        botaoSairConta = findViewById(R.id.botaoSairConta);
        botaoEntrarConta = findViewById(R.id.botaoEntrarConta);
        botaoCriarConta = findViewById(R.id.botaoCriarConta);

        secaoAcoesVisitante = findViewById(R.id.secaoAcoesVisitante);
        secaoAcoesLogado = findViewById(R.id.secaoAcoesLogado);
        cardVisitanteConta = findViewById(R.id.cardVisitanteConta);
        cardFavoritosConta = findViewById(R.id.cardFavoritosConta);
        blocoConfiguracoesConta = findViewById(R.id.blocoConfiguracoesConta);
        cardInformacoesPerfilConta = findViewById(R.id.cardInformacoesPerfilConta);

        atalhoFavoritos = findViewById(R.id.atalhoFavoritos);
        atalhoConversas = findViewById(R.id.atalhoConversas);
        atalhoEditarPerfil = findViewById(R.id.atalhoEditarPerfil);

        itemAbrirConfiguracoes = findViewById(R.id.itemAbrirConfiguracoes);

        textoNumeroFavoritosConta = findViewById(R.id.textoNumeroFavoritosConta);
        textoNumeroPostsConta = findViewById(R.id.textoNumeroPostsConta);
        textoNumeroAvaliacoesConta = findViewById(R.id.textoNumeroAvaliacoesConta);
    }

    private void configurarAcoes() {
        imagemPerfilConta.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em uma conta para alterar a foto", Toast.LENGTH_SHORT).show();
                return;
            }
            AnimationUtils.playBounce(v);
            selecionarFoto();
        });

        textoAlterarFoto.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em uma conta para alterar a foto", Toast.LENGTH_SHORT).show();
                return;
            }
            AnimationUtils.playBounce(v);
            selecionarFoto();
        });

        botaoEntrarConta.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            TransitionHelper.slideForward(this);
        });

        botaoCriarConta.setOnClickListener(v -> {
            startActivity(new Intent(this, CadastroActivity.class));
            TransitionHelper.slideForward(this);
        });

        botaoSairConta.setOnClickListener(v -> {
            AnimationUtils.playBounce(v);
            sessionManager.logout();
            Toast.makeText(this, "Você saiu da conta", Toast.LENGTH_SHORT).show();
            atualizarInterface();
        });

        atalhoFavoritos.setOnClickListener(v -> {
            if (!sessionManager.estaLogado()) {
                Toast.makeText(this, "Entre em uma conta para ver seus favoritos", Toast.LENGTH_SHORT).show();
                return;
            }

            AnimationUtils.playBounce(v);

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

            AnimationUtils.playBounce(v);
            abrirDialogEditarUsername();
        });

        itemAbrirConfiguracoes.setOnClickListener(v -> {
            AnimationUtils.playBounce(v);
            startActivity(new Intent(this, ConfiguracoesActivity.class));
            TransitionHelper.slideForward(this);
        });
    }

    private void aplicarMicrointeracoesConta() {
        AnimationUtils.applyPressAnimation(imagemPerfilConta);
        AnimationUtils.applyPressAnimation(textoAlterarFoto);

        AnimationUtils.applyPressAnimation(botaoEntrarConta);
        AnimationUtils.applyPressAnimation(botaoCriarConta);
        AnimationUtils.applyPressAnimation(botaoSairConta);

        AnimationUtils.applyPressAnimation(atalhoFavoritos);
        AnimationUtils.applyPressAnimation(atalhoConversas);
        AnimationUtils.applyPressAnimation(atalhoEditarPerfil);
        AnimationUtils.applyPressAnimation(itemAbrirConfiguracoes);
    }

    private void configurarListaFavoritos() {
        listaFavoritosConta.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        listaFavoritosConta.setHasFixedSize(true);
        listaFavoritosConta.setItemViewCacheSize(6);
        listaFavoritosConta.setItemAnimator(null);

        favoritosAdapter = new DestinoAdapter(this, favoritosExibidos);
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
        cardInformacoesPerfilConta.setVisibility(View.VISIBLE);

        textoTituloSecaoConfiguracoes.setText("Configurações");
        textoSubtituloHeroConta.setText(obterSubtituloHeroLogado());
        textoAlterarFoto.setText("Alterar foto de perfil");
        botaoSairConta.setVisibility(View.VISIBLE);

        preencherDadosUsuario();
        carregarFavoritos();
        atualizarResumoDoUsuario();
    }

    private void aplicarEstadoVisitante() {
        secaoAcoesVisitante.setVisibility(View.VISIBLE);
        secaoAcoesLogado.setVisibility(View.GONE);
        cardVisitanteConta.setVisibility(View.VISIBLE);
        cardFavoritosConta.setVisibility(View.GONE);
        blocoConfiguracoesConta.setVisibility(View.VISIBLE);
        cardInformacoesPerfilConta.setVisibility(View.GONE);

        textoNomeUsuarioConta.setText("Visitante");
        textoEmailUsuarioConta.setText("Entre para salvar favoritos, participar do fórum e personalizar seu perfil");
        textoAlterarFoto.setText("Faça login para personalizar seu perfil");
        textoSubtituloHeroConta.setText("Seu espaço no Mapa do Intercambista");
        textoTituloSecaoConfiguracoes.setText("Configurações");
        botaoSairConta.setVisibility(View.GONE);

        Glide.with(this).clear(imagemPerfilConta);
        imagemPerfilConta.setImageDrawable(null);
        imagemPerfilConta.setImageTintList(null);

        Bitmap avatar = AvatarUtils.criarAvatarComInicial(this, "Visitante", 120);
        imagemPerfilConta.setImageBitmap(avatar);

        textoNumeroFavoritosConta.setText("0");
        textoNumeroPostsConta.setText("0");
        textoNumeroAvaliacoesConta.setText("0");

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
        String nomeCompleto = sessionManager.getNomeUsuario();
        String email = sessionManager.getEmailUsuario();
        String fotoUri = sessionManager.getFotoUsuario();
        String username = sessionManager.getUsernameUsuario();
        int idade = sessionManager.getIdadeUsuario();

        textoNomeUsuarioConta.setText(nomeCompleto);

        StringBuilder descricao = new StringBuilder();
        if (email != null && !email.isEmpty()) {
            descricao.append(email);
        }

        if (username != null && !username.isEmpty()) {
            if (descricao.length() > 0) {
                descricao.append("\n");
            }
            descricao.append("@").append(username);
        }

        textoEmailUsuarioConta.setText(descricao.length() > 0 ? descricao.toString() : "Perfil ativo");
        textoAlterarFoto.setText("Alterar foto de perfil");

        textoUsernameConta.setText(
                username != null && !username.isEmpty() ? "@" + username : "Não informado"
        );

        textoIdadeConta.setText(
                idade > 0 ? idade + " anos" : "Não informado"
        );

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
            Bitmap avatar = AvatarUtils.criarAvatarComInicial(this, nomeCompleto, 120);
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
        textoNumeroFavoritosConta.setText(String.valueOf(quantidade));

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

    private void atualizarResumoDoUsuario() {
        String emailUsuario = sessionManager.getEmailUsuario();
        if (emailUsuario == null || emailUsuario.trim().isEmpty()) {
            textoNumeroPostsConta.setText("0");
            textoNumeroAvaliacoesConta.setText("0");
            return;
        }

        int quantidadePosts = 0;
        List<PostForum> posts = forumStorage.carregarPosts();
        for (PostForum post : posts) {
            if (post != null
                    && post.getAutorEmail() != null
                    && post.getAutorEmail().equalsIgnoreCase(emailUsuario)) {
                quantidadePosts++;
            }
        }

        int quantidadeAvaliacoes = 0;
        List<Destino> destinos = destinoStorage.carregarDestinos();
        for (Destino destino : destinos) {
            if (destino == null || destino.getListaAvaliacoes() == null) {
                continue;
            }

            for (AvaliacaoDestino avaliacao : destino.getListaAvaliacoes()) {
                if (avaliacao != null
                        && avaliacao.getAutorEmail() != null
                        && avaliacao.getAutorEmail().equalsIgnoreCase(emailUsuario)) {
                    quantidadeAvaliacoes++;
                }
            }
        }

        textoNumeroPostsConta.setText(String.valueOf(quantidadePosts));
        textoNumeroAvaliacoesConta.setText(String.valueOf(quantidadeAvaliacoes));
    }

    private String obterSubtituloHeroLogado() {
        String username = sessionManager.getUsernameUsuario();
        if (username != null && !username.isEmpty()) {
            return "Seu perfil • @" + username;
        }
        return "Seu perfil no Mapa do Intercambista";
    }

    private void tentarSincronizarPerfilApi() {
        if (!sessionManager.estaLogado() || !sessionManager.isModoApi()) {
            return;
        }

        String username = sessionManager.getUsernameUsuario();
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        if (sincronizandoPerfil) {
            return;
        }

        sincronizandoPerfil = true;

        ApiService apiService = ApiClient.getApiService(this);
        apiService.getIntercambista(username).enqueue(new Callback<IntercambistaResponseDto>() {
            @Override
            public void onResponse(Call<IntercambistaResponseDto> call, Response<IntercambistaResponseDto> response) {
                sincronizandoPerfil = false;

                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                IntercambistaResponseDto body = response.body();

                String usernameRemoto = body.getUsername() != null ? body.getUsername() : username;
                String nomeRemoto = body.getNome() != null ? body.getNome() : sessionManager.getPrimeiroNomeUsuario();
                int idadeRemota = body.getIdade();

                sessionManager.salvarPerfilApi(
                        nomeRemoto,
                        sessionManager.getEmailUsuario(),
                        usernameRemoto,
                        sessionManager.getSobrenomeUsuario(),
                        idadeRemota
                );

                if (!isFinishing() && !isDestroyed()) {
                    preencherDadosUsuario();
                }
            }

            @Override
            public void onFailure(Call<IntercambistaResponseDto> call, Throwable t) {
                sincronizandoPerfil = false;
            }
        });
    }

    private void abrirDialogEditarUsername() {
        String usernameAtual = sessionManager.getUsernameUsuario();

        if (usernameAtual == null || usernameAtual.trim().isEmpty()) {
            Toast.makeText(this, "Seu username ainda não está disponível para edição.", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_input_simples, null);
        EditText input = view.findViewById(R.id.inputDialogSimples);

        input.setHint("Novo username");
        input.setText(usernameAtual);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Editar username")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String novoUsername = normalizarUsername(input.getText().toString());

                    if (atualizandoUsername) {
                        return;
                    }

                    if (novoUsername.isEmpty()) {
                        Toast.makeText(this, "Digite um username.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (novoUsername.length() < 3) {
                        Toast.makeText(this, "O username deve ter pelo menos 3 caracteres.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!usernameValido(novoUsername)) {
                        Toast.makeText(this, "Use apenas letras, números, ponto e underline.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (novoUsername.equalsIgnoreCase(usernameAtual)) {
                        Toast.makeText(this, "Digite um username diferente do atual.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!sessionManager.usernameDisponivelLocalmente(novoUsername)) {
                        Toast.makeText(this, "Esse username já está em uso localmente.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!sessionManager.isModoApi()) {
                        sessionManager.atualizarUsernameUsuario(usernameAtual, novoUsername);
                        preencherDadosUsuario();
                        Toast.makeText(this, "Username atualizado localmente.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    atualizarUsernameApi(usernameAtual, novoUsername);
                })
                .show();
    }

    private void atualizarUsernameApi(String usernameAtual, String novoUsername) {
        if (atualizandoUsername) {
            return;
        }

        atualizandoUsername = true;

        ApiService apiService = ApiClient.getApiService(this);
        IntercambistaUpdtRequestDto request = new IntercambistaUpdtRequestDto(usernameAtual, novoUsername);

        apiService.updateIntercambista(request).enqueue(new Callback<IntercambistaResponseDto>() {
            @Override
            public void onResponse(Call<IntercambistaResponseDto> call, Response<IntercambistaResponseDto> response) {
                atualizandoUsername = false;

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ContaActivity.this, "Não foi possível atualizar o username.", Toast.LENGTH_SHORT).show();
                    return;
                }

                IntercambistaResponseDto body = response.body();
                String usernameFinal = body.getUsername() != null && !body.getUsername().trim().isEmpty()
                        ? body.getUsername().trim().toLowerCase()
                        : novoUsername;

                sessionManager.atualizarUsernameUsuario(usernameAtual, usernameFinal);
                sessionManager.salvarPerfilApi(
                        body.getNome() != null ? body.getNome() : sessionManager.getPrimeiroNomeUsuario(),
                        sessionManager.getEmailUsuario(),
                        usernameFinal,
                        sessionManager.getSobrenomeUsuario(),
                        body.getIdade()
                );

                preencherDadosUsuario();
                textoSubtituloHeroConta.setText(obterSubtituloHeroLogado());

                Toast.makeText(ContaActivity.this, "Username atualizado com sucesso.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<IntercambistaResponseDto> call, Throwable t) {
                atualizandoUsername = false;
                Toast.makeText(ContaActivity.this, "Falha ao conectar para atualizar username.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String normalizarUsername(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim().toLowerCase();
    }

    private boolean usernameValido(String username) {
        return username.matches("^[a-z0-9._]+$");
    }
}