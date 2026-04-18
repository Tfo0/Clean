package com.clean.editor;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor;

import com.clean.processor.JsonProcessor;
import com.clean.processor.JsonTruncator;
import com.clean.processor.UnicodeDecoder;
import com.clean.ui.SyntaxArea;
import com.clean.ui.ThemeManager;

import org.apache.commons.text.StringEscapeUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class ResponseEditor implements ExtensionProvidedHttpResponseEditor {
    private final MontoyaApi api;
    private final EditorCreationContext creationContext;
    private final SyntaxArea contentArea;
    private final RTextScrollPane contentScrollPane;
    private final JPanel panel;
    private final JComboBox<String> themeSelector;

    public ResponseEditor(MontoyaApi api, EditorCreationContext creationContext) {
        this.api = api;
        this.creationContext = creationContext;

        panel = new JPanel(new BorderLayout());

        contentArea = new SyntaxArea();
        contentArea.setAntiAliasingEnabled(true);
        contentArea.setMargin(new Insets(10, 10, 10, 10));

        contentScrollPane = new RTextScrollPane(contentArea);
        contentScrollPane.setFoldIndicatorEnabled(true);
        contentScrollPane.getVerticalScrollBar().setBackground(ThemeManager.get().scrollbarBg());
        contentScrollPane.getHorizontalScrollBar().setBackground(ThemeManager.get().scrollbarBg());
        contentScrollPane.setLineNumbersEnabled(false);

        contentArea.setHighlightCurrentLine(false);
        contentArea.setHyperlinksEnabled(true);
        contentArea.setLinkScanningMask(InputEvent.CTRL_DOWN_MASK);

        HyperlinkListener hyperlinkListener = new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(new URI(e.getURL().toString()));
                    } catch (Exception ex) {
                        api.logging().logToError("Failed to open URL: " + ex.getMessage());
                    }
                }
            }
        };
        contentArea.addHyperlinkListener(hyperlinkListener);

        MouseWheelListener mouseWheelListener = new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    RSyntaxTextArea area = (RSyntaxTextArea) e.getSource();
                    Font currentFont = area.getFont();
                    int newSize = currentFont.getSize() + (e.getWheelRotation() < 0 ? 2 : -2);
                    if (newSize >= 10 && newSize <= 28) {
                        area.setFont(new Font(currentFont.getFamily(), Font.PLAIN, newSize));
                        area.revalidate();
                        area.repaint();
                    }
                } else {
                    e.getComponent().getParent().dispatchEvent(e);
                }
            }
        };
        contentArea.addMouseWheelListener(mouseWheelListener);

        String[] themes = {"dark", "monokai", "vs", "idea", "eclipse"};
        themeSelector = new JComboBox<>(themes);
        themeSelector.setSelectedItem("dark");
        themeSelector.addActionListener(e -> applySelectedTheme());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Select Theme: "));
        toolbar.add(themeSelector);

        panel.add(toolbar, BorderLayout.SOUTH);
        panel.add(contentScrollPane, BorderLayout.CENTER);

        applySelectedTheme();

        contentArea.revalidate();
        contentArea.repaint();
    }

    private void applySelectedTheme() {
        String selected = (String) themeSelector.getSelectedItem();
        ThemeManager.get().applyBuiltIn(contentArea, selected);
        if ("dark".equals(selected)) {
            ThemeManager.get().applyDark(contentArea, true);
        }
        contentArea.repaint();
    }

    @Override
    public HttpResponse getResponse() { return null; }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse) {
        SwingUtilities.invokeLater(() -> {
            contentArea.setCaretPosition(0);
            contentScrollPane.getVerticalScrollBar().setValue(0);
        });

        HttpResponse response = requestResponse.response();
        ByteArray bodyBytes = response.body();
        String body = bodyBytes.length() > 0 ? new String(bodyBytes.getBytes(), StandardCharsets.UTF_8) : "";
        String formattedBody = body;

        if (!body.isEmpty()) {
            try {
                body = UnicodeDecoder.decode(body);
                Object json = JsonProcessor.parseDeep(body);
                formattedBody = JsonProcessor.pretty(json);
                formattedBody = JsonTruncator.truncate(formattedBody, 100, contentArea);
                formattedBody = StringEscapeUtils.unescapeJava(formattedBody);
                contentArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
            } catch (Exception e) {
                api.logging().logToOutput("Response body is not JSON: " + e.getMessage());
                formattedBody = UnicodeDecoder.decode(body);
                contentArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
            }
        } else {
            formattedBody = "No body";
            contentArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        }

        contentArea.setText(formattedBody);
        contentArea.revalidate();
        contentArea.repaint();
    }

    @Override
    public boolean isEnabledFor(HttpRequestResponse requestResponse) {
        return true;
    }

    @Override public String caption() { return "Clean"; }
    @Override public Component uiComponent() { return panel; }
    @Override public Selection selectedData() { return null; }
    @Override public boolean isModified() { return false; }
}
