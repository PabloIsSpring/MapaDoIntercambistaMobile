package com.example.mapadointercambista.dto.request;

public class IntercambistaUpdtRequestDto {

    private String username;
    private String nUsername;

    public IntercambistaUpdtRequestDto(String username, String nUsername) {
        this.username = username;
        this.nUsername = nUsername;
    }

    public String getUsername() {
        return username;
    }

    public String getnUsername() {
        return nUsername;
    }
}