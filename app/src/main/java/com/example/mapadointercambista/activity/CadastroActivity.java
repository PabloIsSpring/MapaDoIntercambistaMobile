package com.example.mapadointercambista.activity;

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
import com.example.mapadointercambista.model.EmailUtils;
import com.example.mapadointercambista.model.FiltroSenha;
import com.example.mapadointercambista.model.SenhaUtils;
import com.example.mapadointercambista.model.SessionManager;

public class CadastroActivity extends AppCompatActivity {

    private boolean senhaVisivel = false;
    private boolean confirmarSenhaVisivel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        SessionManager sessionManager = new SessionManager(this);

        EditText inputNome = findViewById(R.id.inputNomeCadastro);
        EditText inputEmail = findViewById(R.id.inputEmailCadastro);
        EditText inputSenha = findViewById(R.id.inputSenhaCadastro);
        EditText inputConfirmarSenha = findViewById(R.id.inputConfirmarSenhaCadastro);

        ImageView olhoSenha = findViewById(R.id.iconeOlhoSenhaCadastro);
        ImageView olhoConfirmarSenha = findViewById(R.id.iconeOlhoConfirmarSenhaCadastro);

        InputFilter[] filtrosSenha = new InputFilter[]{
                new InputFilter.LengthFilter(32),
                new FiltroSenha()
        };

        inputSenha.setFilters(filtrosSenha);
        inputConfirmarSenha.setFilters(filtrosSenha);

        olhoSenha.setOnClickListener(v -> {
            senhaVisivel = !senhaVisivel;

            if (senhaVisivel) {
                inputSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                olhoSenha.setImageResource(R.drawable.ic_eye_off);
            } else {
                inputSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                olhoSenha.setImageResource(R.drawable.ic_eye);
            }

            inputSenha.setSelection(inputSenha.getText().length());
        });

        olhoConfirmarSenha.setOnClickListener(v -> {
            confirmarSenhaVisivel = !confirmarSenhaVisivel;

            if (confirmarSenhaVisivel) {
                inputConfirmarSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                olhoConfirmarSenha.setImageResource(R.drawable.ic_eye_off);
            } else {
                inputConfirmarSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                olhoConfirmarSenha.setImageResource(R.drawable.ic_eye);
            }

            inputConfirmarSenha.setSelection(inputConfirmarSenha.getText().length());
        });

        findViewById(R.id.botaoVoltarCadastro).setOnClickListener(v -> finish());

        findViewById(R.id.botaoCadastrar).setOnClickListener(v -> {
            String nome = inputNome.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String senha = inputSenha.getText().toString();
            String confirmarSenha = inputConfirmarSenha.getText().toString();

            if (nome.isEmpty()) {
                inputNome.setError("Digite seu nome");
                inputNome.requestFocus();
                return;
            }

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

            if (senha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, "Preencha os campos de senha", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!SenhaUtils.contemApenasCaracteresPermitidos(senha) ||
                    !SenhaUtils.contemApenasCaracteresPermitidos(confirmarSenha)) {
                Toast.makeText(this, "Use apenas caracteres permitidos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!SenhaUtils.senhaForte(senha)) {
                Toast.makeText(this,
                        "A senha deve ter 8 a 32 caracteres, com maiúscula, minúscula, número e símbolo",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (!senha.equals(confirmarSenha)) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
                return;
            }

            sessionManager.cadastrarUsuario(nome, email, senha);

            Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}