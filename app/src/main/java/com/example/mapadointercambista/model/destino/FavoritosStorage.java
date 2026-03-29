package com.example.mapadointercambista.model.destino;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class FavoritosStorage {

    private static final String PREF_NAME = "favoritos_storage";
    private static final String KEY_PREFIX = "favoritos_";

    private final SharedPreferences prefs;
    private final Gson gson;

    public FavoritosStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    private String getKey(String emailUsuario) {
        return KEY_PREFIX + (emailUsuario != null ? emailUsuario.trim().toLowerCase() : "anonimo");
    }

    public Set<String> carregarFavoritos(String emailUsuario) {
        String json = prefs.getString(getKey(emailUsuario), null);

        if (json == null) {
            return new HashSet<>();
        }

        Type type = new TypeToken<Set<String>>() {}.getType();
        Set<String> favoritos = gson.fromJson(json, type);

        return favoritos != null ? favoritos : new HashSet<>();
    }

    public void salvarFavoritos(String emailUsuario, Set<String> favoritos) {
        prefs.edit()
                .putString(getKey(emailUsuario), gson.toJson(favoritos))
                .apply();
    }

    public boolean isFavorito(String emailUsuario, String destinoId) {
        return carregarFavoritos(emailUsuario).contains(destinoId);
    }

    public boolean toggleFavorito(String emailUsuario, String destinoId) {
        Set<String> favoritos = carregarFavoritos(emailUsuario);

        boolean agoraFavorito;
        if (favoritos.contains(destinoId)) {
            favoritos.remove(destinoId);
            agoraFavorito = false;
        } else {
            favoritos.add(destinoId);
            agoraFavorito = true;
        }

        salvarFavoritos(emailUsuario, favoritos);
        return agoraFavorito;
    }
}