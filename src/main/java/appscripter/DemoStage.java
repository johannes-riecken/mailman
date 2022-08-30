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

package appscripter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author sky
 */
public class DemoStage {
    private final List<DemoMessage> messages;
    private final List<Object> codeLocs;
    private final Object script;
    private final int pauseTime;

    public DemoStage(List<DemoMessage> messages, List<Object> codeLocs, Object script,
            int pauseTime) {
        messages = new ArrayList<DemoMessage>(messages);
        this.messages = Collections.unmodifiableList(messages);
        if (codeLocs == null) {
            codeLocs = Collections.emptyList();
        } else {
            codeLocs = new ArrayList<Object>(codeLocs);
        }
        this.codeLocs = Collections.unmodifiableList(codeLocs);
        this.script = script;
        this.pauseTime = pauseTime;
    }

    public final int getPauseTime() {
        return pauseTime;
    }

    public final List<DemoMessage> getMessages() {
        return messages;
    }

    public final List<Object> getCodeLocations() {
        return codeLocs;
    }

    public final Object getScript() {
        return script;
    }

    @Override
    public String toString() {
        return "DemoStage [messages=" + messages +
                ", codeLocs=" + codeLocs +
                ", script=" + script +
                ", pauseTime=" + pauseTime +
                "]";
    }
}
