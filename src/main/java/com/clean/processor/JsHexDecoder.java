package com.clean.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsHexDecoder {
    private static final Pattern HEX_ESCAPE = Pattern.compile("\\\\x([0-9a-fA-F]{2})");
    private static final Pattern UNICODE_ESCAPE = Pattern.compile("\\\\u([0-9a-fA-F]{4})");

    private JsHexDecoder() {}

    public static String decode(String js) {
        if (js == null || js.isEmpty()) return js;
        js = decodeHexEscapes(js);
        js = decodeUnicodeEscapes(js);
        return js;
    }

    private static String decodeHexEscapes(String js) {
        Matcher m = HEX_ESCAPE.matcher(js);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            char c = (char) Integer.parseInt(m.group(1), 16);
            m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(c)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String decodeUnicodeEscapes(String js) {
        Matcher m = UNICODE_ESCAPE.matcher(js);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            char c = (char) Integer.parseInt(m.group(1), 16);
            m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(c)));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
