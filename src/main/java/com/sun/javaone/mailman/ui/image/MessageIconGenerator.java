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
package com.sun.javaone.mailman.ui.image;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sun.javaone.mailman.model.Message;
import com.sun.javaone.mailman.ui.geom.StarShape;

public final class MessageIconGenerator {
    private MessageIconGenerator() {
    }


    public static BufferedImage createIcon(Message[] messages) {
        return createIcon(messages.length);
    }

    public static BufferedImage createIcon(int count) {
        int width = 32;
        int height = 32;

        if (count == 2) {
            width += 4;
            height += 4;
        } else if (count == 3) {
            // two more messages in the stack
            width += 8;
            height += 8;
        } else if (count > 3) {
            // badge + messages stack
            width += 10 + 8;
            height += 8;
        }

        BufferedImage paperSheet = null;
        try {
            paperSheet = ImageIO.read(MessageIconGenerator.class.getResource("/resources/icons/paper-sheet.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage image = GraphicsUtil.createTranslucentCompatibleImage(width, height);
        Graphics2D g2 = image.createGraphics();

        g2.drawImage(paperSheet, 0, 0, null);
        if (count >= 2) {
            g2.drawImage(paperSheet, 4, 4, null);
        }

        if (count >= 3) {
            g2.drawImage(paperSheet, 8, 8, null);
        }

        BufferedImage image2 = GraphicsUtil.createTranslucentCompatibleImage(width, height);
        Graphics2D g2d = image2.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2.dispose();
        image.flush();

        image = image2;
        g2 = g2d;

        if (count > 3) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(image.getWidth() - 25, 0);

            g2.setColor(new Color(0.0f, 0.0f, 0.0f, 0.25f));
            StarShape star = new StarShape(0.0, 0.0, 8.0, 11.0, 22);
            g2.translate(0, -1);
            g2.fill(star);
            g2.translate(0, 2);
            g2.fill(star);
            g2.translate(0, -1);
            g2.translate(-1, 0);
            g2.fill(star);
            g2.translate(2, 0);
            g2.fill(star);
            g2.translate(-1, 0);

            Paint paint = g2.getPaint();
            g2.setPaint(new GradientPaint(
                        new Point2D.Double(0, 0),
                        new Color(244, 90, 90),
                        new Point2D.Double(18, 18),
                        new Color(200, 45, 45)));
            g2.fill(star);
            g2.setPaint(paint);

            String text = String.valueOf(count);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 14));

            Rectangle2D bounds = g2.getFontMetrics().getStringBounds(text, g2);
            double textWidth = bounds.getWidth();
            double textHeight = bounds.getHeight();

            double tx = (22.0 - textWidth) / 2.0;
            double ty = (22.0 - textHeight) / 2.0 + g2.getFontMetrics().getAscent();

            g2.translate(tx, ty);
            g2.drawString(text, 0, 0);
            g2.translate(-tx, -ty);

            g2.translate(-image.getWidth() + 25, 0);
        }
        g2.dispose();

        return image;
    }

    public static BufferedImage createIcon(int oldNum, int newNum, float pct) {
        int width = 22;
        int height = 22;

        BufferedImage image = GraphicsUtil.createTranslucentCompatibleImage(width, height);
        Graphics2D g2 = image.createGraphics();
        if (oldNum == 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pct));
        } else if (newNum == 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1 - pct));
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0.0f, 0.0f, 0.0f, 0.25f));
        StarShape star = new StarShape(0.0, 0.0, 8.0, 11.0, 22);
        g2.translate(0, -1);
        g2.fill(star);
        g2.translate(0, 2);
        g2.fill(star);
        g2.translate(0, -1);
        g2.translate(-1, 0);
        g2.fill(star);
        g2.translate(2, 0);
        g2.fill(star);
        g2.translate(-1, 0);

        Paint paint = g2.getPaint();
        g2.setPaint(new GradientPaint(
                new Point2D.Double(0, 0),
                new Color(244, 90, 90),
                new Point2D.Double(18, 18),
                new Color(200, 45, 45)));
        g2.fill(star);
        g2.setPaint(paint);

        if (oldNum != 0 && newNum != 0 && oldNum != newNum) {
            paintNum(g2, oldNum, 1.0f - pct);
            paintNum(g2, newNum, pct);
        } else if (oldNum != 0) {
            paintNum(g2, oldNum, -1f);
        } else {
            paintNum(g2, newNum, -1f);
        }

        g2.translate(-image.getWidth() + 25, 0);

        g2.dispose();
        return image;
    }

    private static void paintNum(Graphics2D g2, int count, float pct) {
        String text = String.valueOf(count);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));

        Rectangle2D bounds = g2.getFontMetrics().getStringBounds(text, g2);
        double textWidth = bounds.getWidth();
        double textHeight = bounds.getHeight();

        double tx = (22.0 - textWidth) / 2.0;
        double ty = (22.0 - textHeight) / 2.0 + g2.getFontMetrics().getAscent();

        g2.translate(tx, ty);

        if (pct >= 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pct));
        }
        g2.drawString(text, 0, 0);
        g2.translate(-tx, -ty);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Icon Generator");

        JPanel panel = new JPanel(new FlowLayout());
        for (int i = 1; i < 5; i++) {
            ImageIcon icon = new ImageIcon(createIcon(new Message[i]));
            panel.add(new JLabel(String.valueOf(i), icon, JLabel.TRAILING));
        }
        f.add(panel);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(320, 100);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
