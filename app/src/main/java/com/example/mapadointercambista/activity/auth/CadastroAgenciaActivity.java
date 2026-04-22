package com.example.mapadointercambista.activity.auth;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapadointercambista.R;
import com.example.mapadointercambista.dto.request.CreateAgenciaRequestDto;
import com.example.mapadointercambista.dto.response.AgenciaResponseDto;
import com.example.mapadointercambista.model.user.SessionManager;
import com.example.mapadointercambista.network.ApiClient;
import com.example.mapadointercambista.network.ApiService;
import com.example.mapadointercambista.util.EmailUtils;
import com.example.mapadointercambista.util.FiltroSenha;
import com.example.mapadointercambista.util.SenhaUtils;
import com.example.mapadointercambista.util.TransitionHelper;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroAgenciaActivity extends AppCompatActivity {

    private EditText inputRazaoSocial;
    private EditText inputNomeFantasia;
    private EditText inputCnpj;
    private EditText inputUsername;
    private EditText inputEmail;
    private EditText inputSenha;
    private EditText inputConfirmarSenha;

    private ImageView olhoSenha;
    private ImageView olhoConfirmarSenha;
    private MaterialButton botaoCadastrar;
    private ScrollView scrollCadastro;

    private boolean senhaVisivel = false;
    private boolean confirmarSenhaVisivel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_agencia);

        aplicarModoImersivo();

        inputRazaoSocial = findViewById(R.id.inputRazaoSocialCadastroAgencia);
        inputNomeFantasia = findViewById(R.id.inputNomeFantasiaCadastroAgencia);
        inputCnpj = findViewById(R.id.inputCnpjCadastroAgencia);
        inputUsername = findViewById(R.id.inputUsernameCadastroAgencia);
        inputEmail = findViewById(R.id.inputEmailCadastroAgencia);
        inputSenha = findViewById(R.id.inputSenhaCadastroAgencia);
        inputConfirmarSenha = findViewById(R.id.inputConfirmarSenhaCadastroAgencia);

        olhoSenha = findViewById(R.id.iconeOlhoSenhaCadastroAgencia);
        olhoConfirmarSenha = findViewById(R.id.iconeOlhoConfirmarSenhaCadastroAgencia);
        botaoCadastrar = findViewById(R.id.botaoCadastrarAgencia);
        scrollCadastro = findViewById(R.id.scrollCadastroAgencia);

        InputFilter[] filtrosSenha = new InputFilter[]{
                new InputFilter.LengthFilter(32),
                new FiltroSenha()
        };

        inputSenha.setFilters(filtrosSenha);
        inputConfirmarSenha.setFilters(filtrosSenha);

        olhoSenha.setOnClickListener(v -> alternarSenhaPrincipal());
        olhoConfirmarSenha.setOnClickListener(v -> alternarConfirmacaoSenha());

        configurarScrollCampoSenha(inputSenha, scrollCadastro);
        configurarScrollCampoSenha(inputConfirmarSenha, scrollCadastro);

        findViewById(R.id.botaoVoltarCadastroAgencia).setOnClickListener(v -> {
            finish();
            TransitionHelper.slideBack(this);
        });

        botaoCadastrar.setOnClickListener(v -> {
            String razaoSocial = inputRazaoSocial.getText().toString().trim();
            String nomeFantasia = inputNomeFantasia.getText().toString().trim();
            String cnpj = normalizarCnpj(inputCnpj.getText().toString());
            String username = normalizarUsername(inputUsername.getText().toString());
            String email = inputEmail.getText().toString().trim();
            String senha = inputSenha.getText().toString();
            String confirmarSenha = inputConfirmarSenha.getText().toString();

            if (razaoSocial.isEmpty()) {
                inputRazaoSocial.setError("Digite a razão social");
                inputRazaoSocial.requestFocus();
                return;
            }

            if (razaoSocial.length() < 3) {
                inputRazaoSocial.setError("Digite uma razão social válida");
                inputRazaoSocial.requestFocus();
                return;
            }

            if (nomeFantasia.isEmpty()) {
                inputNomeFantasia.setError("Digite o nome fantasia");
                inputNomeFantasia.requestFocus();
                return;
            }

            if (nomeFantasia.length() < 2) {
                inputNomeFantasia.setError("Digite um nome fantasia válido");
                inputNomeFantasia.requestFocus();
                return;
            }

            if (cnpj.isEmpty()) {
                inputCnpj.setError("Digite o CNPJ");
                inputCnpj.requestFocus();
                return;
            }

            if (!cnpjValidoBasico(cnpj)) {
                inputCnpj.setError("Digite um CNPJ válido com 14 números");
                inputCnpj.requestFocus();
                return;
            }

            if (username.isEmpty()) {
                inputUsername.setError("Digite um username");
                inputUsername.requestFocus();
                return;
            }

            if (username.length() < 3) {
                inputUsername.setError("O username deve ter pelo menos 3 caracteres");
                inputUsername.requestFocus();
                return;
            }

            if (username.length() > 30) {
                inputUsername.setError("O username deve ter no máximo 30 caracteres");
                inputUsername.requestFocus();
                return;
            }

            if (!usernameValido(username)) {
                inputUsername.setError("Use apenas letras, números, ponto e underline");
                inputUsername.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                inputEmail.setError("Digite o e-mail");
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

            if (!SenhaUtils.contemApenasCaracteresPermitidos(senha)
                    || !SenhaUtils.contemApenasCaracteresPermitidos(confirmarSenha)) {
                Toast.makeText(this, "Use apenas caracteres permitidos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!SenhaUtils.senhaForte(senha)) {
                Toast.makeText(
                        this,
                        "A senha deve ter 8 a 32 caracteres, com maiúscula, minúscula, número e símbolo",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            if (!senha.equals(confirmarSenha)) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
                inputConfirmarSenha.requestFocus();
                return;
            }

            tentarCadastroAgenciaApi(razaoSocial, nomeFantasia, cnpj, username, email, senha);
        });
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

    private void tentarCadastroAgenciaApi(String razaoSocial,
                                          String nomeFantasia,
                                          String cnpj,
                                          String username,
                                          String email,
                                          String senha) {
        setLoading(true);

        SessionManager sessionManager = new SessionManager(this);
        ApiService apiService = ApiClient.getApiService(this);

        CreateAgenciaRequestDto request = new CreateAgenciaRequestDto(
                email,
                senha,
                razaoSocial,
                nomeFantasia,
                cnpj,
                username
        );

        apiService.registerAgencia(request).enqueue(new Callback<AgenciaResponseDto>() {
            @Override
            public void onResponse(Call<AgenciaResponseDto> call, Response<AgenciaResponseDto> response) {
                if (!isFinishing() && !isDestroyed()) {
                    setLoading(false);
                }

                if (response.isSuccessful() && response.body() != null) {
                    AgenciaResponseDto body = response.body();

                    sessionManager.salvarPerfilAgenciaApi(
                            body.getEmail(),
                            body.getUsername(),
                            body.getNomeFantasia(),
                            body.getRazaoSocial(),
                            body.getCnpj()
                    );

                    sessionManager.salvarTipoConta("agencia");

                    Toast.makeText(CadastroAgenciaActivity.this, "Agência cadastrada com sucesso!", Toast.LENGTH_SHORT).show();
                    abrirContaAgencia();
                    return;
                }

                Toast.makeText(CadastroAgenciaActivity.this, "Não foi possível cadastrar a agência.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<AgenciaResponseDto> call, Throwable t) {
                if (!isFinishing() && !isDestroyed()) {
                    setLoading(false);
                }

                Toast.makeText(CadastroAgenciaActivity.this, "Falha ao conectar. Tente novamente.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirContaAgencia() {
        android.content.Intent intent = new android.content.Intent(
                CadastroAgenciaActivity.this,
                com.example.mapadointercambista.activity.perfil.ContaActivity.class
        );
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void configurarScrollCampoSenha(EditText campo, ScrollView scrollView) {
        campo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(() -> scrollView.smoothScrollTo(0, v.getBottom() + dpToPx(160)), 180);
            }
        });

        campo.setOnClickListener(v ->
                scrollView.postDelayed(() -> scrollView.smoothScrollTo(0, v.getBottom() + dpToPx(160)), 180)
        );
    }

    private void alternarSenhaPrincipal() {
        senhaVisivel = !senhaVisivel;

        if (senhaVisivel) {
            inputSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            olhoSenha.setImageResource(R.drawable.ic_eye_off);
        } else {
            inputSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            olhoSenha.setImageResource(R.drawable.ic_eye);
        }

        inputSenha.setSelection(inputSenha.getText().length());
    }

    private void alternarConfirmacaoSenha() {
        confirmarSenhaVisivel = !confirmarSenhaVisivel;

        if (confirmarSenhaVisivel) {
            inputConfirmarSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            olhoConfirmarSenha.setImageResource(R.drawable.ic_eye_off);
        } else {
            inputConfirmarSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            olhoConfirmarSenha.setImageResource(R.drawable.ic_eye);
        }

        inputConfirmarSenha.setSelection(inputConfirmarSenha.getText().length());
    }

    private void setLoading(boolean loading) {
        botaoCadastrar.setEnabled(!loading);
        botaoCadastrar.setText(loading ? "Criando conta..." : "Criar conta de agência");

        inputRazaoSocial.setEnabled(!loading);
        inputNomeFantasia.setEnabled(!loading);
        inputCnpj.setEnabled(!loading);
        inputUsername.setEnabled(!loading);
        inputEmail.setEnabled(!loading);
        inputSenha.setEnabled(!loading);
        inputConfirmarSenha.setEnabled(!loading);
        olhoSenha.setEnabled(!loading);
        olhoConfirmarSenha.setEnabled(!loading);
    }

    private String normalizarUsername(String valor) {
        if (valor == null) return "";
        return valor.trim().toLowerCase();
    }

    private String normalizarCnpj(String valor) {
        if (valor == null) return "";
        return valor.replaceAll("\\D", "");
    }

    private boolean cnpjValidoBasico(String cnpj) {
        return cnpj != null && cnpj.matches("\\d{14}");
    }

    private boolean usernameValido(String username) {
        return username.matches("^[a-z0-9._]+$");
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void aplicarModoImersivo() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}