package com.clean.ui;

import com.clean.util.UrlDetector;
import org.fife.ui.rsyntaxtextarea.LinkGenerator;
import org.fife.ui.rsyntaxtextarea.LinkGeneratorResult;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.event.HyperlinkEvent;
import java.awt.Desktop;
import java.net.URI;

public class UrlLinkGenerator implements LinkGenerator {
    @Override
    public LinkGeneratorResult isLinkAtOffset(final RSyntaxTextArea textArea, final int offs) {
        String text = textArea.getText();
        final int[] range = UrlDetector.rangeAt(text, offs);
        if (range == null) return null;
        final String url = text.substring(range[0], range[1]);
        return new LinkGeneratorResult() {
            @Override
            public HyperlinkEvent execute() {
                try {
                    Desktop.getDesktop().browse(URI.create(url));
                } catch (Exception ignored) {
                }
                return null;
            }
            @Override
            public int getSourceOffset() { return range[0]; }
        };
    }
}
