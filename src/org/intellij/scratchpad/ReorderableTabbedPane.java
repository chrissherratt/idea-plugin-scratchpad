package org.intellij.scratchpad;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

public class ReorderableTabbedPane extends JTabbedPane {

    private Cursor defaultCursor, handCursor;

    public ReorderableTabbedPane() {
        this(TOP);
    }

    public ReorderableTabbedPane(int tabPlacement) {
        this(tabPlacement, WRAP_TAB_LAYOUT);
    }

    public ReorderableTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);

        MouseInputListener mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private void dragTab(int dragIndex, int tabIndex) {
        String title = getTitleAt(dragIndex);
        Icon icon = getIconAt(dragIndex);
        Component component = getComponentAt(dragIndex);
        String toolTipText = getToolTipTextAt(dragIndex);

        Color background = getBackgroundAt(dragIndex);
        Color foreground = getForegroundAt(dragIndex);
        Icon disabledIcon = getDisabledIconAt(dragIndex);
        int mnemonic = getMnemonicAt(dragIndex);
        int displayedMnemonicIndex = getDisplayedMnemonicIndexAt(dragIndex);
        boolean enabled = isEnabledAt(dragIndex);

        remove(dragIndex);
        insertTab(title, icon, component, toolTipText, tabIndex);

        setBackgroundAt(tabIndex, background);
        setForegroundAt(tabIndex, foreground);
        setDisabledIconAt(tabIndex, disabledIcon);
        setMnemonicAt(tabIndex, mnemonic);
        setDisplayedMnemonicIndexAt(tabIndex, displayedMnemonicIndex);
        setEnabledAt(tabIndex, enabled);
    }

    private Cursor getDefaultCursor() {
        if (defaultCursor == null) {
            defaultCursor = Cursor.getDefaultCursor();
        }

        return defaultCursor;
    }

    private Cursor getHandCursor() {
        if (handCursor == null) {
            handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        }

        return handCursor;
    }

    private int getTabIndex(int x, int y) {
        return getUI().tabForCoordinate(this, x, y);
    }

    private void maybeSetDefaultCursor() {
        Cursor cursor = getDefaultCursor();

        if (getCursor() != cursor) {
            setCursor(cursor);
        }
    }

    private void maybeSetHandCursor() {
        Cursor cursor = getHandCursor();

        if (getCursor() != cursor) {
            setCursor(cursor);
        }
    }

    class MouseHandler extends MouseInputAdapter {

        private int dragIndex = -1;

        public void mouseDragged(MouseEvent e) {
            if (dragIndex != -1) {
                if (getTabIndex(e.getX(), e.getY()) != -1) {
                    maybeSetHandCursor();
                } else {
                    maybeSetDefaultCursor();
                }
            }
        }

        public void mousePressed(MouseEvent e) {
            if (!e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) {
                int tabIndex = getTabIndex(e.getX(), e.getY());
                if (tabIndex != -1) {
                    dragIndex = tabIndex;
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (!e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) {
                if (dragIndex != -1) {
                    int tabIndex = getTabIndex(e.getX(), e.getY());
                    if (tabIndex != -1 && tabIndex != dragIndex) {
                        dragTab(dragIndex, tabIndex);
                        setSelectedIndex(tabIndex);
                    }
                    dragIndex = -1;
                    maybeSetDefaultCursor();
                }
            }
        }

    }
}

