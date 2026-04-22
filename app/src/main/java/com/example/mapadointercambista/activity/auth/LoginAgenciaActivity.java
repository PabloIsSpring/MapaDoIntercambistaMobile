package com.example.mapadointercambista.activity.auth;

import android.content.Intent;
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
import com.example.mapadointercambista.dto.request.LoginRequestDto;
import com.example.mapadointercambista.dto.response.LoginResponseDto;
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

public class LoginAgenciaActivity extends AppCompatActivity {

    private static final long DURACAO_TOKEN_API_MILLIS = 4 * 60 * 60 * 1000L;

    private EditText inputEmail;
    private EditText inputSenha;
    private ImageView iconeOlho;
    private MaterialButton botaoEntrar;
    private ScrollView scrollLogin;
    private boolean senhaVisivel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_agencia);

        aplicarModoImersivo();

        inputEmail = findViewById(R.id.inputEmailLoginAgencia);
        inputSenha = findViewById(R.id.inputSenhaLoginAgencia);
        iconeOlho = findViewById(R.id.iconeOlhoSenhaLoginAgencia);
        botaoEntrar = findViewById(R.id.botaoEntrarLoginAgencia);
        scrollLogin = findViewById(R.id.scrollLoginAgencia);

        inputSenha.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(32),
                new FiltroSenha()
        });

        configurarScrollCampoSenha(inputSenha, scrollLogin);

        iconeOlho.setOnClickListener(v -> alternarVisibilidadeSenha());

        findViewById(R.id.botaoVoltarLoginAgencia).setOnClickListener(v -> {
            finish();
            TransitionHelper.slideBack(this);
        });

        findViewById(R.id.textoCriarContaAgencia).setOnClickListener(v -> {
            startActivity(new Intent(this, CadastroAgenciaActivity.class));
            TransitionHelper.slideForward(this);
        });

        botaoEntrar.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String senha = inputSenha.getText().toString();

            if (email.isEmpty()) {
                inputEmail.setError("Digite o e-mail da agência");
                inputEmail.requestFocus();
                return;
            }

            if (!EmailUtils.emailValido(email)) {
                inputEmail.setError("Digite um e-mail válido");
                inputEmail.requestFocus();
                return;
            }

            if (senha.isEmpty()) {
                Toast.makeText(this, "Digite a senha", Toast.LENGTH_SHORT).show();
                inputSenha.requestFocus();
                return;
            }

            if (!SenhaUtils.contemApenasCaracteresPermitidos(senha)) {
                Toast.makeText(this, "A senha contém caracteres inválidos", Toast.LENGTH_SHORT).show();
                inputSenha.requestFocus();
                return;
            }

            tentarLoginApiAgencia(email, senha);
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

    private void tentarLoginApiAgencia(String email, String senha) {
        setLoading(true);

        SessionManager sessionManager = new SessionManager(this);
        ApiService apiService = ApiClient.getApiService(this);
        LoginRequestDto request = new LoginRequestDto(email, senha);

        apiService.login(request).enqueue(new Callback<LoginResponseDto>() {
            @Override
            public void onResponse(Call<LoginResponseDto> call, Response<LoginResponseDto> response) {
                if (!isFinishing() && !isDestroyed()) {
                    setLoading(false);
                }

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getToken() != null
                        && !response.body().getToken().trim().isEmpty()) {

                    sessionManager.salvarLoginApiAgencia(email, response.body().getToken(), DURACAO_TOKEN_API_MILLIS);

                    inputSenha.setText("");
                    Toast.makeText(LoginAgenciaActivity.this, "Login da agência realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    abrirContaAgencia();
                    return;
                }

                inputSenha.setText("");
                Toast.makeText(LoginAgenciaActivity.this, "E-mail ou senha inválidos", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<LoginResponseDto> call, Throwable t) {
                if (!isFinishing() && !isDestroyed()) {
                    setLoading(false);
                }

                inputSenha.setText("");
                Toast.makeText(LoginAgenciaActivity.this, "Falha ao conectar. Tente novamente.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirContaAgencia() {
        Intent intent = new Intent(this, com.example.mapadointercambista.activity.perfil.ContaActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void configurarScrollCampoSenha(EditText campo, ScrollView scrollView) {
        campo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(() -> scrollView.smoothScrollTo(0, v.getBottom() + dpToPx(140)), 180);
            }
        });

        campo.setOnClickListener(v ->
                scrollView.postDelayed(() -> scrollView.smoothScrollTo(0, v.getBottom() + dpToPx(140)), 180)
        );
    }

    private void alternarVisibilidadeSenha() {
        senhaVisivel = !senhaVisivel;

        if (senhaVisivel) {
            inputSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            iconeOlho.setImageResource(R.drawable.ic_eye_off);
        } else {
            inputSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            iconeOlho.setImageResource(R.drawable.ic_eye);
        }

        inputSenha.setSelection(inputSenha.getText().length());
    }

    private void setLoading(boolean loading) {
        botaoEntrar.setEnabled(!loading);
        botaoEntrar.setText(loading ? "Entrando..." : "Entrar");
        inputEmail.setEnabled(!loading);
        inputSenha.setEnabled(!loading);
        iconeOlho.setEnabled(!loading);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void aplicarModoImersivo() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}