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

import com.sun.javaone.mailman.model.Account;
import com.sun.javaone.mailman.model.MailBox;
import com.sun.javaone.mailman.model.Message;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author sky
 */
public final class UIController {
    private final PropertyChangeSupport changeSupport;
    private Account account;
    private MailBox selectedMailBox;
    private Message selectedMessage;

    UIController() {
        changeSupport = new PropertyChangeSupport(this);
    }
    
    public void setSelectedElement(Object selectedElement) {
        if (selectedElement instanceof MailBox) {
            setSelectedMailBox((MailBox)selectedElement);
        } else {
            setSelectedMailBox(null);
        }
    }
    
    public Object getSelectedElement() {
        return getSelectedMailBox();
    }
    
    public void setAccount(Account account) {
        Account lastAccount = this.account;
        this.account = account;
        firePropertyChange("account", lastAccount, account);
    }
    
    public Account getAccount() {
        return account;
    }
    
    public void setSelectedMailBox(MailBox mailBox) {
        MailBox lastMailBox = this.selectedMailBox;
        this.selectedMailBox = mailBox;
        firePropertyChange("selectedMailBox", lastMailBox, mailBox);
        setSelectedMessage(null);
    }
    
    public MailBox getSelectedMailBox() {
        return selectedMailBox;
    }
    
    public void setSelectedMessage(Message message) {
        Message lastMessage = this.selectedMessage;
        this.selectedMessage = message;
        firePropertyChange("selectedMessage", lastMessage, message);
    }
    
    public Message getSelectedMessage() {
        return selectedMessage;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }
    
    public void addPropertyChangeListener(String key,
            PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(key, l);
    }
    
    public void removePropertyChangeListener(String key,
            PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(key, l);
    }
    
    protected void firePropertyChange(String key, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(key, oldValue, newValue);
    }
}
