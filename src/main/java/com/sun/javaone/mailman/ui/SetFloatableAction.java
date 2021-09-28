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

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

class SetFloatableAction extends AbstractAction {
    //public static final String DOCKED_BORDER = "docked_border";
    public static final String FLOATABLE_STATUS = "floatable_status";

    private final JComponent component;
    private final ActionListener action;
    private final JComponent target;
    private final Object constraints;
    private final Box box;

    SetFloatableAction(JComponent component, ActionListener action,
                       JComponent target, Object constraints) {
        this.component = component;
        this.action = action;
        this.target = target;
        this.constraints = constraints;

        VerticalLabel showFloatable = new VerticalLabel(component.getName() + " ",
                                                        new ImageIcon(getClass().getResource("/resources/icons/get-mail_small.png")),
                                                        JLabel.TRAILING);
        showFloatable.setRotation(VerticalLabel.ROTATE_LEFT);
        showFloatable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showFloatable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SetFloatableAction.this.action.actionPerformed(
                    new ActionEvent(SetFloatableAction.this.component,
                                                   e.getID(), ""));
            }
        });
        box = Box.createVerticalBox();
        box.add(showFloatable);
    }

    public void actionPerformed(ActionEvent e) {
        JComponent parent = (JComponent) component.getParent();
        JRootPane rootPane = SwingUtilities.getRootPane(parent);
        JLayeredPane layeredPane = rootPane.getLayeredPane();

        Object status = component.getClientProperty(FLOATABLE_STATUS);
        if ((status == null || status == Boolean.FALSE) && isComponentVisible()) {
            Point location = component.getLocation();
            SwingUtilities.convertPointToScreen(location, parent);

            parent.remove(component);
            target.add(box, constraints);
            parent.revalidate();

            SwingUtilities.convertPointFromScreen(location, layeredPane);
            location.translate(-3, -3);
            component.setLocation(location);
            component.revalidate();

            layeredPane.add(component, JLayeredPane.PALETTE_LAYER, 10);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    component.putClientProperty(FLOATABLE_STATUS, Boolean.TRUE);
                }
            });
        } else {
            component.putClientProperty(FLOATABLE_STATUS, Boolean.FALSE);

            layeredPane.remove(component);
            layeredPane.revalidate();

            target.remove(box);
            target.add(component, constraints);
            if (!component.isVisible()) {
                component.setVisible(true);
            }
            target.revalidate();

            layeredPane.repaint();
        }
    }

    // work around for horizontal collapsible pane
    private boolean isComponentVisible() {
        Insets insets = component.getInsets();
        return component.isVisible() &&
               component.getWidth() > (insets.left + insets.right);
    }
}
