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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;
import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.TimingTarget;

/**
 *
 * @author sky
 */
public class QuotedPathPanel extends JPanel {
    private final List<String> paths;
    private List<String> targetPaths;
    private boolean transitioning;
    private int differsIndex;
    private boolean pathsChangedDuringTransition;
    private int transitionIndex;
    private boolean fadingIn;

    public QuotedPathPanel() {
        paths = new ArrayList<String>(1);
        setLayout(new Layout());
    }

    public void setQuotedPath(List<String> paths) {
        List<String> oldPaths = this.paths;
        if (paths == null) {
            paths = new ArrayList<String>(1);
        }
        targetPaths = new ArrayList<String>(paths);
        if (isTransitioning()) {
            pathsChangedDuringTransition = true;
        } else if (!oldPaths.equals(paths)) {
            startTransition();
        }
    }

    public List<String> getQuotedPath() {
        return new ArrayList<String>(paths);
    }

    private void startTransition() {
        transitioning = true;
        calcPathDiff();
        calcNextTransition();
    }

    private void calcPathDiff() {
        int oldSize = paths.size();
        int newSize = targetPaths.size();
        int differsAt = -1;
        int common = Math.min(oldSize, newSize);
        for (int i = 0; i < common; i++) {
            if (!paths.get(i).equals(targetPaths.get(i))) {
                differsAt = i;
                break;
            }
        }
        if (differsAt == -1 && newSize != oldSize) {
            differsAt = Math.min(newSize, oldSize);
        }
        assert (differsAt != -1);
        differsIndex = differsAt;
    }

    // find index that differs
    // remember index
    // if index < paths.size() -> fade element out
    // if index >= paths.size() -> add element, fading in
    // If paths change, reevaluate index at

    private void calcNextTransition() {
        if (pathsChangedDuringTransition) {
            pathsChangedDuringTransition = false;
            if (paths.equals(targetPaths)) {
                transitioning = false;
                return;
            }
            calcPathDiff();
        }
        if (differsIndex < paths.size()) {
            // Remove last element
            transitionIndex = paths.size() - 1;
            paths.remove(transitionIndex);
            fadingIn = false;
        } else {
            // Add new element
            differsIndex++;
            if (paths.size() == targetPaths.size()) {
                // DONE
                transitioning = false;
                return;
            }
            transitionIndex = paths.size();
            String path = targetPaths.get(transitionIndex);
            paths.add(path);
            QuoteLabel label = createLabel(transitionIndex, path);
            label.setAlpha(0f);
            add(label);
            fadingIn = true;
        }
        TimingController tc = new TimingController(
                new Cycle(250, 30),
                new Envelope(1, 0, Envelope.RepeatBehavior.FORWARD,
                Envelope.EndBehavior.HOLD));
        tc.addTarget(new TimingHandler());
        tc.start();
        revalidate();
        repaint();
    }

    private QuoteLabel createLabel(int index, String path) {
        QuoteLabel label = new QuoteLabel();
        if (path == null || "".equals(path)) {
            path = "?";
        }
        label.setText(path);
        ColorScheme scheme = ColorScheme.getScheme(index);
        label.setForeground(scheme.getOuterColor());
        label.setBackground(scheme.getInnerColor());
        return label;
    }

    private boolean isTransitioning() {
        return transitioning;
    }


    private class TimingHandler implements TimingTarget {
        @Override
        public void timingEvent(long l, long l0, float delta) {
            if (!fadingIn) {
                delta = 1f - delta;
            }
            ((QuoteLabel)getComponent(transitionIndex)).setAlpha(delta);
        }

        @Override
        public void begin() {
        }

        @Override
        public void end() {
            if (!fadingIn) {
                remove(getComponentCount() - 1);
                revalidate();
                repaint();
            }
            calcNextTransition();
        }
    }


    private static final int X_PAD = 4;


    private final class Layout implements LayoutManager2 {
        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
        }

        @Override
        public float getLayoutAlignmentX(Container target) {
            return 0f;
        }

        @Override
        public float getLayoutAlignmentY(Container target) {
            return 0f;
        }

        @Override
        public void invalidateLayout(Container target) {
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return calcSize();
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return calcSize();
        }

        @Override
        public Dimension maximumLayoutSize(Container target) {
            Dimension size = calcSize();
            size.width = Integer.MAX_VALUE;
            return size;
        }

        private Dimension calcSize() {
            if (getComponentCount() == 0) {
                QuoteLabel label = new QuoteLabel();
                label.setText("blah");
                return new Dimension(1, label.getPreferredSize().height);
            }
            int width = 0;
            int height = 0;
            for (Component c : getComponents()) {
                Dimension pref = c.getPreferredSize();
                width += pref.width;
                height = Math.max(pref.height, height);
            }
            width += X_PAD * (getComponentCount() - 1);
            return new Dimension(width, height);
        }

        @Override
        public void layoutContainer(Container parent) {
            int x = 0;
            for (Component c : getComponents()) {
                Dimension pref = c.getPreferredSize();
                c.setBounds(x, 0, pref.width, pref.height);
                x += pref.width + X_PAD;
            }
        }
    }
}
