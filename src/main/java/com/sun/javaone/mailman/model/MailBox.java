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
import com.sun.javaone.mailman.data.InputByteBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailBox {
    private static final Pattern contactPattern = Pattern.compile("(.+?) <([^>]+)>");
    private static final Matcher contactMatcher = contactPattern.matcher("");
    private static final int CHUNK_SIZE = 2048;
    private static final byte[] TMP_BUF = new byte[12];
    private static final StringBuilder stringBuilder = new StringBuilder(256);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            "EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
    private static final byte[] H_CC = "c:".getBytes();
    private static final byte[] H_DATE = "ate:".getBytes();
    private static final byte[] H_FROM = "rom:".getBytes();
    private static final byte[] H_MESSAGE_ID = "essage-id:".getBytes();
    private static final byte[] H_REFERENCES = "eferences:".getBytes();
    private static final byte[] H_SUBJECT = "ubject:".getBytes();
    private static final byte[] H_TO = "o:".getBytes();
    private InputByteBuffer buffer;



    public enum Type {
        FOLDER, INBOX, TRASH, SENT, DRAFTS, OTHER
    }
    private final File path;
    private String name;
    private final Type type;
    private MailBox parent;

    private final List<Message> messages = BindingCollections.observableList(new ArrayList<Message>());
    private final List<MailBox> folders = BindingCollections.observableList(new ArrayList<MailBox>());

    public MailBox(Type type, String name, MailBox parent,
            File path) throws IOException {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }

        this.name = name;
        this.type = type;
        this.parent = parent;
        this.path = path;
        loadMessages();
    }

    public void addMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null.");
        }
        messages.add(message);
    }

    public void removeMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null.");
        }
        messages.remove(message);
    }

    public void addFolder(MailBox folder) {
        if (folder == null || folder.getType() != Type.FOLDER) {
            throw new IllegalArgumentException(
                    "Folder must be non-null and a MailBox of" +
                    "type Folder.");
        }

        folders.add(folder);
    }

    public void removeFolder(MailBox folder) {
        if (folder == null || folder.getType() != Type.FOLDER) {
            throw new IllegalArgumentException(
                    "Folder must be non-null and a MailBox of" +
                    "type Folder.");
        }

        folders.remove(folder);
    }

    /**
     * Recursive find
     */
    public Message[] findAll() {
        List<Message> messages = new LinkedList<Message>(this.messages);
        for (MailBox mbox : folders) {
            Collections.addAll(messages, mbox.findAll());
        }
        return messages.toArray(new Message[0]);
    }

    /**
     * Recursive find
     */
    public Message[] findBySender(String sender) {
        List<Message> messages = new LinkedList<Message>();
        for (Message message : this.messages) {
            Contact author = message.getFrom();
            if (author.getDisplayName().contains(sender) ||
                    author.getFirstName().contains(sender) ||
                    author.getLastName().contains(sender) ||
                    author.getAddress().contains(sender)) {
                messages.add(message);
            }
        }

        for (MailBox mbox : folders) {
            Collections.addAll(messages, mbox.findBySender(sender));
        }
        return messages.toArray(new Message[0]);
    }

    /**
     * Recursive find
     */
    public Message[] findByRecipient(String recipient) {
        List<Message> messages = new LinkedList<Message>();
        for (Message message : this.messages) {
            for (Contact contact : message.getTo()) {
                if (contact.getDisplayName().contains(recipient) ||
                        contact.getFirstName().contains(recipient) ||
                        contact.getLastName().contains(recipient) ||
                        contact.getAddress().contains(recipient)) {
                    messages.add(message);
                }
            }
        }

        for (MailBox mbox : folders) {
            Collections.addAll(messages, mbox.findByRecipient(recipient));
        }
        return messages.toArray(new Message[0]);
    }

    /**
     * Recursive find
     */
    public Message[] findBySubject(String subject) {
        List<Message> messages = new LinkedList<Message>();
        for (Message message : this.messages) {
            if (message.getSubject().contains(subject)) {
                messages.add(message);
            }
        }

        for (MailBox mbox : folders) {
            Collections.addAll(messages, mbox.findBySubject(subject));
        }
        return messages.toArray(new Message[0]);
    }

    /**
     * Recursive find
     */
    public Message[] findByContent(String content) {
        List<Message> messages = new LinkedList<Message>();
        for (Message message : this.messages) {
            if (message.getBody().contains(content)) {
                messages.add(message);
            }
        }

        for (MailBox mbox : folders) {
            Collections.addAll(messages, mbox.findByContent(content));
        }
        return messages.toArray(new Message[0]);
    }

    /**
     * Used to reparent a mailbox in the tree view
     */
    public void setParent(MailBox parent) {
        this.parent = parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public MailBox getParent() {
        return parent;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<MailBox> getFolders() {
        return folders;
    }

    @Override
            public String toString() {
        return getName() + " [" + " messages=" + messages +
                " folder=" + folders +
                "]";
    }

    private void loadMessages() throws IOException {
        // Search for starting with ^From
        // Header separated with a newline
        long startTime = System.currentTimeMillis();
        FileChannel channel = new FileInputStream(path).getChannel();
        ByteBuffer bBuffer = ByteBuffer.allocateDirect(CHUNK_SIZE);
        buffer = new InputByteBuffer(channel, bBuffer);
        int start = 0;
        if (!buffer.atEnd()) {
            int count = 0;
            do {
                Message message = new Message(this, start);
                parseHeader(buffer, message);
                int headerEnd = (int)(bBuffer.position() + channel.position() - CHUNK_SIZE);
                parseToNextMessage(buffer);
                int end = (int)(bBuffer.position() + channel.position() -
                        CHUNK_SIZE);
                start = end;
                addMessage(message);
                count++;
            } while (!buffer.atEnd());
        }
        long endTime = System.currentTimeMillis();
    }

    private boolean parseHeader(InputByteBuffer buffer, Message message) throws IOException {
        while (true) {
            switch(buffer.get()) {
                case 'C':
                    if (headerEquals(H_CC, buffer)) {
                        message.setCc(getCC(buffer));
                    }
                    break;
                case 'D':
                    if (headerEquals(H_DATE, buffer)) {
                        message.setDate(getDate(buffer));
                    }
                    break;
                case 'F':
                    if (headerEquals(H_FROM, buffer)) {
                        message.setFrom(getFrom(buffer));
                    }
                    break;
                case 'M':
                    if (headerEquals(H_MESSAGE_ID, buffer)) {
                        message.setID(getID(buffer));
                    }
                    break;
                case 'R':
                    if (headerEquals(H_REFERENCES, buffer)) {
                        message.setReferences(getReferences(buffer));
                    }
                    break;
                case 'S':
                    if (headerEquals(H_SUBJECT, buffer)) {
                        message.setSubject(getSubject(buffer));
                    }
                    break;
                case 'T':
                    if (headerEquals(H_TO, buffer)) {
                        message.setTo(getTo(buffer));
                    }
                    break;
                case '\r':
                    if (buffer.atEnd()) {
                        return true;
                    }
                    if (buffer.get() == '\n') {
                        return true;
                    }
                    buffer.rewind(1);
                    break;
                default:
                    while (!buffer.atEnd() && buffer.get() != '\n');
                    if (buffer.atEnd()) {
                        return false;
                    }
                    break;
            }
        }
    }

    private boolean isEndOfHeader(InputByteBuffer buffer) throws IOException {
        if (buffer.atEnd()) {
            return true;
        }
        int delta = 0;
        if (buffer.get() == '\n') {
            if (buffer.atEnd()) {
                return true;
            }
            if (buffer.get() == '\r') {
                if (buffer.atEnd()) {
                    return true;
                }
                if (buffer.get() == '\n') {
                    return true;
                }
                delta++;
            } else {
                delta++;
            }
        } else {
            delta++;
        }
        buffer.rewind(delta);
        return false;
    }

    private boolean parseToNextMessage(InputByteBuffer buffer) throws IOException {
        while (!buffer.atEnd()) {
            if (buffer.get() == '\r') {
                if (!buffer.atEnd() && buffer.get() == '\n') {
                    if (!buffer.atEnd() && buffer.get() == 'F') {
                        if (!buffer.atEnd() && buffer.get() == 'r') {
                            if (!buffer.atEnd() && buffer.get() == 'o') {
                                if (!buffer.atEnd() && buffer.get() == 'm') {
                                    if (!buffer.atEnd() && buffer.get() == ' ') {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                buffer.rewind(1);
            }
//            int size = buffer.get(TMP_BUF, 0, 7);
//            if (size != 7) {
//                return false;
//            }
//            if (TMP_BUF[0] == '\r' &&
//                    TMP_BUF[1] == '\n' &&
//                    TMP_BUF[2] == 'F' &&
//                    TMP_BUF[3] == 'r' &&
//                    TMP_BUF[4] == 'o' &&
//                    TMP_BUF[5] == 'm' &&
//                    TMP_BUF[6] == ' ') {
//                return true;
//            }
//            buffer.rewind(6);
        }
        return false;
    }

    private String getHeaderString(InputByteBuffer buffer) throws IOException {
        StringBuilder builder = stringBuilder;
        builder.setLength(0);
        byte b;
        buffer.get();
        for (;;) {
            b = buffer.get();
            if (b == '\r') {
                b = buffer.get();
                if (b == '\n') {
                    b = buffer.get();
                    if (b == ' ' || b == '\t') {
                        while (b == ' ' || b == '\t') {
                            b = buffer.get();
                        }
                        builder.append(' ');
                        builder.append((char)b);
                    } else {
                        buffer.rewind(1);
                        break;
                    }
                } else {
                    builder.append((char)b);
                }
            } else {
                builder.append((char)b);
            }
        }
        return builder.toString();
    }

    private void skipHeader() throws IOException {
        for (;;) {
            if (buffer.get() == '\r' &&isEndOfHeader(buffer)) {
                return;
            }
        }
    }

    String getBody(Message message) throws IOException {
        buffer.setChannelPosition(message.getPosition());
        skipHeader();
        int start = (int)buffer.getChannelPosition();
        parseToNextMessage(buffer);
        int end = (int)buffer.getChannelPosition();
        if (!buffer.atEnd()) {
            end -= 8;
        } else {
            end--;
        }
        buffer.setChannelPosition(start);
        StringBuilder builder = new StringBuilder(end - start);
        for (int i = 0; i < (end - start); i++) {
            builder.append((char)buffer.get());
        }
        return builder.toString();
    }

    private boolean headerEquals(byte[] data, InputByteBuffer buffer) throws IOException {
        if (buffer.get(TMP_BUF, 0, data.length) != data.length) {
            return false;
        }
        for (int i = data.length - 1; i >= 0; i--) {
            if (TMP_BUF[i] != data[i]) {
                buffer.rewind(data.length - 1);
                while (buffer.get() != '\n');
                return false;
            }
        }
        return true;
    }

    private Contact[] getCC(InputByteBuffer buffer) throws IOException {
        String ccString = getHeaderString(buffer);
        Contact[] cc = getContacts(ccString);
        return cc;
    }

    private long getDate(InputByteBuffer buffer) throws IOException {
        String dateString = getHeaderString(buffer);
        try {
            return DATE_FORMAT.parse(dateString).getTime();
        } catch (ParseException e) {
            System.err.println("datestring=" + dateString + "!");
            e.printStackTrace();
            System.exit(0);
        }
        return 0;
    }

    private Contact getFrom(InputByteBuffer buffer) throws IOException {
        String from = getHeaderString(buffer);
        return getContact(from);
    }

    private String getID(InputByteBuffer buffer) throws IOException {
        String id = getHeaderString(buffer);
        return id;
    }

    private String[] getReferences(InputByteBuffer buffer) throws IOException {
        String refs = getHeaderString(buffer);
        return split(refs);
    }

    private String getSubject(InputByteBuffer buffer) throws IOException {
        String subject = getHeaderString(buffer);
        return subject;
    }

    private Contact[] getTo(InputByteBuffer buffer) throws IOException {
        String to = getHeaderString(buffer);
        return getContacts(to);
    }

    private Contact[] getContacts(String string) {
        String[] names = split(string);
        Contact[] contacts = new Contact[names.length];
        for (int i = 0; i < contacts.length; i++) {
            contacts[i] = getContact(names[i]);
        }
        return contacts;
    }

    private Contact getContact(String name) {
        contactMatcher.reset(name);
        if (contactMatcher.matches() && contactMatcher.groupCount() == 2) {
            String displayName = contactMatcher.group(1);
            String firstName = "";
            String lastName = "";
            String[] parts = displayName.split(" ", 2);
            if (parts.length == 2) {
                firstName = parts[0];
                lastName = parts[1];
            }

            return Contact.getContact(firstName, lastName, displayName,
                    contactMatcher.group(2));
        }
        return Contact.getContact(null, null, name, name);
    }

    private String[] split(String text) {
        return text.split(",\\w+");
    }

}
