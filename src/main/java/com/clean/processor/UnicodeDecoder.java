package com.clean.processor;

import org.apache.commons.text.StringEscapeUtils;

public final class UnicodeDecoder {
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
}
