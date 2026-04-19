package com.example.mapadointercambista.model.forum;

import com.example.mapadointercambista.util.TimeUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RespostaForum implements Serializable {

    private String id;
    private String autorNome;
    private String autorEmail;
    private String autorFotoUri;
    private String mensagem;
    private long criadoEm;

    private int nivel;
    private boolean visivel;
    private boolean temRespostas;

    private List<String> usuariosLike;
    private List<String> usuariosDislike;

    public RespostaForum(String autorNome, String autorEmail, String autorFotoUri,
                         String mensagem, long criadoEm,
                         int nivel, boolean visivel, boolean temRespostas) {
        this.id = UUID.randomUUID().toString();
        this.autorNome = autorNome;
        this.autorEmail = autorEmail;
        this.autorFotoUri = autorFotoUri;
        this.mensagem = mensagem;
        this.criadoEm = criadoEm;
        this.nivel = nivel;
        this.visivel = visivel;
        this.temRespostas = temRespostas;
        this.usuariosLike = new ArrayList<>();
        this.usuariosDislike = new ArrayList<>();
    }

    public RespostaForum(String id, String autorNome, String autorEmail, String autorFotoUri,
                         String mensagem, long criadoEm,
                         int nivel, boolean visivel, boolean temRespostas,
                         List<String> usuariosLike, List<String> usuariosDislike) {
        this.id = id;
        this.autorNome = autorNome;
        this.autorEmail = autorEmail;
        this.autorFotoUri = autorFotoUri;
        this.mensagem = mensagem;
        this.criadoEm = criadoEm;
        this.nivel = nivel;
        this.visivel = visivel;
        this.temRespostas = temRespostas;
        this.usuariosLike = usuariosLike != null ? usuariosLike : new ArrayList<>();
        this.usuariosDislike = usuariosDislike != null ? usuariosDislike : new ArrayList<>();
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

    public int getNivel() {
        return nivel;
    }

    public boolean isVisivel() {
        return visivel;
    }

    public boolean isTemRespostas() {
        return temRespostas;
    }

    public List<String> getUsuariosLike() {
        return usuariosLike;
    }

    public List<String> getUsuariosDislike() {
        return usuariosDislike;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    public void setTemRespostas(boolean temRespostas) {
        this.temRespostas = temRespostas;
    }

    public int getLikes() {
        return usuariosLike != null ? usuariosLike.size() : 0;
    }

    public int getDislikes() {
        return usuariosDislike != null ? usuariosDislike.size() : 0;
    }

    public void setAutorNome(String autorNome) {
        this.autorNome = autorNome;
    }

    public void setAutorEmail(String autorEmail) {
        this.autorEmail = autorEmail;
    }

    public void setAutorFotoUri(String autorFotoUri) {
        this.autorFotoUri = autorFotoUri;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public boolean usuarioCurtiu(String emailUsuario) {
        String emailNormalizado = normalizarEmail(emailUsuario);
        return !emailNormalizado.isEmpty()
                && usuariosLike != null
                && usuariosLike.contains(emailNormalizado);
    }

    public boolean usuarioDescurtiu(String emailUsuario) {
        String emailNormalizado = normalizarEmail(emailUsuario);
        return !emailNormalizado.isEmpty()
                && usuariosDislike != null
                && usuariosDislike.contains(emailNormalizado);
    }

    private String normalizarEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}