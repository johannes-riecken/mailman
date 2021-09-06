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

import com.sun.javaone.mailman.model.MailBox;
import com.sun.javaone.mailman.ui.image.DropShadowPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;

/**
 *
 * @author sky
 */
public class MessageListPanel extends JPanel {
    private final JList list;
    private final MailBoxLabel mailBoxLabel;
    private final JXPanel header;
    private final JScrollPane scrollPane;

    public MessageListPanel() {
        FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
        layout.setHgap(0);
        layout.setVgap(0);
        header = new JXPanel(layout);
        header.setBorder(new EmptyBorder(2, 5, 5, 5));
        header.setOpaque(false);

        DropShadowPanel dsp = new DropShadowPanel(new BorderLayout());
        dsp.setBorder(new EmptyBorder(0, 0, 5, 5));
        dsp.add(new JLabel(new BackIcon()));
        header.add(dsp);
        header.setAlpha(0f);

        mailBoxLabel = new MailBoxLabel();
        dsp = new DropShadowPanel(new BorderLayout());
        dsp.setBorder(new EmptyBorder(0, 2, 5, 5));
        dsp.add(mailBoxLabel);
        header.add(dsp);

        setLayout(new BorderLayout());
        list = new MessageList();
        list.setName("mailList");
        list.setCellRenderer(new MessageListCellRenderer());
        list.setPrototypeCellValue("xxx");
        JPanel scrollPanePanel = new JPanel(new BorderLayout());
        scrollPanePanel.setBorder(new DropShadowBorder(Color.BLACK,
                        0, 5, .5f, 12,
                        false, true, true, true));
        scrollPane = new JScrollPane(list);
        scrollPanePanel.add(scrollPane);
        add(scrollPanePanel, BorderLayout.CENTER);
    }

    public void ensureListInScrollPane() {
        scrollPane.setViewportView(list);
    }

    public void setMailBox(MailBox mbox) {
        mailBoxLabel.setMailBox(mbox);
    }

    public JList getList() {
        return list;
    }

    public void showHeader() {
        if (!header.isShowing()) {
            add(header, BorderLayout.NORTH);
            final Dimension headerPref = header.getPreferredSize();
            header.setPreferredSize(new Dimension(headerPref.width, 0));
            TimingController tc = new TimingController(
                    new Cycle(250, 30),
                    new Envelope(1, 0, Envelope.RepeatBehavior.FORWARD,
                    Envelope.EndBehavior.HOLD));
            tc.addTarget(new TimingTarget() {
                public void begin() {
                }
                public void end() {
                    header.setPreferredSize(headerPref);
                    fadeInHeaderIcons();
                }
                public void timingEvent(long l, long l0, float f) {
                    header.setPreferredSize(new Dimension(1, (int)(headerPref.height * f)));
                    revalidate();
                    repaint();
                }
            });
            tc.start();
        }
    }


    private void fadeInHeaderIcons() {
        TimingController tc = new TimingController(
                new Cycle(250, 30),
                new Envelope(1, 0, Envelope.RepeatBehavior.FORWARD,
                Envelope.EndBehavior.HOLD));
        tc.addTarget(new TimingTarget() {
            public void begin() {
            }
            public void end() {
                header.setAlpha(1.0f);
            }
            public void timingEvent(long l, long l0, float f) {
                header.setAlpha(f);
            }
        });
        tc.start();
    }


    private static final class MessageList extends JList {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int height = getHeight();
            int prefHeight = getPreferredSize().height;
            if (prefHeight < height) {
                int modelSize = getModel().getSize();
                int cellHeight = getFixedCellHeight();
                int startRow = modelSize / 2 * 2;
                if (startRow < modelSize) {
                    startRow += 2;
                }
                int y = startRow * cellHeight;
                int w = getWidth();
                g.setColor(MailTable.STRIPE_COLOR);
                while (y < height) {
                    g.fillRect(0, y, w, cellHeight);
                    y += cellHeight + cellHeight;
                }
            }
        }
    }

    private static final class MailBoxLabel extends JLabel {
        MailBoxLabel() {
            setVerticalAlignment(JButton.CENTER);
            setBorder(MailBoxTree.LRBORDER);
            setForeground(Color.BLACK);
            setFont(getFont().deriveFont(Font.BOLD, getFont().getSize() + 6f));
        }

        public void setMailBox(MailBox mailBox) {
            String val = mailBox.getName();
            setText(mailBox.getName());
            if (val.equals(MailBoxTree.SENT)) {
                setIcon(MailBoxTree.SENT_ICON);
            } else if (val.equals(MailBoxTree.TRASH)) {
                setIcon(MailBoxTree.TRASH_ICON);
            } else if (val.equals(MailBoxTree.INBOX)) {
                setIcon(MailBoxTree.INBOX_ICON);
            } else if (val.equals(MailBoxTree.DRAFT)) {
                setIcon(MailBoxTree.DRAFT_ICON);
            } else {
                setIcon(MailBoxTree.CLOSED_ICON);
            }
        }

        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D)g.create();
            int corn = 20;
            int h = getHeight() - 2 * MailBoxTree.OUTSET;
            boolean sel = true;

            if (sel) {
                g2d.setPaint(new GradientPaint(0, MailBoxTree.OUTSET, Color.WHITE, 0, getHeight() - 2 * MailBoxTree.OUTSET, MailBoxTree.COLOR1, true));
            } else {
                g2d.setPaint(MailBoxTree.GP);
            }

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D.Float r2d = new RoundRectangle2D.Float(0, MailBoxTree.OUTSET, getWidth(), h, corn, corn);
            Shape clip = g2d.getClip();
            g2d.clip(r2d);
            g2d.fillRoundRect(0, MailBoxTree.OUTSET, getWidth(), h, corn, corn);
            g2d.setClip(clip);
            GradientPaint p1 = new GradientPaint(0, MailBoxTree.OUTSET, new Color(100, 100, 100), 0, h - 1, new Color(0, 0, 0));
            g2d.setPaint(p1);
            g2d.drawRoundRect(0, MailBoxTree.OUTSET, getWidth() - 1, h - 1, corn, corn);
            GradientPaint p2 = new GradientPaint(0, MailBoxTree.OUTSET + 1, new Color(255, 255, 255, 100), 0, h - 3, new Color(0, 0, 0, 50));
            g2d.setPaint(p2);
            g2d.drawRoundRect(1, MailBoxTree.OUTSET + 1, getWidth() - 3, h - 3, corn - 2, corn - 2);

            g2d.dispose();
            super.paint(g);
        }

        public Dimension getPreferredSize() {
            int extremeLevel = 1;
            return extremeLevel >= 1
                    ? new Dimension(Math.max(MailBoxTree.NODE_SIZE.width, super.getPreferredSize().width + 6), MailBoxTree.NODE_SIZE.height)
                    : super.getPreferredSize();
        }
    }
   }
