package com.example.mapadointercambista.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "mapa_intercambista_session";

    private static final String KEY_NOME = "nome";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_SENHA = "senha";
    private static final String KEY_LOGADO = "logado";

    private static final String KEY_FOTO_URI = "foto_uri";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void cadastrarUsuario(String nome, String email, String senha) {
        editor.putString(KEY_NOME, nome);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_SENHA, senha);
        editor.apply();
    }

    public boolean usuarioExiste() {
        return prefs.contains(KEY_EMAIL) && prefs.contains(KEY_SENHA);
    }

    public boolean fazerLogin(String email, String senha) {
        String emailSalvo = prefs.getString(KEY_EMAIL, null);
        String senhaSalva = prefs.getString(KEY_SENHA, null);

        if (emailSalvo != null && senhaSalva != null &&
                emailSalvo.equals(email) && senhaSalva.equals(senha)) {
            editor.putBoolean(KEY_LOGADO, true);
            editor.apply();
            return true;
        }

        return false;
    }

    public void logout() {
        editor.putBoolean(KEY_LOGADO, false);
        editor.apply();
    }

    public boolean estaLogado() {
        return prefs.getBoolean(KEY_LOGADO, false);
    }

    public String getNomeUsuario() {
        return prefs.getString(KEY_NOME, "");
    }

    public String getEmailUsuario() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public void salvarFotoUsuario(String fotoUri) {
        editor.putString(KEY_FOTO_URI, fotoUri);
        editor.apply();
    }

    public String getFotoUsuario() {
        return prefs.getString(KEY_FOTO_URI, "");
    }
}