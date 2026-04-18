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
    private final JComboBox<String> themeSelector;

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
        contentScrollPane.setFoldIndicatorEnabled(true);
        contentScrollPane.getVerticalScrollBar().setBackground(ThemeManager.get().scrollbarBg());
        contentScrollPane.getHorizontalScrollBar().setBackground(ThemeManager.get().scrollbarBg());
        contentScrollPane.setLineNumbersEnabled(false);

        JScrollPane headerScrollPane = new JScrollPane(headerArea);
        headerScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        headerScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        headerScrollPane.getVerticalScrollBar().setBackground(ThemeManager.get().scrollbarBg());
        headerScrollPane.getHorizontalScrollBar().setBackground(ThemeManager.get().scrollbarBg());

        headerArea.setHighlightCurrentLine(false);
        contentArea.setHighlightCurrentLine(false);

        headerArea.setHyperlinksEnabled(true);
        contentArea.setHyperlinksEnabled(true);
        headerArea.setLinkScanningMask(InputEvent.CTRL_DOWN_MASK);
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

        String[] themes = {"dark", "monokai", "vs", "idea", "eclipse", "default-alt", "druid"};
        themeSelector = new JComboBox<>(themes);
        themeSelector.setSelectedItem("dark");
        themeSelector.addActionListener(e -> applySelectedTheme());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel("Select Theme: "));
        toolbar.add(themeSelector);

        panel.add(toolbar, BorderLayout.SOUTH);
        panel.add(headerScrollPane, BorderLayout.NORTH);
        panel.add(contentScrollPane, BorderLayout.CENTER);

        applySelectedTheme();

        headerArea.revalidate();
        headerArea.repaint();
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void applySelectedTheme() {
        String selected = (String) themeSelector.getSelectedItem();
        ThemeManager.get().applyBuiltIn(headerArea, selected);
        ThemeManager.get().applyBuiltIn(contentArea, selected);
        if ("dark".equals(selected)) {
            ThemeManager.get().applyDark(headerArea, false);
            ThemeManager.get().applyDark(contentArea, false);
        }
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
            formattedQuery = "";
            if (!body.isEmpty()) {
                try {
                    body = UnicodeDecoder.decode(body);
                    Object json = JsonProcessor.parseDeep(body);
                    formattedBody = JsonProcessor.pretty(json);
                    formattedBody = JsonTruncator.truncate(formattedBody, 99, contentArea);
                    formattedBody = StringEscapeUtils.unescapeJava(formattedBody);
                    formattedBody = formattedBody.replaceAll("(?m)^[ \t]*\\r?\\n", "");
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
