package com.example.mapadointercambista.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.perfil.ContaActivity;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.util.TransitionHelper;
import com.google.android.material.button.MaterialButton;

public class CompletarPerfilGoogleActivity extends AppCompatActivity {

    private EditText inputUsername;
    private EditText inputIdade;
    private MaterialButton botaoSalvar;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_completar_perfil_google);

        aplicarModoImersivo();

        sessionManager = new SessionManager(this);

        inputUsername = findViewById(R.id.inputUsernameCompletarGoogle);
        inputIdade = findViewById(R.id.inputIdadeCompletarGoogle);
        botaoSalvar = findViewById(R.id.botaoSalvarCompletarGoogle);

        inputUsername.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        inputIdade.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        preencherSugestoes();

        findViewById(R.id.botaoVoltarCompletarGoogle).setOnClickListener(v -> {
            abrirConta();
        });

        botaoSalvar.setOnClickListener(v -> salvarPerfil());
    }

    @Override
    protected void onResume() {
        super.onResume();
        aplicarModoImersivo();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            aplicarModoImersivo();
        }
    }

    private void preencherSugestoes() {
        String usernameAtual = sessionManager.getUsernameUsuario();
        int idadeAtual = sessionManager.getIdadeUsuario();

        if (usernameAtual != null && !usernameAtual.trim().isEmpty()) {
            inputUsername.setText(usernameAtual);
        } else {
            inputUsername.setText(gerarUsernameSugerido());
        }

        if (idadeAtual > 0) {
            inputIdade.setText(String.valueOf(idadeAtual));
        }
    }

    private String gerarUsernameSugerido() {
        String email = sessionManager.getEmailUsuario();
        String primeiroNome = sessionManager.getPrimeiroNomeUsuario();

        String base;
        if (primeiroNome != null && !primeiroNome.trim().isEmpty() && !"Usuário".equalsIgnoreCase(primeiroNome.trim())) {
            base = primeiroNome.trim().toLowerCase();
        } else if (email != null && email.contains("@")) {
            base = email.substring(0, email.indexOf("@")).toLowerCase();
        } else {
            base = "usuario";
        }

        base = base.replaceAll("[^a-z0-9._]", "");
        if (base.length() < 3) {
            base = base + "user";
        }
        if (base.length() > 20) {
            base = base.substring(0, 20);
        }

        return base;
    }

    private void salvarPerfil() {
        String username = normalizarUsername(inputUsername.getText().toString());
        String idadeTexto = inputIdade.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Digite um username.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 3) {
            Toast.makeText(this, "O username deve ter pelo menos 3 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!usernameValido(username)) {
            Toast.makeText(this, "Use apenas letras, números, ponto e underline.", Toast.LENGTH_SHORT).show();
            return;
        }

        int idade;
        try {
            idade = Integer.parseInt(idadeTexto);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Digite uma idade válida.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idade < 0 || idade > 120) {
            Toast.makeText(this, "Digite uma idade válida.", Toast.LENGTH_SHORT).show();
            return;
        }

        String usernameAtual = sessionManager.getUsernameUsuario();

        if (!sessionManager.usernameDisponivelLocalmente(username)
                && (usernameAtual == null || !usernameAtual.equalsIgnoreCase(username))) {
            Toast.makeText(this, "Esse username já está em uso localmente.", Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.atualizarUsernameUsuario(usernameAtual, username);
        sessionManager.salvarPerfilApi(
                sessionManager.getPrimeiroNomeUsuario(),
                sessionManager.getEmailUsuario(),
                username,
                sessionManager.getSobrenomeUsuario(),
                idade
        );

        Toast.makeText(this, "Perfil atualizado com sucesso.", Toast.LENGTH_SHORT).show();
        abrirConta();
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

    private void abrirConta() {
        Intent intent = new Intent(this, ContaActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        TransitionHelper.slideForward(this);
    }

    private void aplicarModoImersivo() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }
}