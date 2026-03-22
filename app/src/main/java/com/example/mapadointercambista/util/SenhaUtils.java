package com.example.mapadointercambista.util;

import java.util.regex.Pattern;

public class SenhaUtils {

    private static final Pattern PADRAO_SENHA_FORTE = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%&*_.?\\-])[A-Za-z\\d!@#$%&*_.?\\-]{8,32}$"
    );

    public static boolean senhaForte(String senha) {
        if (senha == null) return false;
        return PADRAO_SENHA_FORTE.matcher(senha).matches();
    }

    public static boolean contemApenasCaracteresPermitidos(String texto) {
        if (texto == null) return false;
        return texto.matches("^[A-Za-z\\d!@#$%&*_.?\\-]{0,32}$");
    }
}