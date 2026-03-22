package com.example.mapadointercambista.model;

import android.util.Patterns;

public class EmailUtils {

    public static boolean emailValido(String email) {
        return email != null
                && !email.trim().isEmpty()
                && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }
}