package com.example.mapadointercambista.dto.request;

public class RegisterUserRequestDto {

    private String nome;
    private String email;
    private String password;

    public RegisterUserRequestDto(String nome, String email, String password) {
        this.nome = nome;
        this.email = email;
        this.password = password;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}