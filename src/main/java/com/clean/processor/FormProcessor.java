package com.clean.processor;

import java.io.UnsupportedEncodingException;

public final class FormProcessor {
    private FormProcessor() {}

    public static String process(String str) throws UnsupportedEncodingException {
        if (str == null || str.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String[] pairs = str.split("&");
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            int eq = pair.indexOf('=');
            String rawKey = eq >= 0 ? pair.substring(0, eq) : pair;
            String rawVal = eq >= 0 ? pair.substring(eq + 1) : "";
            String key = UrlDecoder.decodeFully(rawKey);
            String val = UrlDecoder.decodeFully(rawVal);

            sb.append(key).append(" = ");
            if (JsonProcessor.looksLikeJson(val)) {
                try {
                    Object json = JsonProcessor.parseDeep(val);
                    sb.append('\n').append(JsonProcessor.pretty(json));
                } catch (Exception ignored) {
                    sb.append(val);
                }
            } else {
                sb.append(val);
            }
            if (i < pairs.length - 1) sb.append("\n");
        }
        return sb.toString();
    }
}
