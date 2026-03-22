package com.example.mapadointercambista.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class AvatarUtils {

    public static Bitmap criarAvatarComInicial(Context context, String nome, int tamanhoPx) {
        Bitmap bitmap = Bitmap.createBitmap(tamanhoPx, tamanhoPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint fundo = new Paint(Paint.ANTI_ALIAS_FLAG);
        fundo.setColor(corPeloNome(nome));
        canvas.drawCircle(tamanhoPx / 2f, tamanhoPx / 2f, tamanhoPx / 2f, fundo);

        String inicial = obterInicial(nome);

        Paint texto = new Paint(Paint.ANTI_ALIAS_FLAG);
        texto.setColor(Color.WHITE);
        texto.setTextAlign(Paint.Align.CENTER);
        texto.setTextSize(tamanhoPx * 0.42f);
        texto.setFakeBoldText(true);

        Paint.FontMetrics fontMetrics = texto.getFontMetrics();
        float y = tamanhoPx / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f;

        canvas.drawText(inicial, tamanhoPx / 2f, y, texto);

        return bitmap;
    }

    private static String obterInicial(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return "?";
        }

        return String.valueOf(nome.trim().toUpperCase().charAt(0));
    }

    private static int corPeloNome(String nome) {
        int[] cores = new int[]{
                Color.parseColor("#5C6BC0"),
                Color.parseColor("#26A69A"),
                Color.parseColor("#EF5350"),
                Color.parseColor("#FF7043"),
                Color.parseColor("#AB47BC"),
                Color.parseColor("#42A5F5"),
                Color.parseColor("#66BB6A")
        };

        int indice = Math.abs((nome != null ? nome.hashCode() : 0)) % cores.length;
        return cores[indice];
    }
}