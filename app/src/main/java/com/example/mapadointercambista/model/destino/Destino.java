package com.example.mapadointercambista.model.destino;

public class Destino {

    private String nome;
    private int imagem;
    private float avaliacao;
    private int reviews;

    public Destino(String nome, int imagem, float avaliacao, int reviews) {
        this.nome = nome;
        this.imagem = imagem;
        this.avaliacao = avaliacao;
        this.reviews = reviews;
    }

    public String getNome() {
        return nome;
    }

    public int getImagem() {
        return imagem;
    }

    public float getAvaliacao() {
        return avaliacao;
    }

    public int getReviews() {
        return reviews;
    }
}