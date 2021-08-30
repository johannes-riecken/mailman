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

package com.sun.javaone.mailman.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.javaone.mailman.model.Account;
import com.sun.javaone.mailman.model.AddressBook;
import com.sun.javaone.mailman.model.Attachment;
import com.sun.javaone.mailman.model.Contact;
import com.sun.javaone.mailman.model.MailBox;
import com.sun.javaone.mailman.model.MailServer;
import com.sun.javaone.mailman.model.Message;

class PlainTextDataAccessObjectProvider extends DataAccessObjectProvider {
    private static final String MAIL_DIRECTORY =
            System.getProperty("user.dir") + File.separator + ".mailman";
    // For profiling
//            "c:\\sky\\dev\\mailman\\mailman\\.mailman"
    
    @Override
            public AccountListDataAccessObject getAccountListDataAccessObject() {
        return new PlainTextAccountListDataAccessObject();
    }
    
    @Override
            public AccountDataAccessObject getAccountDataAccessObject() {
        return new PlainTextAccountDataAccessObject();
    }
    
    @Override
            public AddressBookDataAccessObject getAddressBookDataAccessObject() {
        return new PlainTextAddressBookDataAccessObject();
    }
    
    private static final class PlainTextAccountListDataAccessObject implements
            AccountListDataAccessObject {
        public String[] getAccountNames() {
            File accountListFile = new File(MAIL_DIRECTORY, "accountlist");
            List<String> accounts = new ArrayList<String>();
            try {
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(
                        new FileInputStream(accountListFile)));
                String line;
                while ((line = in.readLine()) != null) {
                    accounts.add(line);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return accounts.toArray(new String[0]);
        }
    }
    
    private static final class PlainTextAccountDataAccessObject implements
            AccountDataAccessObject {
        private static final Pattern contactPattern = Pattern.compile("(.+?) <([^>]+)>");
        private static final Matcher contactMatcher = contactPattern.matcher("");
        private static final Map<String,Contact> contacts = new HashMap<String,Contact>();
        private static final Map<String,MailBox.Type> typeMap = new HashMap<String,MailBox.Type>();
        
        static {
            for (MailBox.Type type : MailBox.Type.values()) {
                typeMap.put(type.toString().toLowerCase(), type);
            }
        }
        
        public Account getAccount(String name) {
            File accountFile = new File(MAIL_DIRECTORY + File.separator + name,
                    "account");
            return loadAccount(name, accountFile);
        }
        
        private static Account loadAccount(String name, File accountFile) {
            try {
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(
                        new FileInputStream(accountFile)));
                
                Contact identity = loadIdentity(in);
                MailServer sendMailServer = loadMailServer(in);
                MailServer getMailServer = loadMailServer(in);
                
                MailBox inbox = loadMailBox(name, in);
                MailBox trash = loadMailBox(name, in);
                MailBox sent = loadMailBox(name, in);
                MailBox drafts = loadMailBox(name, in);
                
                List<MailBox> mboxs = new LinkedList<MailBox>();
                MailBox mbox;
                while ((mbox = loadMailBox(name, in)) != null) {
                    mboxs.add(mbox);
                }
                in.close();
                
                return new Account(name, getMailServer, sendMailServer, identity,
                        inbox, trash, sent, drafts, mboxs);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        
        
        
        private static MailServer loadMailServer(BufferedReader in) throws IOException {
            String[] parts = in.readLine().split("\t");
            return new MailServer(MailServer.Protocol.valueOf(parts[0]),
                    parts[1], parts[2], parts[3], parts[4]);
        }
        
        private static Contact loadIdentity(BufferedReader in) throws IOException {
            String[] parts = in.readLine().split("\t");
            return Contact.getContact(parts[0], parts[1], parts[2], parts[3]);
        }
        
        private static MailBox loadMailBox(String accountName,
                BufferedReader in) throws IOException {
            String mboxName = in.readLine();
            if (mboxName == null) {
                return null;
            }
            return loadMailBox(accountName, mboxName, false);
        }
        
        private static MailBox loadMailBox(String accountName,
                String mboxName,
                boolean asFolder) throws IOException {
            MailBox.Type type = asFolder ? MailBox.Type.FOLDER :
                typeMap.get(mboxName.toLowerCase());
            System.err.println("mboxName=" + mboxName + "type=" + type);
            File messagesFile =
                    new File(MAIL_DIRECTORY + File.separator + accountName,
                    mboxName + ".mbox");
            if (type == null) {
                type = MailBox.Type.OTHER;
            }
            System.err.println("loading " + mboxName);
            MailBox mailBox = new MailBox(type, mboxName, null, messagesFile);
            loadFolders(accountName, mailBox);
            return mailBox;
        }
        
        private static void loadFolders(String accountName, MailBox mailBox)
        throws IOException {
            File foldersFile =
                    new File(MAIL_DIRECTORY + File.separator + accountName,
                    mailBox.getName() + ".folders");
            if (foldersFile.exists()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        new FileInputStream(foldersFile)));
                
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.trim().length() > 0) {
                        MailBox folder = loadMailBox(
                                accountName + File.separator + line, line, true);
                        folder.setParent(mailBox);
                        mailBox.addFolder(folder);
                    }
                }
                
                in.close();
            }
        }
        
        private static void loadMessage(String accountName, MailBox mailBox)
        throws IOException {
            File messagesFile =
                    new File(MAIL_DIRECTORY + File.separator + accountName,
                    mailBox.getName() + ".mbox");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(messagesFile)));
            
            Message nextMessage;
            while ((nextMessage = loadMessage(in)) != null) {
                mailBox.addMessage(nextMessage);
            }
            
            in.close();
        }
        
        private static Message loadMessage(BufferedReader in) throws IOException {
            if (!in.ready()) {
                return null;
            }
            
            Map<String, String> headers = parseHeaders(in);
            String body = parseBody(in);
            String subject = headers.get("Subject");
            Contact from = parseContact(headers.get("From"));
            Contact[] to = parseContacts(headers.get("To"));
            Contact[] cc = parseContacts(headers.get("Cc"));
            Attachment[] attachments = new Attachment[0];
            Date date = parseDate(headers.get("Date"));
            
            return null;
//            return new Message(from, to, cc, subject, body, attachments,
//                               headers, date);
        }
        
        private static Date parseDate(String value) {
            if (value == null) {
                return new Date();
            }
            
            try {
                return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z",
                        Locale.ENGLISH).parse(value);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
        
        private static Contact[] parseContacts(String value) {
            if (value == null) {
                return new Contact[0];
            }
            
            List<Contact> contacts = new LinkedList<Contact>();
            for (String contactValue : value.split(",\\w+")) {
                contacts.add(parseContact(contactValue));
            }
            return contacts.toArray(new Contact[0]);
        }
        
        private static Contact parseContact(String value) {
            if (value == null) {
                return null;
            }
            
            contactMatcher.reset(value);
            if (contactMatcher.matches() && contactMatcher.groupCount() == 2) {
                String displayName = contactMatcher.group(1);
                String firstName = "";
                String lastName = "";
                String[] parts = displayName.split(" ", 2);
                if (parts.length == 2) {
                    firstName = parts[0];
                    lastName = parts[1];
                }
                
                return getContact(firstName, lastName, displayName,
                        contactMatcher.group(2));
            }
            return null;
        }
        
        private static Contact getContact(String firstName, String lastName,
                String displayName, String address) {
            return null;
//            Contact contact = contacts.get(address);
//            if (contact == null) {
//                contact = new Contact(firstName, lastName, displayName,
//                        address);
//                contacts.put(address, contact);
//            }
//            return contact;
        }
        
        private static String parseBody(BufferedReader in) throws IOException {
            StringBuilder body = new StringBuilder();
            
            String line;
            String lastLine = null;
            while ((line = in.readLine()) != null) {
                if (lastLine == null || lastLine.trim().length() == 0) {
                    if (line.matches("From - .+")) {
                        break;
                    }
                }
                
                body.append(line).append('\n');
                lastLine = line;
            }
            
            return body.toString();
        }
        
        private static Map<String, String> parseHeaders(BufferedReader in) throws
                IOException {
            String line;
            StringBuilder buffer = new StringBuilder();
            Map<String, String> headers = new HashMap<String, String>();
            
            // headers
            while ((line = in.readLine()) != null) {
                if (line.trim().length() == 0) {
                    break;
                }
                
                if (line.charAt(0) == ' ' || line.charAt(0) == '\t') {
                    buffer.append(line);
                } else {
                    if (buffer.length() > 0) {
                        String header = buffer.toString();
                        String[] pair = header.split(": ", 2);
                        headers.put(pair[0], pair[1]);
                    }
                    
                    buffer = new StringBuilder();
                    buffer.append(line);
                }
            }
            
            if (buffer.length() > 0) {
                String header = buffer.toString();
                String[] pair = header.split(": ", 2);
                headers.put(pair[0], pair[1]);
            }
            
            return headers;
        }
    }
    
    private static final class PlainTextAddressBookDataAccessObject implements
            AddressBookDataAccessObject {
        public AddressBook getAddressBook(Account account) {
            File addressBookFile = new File(
                    MAIL_DIRECTORY + File.separator + account.getName(),
                    "addressbook");
            return loadAddressBook(addressBookFile);
        }
        
        private static AddressBook loadAddressBook(File addressBookFile) {
            AddressBook book = new AddressBook();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        new FileInputStream(addressBookFile)));
                
                String line;
                while ((line = in.readLine()) != null) {
                    String[] parts = line.split("\t");
                    book.add(Contact.getContact(parts[0], parts[1], parts[2], parts[3]));
                }
                
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return book;
        }
    }
}
