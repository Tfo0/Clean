package com.clean.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.Theme;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;

import java.awt.Color;
import java.awt.Font;
import java.io.InputStream;
import java.util.Properties;

public final class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();
    private final Properties dark = new Properties();
    private final Properties light = new Properties();
    private boolean isDark = true;
    private Font burpFont = null;
    private boolean loaded = false;

    private ThemeManager() {}

    public static ThemeManager get() { return INSTANCE; }

    public synchronized void load(MontoyaApi api) {
        if (loaded) return;
        try (InputStream in = ThemeManager.class.getResourceAsStream("/theme/dark.properties")) {
            if (in != null) dark.load(in);
            else if (api != null) api.logging().logToError("theme/dark.properties not found");
        } catch (Exception e) {
            if (api != null) api.logging().logToError("dark theme load failed: " + e.getMessage());
        }
        try (InputStream in = ThemeManager.class.getResourceAsStream("/theme/light.properties")) {
            if (in != null) light.load(in);
            else if (api != null) api.logging().logToError("theme/light.properties not found");
        } catch (Exception e) {
            if (api != null) api.logging().logToError("light theme load failed: " + e.getMessage());
        }
        if (api != null) {
            isDark = api.userInterface().currentTheme() == Theme.DARK;
            burpFont = api.userInterface().currentEditorFont();
        }
        loaded = true;
    }

    public boolean isDark() { return isDark; }

    public void applyTheme(RSyntaxTextArea area, boolean isResponse) {
        Properties props = isDark ? dark : light;
        Color defBg = isDark ? new Color(43, 43, 43) : new Color(255, 255, 255);
        Color defFg = isDark ? new Color(200, 215, 236) : new Color(43, 43, 43);
        Color defCaret = isDark ? Color.WHITE : Color.BLACK;
        Color defSelection = isDark ? new Color(77, 72, 112) : new Color(179, 215, 255);
        Color defCurrentLine = isDark ? new Color(60, 63, 65) : new Color(242, 242, 242);
        Color defBracketBg = isDark ? new Color(47, 112, 255) : new Color(179, 215, 255);
        Color defBracketBorder = new Color(160, 160, 160);

        area.setBackground(color(props, "background", defBg));
        area.setForeground(color(props, "foreground", defFg));
        area.setCaretColor(color(props, "caret", defCaret));
        area.setSelectionColor(color(props, "selection", defSelection));
        area.setCurrentLineHighlightColor(color(props, "currentLine", defCurrentLine));
        area.setMatchedBracketBGColor(color(props, "matchedBracket.bg", defBracketBg));
        area.setMatchedBracketBorderColor(color(props, "matchedBracket.border", defBracketBorder));
        area.setFont(font());

        SyntaxScheme s = area.getSyntaxScheme();
        s.getStyle(Token.LITERAL_STRING_DOUBLE_QUOTE).foreground = color(props, "token.string", isDark ? new Color(149, 191, 94) : new Color(6, 125, 23));
        s.getStyle(Token.LITERAL_NUMBER_DECIMAL_INT).foreground = color(props, "token.number.int", isDark ? new Color(110, 177, 255) : new Color(23, 80, 235));
        s.getStyle(Token.LITERAL_NUMBER_FLOAT).foreground = color(props, "token.number.float", defFg);
        s.getStyle(Token.LITERAL_BOOLEAN).foreground = color(props, "token.boolean", isDark ? new Color(255, 91, 91) : new Color(204, 0, 0));
        s.getStyle(Token.LITERAL_BACKQUOTE).foreground = color(props, "token.backquote", new Color(160, 160, 160));
        s.getStyle(Token.SEPARATOR).foreground = color(props, "token.separator", defFg);
        s.getStyle(Token.VARIABLE).foreground = color(props, "token.variable", isDark ? new Color(237, 189, 91) : new Color(158, 107, 0));
        s.getStyle(Token.RESERVED_WORD_2).foreground = color(props, "token.reservedWord2", isDark ? new Color(239, 138, 138) : new Color(204, 68, 68));
        s.getStyle(Token.COMMENT_KEYWORD).foreground = color(props, "token.commentKeyword", isDark ? Color.WHITE : Color.BLACK);

        if (isResponse) {
            s.getStyle(Token.LITERAL_CHAR).foreground = color(props, "response.token.char", isDark ? new Color(221, 109, 18) : new Color(184, 92, 0));
            s.getStyle(Token.RESERVED_WORD).foreground = color(props, "response.token.reservedWord", isDark ? new Color(255, 171, 85) : new Color(184, 92, 0));
        } else {
            s.getStyle(Token.LITERAL_CHAR).foreground = color(props, "token.char", isDark ? new Color(237, 189, 91) : new Color(158, 107, 0));
            s.getStyle(Token.RESERVED_WORD).foreground = color(props, "token.reservedWord", isDark ? new Color(237, 189, 91) : new Color(158, 107, 0));
        }
    }

    public Color scrollbarBg() {
        Properties props = isDark ? dark : light;
        return color(props, "scrollbar.bg", isDark ? new Color(60, 63, 65) : new Color(232, 232, 232));
    }

    public Font font() {
        Properties props = isDark ? dark : light;
        String family = props.getProperty("font.family");
        String sizeStr = props.getProperty("font.size");
        if (burpFont != null && family == null && sizeStr == null) {
            return burpFont;
        }
        if (family == null) {
            family = burpFont != null ? burpFont.getFamily() : "JetBrains Mono";
        }
        int size;
        if (sizeStr != null) {
            try { size = Integer.parseInt(sizeStr.trim()); }
            catch (Exception e) { size = burpFont != null ? burpFont.getSize() : 14; }
        } else {
            size = burpFont != null ? burpFont.getSize() : 14;
        }
        return new Font(family, Font.PLAIN, size);
    }

    private Color color(Properties props, String key, Color fallback) {
        String v = props.getProperty(key);
        if (v == null) return fallback;
        try { return Color.decode(v.trim()); }
        catch (Exception e) { return fallback; }
    }
}
