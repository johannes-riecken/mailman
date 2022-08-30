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

import binding.BindingContext;
import binding.BindingDescription;
import javax.swing.plaf.basic.BasicLookAndFeel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DefaultStyledDocument.ElementSpec;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.interpolation.ObjectModifier;
import org.jdesktop.animation.timing.interpolation.PropertyRange;

/**
 *
 * @author sky
 */
// Every time reshape calculate current depth and based on y position.
public class MessagePane extends JTextPane {
    private static final AttributeSet[] DEPTH_ATTRS;
    private static final char[] NEWLINE_TEXT = new char[] { '\n' };

    private static final String TEST_TEXT =
            "asdfa sfas wrote\r\n" +
            ">asdfasdf\r\n" +
            ">asfd\r\n" +
            ">asdf wer wer wrote\r\n" +
            ">>asdfas asfd as wrote fd\r\n" +
            ">>asfasfd\r\n" +
            ">>asfdasfd\r\n" +
            ">>asdfas asfd as wrote fd\r\n" +
            ">>asfasfd\r\n" +
            ">>asfdasfd\r\n" +
            ">>asdfas asfd as wrote fd\r\n" +
            ">>asfasfd\r\n" +
            ">>asfdasfd\r\n" +
            "asdfa sfas wrote\r\n" +
            ">asdfasdf\r\n" +
            ">asdfasdf\r\n" +
            ">asdfasdf\r\n" +
            ">asdfasdf\r\n" +
            "asfdasdf\r\n" +
            ">  Chet, did you try NB 4.2 on your box yet?\r\n" +
            ">\r\n" +
            ">     -Scott\r\n" +
            "> \r\n" +
            "> Hans Muller wrote:\r\n" +
            "> \r\n" +
            ">>\r\n" +
            ">> When we're ready, I hope we'll provide a NetBeans version\r\n";

    private static final Object RESPONSE_TYPE = "Response";
    private static final int RESPONSE_INDENT = 11;
    private static MessagePane SHARED_MESSAGE_PANE;
    private String text;
    QuotedPathPanel pathPanel;

    static {
        DEPTH_ATTRS = new AttributeSet[ColorScheme.getColorSchemeCount()];
        for (int i = 0; i < ColorScheme.getColorSchemeCount(); i++) {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setForeground(attrs,
                    ColorScheme.getScheme(i).getOuterColor());
            StyleConstants.setBold(attrs, true);
            DEPTH_ATTRS[i] = attrs;
        }
    }

    static MessagePane getMessagePane() {
        return SHARED_MESSAGE_PANE;
    }

    private static Color getColorForDepth(int depth) {
        return ColorScheme.getScheme(depth - 1).getOuterColor();
    }

    public static final void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(new MetalLookAndFeel());
                } catch (Exception e) {
                }
                JFrame frame = new JFrame();
                MessagePane mp = new MessagePane();
                QuotedPathPanel pathC = new QuotedPathPanel();
                mp.setBackground(Color.WHITE);
                mp.setEditable(false);
                mp.setText(TEST_TEXT);
                frame.getContentPane().add(pathC, BorderLayout.NORTH);
                JScrollPane sp = new JScrollPane(mp);
                sp.setPreferredSize(new Dimension(200, 200));
                frame.getContentPane().add(sp);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                BindingContext bc = new BindingContext();
                bc.addDescription(new BindingDescription(
                        mp, "quotedPath", pathC, "quotedPath"));
                bc.bind();
                frame.pack();
                frame.show();
            }
        });
    }

    // Look for:
    //   X Y wrote:
    //   On ... X Y wrote:
    // Make this text bold, or italic, or something
    //
    // When you scroll past it, fade persons name into area above
    // Not handling Content-type: text/plain; charset=ISO-8859-1; format=flowed
    // correctly. This content type seems to imply \r\n's are stripped, unless on
    // an empty line.
    public MessagePane() {
        SHARED_MESSAGE_PANE = this;
        DefaultCaret caret = new DefaultCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        setCaret(caret);
        setName("messagePane");
    }

    public Point getExpandLoc(int[] path) {
        return getExpandLoc(getUI().getRootView(this).getView(0), getTextRect(), path, 0);
    }

    public Point getExpandLoc(View view, Rectangle bounds, int[] path, int depth) {
        System.err.println("getExpandLoc, view=" + view + " bounds=" + bounds + " depth=" + depth);
        int responseCount = 0;
        for (int i = 0; i < view.getViewCount(); i++) {
            if (view.getView(i) instanceof ResponseView) {
                if (responseCount++ == path[depth]) {
                    Rectangle childBounds = view.getChildAllocation(i, bounds).
                            getBounds();
                    if (++depth == path.length) {
                        // Return it for this one
                        System.err.println("getting from click view");
                        return ((ResponseView)view.getView(i)).getClickCenter(
                                childBounds.x, childBounds.y);
                    } else {
                        return getExpandLoc(view.getView(i),
                                childBounds, path, depth);
                    }
                }
            }
        }
        return null;
    }

    public void setFoldsQuotes(boolean foldsQuotes) {
        if (foldsQuotes) {
            setEditorKit(new MessageEditorKit());
        } else {
            setEditorKit(new StyledEditorKit());
        }
        setText(text);
    }

    public boolean getFoldsQuotes() {
        return (getEditorKit() instanceof MessageEditorKit);
    }

    public List<String> getQuotedPath() {
        List<String> path = new ArrayList<String>(1);
        Rectangle rect = getTextRect();
        Rectangle visRect = getVisibleRect();
        View view = getDeepestView(getUI().getRootView(this), (float)(rect.x + rect.width - 1),
                (float)visRect.y, rect);
        while (view != null) {
            if (view instanceof ResponseView) {
                path.add(((ResponseView)view).getSender());
            }
            view = view.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public void setText(String text) {
        this.text = text;
        if (getEditorKit() instanceof MessageEditorKit) {
            MessageDocument doc;
            if (text != null) {
                doc = new MessageDocument(text.toCharArray());
            } else {
                doc = new MessageDocument();
            }
            setDocument(doc);
        } else {
            setDocument(getEditorKit().createDefaultDocument());
            try {
                getDocument().insertString(0, text, null);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
        select(0, 0);
        scrollRectToVisible(new Rectangle());
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        if (!e.isConsumed()) {
            if (e.isPopupTrigger()) {
                getPopupMenu().show(this, e.getX(), e.getY());
            } else {
                if (getFoldsQuotes() && e.getClickCount() == 1 &&
                        e.getID() == MouseEvent.MOUSE_CLICKED) {
                    handleClick(e);
                }
            }
        }
    }

    private JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JCheckBoxMenuItem foldsMI = new JCheckBoxMenuItem("Fold Quotes");
        foldsMI.setSelected(getFoldsQuotes());
        foldsMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFoldsQuotes(((JCheckBoxMenuItem)e.getSource()).isSelected());
            }
        });
        popupMenu.add(foldsMI);
        JCheckBoxMenuItem pathPanelMI = new JCheckBoxMenuItem("Show Quote Path");
        pathPanelMI.setSelected(pathPanel.isVisible());
        pathPanelMI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pathPanel.setVisible(((JCheckBoxMenuItem)e.getSource()).isSelected());
            }
        });
        popupMenu.add(pathPanelMI);
        return popupMenu;
    }

    private View getDeepestView(View v, float x, float y, Shape alloc) {
        if (v == null) {
            return null;
        }
        View child = null;
        int index = v.getViewIndex(x, y, alloc);
        if (index != -1) {
            alloc = v.getChildAllocation(index, alloc);
            child = getDeepestView(v.getView(index), x, y, alloc);
        }
        if (child == null) {
            return v;
        }
        return child;
    }

    private Rectangle getTextRect() {
        Rectangle bounds = getBounds();
        bounds.x = 0;
        bounds.y = 0;
        Insets insets = getInsets();
        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= (insets.left + insets.right);
        bounds.height -= (insets.top + insets.bottom);
        return bounds;
    }

    private void handleClick(MouseEvent e) {
        float x = (float)e.getX();
        float y = (float)e.getY();
        View view = getUI().getRootView(this);
        toggleView(view, x, y, getTextRect());
    }

    private View toggleView(View v, float x, float y, Shape alloc) {
        if (v == null) {
            return null;
        }
        int index = v.getViewIndex(x, y, alloc);
        if (index == -1) {
            if (v instanceof ResponseView) {
                ((ResponseView)v).toggleIfNecessary(x, y, alloc);
                return v;
            } else {
                return null;
            }
        }
        alloc = v.getChildAllocation(index, alloc);
        return toggleView(v.getView(index), x, y, alloc);
    }

    @Deprecated
    @Override
    public void reshape(int x, int y, int w, int h) {
        super.reshape(x, y, w, h);
        firePropertyChange("quotedPath", null, null);
    }


    public static final class ResponseView extends BoxView {
        private static final int MIN_HEIGHT = 20;
        private int depth;
        private int fontCenter;
        private boolean expanded = true;
        private boolean animating;
        private float height;

        ResponseView(Element e) {
            super(e, BoxView.Y_AXIS);
        }

        private String getSender() {
            Element e = getElement();
            Element parent = e.getParentElement();
            int index = parent.getElementIndex(getStartOffset());
            if (index > 0) {
                Element previousSibling = parent.getElement(index - 1);
                if (AbstractDocument.ParagraphElementName == previousSibling.getName()) {
                    for (int i = 0; i < previousSibling.getElementCount(); i++) {
                        Element childE = previousSibling.getElement(i);
                        if (childE.getAttributes().getAttribute(StyleConstants.Foreground) != null) {
                            try {
                                return getDocument().getText(childE.getStartOffset(),
                                        childE.getEndOffset() - childE.getStartOffset());
                            } catch (BadLocationException ex) {
                            }
                            break;
                        }
                    }
                }
            }
            return "";
        }

        @Override
        public void setParent(View parent) {
            super.setParent(parent);
            depth = 1;
            while (parent != null) {
                if (parent instanceof ResponseView) {
                    depth++;
                }
                parent = parent.getParent();
            }
        }

        private int getDepth() {
            return depth;
        }

        @Override
        public void paint(Graphics g, Shape a) {
            if (fontCenter == 0) {
                calcFontHeight();
            }
            Rectangle bounds = (a instanceof Rectangle) ? (Rectangle)a :
                a.getBounds();
            Icon icon;
            if (!isExpanded()) {
                icon = UIManager.getIcon("Tree.collapsedIcon");
            } else {
                icon = UIManager.getIcon("Tree.expandedIcon");
            }
            int xOffset = 0;
            int yOffset = fontCenter - icon.getIconHeight() / 2;
            int lineX = bounds.x + 4;
            int lineY = bounds.y + RESPONSE_INDENT / 2 + yOffset;
            int endY = bounds.y + bounds.height - fontCenter + 2;
            Color lineColor = getColorForDepth(getDepth());
            if (!isExpanded() || animating || getHeight() > MIN_HEIGHT) {
                if (animating || isExpanded()) {
                    g.setColor(lineColor);
                    g.fillRect(lineX, lineY, 2, endY - lineY);
                    g.fillRect(lineX, endY - 2, 6, 2);
                }
                icon.paintIcon(getContainer(), g, bounds.x + xOffset, bounds.y + yOffset);
            } else {
                g.setColor(lineColor);
                g.fillRect(lineX, bounds.y, 2, bounds.height);
            }
            if (height != 0) {
                Graphics g2 = g.create();
                g2.clipRect(bounds.x, bounds.y, bounds.width, (int)height);
                super.paint(g2, a);
                g2.dispose();
            } else {
                super.paint(g, a);
            }
        }

        @Override
        protected short getLeftInset() {
            return RESPONSE_INDENT;
        }

        private boolean isExpanded() {
            return expanded;
        }

        private void calcFontHeight() {
            Container host = getContainer();
            FontMetrics metrics = host.getFontMetrics(host.getFont());
            fontCenter = metrics.getAscent() / 2 + 3;
        }

        private void toggleIfNecessary(float x, float y, Shape alloc) {
            Rectangle bounds = alloc.getBounds();
            if ((!isExpanded() || bounds.height > MIN_HEIGHT) &&
                    inClickArea((int)x, (int)y, bounds)) {
                startAnimation();
                expanded = !expanded;
                preferenceChanged(this, true, true);
                getContainer().repaint();
            }
        }

        private Point getClickCenter(int x, int y) {
            return new Point(x + 7, y + fontCenter);
        }

        private boolean inClickArea(int x, int y, Rectangle bounds) {
            return (x >= bounds.x + 2 && x <= bounds.x + 13 &&
                    y >= bounds.y + fontCenter - 4 &&
                    y <= bounds.y + fontCenter + 5);
        }

        @Override
        public float getPreferredSpan(int axis) {
            if (axis == View.Y_AXIS && (animating || !isExpanded()) &&
                    height != 0) {
                return height;
            }
            return super.getPreferredSpan(axis);
        }

        @Override
        public float getMinimumSpan(int axis) {
            if (axis == View.Y_AXIS && (animating || !isExpanded()) &&
                    height != 0) {
                return height;
            }
            return super.getMinimumSpan(axis);
        }

        @Override
        public float getMaximumSpan(int axis) {
            if (axis == View.Y_AXIS && (animating || !isExpanded()) &&
                    height != 0) {
                return height;
            }
            return super.getMaximumSpan(axis);
        }

        private float getCollapsedHeight() {
            return 16f;
        }

        @Override
        public void setSize(float width, float height) {
            super.setSize(width, height);
        }

        private void startAnimation() {
            float start;
            float end;
            if (isExpanded()) {
                start = getPreferredSpan(View.Y_AXIS);
                end = getCollapsedHeight();
            } else {
                start = getCollapsedHeight();
                end = super.getPreferredSpan(View.Y_AXIS);
            }
            animating = true;
            PropertyRange heightRange = PropertyRange.createPropertyRangeFloat(
                    "height", start, end);
            TimingController controller = new TimingController(
                    new Cycle(220, 20),
                    new Envelope(1, 0, Envelope.RepeatBehavior.FORWARD,
                    Envelope.EndBehavior.HOLD));
            controller.setAcceleration(1f);
            controller.addTarget(new ObjectModifier(this, heightRange));
            controller.addTarget(new AnimateListener());
            controller.start();
        }

        public void setHeight(float height) {
            Container host = getContainer();
            this.height = height;
            preferenceChanged(this, true, true);
            host.repaint();
            if (host instanceof JComponent) {
                host.revalidate();
            }
        }


        private class AnimateListener implements TimingTarget {
            @Override
            public void timingEvent(long l, long l0, float f) {
            }

            @Override
            public void begin() {
            }

            @Override
            public void end() {
                animating = false;
            }
        }
    }


    private static final class MessageViewFactory implements ViewFactory {
        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind == RESPONSE_TYPE) {
                    return new ResponseView(elem);
                } else if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }
            // default to text display
            return new LabelView(elem);
        }
    }

    private static final class MessageEditorKit extends StyledEditorKit {
        @Override
        public ViewFactory getViewFactory() {
            return new MessageViewFactory();
        }

        @Override
        public Document createDefaultDocument() {
            return new MessageDocument();
        }

    }


    private static final class MessageDocument extends DefaultStyledDocument {
        private final int length;
        private int currentDepth;
        private int offset;
        private int depth;
        private int nonResponseOffset;

        public MessageDocument() {
            length = 0;
        }

        public MessageDocument(char[] text) {
            super();
            length = (text == null) ? 0 : text.length;
            insert(text);
        }

        private void insert(char[] text) {
            int lastDepth;
            offset = 0;
            depth = 0;
            List<ElementSpec> specList = new ArrayList<ElementSpec>(100);
            specList.add(new ElementSpec(null, ElementSpec.EndTagType));
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(AbstractDocument.ElementNameAttribute, RESPONSE_TYPE);
            while (offset < length) {
                lastDepth = depth;
                parseLine(text);
                if (lastDepth < depth) {
                    lastDepth++;
                    highlightFromIfNecessary(specList, text, lastDepth);
                    specList.add(new ElementSpec(attrs, ElementSpec.StartTagType));
                }
                while (lastDepth < depth) {
                    // insert starts
                    specList.add(new ElementSpec(attrs, ElementSpec.StartTagType));
                    lastDepth++;
                }
                while (lastDepth > depth) {
                    specList.add(new ElementSpec(null, ElementSpec.EndTagType));
                    lastDepth--;
                }
                // line range from nonResponseOffset - offset
                specList.add(new ElementSpec(null, ElementSpec.StartTagType));
                if (offset == nonResponseOffset) {
                    specList.add(new ElementSpec(null, ElementSpec.ContentType, NEWLINE_TEXT, 0, 1));
                } else {
                    specList.add(new ElementSpec(null, ElementSpec.ContentType, text, nonResponseOffset, offset - nonResponseOffset));
                }
                specList.add(new ElementSpec(null, ElementSpec.EndTagType));
                offset++;
                if (offset < length && text[offset] == '\n') {
                    offset++;
                }
            }
            try {
                super.insert(0, specList.toArray(new ElementSpec[specList.size()]));
            } catch (BadLocationException ex) {
                System.err.println("bad insert!");
                assert false;
            }
        }

        private void parseLine(char[] text) {
            depth = 0;
            nonResponseOffset = offset;
            while (text[offset] == ' ' && offset < length) {
                offset++;
            }
            if (offset < length && isResponseChar(text[offset])) {
                depth++;
                offset++;
                for (;;) {
                    while (offset < length && text[offset] == ' ') {
                        offset++;
                    }
                    if (offset < length && isResponseChar(text[offset])) {
                        depth++;
                        offset++;
                    } else {
                        nonResponseOffset = offset;
                        break;
                    }
                }
            }
            while (offset < length && text[offset] != '\r') {
                offset++;
            }
        }

        private boolean isResponseChar(char c) {
            return (c == '>');
        }

        private void highlightFromIfNecessary(List<ElementSpec> specs, char[] text, int depth) {
            int sSize = specs.size();
            if (sSize > 2 && specs.get(sSize - 1).getType() == ElementSpec.EndTagType &&
                    specs.get(sSize - 2).getType() == ElementSpec.ContentType) {
                ElementSpec contentSpec = specs.get(sSize - 2);
                int offset = contentSpec.getOffset();
                int current = contentSpec.getOffset();
                int max = current + contentSpec.getLength() - 4;
                //   X Y wrote:
                //   On ... X Y wrote:
                for (; current < max; current++) {
                    if (text[current] == ' ' &&
                            (text[current + 1] == 'w' || text[current + 1] == 'W') &&
                            text[current + 2] == 'r' &&
                            text[current + 3] == 'o' &&
                            text[current + 4] == 't' &&
                            text[current + 5] == 'e') {
                        int lastNameEnd = findLastNonSpace(text, current, offset);
                        int lastNameStart = findLastSpace(text, lastNameEnd, offset);
                        int firstNameEnd = findLastNonSpace(text, lastNameStart, offset);
                        int firstNameStart = findLastSpace(text, firstNameEnd, offset);
                        if (lastNameEnd != -1 && lastNameStart != -1 &&
                                firstNameEnd != -1) {
                            if (firstNameStart == -1) {
                                firstNameStart = offset;
                            }
                            int insertIndex = sSize - 2;
                            specs.remove(insertIndex);
                            if (firstNameStart > offset) {
                                specs.add(insertIndex++, new ElementSpec(
                                        null, ElementSpec.ContentType, text,
                                        offset, firstNameStart - offset));
                            }
                            specs.add(insertIndex++, new ElementSpec(
                                    getNameAttributeSet(depth), ElementSpec.ContentType,
                                    text, firstNameStart, lastNameEnd - firstNameStart + 1));
                            if (lastNameEnd + 1 < offset + contentSpec.getLength()) {
                                specs.add(insertIndex++, new ElementSpec(
                                        null, ElementSpec.ContentType, text,
                                        lastNameEnd + 1, offset + contentSpec.getLength() - lastNameEnd - 1));
                            }
                        }
                        break;
                    }
                }
            }
        }

        private int findLastNonSpace(char[] text, int i, int min) {
            while (i >= min && text[i] == ' ') {
                i--;
            }
            if (i < min) {
                return -1;
            }
            return i;
        }

        private int findLastSpace(char[] text, int i, int min) {
            while (i >= min && text[i] != ' ') {
                i--;
            }
            if (i < min) {
                return -1;
            }
            return i;
        }

        private AttributeSet getNameAttributeSet(int depth) {
            return DEPTH_ATTRS[Math.min(DEPTH_ATTRS.length - 1, depth - 1)];
        }
    }
}
