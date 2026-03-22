package com.example.mapadointercambista.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ForumStorage {

    private static final String PREF_NAME = "forum_storage";
    private static final String KEY_POSTS = "posts";
    private static final String KEY_RESPOSTAS = "respostas";

    private final SharedPreferences prefs;
    private final Gson gson;

    public ForumStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void salvarPosts(List<PostForum> posts) {
        String json = gson.toJson(posts);
        prefs.edit().putString(KEY_POSTS, json).apply();
    }

    public List<PostForum> carregarPosts() {
        String json = prefs.getString(KEY_POSTS, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<PostForum>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void salvarRespostas(List<RespostaForum> respostas) {
        String json = gson.toJson(respostas);
        prefs.edit().putString(KEY_RESPOSTAS, json).apply();
    }

    public List<RespostaForum> carregarRespostas() {
        String json = prefs.getString(KEY_RESPOSTAS, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<List<RespostaForum>>() {}.getType();
        return gson.fromJson(json, type);
    }
}