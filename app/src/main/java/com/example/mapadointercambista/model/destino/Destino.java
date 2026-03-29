package com.example.mapadointercambista.model.destino;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Destino implements Serializable {

    private String id;
    private String nome;
    private String imagemNome;

    private String pais;
    private String idioma;
    private String moeda;
    private String descricao;

    private float nota;
    private int avaliacoes;

    private List<String> agencias;
    private List<AvaliacaoDestino> listaAvaliacoes;
    private String continente;

    public Destino(String id,
                   String nome,
                   String imagemNome,
                   String pais,
                   String continente,
                   String idioma,
                   String moeda,
                   String descricao,
                   List<String> agencias,
                   List<AvaliacaoDestino> listaAvaliacoes) {

        this.id = id;
        this.nome = nome;
        this.imagemNome = imagemNome;
        this.pais = pais;
        this.continente = continente;
        this.idioma = idioma;
        this.moeda = moeda;
        this.descricao = descricao;
        this.agencias = agencias != null ? agencias : new ArrayList<>();
        this.listaAvaliacoes = listaAvaliacoes != null ? listaAvaliacoes : new ArrayList<>();

        recalcularResumoAvaliacoes();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getImagemNome() {
        return imagemNome;
    }

    public String getPais() {
        return pais;
    }

    public String getIdioma() {
        return idioma;
    }

    public String getMoeda() {
        return moeda;
    }

    public String getDescricao() {
        return descricao;
    }

    public float getNota() {
        return nota;
    }

    public int getAvaliacoes() {
        return avaliacoes;
    }

    public List<String> getAgencias() {
        return agencias;
    }

    public List<AvaliacaoDestino> getListaAvaliacoes() {
        return listaAvaliacoes;
    }

    public void setListaAvaliacoes(List<AvaliacaoDestino> listaAvaliacoes) {
        this.listaAvaliacoes = listaAvaliacoes != null ? listaAvaliacoes : new ArrayList<>();
        recalcularResumoAvaliacoes();
    }

    public void recalcularResumoAvaliacoes() {
        if (listaAvaliacoes == null || listaAvaliacoes.isEmpty()) {
            nota = 0f;
            avaliacoes = 0;
            return;
        }

        float soma = 0f;
        for (AvaliacaoDestino avaliacao : listaAvaliacoes) {
            soma += avaliacao.getNotaEstrelas();
        }

        avaliacoes = listaAvaliacoes.size();
        nota = soma / avaliacoes;
    }

    public String getContinente() {
        return continente;
    }

}