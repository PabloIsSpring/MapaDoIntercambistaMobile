package com.example.mapadointercambista.model.user;

import java.io.Serializable;

public class Usuario implements Serializable {

    private String nome;
    private String email;
    private String senha;
    private String fotoUri;

    public Usuario(String nome, String email, String senha, String fotoUri) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.fotoUri = fotoUri;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getFotoUri() {
        return fotoUri;
    }

    public void setFotoUri(String fotoUri) {
        this.fotoUri = fotoUri;
    }
}