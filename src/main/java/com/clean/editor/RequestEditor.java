package com.clean.editor;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;

import com.clean.processor.FormProcessor;
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

public class RequestEditor implements ExtensionProvidedHttpRequestEditor {
    private final MontoyaApi api;
    private final EditorCreationContext creationContext;
    private final SyntaxArea headerArea;
    private final SyntaxArea contentArea;
    private final RTextScrollPane contentScrollPane;
    private final JPanel panel;

    public RequestEditor(MontoyaApi api, EditorCreationContext creationContext) {
        this.api = api;
        this.creationContext = creationContext;

        panel = new JPanel(new BorderLayout());

        headerArea = new SyntaxArea();
        headerArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        headerArea.setEditable(true);
        headerArea.setLineWrap(true);
        headerArea.setWrapStyleWord(true);

        contentArea = new SyntaxArea();
        contentArea.setEditable(true);

        headerArea.setAntiAliasingEnabled(true);
        contentArea.setAntiAliasingEnabled(true);
        headerArea.setMargin(new Insets(10, 10, 10, 10));
        contentArea.setMargin(new Insets(10, 10, 10, 10));

        contentScrollPane = new RTextScrollPane(contentArea);
        contentScrollPane.setFoldIndicatorEnabled(false);
        contentScrollPane.getVerticalScrollBar().setBackground(ThemeManager.get().scrollbarBg());
        contentScrollPane.getHorizontalScrollBar().setBackground(ThemeManager.get().scrollbarBg());
        contentScrollPane.setLineNumbersEnabled(false);
        contentScrollPane.getGutter().setBorder(null);
        contentScrollPane.getGutter().setVisible(false);

        JScrollPane headerScrollPane = new JScrollPane(headerArea);
        headerScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        headerScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        headerScrollPane.getVerticalScrollBar().setBackground(ThemeManager.get().scrollbarBg());
        headerScrollPane.getHorizontalScrollBar().setBackground(ThemeManager.get().scrollbarBg());
        headerScrollPane.setPreferredSize(new Dimension(0, 40));
        headerScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        headerArea.setHighlightCurrentLine(false);
        contentArea.setHighlightCurrentLine(false);

        headerArea.setHyperlinksEnabled(true);
        contentArea.setHyperlinksEnabled(true);

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
        headerArea.addHyperlinkListener(hyperlinkListener);
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
        headerArea.addMouseWheelListener(mouseWheelListener);
        contentArea.addMouseWheelListener(mouseWheelListener);

        panel.add(headerScrollPane, BorderLayout.NORTH);
        panel.add(contentScrollPane, BorderLayout.CENTER);

        ThemeManager.get().applyTheme(headerArea, false);
        ThemeManager.get().applyTheme(contentArea, false);

        headerArea.revalidate();
        headerArea.repaint();
        contentArea.revalidate();
        contentArea.repaint();
    }

    @Override
    public HttpRequest getRequest() {
        return null;
    }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse) {
        SwingUtilities.invokeLater(() -> {
            headerArea.setCaretPosition(0);
            contentArea.setCaretPosition(0);
            contentScrollPane.getVerticalScrollBar().setValue(0);
        });

        HttpRequest request = requestResponse.request();
        String method = request.method();
        String pathwithoutquery = request.pathWithoutQuery();
        String host = request.httpService().host();
        String query = request.query();
        ByteArray bodyBytes = request.body();

        boolean isJsonBody = false;
        String body = bodyBytes.length() > 0 ? new String(bodyBytes.getBytes(), StandardCharsets.UTF_8) : "";
        String formattedBody = body;
        String formattedQuery = query;

        if (method.equalsIgnoreCase("POST")) {
            if (!query.isEmpty()) {
                try {
                    formattedQuery = FormProcessor.process(query);
                } catch (Exception e) {
                    formattedQuery = UnicodeDecoder.decode(query);
                }
            } else {
                formattedQuery = "";
            }
            if (!body.isEmpty()) {
                try {
                    body = UnicodeDecoder.decode(body);
                    Object json = JsonProcessor.parseDeep(body);
                    formattedBody = JsonProcessor.pretty(json);
                    formattedBody = JsonTruncator.truncate(formattedBody, 99, contentArea);
                    formattedBody = UnicodeDecoder.decodeUnicodeOnly(formattedBody);
                    isJsonBody = true;
                } catch (Exception e) {
                    api.logging().logToOutput("Body is not JSON: " + e.getMessage());
                    try {
                        formattedBody = FormProcessor.process(body);
                    } catch (Exception ex) {
                        formattedBody = UnicodeDecoder.decode(body);
                    }
                }
            } else {
                formattedBody = "";
            }
        } else if (method.equalsIgnoreCase("GET")) {
            formattedBody = "";
            if (!query.isEmpty()) {
                try {
                    formattedQuery = FormProcessor.process(query);
                } catch (Exception e) {
                    formattedQuery = UnicodeDecoder.decode(query);
                }
            } else {
                formattedQuery = "";
            }
        }

        headerArea.setText(method + "  " + host + pathwithoutquery);

        String contentText = (formattedQuery.isEmpty() ? "" : formattedQuery + "\n\n") + formattedBody;
        contentArea.setText(contentText);

        if (method.equalsIgnoreCase("GET") || (!isJsonBody && method.equalsIgnoreCase("POST"))) {
            contentArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
        } else if (isJsonBody) {
            contentArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        } else {
            contentArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        }

        headerArea.revalidate();
        headerArea.repaint();
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
