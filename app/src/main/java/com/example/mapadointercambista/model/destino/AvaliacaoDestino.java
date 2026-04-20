package com.example.mapadointercambista.model.destino;

import com.example.mapadointercambista.util.TimeUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AvaliacaoDestino implements Serializable {

    private String id;
    private String autorNome;
    private String autorEmail;
    private String autorFotoUri;
    private String mensagem;
    private float notaEstrelas;
    private String agenciaEscolhida;
    private long criadoEm;

    private List<String> usuariosLike;
    private List<String> usuariosDislike;

    public AvaliacaoDestino(String autorNome, String autorEmail, String autorFotoUri,
                            String mensagem, float notaEstrelas, String agenciaEscolhida, long criadoEm) {
        this.id = UUID.randomUUID().toString();
        this.autorNome = autorNome;
        this.autorEmail = autorEmail;
        this.autorFotoUri = autorFotoUri;
        this.mensagem = mensagem;
        this.notaEstrelas = notaEstrelas;
        this.agenciaEscolhida = agenciaEscolhida;
        this.criadoEm = criadoEm;
        this.usuariosLike = new ArrayList<>();
        this.usuariosDislike = new ArrayList<>();
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

    public float getNotaEstrelas() {
        return notaEstrelas;
    }

    public String getAgenciaEscolhida() {
        return agenciaEscolhida;
    }

    public long getCriadoEm() {
        return criadoEm;
    }

    public String getTempoFormatado() {
        return TimeUtils.formatarTempoRelativo(criadoEm);
    }

    public List<String> getUsuariosLike() {
        return usuariosLike;
    }

    public List<String> getUsuariosDislike() {
        return usuariosDislike;
    }

    public int getLikes() {
        return usuariosLike != null ? usuariosLike.size() : 0;
    }

    public int getDislikes() {
        return usuariosDislike != null ? usuariosDislike.size() : 0;
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

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public void setNotaEstrelas(float notaEstrelas) {
        this.notaEstrelas = notaEstrelas;
    }

    public void setAgenciaEscolhida(String agenciaEscolhida) {
        this.agenciaEscolhida = agenciaEscolhida;
    }

    public void setUsuariosLike(List<String> usuariosLike) {
        this.usuariosLike = usuariosLike != null ? usuariosLike : new ArrayList<>();
    }

    public void setUsuariosDislike(List<String> usuariosDislike) {
        this.usuariosDislike = usuariosDislike != null ? usuariosDislike : new ArrayList<>();
    }

    private String normalizarEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}