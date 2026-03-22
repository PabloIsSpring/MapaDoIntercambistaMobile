package com.example.mapadointercambista.model.user;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    private static final String PREF_NAME = "mapa_intercambista_session";
    private static final String KEY_USUARIOS = "usuarios";
    private static final String KEY_EMAIL_LOGADO = "email_logado";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    public boolean cadastrarUsuario(String nome, String email, String senha) {
        List<Usuario> usuarios = carregarUsuarios();

        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equalsIgnoreCase(email)) {
                return false;
            }
        }

        usuarios.add(new Usuario(nome, email, senha, ""));
        salvarUsuarios(usuarios);
        return true;
    }

    public boolean usuarioExiste() {
        return !carregarUsuarios().isEmpty();
    }

    public boolean fazerLogin(String email, String senha) {
        List<Usuario> usuarios = carregarUsuarios();

        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equalsIgnoreCase(email) && usuario.getSenha().equals(senha)) {
                editor.putString(KEY_EMAIL_LOGADO, usuario.getEmail());
                editor.apply();
                return true;
            }
        }

        return false;
    }

    public void logout() {
        editor.remove(KEY_EMAIL_LOGADO);
        editor.apply();
    }

    public boolean estaLogado() {
        return getUsuarioLogado() != null;
    }

    public Usuario getUsuarioLogado() {
        String emailLogado = prefs.getString(KEY_EMAIL_LOGADO, null);

        if (emailLogado == null || emailLogado.isEmpty()) {
            return null;
        }

        List<Usuario> usuarios = carregarUsuarios();

        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equalsIgnoreCase(emailLogado)) {
                return usuario;
            }
        }

        return null;
    }

    public String getNomeUsuario() {
        Usuario usuario = getUsuarioLogado();
        return usuario != null ? usuario.getNome() : "";
    }

    public String getEmailUsuario() {
        Usuario usuario = getUsuarioLogado();
        return usuario != null ? usuario.getEmail() : "";
    }

    public void salvarFotoUsuario(String fotoUri) {
        Usuario usuarioLogado = getUsuarioLogado();

        if (usuarioLogado == null) {
            return;
        }

        List<Usuario> usuarios = carregarUsuarios();

        for (Usuario usuario : usuarios) {
            if (usuario.getEmail().equalsIgnoreCase(usuarioLogado.getEmail())) {
                usuario.setFotoUri(fotoUri);
                break;
            }
        }

        salvarUsuarios(usuarios);
    }

    public String getFotoUsuario() {
        Usuario usuario = getUsuarioLogado();
        return usuario != null ? usuario.getFotoUri() : "";
    }

    private List<Usuario> carregarUsuarios() {
        String json = prefs.getString(KEY_USUARIOS, null);

        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<Usuario>>() {}.getType();
        List<Usuario> usuarios = gson.fromJson(json, type);
        return usuarios != null ? usuarios : new ArrayList<>();
    }

    private void salvarUsuarios(List<Usuario> usuarios) {
        editor.putString(KEY_USUARIOS, gson.toJson(usuarios));
        editor.apply();
    }
}