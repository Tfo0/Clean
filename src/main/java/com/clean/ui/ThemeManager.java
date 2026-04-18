package com.clean.ui;

import burp.api.montoya.MontoyaApi;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.Token;

import java.awt.Color;
import java.awt.Font;
import java.io.InputStream;
import java.util.Properties;

public final class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();
    private final Properties dark = new Properties();
    private boolean loaded = false;

    private ThemeManager() {}

    public static ThemeManager get() { return INSTANCE; }

    public synchronized void load(MontoyaApi api) {
        if (loaded) return;
        try (InputStream in = ThemeManager.class.getResourceAsStream("/theme/dark.properties")) {
            if (in != null) dark.load(in);
            else if (api != null) api.logging().logToError("theme/dark.properties not found");
        } catch (Exception e) {
            if (api != null) api.logging().logToError("theme load failed: " + e.getMessage());
        }
        loaded = true;
    }

    public void applyBuiltIn(RSyntaxTextArea area, String name) {
        String path = "/org/fife/ui/rsyntaxtextarea/themes/" + name + ".xml";
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in != null) {
                Theme theme = Theme.load(in);
                theme.apply(area);
            }
        } catch (Exception ignored) {
        }
    }

    public void applyDark(RSyntaxTextArea area, boolean isResponse) {
        area.setBackground(color("background", new Color(43, 43, 43)));
        area.setForeground(color("foreground", new Color(200, 215, 236)));
        area.setCaretColor(color("caret", Color.WHITE));
        area.setSelectionColor(color("selection", new Color(77, 72, 112)));
        area.setCurrentLineHighlightColor(color("currentLine", new Color(60, 63, 65)));
        area.setMatchedBracketBGColor(color("matchedBracket.bg", new Color(47, 112, 255)));
        area.setMatchedBracketBorderColor(color("matchedBracket.border", new Color(160, 160, 160)));
        area.setFont(font());

        SyntaxScheme s = area.getSyntaxScheme();
        s.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).foreground = color("token.string", new Color(149, 191, 94));
        s.getStyle(Token.LITERAL_NUMBER_DECIMAL_INT).foreground = color("token.number.int", new Color(110, 177, 255));
        s.getStyle(Token.LITERAL_NUMBER_FLOAT).foreground = color("token.number.float", new Color(200, 215, 236));
        s.getStyle(Token.LITERAL_BOOLEAN).foreground = color("token.boolean", new Color(255, 91, 91));
        s.getStyle(Token.LITERAL_BACKQUOTE).foreground = color("token.backquote", new Color(160, 160, 160));
        s.getStyle(Token.SEPARATOR).foreground = color("token.separator", new Color(200, 215, 236));
        s.getStyle(Token.VARIABLE).foreground = color("token.variable", new Color(237, 189, 91));
        s.getStyle(Token.RESERVED_WORD_2).foreground = color("token.reservedWord2", new Color(239, 138, 138));
        s.getStyle(Token.COMMENT_KEYWORD).foreground = color("token.commentKeyword", new Color(255, 255, 255));

        if (isResponse) {
            s.getStyle(Token.LITERAL_CHAR).foreground = color("response.token.char", new Color(221, 109, 18));
            s.getStyle(Token.RESERVED_WORD).foreground = color("response.token.reservedWord", new Color(255, 171, 85));
        } else {
            s.getStyle(Token.LITERAL_CHAR).foreground = color("token.char", new Color(237, 189, 91));
            s.getStyle(Token.RESERVED_WORD).foreground = color("token.reservedWord", new Color(237, 189, 91));
        }
    }

    public Color scrollbarBg() { return color("scrollbar.bg", new Color(60, 63, 65)); }

    public Font font() {
        String family = dark.getProperty("font.family", "JetBrains Mono");
        int size;
        try { size = Integer.parseInt(dark.getProperty("font.size", "20").trim()); }
        catch (Exception e) { size = 20; }
        return new Font(family, Font.PLAIN, size);
    }

    private Color color(String key, Color fallback) {
        String v = dark.getProperty(key);
        if (v == null) return fallback;
        try { return Color.decode(v.trim()); }
        catch (Exception e) { return fallback; }
    }
}
