package com.example.mapadointercambista.activity.perfil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
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
    private RecyclerView listaFavoritosConta;
    private MaterialButton botaoSairConta;

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

        if (!sessionManager.estaLogado()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        configurarLauncherGaleria();
        inicializarViews();
        configurarListaFavoritos();
        configurarAcoes();
        preencherDadosUsuario();
        carregarFavoritos();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_perfil);
    }

    @Override
    protected void onResume() {
        super.onResume();
        preencherDadosUsuario();
        carregarFavoritos();
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
        listaFavoritosConta = findViewById(R.id.listaFavoritosConta);
        botaoSairConta = findViewById(R.id.botaoSairConta);
    }

    private void configurarAcoes() {
        imagemPerfilConta.setOnClickListener(v -> selecionarFoto());
        findViewById(R.id.textoAlterarFoto).setOnClickListener(v -> selecionarFoto());

        botaoSairConta.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Você saiu da conta", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void configurarListaFavoritos() {
        listaFavoritosConta.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        listaFavoritosConta.setHasFixedSize(true);
        listaFavoritosConta.setItemViewCacheSize(6);

        favoritosAdapter = new DestinoAdapter(favoritosExibidos);
        listaFavoritosConta.setAdapter(favoritosAdapter);
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

        textoNomeUsuarioConta.setText(nome != null ? nome : "Usuário");
        textoEmailUsuarioConta.setText(email != null ? email : "");
        textoNomeCardConta.setText(nome != null ? nome : "Usuário");
        textoEmailCardConta.setText(email != null ? email : "");

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
            textoVazioFavoritosConta.setVisibility(android.view.View.VISIBLE);
            listaFavoritosConta.setVisibility(android.view.View.GONE);
        } else {
            textoResumoFavoritosConta.setText(
                    quantidade == 1 ? "1 destino favoritado" : quantidade + " destinos favoritados"
            );
            textoVazioFavoritosConta.setVisibility(android.view.View.GONE);
            listaFavoritosConta.setVisibility(android.view.View.VISIBLE);
        }
    }
}