package com.example.mapadointercambista.util;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class ImageUtils {

    private static final Map<String, Integer> drawableCache = new HashMap<>();

    public static int getDrawableId(Context context, String nomeImagem) {
        if (nomeImagem == null || nomeImagem.trim().isEmpty()) {
            return 0;
        }

        String chave = nomeImagem.trim().toLowerCase();

        Integer cached = drawableCache.get(chave);
        if (cached != null) {
            return cached;
        }

        int id = context.getResources().getIdentifier(
                chave,
                "drawable",
                context.getPackageName()
        );

        drawableCache.put(chave, id);
        return id;
    }
}