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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapadointercambista.BuildConfig;
import com.example.mapadointercambista.R;
import com.example.mapadointercambista.activity.perfil.ContaActivity;
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

import androidx.annotation.NonNull;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.core.content.ContextCompat;

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;

public class LoginActivity extends AppCompatActivity {

    private static final long DURACAO_TOKEN_API_MILLIS = 4 * 60 * 60 * 1000L;

    private boolean senhaVisivel = false;

    private EditText inputEmail;
    private EditText inputSenha;
    private ImageView iconeOlho;
    private MaterialButton botaoEntrar;
    private ScrollView scrollLogin;
    private MaterialButton botaoGoogleLogin;
    private CredentialManager credentialManager;
    private ActivityResultLauncher<Intent> launcherCompletarPerfilGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        aplicarModoImersivo();

        SessionManager sessionManager = new SessionManager(this);

        inputEmail = findViewById(R.id.inputEmail);
        inputSenha = findViewById(R.id.inputSenha);
        iconeOlho = findViewById(R.id.iconeOlhoSenhaLogin);
        botaoEntrar = findViewById(R.id.botaoEntrar);
        botaoGoogleLogin = findViewById(R.id.botaoGoogleLogin);
        credentialManager = CredentialManager.create(this);
        configurarLauncherCompletarPerfilGoogle();

        botaoGoogleLogin.setOnClickListener(v -> iniciarLoginGoogle());

        inputSenha.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(32),
                new FiltroSenha()
        });

        iconeOlho.setOnClickListener(v -> alternarVisibilidadeSenha());

        findViewById(R.id.botaoVoltar).setOnClickListener(v -> {
            finish();
            TransitionHelper.slideBack(this);
        });

        findViewById(R.id.textoCriarConta).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, EscolhaAcessoActivity.class));
            TransitionHelper.slideForward(this);
        });

        scrollLogin = findViewById(R.id.scrollLogin);
        configurarScrollCampoSenha(inputSenha, scrollLogin);

        botaoEntrar.setOnClickListener(v -> {
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
                inputSenha.requestFocus();
                return;
            }

            if (!SenhaUtils.contemApenasCaracteresPermitidos(senha)) {
                Toast.makeText(this, "A senha contém caracteres inválidos", Toast.LENGTH_SHORT).show();
                inputSenha.requestFocus();
                return;
            }

            tentarLoginApiComFallback(sessionManager, email, senha);
        });

        findViewById(R.id.textoEsqueceuSenha).setOnClickListener(v ->
                Toast.makeText(this, "Recuperação de senha será adicionada em breve", Toast.LENGTH_SHORT).show()
        );
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

    private void configurarLauncherCompletarPerfilGoogle() {
        launcherCompletarPerfilGoogle = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> abrirConta()
        );
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

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void aplicarModoImersivo() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
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
        botaoGoogleLogin.setEnabled(!loading);
    }

    private void iniciarLoginGoogle() {
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID == null
                || BuildConfig.GOOGLE_WEB_CLIENT_ID.trim().isEmpty()
                || "COLOQUE_AQUI_O_WEB_CLIENT_ID".equals(BuildConfig.GOOGLE_WEB_CLIENT_ID)) {
            Toast.makeText(this, "Falta configurar o Google Cloud client ID.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        GetSignInWithGoogleOption googleOption =
                new GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        .setNonce(java.util.UUID.randomUUID().toString())
                        .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        if (!isFinishing() && !isDestroyed()) {
                            setLoading(false);
                        }
                        tratarRespostaGoogle(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        if (!isFinishing() && !isDestroyed()) {
                            setLoading(false);
                        }

                        android.util.Log.e("GOOGLE_LOGIN",
                                "type=" + e.getType() + " message=" + e.getMessage(), e);

                        Toast.makeText(
                                LoginActivity.this,
                                "Erro Google: " + e.getType(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void tratarRespostaGoogle(GetCredentialResponse result) {
        if (!(result.getCredential() instanceof CustomCredential)) {
            Toast.makeText(this, "Credencial Google inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomCredential credential = (CustomCredential) result.getCredential();

        if (!GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
            Toast.makeText(this, "Tipo de credencial não suportado.", Toast.LENGTH_SHORT).show();
            return;
        }

        GoogleIdTokenCredential googleCredential =
                GoogleIdTokenCredential.createFrom(credential.getData());

        String nome = googleCredential.getDisplayName() != null
                ? googleCredential.getDisplayName()
                : "Usuário";
        String email = googleCredential.getId();
        String foto = googleCredential.getProfilePictureUri() != null
                ? googleCredential.getProfilePictureUri().toString()
                : "";

        SessionManager sessionManager = new SessionManager(this);
        sessionManager.entrarComGoogle(nome, email, foto);

        Toast.makeText(this, "Login com Google realizado com sucesso!", Toast.LENGTH_SHORT).show();

        if (perfilPrecisaSerCompletado(sessionManager)) {
            Intent intent = new Intent(this, CompletarPerfilGoogleActivity.class);
            launcherCompletarPerfilGoogle.launch(intent);
        } else {
            abrirConta();
        }
    }

    private boolean perfilPrecisaSerCompletado(SessionManager sessionManager) {
        String username = sessionManager.getUsernameUsuario();
        int idade = sessionManager.getIdadeUsuario();

        return username == null || username.trim().isEmpty() || idade <= 0;
    }

    private void tentarLoginApiComFallback(SessionManager sessionManager, String email, String senha) {
        setLoading(true);

        ApiService apiService = ApiClient.getApiService(LoginActivity.this);
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

                    sessionManager.salvarLoginApi(email, response.body().getToken(), DURACAO_TOKEN_API_MILLIS);
                    inputSenha.setText("");
                    Toast.makeText(LoginActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    abrirConta();
                    return;
                }

                inputSenha.setText("");
                Toast.makeText(LoginActivity.this, "E-mail ou senha inválidos", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<LoginResponseDto> call, Throwable t) {
                if (!isFinishing() && !isDestroyed()) {
                    setLoading(false);
                }

                if (sessionManager.fazerLogin(email, senha)) {
                    inputSenha.setText("");
                    Toast.makeText(LoginActivity.this, "API indisponível. Login local realizado.", Toast.LENGTH_SHORT).show();
                    abrirConta();
                } else {
                    inputSenha.setText("");
                    Toast.makeText(LoginActivity.this, "Falha ao conectar. Verifique sua conexão ou tente novamente.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void abrirConta() {
        Intent intent = new Intent(LoginActivity.this, ContaActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}