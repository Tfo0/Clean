package com.clean.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UrlDetector {
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)",
            Pattern.CASE_INSENSITIVE);

    private UrlDetector() {}

    public static String urlAt(String text, int offset) {
        if (text == null) return null;
        Matcher m = URL_PATTERN.matcher(text);
        while (m.find()) {
            if (offset >= m.start() && offset <= m.end()) return m.group();
        }
        return null;
    }

    public static int[] rangeAt(String text, int offset) {
        if (text == null) return null;
        Matcher m = URL_PATTERN.matcher(text);
        while (m.find()) {
            if (offset >= m.start() && offset <= m.end()) return new int[]{m.start(), m.end()};
        }
        return null;
    }
}
