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

package com.sun.javaone.dna;

import binding.swing.SwingBindingSupport;
import com.sun.javaone.mailman.Application;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.interpolation.ObjectModifier;
import org.jdesktop.animation.timing.interpolation.PropertyRange;
import org.jdesktop.swingx.JXHyperlink;

public class DnaFrame extends JWindow {
    private TimingController waitController;
    private DnaPanel dnaPanel;
    private final Timer launchTimer;

    public DnaFrame() {
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(new StackLayout());

        ((JPanel) getContentPane()).setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(buildImagePanel());
        add(buildDnaPanel());

        setSize(350, 400);
        launchTimer = new Timer(3000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                launchApp(true);
            }
        });
        launchTimer.setRepeats(true);
        launchTimer.start();
    }

    private void skipDemo() {
        launchApp(false);
    }

    private void launchApp(boolean showDemo) {
        Application.getMainFrame().setVisible(true);
        dispose();
        launchTimer.stop();
        waitController.stop();
        if (showDemo) {
            Application.getMainFrame().runDemo();
        }
    }

    private Component buildDnaPanel() {
        dnaPanel = new DnaPanel();
        dnaPanel.setPreferredSize(new Dimension(350, 100));
        dnaPanel.setMinimumSize(new Dimension(350, 100));

        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };

        GridBagConstraints constraints;

        constraints = new GridBagConstraints(0, 0, 1, 1,
                                             0.0, 0.0,
                                             GridBagConstraints.CENTER,
                                             GridBagConstraints.NONE,
                                             new Insets(35, 0, 0, 0),
                                             0, 0);
        JLabel label = new JLabel(new ImageIcon(
                getClass().getResource("Title.png")));
        panel.add(label, constraints);

        constraints = new GridBagConstraints(0, 1, 1, 1,
                                             0.0, 1.0,
                                             GridBagConstraints.CENTER,
                                             GridBagConstraints.BOTH,
                                             new Insets(0, 0, 0, 0),
                                             0, 0);
        panel.add(Box.createVerticalGlue(), constraints);

        constraints = new GridBagConstraints(0, 2, 1, 1,
                                             0.0, 0.0,
                                             GridBagConstraints.CENTER,
                                             GridBagConstraints.NONE,
                                             new Insets(0, 0, 0, 0),
                                             0, 0);
        label = new JLabel(new ImageIcon(
                getClass().getResource("Load_Message.png")));
        panel.add(label, constraints);

        constraints = new GridBagConstraints(0, 3, 1, 1,
                                             1.0, 0.0,
                                             GridBagConstraints.CENTER,
                                             GridBagConstraints.HORIZONTAL,
                                             new Insets(0, 0, 0, 0),
                                             0, 0);
        panel.add(dnaPanel, constraints);

        constraints = new GridBagConstraints(0, 4, 1, 1,
                                             0.0, 0.0,
                                             GridBagConstraints.CENTER,
                                             GridBagConstraints.NONE,
                                             new Insets(0, 0, 0, 0),
                                             0, 0);
        panel.add(Box.createVerticalStrut(20), constraints);

        JXHyperlink skipHyperlink = new JXHyperlink();
        skipHyperlink.setText("Skip Demo");
        skipHyperlink.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                skipDemo();
            }
        });
        constraints = new GridBagConstraints(0, 5, 1, 1,
                                             0.0, 0.0,
                                             GridBagConstraints.EAST,
                                             GridBagConstraints.NONE,
                                             new Insets(0, 0, 5, 5),
                                             0, 0);
        panel.add(skipHyperlink, constraints);

        return panel;
    }

    private static Component buildImagePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
//        GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1,
//                                                                1.0, 1.0,
//                                                                GridBagConstraints.SOUTHEAST,
//                                                                GridBagConstraints.NONE,
//                                                                new Insets(0, 0, 0, 0),
//                                                                0, 0);
//        panel.add(new JLabel(new ImageIcon(getClass().getResource("Mail_Large.png"))),
//                  constraints);
        panel.setOpaque(false);
        return panel;
    }

    public void startWaitSequence() {
        if (waitController == null || !waitController.isRunning()) {
            Cycle cycle = new Cycle(4000, 12);
            Envelope envelope = new Envelope(TimingController.INFINITE,
                                             0, Envelope.RepeatBehavior.FORWARD,
                                             Envelope.EndBehavior.HOLD);
            PropertyRange range = PropertyRange.createPropertyRangeDouble("offset", 0.0, 1.0);
            waitController = new TimingController(cycle, envelope,
                                                  new ObjectModifier(dnaPanel, range));
            waitController.start();
        }
    }

    public void stopWaitSequence() {

    }

    public static class DnaPanel extends JPanel {
        private int spacing = 20;
        private int spotSize = 4;
        private int fadeLength = 100;
        private boolean fadeEdges = true;
        private boolean drawLines = true;
        private double offset = 0.0;

        private DnaPanel() {
        }

        @Override
        public boolean isOpaque() {
            return false;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            setupGraphics(g2);
            int height = getHeight() - spotSize * 4;
            for (int x = spacing; x < getWidth() - spacing; x += spacing) {
                double psi = x / (double) getWidth() + offset;
                double omega = psi * 2.0 * Math.PI;
                float alpha = 1.0f;

                if (fadeEdges) {
                    float newAlpha = 1.0f;
                    if (x < fadeLength) {
                        newAlpha = x / (float) fadeLength;
                    } else if (x > getWidth() - fadeLength) {
                        newAlpha = (getWidth() - x) / (float) fadeLength;
                    }
                    alpha *= newAlpha;
                }

                double y1 = Math.sin(omega) * height / 2.0 +
                            height / 2.0 + spotSize * 2;
                float alpha1 = (float) (alpha * (1 + Math.cos(omega)) / 2);
                alpha1 = 0.2f + 0.8f * alpha1;
                Color color1 = new Color(0.0f, 0.0f, 0.0f, alpha1);
                g2.setColor(color1);
                float spotSize1 = spotSize + spotSize * alpha1 / 3.0f;
                Ellipse2D spot1 = new Ellipse2D.Double(x - spotSize1, y1 - spotSize1,
                            spotSize1 * 2, spotSize1 * 2);
                g2.fill(spot1);

                double y2 = Math.sin(omega + Math.PI) * height / 2.0 +
                            height / 2.0 + spotSize * 2;
                float alpha2 = (float) (alpha * (1 - Math.cos(omega)) / 2);
                alpha2 = 0.2f + 0.8f * alpha2;
                Color color2 = new Color(0.0f, 0.0f, 0.0f, alpha2);
                g2.setColor(color2);
                float spotSize2 = spotSize + spotSize * alpha2 / 3.0f;
                Ellipse2D spot2 = new Ellipse2D.Double(x - spotSize2, y2 - spotSize2,
                            spotSize2 * 2, spotSize2 * 2);
                g2.fill(spot2);

                if (isDrawLines()) {
                    Paint paint = g2.getPaint();
                    g2.setPaint(new GradientPaint(x, (float) (y1 - spotSize1), color1,
                                                  x, (float) (y2 - spotSize2), color2));
                    alpha1 = (float) (alpha * (1 + Math.cos(omega)) / 2);
                    alpha2 = (float) (alpha * (1 - Math.cos(omega)) / 2);

                    GeneralPath path = new GeneralPath();
                    path.moveTo(x - 1.0f - alpha1, (float) y1);
                    path.lineTo(x + 1.0f + alpha1, (float) y1);
                    path.lineTo(x + 1.0f + alpha2, (float) y2);
                    path.lineTo(x - 1.0f - alpha2, (float) y2);
                    path.lineTo(x - 1.0f - alpha1, (float) y1);

                    Area area = new Area(path);
                    area.subtract(new Area(spot1));
                    area.subtract(new Area(spot2));
                    g2.fill(area);

                    g2.setPaint(paint);
                }
            }
        }

        private static void setupGraphics(Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
        }

        public boolean isDrawLines() {
            return drawLines;
        }

        public void setDrawLines(boolean drawLines) {
            this.drawLines = drawLines;
            repaint();
        }

        public double getOffset() {
            return offset;
        }

        public void setOffset(double offset) {
            this.offset = offset;
            repaint();
        }

        public int getSpacing() {
            return spacing;
        }

        public void setSpacing(int spacing) {
            this.spacing = spacing;
            repaint();
        }

        public int getSpotSize() {
            return spotSize;
        }

        public void setSpotSize(int spotSize) {
            this.spotSize = spotSize;
            repaint();
        }

        public int getFadeLength() {
            return fadeLength;
        }

        public void setFadeLength(int fadeLength) {
            this.fadeLength = fadeLength;
            repaint();
        }

        public boolean isFadeEdges() {
            return fadeEdges;
        }

        public void setFadeEdges(boolean fadeEdges) {
            this.fadeEdges = fadeEdges;
            repaint();
        }
    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SwingBindingSupport.register();
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (InstantiationException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                DnaFrame dnaFrame = new DnaFrame();
                dnaFrame.setLocationRelativeTo(null);
                dnaFrame.setVisible(true);
                dnaFrame.startWaitSequence();
            }
        });
    }
}
