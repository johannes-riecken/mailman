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

import appscripter.DemoController;
import appscripter.DemoParser;
import appscripter.DemoStage;
import binding.ListBindingDescription;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.VolatileImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.CompoundBorder;

import binding.BindingContext;
import binding.BindingDescription;
import binding.swing.JTreeChildrenBindingDescription;
import binding.swing.JTreeRootBindingDescription;
import com.sun.javaone.mailman.Application;
import com.sun.javaone.mailman.model.Account;
import com.sun.javaone.mailman.model.MailBox;
import com.sun.javaone.mailman.ui.image.DropShadowPanel;
import com.sun.javaone.mailman.ui.image.ShadowFactory;
import java.awt.Graphics;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.jdesktop.animation.timing.Cycle;
import org.jdesktop.animation.timing.Envelope;
import org.jdesktop.animation.timing.TimingController;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXSearchPanel;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.xml.sax.SAXException;

public class MainFrame extends javax.swing.JFrame {
    private static final boolean STANDARD_LAYOUT = false;

    ComposeMailFrame sharedFrame;

    private final UIController controller;
    private final GlobalKeyStrokeHandler globalKeyListener;

    private final JMenuItem ds = new JCheckBoxMenuItem("Drop Shadows");
    private final JMenuItem coolbar = new JCheckBoxMenuItem("Raised Toolbar");
    private final JMenuItem coolbuttons = new JCheckBoxMenuItem("Cool Buttons");
    private final JMenuItem updateTree = new JMenuItem("Update Tree");
    private final JRadioButtonMenuItem asTableMI = new JRadioButtonMenuItem("View As Table");
    private final JRadioButtonMenuItem asListMI= new JRadioButtonMenuItem("View As List");
    private final JRadioButtonMenuItem asFullListMI= new JRadioButtonMenuItem("View As Full List");
    private JComponent mainToolBar;

    private final JScrollPane mailTableScrollPane = new JScrollPane();
    private final JScrollPane messageViewScrollPane = new JScrollPane();
    private final JScrollPane foldersScrollPane = new JScrollPane();
    private JLabel statusBar;


    private enum SearchDialogType {
        CLASSIC, APPLE, VISTA
    }

    public MainFrame() {
        globalKeyListener = new GlobalKeyStrokeHandler();

        controller = new UIController();
        controller.setAccount(Application.getAccounts()[0]);

        initComponents();
        initBindings();
        registerGlobalKeys();

        FocusTraversalPolicy policy = getFocusTraversalPolicy();
        setFocusTraversalPolicy(new DefaultFocusTraversalPolicy(foldersTree,
                policy));

        foldersTree.setSelectionRow(1);
        foldersTree.addFocusListener(new TreeDocker());
        if (mailTable.getRowCount() != 0) {
            mailTable.setRowSelectionInterval(0, 0);
        }

        mailTable.setTransferHandler(foldersTree.getTransferHandler());

        floatablePanel.addPropertyChangeListener(SetFloatableAction.FLOATABLE_STATUS,
                new FloatingTreeFocusHandler());
        ActionListener action = new DockedTreeButton();
        foldersCollapsiblePane.setLayout(new GridLayout(1, 1));
        enableFloatableCapability(floatablePanel, action);

        setLocationRelativeTo(null);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt 1"), "updateBoxCounts");
        getRootPane().getActionMap().put("updateBoxCounts", new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                foldersTree.changeCounts();
            }
        });
    }

    public void runDemo() {
        try {
            List<DemoStage> stages = DemoParser.parse(MainFrame.class.getResourceAsStream("script.xml"));
            stages = DemoController.flowStages(stages, getWidth() - 60);
            final DemoController controller = new DemoController(this, stages);
            Timer timer = new Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    controller.start(0);
                }
            });
            timer.setRepeats(false);
            timer.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    private void initBindings() {
        BindingContext context = new BindingContext();
        JTreeRootBindingDescription treeBD = new JTreeRootBindingDescription(
                controller, "account", foldersTree, "root");
        treeBD.setEmptyNodesTreatedAsLeafs(true);
        treeBD.addDescription(new JTreeChildrenBindingDescription(
                Account.class, "mailBoxs"));
        treeBD.addDescription(new JTreeChildrenBindingDescription(
                MailBox.class, "folders"));
        context.addDescription(treeBD);

        context.addDescription(new BindingDescription(
                controller, "selectedElement", foldersTree, "selectedElement"));

        mailTable.addBindings(controller, context);

        context.addDescription(new BindingDescription(
                controller, "selectedMessage", mailTable, "selectedElement"));

        context.addDescription(new BindingDescription(
                controller, "selectedMessage.body", messageView, "text"));

        context.addDescription(new BindingDescription(
                controller, "selectedMessage", messageHeaderPanel, "message"));

        context.addDescription(new BindingDescription(
                messageView, "quotedPath", messageHeaderPanel.getPathPanel(), "quotedPath"));

        context.bind();

        mailTable.bound();
    }

    public void showFullMessageList() {
        createMessageListIfNecessary();
        if (!listPanel.isShowing()) {
            listPanel.ensureListInScrollPane();
            floatableTarget.remove(mailTableScrollPane.getParent());
            floatableTarget.add(listPanel, BorderLayout.CENTER);
            floatableTarget.revalidate();
            floatableTarget.repaint();
        }
    }

    public void showMessageList() {
        createMessageListIfNecessary();
        if (listPanel.isShowing()) {
            floatableTarget.remove(listPanel);
            floatableTarget.add(mailTableScrollPane.getParent(), BorderLayout.CENTER);
            mailTableScrollPane.setViewportView(listPanel.getList());
            floatableTarget.revalidate();
            floatableTarget.repaint();
        } else if (!listPanel.getList().isShowing()) {
            mailTableScrollPane.setViewportView(listPanel.getList());
            mailTableScrollPane.revalidate();
            mailTableScrollPane.repaint();
        }
    }

    public void showMessageTable() {
        if (!mailTable.isShowing()) {
            if (listPanel.isShowing()) {
                floatableTarget.remove(listPanel);
                floatableTarget.add(mailTableScrollPane.getParent(), BorderLayout.CENTER);
                floatableTarget.revalidate();
                floatableTarget.repaint();
            }
            mailTableScrollPane.setViewportView(mailTable);
            mailTableScrollPane.revalidate();
            mailTableScrollPane.repaint();
        }
    }

    public void showMessagePanel() {
        if (listPanel.isShowing()) {
            // slide MessageListPanel over, when it's docked, then reduce size
            TimingController tc = new TimingController(
                    new Cycle(500, 30),
                    new Envelope(1, 0, Envelope.RepeatBehavior.FORWARD,
                    Envelope.EndBehavior.HOLD));
            tc.addTarget(new ShowMessagePanelHandler());
            tc.start();
        }
    }

    private void createMessageListIfNecessary() {
        if (listPanel == null) {
            listPanel = new MessageListPanel();
            listPanel.getList().addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    System.err.println("folders showing=" + foldersTree.isShowing());
                    if (asFullListMI.isSelected() && foldersTree.isShowing()) {
                        showMessagePanel();
                    }
                }
            });
            BindingContext context = new BindingContext();
            ListBindingDescription listBD = new ListBindingDescription(
                    controller, "selectedMailBox.messages", listPanel.getList(), "elements");
            context.addDescription(listBD);
            context.addDescription(new BindingDescription(
                    controller, "selectedMessage", listPanel.getList(), "selectedElement"));
            context.bind();
        }
    }


    private final class ShowMessagePanelHandler implements TimingTarget {
        private final VolatileImage windowImage;
        private final VolatileImage messagePanelImage;
        private final int targetListPanelWidth;
        private final int targetMessagePanelWidth;
        private final int initialListPanelWidth;
        private final int initialListPanelHeight;
        private final JPanel panel;

        private int messagePanelX;
        private int listPanelWidth;
        private int listPanelX;

        ShowMessagePanelHandler() {
            int availableWidth = floatableTarget.getWidth();
            targetListPanelWidth = availableWidth * 4 / 10;
            targetMessagePanelWidth = availableWidth - targetListPanelWidth;
            windowImage = createVolatileImage(floatableTarget.getWidth(),
                    floatableTarget.getHeight());
            Graphics g = windowImage.getGraphics();
            floatableTarget.paint(g);
            g.dispose();
            initialListPanelWidth = listPanel.getWidth();
            initialListPanelHeight = listPanel.getHeight();
            messagePanel.setSize(targetMessagePanelWidth, floatableTarget.getHeight());
            messagePanel.validate();
            messagePanelImage = createVolatileImage(messagePanel.getWidth(),
                    messagePanel.getHeight());
            g = messagePanelImage.getGraphics();
            messagePanel.paint(g);
            g.dispose();

            listPanelX = listPanel.getX();
            listPanelWidth = listPanel.getWidth();
            messagePanelX = availableWidth;

            panel = new AnimatePanel();
            panel.setLayout(null);
            panel.setOpaque(true);
            messagePanel.setBounds(floatableTarget.getWidth(), 0,
                    targetMessagePanelWidth, floatableTarget.getHeight());
            floatableTarget.removeAll();
            floatableTarget.add(panel, BorderLayout.CENTER);
            floatableTarget.revalidate();
            floatableTarget.repaint();
        }

        public void begin() {
        }

        public void timingEvent(long l, long l0, float delta) {
            messagePanelX = floatableTarget.getWidth() -
                    (int)((float)targetMessagePanelWidth * delta);
            listPanelX = messagePanelX - listPanelWidth;
            if (listPanelX < 0) {
                listPanelWidth = messagePanelX;
                listPanelX = 0;
                panel.add(listPanel);
            }
            if (listPanel.getParent() != null) {
                listPanel.setBounds(0, 0, listPanelWidth, listPanel.getHeight());
                listPanel.validate();
            }
            panel.repaint();
        }

        public void end() {
            floatableTarget.removeAll();
            floatableTarget.setLayout(new BorderLayout());
            floatableTarget.add(listPanel, BorderLayout.WEST);
            listPanel.setPreferredSize(new Dimension(targetListPanelWidth, listPanel.getHeight()));
            floatableTarget.add(messagePanel, BorderLayout.CENTER);
            floatableTarget.revalidate();
            floatableTarget.repaint();

            windowImage.flush();
            messagePanelImage.flush();

            listPanel.showHeader();
            listPanel.setMailBox(controller.getSelectedMailBox());
        }

        private final class AnimatePanel extends JPanel {
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                g.drawImage(windowImage, 0, 0, null);

                g.drawImage(messagePanelImage, messagePanelX, 0, null);

                if (listPanel.getParent() == null) {
                    g.drawImage(windowImage, listPanelX, 0, listPanelX + listPanelWidth, windowImage.getHeight(null),
                            windowImage.getWidth(null) - initialListPanelWidth, 0, windowImage.getWidth(null), initialListPanelHeight, null);
                }
            }
        }
    }


    private void enableFloatableCapability(JComponent component,
            ActionListener action) {
        globalKeyListener.putKeyBinding(KeyStroke.getKeyStroke("ctrl 1"),
                new SetFloatableActionWrapper(component,
                action,
                floatableTarget,
                BorderLayout.WEST));
        enableResizableCapability(component, SwingConstants.EAST, 10);
    }

    private class SetFloatableActionWrapper extends AbstractAction {
        private final Action action;

        public SetFloatableActionWrapper(JComponent component,
                ActionListener action,
                JPanel floatableTarget,
                String constraints) {
            this.action = new SetFloatableAction(component,
                    action,
                    floatableTarget,
                    constraints);
        }

        public void actionPerformed(ActionEvent e) {
            if (foldersScrollPane.getBorder() instanceof CompoundBorder) {
                CompoundBorder border = (CompoundBorder) foldersScrollPane.getBorder();
                foldersScrollPane.setBorder(border.getInsideBorder());
                floatablePanel.setBorder(new DropShadowBorder(Color.BLACK,
                        0, 5, .5f, 12,
                        false, true, true, true));
            } else {
                foldersScrollPane.setBorder(BorderFactory.createCompoundBorder(
                        new DropShadowBorder(Color.BLACK, 0, 5, .5f, 12, false, true, true, true),
                        foldersScrollPane.getBorder()));
                floatablePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
            }
            action.actionPerformed(e);
        }
    }

    // we assume side == SwingConstants.EAST
    private static void enableResizableCapability(JComponent component, int side,
            int threshold) {
        ResizingMouseListener resizer = new ResizingMouseListener(side, threshold);
        component.addMouseListener(resizer);
        component.addMouseMotionListener(resizer);
    }

    private void registerGlobalKeys() {
        getToolkit().addAWTEventListener(globalKeyListener,
                KeyEvent.KEY_EVENT_MASK);
        globalKeyListener.putKeyBinding(KeyStroke.getKeyStroke("ctrl F"),
                new ShowHideSearchBarAction());
        globalKeyListener.putKeyBinding(KeyStroke.getKeyStroke("ctrl shift F"),
                new ShowSearchDialogAction(SearchDialogType.CLASSIC));
        globalKeyListener.putKeyBinding(KeyStroke.getKeyStroke("ctrl alt F"),
                new ShowSearchDialogAction(SearchDialogType.APPLE));
        Action vistaAction =  new ShowSearchDialogAction(SearchDialogType.VISTA);
        vistaAction.putValue(Action.NAME, "vista");
        globalKeyListener.putKeyBinding(KeyStroke.getKeyStroke("ctrl alt shift F"),
               vistaAction);
        globalKeyListener.putKeyBinding(KeyStroke.getKeyStroke("ctrl M"),
                new ShowHideFoldersAction());
        globalKeyListener.putKeyBinding(KeyStroke.getKeyStroke("ctrl 6"),
                new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showMessagePanel();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    // COMPONENTS CREATION
    ////////////////////////////////////////////////////////////////////////////

    private void initComponents() {
        JPanel contentPanel;
        JMenu editMenu;
        GridBagConstraints gridBagConstraints;
        JMenu helpMenu;
        JMenu mailMenu;
        JMenu messageMenu;
        StealthSplitPane stealthSplitPane;
        JMenu toolsMenu;
        JMenu viewMenu;

        contentPanel = new JPanel();
        floatableTarget = new JPanel();
        stealthSplitPane = new StealthSplitPane();
        mailTable = new MailTable(controller);
        mailTable.setName("mailTable");
        mailTable.getInputMap(JComponent.WHEN_FOCUSED).
                put(KeyStroke.getKeyStroke("pressed SPACE"), "scrollDown");
        mailTable.getInputMap(JComponent.WHEN_FOCUSED).
                put(KeyStroke.getKeyStroke("shift pressed SPACE"), "scrollUp");
        mailTable.getActionMap().put("scrollDown", new MessagePaneScrollAction(true));
        mailTable.getActionMap().put("scrollUp", new MessagePaneScrollAction(false));
        messagePanel = new JPanel();
        messageView = new MessagePane();//new JTextArea();
        messageView.setBackground(Color.WHITE);
        findCollapsiblePane = new JXCollapsiblePane();
        JPanel jPanel1 = new JPanel();
        //JSeparator jSeparator4 = new JSeparator();
        searchPanel = new JXSearchPanel();
        JButton closeSearchPanel = new JButton();
        floatablePanel = new ResizableJXPanel();
        foldersCollapsiblePane = new JXCollapsiblePane();
        foldersScrollPane.setPreferredSize(new Dimension(170, 100));
        foldersTree = new MailBoxTree();
        foldersTree.setName("mailTree");
        JMenuBar mainMenuBar = new JMenuBar();
        mainMenuBar.setBorder(new javax.swing.border.EmptyBorder(0, 5, 0, 0));
        mailMenu = new JMenu();
        JMenuItem quitMenuItem = new JMenuItem();
        editMenu = new JMenu();
        viewMenu = new JMenu();
        messageMenu = new JMenu();
        toolsMenu = new JMenu();
        helpMenu = new JMenu();
        JMenuItem bindings = new JMenuItem("Demo Bindings");
        bindings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new Bindings(MainFrame.this, false).setVisible(true);
            }
        });
        helpMenu.add(bindings);

        messageHeaderPanel = new MessageHeaderPanel();
        messageView.pathPanel = messageHeaderPanel.getPathPanel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Java Mail");
        setName("mainFrame");


        floatableTarget.setLayout(new BorderLayout());

        stealthSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
        stealthSplitPane.setDividerLocation(160);
        stealthSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        stealthSplitPane.setContinuousLayout(true);
        mailTable.setShowHorizontalLines(false);
        mailTable.setShowVerticalLines(false);
        mailTable.setFillsViewportHeight(true);
        mailTable.setIntercellSpacing(new Dimension(0, 0));
        mailTableScrollPane.setViewportView(mailTable);

        stealthSplitPane.setLeftComponent(mailTableScrollPane);

        messagePanel.setLayout(new BorderLayout());

        messagePanel.add(messageHeaderPanel, BorderLayout.NORTH);

        messagePanel.setBorder(null);
        messageView.setEditable(false);
        messageView.setFont(new Font("Monospaced", 0, 12));
        messageView.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        messageViewScrollPane.getVerticalScrollBar().setName("messagePaneVerticalScrollBar");
        messageViewScrollPane.setViewportView(messageView);

        messagePanel.add(messageViewScrollPane, BorderLayout.CENTER);

        findCollapsiblePane.getContentPane().setLayout(new BorderLayout());

        findCollapsiblePane.setCollapsed(true);
        jPanel1.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        //jPanel1.add(jSeparator4, gridBagConstraints);

        searchPanel.setFieldName("Find: ");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(searchPanel, gridBagConstraints);

        closeSearchPanel.setIcon(new ImageIcon(getClass().getResource("/resources/icons/close_01.png")));
        closeSearchPanel.setBorder(null);
        closeSearchPanel.setBorderPainted(false);
        closeSearchPanel.setContentAreaFilled(false);
        closeSearchPanel.setFocusPainted(false);
        closeSearchPanel.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/close_03.png")));
        closeSearchPanel.setRolloverIcon(new ImageIcon(getClass().getResource("/resources/icons/close_02.png")));
        closeSearchPanel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                closeSearchPanelActionPerformed(evt);
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 3, 0, 0);
        jPanel1.add(closeSearchPanel, gridBagConstraints);

        findCollapsiblePane.getContentPane().add(jPanel1, BorderLayout.NORTH);

        messagePanel.add(findCollapsiblePane, BorderLayout.SOUTH);

        stealthSplitPane.setRightComponent(messagePanel);

        floatableTarget.add(stealthSplitPane, BorderLayout.CENTER);
        floatableTarget.setOpaque(false);

        floatablePanel.setLayout(new BorderLayout());

        floatablePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        floatablePanel.setOpaque(false);
        floatablePanel.setName("Mailboxes");
        foldersCollapsiblePane.getContentPane().setLayout(new BorderLayout());

        foldersCollapsiblePane.setOrientation(JXCollapsiblePane.Orientation.HORIZONTAL);

        DropShadowPanel dsp2 = new ScrollableShadowPanel(foldersTree);
        ShadowFactory factory2 = new ShadowFactory(5, 0.2f, Color.BLACK);
        //factory.setRenderingHint(ShadowFactory.KEY_BLUR_QUALITY, ShadowFactory.VALUE_BLUR_QUALITY_HIGH);
        dsp2.setLayout(new BorderLayout());
        dsp2.setShadowFactory(factory2);
        dsp2.setDistance(5);
        dsp2.setAngle(90f);
        dsp2.add(foldersTree);
        foldersScrollPane.getViewport().setBackground(Color.WHITE);
        foldersScrollPane.setViewportView(dsp2);

        foldersCollapsiblePane.getContentPane().add(foldersScrollPane, BorderLayout.CENTER);

        floatablePanel.add(foldersCollapsiblePane, BorderLayout.CENTER);

        floatableTarget.add(floatablePanel, BorderLayout.WEST);

        contentPanel.setLayout(new BorderLayout());
        //contentPanel.add(topSeparator, BorderLayout.NORTH);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(floatableTarget);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 3, 0, 3));
        contentPanel.add(panel, BorderLayout.CENTER);

        mailMenu.setText("Mail");
        quitMenuItem.setText("Quit");
        quitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                quitMenuItemActionPerformed(evt);
            }
        });

        mailMenu.add(quitMenuItem);

        mainMenuBar.add(mailMenu);

        editMenu.setText("Edit");
        mainMenuBar.add(editMenu);

        viewMenu.setText("View");
        mainMenuBar.add(viewMenu);

        messageMenu.setText("Message");
        mainMenuBar.add(messageMenu);

        toolsMenu.setText("Tools");
        mainMenuBar.add(toolsMenu);

        helpMenu.setText("Help");
        mainMenuBar.add(helpMenu);

        setJMenuBar(mainMenuBar);

        mainToolBar = createMainToolBar();
        add(mainToolBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                Object src = ae.getSource();
                if (src == asFullListMI || src == asListMI || src == asTableMI) {
                    if (asFullListMI.isSelected()) {
                        showFullMessageList();
                    } else if (asListMI.isSelected()) {
                        showMessageList();
                    } else {
                        showMessageTable();
                    }
                } else if (src == coolbar || src == coolbuttons) {
                    updateToolBar();
                } else if (src == ds) {
                    updateBorders();
                } else if (src == updateTree) {
                    foldersTree.switchExtremeLevel();
                }
            }
        };

        updateTree.addActionListener(al);
        ds.addActionListener(al);
        coolbar.addActionListener(al);
        coolbuttons.addActionListener(al);
        asTableMI.setSelected(true);
        ButtonGroup viewBG = new ButtonGroup();
        viewBG.add(asTableMI);
        viewBG.add(asListMI);
        viewBG.add(asFullListMI);
        asFullListMI.addActionListener(al);
        asListMI.addActionListener(al);
        asTableMI.addActionListener(al);

        //pack();
        int height = 650;
        // Romain, tweak this test if it's still too big for your display size.
        if (Toolkit.getDefaultToolkit().getScreenSize().height > 1000) {
            height = 800;
        }
        setSize(900, height);
    }

    private void updateBorders() {
        updateBorder(mailTableScrollPane);
        updateBorder(messageViewScrollPane);
        updateBorder(foldersScrollPane);
    }

    private void updateBorder(JComponent comp) {
        if (ds.isSelected()) {
            comp.setBorder(BorderFactory.createCompoundBorder(new DropShadowBorder(Color.BLACK, 0, 5, .5f,
                    12, false, true, true, true), comp.getBorder()));
        } else {
            CompoundBorder border = (CompoundBorder)comp.getBorder();
            comp.setBorder(border.getInsideBorder());
        }
    }

    private JLabel createStatusBar() {
        if (coolbar.isSelected()) {
            return new StatusBar();
        }

        JLabel label = new JLabel("   You have 6 new messages...") {
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, super.getPreferredSize().height + 2);
            }
        };
        return label;
    }

    private void updateToolBar() {
        getContentPane().remove(mainToolBar);
        mainToolBar = createMainToolBar();
        getContentPane().add(mainToolBar, BorderLayout.NORTH);
        mainToolBar.revalidate();
        mainToolBar.repaint();

        getContentPane().remove(statusBar);
        statusBar = createStatusBar();
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        statusBar.revalidate();
        statusBar.repaint();
    }

    private JComponent createMainToolBar() {
        boolean cool = coolbar.isSelected();
        boolean coolButtons = coolbuttons.isSelected();
        JToolBar mainToolBar = cool ? new CoolBar() : new JToolBar();
        mainToolBar.setName("toolbar");
        mainToolBar.setFloatable(false);
        JComponent retComp;

        if (cool) {
            retComp = mainToolBar;
        } else {
            JPanel p = new JPanel(new BorderLayout());
            p.add(new JSeparator(), BorderLayout.NORTH);
            p.add(mainToolBar);
            p.add(new JSeparator(), BorderLayout.SOUTH);

            retComp = p;
        }

        JButton getMailButton = createToolbarButton("Get Mail", "get-mail", 1);
        getMailButton.setName("getMail");
        JButton composeButton = createToolbarButton("Compose", "compose-mail", 2);
        composeButton.setName("composeButton");
        composeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ComposeMailFrame frame = new ComposeMailFrame();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                sharedFrame = frame;
            }
        });
        JButton addressBookButton = createToolbarButton("Address Book", "address-book", 3);
        JButton replyButton = createToolbarButton("Reply", "reply", 1);
        JButton replyAllButton = createToolbarButton("Reply All", "reply-all", 2);
        JButton forwardButton = createToolbarButton("Forward", "forward", 3);
        JButton deleteButton = createToolbarButton("Delete", "delete", 1);
        JButton junkButton = createToolbarButton("Junk", "junk", 3);
        JButton printButton = createToolbarButton("Print", "print", 1);
        JButton stopButton = createToolbarButton("Stop", "stop", 3);
        stopButton.setName("stop");

        if (coolButtons) {
            DropShadowPanel dsp = createDropShadowPanel();
            dsp.setBorder(new javax.swing.border.EmptyBorder(2, 4, 6, 4));
            dsp.add(getMailButton);
            dsp.add(composeButton);
            dsp.add(addressBookButton);
            mainToolBar.add(dsp);

            dsp = createDropShadowPanel();
            dsp.setBorder(new javax.swing.border.EmptyBorder(2, 4, 6, 4));
            dsp.add(replyButton);
            dsp.add(replyAllButton);
            dsp.add(forwardButton);
            mainToolBar.add(dsp);

            dsp = createDropShadowPanel();
            dsp.setBorder(new javax.swing.border.EmptyBorder(2, 4, 6, 4));
            dsp.add(deleteButton);
            dsp.add(junkButton);
            mainToolBar.add(dsp);

            dsp = createDropShadowPanel();
            dsp.setBorder(new javax.swing.border.EmptyBorder(2, 4, 6, 20));
            dsp.add(printButton);
            dsp.add(stopButton);
            mainToolBar.add(dsp);
        } else {
            mainToolBar.add(getMailButton);
            mainToolBar.add(composeButton);
            mainToolBar.add(addressBookButton);
            mainToolBar.addSeparator();
            mainToolBar.add(replyButton);
            mainToolBar.add(replyAllButton);
            mainToolBar.add(forwardButton);
            mainToolBar.addSeparator();
            mainToolBar.add(deleteButton);
            mainToolBar.add(junkButton);
            mainToolBar.addSeparator();
            mainToolBar.add(printButton);
            mainToolBar.add(stopButton);
        }

        JPopupMenu popup = new JPopupMenu();
        popup.add(ds);
        popup.add(coolbar);
        popup.add(coolbuttons);
        popup.addSeparator();
        popup.add(updateTree);
        popup.addSeparator();
        popup.add(asTableMI);
        popup.add(asListMI);
        popup.add(asFullListMI);

        mainToolBar.setComponentPopupMenu(popup);
        return retComp;
    }

    private static DropShadowPanel createDropShadowPanel() {
        DropShadowPanel dsp = new DropShadowPanel();
        ShadowFactory factory = new ShadowFactory(5, 0.2f, Color.BLACK);
        dsp.setShadowFactory(factory);
        dsp.setDistance(5);
        dsp.setAngle(100f);
        dsp.setLayout(new BoxLayout(dsp, BoxLayout.X_AXIS));
        return dsp;
    }

    private JButton createToolbarButton(String text, String icon, int type) {
        boolean cool = coolbuttons.isSelected();
        JButton button = cool ? new CoolButton(type) : new JButton();
        button.setIcon(new ImageIcon(getClass().getResource("/resources/icons/" + icon + "_01.png")));
        button.setText(text);
        if (cool) {
            button.setBorder(BorderFactory.createEmptyBorder(4, 12, 3, 12));
        } else {
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 4, 10));
            if (coolbar.isSelected()) {
                button.setContentAreaFilled(false);
            }
        }
        button.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/" + icon + "_03.png")));
        button.setFocusPainted(false);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setRolloverIcon(new ImageIcon(getClass().getResource("/resources/icons/" + icon + "_02.png")));
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setRequestFocusEnabled(false);
        return button;
    }

    /** @noinspection UNUSED_SYMBOL*/
    private void quitMenuItemActionPerformed(ActionEvent evt) {
        dispose();
    }

    /** @noinspection UNUSED_SYMBOL*/
    private void closeSearchPanelActionPerformed(ActionEvent evt) {
        findCollapsiblePane.setCollapsed(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                messageView.requestFocusInWindow();
            }
        });
    }

    private void installInLayeredPane(JComponent component) {
        JLayeredPane layeredPane = getRootPane().getLayeredPane();
        layeredPane.add(component, JLayeredPane.PALETTE_LAYER, 20);
        Dimension size = component.getPreferredSize();
        component.setSize(size);
        component.setLocation((getWidth() - size.width) / 2,
                (getHeight() - size.height) / 2);
        component.revalidate();
        component.setVisible(true);
    }

////////////////////////////////////////////////////////////////////////////
// INNER CLASSES
////////////////////////////////////////////////////////////////////////////

    private class TreeDocker implements FocusListener {
        public void focusGained(FocusEvent e) {
        }

        public void focusLost(FocusEvent e) {
            Object status = floatablePanel.getClientProperty(
                    SetFloatableAction.FLOATABLE_STATUS);
            if (status == Boolean.TRUE) {
                floatablePanel.setVisible(false);
            }
        }
    }

    private class FloatingTreeFocusHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            foldersTree.requestFocusInWindow();
        }
    }

    private class DockedTreeButton implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!floatablePanel.isVisible()) {
                floatablePanel.setVisible(true);
                foldersTree.requestFocusInWindow();
            }
        }
    }

    private class GlobalKeyStrokeHandler implements AWTEventListener {
        private final Map<KeyStroke, Action> bindings = new HashMap<KeyStroke, Action>();

        public void eventDispatched(AWTEvent event) {
            Window window = SwingUtilities.getWindowAncestor((Component) event.getSource());
            if (window == MainFrame.this) {
                KeyEvent keyEvent = (KeyEvent) event;
                if (keyEvent.getID() == KeyEvent.KEY_RELEASED) {
                    KeyStroke typed = KeyStroke.getKeyStroke(keyEvent.getKeyCode(),
                            keyEvent.getModifiers());

                    for (Map.Entry<KeyStroke, Action> entry : bindings.entrySet()) {
                        if (typed.equals(entry.getKey())) {
                            ActionEvent e = new ActionEvent(MainFrame.this,
                                    event.getID(),
                                    "global_key_binding");
                            entry.getValue().actionPerformed(e);
                        }
                    }
                }
            }
        }

        public void putKeyBinding(KeyStroke keyStroke, Action action) {
            bindings.put(keyStroke, action);
        }

        public Action getActionByName(String name) {
            for (Action action : bindings.values()) {
                if (name.equals(action.getValue(Action.NAME))) {
                    return action;
                }
            }
            return null;
        }
    }

    Action getGlobalActionByName(String name) {
        return globalKeyListener.getActionByName(name);
    }

    private class ShowHideFoldersAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            Object status = floatablePanel.getClientProperty(
                    SetFloatableAction.FLOATABLE_STATUS);
            if (status == null || status == Boolean.FALSE) {
                boolean collapsed = foldersCollapsiblePane.isCollapsed();
                foldersCollapsiblePane.setCollapsed(!collapsed);
            }
        }
    }

    private class ShowHideSearchBarAction extends AbstractAction {
        ShowHideSearchBarAction() {
            super("firefox");
        }
        public void actionPerformed(ActionEvent e) {
            boolean collapsed = findCollapsiblePane.isCollapsed();
            findCollapsiblePane.setCollapsed(!collapsed);
            if (collapsed) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        try {
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    searchPanel.requestFocusInWindow();
                                }
                            });
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    searchPanel.transferFocus();
                                }
                            });
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    FocusManager.getCurrentManager().focusNextComponent();
                                }
                            });
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        } catch (InvocationTargetException ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        foldersTree.requestFocusInWindow();
                    }
                });
            }
        }
    }

    private class MessagePaneScrollAction extends AbstractAction {
        private final boolean down;

        MessagePaneScrollAction(boolean down) {
            this.down = down;
        }

        public void actionPerformed(ActionEvent e) {
            JScrollPane sp = (JScrollPane)messageView.getParent().getParent();
            String key = (down) ? "scrollDown" : "scrollUp";
            sp.getActionMap().get(key).actionPerformed(new ActionEvent(sp, 0, null));
        }
    }

    private class ShowSearchDialogAction extends AbstractAction {
        private final SearchDialogType type;

        public ShowSearchDialogAction(SearchDialogType type) {
            this.type = type;
        }

        public void actionPerformed(ActionEvent e) {
            if (type == SearchDialogType.CLASSIC) {
                Component c = (Component) e.getSource();
                SearchDialog searchDialog =
                        new SearchDialog(MainFrame.this, true);
                searchDialog.setLocationRelativeTo(c);
                searchDialog.setVisible(true);
            } else if (type == SearchDialogType.APPLE) {
            } else if (type == SearchDialogType.VISTA) {
                installInLayeredPane(new VistaSearchDialog(MainFrame.this));
            }
        }
    }

////////////////////////////////////////////////////////////////////////////
// MEMBERS/UI COMPONENTS
////////////////////////////////////////////////////////////////////////////

    private MessageListPanel listPanel;
    private MessageHeaderPanel messageHeaderPanel;
    private JXCollapsiblePane findCollapsiblePane;
    private JXPanel floatablePanel;
    private JPanel floatableTarget;
    private JXCollapsiblePane foldersCollapsiblePane;
    private MailBoxTree foldersTree;
    private MailTable mailTable;
    private MessagePane messageView;
    private JXSearchPanel searchPanel;
    private JPanel messagePanel;
//private JImagePanel imagePanel;
}
