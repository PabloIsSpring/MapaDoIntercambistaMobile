package com.example.mapadointercambista.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.perfil.ContaActivity;
import com.example.mapadointercambista.util.EmailUtils;
import com.example.mapadointercambista.util.FiltroSenha;
import com.example.mapadointercambista.util.SenhaUtils;
import com.example.mapadointercambista.model.user.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private boolean senhaVisivel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        SessionManager sessionManager = new SessionManager(this);

        EditText inputEmail = findViewById(R.id.inputEmail);
        EditText inputSenha = findViewById(R.id.inputSenha);
        ImageView iconeOlho = findViewById(R.id.iconeOlhoSenhaLogin);

        inputSenha.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(32),
                new FiltroSenha()
        });

        iconeOlho.setOnClickListener(v -> {
            senhaVisivel = !senhaVisivel;

            if (senhaVisivel) {
                inputSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                iconeOlho.setImageResource(R.drawable.ic_eye_off);
            } else {
                inputSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                iconeOlho.setImageResource(R.drawable.ic_eye);
            }

            inputSenha.setSelection(inputSenha.getText().length());
        });

        findViewById(R.id.botaoVoltar).setOnClickListener(v -> finish());

        findViewById(R.id.textoCriarConta).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.botaoEntrar).setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String senha = inputSenha.getText().toString();

            if (email.isEmpty()) {
                inputEmail.setError("Digite seu e-mail");
                inputEmail.requestFocus();
                return;
            }

            if (!EmailUtils.emailValido(email)) {
                inputEmail.setError("Digite um e-mail válido");
                inputEmail.requestFocus();
                return;
            }

            if (senha.isEmpty()) {
                Toast.makeText(this, "Digite sua senha", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!SenhaUtils.contemApenasCaracteresPermitidos(senha)) {
                Toast.makeText(this, "Senha contém caracteres inválidos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!sessionManager.usuarioExiste()) {
                Toast.makeText(this, "Nenhum usuário cadastrado neste dispositivo", Toast.LENGTH_SHORT).show();
                return;
            }

            if (sessionManager.fazerLogin(email, senha)) {
                Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, ContaActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.textoEsqueceuSenha).setOnClickListener(v -> {
            Toast.makeText(this, "Recuperação de senha será implementada depois", Toast.LENGTH_SHORT).show();
        });
    }
}