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

import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Contact implements Cloneable {
    private static final Map<String,URI> IMAGE_MAP;
    private static final String IMAGE_DIR =
            System.getProperty("user.dir") + File.separator + ".mailman" +
            File.separator + "contactImages";
    private static final String MAP_FILE =
            System.getProperty("user.dir") + File.separator + ".mailman" +
            File.separator + "contactImageMap";
    private static final Map<String,Contact> CONTACT_MAP =
            new HashMap<String,Contact>();
    private static final URI DEFAULT_IMAGE_URI;

    private String firstName;
    private String lastName;
    private String displayName;
    private String address;
    private URI imageURI;

    static {
        IMAGE_MAP = new HashMap<String,URI>();
        Properties props = new Properties();
        DEFAULT_IMAGE_URI = new File(IMAGE_DIR, "unknown.jpg").toURI();
        try {
            FileReader reader = new FileReader(MAP_FILE);
            props.load(reader);
            for (Object key : props.keySet()) {
                IMAGE_MAP.put((String)key, new File(
                        IMAGE_DIR, props.getProperty((String)key)).toURI());
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("exception loading contact images");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static Contact getContact(String first, String last,
            String displayName,
            String address) {
        Contact contact = CONTACT_MAP.get(address);
        if (contact == null) {
            contact = new Contact(first, last, displayName, address);
            CONTACT_MAP.put(address, contact);
        }
        return contact;
    }

    private Contact(String firstName, String lastName, String displayName,
                   String address) {
        if (displayName == null || displayName.length() == 0) {
            throw new IllegalArgumentException(
                    "Display name cannot be null or empty.");
        } else if (address == null || address.length() == 0) {
            throw new IllegalArgumentException(
                    "Address cannot be null or empty.");
        }

        this.firstName = firstName == null ? "" : firstName;
        this.lastName = lastName == null ? "" : lastName;
        this.displayName = displayName;
        this.address = address;
    }

    public void setImageLocation(URI path) {
        IMAGE_MAP.put(getAddress().toLowerCase(), path);
    }

    public URI getImageLocation() {
        URI path = IMAGE_MAP.get(getAddress().toLowerCase());
        if (path == null) {
            return DEFAULT_IMAGE_URI;
        }
        return path;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Contact contact = (Contact) o;
        if (address != null ? !address.equals(contact.address) :
            contact.address != null) {
            return false;
        }
        if (displayName != null ? !displayName.equals(contact.displayName) :
            contact.displayName != null) {
            return false;
        }
        if (firstName != null ? !firstName.equals(contact.firstName) :
            contact.firstName != null) {
            return false;
        }
        return !(lastName != null ? !lastName.equals(contact.lastName) :
                 contact.lastName != null);
    }

    @Override
    public int hashCode() {
        int result;
        result = (firstName != null ? firstName.hashCode() : 0);
        result = 29 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 29 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 29 * result + (address != null ? address.hashCode() : 0);
        return result;
    }

    @Override
    public Contact clone() {
        try {
            Contact clone = (Contact) super.clone();
            clone.setFirstName(firstName);
            clone.setLastName(lastName);
            clone.setDisplayName(displayName);
            clone.setAddress(address);
            return clone;
        } catch (CloneNotSupportedException e) {
            assert false;
        }
        return null;
    }

    @Override
    public String toString() {
        return getDisplayName() + " <" + getAddress() + '>';
    }
}
