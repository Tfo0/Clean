package com.clean.processor;

public final class JsFormatter {

    private JsFormatter() {}

    public static String format(String js) {
        if (js == null || js.isEmpty()) return js;
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        char stringChar = 0;
        boolean escaped = false;

        for (int i = 0; i < js.length(); i++) {
            char c = js.charAt(i);

            if (escaped) {
                sb.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\' && inString) {
                sb.append(c);
                escaped = true;
                continue;
            }

            if ((c == '\'' || c == '"' || c == '`') && !inString) {
                inString = true;
                stringChar = c;
                sb.append(c);
                continue;
            }

            if (inString) {
                sb.append(c);
                if (c == stringChar) {
                    inString = false;
                }
                continue;
            }

            switch (c) {
                case '{':
                    sb.append(c);
                    indent++;
                    newLine(sb, indent);
                    break;
                case '}':
                    indent = Math.max(0, indent - 1);
                    newLine(sb, indent);
                    sb.append(c);
                    if (i + 1 < js.length() && js.charAt(i + 1) != ';' && js.charAt(i + 1) != ',' && js.charAt(i + 1) != ')') {
                        newLine(sb, indent);
                    }
                    break;
                case ';':
                    sb.append(c);
                    if (i + 1 < js.length() && js.charAt(i + 1) != '}') {
                        newLine(sb, indent);
                    }
                    break;
                case ',':
                    sb.append(c);
                    sb.append(' ');
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString().replaceAll("(?m)^[ \t]*\\r?\\n", "");
    }

    private static void newLine(StringBuilder sb, int indent) {
        sb.append('\n');
        for (int i = 0; i < indent; i++) {
            sb.append("    ");
        }
    }
}
