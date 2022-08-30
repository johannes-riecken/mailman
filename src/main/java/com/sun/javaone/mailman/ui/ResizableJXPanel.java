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

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.TimingListener;
import org.jdesktop.animation.timing.TimingEvent;
import org.jdesktop.animation.timing.interpolation.PropertyRange;
import org.jdesktop.animation.timing.interpolation.ObjectModifier;

class ResizableJXPanel extends JXPanel {
    private boolean preferredSizeSet = false;
    private Dimension prefSize = null;

    @Override
    public void setVisible(boolean visible) {
        Object status = getClientProperty(SetFloatableAction.FLOATABLE_STATUS);
        if (status != null && status == Boolean.TRUE) {
            if (visible) {
                startFadeIn();
            } else {
                startFadeOut();
            }
        } else {
            if (visible) {
                setAlpha(1.0f);
            }
            super.setVisible(visible);
        }
    }

    private void startFadeOut() {
        setAlpha(1.0f);

        Cycle cycle = new Cycle(400, 12);
        Envelope envelope = new Envelope(1, 0, Envelope.RepeatBehavior.FORWARD,
                                         Envelope.EndBehavior.HOLD);
        PropertyRange range = PropertyRange.createPropertyRangeFloat("alpha", 1.0f, 0.0f);
        TimingController controller = new TimingController(cycle, envelope,
                                                           new ObjectModifier(this, range));
        controller.addTimingListener(new TimingListener() {
            @Override
            public void timerStarted(TimingEvent timingEvent) {
            }
@Override

            public void timerStopped(TimingEvent timingEvent) {
                ResizableJXPanel.super.setVisible(false);
            }

            @Override
            public void timerRepeated(TimingEvent timingEvent) {
            }
        });
        controller.setAcceleration(0.4f);
        controller.setDeceleration(0.2f);
        controller.start();
    }

    private void startFadeIn() {
        setAlpha(0.0f);
        super.setVisible(true);

        Cycle cycle = new Cycle(400, 12);
        Envelope envelope = new Envelope(1, 0, Envelope.RepeatBehavior.FORWARD,
                                         Envelope.EndBehavior.HOLD);
        PropertyRange range = PropertyRange.createPropertyRangeFloat("alpha", 0.0f, 1.0f);
        TimingController controller = new TimingController(cycle, envelope,
                                                           new ObjectModifier(this, range));
        controller.setAcceleration(0.4f);
        controller.setDeceleration(0.2f);
        controller.start();
    }

    @Override
    public Dimension getPreferredSize() {
        if (preferredSizeSet) {
            return prefSize;
        }
        return super.getPreferredSize();
    }

    @Override
    public void setPreferredSize(Dimension size) {
        preferredSizeSet = true;
        prefSize = size;
        super.setPreferredSize(size);
    }
}
