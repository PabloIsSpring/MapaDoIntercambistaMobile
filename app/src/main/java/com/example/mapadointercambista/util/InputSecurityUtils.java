package com.example.mapadointercambista.util;

public final class InputSecurityUtils {

    private InputSecurityUtils() {
    }

    public static String sanitizeUserText(String text) {
        if (text == null) {
            return "";
        }

        String sanitized = text
                .replaceAll("[\\p{Cntrl}&&[^\n\t]]", "")
                .replace("\u0000", "")
                .trim();

        sanitized = sanitized.replaceAll("[ ]{2,}", " ");
        sanitized = sanitized.replaceAll("\\n{3,}", "\n\n");

        return sanitized;
    }

    public static boolean isNullOrBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean exceedsMaxLength(String text, int maxLength) {
        return text != null && text.length() > maxLength;
    }

    public static boolean containsSuspiciousPattern(String text) {
        if (text == null) {
            return false;
        }

        String lower = text.toLowerCase();

        return lower.contains("<script")
                || lower.contains("javascript:")
                || lower.contains(" onerror=")
                || lower.contains(" onload=");
    }
}