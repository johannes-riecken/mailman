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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.NoiseFilter;
import com.sun.javaone.mailman.ui.image.DropShadowPanel;
import com.sun.javaone.mailman.ui.image.GraphicsUtil;
import com.sun.javaone.mailman.ui.image.ShadowFactory;
import com.sun.javaone.mailman.ui.image.WrappedBoxBlurFilter;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.ImageEffect;
import org.jdesktop.swingx.painter.ShapePainter;
import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;
import org.jdesktop.swingx.util.Resize;

/**
 *
 * @author  gfx
 */
public class ComposeMailFrame extends javax.swing.JFrame {

    /** Creates new form ComposeMailFrame */
    public ComposeMailFrame() {
        initComponents();
        initRendering();
        initTextArea();
    }

    private void initTextArea() {
        messageTextArea.setText(">Oué j'ai vu, justement je croyais que c'était un non gentiment tourné ^\n" +
                                "\n" +
                                "Ahah j'avais pas calé. En fait j'ai vu un popup avec ta tête annonçant\n" +
                                "ton mail au moment où je cliquais envoyer message sur ton blog :))\n" +
                                "\n" +
                                "On 27 avr. 06, at 04:03, Fabrice Veniard wrote:\n" +
                                "\n" +
                                "> Oué j'ai vu, justement je croyais que c'était un non gentiment tourné ^^ Bon\n" +
                                "> alors normalement je débarque à san francisco le 5 mai à 12h30 au terminal 1\n" +
                                "> :D\n" +
                                ">\n" +
                                "> MERCI !!\n" +
                                ">\n" +
                                "> -----Message d'origine-----\n" +
                                "> De : Romain GUY [mailto:romain_guy@dev.null]\n" +
                                "> Envoyé : jeudi 27 avril 2006 12:27\n" +
                                "> À : Fabrice Veniard\n" +
                                "> Objet : Re: YEEEHAAAAAAAAAAAAAAAAAAAAAAAA\n" +
                                ">\n" +
                                "> \\o/ Je viens de répondre sur ton blog. Sinon ramène-toi ici y'a aucun\n" +
                                "> souci, dis-moi quand et c'est bon.\n" +
                                ">\n" +
                                "> On 27 avr. 06, at 02:58, Fabrice Veniard wrote:\n" +
                                ">\n" +
                                ">>\n" +
                                ">> Je suis pris au campus ubisoft, à moi montréal, le Canada, la neige\n" +
                                ">> et les\n" +
                                ">> caribous !\n" +
                                ">> Bon, ne te sens absolument pas obligé de répondre oui mais du coup\n" +
                                ">> que je\n" +
                                ">> suis en vacances je repense à mes billets d'avions pour et en\n" +
                                ">> partance de\n" +
                                ">> san francisco entre le 5 et le 19 mai. Si t'as prévu autre chose\n" +
                                ">> c'est pas\n" +
                                ">> grave, je comprendrais vu que j'ai pas été d'une clarté limpide\n" +
                                ">> dans mes\n" +
                                ">> intentions.\n" +
                                ">>\n" +
                                ">> Je t'embrasse depuis mon petit nuage.\n" +
                                ">>\n" +
                                ">> Fabrice\n");
        messageTextArea.setCaretPosition(0);
    }

    private void initRendering() {
        BasicGradientPainter gradient = new BasicGradientPainter(
            new GradientPaint(new Point2D.Double(0.0, 0.0), new Color(0xd67801),
                              new Point2D.Double(0.0, 1.0), new Color(0xb35b01)));
        NoiseFilter noise = new NoiseFilter();
        noise.setDistribution(NoiseFilter.GAUSSIAN);
        noise.setMonochrome(true);
        WrappedBoxBlurFilter blur = new WrappedBoxBlurFilter();
        gradient.setEffects(new ImageEffect(noise), new ImageEffect(blur));
        gradient.setUseCache(true);

        ShapePainter shape = new ShapePainter();
        shape.setFillPaint(new Color(0.0f, 0.0f, 0.0f, 0.2f));
        shape.setShape(new Rectangle(0, 0, 1, 6));
        shape.setResize(Resize.HORIZONTAL);
        GaussianFilter gaussian = new GaussianFilter();
        gaussian.setRadius(10.0f);
        shape.setEffects(new ImageEffect(gaussian));
        shape.setUseCache(true);

        CompoundPainter background = new CompoundPainter(gradient, shape);
        background.setUseCache(true);
        messageBackground.setBackgroundPainter(background);
        //messageScrollPane.setOpaque(false);
        messageScrollPane.getViewport().setOpaque(false);
        //messageTextArea.setVisible(false);
        messageBackground.addPropertyChangeListener("message_sent", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                for (Component c : mainToolBar.getComponents()) {
                    c.setEnabled(false);
                }
            }
        });
    }

    public void startSendAnimation(boolean shiftDown) {
        Dimension size = messageTextArea.getSize();
        BufferedImage image =
                GraphicsUtil.createCompatibleImage(size.width, size.height);
        Graphics2D g = image.createGraphics();
        messageTextArea.paint(g);
        g.dispose();

        ((AnimatedSendMailPanel) messageBackground).startAnimation(image,
                                                                   shiftDown);
        messageScrollPane.setVisible(false);
    }

    private void initComponents() {
        javax.swing.JComboBox accountsList;
        javax.swing.JButton attachButton;
        javax.swing.JLabel ccLabel;
        javax.swing.JTextField ccTextField;
        com.sun.javaone.mailman.ui.StealthSplitPane composePanel;
        javax.swing.JButton contactsButton;
        javax.swing.JPanel contentPane;
        javax.swing.JMenu editMenu;
        javax.swing.JMenu fileMenu;
        javax.swing.JLabel fromLabel;
        javax.swing.JPanel headerPanel;
        javax.swing.JMenu helpMenu;
        javax.swing.JMenuBar menuBar;
        javax.swing.JMenu optionsMenu;
        javax.swing.JButton saveButton;
        javax.swing.JButton securityButton;
        javax.swing.JButton sendButton;
        javax.swing.JButton spellButton;
        org.jdesktop.swingx.JXStatusBar statusBar;
        javax.swing.JLabel subjectLabel;
        javax.swing.JTextField subjectTextField;
        javax.swing.JLabel toLabel;
        javax.swing.JTextField toTextField;
        javax.swing.JMenu toolsMenu;
        javax.swing.JMenu viewMenu;

        mainToolBar = new CoolBar();
        mainToolBar.setFloatable(false);
        sendButton = new CoolButton(1);
        sendButton.setName("sendButton");
        contactsButton = new CoolButton(2);
        spellButton = new CoolButton(2);
        attachButton = new CoolButton(2);
        securityButton = new CoolButton(2);
        saveButton = new CoolButton(3);
        contentPane = new javax.swing.JPanel();
        composePanel = new com.sun.javaone.mailman.ui.StealthSplitPane();
        headerPanel = new javax.swing.JPanel();
        fromLabel = new javax.swing.JLabel();
        accountsList = new javax.swing.JComboBox();
        toLabel = new javax.swing.JLabel();
        toTextField = new javax.swing.JTextField();
        ccLabel = new javax.swing.JLabel();
        ccTextField = new javax.swing.JTextField();
        subjectLabel = new javax.swing.JLabel();
        subjectTextField = new javax.swing.JTextField();
        messageBackground = new AnimatedSendMailPanel();
        messageScrollPane = new javax.swing.JScrollPane();
        messageTextArea = new javax.swing.JTextArea();
        statusBar = new org.jdesktop.swingx.JXStatusBar();
        menuBar = new javax.swing.JMenuBar();
        menuBar.setBorder(new javax.swing.border.EmptyBorder(0, 5, 0, 0));
        fileMenu = new javax.swing.JMenu();
        editMenu = new javax.swing.JMenu();
        viewMenu = new javax.swing.JMenu();
        optionsMenu = new javax.swing.JMenu();
        toolsMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();

        DropShadowPanel dsp = new DropShadowPanel();
        dsp.setBorder(new javax.swing.border.EmptyBorder(2, 4, 6, 20));
        ShadowFactory factory = new ShadowFactory(5, 0.2f, Color.BLACK);
        //factory.setRenderingHint(ShadowFactory.KEY_BLUR_QUALITY, ShadowFactory.VALUE_BLUR_QUALITY_HIGH);
        dsp.setShadowFactory(factory);
        dsp.setDistance(5);
        dsp.setAngle(90f);
        dsp.setLayout(new BoxLayout(dsp, BoxLayout.X_AXIS));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Compose");
        sendButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/send-mail_01.png")));
        sendButton.setText("Send");
        sendButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 3, 12));
        sendButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/send-mail_03.png")));
        sendButton.setFocusPainted(false);
        sendButton.setFocusable(false);
        sendButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        sendButton.setRequestFocusEnabled(false);
        sendButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/send-mail_02.png")));
        sendButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        dsp.add(sendButton);

        contactsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/show-address-book_01.png")));
        contactsButton.setText("Contacts");
        contactsButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 3, 12));
        contactsButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/show-address-book_03.png")));
        contactsButton.setFocusPainted(false);
        contactsButton.setFocusable(false);
        contactsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        contactsButton.setRequestFocusEnabled(false);
        contactsButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/show-address-book_02.png")));
        contactsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        dsp.add(contactsButton);

        spellButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/spelling_01.png")));
        spellButton.setText("Spell");
        spellButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 3, 12));
        spellButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/spelling_03.png")));
        spellButton.setFocusPainted(false);
        spellButton.setFocusable(false);
        spellButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        spellButton.setRequestFocusEnabled(false);
        spellButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/spelling_02.png")));
        spellButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        dsp.add(spellButton);

        attachButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/attachment_01.png")));
        attachButton.setText("Attach");
        attachButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 3, 12));
        attachButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/attachment_03.png")));
        attachButton.setFocusPainted(false);
        attachButton.setFocusable(false);
        attachButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        attachButton.setRequestFocusEnabled(false);
        attachButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/attachment_02.png")));
        attachButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        dsp.add(attachButton);

        securityButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/lock_01.png")));
        securityButton.setText("Security");
        securityButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 3, 12));
        securityButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/lock_03.png")));
        securityButton.setFocusPainted(false);
        securityButton.setFocusable(false);
        securityButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        securityButton.setRequestFocusEnabled(false);
        securityButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/lock_02.png")));
        securityButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        dsp.add(securityButton);

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/save_01.png")));
        saveButton.setText("Save");
        saveButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 3, 12));
        saveButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/save_03.png")));
        saveButton.setFocusPainted(false);
        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setRequestFocusEnabled(false);
        saveButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/save_02.png")));
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        dsp.add(saveButton);
        mainToolBar.add(dsp);
        getContentPane().add(mainToolBar, java.awt.BorderLayout.NORTH);

        contentPane.setLayout(new java.awt.BorderLayout());


        composePanel.setBorder(null);
        composePanel.setDividerLocation(120);
        composePanel.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        composePanel.setEnabled(false);
        fromLabel.setText("From:");

        accountsList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Romain Guy<Romain_Guy@dev.null>" }));

        toLabel.setText("To:");

        toTextField.setText("Scott Violet <scott_violet@dev.null>");

        ccLabel.setText("Cc:");

        ccTextField.setText("Shannon Hickey <shannon_hickey@dev.null>");

        subjectLabel.setText("Subject:");

        subjectTextField.setText("Extreme GUI Makeover @ JavaOne");

        org.jdesktop.layout.GroupLayout headerPanelLayout = new org.jdesktop.layout.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, subjectLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, ccLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, toLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, fromLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(toTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                    .add(accountsList, 0, 509, Short.MAX_VALUE)
                    .add(ccTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE)
                    .add(subjectTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 509, Short.MAX_VALUE))
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(accountsList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fromLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(toTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(toLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ccTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ccLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(headerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(subjectTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(subjectLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        composePanel.setTopComponent(headerPanel);

        messageBackground.setLayout(new java.awt.BorderLayout());

        messageTextArea.setColumns(20);
        messageTextArea.setFont(new java.awt.Font("Monospaced", 0, 11));
        messageTextArea.setRows(15);
        messageTextArea.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        messageScrollPane.setViewportView(messageTextArea);

        messageBackground.add(messageScrollPane, java.awt.BorderLayout.CENTER);

        composePanel.setRightComponent(messageBackground);

        contentPane.add(composePanel, java.awt.BorderLayout.CENTER);

        statusBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 4, 22));
        statusBar.setPreferredSize(new java.awt.Dimension(27, 20));
        contentPane.add(statusBar, java.awt.BorderLayout.SOUTH);

        getContentPane().add(contentPane, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");
        menuBar.add(fileMenu);

        editMenu.setText("Edit");
        menuBar.add(editMenu);

        viewMenu.setText("View");
        menuBar.add(viewMenu);

        optionsMenu.setText("Options");
        menuBar.add(optionsMenu);

        toolsMenu.setText("Tools");
        menuBar.add(toolsMenu);

        helpMenu.setText("Help");
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }

    /** @noinspection UNUSED_SYMBOL*/
    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {
        final boolean isShiftDown = (evt.getModifiers() & KeyEvent.SHIFT_MASK) != 0;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                startSendAnimation(isShiftDown);
            }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
                ComposeMailFrame frame = new ComposeMailFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }


    private javax.swing.JToolBar mainToolBar;
    private org.jdesktop.swingx.JXPanel messageBackground;
    private javax.swing.JScrollPane messageScrollPane;
    private javax.swing.JTextArea messageTextArea;


}
