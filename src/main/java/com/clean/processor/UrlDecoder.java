package com.clean.processor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class UrlDecoder {
    private UrlDecoder() {}

    public static String decodeFully(String input) throws UnsupportedEncodingException {
        if (input == null || input.isEmpty()) return input;
        String result = input;
        int i = 0;
        while (i < 5) {
            String decoded = URLDecoder.decode(result, "UTF-8");
            if (decoded.equals(result)) break;
            result = decoded;
            i++;
        }
        return result;
    }

    public static String decodeSafe(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }
}
