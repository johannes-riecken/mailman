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

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author sky
 */
public final class DemoParser {
    public static List<DemoStage> parse(InputStream stream) throws SAXException, IOException, ParserConfigurationException {
        Handler handler = new Handler();
        SAXParserFactory.newInstance().newSAXParser().parse(stream, handler);
        List<DemoStage> stages = handler.getStages();
        return stages;
    }

    private DemoParser() {
    }


    private static final class Handler extends DefaultHandler {
        private static final Map<String,DemoCommand.Type> TYPE_MAP;

        private static final String E_STAGES = "stages";
        private static final String E_STAGE = "stage";
        private static final String E_MESSAGE = "message";
        private static final String E_SCRIPT = "script";
        private static final String T_FONT = "font";
        private static final String T_PAUSE = "pause";

        private final List<DemoStage> stages;
        private final List<DemoMessage> messages;
        private final List<Font> fonts;
        private final List<DemoCommand> commands;
        private boolean inMessage;
        private boolean inScript;
        private String message;
        private int stagePauseTime = 0;

        static {
            TYPE_MAP = new HashMap<String,DemoCommand.Type>();
            for (DemoCommand.Type type : DemoCommand.Type.values()) {
                StringBuilder builder = new StringBuilder();
                for (String chunk : type.toString().split("_")) {
                    builder.append(chunk);
                }
                TYPE_MAP.put(builder.toString().toUpperCase(Locale.ENGLISH), type);
            }
        }

        Handler() {
            stages = new ArrayList<DemoStage>();
            messages = new ArrayList<DemoMessage>();
            fonts = new ArrayList<Font>(3);
            fonts.add(new Font("Arial", Font.PLAIN, 36));
            commands = new ArrayList<DemoCommand>(11);
        }

        private List<DemoStage> getStages() {
            return stages;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            String eName = qName.intern();
            inMessage = false;
            if (inScript) {
                addCommand(eName, attributes);
            } else if (eName.equals(E_STAGE)) {
                pushFont(attributes.getValue(T_FONT));
                String pauseTimeString = attributes.getValue(T_PAUSE);
                if (pauseTimeString != null) {
                    stagePauseTime = Integer.parseInt(pauseTimeString);
                } else {
                    stagePauseTime = 0;
                }
            } else if (eName.equals(E_MESSAGE)) {
                inMessage = true;
                pushFont(attributes.getValue(T_FONT));
            } else if (eName.equals(E_SCRIPT)) {
                commands.clear();
                inScript = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String eName = qName.intern();
            if (eName.equals(E_STAGE)) {
                // stage end
                Object script = null;
                if (commands.size() > 0) {
                    script = new ArrayList<DemoCommand>(commands);
                }
                if (messages.size() > 0) {
                    stages.add(new DemoStage(messages, null, script, stagePauseTime));
                    messages.clear();
                }
                commands.clear();
                popFont();
            } else if (eName.equals(E_MESSAGE)) {
                inMessage = false;
                messages.add(new DemoMessage(message, getFont(), 0));
                message = "";
                popFont();
            } else if (eName.equals(E_SCRIPT)) {
                inScript = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if (inMessage) {
                message = new String(ch, start, length);
            }
        }

        private Font parseFont(String fontString) {
            Font currentFont = getFont();
            String[] chunks = fontString.split(" ");
            boolean styleDefined = false;
            int style = Font.PLAIN;
            int chunkIndex = 0;
            if ("bold".equals(chunks[0])) {
                style = Font.BOLD;
                chunkIndex++;
                if (chunkIndex < chunks.length && "italic".equals(chunks[chunkIndex])) {
                    chunkIndex++;
                    style |= Font.ITALIC;
                }
            } else if ("italic".equals(chunks[0])) {
                style = Font.ITALIC;
                chunkIndex++;
                if (chunkIndex < chunks.length && "bold".equals(chunks[chunkIndex])) {
                    chunkIndex++;
                    style |= Font.BOLD;
                }
            }
            int size;
            if (chunkIndex < chunks.length) {
                if (chunks[chunkIndex].charAt(0) == '+') {
                    size = currentFont.getSize() + Integer.parseInt(chunks[chunkIndex].substring(1));
                    chunkIndex++;
                } else if (chunks[chunkIndex].charAt(0) == '-') {
                    size = currentFont.getSize() - Integer.parseInt(chunks[chunkIndex].substring(1));
                    chunkIndex++;
                } else {
                    try {
                        size = Integer.parseInt(chunks[chunkIndex]);
                        chunkIndex++;
                    } catch (NumberFormatException nfe) {
                        size = currentFont.getSize();
                    }
                }
            } else {
                size = currentFont.getSize();
            }
            String name;
            if (chunkIndex < chunks.length) {
                StringBuilder nameBuilder = new StringBuilder();
                while (chunkIndex < chunks.length) {
                    if (nameBuilder.length() > 0) {
                        nameBuilder.append(" ");
                    }
                    nameBuilder.append(chunks[chunkIndex]);
                    chunkIndex++;
                }
                name = nameBuilder.toString();
            } else {
                name = currentFont.getName();
            }
            return new Font(name, style, size);
        }

        private Font getFont() {
            assert fonts.size() > 0;
            return fonts.get(fonts.size() - 1);
        }

        private void pushFont(String fontValue) {
            Font font;
            if (fontValue == null) {
                font = fonts.get(fonts.size() - 1);
            } else {
                font = parseFont(fontValue);
            }
            fonts.add(font);
        }

        private void popFont() {
            fonts.remove(fonts.size() - 1);
        }

        private void addCommand(String command, Attributes attributes) {
            Map<Object,Object> args = new HashMap<Object,Object>(attributes.getLength());
            for (int i = 0; i < attributes.getLength(); i++) {
                args.put(attributes.getQName(i), attributes.getValue(i));
            }
            String mappedCommand = command.toUpperCase(Locale.ENGLISH);
            if (TYPE_MAP.get(mappedCommand) == null) {
                throw new IllegalArgumentException("Unknown command " + command);
            }
            commands.add(new DemoCommand(TYPE_MAP.get(mappedCommand), args));
        }
    }
}
