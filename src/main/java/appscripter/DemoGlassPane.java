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

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;

/**
 *
 * @author sky
 */
public class DemoGlassPane extends WrappingGlassPane {
    private enum NavigationType {
        STOP, SHOW_CODE, NEXT
    };

    private final List<TranslucentLabel> messages;

    private boolean isNextVisible;

    private Component nextComponent;

    private ActionListener nextActionListener;

    private boolean isShowCodeVisible;

    private Component showCodeComponent;

    private ActionListener showCodeActionListener;

    private boolean isStopVisible;

    private Component stopComponent;

    private ActionListener stopActionListener;

    private float labelTranslucency;

    private float bgAlpha;

    private Image bgImage;


    public DemoGlassPane() {
        messages = new ArrayList<TranslucentLabel>();
        labelTranslucency = 1.0f;
        setStopVisible(false);
        setNextVisible(false);
        setShowCodeVisible(false);
    }

    public void setLabelTranslucency(float labelTranslucency) {
        if (labelTranslucency < 0f || labelTranslucency > 1f) {
            throw new IllegalArgumentException("Translucency must be between 0 and 1");
        }
        this.labelTranslucency = labelTranslucency;
        for (TranslucentLabel label : messages) {
            label.setAlpha(labelTranslucency);
        }
    }

    public float getLabelTranslucency() {
        return labelTranslucency;
    }

    public void setStopVisible(boolean stopVisible) {
        if (isStopVisible != stopVisible) {
            isStopVisible = stopVisible;
            if (stopVisible) {
                if (stopComponent == null) {
                    stopComponent = createDefaultStopComponent();
                    add(stopComponent);
                }
            } else {
                remove(stopComponent);
            }
        }
    }

    public boolean isStopVisible() {
        return isStopVisible;
    }

    public void addStopActionListener(ActionListener stopActionListener) {
        if (this.stopActionListener!= null) {
            throw new IllegalStateException("Only one listener is supported");
        }
        this.stopActionListener = stopActionListener;
    }

    public void setNextVisible(boolean nextVisible) {
        if (isNextVisible != nextVisible) {
            isNextVisible = nextVisible;
            if (nextVisible) {
                if (nextComponent == null) {
                    nextComponent = createDefaultNextComponent();
                    add(nextComponent);
                }
            } else {
                remove(nextComponent);
            }
        }
    }

    public boolean isNextVisible() {
        return isNextVisible;
    }

    public void addNextActionListener(ActionListener nextActionListener) {
        if (this.nextActionListener != null) {
            throw new IllegalStateException("Only one listener is supported");
        }
        this.nextActionListener = nextActionListener;
    }

    public void setShowCodeVisible(boolean showCodeVisible) {
        if (isShowCodeVisible != showCodeVisible) {
            isShowCodeVisible = showCodeVisible;
            if (showCodeVisible) {
                if (showCodeComponent == null) {
                    showCodeComponent = createDefaultShowCodeComponent();
                    add(showCodeComponent);
                }
            } else {
                remove(showCodeComponent);
            }
        }
    }

    public boolean isShowCodeVisible() {
        return isShowCodeVisible;
    }

    public void addShowCodeListener(ActionListener showCodeActionListener) {
        if (this.showCodeActionListener != null) {
            throw new IllegalStateException("Only one listener is supported");
        }
        this.showCodeActionListener = showCodeActionListener;
    }

    public void appendMessage(Font font, String message) {
        TranslucentLabel label = new TranslucentLabel(message);
        label.setForeground(Color.BLACK);
        label.setAlpha(getLabelTranslucency());
        label.setFont(font);
        messages.add(label);
        add(label);
        revalidate();
        repaint();
    }

    public void removeMessages() {
        for (JLabel label : messages) {
            remove(label);
        }
        messages.clear();
        repaint();
        revalidate();
    }

    public List<TranslucentLabel> getMessages() {
        List<TranslucentLabel> labels = new ArrayList<TranslucentLabel>(messages);
        return labels;
    }

    public void layout() {
        super.layout();
        layoutLabels();
        layoutNavigationButtons();
    }

    private void layoutNavigationButtons() {
        if (isNextVisible()) {
            Component next = nextComponent;
            Dimension pref = next.getPreferredSize();
            next.setBounds(getWidth() - getNavigationXPadding() - pref.width,
                    getHeight() - pref.height - getNavigationYPadding(),
                    pref.width, pref.height);
        }
        if (isShowCodeVisible()) {
            Component showCode = showCodeComponent;
            Dimension pref = showCode.getPreferredSize();
            int x = getNavigationXPadding();
            showCode.setBounds((getWidth() - pref.width) / 2,
                    getHeight() - pref.height - getNavigationYPadding(),
                    pref.width, pref.height);
        }
        if (isStopVisible()) {
            Component stop = stopComponent;
            Dimension pref = stop.getPreferredSize();
            stop.setBounds(getNavigationXPadding(),
                    getHeight() - pref.height - getNavigationYPadding(),
                    pref.width, pref.height);
        }
    }

    private void layoutLabels() {
        int width = getWidth();
        int y = getInitialY();
        for (TranslucentLabel label : messages) {
            Dimension pref = label.getPreferredSize();
            int x = (width - pref.width) / 2;
            label.setBounds(x, y, pref.width, pref.height);
            y += pref.height + getYSpacing(label);
        }
    }

    protected int getInitialY() {
        return 40;
    }

    private int getYSpacing(JLabel label) {
        return 10;
    }

    public void addNotify() {
        super.addNotify();
        //Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventHandler(), AWTEvent.MOUSE_EVENT_MASK);
    }

    public void removeNotify() {
        super.removeNotify();
        //Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventHandler(), AWTEvent.MOUSE_EVENT_MASK);
    }

    private Component createDefaultNextComponent() {
        return createNavigationComponent(NavigationType.NEXT, "Next");
    }

    private Component createDefaultShowCodeComponent() {
        return createNavigationComponent(NavigationType.SHOW_CODE, "Code...");
    }

    private Component createDefaultStopComponent() {
        return createNavigationComponent(NavigationType.STOP, "Stop");
    }

    private Component createNavigationComponent(NavigationType type, String text) {
        JLabel label = new TranslucentLabel(text);
        label.setFont(getDefaultNavigationFont());
        label.addMouseListener(new MouseHandler(type));
        return label;
    }

    protected Font getDefaultNavigationFont() {
        return new Font("Arial", Font.ITALIC, 18);
    }

    private void notifyListeners(NavigationType type) {
        ActionListener listener = null;
        switch(type) {
            case STOP:
                listener = stopActionListener;
                break;
            case NEXT:
                listener = nextActionListener;
                break;
            case SHOW_CODE:
                listener = showCodeActionListener;
                break;
        }
        if (listener!= null) {
            listener.actionPerformed(new ActionEvent(this, 0, null));
        }
    }

    private int getNavigationXPadding() {
        return 20;
    }

    private int getNavigationYPadding() {
        return 20;
    }

    public boolean isOptimizedDrawingEnabled() {
        return (getAltGlassPane() == null);
    }

    public void setBackgroundImage(Image image) {
        bgImage = image;
        repaint();
    }

    public Image getBackgroundImage() {
        return bgImage;
    }

    public void setBackgroundImageAlpha(float alpha) {
        bgAlpha = alpha;
        repaint();
    }

    public float getBackgroundImageAlpha() {
        return bgAlpha;
    }

    protected void paintComponent(Graphics g) {
        if (bgImage != null) {
            float alpha = getBackgroundImageAlpha();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            if (alpha != 1f) {
                ((Graphics2D)g).setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, alpha));
            }
            g.drawImage(bgImage, 0, 0, null);
            if (alpha != 1f) {
                ((Graphics2D)g).setComposite(AlphaComposite.
                        getInstance(AlphaComposite.SRC_OVER));
            }
        } else {
            super.paintComponent(g);
        }
    }


    private final class AWTEventHandler implements AWTEventListener {
        public void eventDispatched(AWTEvent event) {
            // PENDING: look for click on next/show
        }
    }


    private final class MouseHandler extends MouseAdapter {
        private final NavigationType type;

        MouseHandler(NavigationType type) {
            this.type = type;
        }

        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            notifyListeners(type);
        }
    }
}
