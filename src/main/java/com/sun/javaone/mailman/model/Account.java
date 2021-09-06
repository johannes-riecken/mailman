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

package com.sun.javaone.mailman.model;

import binding.collections.BindingCollections;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private String name;
    private MailServer getMailServer;
    private MailServer sendMailServer;
    private Contact identity;

    private final MailBox inbox;
    private final MailBox trash;
    private final MailBox sent;
    private final MailBox drafts;
    private final List<MailBox> mboxs;

    public Account(String name, MailServer getMailServer,
                   MailServer sendMailServer, Contact identity,
                   MailBox inbox, MailBox trash, MailBox sent, MailBox drafts,
            List<MailBox> otherMBoxs) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        } else if (getMailServer == null) {
            throw new IllegalArgumentException(
                    "Server to receive mail cannot be null");
        } else if (sendMailServer == null) {
            throw new IllegalArgumentException(
                    "Server to send mail cannot be null");
        } else if (sendMailServer.getProtocol() != MailServer.Protocol.SMTP) {
            throw new IllegalArgumentException(
                    "Server to send mail must support SMTP protocol");
        } else if (identity == null) {
            throw new IllegalArgumentException(
                    "Account identity cannot be null.");
        } else if (inbox == null || inbox.getType() != MailBox.Type.INBOX) {
            throw new IllegalArgumentException("Inbox must be non-null and of" +
                                               "type INBOX");
        } else if (trash == null || trash.getType() != MailBox.Type.TRASH) {
            throw new IllegalArgumentException("Trash must be non-null and of" +
                                               "type TRASH");
        } else if (sent == null || sent.getType() != MailBox.Type.SENT) {
            throw new IllegalArgumentException("Sent must be non-null and of" +
                                               "type SENT");
        } else if (drafts == null || drafts.getType() != MailBox.Type.DRAFTS) {
            throw new IllegalArgumentException("Drafts must be non-null and of" +
                                               "type DRAFTS");
        }

        this.name = name;
        this.getMailServer = getMailServer;
        this.sendMailServer = sendMailServer;
        this.identity = identity;
        this.inbox = inbox;
        this.trash = trash;
        this.sent = sent;
        this.drafts = drafts;
        List<MailBox> mboxs = new ArrayList<MailBox>(4);
        mboxs = BindingCollections.observableList(mboxs);
        mboxs.add(inbox);
        mboxs.add(trash);
        mboxs.add(sent);
        mboxs.add(drafts);
        if (otherMBoxs != null) {
            mboxs.addAll(otherMBoxs);
        }
        this.mboxs = mboxs;
    }

    public List<MailBox> getMailBoxs() {
        return mboxs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }

        this.name = name;
    }

    public MailServer getGetMailServer() {
        return getMailServer;
    }

    public void setGetMailServer(MailServer getMailServer) {
        if (getMailServer == null) {
            throw new IllegalArgumentException(
                    "Server to receive mail cannot be null");
        }

        this.getMailServer = getMailServer;
    }

    public MailServer getSendMailServer() {
        return sendMailServer;
    }

    public void setSendMailServer(MailServer sendMailServer) {
        if (sendMailServer == null) {
            throw new IllegalArgumentException(
                    "Server to send mail cannot be null");
        } else if (sendMailServer.getProtocol() != MailServer.Protocol.SMTP) {
            throw new IllegalArgumentException(
                    "Server to send mail must support SMTP protocol");
        }

        this.sendMailServer = sendMailServer;
    }

    public Contact getIdentity() {
        return identity;
    }

    public void setIdentity(Contact identity) {
        this.identity = identity;
    }

    public MailBox getInbox() {
        return inbox;
    }

    public MailBox getTrash() {
        return trash;
    }

    public MailBox getSent() {
        return sent;
    }

    public MailBox getDrafts() {
        return drafts;
    }

    @Override
    public String toString() {
        return getName();
    }
}
