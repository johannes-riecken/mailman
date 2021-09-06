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
package test;

import com.sun.javaone.mailman.data.DataAccessObjectProvider;
import com.sun.javaone.mailman.data.AccountDataAccessObject;
import com.sun.javaone.mailman.data.AddressBookDataAccessObject;
import com.sun.javaone.mailman.data.AccountListDataAccessObject;
import com.sun.javaone.mailman.model.Account;
import com.sun.javaone.mailman.model.AddressBook;
import com.sun.javaone.mailman.model.Contact;
import com.sun.javaone.mailman.model.Message;
import com.sun.javaone.mailman.model.MailBox;

public class DataAccessTest {
    private DataAccessTest() {
    }

    public static void main(String[] args) {
        DataAccessObjectProvider daoProvider = DataAccessObjectProvider.newInstance();
        AccountListDataAccessObject accountListDao =
                daoProvider.getAccountListDataAccessObject();
        AccountDataAccessObject accountDao =
                daoProvider.getAccountDataAccessObject();
        AddressBookDataAccessObject bookDao =
                daoProvider.getAddressBookDataAccessObject();

        String[] accountNames = accountListDao.getAccountNames();
        for (String name : accountNames) {
            printAccount(name, accountDao, bookDao);
        }
    }

    private static void printAccount(String name,
                                     AccountDataAccessObject accountDao,
                                     AddressBookDataAccessObject bookDao) {
        System.out.println("=================================================");
        System.out.println("Account: " + name);
        System.out.println("=================================================");
        System.out.println("");

        Account account = accountDao.getAccount(name);
        System.out.println("Account: ");
        System.out.println("\tName: " + account.getName());
        System.out.println("\tIdentity: " + account.getIdentity());
        System.out.println("\tSend: " + account.getSendMailServer());
        System.out.println("\tGet: " + account.getGetMailServer());
        System.out.println("");
        AddressBook book = bookDao.getAddressBook(account);
        System.out.println("Address book: ");
        for (Contact contact : book.findAll()) {
            System.out.println("\t" + contact);
        }
        System.out.println("");
        System.out.println("Find contacts @*.gov: ");
        for (Contact contact : book.find(".gov")) {
            System.out.println("\t" + contact);
        }
        System.out.println("");
        System.out.println("Find contacts called Scott: ");
        for (Contact contact : book.findByFirstName("Scott")) {
            System.out.println("\t" + contact);
        }
        System.out.println("");
        printMailBox(account.getInbox());
        System.out.println("");
        printMailBox(account.getTrash());
        System.out.println("");
        printMailBox(account.getSent());
        System.out.println("");
        printMailBox(account.getDrafts());
        System.out.println("");
        System.out.println("Find inbox mail sent to Chet: ");
        for (Message message : account.getInbox().findByRecipient("Chet")) {
            System.out.println("\t" + message);
        }
        System.out.println("");
        System.out.println("Find inbox mail sent by Jeff Dinkins: ");
        for (Message message : account.getInbox().findBySender("Dinkins")) {
            System.out.println("\t" + message);
        }
        System.out.println("");
        System.out.println("Find inbox mail with the reply prefix: ");
        for (Message message : account.getInbox().findBySubject("Re: ")) {
            System.out.println("\t" + message);
        }
        System.out.println("");
    }

    private static void printMailBox(MailBox mbox) {
        System.out.println(mbox);
        printMailBox(mbox, 1);
    }

    private static void printMailBox(MailBox mbox, int level) {
        System.out.println(repeatString("\t", level) + "Messages:");
        for (Message message : mbox.getMessages()) {
            System.out.print(repeatString("\t", level + 1));
            System.out.println(message);
        }

        System.out.println(repeatString("\t", level) + "Folders:");
        for (MailBox folder : mbox.getFolders()) {
            System.out.println(repeatString("\t", level + 1) + folder);
            printMailBox(folder, level + 2);
        }
    }

    private static String repeatString(String value, int count) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < count; i++) {
            buffer.append(value);
        }
        return buffer.toString();
    }
}
