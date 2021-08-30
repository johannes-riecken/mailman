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


package com.sun.javaone.mailman;

import java.awt.EventQueue;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import binding.swing.SwingBindingSupport;
import com.sun.javaone.mailman.data.AccountDataAccessObject;
import com.sun.javaone.mailman.data.AccountListDataAccessObject;
import com.sun.javaone.mailman.data.DataAccessObjectProvider;
import com.sun.javaone.mailman.model.Account;
import com.sun.javaone.mailman.ui.MainFrame;

/**
 *
 * @author Romain Guy <romain.guy@mac.com>
 */
public class Application {
    private static MainFrame MAIN_FRAME;

    private Application() {
    }

    public synchronized static MainFrame getMainFrame() {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Do not attempt to access the " +
                                            "main frame outside of the EDT.");
        }

        if (MAIN_FRAME == null) {
            MAIN_FRAME = new MainFrame();
        }

        return MAIN_FRAME;
    }
    
    public static Account[] getAccounts() {
        DataAccessObjectProvider daoProvider = DataAccessObjectProvider.newInstance();
        AccountListDataAccessObject accountListDao =
                daoProvider.getAccountListDataAccessObject();
        AccountDataAccessObject accountDao =
                daoProvider.getAccountDataAccessObject();
        
        List<Account> accounts = new LinkedList<Account>();
        for (String name : accountListDao.getAccountNames()) {
            accounts.add(accountDao.getAccount(name));
        }

        return accounts.toArray(new Account[0]);
    }
    
    public static void main(String[] args) {
        SwingBindingSupport.register();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (InstantiationException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }

                getMainFrame().setVisible(true);
                getMainFrame().runDemo();
            }
        });
    }
}
