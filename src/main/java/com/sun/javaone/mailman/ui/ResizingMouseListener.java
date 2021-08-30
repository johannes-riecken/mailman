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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

class ResizingMouseListener extends MouseAdapter {
    private final int side;
    private final int threshold;
    private Point startPoint;

    // assume side == SwingConstants.EAST
    ResizingMouseListener(int side, int threshold) {
        this.side = side;
        this.threshold = threshold;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        JComponent source = (JComponent) e.getSource();
        source.setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point point = e.getPoint();
        int distance = point.x - startPoint.x;

        JComponent component = (JComponent) e.getSource();
        component.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));

        Dimension size = component.getSize();
        Dimension newSize = new Dimension(size.width + distance,
                                          size.height);
//        Dimension minimumSize = component.getMinimumSize();
//        if (newSize.width < minimumSize.width ||
//            newSize.height < minimumSize.height) {
//            return;
//        }

        component.setPreferredSize(newSize);
        component.setSize(newSize);
        ((JComponent) component.getParent()).revalidate();

        startPoint = point;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        JComponent source = (JComponent) e.getSource();
        int cursor = Cursor.DEFAULT_CURSOR;

        if (side == SwingConstants.EAST) {
            double x = e.getPoint().getX();
            if (x > source.getWidth() - threshold) {
                cursor = Cursor.E_RESIZE_CURSOR;
            }
        }

        source.setCursor(Cursor.getPredefinedCursor(cursor));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        JComponent source = (JComponent) e.getSource();
        source.setCursor(Cursor.getDefaultCursor());
    }
}
