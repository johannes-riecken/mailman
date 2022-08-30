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
import binding.BindingConverter;
import binding.BindingDescription;
import com.sun.javaone.mailman.model.Contact;
import com.sun.javaone.mailman.model.Message;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.*;
import org.jdesktop.swingx.border.DropShadowBorder;

/**
 *
 * @author sky
 */
public class MessageHeaderPanel extends JPanel {
    private static final int IS = 96;

    private BlurryLabel subject;
    private JLabel from;
    private JLabel to;
    private JLabel cc;

    private JLabel subjectLabel;
    private JLabel fromLabel;
    private JLabel toLabel;
    private JLabel ccLabel;

    private QuotedPathPanel pathPanel;

    private JImagePanel imagePanel;

    private Message message;

    private boolean showImagePanel;

    public MessageHeaderPanel() {
        initComponents();
        configureBindings();
        configureLayout();
        enableEvents(MouseEvent.MOUSE_EVENT_MASK);
        setName("messageHeader");
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        if (!e.isConsumed() && e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    private void showPopup(MouseEvent e) {
        getPopupMenu().show(this, e.getX(), e.getY());
    }


    private JPopupMenu getPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JCheckBoxMenuItem bigSubjectCI = new JCheckBoxMenuItem("Big Subject");
        bigSubjectCI.setSelected(subject.getDrawBlur());
        bigSubjectCI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setBigSubject(((JCheckBoxMenuItem)e.getSource()).isSelected());
            }
        });
        popupMenu.add(bigSubjectCI);
        JCheckBoxMenuItem imagePanelCB = new JCheckBoxMenuItem("Images");
        imagePanelCB.setSelected(getShowImagePanel());
        imagePanelCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setShowImagePanel(((JCheckBoxMenuItem)e.getSource()).
                        isSelected());
            }
        });
        popupMenu.add(imagePanelCB);
        return popupMenu;
    }

    private void setShowImagePanel(boolean showImagePanel) {
        this.showImagePanel = showImagePanel;
        imagePanel.getParent().setVisible(showImagePanel);
        revalidate();
        repaint();
    }

    private boolean getShowImagePanel() {
        return imagePanel.getParent().isVisible();
    }

    private void setBigSubject(boolean bigSubject) {
        if (bigSubject) {
            subject.setFont(subject.getFont().deriveFont(18f));
            subject.setDrawBlur(true);
            subject.setBorder(new EmptyBorder(0, 5, 2, 5));
            subjectLabel.setVisible(false);
        } else {
            subject.setFont(to.getFont());
            subject.setDrawBlur(false);
            subject.setBorder(null);
            subjectLabel.setVisible(true);
        }
        resetLayout();
    }

    public QuotedPathPanel getPathPanel() {
        return pathPanel;
    }

    private void configureBindings() {
        BindingContext context = new BindingContext();
        BindingDescription sBD = context.addDescription(new BindingDescription(
                this, "message.subject", subject, "text"));
        sBD.setNullSourceValue(" ");
        sBD.setConverter(new BindingConverter() {
            @Override
            public Object getValueForIncompleteSource(BindingDescription description) {
                return " ";
            }
            @Override
            public Object convertToTarget(BindingDescription description,
                    Object value) {
                if (value == null || "".equals(value)) {
                    return " ";
                }
                return value;
            }
        });
        context.addDescription(new BindingDescription(
                this, "message.from", from, "text")).
                setConverter(new ContactConverter());
        context.addDescription(new BindingDescription(
                this, "message.to", to, "text")).
                setConverter(new ContactConverter());
        context.addDescription(new BindingDescription(
                this, "message.cc", cc, "text")).
                setConverter(new ContactConverter());
        BindingDescription vis = context.addDescription(new BindingDescription(
                this, "message.cc", cc, "visible"));
        vis.setConverter(new BooleanConverter());
        vis.setNullSourceValue(Boolean.FALSE);
//        BindingDescription visLabel = context.addDescription(new BindingDescription(
//                this, "message.cc", ccLabel, "visible"));
//        visLabel.setConverter(new BooleanConverter());
//        visLabel.setNullSourceValue(Boolean.FALSE);
        BindingDescription editDescription = new BindingDescription(
                this, "message", imagePanel, "editable");
        editDescription.setNullSourceValue(Boolean.FALSE);
        editDescription.setConverter(new BooleanConverter());
        context.addDescription(editDescription);
        context.addDescription(new BindingDescription(
                this, "message.from.imageLocation",
                imagePanel, "imagePath"));
        context.bind();
    }

    public void setMessage(Message message) {
        Message oldMessage = this.message;
        this.message = message;
        firePropertyChange("message", oldMessage, message);
    }

    public Message getMessage() {
        return message;
    }

    private void configureLayout() {
        setBorder(new EmptyBorder(0, 0, 3, 0));
        JPanel imagePanelWrapper = new JPanel(new BorderLayout());
        imagePanelWrapper.add(imagePanel);
        imagePanelWrapper.setOpaque(false);
        imagePanelWrapper.setBorder(new LineBorder(new Color(127, 157, 185), 1));
        imagePanelWrapper.setBorder(BorderFactory.createCompoundBorder(new DropShadowBorder(Color.BLACK, 0, 5, .5f,
                                                                       12, false, true, true, true), imagePanelWrapper.getBorder()));
        imagePanelWrapper.setVisible(false);

        add(imagePanelWrapper);

        resetLayout();
    }

    private void resetLayout() {
        Component imagePanelWrapper = imagePanel.getParent();
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);
        GroupLayout.SequentialGroup hg = layout.createSequentialGroup();
        if (subject.getDrawBlur()) {
        hg.addGroup(layout.createParallelGroup().
             addGroup(layout.createSequentialGroup().
               addGap(6).
               addComponent(pathPanel, 0, 0, Integer.MAX_VALUE)).
             addComponent(subject, GroupLayout.Alignment.CENTER, 0, 0, Integer.MAX_VALUE).
             addGroup(layout.createSequentialGroup().
               addGap(6).
               addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).
                 addComponent(fromLabel).
                 addComponent(toLabel).
                 addComponent(ccLabel)).
              addGroup(layout.createParallelGroup().
                addComponent(from, 0, 0, Integer.MAX_VALUE).
                addComponent(to, 0, 0, Integer.MAX_VALUE).
                addComponent(cc, 0, 0, Integer.MAX_VALUE)))).
           addComponent(imagePanelWrapper, IS, IS, IS);
        } else {
        hg.addGroup(layout.createParallelGroup().
             addGroup(layout.createSequentialGroup().
               addGap(6).
               addComponent(pathPanel, 0, 0, Integer.MAX_VALUE)).
             addGroup(layout.createSequentialGroup().
               addGap(6).
               addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).
                 addComponent(subjectLabel).
                 addComponent(fromLabel).
                 addComponent(toLabel).
                 addComponent(ccLabel)).
              addGroup(layout.createParallelGroup().
                addComponent(from, 0, 0, Integer.MAX_VALUE).
                addComponent(to, 0, 0, Integer.MAX_VALUE).
                addComponent(cc, 0, 0, Integer.MAX_VALUE).
                addComponent(subject, 0, 0, Integer.MAX_VALUE)))).
           addComponent(imagePanelWrapper, IS, IS, IS);
        }
        layout.setHorizontalGroup(hg);

        GroupLayout.ParallelGroup vg = layout.createParallelGroup();
        if (subject.getDrawBlur()) {
        vg.addComponent(imagePanelWrapper, GroupLayout.Alignment.CENTER, IS, IS, IS).
           addGroup(layout.createSequentialGroup().
             addComponent(subject).
             addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
               addComponent(fromLabel).
               addComponent(from)).
             addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
               addComponent(toLabel).
               addComponent(to)).
             addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
               addComponent(ccLabel).
               addComponent(cc)).
             addComponent(pathPanel).
             addGap(5));
        } else {
        vg.addComponent(imagePanelWrapper, GroupLayout.Alignment.CENTER, IS, IS, IS).
           addGroup(layout.createSequentialGroup().
             addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
               addComponent(subjectLabel).
               addComponent(subject)).
             addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
               addComponent(fromLabel).
               addComponent(from)).
             addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
               addComponent(toLabel).
               addComponent(to)).
             addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
               addComponent(ccLabel).
               addComponent(cc)).
             addComponent(pathPanel).
             addGap(5));
        }
        layout.setVerticalGroup(vg);
    }

    private void initComponents() {
        subject = new BlurryLabel();//new JLabel(" ");
        subject.setBorder(null);
        subject.setDrawBlur(false);
        subject.setBorder(null);
        from = new JLabel();
        to = new JLabel();
        cc = new JLabel();
        fromLabel = new JLabel("From:");
        toLabel = new JLabel("To:");
        ccLabel = new JLabel("CC:");
        subjectLabel = new JLabel("Subject:");
        imagePanel = new JImagePanel();

        Font labelFont = fromLabel.getFont().deriveFont(Font.BOLD);
        fromLabel.setFont(labelFont);
        toLabel.setFont(labelFont);
        ccLabel.setFont(labelFont);
        subjectLabel.setFont(labelFont);
        pathPanel = new QuotedPathPanel();
        pathPanel.setVisible(false);
    }



    private static final class BooleanConverter extends BindingConverter {
        @Override
        public Object getValueForIncompleteSource(BindingDescription description) {
            return false;
        }
        @Override
        public Object convertToTarget(BindingDescription description,
                Object value) {
            return value != null;
        }
    }


    private static final class ContactConverter extends BindingConverter {
        @Override
        public Object convertToTarget(BindingDescription description,
                Object value) {
            if (value == null) {
                return "";
            }
            if (value instanceof Contact) {
                return getContactText0((Contact)value);
            }
            return getContactText((Contact[])value);
        }

        private String getContactText(Contact...contacts) {
            if (contacts.length == 1) {
                return getContactText0(contacts[0]);
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < contacts.length; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(getContactText0(contacts[i]));
            }
            return builder.toString();
        }

        private String getContactText0(Contact contact) {
            return contact.getAddress();
        }
    }
}
