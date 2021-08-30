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
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.SplitPaneUI;

/**
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class StealthSplitPane extends JSplitPane {
    public StealthSplitPane() {
        super();
        setEmptyDividerBorder();
    }

    public StealthSplitPane(int newOrientation) {
        super(newOrientation);
        setEmptyDividerBorder();
    }

    public StealthSplitPane(int newOrientation, boolean newContinuousLayout) {
        super(newOrientation, newContinuousLayout);
        setEmptyDividerBorder();
    }

    public StealthSplitPane(int newOrientation, Component newLeftComponent,
                            Component newRightComponent) {
        super(newOrientation, newLeftComponent, newRightComponent);
        setEmptyDividerBorder();
    }

    public StealthSplitPane(int newOrientation, boolean newContinuousLayout,
                            Component newLeftComponent, Component newRightComponent) {
        super(newOrientation, newContinuousLayout, newLeftComponent,
              newRightComponent);
        setEmptyDividerBorder();
    }

    private void setEmptyDividerBorder() {
        SplitPaneUI splitPaneUI = getUI();
        if (splitPaneUI instanceof BasicSplitPaneUI) {
            BasicSplitPaneUI basicUI = (BasicSplitPaneUI) splitPaneUI;
            basicUI.getDivider().setBorder(new EmptyBorder(0, 0, 0, 0));
        }
    }
}
