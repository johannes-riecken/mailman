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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.JLabel;

public class VerticalLabel extends JLabel {
    public final static int ROTATE_RIGHT = 1;
    public final static int DONT_ROTATE = 0;
    public final static int ROTATE_LEFT = -1;
    private int rotation = DONT_ROTATE;
    private boolean painting = false;

    public VerticalLabel() {
        super();
    }

    public VerticalLabel(Icon icon) {
        super(icon);
    }

    public VerticalLabel(String text) {
        super(text);
    }

    public VerticalLabel(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
    }

    public VerticalLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
    }

    public VerticalLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public boolean isRotated() {
        return rotation != DONT_ROTATE;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        if (isRotated()) {
            g2d.rotate(Math.toRadians(90 * rotation));
        }

        if (rotation == ROTATE_RIGHT) {
            g2d.translate(0, -this.getWidth());
        } else if (rotation == ROTATE_LEFT) {
            g2d.translate(-this.getHeight(), 0);
        }

        painting = true;
        super.paint(g2d);
        painting = false;

        if (isRotated()) {
            g2d.rotate(-Math.toRadians(90 * rotation));
        }

        if (rotation == ROTATE_RIGHT) {
            g2d.translate(-this.getWidth(), 0);
        } else if (rotation == ROTATE_LEFT) {
            g2d.translate(0, -this.getHeight());
        }
    }

    @Override
    public Insets getInsets(Insets insets) {
        insets = super.getInsets(insets);
        if (painting) {
            if (rotation == ROTATE_LEFT) {
                int temp = insets.bottom;
                insets.bottom = insets.left;
                insets.left = insets.top;
                insets.top = insets.right;
                insets.right = temp;
            } else if (rotation == ROTATE_RIGHT) {
                int temp = insets.bottom;
                insets.bottom = insets.right;
                insets.right = insets.top;
                insets.top = insets.left;
                insets.left = temp;
            }
        }
        return insets;
    }

    @Override
    public Insets getInsets() {
        Insets insets = super.getInsets();
        if (painting) {
            if (rotation == ROTATE_LEFT) {
                int temp = insets.bottom;
                insets.bottom = insets.left;
                insets.left = insets.top;
                insets.top = insets.right;
                insets.right = temp;
            } else if (rotation == ROTATE_RIGHT) {
                int temp = insets.bottom;
                insets.bottom = insets.right;
                insets.right = insets.top;
                insets.top = insets.left;
                insets.left = temp;
            }
        }
        return insets;
    }

    @Override
    public int getWidth() {
        if ((painting) && (isRotated())) {
            return super.getHeight();
        }
        return super.getWidth();
    }

    @Override
    public int getHeight() {
        if ((painting) && (isRotated())) {
            return super.getWidth();
        }
        return super.getHeight();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (isRotated()) {
            int width = d.width;
            d.width = d.height;
            d.height = width;
        }
        return d;
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        if (isRotated()) {
            int width = d.width;
            d.width = d.height;
            d.height = width;
        }
        return d;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        if (isRotated()) {
            int width = d.width;
            d.width = d.height;
            d.height = width;
        }
        return d;
    }
}
