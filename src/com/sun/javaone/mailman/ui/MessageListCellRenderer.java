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

import com.sun.javaone.mailman.model.Contact;
import com.sun.javaone.mailman.model.Message;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.text.BreakIterator;
import java.text.CharacterIterator;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 *
 * @author sky
 */
public class MessageListCellRenderer implements ListCellRenderer {
    private static final int IS = 48;
    private static final Color FROM_COLOR = new Color(0,  81, 212);
    
    private final RendererPanel panel;
    
    public MessageListCellRenderer() {
        panel = new RendererPanel();
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof String) {
            panel.setMessage(null);
        } else {
            panel.setMessage((Message)value);
        }
        if (isSelected) {
            adjustColors(list.getSelectionBackground(),
                    list.getSelectionForeground());
        } else {
            adjustColors(list.getBackground(),
                    list.getForeground());
            if ((index % 2) == 0) {
                panel.setBackground(MailTable.STRIPE_COLOR);
            }
            panel.fromLabel.setForeground(FROM_COLOR);
            for (Component c : panel.labels) {
                c.setForeground(Color.DARK_GRAY);
            }
        }
        return panel;
    }
        
    private void adjustColors(Color bg, Color fg) {
        for (Component c : panel.toAdjust) {
            c.setForeground(fg);
            c.setBackground(bg);
        }
    }

    private int getRowCount() {
        return 2;
    }
    
    private class RendererPanel extends JPanel {
        private final JLabel dateLabel;
        private final JLabel labels[];
        private final JLabel fromLabel;
        private final JImagePanel imagePanel;
        private final JLabel subjectLabel;
        private final List<Component> toAdjust;
        private final char[] tmpChars;
        private String text;
        private int layoutWidth;

        RendererPanel() {
            tmpChars = new char[512];
            setBorder(new EmptyBorder(4, 4, 4, 4));
            setOpaque(true);
            fromLabel = new JLabel(" ");
            fromLabel.setForeground(FROM_COLOR);
            fromLabel.setFont(fromLabel.getFont().deriveFont(Font.ITALIC));
            subjectLabel = new JLabel("a");
            dateLabel = new JLabel("a");
            dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD));
            Font f = subjectLabel.getFont();
            subjectLabel.setFont(f.deriveFont(Font.BOLD, f.getSize() + 4f));
            labels = new JLabel[getRowCount()];
            for (int i = 0; i < getRowCount(); i++) {
                labels[i] = new JLabel(" ");
            }
            imagePanel = new JImagePanel();
            imagePanel.setBorder(new LineBorder(Color.BLACK, 1));
            imagePanel.setEditable(false);
            
            GroupLayout layout = new GroupLayout(this);
            setLayout(layout);
            
            GroupLayout.ParallelGroup labelHG = layout.createParallelGroup();
            GroupLayout.SequentialGroup labelVG = layout.createSequentialGroup();
            for (int i = 0; i < labels.length; i++) {
                if (i > 0) {
                    labelVG.addGap(0);
                }
                if (i < labels.length - 1) {
                    labelHG.addComponent(labels[i], 0, 0, Integer.MAX_VALUE);
                    labelVG.addComponent(labels[i]);
                }
            }
            labelVG.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                    addComponent(labels[labels.length - 1], 0, 0, Integer.MAX_VALUE).
                    addComponent(fromLabel));
            labelHG.addGroup(layout.createSequentialGroup().
                    addComponent(labels[labels.length - 1], 0, 0, Integer.MAX_VALUE).
                    addComponent(fromLabel));
            GroupLayout.SequentialGroup hg = layout.createSequentialGroup();
            hg.addComponent(imagePanel, IS, IS, IS).
               addGroup(layout.createParallelGroup().
                 addGroup(layout.createSequentialGroup().
                   addComponent(subjectLabel, 0, 0, Integer.MAX_VALUE).
                   addComponent(dateLabel)).
                 addGroup(labelHG));
            layout.setHorizontalGroup(hg);

            GroupLayout.ParallelGroup vg = layout.createParallelGroup();
            vg.addComponent(imagePanel, GroupLayout.Alignment.CENTER, IS, IS, IS).
               addGroup(layout.createSequentialGroup().
                 addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                   addComponent(subjectLabel).
                   addComponent(dateLabel)).
                 addGap(0).
                 addGroup(labelVG));
            layout.setVerticalGroup(vg);
//            layout.setAutoCreateContainerGaps(true);
            layout.setAutoCreateGaps(true);
            
            toAdjust = new LinkedList<Component>();
            toAdjust.add(dateLabel);
            toAdjust.addAll(Arrays.asList(labels));
            toAdjust.add(this);
            toAdjust.add(fromLabel);
        }
        
        public void setMessage(Message m) {
            if (m == null) {
                text = null;
                subjectLabel.setText(" ");
                fromLabel.setText(" ");
                resetMessageLabels();
            } else {
                subjectLabel.setText(m.getSubject());
                if (m.getFrom() != null) {
                    imagePanel.setImagePath(m.getFrom().getImageLocation());
                } else {
                    imagePanel.setImage(null);
                }
                text = m.getBody();
                reflowText();
                dateLabel.setText(DateHelper.convert(m.getDateTime()));
                Contact from = m.getFrom();
                if (from != null) {
                    fromLabel.setText(from.getDisplayName());
                } else {
                    fromLabel.setText(" ");
                }
             }
        }
        
        public void doLayout() {
            super.doLayout();
            if (layoutWidth != getWidth()) {
                layoutWidth = getWidth();
                reflowText();
            }
        }
        
        private int nextNonWhitespace(int index, int length) {
            while (index < length && Character.isWhitespace(tmpChars[index])) {
                tmpChars[index] = ' ';
                index++;
            }
            return index;
        }
        
        private int nextWhitespace(int index, int length) {
            while (index < length && !Character.isWhitespace(tmpChars[index])) {
                index++;
            }
            return index;
        }

        private void resetMessageLabels() {
            for (JLabel label : labels) {
                label.setText(" ");
            }
        }
        
        private void reflowText() {
            resetMessageLabels();
            int availableWidth = labels[0].getWidth();
            if (availableWidth > 0 && text != null) {
                int charCount = Math.min(tmpChars.length, text.length());
                text.getChars(0, charCount, tmpChars, 0);
                FontMetrics fm = labels[0].getFontMetrics(labels[0].getFont());
                int lineStart = nextNonWhitespace(0, charCount);
                int lastChunkThatFits = lineStart;
                int lineIndex = 0;
                int index = lineStart;
                boolean tooLong = false;
                while (!tooLong && index < charCount && lineIndex < labels.length) {
                    index = nextWhitespace(index, charCount);
                    int charWidth = fm.charsWidth(tmpChars, lineStart, index - lineStart);
                    if (charWidth < availableWidth) {
                        lastChunkThatFits = index;
                        index = nextNonWhitespace(index, charCount);
                    } else if (lastChunkThatFits == lineStart) {
                        tooLong = true;
                    } else {
                        // charWidth > availableWidth, lastChunkThatFits != lineStart
                        labels[lineIndex++].setText(new String(
                                tmpChars, lineStart, lastChunkThatFits - lineStart));
                        lineStart = nextNonWhitespace(lastChunkThatFits, charCount);
                        lastChunkThatFits = lineStart;
                    }
                }
                if (!tooLong && lineIndex < labels.length && lineStart != charCount) {
                    labels[lineIndex].setText(new String(
                            tmpChars, lineStart, charCount - lineStart));
                }
            }
        }
    }
}
