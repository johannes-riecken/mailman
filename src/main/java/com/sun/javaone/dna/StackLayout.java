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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

/**
 * <p><code>StackLayout</code> is a Swing layout aimed to act as the layers
 * stack of most popuplar graphics editing tools like <i>The GIMP</i> or
 * <i>Photoshop</i>. While similar to <code>CardLayout</code>, this layout
 * displays all the components of the container. If you are using non-rectangular
 * components (i.e. transparent) you will see them from top to bottom of the
 * stack.</p>
 * <p>When using this layout, each component can be added in the container
 * either on top of the stack or at the bootom:</p>
 * <pre>
 * JPanel panel = new JPanel(new StackLayout());
 * panel.add(new JLabel("On top"),    StackLayout.TOP);
 * panel.add(new JLabel("At bottom"), StackLayout.BOTTOM);
 * </pre>
 * If you don't specify the constraint, the component will be added at the top
 * of the components stack.</p>
 * <p>All the components managed by this layout will be given the same size as
 * the container itself. The minimum, maximum and preferred size of the
 * container are based upon the largest minimum, maximum and preferred size of
 * the children components.</p>
 * <p><code>StackLayout</code> works only with JSE 1.5 and Java SE 6 and
 * greater.</p>
 *
 * @author Romain Guy <romain.guy@mac.com>
 */

public class StackLayout implements LayoutManager2 {
    /** Use this constraint to add a component at the bottom of the stack. */
    public static final String BOTTOM = "bottom";
    /** Use this contrainst to add a component at the top of the stack. */
    public static final String TOP = "top";

    // removing components does not happen often compared to adding components
    // hence we choose a linked list to make insertion at the bottom faster
    private final List<Component> components = new LinkedList<Component>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLayoutComponent(final Component comp,
                                   final Object constraints) {
        synchronized (comp.getTreeLock()) {
            if (BOTTOM.equals(constraints)) {
                components.add(0, comp);
            } else if (TOP.equals(constraints)) {
                components.add(comp);
            } else {
                components.add(comp);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLayoutComponent(final String name, final Component comp) {
        addLayoutComponent(comp, TOP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLayoutComponent(final Component comp) {
        synchronized (comp.getTreeLock()) {
            components.remove(comp);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getLayoutAlignmentX(final Container target) {
        return 0.5f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getLayoutAlignmentY(final Container target) {
        return 0.5f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateLayout(final Container target) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension preferredLayoutSize(final Container parent) {
        synchronized (parent.getTreeLock()) {
            int width = 0;
            int height = 0;

            for (Component comp: components) {
                Dimension size = comp.getPreferredSize();
                width = Math.max(size.width, width);
                height = Math.max(size.height, height);
            }

            Insets insets = parent.getInsets();
            width += insets.left + insets.right;
            height += insets.top + insets.bottom;

            return new Dimension(width, height);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension minimumLayoutSize(final Container parent) {
        synchronized (parent.getTreeLock()) {
            int width = 0;
            int height = 0;

            for (Component comp: components) {
                Dimension size = comp.getMinimumSize();
                width = Math.max(size.width, width);
                height = Math.max(size.height, height);
            }

            Insets insets = parent.getInsets();
            width += insets.left + insets.right;
            height += insets.top + insets.bottom;

            return new Dimension(width, height);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension maximumLayoutSize(final Container target) {
        return new Dimension(Integer.MAX_VALUE,
                             Integer.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void layoutContainer(final Container parent) {
        synchronized (parent.getTreeLock()) {
            int width = parent.getWidth();
            int height = parent.getHeight();

            Rectangle bounds = new Rectangle(0, 0, width, height);

            int componentsCount = components.size();

            for (int i = 0; i < componentsCount; i++) {
                Component comp = components.get(i);
                comp.setBounds(bounds);
                parent.setComponentZOrder(comp, componentsCount - i - 1);
            }
        }
    }
}
