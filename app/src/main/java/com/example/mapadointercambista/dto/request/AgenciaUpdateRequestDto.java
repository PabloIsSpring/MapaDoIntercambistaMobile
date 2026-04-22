package com.example.mapadointercambista.dto.request;

public class AgenciaUpdateRequestDto {

    private String username;
    private String newUsername;
    private String nomeFantasia;
    private String razaoSocial;
    private String cnpj;

    public AgenciaUpdateRequestDto(String username,
                                   String newUsername,
                                   String nomeFantasia,
                                   String razaoSocial,
                                   String cnpj) {
        this.username = username;
        this.newUsername = newUsername;
        this.nomeFantasia = nomeFantasia;
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
    }

    public String getUsername() {
        return username;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }
}