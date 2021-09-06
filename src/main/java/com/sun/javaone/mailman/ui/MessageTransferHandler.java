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
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.image.*;
import com.sun.javaone.mailman.ui.image.*;
import org.jdesktop.animation.timing.*;

/**
 * @author shannonh
 */
public class MessageTransferHandler extends TransferHandler implements DragSourceMotionListener, TimingTarget {
    private MailBoxTree tree;

    public MessageTransferHandler(MailBoxTree tree) {
        this.tree = tree;
    }

    private DragOverGlassPane gp = new DragOverGlassPane();

    public int getSourceActions(JComponent c) {
        return c instanceof JTable ? COPY_OR_MOVE : NONE;
    }

    public Transferable createTransferable(JComponent c) {
        if (!(c instanceof JTable)) {
            return null;
        }

        JTable table = (JTable)c;
        int count = table.getSelectedRowCount();
        if (count == 0) {
            return null;
        }

        if (tree.getExtremeLevel() >= 1) {
            DragSource.getDefaultDragSource().addDragSourceMotionListener(this);
            gp.showIt(c, MessageIconGenerator.createIcon(count), tree.getExtremeLevel() >= 2);
        }
        return new StringSelection("foo");
    }

    public boolean canImport(TransferSupport support) {
        Component comp = support.getComponent();
        if (comp instanceof JTable) {
            return false;
        }

        JTree tree = (JTree)comp;
        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();

        if (dl.getPath() == null) {
            return false;
        }
        return tree.getRowForPath(dl.getPath()) != 0;
    }

    public void exportDone(JComponent source, Transferable data, int action) {
        if (tree.getExtremeLevel() >= 1) {
            DragSource.getDefaultDragSource().removeDragSourceMotionListener(this);
            boolean accepted = (action != NONE);
            if (tree.getExtremeLevel() >= 2) {
                gp.setAccepted(accepted);
                new TimingController(300, this).start();
            } else {
                gp.hideIt();
            }
        }
    }

    public boolean importData(TransferSupport support) {
        return true;
    }

    public void dragMouseMoved(DragSourceDragEvent dsde) {
        if (to == null) {
            to = dsde.getDragSourceContext().getTrigger().getDragOrigin();

            //java.awt.event.MouseEvent me = (java.awt.event.MouseEvent)ie;
            //Point p = me.getLocationOnScreen();
            to = SwingUtilities.convertPoint(dsde.getDragSourceContext().getTrigger().getComponent(), to, gp);
            to.x += 12;
            to.y -= 2;
            //to = p;
        }
        gp.moveIt(dsde.getLocation());
    }

    public void timingEvent(long l, long l0, float f) {
        if (gp.getAccepted()) {
            gp.repaintIt(f);
        } else {
            Point newP = new Point();
            newP.x = from.x + (int)((to.x - from.x) * f);
            newP.y = from.y + (int)((to.y - from.y) * f);
            gp.moveItNoConv(newP, f);
        }
    }

    Point from;
    Point to;

    public void begin() {
        from = gp.getPoint();
    }

    public void end() {
        from = null;
        to = null;
        gp.hideIt();
    }
}

class DragOverGlassPane extends JPanel {
    JRootPane rp;
    Component oldGP;
    JComponent comp;
    Point p = null;
    BufferedImage image = null;
    private boolean withShadow;
    private boolean accepted;

    private static final int XOFFSET = 10;
    private static final int YOFFSET = 20;

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
    public boolean getAccepted() {
        return this.accepted;
    }
    DragOverGlassPane() {
        setOpaque(false);
    }

    public Point getPoint() {
        return p;
    }

    public void showIt(JComponent comp, BufferedImage image, boolean withShadow) {
        this.comp = comp;
        this.image = image;
        this.withShadow = withShadow;

        rp = ((JFrame)SwingUtilities.getWindowAncestor(comp)).getRootPane();
        oldGP = rp.getGlassPane();
        rp.setGlassPane(this);
        setVisible(true);

        Point p = MouseInfo.getPointerInfo().getLocation();
        moveIt(p);

        repaint();
    }

    public void hideIt() {
        if (oldGP != null) {
            rp.setGlassPane(oldGP);
            oldGP = null;
            rp = null;
            p = null;
            comp = null;
            f = -1;
        }
    }

    private float f;

    public void repaintIt(float f) {
        this.f = f;
        repaintPoint(this.p);
    }

    public void moveItNoConv(Point p, float f) {
        repaintIt(f);
        this.p = p;
        this.f = f;
        repaintIt(f);
    }
    public void moveIt(Point p) {
        repaintPoint(this.p);

        SwingUtilities.convertPointFromScreen(p, this);
        p.x += 12;
        p.y -= 2;
        this.p = p;

        repaintPoint(this.p);
    }

    private void repaintPoint(Point p) {
        if (this.p != null) {
            repaint(p.x, p.y, image.getWidth() + XOFFSET, image.getHeight() + YOFFSET);
        }
    }

    public void paintComponent(Graphics g) {
        if (p != null) {
            Graphics2D g2d = (Graphics2D)g.create();
            float b = -1;

            if (!accepted) {
                if (f >= 0.8f) {
                    b = (f - 0.8f) / 0.8f;
                    b = 1f - b;
                }
            } else if (f >= 0f) {
                b = 1f - f;
            }

            if (b != -1) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, b));
            }
            int width = image.getWidth();
            int height = image.getHeight();

            b = -1;
            if (!accepted) {
                if (f >= 0.5f) {
                    b = (f - 0.5f) / 0.5f;
                    b = 1f - b;
                }
            } else if (f >= 0f) {
                b = 1f - f;
            }

            if (b != -1) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                width = (int)(width * b);
                height = (int)(height * b);
            }

            if (withShadow) {
                ShadowFactory factory = new ShadowFactory(5, 0.2f, Color.BLACK);
                BufferedImage shadow = factory.createShadow(image);
                if (b != -1) {
                    g2d.translate(b * XOFFSET, b * YOFFSET);
                    g2d.drawImage(shadow, p.x, p.y, width, height, null);
                    g2d.translate(-b * XOFFSET, -b * YOFFSET);
                } else {
                    g2d.drawImage(shadow, p.x + XOFFSET, p.y + YOFFSET, width, height, null);
                }
            }
            g2d.drawImage(image, p.x, p.y, width, height, null);
            g2d.dispose();
        }
    }
}
