package com.example.mapadointercambista.util;

public class TimeUtils {

    public static long agora() {
        return System.currentTimeMillis();
    }

    public static String formatarTempoRelativo(long timestamp) {
        long diferenca = System.currentTimeMillis() - timestamp;

        long segundos = diferenca / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        long dias = horas / 24;

        if (segundos < 60) {
            return "agora";
        } else if (minutos < 60) {
            return "há " + minutos + "min";
        } else if (horas < 24) {
            return "há " + horas + "h";
        } else {
            return "há " + dias + "d";
        }
    }
}