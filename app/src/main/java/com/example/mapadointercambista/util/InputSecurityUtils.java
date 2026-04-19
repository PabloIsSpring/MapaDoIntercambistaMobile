package com.example.mapadointercambista.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class InputSecurityUtils {

    private static final Pattern CONTROL_EXCEPT_LINE_BREAKS =
            Pattern.compile("[\\p{Cntrl}&&[^\n\t]]");

    private static final Pattern MULTIPLE_SPACES =
            Pattern.compile("[ ]{2,}");

    private static final Pattern EXCESSIVE_BREAKS =
            Pattern.compile("\\n{3,}");

    private static final Pattern SUSPICIOUS_HTML_JS = Pattern.compile(
            "(<\\s*script|</\\s*script|javascript\\s*:|data\\s*:\\s*text/html|"
                    + "<\\s*iframe|<\\s*svg|<\\s*object|<\\s*embed|"
                    + "onerror\\s*=|onload\\s*=|onclick\\s*=|onmouseover\\s*=)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern VERY_SUSPICIOUS_SQL = Pattern.compile(
            "(\\bunion\\b\\s+\\bselect\\b|\\bdrop\\b\\s+\\btable\\b|\\bor\\b\\s+1\\s*=\\s*1|--|;)",
            Pattern.CASE_INSENSITIVE
    );

    private InputSecurityUtils() {
    }

    public static String sanitizeUserText(String text) {
        if (text == null) {
            return "";
        }

        String sanitized = Normalizer.normalize(text, Normalizer.Form.NFKC);

        sanitized = CONTROL_EXCEPT_LINE_BREAKS.matcher(sanitized).replaceAll("");
        sanitized = sanitized.replace("\u0000", "");

        sanitized = sanitized.replace("\r\n", "\n").replace('\r', '\n');

        sanitized = MULTIPLE_SPACES.matcher(sanitized).replaceAll(" ");
        sanitized = EXCESSIVE_BREAKS.matcher(sanitized).replaceAll("\n\n");

        return sanitized.trim();
    }

    public static String sanitizeSearchText(String text, int maxLength) {
        String sanitized = sanitizeUserText(text);

        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }

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

        String normalized = sanitizeUserText(text).toLowerCase(Locale.ROOT);

        return SUSPICIOUS_HTML_JS.matcher(normalized).find()
                || VERY_SUSPICIOUS_SQL.matcher(normalized).find();
    }
}