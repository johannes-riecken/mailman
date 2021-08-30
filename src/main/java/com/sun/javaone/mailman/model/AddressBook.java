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

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;

public class AddressBook {
    // prevents from having duplicates
    private final Set<Contact> contacts = new HashSet<Contact>();

    public void add(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null.");
        }

        contacts.add(contact);
    }

    public void removeAll() {
        contacts.clear();
    }

    public void remove(Contact[] contacts) {
        if (contacts == null) {
            throw new IllegalArgumentException("Contact list cannot be null.");
        }

        for (Contact contact : contacts) {
            remove(contact);
        }
    }

    public void remove(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null.");
        }

        contacts.remove(contact);
    }

    public int getContactsCount() {
        return contacts.size();
    }

    public Contact[] findAll() {
        return contacts.toArray(new Contact[0]);
    }

    public Contact[] find(String query) {
        List<Contact> matches = new LinkedList<Contact>();
        for (Contact contact : contacts) {
            if (contact.getFirstName().contains(query)) {
                matches.add(contact);
            } else if (contact.getLastName().contains(query)) {
                matches.add(contact);
            } else if (contact.getDisplayName().contains(query)) {
                matches.add(contact);
            } else if (contact.getAddress().contains(query)) {
                matches.add(contact);
            }
        }
        return matches.toArray(new Contact[0]);
    }

    public Contact[] findByDisplayName(String query) {
        List<Contact> matches = new LinkedList<Contact>();
        for (Contact contact : contacts) {
            if (contact.getDisplayName().contains(query)) {
                matches.add(contact);
            }
        }
        return matches.toArray(new Contact[0]);
    }

    public Contact[] findByFirstName(String query) {
        List<Contact> matches = new LinkedList<Contact>();
        for (Contact contact : contacts) {
            if (contact.getFirstName().contains(query)) {
                matches.add(contact);
            }
        }
        return matches.toArray(new Contact[0]);
    }

    public Contact[] findByLastName(String query) {
        List<Contact> matches = new LinkedList<Contact>();
        for (Contact contact : contacts) {
            if (contact.getLastName().contains(query)) {
                matches.add(contact);
            }
        }
        return matches.toArray(new Contact[0]);
    }

    public Contact[] findByAddress(String query) {
        List<Contact> matches = new LinkedList<Contact>();
        for (Contact contact : contacts) {
            if (contact.getAddress().contains(query)) {
                matches.add(contact);
            }
        }
        return matches.toArray(new Contact[0]);
    }

    @Override
    public String toString() {
        return getContactsCount() + " contacts";
    }
}
