package com.example.mapadointercambista.dto.request;

public class RegisterUserRequestDto {

    private String nome;
    private String email;
    private String password;
    private String username;
    private String sobrenome;
    private int idade;

    public RegisterUserRequestDto(String nome,
                                  String email,
                                  String password,
                                  String username,
                                  String sobrenome,
                                  int idade) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.username = username;
        this.sobrenome = sobrenome;
        this.idade = idade;
    }

    // Construtor de transição para não quebrar o app antes da Etapa 2
    public RegisterUserRequestDto(String nome, String email, String password) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.username = gerarUsernameTemporario(email);
        this.sobrenome = "Nao informado";
        this.idade = 0;
    }

    private String gerarUsernameTemporario(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "usuario_temp";
        }

        String base = email.trim().toLowerCase();
        int arroba = base.indexOf("@");
        if (arroba > 0) {
            base = base.substring(0, arroba);
        }

        base = base.replaceAll("[^a-z0-9._]", "");
        if (base.isEmpty()) {
            return "usuario_temp";
        }

        return base;
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

    public String getUsername() {
        return username;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public int getIdade() {
        return idade;
    }
}