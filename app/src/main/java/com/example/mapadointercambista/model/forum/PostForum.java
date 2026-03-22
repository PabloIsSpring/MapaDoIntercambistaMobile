package com.example.mapadointercambista.model.forum;

import com.example.mapadointercambista.util.TimeUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostForum implements Serializable {

    private String id;
    private String autorNome;
    private String autorEmail;
    private String autorFotoUri;
    private String mensagem;
    private long criadoEm;

    private List<String> usuariosLike;
    private List<String> usuariosDislike;
    private List<RespostaForum> respostas;

    public PostForum(String autorNome, String autorEmail, String autorFotoUri,
                     String mensagem, long criadoEm) {
        this.id = UUID.randomUUID().toString();
        this.autorNome = autorNome;
        this.autorEmail = autorEmail;
        this.autorFotoUri = autorFotoUri;
        this.mensagem = mensagem;
        this.criadoEm = criadoEm;
        this.usuariosLike = new ArrayList<>();
        this.usuariosDislike = new ArrayList<>();
        this.respostas = new ArrayList<>();
    }

    public PostForum(String id, String autorNome, String autorEmail, String autorFotoUri,
                     String mensagem, long criadoEm,
                     List<String> usuariosLike, List<String> usuariosDislike,
                     List<RespostaForum> respostas) {
        this.id = id;
        this.autorNome = autorNome;
        this.autorEmail = autorEmail;
        this.autorFotoUri = autorFotoUri;
        this.mensagem = mensagem;
        this.criadoEm = criadoEm;
        this.usuariosLike = usuariosLike != null ? usuariosLike : new ArrayList<>();
        this.usuariosDislike = usuariosDislike != null ? usuariosDislike : new ArrayList<>();
        this.respostas = respostas != null ? respostas : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getAutorNome() {
        return autorNome;
    }

    public String getAutorEmail() {
        return autorEmail;
    }

    public String getAutorFotoUri() {
        return autorFotoUri;
    }

    public String getMensagem() {
        return mensagem;
    }

    public long getCriadoEm() {
        return criadoEm;
    }

    public String getTempoPostagem() {
        return TimeUtils.formatarTempoRelativo(criadoEm);
    }

    public List<String> getUsuariosLike() {
        return usuariosLike;
    }

    public List<String> getUsuariosDislike() {
        return usuariosDislike;
    }

    public List<RespostaForum> getRespostas() {
        return respostas;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public void setRespostas(List<RespostaForum> respostas) {
        this.respostas = respostas != null ? respostas : new ArrayList<>();
    }

    public int getLikes() {
        return usuariosLike != null ? usuariosLike.size() : 0;
    }

    public int getDislikes() {
        return usuariosDislike != null ? usuariosDislike.size() : 0;
    }

    public int getQuantidadeRespostas() {
        return respostas != null ? respostas.size() : 0;
    }

    public boolean usuarioCurtiu(String emailUsuario) {
        return emailUsuario != null && usuariosLike != null && usuariosLike.contains(emailUsuario);
    }

    public boolean usuarioDescurtiu(String emailUsuario) {
        return emailUsuario != null && usuariosDislike != null && usuariosDislike.contains(emailUsuario);
    }
}