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

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Date;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class Message {
    private MailBox mailBox;
    private int position;
    private String id;
    private Contact from;
    private Contact[] to;
    private Contact[] cc;
    private String subject;
    private long date;
    private String[] refs;
    private SoftReference<String> bodyRef;
    private boolean read;

    public Message(MailBox mailBox, int position) {
        this.mailBox = mailBox;
        this.position = position;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isRead() {
        return read;
    }

    int getPosition() {
        return position;
    }

    public String toString() {
        return "Message [" +
                "id=" + id +
                "read=" + read+
                " subject=" + subject +
                " from=" + from +
                " to=" + ((to == null) ? "" : Arrays.asList(to)) +
                " cc=" + ((cc == null) ? "" : Arrays.asList(cc)) +
                " refs=" + ((refs == null) ? "" : Arrays.asList(refs)) +
                " date=" + new Date(date) + "]";
    }

//    public Message(Contact from, Contact[] to, Contact[] cc, String subject,
//                   String body, Attachment[] attachment,
//                   Map<String, String> headers, Date dateTime) {
//        if (from == null) {
//            throw new IllegalArgumentException("Sender cannot be null.");
//        } else if (to == null || to.length == 0) {
//            throw new IllegalArgumentException("One recipient is required.");
//        }
//        this.from = from;
//        this.to = to;
//        this.cc = cc;
//        this.subject = subject == null ? "" : subject;
//        this.body = body == null ? "" : body;
//        this.attachment = attachment;
//        this.headers = headers == null ? new HashMap<String, String>() : headers;
//        if (dateTime == null) {
//            dateTime = new Date();
//        }
//        this.dateTime = (Date) dateTime.clone();
//    }

    public long getDateTime() {
        return date;
    }

    public Map<String, String> getHeaders() {
        // PENDING:
        throw new RuntimeException("implement me");
//        return null;
    }

    /**
     * Returns a clone of the contact
     */
    public Contact getFrom() {
        return from;
    }

    /**
     * Returns a clone of the contacts
     */
    public Contact[] getTo() {
        if (to == null) {
            return to;
        }
        return to.clone();
    }

    /**
     * Returns a clone of the contacts
     */
    public Contact[] getCc() {
        if (cc == null) {
            return null;
        }
        return cc.clone();
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        String body;
        if (bodyRef == null || (body = bodyRef.get()) == null) {
            try {
                body = mailBox.getBody(this);
            } catch (IOException ex) {
                ex.printStackTrace();
                body = "";
            }
            bodyRef = new SoftReference<String>(body);
        }
        return body;
    }

    public Attachment[] getAttachment() {
        throw new RuntimeException("attachment");
//        return null;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//        final Message message = (Message) o;
//        if (!Arrays.equals(attachment, message.attachment)) {
//            return false;
//        }
//        if (body != null ? !body.equals(message.body) : message.body != null) {
//            return false;
//        }
//        if (!Arrays.equals(cc, message.cc)) {
//            return false;
//        }
//        if (dateTime != null ? !dateTime.equals(message.dateTime) :
//            message.dateTime != null) {
//            return false;
//        }
//        if (from != null ? !from.equals(message.from) : message.from != null) {
//            return false;
//        }
//        if (subject != null ? !subject.equals(message.subject) :
//            message.subject != null) {
//            return false;
//        }
//        return Arrays.equals(to, message.to);
//    }
//
//    @Override
//    public int hashCode() {
//        int result;
//        result = (from != null ? from.hashCode() : 0);
//        result = 29 * result + (subject != null ? subject.hashCode() : 0);
//        result = 29 * result + (body != null ? body.hashCode() : 0);
//        result = 29 * result + (dateTime != null ? dateTime.hashCode() : 0);
//        return result;
//    }

    void setCc(Contact[] contact) {
        this.cc = contact;
    }

    void setDate(long date) {
        this.date = date;
    }

    void setFrom(Contact from) {
        this.from = from;
    }

    void setID(String id) {
        this.id = id;
    }

    void setReferences(String[] refs) {
        this.refs = refs;
    }

    void setSubject(String subject) {
        this.subject = subject;
    }

    void setTo(Contact[] to) {
        this.to = to;
    }
}
