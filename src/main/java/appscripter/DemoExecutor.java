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
import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.AWTEventListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

/**
 *
 * @author sky
 */
public class DemoExecutor {
    private static final String ARG_KEY_CODE = "keyCode";
        
    private static final String ARG_PAUSE = "pause";
    
    private static final String ARG_DELTA_X = "deltaX";

    private static final String ARG_DELTA_Y = "deltaY";

    private static final String ARG_COMPONENT_NAME = "componentName";
    
    private static final String ARG_FIND_EMPTY_REGION = "findEmptyRegion";
    
    private static final String ARG_INDEX = "index";
    
    private static final String ARG_TIME = "time";
    
    private static final String ARG_CLASS_NAME = "class";

    private static final int GLIDE_PAUSE_TIME = 10;

    private static final int PAUSE_TIME = 500;

    private static final long TIME_OUT_THRESHOLD = 3000;
    
    private static final String ON_EDT_METHOD_NAME = "onEDT";

    private static final String IN_BACKGROUND_METHOD_NAME = "offEDT";

    private static final int MOUSE_MOVE_SIZE = 10;

    private DemoGlassPane gp;
    private List<DemoCommand> commands;
    private Robot robot;
    private boolean[] hitRegion;

    private DemoExecutor.EventConditional conditional;
    
    private AWTEventHandler eventHandler;
    
    private volatile boolean running;

    private boolean isMouseDown;
    
    private int mouseX;
    
    private int mouseY;
    
    private boolean errored;
    
    private boolean initedDND;

    
    public DemoExecutor() {
    }


    public final void execute(DemoGlassPane gp, Object script) {
        eventHandler = new AWTEventHandler();
        DragSource.getDefaultDragSource().addDragSourceMotionListener(eventHandler);
        DragSource.getDefaultDragSource().addDragSourceListener(eventHandler);
        Toolkit.getDefaultToolkit().addAWTEventListener(
                eventHandler, AWTEvent.FOCUS_EVENT_MASK | AWTEvent.KEY_EVENT_MASK |
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK |
                AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        this.gp = gp;
        commands = (List<DemoCommand>)script;
        running = true;
        errored = false;
        new Thread(new Runner()).start();
    }
    
    public boolean errored() {
        return errored;
    }

    protected boolean isDone() {
        // PENDING:
        return !running;
    }
    
    private void updateMouseLocation() {
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        updateMouseLocation(mouseLoc.x, mouseLoc.y);
    }
    
    private void updateMouseLocation(int x, int y) {
        mouseX = x;
        mouseY = y;
    }

    private boolean didMouseLocationChange() {
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        return (mouseX != mouseLoc.x || mouseY != mouseLoc.y);
    }

    protected void executeInBackground() {
        try {
            for (DemoCommand command : commands) {
                executeInBackground(command);
            }
        } catch (Exception e) {
            errored = true;
            System.err.println("Problem executing script, stopping: " + e);
            e.printStackTrace();
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(eventHandler);
        }
        running = false;
    }
    
    protected void executeInBackground(DemoCommand command) throws Exception {
        final Object executor = getExecutor(command);
        HashSet<Method> allMethods = new HashSet<Method>();
        allMethods.addAll(Arrays.asList(executor.getClass().getDeclaredMethods()));
        List<Method> methods = new ArrayList<Method>();
        for (Method method : allMethods) {
            if (method.getName().startsWith(ON_EDT_METHOD_NAME)) {
                methods.add(method);
            }
            else if (method.getName().startsWith(IN_BACKGROUND_METHOD_NAME)) {
                methods.add(method);
            }
        }
        Comparator<Method> comparator = new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                int index1 = getIndex(m1);
                int index2 = getIndex(m2);
                return index1 - index2;
            }
            private int getIndex(Method m) {
                String name = m.getName();
                String indexAsString;
                if (name.startsWith(ON_EDT_METHOD_NAME)) {
                    indexAsString = name.substring(
                            ON_EDT_METHOD_NAME.length());
                }
                else {
                    indexAsString = name.substring(
                            IN_BACKGROUND_METHOD_NAME.length());
                }
                if (indexAsString.length() == 0) {
                    throw new IllegalStateException(executor.getClass() +
                            " onEDT and offEDT must be " +
                            "followed by an integer specifying " +
                            "order.");
                }
                return Integer.parseInt(indexAsString);
            }
        };
        Collections.sort(methods, comparator);
        for (Method method : methods) {
            try {
                method.setAccessible(true);
                if (method.getName().startsWith(ON_EDT_METHOD_NAME)) {
                    SwingUtilities.invokeAndWait(new MethodRunnable(executor, method));
                } else {
                    method.invoke(executor);
                }
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Error executing", ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException("Error executing", ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Error executing", ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException("Error executing", ex);
            }
        }
    }
    
    private Object getExecutor(DemoCommand command) throws Exception {
        switch(command.getCommand()) {
            case SELECT_POPUP_ITEM:
                return new SelectPopupItemExecutor(command);
            case MOVE_MOUSE:
                return new MouseMoveExecutor(command);
            case CLICK_MOUSE:
                return new MouseClickExecutor(command);
            case DRAG_MOUSE:
                return new MouseDragExecutor(command);
            case PAUSE:
                return new PauseExecutor(command);
            case PRESS_MOUSE:
                return new MousePressExecutor(command);
            case RELEASE_MOUSE:
                return new MouseReleaseExecutor(command);
            case TYPE_KEY:
                return new KeyTypeExecutor(command);
            case EXTERNAL:
                return createExternalExecutor(command);
        }
        throw new IllegalArgumentException("Unable to find executor for: " + 
                command);
    }
    
    private Object createExternalExecutor(DemoCommand command) throws Exception {
        String className = (String) command.getArguments().get(ARG_CLASS_NAME);
        Class type = Class.forName(className);
        return type.getConstructor(ExecuteContext.class).newInstance(new ExecuteContext(command));
    }
    
    private EventConditional createConditional(int id, Object...args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Must supply an even number of args");
        }
        for (int i = 0; i < args.length; i += 2) {
            if (!String.class.isInstance(args[i])) {
                throw new IllegalArgumentException(
                        "Even number object must be a String, was " + args[i]);
            }
        }
        return new DefaultEventConditional(id, args);
    }
    
    private void moveRobotTo(int x, int y) {
        getRobot().mouseMove(x, y);
        updateMouseLocation(x, y);
    }
    
    private void moveMouseSmoothlyTo(int x, int y) {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Must be invoked in background");
        }
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        int currentX = mouseLoc.x;
        int currentY = mouseLoc.y;
        if (currentX != x || currentY != y) {
            int maxDelta = Math.max(Math.abs(currentX - x),
                    Math.abs(currentY - y));
            float dx = (x - currentX) / (float)maxDelta;
            float dy = (y - currentY) / (float)maxDelta;
            int type = isMouseDown ? MouseEvent.MOUSE_DRAGGED : MouseEvent.MOUSE_MOVED;
            boolean pauseAfterFirst = false;
            if (isMouseDown && !initedDND) {
                initedDND = true;
                pauseAfterFirst = true;
            }
            scheduleConditional(createConditional(
                    type, "getXOnScreen", x, "getYOnScreen", y));
            for (int i = MOUSE_MOVE_SIZE; i < maxDelta; i += MOUSE_MOVE_SIZE) {
                pause(GLIDE_PAUSE_TIME);
                moveRobotTo((int)(currentX + dx * i),
                        (int)(currentY + dy * i));
                if (pauseAfterFirst && i == MOUSE_MOVE_SIZE) {
                    // This is ugly. Initing DnD can take a while. To make sure
                    // dnd is safely inited, this pauses for a bit to make sure
                    // dnd is loaded before using.
                    pause(1000);
                }
            }
            moveRobotTo(x, y);
            waitForConditional();
        }
    }
    
    private void moveMouse(int x, int y) {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Must be invoked in background");
        }
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        int currentX = mouseLoc.x;
        int currentY = mouseLoc.y;
        if (currentX != x || currentY != y) {
            int type = isMouseDown ? MouseEvent.MOUSE_DRAGGED : MouseEvent.MOUSE_MOVED;
            scheduleConditional(createConditional(
                    type, "getXOnScreen", x, "getYOnScreen", y));
            moveRobotTo(x, y);
            waitForConditional();
        }
    }
    
    private void mousePress(int robotMask, int mouseMask) {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Must be invoked in background");
        }
        if (isMouseDown) {
            throw new IllegalStateException("Mouse already down");
        }
        isMouseDown = true;
        scheduleConditional(createConditional(MouseEvent.MOUSE_PRESSED, 
                "getButton", mouseMask));
        getRobot().mousePress(robotMask);
        waitForConditional();
    }
    
    private void mouseRelease(int robotMask, int mouseMask) {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Must be invoked in background");
        }
        if (!isMouseDown) {
            throw new IllegalStateException("Mouse button is not down");
        }
        isMouseDown = false;
        scheduleConditional(createConditional(MouseEvent.MOUSE_RELEASED, 
                "getButton", mouseMask));
        getRobot().mouseRelease(robotMask);
        waitForConditional();
    }
    
    private void flushEventQueue() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            }
        });
    }
    
    private void pressKey(int keyCode) {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Must be invoked in background");
        }
        // Waiting for a press event was problematic with DND. For that
        // reason this doesn't wait.
        getRobot().keyPress(keyCode);
        waitForConditional();
    }
    
    private void releaseKey(int keyCode) {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Must be invoked in background");
        }
        scheduleConditional(createConditional(KeyEvent.KEY_RELEASED, 
                "getKeyCode", keyCode));
        getRobot().keyRelease(keyCode);
        waitForConditional();
    }
    
    private void scheduleConditional(EventConditional e) {
        synchronized(this) {
            if (conditional != null) {
                throw new IllegalStateException("Conditional is non-null");
            }
            conditional = e;
        }
    }
    
    private void waitForConditional() {
        long start = System.currentTimeMillis();
        synchronized(this) {
            while (conditional != null &&
                    System.currentTimeMillis() - start < TIME_OUT_THRESHOLD) {
                try {
                    wait(100);
                } catch (InterruptedException ex) {
                }
            }
            if (conditional != null) {
                throw new IllegalStateException("Did not receive event; failing");
            }
        }
        flushEventQueue();
        pause(PAUSE_TIME);
        flushEventQueue();
        if (didMouseLocationChange()) {
            throw new RuntimeException("Unexpected mouse location, stopping");
        }
    }

    private void pause(int length) {
        try {
            Thread.sleep(length);
        } catch (InterruptedException ex) {
        }
    }

    private void awtEventDispatched(Object event) {
        EventConditional conditional;
        synchronized(this) {
            conditional = this.conditional;
        }
        boolean matched = false;
        if (conditional != null && conditional.matchesEvent(event)) {
            synchronized(this) {
                if (this.conditional == conditional) {
                    this.conditional = null;
                    notifyAll();
                    matched = true;
                }
            }
        }
    }
    
    protected Robot getRobot() {
        if (robot == null) {
            try {
                robot = new Robot();
                robot.setAutoDelay(0);
                robot.setAutoWaitForIdle(false);
            } catch (AWTException ex) {
            }
        }
        return robot;
    }
    
    private Point getLocation(final DemoCommand command) {
        if (!SwingUtilities.isEventDispatchThread()) {
            final Point[] loc = new Point[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        loc[0] = getLocation0(command);
                    }
                });
            } catch (InvocationTargetException ex) {
                throw new RuntimeException("Exception invoking ", ex);
            } catch (InterruptedException ex) {
                throw new RuntimeException("Exception invoking ", ex);
            }
            return loc[0];
        } else {
            return getLocation0(command);
        }
    }
    
    private Point getLocation0(DemoCommand command) {
        Object dx = command.getArguments().get(ARG_DELTA_X);
        Object dy = command.getArguments().get(ARG_DELTA_Y);
        if (dx != null || dy != null) {
            Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
            if (dx != null) {
                mouseLoc.x += Integer.parseInt((String) dx);
            }
            if (dy != null) {
                mouseLoc.y += Integer.parseInt((String) dy);
            }
            return mouseLoc;
        }
        String cName = (String) command.getArguments().get(ARG_COMPONENT_NAME);
        if (cName == null) {
            // Use current location
            return MouseInfo.getPointerInfo().getLocation();
        }
        Component c = findComponentByName(cName);
        if (c != null) {
            // PENDING: handle row
            Rectangle bounds;
            if (c instanceof JComponent) {
                bounds = ((JComponent)c).getVisibleRect();
            } else {
                bounds = c.getBounds();
            }
            int x = bounds.x;
            int y = bounds.y;
            int h = bounds.height;
            int w = bounds.width;
            if ("true".equals(command.getArguments().
                    get(ARG_FIND_EMPTY_REGION))) {
                Point empty = findEmptyRegion(c, bounds);
                x = c.getX() + empty.x;
                y = c.getY() + empty.y;
            } else if (c instanceof JList) {
                int index = Integer.parseInt((String) command.getArguments().get(ARG_INDEX));
                Rectangle cellBounds = ((JList)c).getCellBounds(index, index);
                x = cellBounds.x + w / 2;
                y = cellBounds.y + cellBounds.height / 2;
            } else if (c instanceof JTable) {
                int index = Integer.parseInt((String) command.getArguments().get(ARG_INDEX));
                Rectangle cellBounds = ((JTable)c).getCellRect(index, 0, true);
                x = cellBounds.x + w / 2;
                y = cellBounds.y + cellBounds.height / 2;
            } else if (c instanceof JTree) {
                int index = Integer.parseInt((String) command.getArguments().get(ARG_INDEX));
                Rectangle cellBounds = ((JTree)c).getRowBounds(index);
                x = cellBounds.x + cellBounds.width / 2;
                y = cellBounds.y + cellBounds.height / 2;
            } else {
                x = bounds.x + bounds.width / 2;
                y = bounds.y + bounds.height / 2;
            }
            Point screenLoc = c.getLocationOnScreen();
            return new Point(x + screenLoc.x, y + screenLoc.y);
        } else {
            throw new IllegalStateException("Unable to locate component named " + cName);
        }
    }
    
    private Point findEmptyRegion(Component c, Rectangle bounds) {
        if (c instanceof Container) {
            int x = bounds.x;
            int y = bounds.y;
            int w = bounds.width;
            int h = bounds.height;
            if (hitRegion == null || hitRegion.length < w * h) {
                hitRegion = new boolean[w * h];
            }
            for (int i = w * h - 1; i >= 0; i--) {
                hitRegion[i] = false;
            }
            int maxX = x + w;
            int maxY = y + h;
            // NOTE: this is extremely inefficient.
            for (Component child : ((Container)c).getComponents()) {
                int childX = child.getX();
                int childY = child.getY();
                int childMaxX = childX + child.getWidth();
                int childMaxY = childY + child.getHeight();
                childX = Math.max(x, Math.min(childX, maxX));
                childY = Math.max(y, Math.min(childY, maxY));
                childMaxX = Math.max(x, Math.min(childMaxX, maxX));
                childMaxY = Math.max(y, Math.min(childMaxY, maxY));
                if (childX != childMaxX && childY != childMaxY) {
                    for (int i = childY; i < childMaxY; i++) {
                        int offset = (i - y) * w + (childX - x);
                        for (int j = childX; j < childMaxX; j++, offset++) {
                            hitRegion[offset] = true;
                        }
                    }
                }
            }
            int freeX = 0;
            int freeY = 0;
            for (Component child : ((Container)c).getComponents()) {
                freeX = Math.max(child.getX(), freeX);
                freeY = Math.max(child.getY(), freeY);
            }
            if (freeX < maxX) {
                int testX = freeX + (maxX - freeX) / 2;
                int testY = y + (maxY - y) / 2;
                if (!hitRegion[(testY - y) * w + testX - x]) {
                    return new Point(testX, testY);
                }
            }
            
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    if (!hitRegion[i * w + j]) {
                        return new Point(x + j, y + i);
                    }
                }
            }
            // All points are taken
            throw new IllegalStateException("Unable to find empty region");
        } else {
            return new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        }
    }
    
    private Component findComponentByName(String name) {
        Component result = findComponentByName(gp.getParent(), name);
        if (result == null) {
            for (Frame frame : Frame.getFrames()) {
                if (frame.isActive()) {
                    if (SwingUtilities.getWindowAncestor(gp) != frame) {
                        return findComponentByName(frame, name);
                    }
                    return null;
                }
            }
        }
        return result;
    }

    private Component findComponentByName(Component component, String name) {
        if (name.equals(component.getName())) {
            return component;
        }
        if (component instanceof Container) {
            for (Component child : ((Container)component).getComponents()) {
                Component result = findComponentByName(child, name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    
    private final class Runner implements Runnable {
        public void run() {
            executeInBackground();
        }
    }

    
    private final class SelectPopupItemExecutor {
        private final DemoCommand command;
        
        SelectPopupItemExecutor(DemoCommand command) {
            this.command = command;
        }
        
        private void offEDT10() {
            Point loc = getLocation(command);
            moveMouseSmoothlyTo(loc.x, loc.y);
            mousePress(InputEvent.BUTTON3_MASK, MouseEvent.BUTTON3);
            mouseRelease(InputEvent.BUTTON3_MASK, MouseEvent.BUTTON3);
        }
        
        private void offEDT20() throws InterruptedException, InvocationTargetException {
            String[] indices = ((String)command.getArguments().get(ARG_INDEX)).split("\\.");
            final Point[] points = new Point[1];
            int pathCount = 0;
            for (String sIndex : indices) {
                final int index = Integer.parseInt(sIndex);
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        MenuSelectionManager msm = MenuSelectionManager.defaultManager();
                        MenuElement[] menuSelection = msm.getSelectedPath();
                        Point loc = null;
                        if (menuSelection != null && menuSelection.length > 0) {
                            JPopupMenu popup;
                            if (menuSelection.length <= 2) {
                                popup = (JPopupMenu)menuSelection[0];
                            } else {
                                popup = (JPopupMenu)menuSelection[menuSelection.length - 1];
                            }
                            Component item = popup.getComponent(index);
                            loc = item.getLocationOnScreen();
                            loc.x += item.getWidth() / 2;
                            loc.y += item.getHeight() / 2;
                        }
                        points[0] = loc;
                    }
                });
                if (points[0] != null) {
                    if (pathCount == 0) {
                        moveMouseSmoothlyTo(points[0].x, points[0].y);
                    } else {
                        // This is necessary to avoid triggering a change in selection of
                        // the path. A smoother algorithm would be much better.
                        moveMouse(points[0].x, points[0].y);
                    }
                    mousePress(InputEvent.BUTTON1_MASK, MouseEvent.BUTTON1);
                    mouseRelease(InputEvent.BUTTON1_MASK, MouseEvent.BUTTON1);
                } else {
                    throw new RuntimeException("Unable to find index");
                }
                pathCount++;
            }
        }
    }
    
    
    private final class MouseMoveExecutor {
        private final DemoCommand command;
        
        MouseMoveExecutor(DemoCommand command) {
            this.command = command;
        }
        
        private void offEDT10() {
            Point loc = getLocation(command);
            moveMouseSmoothlyTo(loc.x, loc.y);
        }
    }
    
    
    private final class MouseClickExecutor {
        private final DemoCommand command;
        
        MouseClickExecutor(DemoCommand command) {
            this.command = command;
        }
        
        private void offEDT10() {
            Point loc = getLocation(command);
            moveMouseSmoothlyTo(loc.x, loc.y);
            mousePress(InputEvent.BUTTON1_MASK, MouseEvent.BUTTON1);
            mouseRelease(InputEvent.BUTTON1_MASK, MouseEvent.BUTTON1);
        }
    }
    
    
    private final class MouseDragExecutor {
        private final DemoCommand command;
        
        MouseDragExecutor(DemoCommand command) {
            this.command = command;
        }
        
        private void offEDT10() {
            mousePress(InputEvent.BUTTON1_MASK, MouseEvent.BUTTON1);
            Object pause = command.getArguments().get(ARG_PAUSE);
            if (pause != null) {
                try {
                    Thread.sleep(Integer.parseInt((String) pause));
                } catch (NumberFormatException ex) {
                } catch (InterruptedException ex) {
                }
            }
            Point loc = getLocation(command);
            moveMouseSmoothlyTo(loc.x, loc.y);
            mouseRelease(InputEvent.BUTTON1_MASK, MouseEvent.BUTTON1);
        }
    }
    
    
    private final class MousePressExecutor {
        private final DemoCommand command;
        
        MousePressExecutor(DemoCommand command) {
            this.command = command;
        }

        private void offEDT10() {
            mousePress(InputEvent.BUTTON1_MASK, MouseEvent.BUTTON1);
        }
    }
    
    
    private final class MouseReleaseExecutor {
        private final DemoCommand command;
        
        MouseReleaseExecutor(DemoCommand command) {
            this.command = command;
        }

        private void offEDT10() {
            mouseRelease(InputEvent.BUTTON1_MASK, MouseEvent.BUTTON1);
        }
    }
    
    
    private final class KeyTypeExecutor {
        private final DemoCommand command;

        KeyTypeExecutor(DemoCommand command) {
            this.command = command;
        }

        private void offEDT10() {
            int keyCode = Integer.parseInt((String) command.getArguments().get(ARG_KEY_CODE));
            pressKey(keyCode);
            releaseKey(keyCode);
        }
    }

    
    private final static class PauseExecutor {
        private final DemoCommand command;

        PauseExecutor(DemoCommand command) {
            this.command = command;
        }
        
        private void offEDT10() {
            int value = Integer.parseInt((String) command.getArguments().get(ARG_TIME));
            try {
                Thread.sleep(value);
            } catch (InterruptedException ex) {
            }
        }
    }
    
    
    private static abstract class EventConditional {
        public abstract boolean matchesEvent(Object o);
    }

    
    private static class DefaultEventConditional extends EventConditional {
        private final int id;
        private final Object[] args;
        
        public DefaultEventConditional(int id, Object[] args) {
            this.id = id;
            this.args = args;
        }
        
        public boolean matchesEvent(Object e) {
            if (id == MouseEvent.MOUSE_RELEASED && e instanceof DragSourceDropEvent) {
                return true;
            }
            if (id == MouseEvent.MOUSE_DRAGGED && e instanceof DragSourceDragEvent) {
                DragSourceDragEvent dsde = (DragSourceDragEvent)e;
                int wantX = (Integer)args[1];
                int wantY = (Integer)args[3];
                return (wantX == dsde.getX() && wantY == dsde.getY());
            } else if (e instanceof AWTEvent && ((AWTEvent)e).getID() == id) {
                for (int i = 0; i < args.length; i += 2) {
                    if (!argEquals(e, (String)args[i], args[i + 1])) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        private boolean argEquals(Object event, String methodName,
                Object value) {
            try {
                Method method = event.getClass().getMethod(methodName);
                Object result = method.invoke(event);
                return value.equals(result);
            } catch (IllegalArgumentException ex) {
            } catch (IllegalAccessException ex) {
            } catch (InvocationTargetException ex) {
            } catch (NoSuchMethodException nsme) {
            }
            return false;
        }
        
        public String toString() {
            String result = "EventConditional [";
            for (int i = 0; i < args.length; i += 2) {
                if (i > 0) {
                    result += ", ";
                }
                result += args[i] + "=" + args[i + 1];
            }
            result += "]";
            return result;
        }
    }
    
    
    private final class AWTEventHandler implements AWTEventListener, DragSourceListener, 
            DragSourceMotionListener {
        public void eventDispatched(AWTEvent event) {
            awtEventDispatched(event);
        }

        public void dragMouseMoved(DragSourceDragEvent event) {
            awtEventDispatched(event);
        }

        public void dragEnter(DragSourceDragEvent dsde) {
        }

        public void dragOver(DragSourceDragEvent dsde) {
        }

        public void dropActionChanged(DragSourceDragEvent dsde) {
        }

        public void dragExit(DragSourceEvent dse) {
        }

        public void dragDropEnd(DragSourceDropEvent dsde) {
            awtEventDispatched(dsde);
        }
    }
    
    
    private static final class MethodRunnable implements Runnable {
        private final Object target;
        private final Method method;
        
        MethodRunnable(Object target, Method method) {
            this.target = target;
            this.method = method;
        }
        
        public void run() {
            try {
                method.invoke(target);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    public class ExecuteContext {
        private final DemoCommand command;
        
        ExecuteContext(DemoCommand command) {
            this.command = command;
        }
        
        public DemoCommand getCommand() {
            return command;
        }
        
        public DemoGlassPane getGlassPane() {
            return gp;
        }
        
        public Component getComponentByName(String name) {
            return findComponentByName(name);
        }
        
        public void moveMouse(int x, int y) {
            moveMouseSmoothlyTo(x, y);
        }
    }
}
