/**
 * Copyright (c) 2006, Sun Microsystems, Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of the TimingFramework project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sun.javaone.mailman.ui;

import binding.BindingContext;
import binding.ListBindingDescription;
import com.sun.javaone.mailman.model.Contact;
import com.sun.javaone.mailman.model.Message;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author sky
 */
public class MailTable extends JTable {
    static final Color STRIPE_COLOR = new Color(237, 242, 249);
    private static final String[] COLOR_TITLES = new String[] {
      "Deep Purple", "Red Crush", "Blue Experience", "Aerith"
    };
    private static final Color[] COLORS = new Color[] {
        new Color(170, 175, 230), new Color(210, 210, 255), Color.BLUE,
        new Color(200, 175, 170), new Color(250, 220, 220), Color.RED,
        new Color(168, 204, 241), new Color(44, 61, 146), Color.BLACK,
        Color.WHITE, new Color(64, 110, 161), Color.WHITE
    };
    private final UIController controller;
    private boolean showTableStriping;

    private final Map<String,Integer> colorMap;


    public MailTable(UIController controller) {
        this.controller = controller;
        colorMap = new HashMap<String,Integer>();
        Border emptyBorder = new EmptyBorder(1, 1, 1, 1);
        UIManager.put("Table.focusSelectedCellHighlightBorder", emptyBorder);
        UIManager.put("Table.focusCellHighlightBorder", emptyBorder);
        setDragEnabled(true);
        setName("mailTable");
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    void addBindings(UIController controller, BindingContext context) {
        ListBindingDescription tableBD = new ListBindingDescription(
                controller, "selectedMailBox.messages", this, "elements");
        tableBD.addDescription("subject", "1.value");
        tableBD.addDescription("from.displayName", "2.value");
        tableBD.addDescription("dateTime", "3.value");
        context.addDescription(tableBD);
    }

    void bound() {
//        setFont(getFont().deriveFont(Font.BOLD));
        getTableHeader().setFont(getFont().deriveFont(getFont().getSize() + 2f));
        setRowHeight(22);
        TableColumnModel cm = getColumnModel();
        cm.getColumn(0).setMaxWidth(24);
        cm.getColumn(1).setPreferredWidth(400);
        cm.getColumn(1).setMaxWidth(400);
        cm.getColumn(0).setHeaderValue("");
        cm.getColumn(1).setHeaderValue("Subject");
        cm.getColumn(2).setHeaderValue("Sender");
        cm.getColumn(3).setHeaderValue("Date");
        cm.getColumn(3).setCellRenderer(new DateRenderer());
    }

    protected void paintComponent(Graphics g) {
        Rectangle clip = g.getClipBounds();
        int rh = getRowHeight();
        int startRow = clip.y / rh;
        int endRow = (clip.y + clip.height) / rh + 1;
        int w = getWidth();

        g.setColor(Color.WHITE);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        if (showTableStriping) {
            g.setColor(STRIPE_COLOR);
            for (int row = startRow / 2 * 2; row < endRow; row += 2) {
                g.fillRect(0, rh * row, w, rh);
            }
        }

        if (startRow < getRowCount()) {
            highlightRows(g, startRow, Math.min(getRowCount(), endRow));
        }

        Graphics uig = g.create();
        getUI().paint(uig, this);
        uig.dispose();
    }

    private void highlightRows(Graphics g, int startRow, int endRow) {
        if (controller.getSelectedMailBox() != null && colorMap.size() > 0) {
            List<Message> messages = controller.getSelectedMailBox().getMessages();
            int rh = getRowHeight();
            int width = getWidth();
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            for (int row = startRow; row < endRow; row++) {
                String address = getAddress(messages.get(row));
                Integer colorIndex = colorMap.get(address);
                if (colorIndex != null) {
                    int offset = colorIndex;
                    int x = 4;
                    int y = rh * row + 2;
                    int w = width - 8;
                    int h = rh - 4;
                    int arcSize = 12;
                    ((Graphics2D)g).setPaint(new GradientPaint(x, y, COLORS[offset * 3], x, y + h, COLORS[offset * 3 + 1]));
                    g.fillRoundRect(x, y, w, h, arcSize, arcSize);
                    g.setColor(COLORS[offset * 3 + 2]);
//                            g.drawRoundRect(x, y, w - 1, h - 1, arcSize, arcSize);
                }
            }
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }

    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        if (!e.isConsumed() && e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    private void showPopup(MouseEvent e) {
        getPopupMenu().show(this, e.getX(), e.getY());
    }


    private JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JCheckBoxMenuItem stripeCB = new JCheckBoxMenuItem("Table Striping");
        stripeCB.setSelected(getShowTableStriping());
        stripeCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setShowTableStriping(((JCheckBoxMenuItem)e.getSource()).isSelected());
            }
        });
        popupMenu.add(stripeCB);
        JMenu colorizeMenu = (JMenu) popupMenu.add(new JMenu("Colorize"));
        if (getSelectedRowCount() != 0) {
            ActionListener colorizeAL = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JMenuItem mi = (JMenuItem)e.getSource();
                    colorize(mi.getParent().getComponentZOrder(mi));
                }
            };
            for (int i = 0; i < COLOR_TITLES.length; i++) {
                JMenuItem colorMenuItem = new JMenuItem(COLOR_TITLES[i]);
                colorMenuItem.setIcon(new ColorIcon(i));
                colorMenuItem.addActionListener(colorizeAL);
                colorizeMenu.add(colorMenuItem);
            }
            colorizeMenu.addSeparator();
            JMenuItem uncolorMenuItem = new JMenuItem("None");
            colorizeMenu.add(uncolorMenuItem);
            uncolorMenuItem.addActionListener(colorizeAL);
        }
        return popupMenu;
    }

    public void setShowTableStriping(boolean showStriping) {
        this.showTableStriping = showStriping;
        repaint();
    }

    public boolean getShowTableStriping() {
        return showTableStriping;
    }

    private void colorize(int index) {
        int[] selection = getSelectedRows();
        List<Message> messages = controller.getSelectedMailBox().getMessages();
        for (int i = 0; i < selection.length; i++) {
            Message message = messages.get(selection[i]);
            String address = getAddress(message);
            if (address != null) {
                if (index >= COLOR_TITLES.length) {
                    colorMap.remove(address);
                } else {
                    colorMap.put(address, index);
                }
            }
        }
        repaint();
    }

    private String getAddress(Message message) {
        Contact from = message.getFrom();

        if (from != null) {
            String address = from.getAddress();
            if (address != null) {
                return address.toLowerCase();
            }
        }
        return null;
    }


    private static class ColorIcon implements Icon {
        private final int colorIndex;

        ColorIcon(int index) {
            this.colorIndex = index;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            ((Graphics2D)g).setPaint(new GradientPaint(x, y, COLORS[colorIndex * 3],
                    x, y + getIconHeight(), COLORS[colorIndex* 3 + 1]));
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            g.setColor(Color.WHITE);
        }

        public int getIconWidth() {
            return 16;
        }

        public int getIconHeight() {
            return 16;
        }
    }


    private static class DateRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return super.getTableCellRendererComponent(table,
                    DateHelper.convert((Long)value), isSelected, hasFocus, row,
                    column);
        }
    }
}
