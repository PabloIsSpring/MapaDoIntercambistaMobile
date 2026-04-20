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

    private String titulo;
    private String mensagem;
    private String imagemUri;
    private long criadoEm;

    private List<String> usuariosLike;
    private List<String> usuariosDislike;
    private List<RespostaForum> respostas;

    public PostForum(String autorNome, String autorEmail, String autorFotoUri,
                     String titulo, String mensagem, String imagemUri, long criadoEm) {
        this.id = UUID.randomUUID().toString();
        this.autorNome = autorNome;
        this.autorEmail = autorEmail;
        this.autorFotoUri = autorFotoUri;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.imagemUri = imagemUri;
        this.criadoEm = criadoEm;
        this.usuariosLike = new ArrayList<>();
        this.usuariosDislike = new ArrayList<>();
        this.respostas = new ArrayList<>();
    }

    public PostForum(String id, String autorNome, String autorEmail, String autorFotoUri,
                     String titulo, String mensagem, String imagemUri, long criadoEm,
                     List<String> usuariosLike, List<String> usuariosDislike,
                     List<RespostaForum> respostas) {
        this.id = id;
        this.autorNome = autorNome;
        this.autorEmail = autorEmail;
        this.autorFotoUri = autorFotoUri;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.imagemUri = imagemUri;
        this.criadoEm = criadoEm;
        this.usuariosLike = usuariosLike != null ? usuariosLike : new ArrayList<>();
        this.usuariosDislike = usuariosDislike != null ? usuariosDislike : new ArrayList<>();
        this.respostas = respostas != null ? respostas : new ArrayList<>();
    }

    public PostForum(String autorNome, String autorEmail, String autorFotoUri,
                     String titulo, String mensagem, long criadoEm) {
        this(
                autorNome,
                autorEmail,
                autorFotoUri,
                titulo,
                mensagem,
                "",
                criadoEm
        );
    }

    public PostForum(String id, String autorNome, String autorEmail, String autorFotoUri,
                     String titulo, String mensagem, long criadoEm,
                     List<String> usuariosLike, List<String> usuariosDislike,
                     List<RespostaForum> respostas) {
        this(
                id,
                autorNome,
                autorEmail,
                autorFotoUri,
                titulo,
                mensagem,
                "",
                criadoEm,
                usuariosLike,
                usuariosDislike,
                respostas
        );
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

    public String getTitulo() {
        return titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getImagemUri() {
        return imagemUri;
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

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public void setImagemUri(String imagemUri) {
        this.imagemUri = imagemUri;
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

    public void setRespostas(List<RespostaForum> respostas) {
        this.respostas = respostas != null ? respostas : new ArrayList<>();
    }

    public void setUsuariosLike(List<String> usuariosLike) {
        this.usuariosLike = usuariosLike != null ? usuariosLike : new ArrayList<>();
    }

    public void setUsuariosDislike(List<String> usuariosDislike) {
        this.usuariosDislike = usuariosDislike != null ? usuariosDislike : new ArrayList<>();
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