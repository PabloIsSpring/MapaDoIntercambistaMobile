package com.example.mapadointercambista.model.user;

import java.io.Serializable;

public class Usuario implements Serializable {

    private String nome;
    private String email;
    private String senhaHash;
    private String fotoUri;

    public Usuario(String nome, String email, String senhaHash, String fotoUri) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.fotoUri = fotoUri;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getFotoUri() {
        return fotoUri;
    }

    public void setFotoUri(String fotoUri) {
        this.fotoUri = fotoUri;
    }
}