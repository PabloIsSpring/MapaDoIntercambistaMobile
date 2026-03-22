package com.example.mapadointercambista.activity.perfil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.main.MainActivity;
import com.example.mapadointercambista.navigation.NavigationHelper;
import com.example.mapadointercambista.model.user.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;

public class ContaActivity extends AppCompatActivity {

    private ShapeableImageView imagemPerfilConta;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<String> seletorImagem =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {
                    }

                    imagemPerfilConta.setImageURI(uri);
                    imagemPerfilConta.setImageTintList(null); // remove o tint branco
                    sessionManager.salvarFotoUsuario(uri.toString());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conta);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        sessionManager = new SessionManager(this);

        imagemPerfilConta = findViewById(R.id.imagemPerfilConta);

        TextView textoNome = findViewById(R.id.textoNomeUsuarioConta);
        TextView textoEmail = findViewById(R.id.textoEmailUsuarioConta);
        TextView textoNomeCard = findViewById(R.id.textoNomeCardConta);
        TextView textoEmailCard = findViewById(R.id.textoEmailCardConta);

        textoNome.setText(sessionManager.getNomeUsuario());
        textoEmail.setText(sessionManager.getEmailUsuario());
        textoNomeCard.setText(sessionManager.getNomeUsuario());
        textoEmailCard.setText(sessionManager.getEmailUsuario());

        String fotoSalva = sessionManager.getFotoUsuario();

        if (fotoSalva != null && !fotoSalva.isEmpty()) {
            imagemPerfilConta.setImageURI(Uri.parse(fotoSalva));
            imagemPerfilConta.setImageTintList(null); // foto real sem tint
        } else {
            imagemPerfilConta.setImageResource(R.drawable.ic_user);
            imagemPerfilConta.setImageTintList(
                    ContextCompat.getColorStateList(this, android.R.color.white)
            );
        }

        imagemPerfilConta.setOnClickListener(v -> seletorImagem.launch("image/*"));
        findViewById(R.id.textoAlterarFoto).setOnClickListener(v -> seletorImagem.launch("image/*"));

        findViewById(R.id.botaoSairConta).setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Você saiu da conta", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ContaActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.configurarBottomNavigation(this, bottomNav, R.id.nav_perfil);
    }
}