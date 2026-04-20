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

import com.example.mapadointercambista.BuildConfig;
import com.example.mapadointercambista.R;
import com.example.mapadointercambista.dto.request.RegisterUserRequestDto;
import com.example.mapadointercambista.dto.response.RegisterUserResponseDto;
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

public class CadastroActivity extends AppCompatActivity {

    private boolean senhaVisivel = false;
    private boolean confirmarSenhaVisivel = false;

    private EditText inputNome;
    private EditText inputSobrenome;
    private EditText inputUsername;
    private EditText inputIdade;
    private EditText inputEmail;
    private EditText inputSenha;
    private EditText inputConfirmarSenha;
    private ImageView olhoSenha;
    private ImageView olhoConfirmarSenha;
    private MaterialButton botaoCadastrar;
    private ScrollView scrollCadastro;
    private MaterialButton botaoGoogleCadastro;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        aplicarModoImersivo();

        SessionManager sessionManager = new SessionManager(this);

        inputNome = findViewById(R.id.inputNomeCadastro);
        inputSobrenome = findViewById(R.id.inputSobrenomeCadastro);
        inputUsername = findViewById(R.id.inputUsernameCadastro);
        inputIdade = findViewById(R.id.inputIdadeCadastro);
        inputEmail = findViewById(R.id.inputEmailCadastro);
        inputSenha = findViewById(R.id.inputSenhaCadastro);
        inputConfirmarSenha = findViewById(R.id.inputConfirmarSenhaCadastro);

        olhoSenha = findViewById(R.id.iconeOlhoSenhaCadastro);
        olhoConfirmarSenha = findViewById(R.id.iconeOlhoConfirmarSenhaCadastro);
        botaoCadastrar = findViewById(R.id.botaoCadastrar);

        InputFilter[] filtrosSenha = new InputFilter[]{
                new InputFilter.LengthFilter(32),
                new FiltroSenha()
        };

        inputSenha.setFilters(filtrosSenha);
        inputConfirmarSenha.setFilters(filtrosSenha);

        olhoSenha.setOnClickListener(v -> alternarSenhaPrincipal());
        olhoConfirmarSenha.setOnClickListener(v -> alternarConfirmacaoSenha());

        findViewById(R.id.botaoVoltarCadastro).setOnClickListener(v -> {
            finish();
            TransitionHelper.slideBack(this);
        });

        scrollCadastro = findViewById(R.id.scrollCadastro);

        configurarScrollCampoSenha(inputSenha, scrollCadastro);
        configurarScrollCampoSenha(inputConfirmarSenha, scrollCadastro);
        botaoGoogleCadastro = findViewById(R.id.botaoGoogleCadastro);
        credentialManager = CredentialManager.create(this);

        botaoGoogleCadastro.setOnClickListener(v -> iniciarCadastroGoogle());

        botaoCadastrar.setOnClickListener(v -> {
            String nome = inputNome.getText().toString().trim();
            String sobrenome = inputSobrenome.getText().toString().trim();
            String username = normalizarUsername(inputUsername.getText().toString());
            String idadeTexto = inputIdade.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String senha = inputSenha.getText().toString();
            String confirmarSenha = inputConfirmarSenha.getText().toString();

            if (nome.isEmpty()) {
                inputNome.setError("Digite seu nome");
                inputNome.requestFocus();
                return;
            }

            if (nome.length() < 2) {
                inputNome.setError("Digite um nome válido");
                inputNome.requestFocus();
                return;
            }

            if (sobrenome.isEmpty()) {
                inputSobrenome.setError("Digite seu sobrenome");
                inputSobrenome.requestFocus();
                return;
            }

            if (sobrenome.length() < 2) {
                inputSobrenome.setError("Digite um sobrenome válido");
                inputSobrenome.requestFocus();
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

            if (idadeTexto.isEmpty()) {
                inputIdade.setError("Digite sua idade");
                inputIdade.requestFocus();
                return;
            }

            int idade;
            try {
                idade = Integer.parseInt(idadeTexto);
            } catch (NumberFormatException e) {
                inputIdade.setError("Digite uma idade válida");
                inputIdade.requestFocus();
                return;
            }

            if (idade < 0 || idade > 120) {
                inputIdade.setError("Digite uma idade válida");
                inputIdade.requestFocus();
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

            tentarCadastroApiComFallback(
                    sessionManager,
                    nome,
                    sobrenome,
                    username,
                    idade,
                    email,
                    senha
            );
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

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void aplicarModoImersivo() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
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
        botaoCadastrar.setText(loading ? "Criando conta..." : "Criar conta");

        inputNome.setEnabled(!loading);
        inputSobrenome.setEnabled(!loading);
        inputUsername.setEnabled(!loading);
        inputIdade.setEnabled(!loading);
        inputEmail.setEnabled(!loading);
        inputSenha.setEnabled(!loading);
        inputConfirmarSenha.setEnabled(!loading);

        olhoSenha.setEnabled(!loading);
        olhoConfirmarSenha.setEnabled(!loading);
        botaoGoogleCadastro.setEnabled(!loading);
    }

    private void iniciarCadastroGoogle() {
        if ("COLOQUE_AQUI_O_WEB_CLIENT_ID".equals(BuildConfig.GOOGLE_WEB_CLIENT_ID)) {
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
                        tratarCadastroGoogle(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        if (!isFinishing() && !isDestroyed()) {
                            setLoading(false);
                        }
                        Toast.makeText(CadastroActivity.this, "Não foi possível cadastrar com Google.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void tratarCadastroGoogle(GetCredentialResponse result) {
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

        String nomeCompleto = googleCredential.getDisplayName() != null
                ? googleCredential.getDisplayName()
                : "Usuário";
        String email = googleCredential.getId();
        String foto = googleCredential.getProfilePictureUri() != null
                ? googleCredential.getProfilePictureUri().toString()
                : "";

        SessionManager sessionManager = new SessionManager(this);
        sessionManager.entrarComGoogle(nomeCompleto, email, foto);

        Toast.makeText(this, "Conta Google vinculada com sucesso!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void tentarCadastroApiComFallback(SessionManager sessionManager,
                                              String nome,
                                              String sobrenome,
                                              String username,
                                              int idade,
                                              String email,
                                              String senha) {
        setLoading(true);

        ApiService apiService = ApiClient.getApiService(CadastroActivity.this);

        RegisterUserRequestDto request = new RegisterUserRequestDto(
                nome,
                email,
                senha,
                username,
                sobrenome,
                idade
        );

        apiService.registerUser(request).enqueue(new Callback<RegisterUserResponseDto>() {
            @Override
            public void onResponse(Call<RegisterUserResponseDto> call, Response<RegisterUserResponseDto> response) {
                if (!isFinishing() && !isDestroyed()) {
                    setLoading(false);
                }

                if (response.isSuccessful()) {
                    sessionManager.salvarUsuarioLocalSeNaoExistir(
                            nome,
                            sobrenome,
                            username,
                            idade,
                            email,
                            senha
                    );

                    sessionManager.salvarPerfilApi(nome, email, username, sobrenome, idade);

                    Toast.makeText(CadastroActivity.this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                boolean cadastradoLocal = sessionManager.cadastrarUsuario(
                        nome,
                        sobrenome,
                        username,
                        idade,
                        email,
                        senha
                );

                if (cadastradoLocal) {
                    Toast.makeText(
                            CadastroActivity.this,
                            "API indisponível para cadastro remoto. Conta salva localmente.",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                } else {
                    Toast.makeText(CadastroActivity.this, "Já existe uma conta com este e-mail", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterUserResponseDto> call, Throwable t) {
                if (!isFinishing() && !isDestroyed()) {
                    setLoading(false);
                }

                boolean cadastradoLocal = sessionManager.cadastrarUsuario(
                        nome,
                        sobrenome,
                        username,
                        idade,
                        email,
                        senha
                );

                if (cadastradoLocal) {
                    Toast.makeText(
                            CadastroActivity.this,
                            "API indisponível. Cadastro salvo localmente.",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                } else {
                    Toast.makeText(CadastroActivity.this, "Já existe uma conta com este e-mail", Toast.LENGTH_SHORT).show();
                }
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