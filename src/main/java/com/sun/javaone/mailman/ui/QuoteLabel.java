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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author sky
 */
public class QuoteLabel extends JLabel {
    private float alpha = 1f;

    public QuoteLabel() {
        setBorder(new EmptyBorder(2, 2, 2, 2));
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }

    public float getAlpha() {
        return alpha;
    }

    protected void paintComponent(Graphics g) {
        Composite oldC = null;
        if (alpha != 1f) {
            oldC = ((Graphics2D)g).getComposite();
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, alpha));
        }
        final int arcSize = 6;
        int w = getWidth();
        int h = getHeight();
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, w, h, arcSize, arcSize);
        g.setColor(getForeground());
        g.drawRoundRect(0, 0, w - 1, h - 1, arcSize, arcSize);
        g.setColor(getForeground());
        super.paintComponent(g);
        if (alpha != 1f) {
            ((Graphics2D)g).setComposite(oldC);
        }
    }
}
