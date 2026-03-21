package com.example.mapadointercambista.model;

public class RespostaForum {

    private String usuario;
    private String mensagem;
    private int fotoPerfil;
    private String tempoPostagem;
    private int likes;
    private int dislikes;
    private int nivel;
    private boolean visivel;
    private boolean temRespostas;

    public RespostaForum(String usuario, String mensagem, int fotoPerfil, String tempoPostagem,
                         int likes, int dislikes, int nivel, boolean visivel, boolean temRespostas) {
        this.usuario = usuario;
        this.mensagem = mensagem;
        this.fotoPerfil = fotoPerfil;
        this.tempoPostagem = tempoPostagem;
        this.likes = likes;
        this.dislikes = dislikes;
        this.nivel = nivel;
        this.visivel = visivel;
        this.temRespostas = temRespostas;
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

    public String getTempoPostagem() {
        return tempoPostagem;
    }

    public int getLikes() {
        return likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public int getNivel() {
        return nivel;
    }

    public boolean isVisivel() {
        return visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    public boolean isTemRespostas() {
        return temRespostas;
    }
}