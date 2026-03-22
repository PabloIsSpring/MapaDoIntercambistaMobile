package com.example.mapadointercambista.util;

import android.text.InputFilter;
import android.text.Spanned;

public class FiltroSenha implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {

        for (int i = start; i < end; i++) {
            char c = source.charAt(i);

            boolean permitido =
                    (c >= 'a' && c <= 'z') ||
                            (c >= 'A' && c <= 'Z') ||
                            (c >= '0' && c <= '9') ||
                            c == '!' || c == '@' || c == '#' || c == '$' ||
                            c == '%' || c == '&' || c == '*' || c == '_' ||
                            c == '-' || c == '.' || c == '?';

            if (!permitido) {
                return "";
            }
        }

        return null;
    }
}