package com.example.mapadointercambista.model;

import java.io.Serializable;

public class PostForum implements Serializable {

    private String usuario;
    private String mensagem;
    private int fotoPerfil;
    private int likes;
    private int dislikes;
    private String tempoPostagem;
    private int quantidadeRespostas;

    public PostForum(String usuario, String mensagem, int fotoPerfil, int likes, int dislikes, String tempoPostagem, int quantidadeRespostas) {
        this.usuario = usuario;
        this.mensagem = mensagem;
        this.fotoPerfil = fotoPerfil;
        this.likes = likes;
        this.dislikes = dislikes;
        this.tempoPostagem = tempoPostagem;
        this.quantidadeRespostas = quantidadeRespostas;
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

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public String getTempoPostagem() {
        return tempoPostagem;
    }

    public int getQuantidadeRespostas() {
        return quantidadeRespostas;
    }
}