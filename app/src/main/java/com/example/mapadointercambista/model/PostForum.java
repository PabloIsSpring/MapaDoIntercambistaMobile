package com.example.mapadointercambista.model;

public class PostForum {

    private String usuario;
    private String mensagem;
    private int fotoPerfil;
    private float avaliacao;
    private int reviews;

    public PostForum(String usuario, String mensagem, int fotoPerfil, float avaliacao, int reviews) {
        this.usuario = usuario;
        this.mensagem = mensagem;
        this.fotoPerfil = fotoPerfil;
        this.avaliacao = avaliacao;
        this.reviews = reviews;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getMensagem() {
        return mensagem;
    }

    public int getFotoPerfil() {
        return fotoPerfil;
    }

    public float getAvaliacao() {
        return avaliacao;
    }

    public int getReviews() {
        return reviews;
    }
}