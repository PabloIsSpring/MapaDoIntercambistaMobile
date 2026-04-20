package com.example.mapadointercambista.model.user;

import java.io.Serializable;

public class Usuario implements Serializable {

    private String nome;
    private String sobrenome;
    private String username;
    private int idade;
    private String email;
    private String senhaHash;
    private String fotoUri;

    public Usuario(String nome,
                   String sobrenome,
                   String username,
                   int idade,
                   String email,
                   String senhaHash,
                   String fotoUri) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.username = username;
        this.idade = idade;
        this.email = email;
        this.senhaHash = senhaHash;
        this.fotoUri = fotoUri;
    }

    // Construtor de compatibilidade para registros antigos
    public Usuario(String nome, String email, String senhaHash, String fotoUri) {
        this.nome = nome;
        this.sobrenome = "";
        this.username = "";
        this.idade = 0;
        this.email = email;
        this.senhaHash = senhaHash;
        this.fotoUri = fotoUri;
    }

    public String getNome() {
        return nome != null ? nome : "";
    }

    public String getSobrenome() {
        return sobrenome != null ? sobrenome : "";
    }

    public String getNomeCompleto() {
        String nomeSeguro = getNome().trim();
        String sobrenomeSeguro = getSobrenome().trim();

        if (nomeSeguro.isEmpty() && sobrenomeSeguro.isEmpty()) {
            return "Usuário";
        }

        if (sobrenomeSeguro.isEmpty()) {
            return nomeSeguro;
        }

        if (nomeSeguro.isEmpty()) {
            return sobrenomeSeguro;
        }

        return nomeSeguro + " " + sobrenomeSeguro;
    }

    public String getUsername() {
        return username != null ? username : "";
    }

    public int getIdade() {
        return Math.max(idade, 0);
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public String getSenhaHash() {
        return senhaHash != null ? senhaHash : "";
    }

    public String getFotoUri() {
        return fotoUri != null ? fotoUri : "";
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setIdade(int idade) {
        this.idade = Math.max(idade, 0);
    }

    public void setFotoUri(String fotoUri) {
        this.fotoUri = fotoUri;
    }
}