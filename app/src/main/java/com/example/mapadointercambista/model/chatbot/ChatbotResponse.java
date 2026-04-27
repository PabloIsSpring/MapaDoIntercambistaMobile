package com.example.mapadointercambista.model.chatbot;

public class ChatbotResponse {

    private final String resposta;
    private final String intencao;
    private final int confianca;
    private final String[] sugestoes;

    public ChatbotResponse(String resposta, String intencao, int confianca, String[] sugestoes) {
        this.resposta = resposta;
        this.intencao = intencao;
        this.confianca = confianca;
        this.sugestoes = sugestoes;
    }

    public String getResposta() {
        return resposta;
    }

    public String getIntencao() {
        return intencao;
    }

    public int getConfianca() {
        return confianca;
    }

    public String[] getSugestoes() {
        return sugestoes;
    }
}