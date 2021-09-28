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

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.plaf.basic.BasicTreeUI;

import com.sun.javaone.mailman.model.MailBox;
import com.sun.javaone.mailman.ui.image.MessageIconGenerator;
import com.sun.javaone.mailman.ui.image.*;
import org.jdesktop.animation.timing.*;

/**
 * @author Shannon Hickey
 */
public class MailBoxTree extends JTree implements TimingTarget, PropertyChangeListener {
    private TreePath dropPath;
    private int extremeLevel = -1;

    private static final int[][] DUMMY_COUNTS = {{0, 0},
    {3, 0},
    {5, 0},
    {5, 2},
    {0, 2}};
    private int count = 0;
    private int[] counts = DUMMY_COUNTS[0];
    private int[] oldCounts = {0, 0};
    private final boolean trans = true;
    private float pct;

    static final String SENT = "Sent";
    static final String TRASH = "Trash";
    static final String INBOX = "Inbox";
    static final String DRAFT = "Drafts";

    static final ImageIcon SERVER_ICON =
            new ImageIcon(MailBoxTree.class.getResource("/resources/icons/server-mail.png"));
    static final ImageIcon CLOSED_ICON =
            new ImageIcon(MailBoxTree.class.getResource("/resources/icons/folder-closed.png"));
    static final ImageIcon DRAFT_ICON =
            new ImageIcon(MailBoxTree.class.getResource("/resources/icons/folder-draft.png"));
    static final ImageIcon INBOX_ICON =
            new ImageIcon(MailBoxTree.class.getResource("/resources/icons/folder-inbox.png"));
    static final ImageIcon OPEN_ICON =
            new ImageIcon(MailBoxTree.class.getResource("/resources/icons/folder-open.png"));
    static final ImageIcon SENT_ICON =
            new ImageIcon(MailBoxTree.class.getResource("/resources/icons/folder-sent.png"));
    static final ImageIcon TRASH_ICON =
            new ImageIcon(MailBoxTree.class.getResource("/resources/icons/folder-trash.png"));

    private static final int INSET = 5;
    static final int OUTSET = 2;
    static final Dimension NODE_SIZE = new Dimension(100, 30);

    private BufferedImage shadow = null;

    static final Color COLOR1 = new Color(125, 161, 237);
    private static final Color COLOR2 = new Color(91, 118, 173);
    static final GradientPaint GP = new GradientPaint(0, OUTSET, COLOR1, 0, NODE_SIZE.height - 2 * OUTSET, COLOR2, true);
    private static final GradientPaint GPR = new GradientPaint(0, OUTSET, COLOR2, 0, NODE_SIZE.height - 2 * OUTSET, COLOR1, true);
    private static final GradientPaint GPB = new GradientPaint(0, OUTSET, COLOR1, 0, NODE_SIZE.height + 16 - 2 * OUTSET, COLOR2, true);
    private static final GradientPaint GPBR = new GradientPaint(0, OUTSET, COLOR2, 0, NODE_SIZE.height + 16 - 2 * OUTSET, COLOR1, true);

    static final Border LRBORDER = new EmptyBorder(0, INSET, 0, INSET);

    private int overRow = -1;

    private final MouseAdapter MLISTENER = new MouseAdapter() {
        public void mouseMoved(MouseEvent me) {
            repaintRows(me);
        }

        public void mouseEntered(MouseEvent me) {
            repaintRows(me);
        }

        public void mouseExited(MouseEvent me) {
            repaintRows(me);
        }

        private void repaintRows(MouseEvent me) {
            int row = getRowForLocation(me.getX(), me.getY());
            if (overRow != row) {
                repaintRow(overRow);
                overRow = row;
                repaintRow(overRow);
            }
        }

        private void repaintRow(int row) {
            if (row != -1) {
                Rectangle rect = getRowBounds(row);
                if (rect != null) {
                    repaint(0, rect.y, getWidth(), rect.height);
                }
            }
        }
    };

    public int getExtremeLevel() {
        return extremeLevel;
    }

    public void changeCounts() {
        count = (count + 1) % DUMMY_COUNTS.length;
        oldCounts = counts;
        counts = DUMMY_COUNTS[count];
        if (extremeLevel >= 1) {
            new TimingController(100, this).start();
        } else {
            pct = 1.0f;
            // bogus - force the tree to recalculate cell sizes
            setRowHeight(getRowHeight() + 1);
            setRowHeight(getRowHeight() - 1);
            repaint();
        }
    }

    public void begin() {
    }

    public void end() {
    }

    public void timingEvent(long cycleElapsedTime, long totalElapsedTime, float fraction) {
        pct = fraction;
        Container p = getParent();
        if (p instanceof DropShadowPanel) {
            ((DropShadowPanel)p).propertyChange(null);
        } else {
            repaint();
        }
    }

    public void propertyChange(PropertyChangeEvent pce) {
        DropLocation dl = getDropLocation();
        TreePath newPath = (dl == null) ? null : dl.getPath();

        if (newPath != dropPath) {
            repaint();
            dropPath = newPath;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (extremeLevel >= 1) {
            paintCount(g, 1, oldCounts[0], counts[0]);
            paintCount(g, 5, oldCounts[1], counts[1]);
        }
    }

    private void paintCount(Graphics g, int row, int oldCount, int newCount) {
        if (row >= getRowCount() || (oldCount == 0 && newCount == 0)) {
            return;
        }

        Graphics2D g2d = (Graphics2D)g.create();

        Image image = MessageIconGenerator.createIcon(oldCount, newCount, pct);
        Rectangle bounds = getRowBounds(row);
        int iw = image.getWidth(null);
        int ih = image.getHeight(null);
        g2d.drawImage(image, bounds.x + bounds.width - 12, bounds.y - 1, null);

        g2d.dispose();
    }

    private static void paintAttachment(Graphics g, int x, int top, int bottom) {
        g.setColor(new Color(100, 100, 100));
        g.drawLine(x, top, x, bottom);
        g.setColor(new Color(175, 197, 242));
        g.drawLine(x + 1, top, x + 1, bottom);
        g.setColor(new Color(100, 100, 100));
        g.drawLine(x + 2, top, x + 2, bottom);
    }

    // 0 -> plain
    // 1 -> custom rendering
    // 2 -> everything
    public void switchExtremeLevel() {
        extremeLevel = (extremeLevel + 1) % 3;

        switch (extremeLevel) {
            case 0:
                setRowHeight(18);
                updateUI();
                setCellRenderer(new IconTreeCellRenderer());
                setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                break;
            case 1:
            case 2:
                setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
                setCellRenderer(new MailBoxRenderer());
                setRowHeight(0);
                setUI(new MailBoxUI());
                updateShadow();
        }

        if (extremeLevel >= 2) {
            setOpaque(false);
        }

        revalidate();
        repaint();
    }

    private void updateShadow() {
        dropPath = getPathForRow(0);
        Component comp = getCellRenderer().getTreeCellRendererComponent(this, "Foo", false, false, false, 0, false);
        ShadowFactory factory = new ShadowFactory(5, 0.2f, Color.BLACK);
        comp.setSize(NODE_SIZE.width + 16, NODE_SIZE.height + 16);
        BufferedImage im = new BufferedImage(NODE_SIZE.width + 16, NODE_SIZE.height + 16, BufferedImage.TYPE_INT_ARGB);
        Graphics g = im.getGraphics();
        comp.paint(g);
        g.dispose();
        shadow = factory.createShadow(im);
        dropPath = null;
        getCellRenderer().getTreeCellRendererComponent(this, "Foo", false, false, false, 0, false);
    }

    public MailBoxTree() {
        setShowsRootHandles(true);
        addMouseMotionListener(MLISTENER);
        addMouseListener(MLISTENER);
        setTransferHandler(new MessageTransferHandler(this));
        setDropMode(DropMode.ON);
        addPropertyChangeListener("dropLocation", this);
        switchExtremeLevel();
    }

    private String getCountString(int row) {
        if (row == 1) {
            return counts[0] == 0 ? "" : " (" + counts[0] + ")";
        } else if (row == 5) {
            return counts[1] == 0 ? "" : " (" + counts[1] + ")";
        }

        return "";
    }

    public String convertValueToText(Object value, boolean selected,
            boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        if (value instanceof MailBox) {
            return ((MailBox)value).getName();
        }

        return value.toString();
    }

    private class MailBoxUI extends BasicTreeUI {
        protected int getRowX(int row, int depth) {
            return super.getRowX(row, depth - 1);
        }

        public void installUI(JComponent c) {
            super.installUI(c);
        }
        protected void paintVerticalLine(Graphics g, JComponent c, int x, int top,
                int bottom) {
            paintAttachment(g, x + 24, top, bottom);
        }

        protected void paintHorizontalPartOfLeg(Graphics g, Rectangle clipBounds,
                Insets insets, Rectangle bounds,
                TreePath path, int row,
                boolean isExpanded,
                boolean hasBeenExpanded, boolean
                isLeaf) {}

        protected void paintRow(Graphics g, Rectangle clipBounds,
                Insets insets, Rectangle bounds, TreePath path,
                int row, boolean isExpanded,
                boolean hasBeenExpanded, boolean isLeaf) {
            super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
            if(shouldPaintExpandControl(path, row, isExpanded,
                    hasBeenExpanded, isLeaf)) {
                paintExpandControl(g, clipBounds, insets, bounds,
                        path, row, isExpanded,
                        hasBeenExpanded, isLeaf);
            }
        }

        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            if (dropPath == null) {
                return;
            }
            int row = tree.getRowForPath(dropPath);
            Rectangle bounds = getRowBounds(row);
            bounds.x -= 8;
            bounds.y -= 8;
            bounds.width += 16;
            bounds.height += 16;
            if (extremeLevel >= 2) {
                g.drawImage(shadow, bounds.x + 2, bounds.y + 10, null);
            }
            paintRow(g, (Rectangle)g.getClip(), getInsets(), bounds, dropPath, row, true, true, true);
        }

        protected boolean isLocationInExpandControl(TreePath path,
                int mouseX, int mouseY) {
            return super.isLocationInExpandControl(path, mouseX - 44, mouseY - 10);
        }
        protected void selectPathForEvent(TreePath path, MouseEvent event) {
            if (isLocationInExpandControl(path, event.getX(), event.getY())) {
                return;
            }

            super.selectPathForEvent(path, event);
        }
        protected void paintExpandControl(Graphics g,
                Rectangle clipBounds, Insets insets,
                Rectangle bounds, TreePath path,
                int row, boolean isExpanded,
                boolean hasBeenExpanded,
                boolean isLeaf) {
            Rectangle transBounds = new Rectangle(bounds);
            transBounds.x += 44;
            transBounds.y += 10;
            super.paintExpandControl(g, clipBounds, insets,
                                     transBounds, path, row, isExpanded,
                                     hasBeenExpanded, isLeaf);
        }
    }

    private class IconTreeCellRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean sel,
                boolean expanded, boolean leaf, int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            String val = tree.convertValueToText(value, sel, expanded,
                    leaf, row, hasFocus);
            if (row == 0) {
                setIcon(SERVER_ICON);
            } else if (val.equals(SENT)) {
                setIcon(SENT_ICON);
            } else if (val.equals(TRASH)) {
                setIcon(TRASH_ICON);
            } else if (val.equals(INBOX)) {
                setIcon(INBOX_ICON);
            } else if (val.equals(DRAFT)) {
                setIcon(DRAFT_ICON);
            } else if (hasFocus) {
                setIcon(OPEN_ICON);
            } else {
                setIcon(CLOSED_ICON);
            }

            String countString = getCountString(row);

            if (countString != "") {
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setFont(null);
            }

            setText(val + countString);

            return this;
        }
    }

    private class MailBoxRenderer extends JLabel implements TreeCellRenderer {
        private int row;
        private boolean showAttach;
        private boolean sel;
        private boolean isLast;
        private JTree tree;

        public void validate() {}
        public void invalidate() {}
        public void revalidate() {}
        public void repaint(long tm, int x, int y, int width, int height) {}
        public void repaint(Rectangle r) {}
        public void repaint() {}

        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean sel,
                boolean expanded, boolean leaf, int row,
                boolean hasFocus) {

            this.row = row;
            this.showAttach = !leaf && expanded;
            this.sel = sel;
            isLast = true;
            this.tree = tree;

            TreePath path = tree.getPathForRow(row);
            if (path != null) {
                TreePath parent = path.getParentPath();
                if (parent != null) {
                    Object parentObj = parent.getLastPathComponent();
                    Object childObj = value;

                    int childIndex = tree.getModel().getIndexOfChild(parentObj, childObj);
                    int totalIndex = tree.getModel().getChildCount(parentObj);
                    isLast = (childIndex == totalIndex - 1);
                }
            }

            String val = tree.convertValueToText(value, sel, expanded,
                    leaf, row, hasFocus);
            setText(val);
            if (row == 0) {
                setIcon(SERVER_ICON);
            } else if (val.equals(SENT)) {
                setIcon(SENT_ICON);
            } else if (val.equals(TRASH)) {
                setIcon(TRASH_ICON);
            } else if (val.equals(INBOX)) {
                setIcon(INBOX_ICON);
            } else if (val.equals(DRAFT)) {
                setIcon(DRAFT_ICON);
            } else if (hasFocus) {
                setIcon(OPEN_ICON);
            } else {
                setIcon(CLOSED_ICON);
            }

            setVerticalAlignment(JButton.CENTER);
            setBorder(LRBORDER);
            if (sel) {
                setForeground(Color.BLACK);
            } else {
                setForeground(Color.WHITE);
            }
            if (dropPath != null && tree.getPathForRow(row) == dropPath) {
            setFont(tree.getFont().deriveFont(Font.BOLD, tree.getFont().getSize() + 6f));
            } else {
                setFont(tree.getFont().deriveFont(Font.BOLD));
            }

            return this;
        }

        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D)g.create();
            int corn = (tree.getPathForRow(row) == dropPath) ? 35 : 20;
            int h = getHeight() - 2 * OUTSET;

            if (sel) {
                if (row == overRow) {
                    g2d.setPaint(new GradientPaint(0, OUTSET, COLOR1, 0, getHeight() - 2 * OUTSET, Color.WHITE, true));
                } else {
                    g2d.setPaint(new GradientPaint(0, OUTSET, Color.WHITE, 0, getHeight() - 2 * OUTSET, COLOR1, true));
                }
            } else if (tree.getPathForRow(row) == dropPath) {
                g2d.setPaint(row == overRow ? GPBR : GPB);
            } else {
                g2d.setPaint(row == overRow ? GPR : GP);
            }

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D.Float r2d = new RoundRectangle2D.Float(0, OUTSET, getWidth(), h, corn, corn);
            Shape clip = g2d.getClip();
            g2d.clip(r2d);
            g2d.fillRoundRect(0, OUTSET, getWidth(), h, corn, corn);
            g2d.setClip(clip);
            GradientPaint p1 = new GradientPaint(0, OUTSET, new Color(100, 100, 100), 0, h - 1, new Color(0, 0, 0));
            g2d.setPaint(p1);
            g2d.drawRoundRect(0, OUTSET, getWidth() - 1, h - 1, corn, corn);
            GradientPaint p2 = new GradientPaint(0, OUTSET + 1, new Color(255, 255, 255, 100), 0, h - 3, new Color(0, 0, 0, 50));
            g2d.setPaint(p2);
            g2d.drawRoundRect(1, OUTSET + 1, getWidth() - 3, h - 3, corn - 2, corn - 2);

            if (tree.getPathForRow(row) != dropPath) {
                if (showAttach) {
                    paintAttachment(g, 31, 28, 100);
                }

                if (row != 0) {
                    paintAttachment(g, 11, 0, 2);
                }

                if (!isLast) {
                    paintAttachment(g, 11, 28, 100);
                }
            }

            g2d.dispose();
            if (tree.getPathForRow(row) == dropPath) {
                g.translate(3, 0);
            }
            super.paint(g);
            if (tree.getPathForRow(row) == dropPath) {
                g.translate(-3, 0);
            }
        }

        public Dimension getPreferredSize() {
            return extremeLevel >= 1
                    ? new Dimension(Math.max(NODE_SIZE.width, super.getPreferredSize().width + 6), NODE_SIZE.height)
                    : super.getPreferredSize();
        }
    }

    public Dimension getPreferredSize() {
        return extremeLevel >= 1
                ? new Dimension(super.getPreferredSize().width + 10, super.getPreferredSize().height)
                : super.getPreferredSize();
    }
}
