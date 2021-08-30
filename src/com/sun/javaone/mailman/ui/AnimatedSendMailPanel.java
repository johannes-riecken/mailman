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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.TimingEvent;
import org.jdesktop.animation.timing.TimingListener;
import org.jdesktop.animation.timing.interpolation.ObjectModifier;
import org.jdesktop.animation.timing.interpolation.PropertyRange;
import org.jdesktop.swingx.JXPanel;
import com.sun.javaone.mailman.ui.geom.StarShape;

public class AnimatedSendMailPanel extends JXPanel {
    private BufferedImage image;

    private TimingController controller;
    private float messageZoom = 1.0f;
    private float folded = 0.0f;
    private float envelopeFolded = 0.0f;
    private float stamp = 0.0f;
    private float send = 0.0f;

    private boolean isFolding = false;
    private boolean isEnvelopeFolding = false;
    private boolean isStamping = false;

    private StarShape stampShape = new StarShape(0.0, 0.0, 16.0, 18.0, 22);

    private static final float ENVELOPE_RATIO = 1.3f;
    private static final float MESSAGE_WIDTH = 0.2f;
    private boolean slowDown;

    void startAnimation(BufferedImage image, boolean slowDown) {
        if (controller != null && controller.isRunning()) {
            return;
        }

        this.image = image;
        this.slowDown = slowDown;

        Cycle cycle = new Cycle(!slowDown ? 700 : 7000, 12);
        Envelope envelope = new Envelope(1, 200, Envelope.RepeatBehavior.FORWARD,
                                         Envelope.EndBehavior.HOLD);
        PropertyRange range =
                PropertyRange.createPropertyRangeFloat("messageZoom", 1.0f, MESSAGE_WIDTH);
        ObjectModifier target = new ObjectModifier(this, range);
        controller = new TimingController(cycle, envelope, target);
        controller.setAcceleration(0.5f);
        controller.setDeceleration(0.2f);
        controller.addTimingListener(new TimingListener() {
            public void timerStarted(TimingEvent timingEvent) {
            }

            public void timerStopped(TimingEvent timingEvent) {
                foldMessage();
            }

            public void timerRepeated(TimingEvent timingEvent) {
            }
        });
        controller.start();
    }

    private void foldMessage() {
        Cycle cycle = new Cycle(!slowDown ? 700 : 7000, 12);
        Envelope envelope = new Envelope(1, 0, Envelope.RepeatBehavior.FORWARD,
                                         Envelope.EndBehavior.HOLD);
        PropertyRange range =
                PropertyRange.createPropertyRangeFloat("folded", 0.0f, 1.0f);
        ObjectModifier target = new ObjectModifier(this, range);
        controller = new TimingController(cycle, envelope, target);
        controller.setAcceleration(0.5f);
        controller.addTimingListener(new TimingListener() {
            public void timerStarted(TimingEvent timingEvent) {
                isFolding = true;
            }

            public void timerStopped(TimingEvent timingEvent) {
                foldEnvelope();
            }

            public void timerRepeated(TimingEvent timingEvent) {
            }
        });
        controller.start();
    }

    private void foldEnvelope() {
        Cycle cycle = new Cycle(!slowDown ? 700 : 7000, 12);
        Envelope envelope = new Envelope(1, 0, Envelope.RepeatBehavior.FORWARD,
                                         Envelope.EndBehavior.HOLD);
        PropertyRange range =
                PropertyRange.createPropertyRangeFloat("envelopeFolded", 0.0f, 1.0f);
        ObjectModifier target = new ObjectModifier(this, range);
        controller = new TimingController(cycle, envelope, target);
        controller.setAcceleration(0.5f);
        controller.addTimingListener(new TimingListener() {
            public void timerStarted(TimingEvent timingEvent) {
                isEnvelopeFolding = true;
            }

            public void timerStopped(TimingEvent timingEvent) {
                applyStamp();
            }

            public void timerRepeated(TimingEvent timingEvent) {
            }
        });
        controller.start();
    }

    private void applyStamp() {
        Cycle cycle = new Cycle(!slowDown ? 500 : 5000, 12);
        Envelope envelope = new Envelope(1, 0, Envelope.RepeatBehavior.FORWARD,
                                         Envelope.EndBehavior.HOLD);
        PropertyRange range =
                PropertyRange.createPropertyRangeFloat("stamp", 0.0f, 1.0f);
        ObjectModifier target = new ObjectModifier(this, range);
        controller = new TimingController(cycle, envelope, target);
        controller.setAcceleration(0.5f);
        controller.setAcceleration(0.2f);
        controller.addTimingListener(new TimingListener() {
            public void timerStarted(TimingEvent timingEvent) {
                isStamping = true;
            }

            public void timerStopped(TimingEvent timingEvent) {
                startSend();
            }

            public void timerRepeated(TimingEvent timingEvent) {
            }
        });
        controller.start();
    }

    private void startSend() {
        Cycle cycle = new Cycle(!slowDown ? 500 : 5000, 12);
        Envelope envelope = new Envelope(1, 0, Envelope.RepeatBehavior.FORWARD,
                                         Envelope.EndBehavior.HOLD);
        PropertyRange range =
                PropertyRange.createPropertyRangeFloat("send", 0.0f, 1.0f);
        ObjectModifier target = new ObjectModifier(this, range);
        controller = new TimingController(cycle, envelope, target);
        controller.setAcceleration(1.0f);
        controller.addTimingListener(new TimingListener() {
            public void timerStarted(TimingEvent timingEvent) {
            }

            public void timerStopped(TimingEvent timingEvent) {
                firePropertyChange("message_sent", false, true);
            }

            public void timerRepeated(TimingEvent timingEvent) {
            }
        });
        controller.start();
    }

    public float getMessageZoom() {
        return messageZoom;
    }

    public void setMessageZoom(float messageZoom) {
        this.messageZoom = messageZoom;
        repaint();
    }

    public float getFolded() {
        return folded;
    }

    public void setFolded(float folded) {
        this.folded = folded;
        repaint();
    }

    public float getEnvelopeFolded() {
        return envelopeFolded;
    }

    public void setEnvelopeFolded(float envelopeFolded) {
        this.envelopeFolded = envelopeFolded;
        repaint();
    }

    public float getStamp() {
        return stamp;
    }

    public void setStamp(float stamp) {
        this.stamp = stamp;
        repaint();
    }

   public float getSend() {
        return send;
    }

    public void setSend(float send) {
        this.send = send;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        setupHints(g2);

        drawSent(g2);

        g2.translate(send * getWidth(), 0);
        drawEnvelope(g2);
        g2.translate(-send * getWidth(), 0);
    }

    private void drawSent(Graphics2D g2) {
        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.SrcOver.derive(send));

        String text = "Your message has been sent!";
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setFont(new Font("Arial", Font.BOLD, 26));

        Rectangle2D bounds = g2.getFontMetrics().getStringBounds(text, g2);
        double textWidth = bounds.getWidth();
        double textHeight = bounds.getHeight();

        double tx = (getWidth() - textWidth) / 2.0;
        double ty = (getHeight() - textHeight) / 2.0 + g2.getFontMetrics().getAscent();

        g2.translate(tx, ty);
        g2.setColor(new Color(0.0f, 0.0f, 0.0f, 0.5f * send));
        g2.drawString(text, 0, 2);
        g2.setColor(Color.WHITE);
        g2.drawString(text, 0, 0);
        g2.translate(-tx, -ty);

        g2.setComposite(oldComposite);
    }

    private void drawEnvelope(Graphics2D g2) {
        float width = image.getWidth() * (MESSAGE_WIDTH * 1.1f);
        float height = width / ENVELOPE_RATIO;

        float ty = height * (1.0f - 0.5f * folded);
        g2.translate(0, ty - 20);

        float x = (getWidth() - width) / 2.0f;
        float y = (getHeight() - height) / 2.0f;

        float alpha = Math.min(1.0f, 1.2f - messageZoom);
        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.SrcOver.derive(alpha));

        Paint gradient = new GradientPaint(x, y - height / 4.0f, new Color(0xfffffe),
                                           x, y + height, new Color(0xffeaba));
        Color borderColor = new Color(0xd5b296);

        Paint oldPaint = g2.getPaint();
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND,
                                     BasicStroke.JOIN_ROUND));

        // back
        Shape main = drawBack(x, y, width, height, g2, gradient);
        if (!isEnvelopeFolding) {
            drawTopFold(x, y, width, height, g2, gradient, borderColor);
        }

        int messageWidth = (int) (image.getWidth() * messageZoom);
        int messageHeight = (int) (image.getHeight() * messageZoom);

        int messageX = (getWidth() - messageWidth) / 2;
        int messageY = 10;

        if (isFolding) {
            double remainder = height - ty;
            remainder -= 10 * folded;
            g2.translate(0, -ty + 20 + remainder);
            g2.setComposite(oldComposite);
            drawMessage(messageX, messageY, messageWidth, messageHeight, g2);
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.translate(0, ty - 20 - remainder);
        }

        x++;
        y++;
        width -= 2;
        height -= 2;

        // left fold
        drawLeftFold(x, y, width, height, g2, borderColor);

        // right fold
        drawRightFold(x, y, width, height, g2, borderColor);

        // bottom fold
        drawBottomFold(x, y, height, width, g2, borderColor);

        // back
        borderColor = new Color(0xa85648);
        g2.setColor(borderColor);
        g2.draw(main);

        if (!isFolding) {
            g2.translate(0, -ty + 20);
            g2.setComposite(oldComposite);
            drawMessage(messageX, messageY, messageWidth, messageHeight, g2);
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.translate(0, ty - 20);
        }

        if (isEnvelopeFolding) {
            x--;
            y--;
            width += 2;
            height += 2;

            drawTopFold(x, y, width, height, g2, gradient, borderColor);
        }

        drawStamp(x, y, width, height, g2);

        // restore context
        g2.setStroke(oldStroke);
        g2.setPaint(oldPaint);
        g2.setComposite(oldComposite);
        g2.translate(0, -ty + 20);
    }

    private void drawStamp(float x, float y, float width, float height,
                           Graphics2D g2) {
        if (!isStamping) {
            return;
        }

        Rectangle bounds = stampShape.getBounds();
        double tx = x + width / 2.0 - bounds.getWidth() / 2.0;
        double ty = y + height / 2.0 - bounds.getHeight() / 2.0;
        g2.translate(tx, ty);

        g2.setComposite(AlphaComposite.SrcOver.derive(stamp));
        g2.setColor(new Color(0.0f, 0.0f, 0.0f, 0.15f));
        g2.translate(0, -1);
        g2.fill(stampShape);
        g2.translate(0, 2);
        g2.fill(stampShape);
        g2.translate(0, -1);
        g2.translate(-1, 0);
        g2.fill(stampShape);
        g2.translate(2, 0);
        g2.fill(stampShape);
        g2.translate(-1, 0);
        Paint paint = g2.getPaint();
        g2.setPaint(new GradientPaint(
                    new Point2D.Double(0, 0),
                    new Color(244, 90, 90),
                    new Point2D.Double(bounds.getWidth(), bounds.getHeight()),
                    new Color(200, 45, 45)));
        g2.fill(stampShape);
        g2.setPaint(paint);

        g2.setColor(Color.WHITE);
        String text = "@";
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));

        Rectangle2D textBounds = g2.getFontMetrics().getStringBounds(text, g2);
        double textWidth = textBounds.getWidth();
        double textHeight = textBounds.getHeight();

        double tx2 = (bounds.getWidth() - textWidth) / 2.0;
        double ty2 = (bounds.getHeight() - textHeight) / 2.0 + g2.getFontMetrics().getAscent();

        g2.translate(tx2, ty2);
        g2.drawString(text, 0, 0);
        g2.translate(-tx2, -ty2);

        g2.translate(-tx, -ty);
    }

    private void drawTopFold(float x, float y, float width, float height,
                             Graphics2D g2, Paint gradient, Color borderColor) {
        GeneralPath topFold = new GeneralPath();
        topFold.moveTo(x, y);

        if (isEnvelopeFolding) {
            y -= 2.0f;
            float new_y = (height / 2.0f - 1.0f) * (1.0f - envelopeFolded);
            float a = -(height / 2.0f - 1.0f) / -(width / 2.0f);
            float new_x = new_y / a;

            topFold.lineTo(x + new_x, y - new_y);
            topFold.lineTo(x + (width - new_x), y - new_y);
            topFold.lineTo(x + width, y + 2.0f);

            g2.setPaint(gradient);
            g2.fill(topFold);
            borderColor = new Color(0xa85648);
            g2.setColor(borderColor);
            g2.draw(topFold);

            topFold = new GeneralPath();
            topFold.moveTo(x + new_x, y - new_y);
            topFold.lineTo(x + (width - new_x), y - new_y);
            topFold.lineTo(x + width / 2.0f, y - new_y + (height / 2.0f + 6.0f) * envelopeFolded);
            topFold.closePath();

            gradient = new GradientPaint(x, y - new_y,
                                         new Color(0xfffffe),
                                         x, y - new_y + (height / 3.0f) * envelopeFolded,
                                         new Color(0xffeaba));
            g2.setPaint(gradient);
            g2.fill(topFold);

            gradient = new GradientPaint(x, y,
                                         new Color(0xa85648),
                                         x, y + height / 2.0f,
                                         new Color(0x926057));
            g2.setPaint(gradient);
            g2.draw(topFold);
        } else {
            topFold.lineTo(x + width / 2.0f, y - height / 2.0f - 3.0f);
            topFold.lineTo(x + width, y);

            g2.setPaint(gradient);
            g2.fill(topFold);

            borderColor = new Color(0xa85648);
            g2.setColor(borderColor);
            g2.draw(topFold);
        }
    }

    private static Shape drawBack(float x, float y, float width, float height,
                                  Graphics2D g2, Paint gradient) {
        //Shape main = new RoundRectangle2D.Double(x, y, width, height, 6, 6);
        GeneralPath main = new GeneralPath();
        main.moveTo(x, y - 1.0f);
        main.lineTo(x, y + height - 3.0f);
        main.curveTo(x, y + height, x + 3.0f, y + height, x + 3.0f, y + height);
        main.lineTo(x + width - 3.0f, y + height);
        main.curveTo(x + width, y + height, x + width, y + height - 3.0f, x + width, y + height - 3.0f);
        main.lineTo(x + width, y - 1.0f);
        g2.setPaint(gradient);
        g2.fill(main);

        return main;
    }

    private static void drawLeftFold(float x, float y, float width, float height,
                                     Graphics2D g2, Color borderColor) {
        Paint gradient;
        GeneralPath leftFold = new GeneralPath();
        leftFold.moveTo(x, y);
        leftFold.lineTo(x + width / 2.0f, y + height / 2.0f);
        leftFold.lineTo(x, y + height);
        leftFold.closePath();
        gradient = new GradientPaint(x, y,
                                     new Color(0xfffffe),
                                     x + width / 6.0f, y + height * 3.0f / 4.0f,
                                     new Color(0xffeaba));
        g2.setPaint(gradient);
        g2.fill(leftFold);
        g2.setColor(borderColor);
        g2.draw(leftFold);
    }

    private static void drawRightFold(float x, float y, float width, float height,
                                      Graphics2D g2, Color borderColor) {
        Paint gradient;
        GeneralPath leftFold = new GeneralPath();
        leftFold.moveTo(x + width, y);
        leftFold.lineTo(x + width / 2.0f, y + height / 2.0f);
        leftFold.lineTo(x + width, y + height);
        leftFold.closePath();
        gradient = new GradientPaint(x, y,
                                     new Color(0xfffffe),
                                     x + width / 6.0f, y + height * 3.0f / 4.0f,
                                     new Color(0xffeaba));
        g2.setPaint(gradient);
        g2.fill(leftFold);
        g2.setColor(borderColor);
        g2.draw(leftFold);
    }

    private static void drawBottomFold(float x, float y, float height, float width,
                                       Graphics2D g2, Color borderColor) {
        Paint gradient;
        GeneralPath bottomFold = new GeneralPath();
        bottomFold.moveTo(x, y + height);
        bottomFold.lineTo(x + width / 2.0f, y + height / 2.0f);
        bottomFold.lineTo(x + width, y + height);
        bottomFold.closePath();
        gradient = new GradientPaint(x + width / 4.0f, y + height * 3.0f / 4.0f,
                                     new Color(0xfffffe),
                                     x + width, y + height,
                                     new Color(0xffeaba));
        g2.setPaint(gradient);
        g2.fill(bottomFold);

        gradient = new GradientPaint(x, y + height / 2.0f,
                                     new Color(0x926057),
                                     x, y + height,
                                     borderColor);
        g2.setPaint(gradient);
        g2.draw(bottomFold);
    }

    private void drawMessage(int x, int y, int width, int height, Graphics2D g2) {
        Shape oldClip = g2.getClip();
        int clipHeight = (int) (folded * (float) height / 2.0f);
        if (folded > 0.0f) {
            g2.setClip(new Rectangle(x, y + clipHeight, width, height));
        }
        g2.drawImage(image, x, y, width, height, null);
        if (folded > 0.0f) {
            g2.setClip(oldClip);
            g2.setColor(new Color(0.0f, 0.0f, 0.0f, 0.3f));
            g2.fill(new RoundRectangle2D.Double(x - 2, y + clipHeight + 2, width + 4, clipHeight,
                                                4, 4));
            Paint oldPaint = g2.getPaint();
            g2.setPaint(new GradientPaint(0, y + clipHeight,
                                          new Color(0xf1f1f1/*0xffeaba*/),
                                          0, y + clipHeight + 6,
                                          Color.WHITE));
            g2.fillRect(x, y + clipHeight - 1, width, clipHeight + 1);
            g2.setPaint(oldPaint);
        }
    }

    private static void setupHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
    }
}
