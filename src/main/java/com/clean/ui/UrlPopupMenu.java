package com.clean.ui;

import com.clean.util.UrlDetector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URI;

class UrlPopupMenu extends JPopupMenu {
    private final SyntaxArea area;
    private final JMenuItem openUrl;
    private final JMenuItem copyUrl;
    private String currentUrl;

    UrlPopupMenu(SyntaxArea area) {
        this.area = area;

        JMenuItem copy = new JMenuItem("复制");
        copy.addActionListener(e -> area.copy());
        JMenuItem cut = new JMenuItem("剪切");
        cut.addActionListener(e -> area.cut());
        JMenuItem paste = new JMenuItem("粘贴");
        paste.addActionListener(e -> area.paste());
        JMenuItem selectAll = new JMenuItem("全选");
        selectAll.addActionListener(e -> area.selectAll());

        openUrl = new JMenuItem("用浏览器打开 URL");
        openUrl.addActionListener(e -> openBrowser(currentUrl));

        copyUrl = new JMenuItem("复制 URL");
        copyUrl.addActionListener(e -> {
            if (currentUrl != null) {
                StringSelection s = new StringSelection(currentUrl);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, s);
            }
        });

        add(copy);
        add(cut);
        add(paste);
        add(new JSeparator());
        add(selectAll);
        add(new JSeparator());
        add(openUrl);
        add(copyUrl);
    }

    @Override
    public void show(Component invoker, int x, int y) {
        int off = area.viewToModel2D(new Point(x, y));
        currentUrl = off >= 0 ? UrlDetector.urlAt(area.getText(), off) : null;
        boolean has = currentUrl != null;
        openUrl.setEnabled(has);
        copyUrl.setEnabled(has);
        super.show(invoker, x, y);
    }

    static void openBrowser(String url) {
        if (url == null) return;
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception ignored) {
        }
    }
}
