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

public class MailServer {
    public enum Protocol {
        POP3, IMAP, SMTP
    }

    private final Protocol protocol;
    private String name;
    private String host;
    private int port;
    private String userName;
    private String password;

    public MailServer(Protocol protocol, String name, String host,
                      String userName, String password) {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException(
                    "Name cannot be null or empty.");
        } else if (host == null || host.length() == 0) {
            throw new IllegalArgumentException(
                    "Host cannot be null or empty.");
        }

        this.protocol = protocol;
        this.name = name;

        int index = host.lastIndexOf(':');
        if (index == -1) {
            this.host = host;
            switch (protocol) {
                case POP3:
                    this.port = 110;
                    break;
                case IMAP:
                    this.port = 143;
                    break;
                case SMTP:
                    this.port = 25;
                    break;
            }
        } else {
            this.host = host.substring(0, index);
            this.port = Integer.valueOf(host.substring(index + 1));
        }

        this.userName = userName;
        this.password = password;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHost(String host) {
        int index = host.lastIndexOf(':');
        if (index == -1) {
            this.host = host;
            switch (protocol) {
                case POP3:
                    this.port = 110;
                    break;
                case IMAP:
                    this.port = 143;
                    break;
                case SMTP:
                    this.port = 25;
                    break;
            }
        } else {
            this.host = host.substring(0, index);
            this.port = Integer.valueOf(host.substring(index + 1));
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return getName() + ": " + getProtocol() + '/' + getHost() + ':' +
               getPort();
    }
}
