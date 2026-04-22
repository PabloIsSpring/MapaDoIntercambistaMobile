package com.example.mapadointercambista.dto.request;

public class CreateAgenciaRequestDto {

    private String email;
    private String password;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private String username;

    public CreateAgenciaRequestDto(String email,
                                   String password,
                                   String razaoSocial,
                                   String nomeFantasia,
                                   String cnpj,
                                   String username) {
        this.email = email;
        this.password = password;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getUsername() {
        return username;
    }
}