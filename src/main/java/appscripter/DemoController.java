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
import java.awt.Component;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.RepaintManager;
import javax.swing.Timer;

/**
 *
 * @author sky
 */
public class DemoController {
    private static final int STATE_DIMMING_BACKGROUND = 0;
    private static final int STATE_ADDING_MESSAGES = STATE_DIMMING_BACKGROUND + 1;
    private static final int STATE_FADE_OUT_MESSAGE = STATE_ADDING_MESSAGES + 1;
    private static final int STATE_EXECUTING_SCRIPT = STATE_FADE_OUT_MESSAGE + 1;
    private static final int LAST_STATE = STATE_EXECUTING_SCRIPT + 1;

    private static final int PAUSE_TIME = 500;
    // Time to fade in messages
    private static final int FADE_IN_TIME = 500;
    // Tim to fade out messages
    private static final int FADE_OUT_TIME = 500;
    private static final int DIM_BACKGROUND_TIME = 500;

    private static final float MAX_DIM = .6f;

    private static char[] tmpChars = new char[256];

    private final JFrame frame;
    private final List<DemoStage> stages;
    private boolean running;
    private DemoGlassPane glassPane;
    private DemoStage currentStage;
    private Timer timer;
    private Incrementor incrementor;

    private int state;

    private boolean pausing;

    private int stageIndex;

    private Image bgImage;

    private final DemoExecutor executor;

    private int mouseX;
    private int mouseY;

    private boolean inScript;

    private boolean aborting;

    public static List<DemoStage> flowStages(List<DemoStage> stages, int width) {
        List<DemoStage> resultingStages = new ArrayList<DemoStage>(stages.size());
        for (DemoStage stage : stages) {
            resultingStages.add(flowStage(stage, width));
        }
        return resultingStages;
    }

    private static DemoStage flowStage(DemoStage stage, int width) {
        List<DemoMessage> messages = stage.getMessages();
        List<DemoMessage> newMessages = new ArrayList<DemoMessage>(messages.size());
        for (DemoMessage message : messages) {
            flowMessage(message, width, newMessages);
        }
        return new DemoStage(newMessages, stage.getCodeLocations(),
                stage.getScript(), stage.getPauseTime());
    }

    private static void flowMessage(DemoMessage message, int width, List<DemoMessage> messages) {
        String text = message.getMessage();
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(message.getFont());
        int textWidth = fm.stringWidth(text);
        if (textWidth > width) {
            int textLength = text.length();
            if (tmpChars.length < textLength) {
                tmpChars = new char[textLength];
            }
            text.getChars(0, textLength, tmpChars, 0);
            int lineStart = nextNonWhitespace(0, textLength);
            int lastChunkThatFits = lineStart;
            int lineIndex = 0;
            int index = lineStart;
            boolean tooLong = false;
            while (!tooLong && index < textLength) {
                index = nextWhitespace(index, textLength);
                int charWidth = fm.charsWidth(tmpChars, lineStart, index - lineStart);
                if (charWidth < width) {
                    lastChunkThatFits = index;
                    index = nextNonWhitespace(index, textLength);
                } else if (lastChunkThatFits == lineStart) {
                    tooLong = true;
                } else {
                    // charWidth > availableWidth, lastChunkThatFits != lineStart
                    messages.add(dupMessage(message, new String(
                            tmpChars, lineStart, lastChunkThatFits - lineStart)));
                    lineStart = nextNonWhitespace(lastChunkThatFits, textLength);
                    lastChunkThatFits = lineStart;
                }
            }
            if (!tooLong && lineStart != textLength) {
                messages.add(dupMessage(message, new String(
                        tmpChars, lineStart, textLength - lineStart)));
            }
        } else {
            messages.add(message);
        }
    }

    private static DemoMessage dupMessage(DemoMessage message, String text) {
        return new DemoMessage(text, message.getFont(), message.getDelay());
    }

    private static int nextWhitespace(int index, int length) {
        while (index < length && !Character.isWhitespace(tmpChars[index])) {
            index++;
        }
        return index;
    }

    private static int nextNonWhitespace(int index, int length) {
        while (index < length && Character.isWhitespace(tmpChars[index])) {
            tmpChars[index] = ' ';
            index++;
        }
        return index;
    }


    public DemoController(JFrame frame, List<DemoStage> stages) {
        this.stages = new ArrayList<DemoStage>(stages);
        this.frame = frame;
        this.executor = new DemoExecutor();
    }

    public void start() {
        start(0);
    }

    public void start(int index) {
        if (running) {
            throw new IllegalStateException("Already running");
        }
        if (index < 0 || index >= stages.size()) {
            throw new IllegalArgumentException("Index must be >= 0 && < stages.size()");
        }
        incrementor = null;
        installGlassPane();
        running = true;
        state = LAST_STATE - 1;
        stageIndex = index - 1;
        startTimer();
    }

    public void stop() {
        if (!running) {
            throw new IllegalStateException("Not running");
        }
        running = false;
        timer.stop();
        timer = null;
        uninstallGlassPane();
    }

    private void updateMouseLocation() {
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        mouseX = mouseLoc.x;
        mouseY = mouseLoc.y;
    }

    private boolean didMouseLocationChange() {
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        return (mouseX != mouseLoc.x || mouseY != mouseLoc.y);
    }

    private void tick() {
        if (incrementor == null) {
            // The first time through. Start things going.
            advanceState();
            updateMouseLocation();
        } else {
            boolean wasInScript = inScript;
            incrementor.tick();
            if (incrementor.isDone()) {
                if (inScript && executor.errored()) {
                    abort();
                } else if (aborting) {
                    // Done, stop
                    incrementor = null;
                } else if (!pausing) {
                    // Pause between every state.
                    pause();
                } else {
                    // We were pausing, advance the state.
                    advanceState();
                }
            }
            if (!aborting) {
                if (wasInScript != inScript) {
                    if (!inScript) {
                        updateMouseLocation();
                    }
                } else if (!inScript) {
                    if (didMouseLocationChange()) {
                        abort();
                    }
                }
            }
        }
        if (incrementor == null) {
            // If incrementor is null, we're done, stop.
            stop();
        }
    }

    private void abort() {
        aborting = true;
        Incrementor newIncrementor = null;
        switch(state) {
            case STATE_DIMMING_BACKGROUND:
                newIncrementor = new FadeBackgroundInIncrementor();
                break;
            case STATE_ADDING_MESSAGES:
                newIncrementor = new FadeOutIncrementor(true);
                break;
            case STATE_FADE_OUT_MESSAGE:
                newIncrementor = incrementor;
                break;
            case STATE_EXECUTING_SCRIPT:
                break;
        }
        if (incrementor != null && newIncrementor != incrementor) {
            incrementor.stop();
        }
        incrementor = newIncrementor;
        aborting = true;
    }

    private void advanceState() {
        state = (state + 1) % LAST_STATE;
        if (state == STATE_DIMMING_BACKGROUND &&
                glassPane.getBackgroundImage() != null) {
            state = (state + 1) % LAST_STATE;
        }
        incrementor = null;
        pausing = false;
        inScript = false;
        switch(state) {
            case STATE_DIMMING_BACKGROUND:
                if (glassPane.getBackgroundImage() == null) {
                    updateBackgroundImage();
                }
                incrementor = new FadeBackgroundIncrementor();
                break;
            case STATE_ADDING_MESSAGES:
                advanceStage();
                break;
            case STATE_FADE_OUT_MESSAGE:
                boolean fadeBackground = (currentStage.getScript() != null ||
                        stageIndex + 1 == stages.size());
                incrementor = new FadeOutIncrementor(fadeBackground,
                        currentStage.getPauseTime());
                break;
            case STATE_EXECUTING_SCRIPT:
                if (currentStage.getScript() != null) {
                    inScript = true;
                    ((DemoRootPane)glassPane.getParent()).setPaintChildren(true);
                    glassPane.setBackgroundImage(null);
                    executor.execute(glassPane, currentStage.getScript());
                    incrementor = new ScriptIncrementor();
                }
                break;
        }
        if (currentStage != null && incrementor == null) {
            pause();
        }
    }

    private void pause() {
        pausing = true;
        incrementor = new PauseIncrementor();
    }

    private void advanceStage() {
        incrementor = null;
        currentStage = nextStage();
        if (currentStage != null) {
            glassPane.removeMessages();
            glassPane.setLabelTranslucency(1f);
            List<DemoMessage> messages = currentStage.getMessages();
            incrementor = new MessageIncrementor(messages);
//            for (DemoMessage message : messages) {
//                if (message.getDelay() == 0) {
//                    appendMessage(message);
//                } else {
//                    incrementor = new MessageIncrementor(messages);
//                    break;
//                }
//            }
        }
    }

    private void appendMessage(DemoMessage message) {
        glassPane.appendMessage(message.getFont(), message.getMessage());
    }

    private DemoStage nextStage() {
        if (++stageIndex == stages.size()) {
            return null;
        }
        return stages.get(stageIndex);
    }

    private void startTimer() {
        timer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });
        timer.setRepeats(true);
        timer.start();
    }

    private void uninstallGlassPane() {
        Exception exception = null;
        glassPane.setVisible(false);
        try {
            Method setRootPaneMethod = JFrame.class.getDeclaredMethod("setRootPane", JRootPane.class);
            setRootPaneMethod.setAccessible(true);

            JRootPane newRoot = new JRootPane();
            newRoot.setContentPane(frame.getContentPane());
            newRoot.setJMenuBar(frame.getJMenuBar());
            if (glassPane.getAltGlassPane() != null) {
                newRoot.setGlassPane(glassPane.getAltGlassPane());
            }

            setRootPaneMethod.invoke(frame, newRoot);
            frame.validate();
        } catch (IllegalArgumentException ex) {
            exception = ex;
        } catch (IllegalAccessException ex) {
            exception = ex;
        } catch (InvocationTargetException ex) {
            exception = ex;
        } catch (SecurityException ex) {
            exception = ex;
        } catch (NoSuchMethodException ex) {
            exception = ex;
        }
        if (exception != null) {
            throw new RuntimeException("Error installing root pane", exception);
        }
        glassPane = null;
    }

    private void installGlassPane() {
        Exception exception = null;
        DemoRootPane rootPane = new DemoRootPane();
        rootPane.setContentPane(frame.getContentPane());
        rootPane.setJMenuBar(frame.getJMenuBar());
        glassPane = createDemoGlassPane();
        rootPane.setGlassPane(glassPane);
        glassPane.setAltGlassPane(frame.getGlassPane());
        try {
            Method setRootPaneMethod = JFrame.class.getDeclaredMethod("setRootPane", JRootPane.class);
            setRootPaneMethod.setAccessible(true);
            setRootPaneMethod.invoke(frame, rootPane);
            frame.validate();
        } catch (IllegalArgumentException ex) {
            exception = ex;
        } catch (IllegalAccessException ex) {
            exception = ex;
        } catch (InvocationTargetException ex) {
            exception = ex;
        } catch (SecurityException ex) {
            exception = ex;
        } catch (NoSuchMethodException ex) {
            exception = ex;
        }
        if (exception != null) {
            throw new RuntimeException("Error installing root pane", exception);
        }
        glassPane.setVisible(true);
        Container parent = glassPane.getParent();
        while (parent != null) {
            parent = parent.getParent();
        }
    }

    private DemoGlassPane createDemoGlassPane() {
        return new DemoGlassPane();
    }

    private void updateBackgroundImage() {
        Container rp = glassPane.getParent();
        Rectangle rpBounds = rp.getBounds();
        int w = glassPane.getWidth();
        int h = glassPane.getHeight();
        if (bgImage == null || bgImage.getWidth(null) != w ||
                bgImage.getHeight(null) != h) {
            bgImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            // bgImage = glassPane.createImage(w, h);
        }
        // Unfortunately this is the only 'safe' way to turn off
        // double buffering. Doubley unfortunate is that it turns off
        // buffer-per-window.
        // PENDING: perhaps this would be best to just turn off
        // double buffering on all components.
        RepaintManager rm = RepaintManager.currentManager(glassPane);
        rm.setDoubleBufferingEnabled(false);
        Graphics g = bgImage.getGraphics();
        rp.paint(g);
        g.dispose();
        rm.setDoubleBufferingEnabled(true);
        ((DemoRootPane)glassPane.getParent()).setPaintChildren(false);
        glassPane.setBackgroundImage(bgImage);
        glassPane.setBackgroundImageAlpha(1f);
    }


    private static abstract class Incrementor {
        protected long start;

        Incrementor() {
            resetTime();
        }

        public abstract void tick();

        public abstract boolean isDone();

        protected float getPercent(int time) {
            return Math.min(1f, (float)(System.currentTimeMillis() - start) / (float)time);
        }

        protected void resetTime() {
            start = System.currentTimeMillis();
        }

        public void stop() {
        }
    }

    private final class MessageIncrementor extends Incrementor {
        private final List<DemoMessage> messages;
        private int index;
        private int componentIndex;
        private boolean pausing;

        MessageIncrementor(List<DemoMessage> messages) {
            this.messages = messages;
            index = 0;
//            index = -1;
//            for (int i = 0; i < messages.size(); i++) {
//                if (messages.get(i).getDelay() != 0) {
//                    index = i;
//                    break;
//                }
//            }
            assert (index != -1);
            addNextMessage();
        }

        public void tick() {
            if (pausing) {
                if (getPercent(PAUSE_TIME) >= 1f) {
                    pausing = false;
                    resetTime();
                }
            } else {
                float percent = getPercent(FADE_IN_TIME);//messages.get(index).getDelay()));
                ((TranslucentLabel)glassPane.getMessages().get(componentIndex)).setAlpha(percent);
                if (percent >= 1 && ++index < messages.size()) {
                    addNextMessage();
                    pausing = true;
                }
            }
        }

        public boolean isDone() {
            return (index == messages.size());
        }

        private void addNextMessage() {
            appendMessage(messages.get(index));
            componentIndex = glassPane.getMessages().size() - 1;
            ((TranslucentLabel)glassPane.getMessages().get(componentIndex)).setAlpha(0);
            resetTime();
        }

    }


    private static final class PauseIncrementor extends Incrementor {
        public void tick() {
        }

        public boolean isDone() {
            return getPercent(PAUSE_TIME) >= 1f;
        }
    }


    private final class FadeOutIncrementor extends Incrementor {
        private final boolean fadeBackgroundImage;
        private final int pauseTime;
        private boolean pausing;

        FadeOutIncrementor(boolean fadeBackgroundImage) {
            this(fadeBackgroundImage, 0);
        }

        FadeOutIncrementor(boolean fadeBackgroundImage, int pauseTime) {
            this.fadeBackgroundImage = fadeBackgroundImage;
            this.pauseTime = 1000;//pauseTime;
            pausing = (this.pauseTime > 0);
        }

        public void tick() {
            if (pausing && getPercent(pauseTime) >= 1f) {
                pausing = false;
                resetTime();
            }
            if (!pausing) {
                glassPane.setLabelTranslucency(1f - getPercent(FADE_OUT_TIME));
                if (fadeBackgroundImage) {
                    glassPane.setBackgroundImageAlpha(1f - MAX_DIM + getPercent(FADE_OUT_TIME) * MAX_DIM);
                }
            }
        }

        public boolean isDone() {
            return !pausing && getPercent(FADE_OUT_TIME) >= 1f;
        }
    }


    private final class FadeBackgroundIncrementor extends Incrementor {
        public void tick() {
            glassPane.setBackgroundImageAlpha(1f - getPercent(DIM_BACKGROUND_TIME) * MAX_DIM);
        }

        public boolean isDone() {
            return getPercent(DIM_BACKGROUND_TIME) >= 1f;
        }
    }


    private final class FadeBackgroundInIncrementor extends Incrementor {
        private final int time;
        private final float startAlpha;
        FadeBackgroundInIncrementor() {
            startAlpha = glassPane.getBackgroundImageAlpha();
            float delta = 1f - startAlpha;
            time = (int)(delta / MAX_DIM * DIM_BACKGROUND_TIME);
        }
        public void tick() {
            glassPane.setBackgroundImageAlpha(startAlpha + getPercent(time) * (1f - startAlpha));
        }

        public boolean isDone() {
            return getPercent(time) >= 1f;
        }
    }


    private final class ScriptIncrementor extends Incrementor {
        public void tick() {
        }

        public boolean isDone() {
            return executor.isDone();
        }

        // PENDING: override stop
    }
}
