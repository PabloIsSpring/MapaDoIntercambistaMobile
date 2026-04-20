package com.example.mapadointercambista.model.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SessionManager {

    private static final String PREF_NAME = "mapa_intercambista_session";
    private static final String KEY_USUARIOS = "usuarios";
    private static final String KEY_EMAIL_LOGADO = "email_logado";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_TOKEN_EXPIRATION = "token_expiration";
    private static final String KEY_AUTH_MODE = "auth_mode";

    private static final String KEY_API_NOME = "api_nome";
    private static final String KEY_API_USERNAME = "api_username";
    private static final String KEY_API_SOBRENOME = "api_sobrenome";
    private static final String KEY_API_IDADE = "api_idade";
    private static final String KEY_API_FOTO_URI = "api_foto_uri";

    private static final String AUTH_MODE_API = "api";
    private static final String AUTH_MODE_LOCAL = "local";

    private static final int PBKDF2_ITERATIONS = 120000;
    private static final int HASH_SIZE_BITS = 256;
    private static final int SALT_SIZE_BYTES = 16;

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    public boolean cadastrarUsuario(String nome, String email, String senha) {
        return cadastrarUsuario(nome, "", "", 0, email, senha);
    }

    public boolean cadastrarUsuario(String nome,
                                    String sobrenome,
                                    String username,
                                    int idade,
                                    String email,
                                    String senha) {
        List<Usuario> usuarios = carregarUsuarios();

        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equalsIgnoreCase(email)) {
                return false;
            }
        }

        String senhaHash = gerarHashSenha(senha);
        usuarios.add(new Usuario(
                valorSeguro(nome),
                valorSeguro(sobrenome),
                valorSeguro(username),
                Math.max(idade, 0),
                valorSeguro(email),
                senhaHash,
                ""
        ));
        salvarUsuarios(usuarios);
        return true;
    }

    public boolean usuarioExiste() {
        return !carregarUsuarios().isEmpty();
    }

    public boolean fazerLogin(String email, String senha) {
        List<Usuario> usuarios = carregarUsuarios();

        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equalsIgnoreCase(email)
                    && verificarSenha(senha, usuario.getSenhaHash())) {

                editor.putString(KEY_EMAIL_LOGADO, usuario.getEmail());
                editor.putString(KEY_AUTH_MODE, AUTH_MODE_LOCAL);
                editor.remove(KEY_TOKEN);
                editor.remove(KEY_TOKEN_EXPIRATION);
                limparCachePerfilApi();
                editor.apply();
                return true;
            }
        }

        return false;
    }

    public void salvarLoginApi(String email, String token, long duracaoMillis) {
        long expirationTime = System.currentTimeMillis() + duracaoMillis;

        editor.putString(KEY_EMAIL_LOGADO, valorSeguro(email));
        editor.putString(KEY_TOKEN, valorSeguro(token));
        editor.putLong(KEY_TOKEN_EXPIRATION, expirationTime);
        editor.putString(KEY_AUTH_MODE, AUTH_MODE_API);
        editor.apply();
    }

    public void salvarPerfilApi(String nome, String email, String username, String sobrenome, int idade) {
        if (email != null && !email.trim().isEmpty()) {
            editor.putString(KEY_EMAIL_LOGADO, email.trim());
        }

        editor.putString(KEY_API_NOME, valorSeguro(nome));
        editor.putString(KEY_API_USERNAME, valorSeguro(username).toLowerCase());
        editor.putString(KEY_API_SOBRENOME, valorSeguro(sobrenome));
        editor.putInt(KEY_API_IDADE, Math.max(idade, 0));
        editor.apply();
    }

    public void entrarComGoogle(String nomeCompleto, String email, String fotoUri) {
        String nomeSeguro = valorSeguro(nomeCompleto);
        String emailSeguro = valorSeguro(email).toLowerCase();
        String fotoSegura = valorSeguro(fotoUri);

        if (emailSeguro.isEmpty()) {
            return;
        }

        String primeiroNome = extrairPrimeiroNome(nomeSeguro);
        String sobrenome = extrairSobrenome(nomeSeguro);

        List<Usuario> usuarios = carregarUsuarios();
        Usuario usuarioExistente = null;

        for (Usuario usuario : usuarios) {
            normalizarUsuarioLegado(usuario);
            if (usuario.getEmail().equalsIgnoreCase(emailSeguro)) {
                usuarioExistente = usuario;
                break;
            }
        }

        if (usuarioExistente == null) {
            String senhaDummyHash = gerarHashSenha("google_auth_" + System.currentTimeMillis());

            usuarios.add(new Usuario(
                    primeiroNome,
                    sobrenome,
                    "",
                    0,
                    emailSeguro,
                    senhaDummyHash,
                    fotoSegura
            ));
            salvarUsuarios(usuarios);
        } else {
            boolean alterado = false;

            if (usuarioExistente.getNome().trim().isEmpty() && !primeiroNome.isEmpty()) {
                usuarioExistente.setNome(primeiroNome);
                alterado = true;
            }

            if (usuarioExistente.getSobrenome().trim().isEmpty() && !sobrenome.isEmpty()) {
                usuarioExistente.setSobrenome(sobrenome);
                alterado = true;
            }

            if (!fotoSegura.isEmpty()) {
                usuarioExistente.setFotoUri(fotoSegura);
                alterado = true;
            }

            if (alterado) {
                salvarUsuarios(usuarios);
            }
        }

        editor.putString(KEY_EMAIL_LOGADO, emailSeguro);
        editor.putString(KEY_AUTH_MODE, AUTH_MODE_LOCAL);
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_TOKEN_EXPIRATION);
        limparCachePerfilApi();
        editor.apply();
    }

    private String extrairPrimeiroNome(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
            return "Usuário";
        }

        String[] partes = nomeCompleto.trim().split("\\s+");
        return partes.length > 0 ? partes[0] : "Usuário";
    }

    private String extrairSobrenome(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.trim().isEmpty()) {
            return "";
        }

        String[] partes = nomeCompleto.trim().split("\\s+", 2);
        return partes.length > 1 ? partes[1] : "";
    }

    public boolean estaLogado() {
        String modo = prefs.getString(KEY_AUTH_MODE, "");

        if (AUTH_MODE_API.equals(modo)) {
            String emailLogado = prefs.getString(KEY_EMAIL_LOGADO, null);
            return isTokenValido() && emailLogado != null && !emailLogado.trim().isEmpty();
        }

        if (AUTH_MODE_LOCAL.equals(modo)) {
            return getUsuarioLogado() != null;
        }

        return false;
    }

    public boolean possuiEmailLogado() {
        String emailLogado = prefs.getString(KEY_EMAIL_LOGADO, null);
        return emailLogado != null && !emailLogado.trim().isEmpty();
    }

    public boolean isTokenValido() {
        long expiration = prefs.getLong(KEY_TOKEN_EXPIRATION, 0);
        return expiration > 0 && System.currentTimeMillis() < expiration;
    }

    public boolean sessaoApiExpirada() {
        String modo = prefs.getString(KEY_AUTH_MODE, "");
        return AUTH_MODE_API.equals(modo) && !isTokenValido();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isModoApi() {
        return AUTH_MODE_API.equals(prefs.getString(KEY_AUTH_MODE, ""));
    }

    public boolean isModoLocal() {
        return AUTH_MODE_LOCAL.equals(prefs.getString(KEY_AUTH_MODE, ""));
    }

    public void logout() {
        editor.remove(KEY_EMAIL_LOGADO);
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_TOKEN_EXPIRATION);
        editor.remove(KEY_AUTH_MODE);
        limparCachePerfilApi();
        editor.apply();
    }

    public Usuario getUsuarioLogado() {
        String emailLogado = prefs.getString(KEY_EMAIL_LOGADO, null);

        if (emailLogado == null || emailLogado.isEmpty()) {
            return null;
        }

        List<Usuario> usuarios = carregarUsuarios();

        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equalsIgnoreCase(emailLogado)) {
                normalizarUsuarioLegado(usuario);
                return usuario;
            }
        }

        return null;
    }

    public String getNomeUsuario() {
        Usuario usuario = getUsuarioLogado();
        if (usuario != null) {
            String nomeCompleto = usuario.getNomeCompleto();
            if (!nomeCompleto.trim().isEmpty()) {
                return nomeCompleto;
            }
        }

        String nomeApi = prefs.getString(KEY_API_NOME, "");
        String sobrenomeApi = prefs.getString(KEY_API_SOBRENOME, "");

        String nomeCompletoApi = montarNomeCompleto(nomeApi, sobrenomeApi);
        if (!nomeCompletoApi.isEmpty()) {
            return nomeCompletoApi;
        }

        return "Usuário";
    }

    public String getPrimeiroNomeUsuario() {
        Usuario usuario = getUsuarioLogado();
        if (usuario != null && !usuario.getNome().trim().isEmpty()) {
            return usuario.getNome().trim();
        }

        String nomeApi = prefs.getString(KEY_API_NOME, "");
        if (nomeApi != null && !nomeApi.trim().isEmpty()) {
            return nomeApi.trim();
        }

        return "Usuário";
    }

    public String getEmailUsuario() {
        Usuario usuario = getUsuarioLogado();
        if (usuario != null && !usuario.getEmail().trim().isEmpty()) {
            return usuario.getEmail();
        }

        String emailApi = prefs.getString(KEY_EMAIL_LOGADO, "");
        return emailApi != null ? emailApi : "";
    }

    public String getUsernameUsuario() {
        Usuario usuario = getUsuarioLogado();
        if (usuario != null && !usuario.getUsername().trim().isEmpty()) {
            return usuario.getUsername();
        }

        String usernameApi = prefs.getString(KEY_API_USERNAME, "");
        return usernameApi != null ? usernameApi : "";
    }

    public String getSobrenomeUsuario() {
        Usuario usuario = getUsuarioLogado();
        if (usuario != null && !usuario.getSobrenome().trim().isEmpty()) {
            return usuario.getSobrenome();
        }

        String sobrenomeApi = prefs.getString(KEY_API_SOBRENOME, "");
        return sobrenomeApi != null ? sobrenomeApi : "";
    }

    public int getIdadeUsuario() {
        Usuario usuario = getUsuarioLogado();
        if (usuario != null && usuario.getIdade() > 0) {
            return usuario.getIdade();
        }

        return Math.max(prefs.getInt(KEY_API_IDADE, 0), 0);
    }

    public String getFotoUsuario() {
        Usuario usuario = getUsuarioLogado();
        if (usuario != null && !usuario.getFotoUri().trim().isEmpty()) {
            return usuario.getFotoUri();
        }

        String fotoApi = prefs.getString(KEY_API_FOTO_URI, "");
        return fotoApi != null ? fotoApi : "";
    }

    public void salvarFotoUsuario(String fotoUri) {
        Usuario usuarioLogado = getUsuarioLogado();

        if (usuarioLogado != null) {
            List<Usuario> usuarios = carregarUsuarios();

            for (Usuario usuario : usuarios) {
                if (usuario.getEmail().equalsIgnoreCase(usuarioLogado.getEmail())) {
                    normalizarUsuarioLegado(usuario);
                    usuario.setFotoUri(valorSeguro(fotoUri));
                    break;
                }
            }

            salvarUsuarios(usuarios);
            return;
        }

        if (isModoApi()) {
            editor.putString(KEY_API_FOTO_URI, valorSeguro(fotoUri));
            editor.apply();
        }
    }

    public void salvarUsuarioLocalSeNaoExistir(String nome, String email, String senha) {
        salvarUsuarioLocalSeNaoExistir(nome, "", "", 0, email, senha);
    }

    public void salvarUsuarioLocalSeNaoExistir(String nome,
                                               String sobrenome,
                                               String username,
                                               int idade,
                                               String email,
                                               String senha) {
        List<Usuario> usuarios = carregarUsuarios();

        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equalsIgnoreCase(email)) {
                normalizarUsuarioLegado(usuario);

                boolean alterado = false;

                if (usuario.getNome().trim().isEmpty() && nome != null && !nome.trim().isEmpty()) {
                    usuario.setNome(nome.trim());
                    alterado = true;
                }

                if (usuario.getSobrenome().trim().isEmpty() && sobrenome != null && !sobrenome.trim().isEmpty()) {
                    usuario.setSobrenome(sobrenome.trim());
                    alterado = true;
                }

                if (usuario.getUsername().trim().isEmpty() && username != null && !username.trim().isEmpty()) {
                    usuario.setUsername(username.trim().toLowerCase());
                    alterado = true;
                }

                if (usuario.getIdade() <= 0 && idade > 0) {
                    usuario.setIdade(idade);
                    alterado = true;
                }

                if (alterado) {
                    salvarUsuarios(usuarios);
                }
                return;
            }
        }

        String senhaHash = gerarHashSenha(senha);
        usuarios.add(new Usuario(
                valorSeguro(nome),
                valorSeguro(sobrenome),
                valorSeguro(username).toLowerCase(),
                Math.max(idade, 0),
                valorSeguro(email),
                senhaHash,
                ""
        ));
        salvarUsuarios(usuarios);
    }

    public long getTempoRestanteToken() {
        long expiration = prefs.getLong(KEY_TOKEN_EXPIRATION, 0);
        long restante = expiration - System.currentTimeMillis();
        return Math.max(restante, 0);
    }

    private List<Usuario> carregarUsuarios() {
        String json = prefs.getString(KEY_USUARIOS, null);

        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<Usuario>>() {}.getType();
        List<Usuario> usuarios = gson.fromJson(json, type);

        if (usuarios == null) {
            return new ArrayList<>();
        }

        for (Usuario usuario : usuarios) {
            normalizarUsuarioLegado(usuario);
        }

        return usuarios;
    }

    private void salvarUsuarios(List<Usuario> usuarios) {
        editor.putString(KEY_USUARIOS, gson.toJson(usuarios));
        editor.apply();
    }

    private void limparCachePerfilApi() {
        editor.remove(KEY_API_NOME);
        editor.remove(KEY_API_USERNAME);
        editor.remove(KEY_API_SOBRENOME);
        editor.remove(KEY_API_IDADE);
        editor.remove(KEY_API_FOTO_URI);
    }

    private void normalizarUsuarioLegado(Usuario usuario) {
        if (usuario == null) {
            return;
        }

        if (usuario.getSobrenome() == null) {
            usuario.setSobrenome("");
        }

        if (usuario.getUsername() == null) {
            usuario.setUsername("");
        }

        if (usuario.getIdade() < 0) {
            usuario.setIdade(0);
        }

        if (usuario.getNome() == null) {
            usuario.setNome("");
        }

        if (usuario.getFotoUri() == null) {
            usuario.setFotoUri("");
        }
    }

    private String montarNomeCompleto(String nome, String sobrenome) {
        String nomeSeguro = valorSeguro(nome);
        String sobrenomeSeguro = valorSeguro(sobrenome);

        if (nomeSeguro.isEmpty() && sobrenomeSeguro.isEmpty()) {
            return "";
        }

        if (sobrenomeSeguro.isEmpty()) {
            return nomeSeguro;
        }

        if (nomeSeguro.isEmpty()) {
            return sobrenomeSeguro;
        }

        return nomeSeguro + " " + sobrenomeSeguro;
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String gerarHashSenha(String senha) {
        try {
            byte[] salt = new byte[SALT_SIZE_BYTES];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(
                    senha.toCharArray(),
                    salt,
                    PBKDF2_ITERATIONS,
                    HASH_SIZE_BITS
            );

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();

            String saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP);
            String hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP);

            return PBKDF2_ITERATIONS + ":" + saltBase64 + ":" + hashBase64;
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar hash da senha", e);
        }
    }

    public void atualizarUsernameUsuario(String usernameAtual, String novoUsername) {
        String atualSeguro = valorSeguro(usernameAtual).toLowerCase();
        String novoSeguro = valorSeguro(novoUsername).toLowerCase();

        if (novoSeguro.isEmpty()) {
            return;
        }

        Usuario usuarioLogado = getUsuarioLogado();
        List<Usuario> usuarios = carregarUsuarios();
        boolean alterado = false;

        for (Usuario usuario : usuarios) {
            normalizarUsuarioLegado(usuario);

            boolean mesmoUsuarioPorEmail = usuarioLogado != null
                    && usuario.getEmail().equalsIgnoreCase(usuarioLogado.getEmail());

            boolean mesmoUsuarioPorUsername = !atualSeguro.isEmpty()
                    && usuario.getUsername().equalsIgnoreCase(atualSeguro);

            if (mesmoUsuarioPorEmail || mesmoUsuarioPorUsername) {
                usuario.setUsername(novoSeguro);
                alterado = true;
                break;
            }
        }

        if (alterado) {
            salvarUsuarios(usuarios);
        }

        editor.putString(KEY_API_USERNAME, novoSeguro);
        editor.apply();
    }

    public boolean usernameDisponivelLocalmente(String usernameDesejado) {
        String usernameSeguro = valorSeguro(usernameDesejado).toLowerCase();
        if (usernameSeguro.isEmpty()) {
            return false;
        }

        Usuario usuarioLogado = getUsuarioLogado();
        List<Usuario> usuarios = carregarUsuarios();

        for (Usuario usuario : usuarios) {
            normalizarUsuarioLegado(usuario);

            boolean mesmoUsuarioLogado = usuarioLogado != null
                    && usuario.getEmail().equalsIgnoreCase(usuarioLogado.getEmail());

            if (!mesmoUsuarioLogado && usuario.getUsername().equalsIgnoreCase(usernameSeguro)) {
                return false;
            }
        }

        return true;
    }

    private boolean verificarSenha(String senhaDigitada, String senhaHashArmazenada) {
        try {
            if (senhaHashArmazenada == null || senhaHashArmazenada.trim().isEmpty()) {
                return false;
            }

            String[] partes = senhaHashArmazenada.split(":");
            if (partes.length != 3) {
                return false;
            }

            int iterations = Integer.parseInt(partes[0]);
            byte[] salt = Base64.decode(partes[1], Base64.NO_WRAP);
            byte[] hashEsperado = Base64.decode(partes[2], Base64.NO_WRAP);

            KeySpec spec = new PBEKeySpec(
                    senhaDigitada.toCharArray(),
                    salt,
                    iterations,
                    hashEsperado.length * 8
            );

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hashAtual = factory.generateSecret(spec).getEncoded();

            return comparacaoConstante(hashEsperado, hashAtual);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean comparacaoConstante(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) {
            return false;
        }

        int resultado = 0;
        for (int i = 0; i < a.length; i++) {
            resultado |= a[i] ^ b[i];
        }
        return resultado == 0;
    }
}