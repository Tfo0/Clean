package com.clean.ui;

import com.clean.util.UrlDetector;

import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URL;

public class SyntaxArea extends RSyntaxTextArea {

    private static final boolean IS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");

    static {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED) return false;
            boolean shortcutDown = IS_MAC ? e.isMetaDown() : e.isControlDown();
            if (!shortcutDown) return false;
            if (IS_MAC ? e.isControlDown() : e.isMetaDown()) return false;
            if (e.isAltDown()) return false;
            Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (!(focused instanceof SyntaxArea)) return false;
            JTextComponent tc = (JTextComponent) focused;
            switch (e.getKeyCode()) {
                case KeyEvent.VK_C: tc.copy(); e.consume(); return true;
                case KeyEvent.VK_V: if (tc.isEditable()) tc.paste(); e.consume(); return true;
                case KeyEvent.VK_X: if (tc.isEditable()) tc.cut(); e.consume(); return true;
                case KeyEvent.VK_A: tc.selectAll(); e.consume(); return true;
                default: return false;
            }
        });
    }

    private static final int SHORTCUT_MASK = IS_MAC ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;

    public SyntaxArea() {
        super();
        setFocusable(true);
        setHyperlinksEnabled(true);
        setLinkScanningMask(SHORTCUT_MASK);
        setLinkGenerator(new UrlLinkGenerator());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                if ((e.getModifiersEx() & SHORTCUT_MASK) == 0) return;
                int off = viewToModel2D(e.getPoint());
                if (off < 0) return;
                String url = UrlDetector.urlAt(getText(), off);
                if (url == null) return;
                try {
                    URI uri = new URL(url).toURI();
                    Desktop.getDesktop().browse(uri);
                    e.consume();
                } catch (Exception ignored) {
                }
            }
        });
    }

    @Override
    protected JPopupMenu createPopupMenu() {
        return new UrlPopupMenu(this);
    }

    @Override
    protected void configurePopupMenu(JPopupMenu popupMenu) {
    }
}
