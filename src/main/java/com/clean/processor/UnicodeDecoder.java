package com.clean.processor;

import org.apache.commons.text.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UnicodeDecoder {
    private static final Pattern UNICODE_ESCAPE = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

    private UnicodeDecoder() {}

    public static String decode(String input) {
        if (input == null || input.isEmpty()) return input;
        String result = input;
        int i = 0;
        while (i < 3 && result.matches(".*\\\\u[0-9a-fA-F]{4}.*")) {
            int oldLength = result.length();
            result = StringEscapeUtils.unescapeJava(result);
            if (oldLength == result.length()) break;
            i++;
        }
        return result;
    }

    public static String decodeUnicodeOnly(String input) {
        if (input == null || input.isEmpty()) return input;
        Matcher m = UNICODE_ESCAPE.matcher(input);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            char c = (char) Integer.parseInt(m.group(1), 16);
            m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(c)));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
