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

package appscripter;

import java.awt.Component;
import javax.swing.JComponent;

/**
 *
 * @author sky
 */
public class WrappingGlassPane extends JComponent {
    private Component altGlassPane;

    public WrappingGlassPane() {
    }

    // PENDING:
//    public final void setOpaque(boolean isOpaque) {
//        if (isOpaque) {
//            throw new IllegalArgumentException("DemoGlassPane can not be made opaque");
//        }
//    }
//
//    public final boolean isOpaque() {
//        return false;
//    }

    public void setAltGlassPane(Component altGlassPane) {
        if (this.altGlassPane != null) {
            super.remove(this.altGlassPane);
        }
        this.altGlassPane = altGlassPane;
        if (altGlassPane != null) {
            super.addImpl(altGlassPane, null, 0);
        }
        revalidate();
        repaint();
    }

    public Component getAltGlassPane() {
        return altGlassPane;
    }

    @Override
    public void remove(int index) {
        super.remove(index);
        checkAltGlassPane();
    }

    @Override
    public void removeAll() {
        super.removeAll();
        checkAltGlassPane();
    }

    private void checkAltGlassPane() {
        if (altGlassPane != null && altGlassPane.getParent() != null) {
            altGlassPane = null;
        }
    }

    @Override
    public void layout() {
        if (altGlassPane != null) {
            altGlassPane.setBounds(0, 0, getWidth(), getHeight());
        }
    }
}
