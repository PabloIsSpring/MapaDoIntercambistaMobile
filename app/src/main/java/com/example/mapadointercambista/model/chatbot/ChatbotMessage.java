package com.example.mapadointercambista.model.chatbot;

public class ChatbotMessage {

    private final String texto;
    private final boolean usuario;
    private final boolean digitando;

    public ChatbotMessage(String texto, boolean usuario) {
        this(texto, usuario, false);
    }

    public ChatbotMessage(String texto, boolean usuario, boolean digitando) {
        this.texto = texto;
        this.usuario = usuario;
        this.digitando = digitando;
    }

    public String getTexto() {
        return texto;
    }

    public boolean isUsuario() {
        return usuario;
    }

    public boolean isDigitando() {
        return digitando;
    }
}