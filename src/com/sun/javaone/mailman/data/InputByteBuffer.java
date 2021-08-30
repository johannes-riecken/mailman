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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 *
 * @author sky
 */
public class InputByteBuffer {
    protected FileChannel _channel;
    protected ByteBuffer _buffer;
    private boolean _atEnd;
    private CharsetDecoder _decoder;
    private CharBuffer _charBuffer;
    private int _readSize;
    
    /** Creates a new instance of InputByteBuffer */
    public InputByteBuffer(FileChannel channel, ByteBuffer buffer) {
        _channel = channel;
        _buffer = buffer;
        _buffer.position(_buffer.capacity());
    }
    
    public void setChannelPosition(int position) throws IOException {
        _atEnd = false;
        _channel.position(position);
        fillBuffer();
    }
    
    public int get(byte[] data, int offset, int length) throws IOException {
        int readSize = 0;
        while (length > 0) {
            int size = Math.min(_buffer.remaining(), length);
            _buffer.get(data, offset, size);
            offset += size;
            length -= size;
            readSize += size;
            if (atEnd()) {
                return readSize;
            }
            if (_buffer.remaining() == 0) {
                fillBuffer();
            }
        }
        return readSize;
    }
    
    public void rewind(int delta) throws IOException {
        int position = _buffer.position();
        if (position >= delta) {
            _buffer.position(position - delta);
        } else {
            _channel.position(_channel.position() - _readSize +
                    position - delta);
            fillBuffer();
        }
    }
    
    public boolean atEnd() throws IOException {
        if (!_atEnd && _buffer.remaining() == 0) {
            fillBuffer();
        }
        return _atEnd;
    }
    
    public byte get() throws IOException {
        ensureAvailable(1);
        return _buffer.get();
    }
    
    public short getShort() throws IOException {
        ensureAvailable(2);
        return _buffer.getShort();
    }
    
    public int getInt() throws IOException {
        ensureAvailable(4);
        return _buffer.getInt();
    }
    
    public long getLong() throws IOException {
        ensureAvailable(8);
        return _buffer.getLong();
    }
    
    public String getString(byte separator) throws IOException {
        getDecoder();
        getCharBuffer();
        byte b;
        ensureAvailable(1);
        if (get() == separator) {
            return "";
        }
        _buffer.position(_buffer.position() - 1);
        _buffer.mark();
        while ((b = get()) != separator) {
            if (_buffer.remaining() == 0) {
                decodeAvailableBytes(false);
                fillBuffer();
                _buffer.mark();
            }
        }
        if (_buffer.position() > 0) {
            _buffer.position(_buffer.position() - 1);
        }
        _buffer.limit(_buffer.position());
        decodeAvailableBytes(true);
        _decoder.flush(_charBuffer);
        _buffer.limit(_buffer.capacity());
        _charBuffer.flip();
        // Skip separator
        get();
        return _charBuffer.toString();
    }
    
    private void decodeAvailableBytes(boolean isEnd) {
        _buffer.reset();
        CoderResult result = _decoder.decode(_buffer, _charBuffer, isEnd);
        if (result == CoderResult.OVERFLOW) {
            CharBuffer newBuffer = CharBuffer.allocate(
                    _charBuffer.capacity() * 2);
            _charBuffer.flip();
            newBuffer.put(_charBuffer);
            _charBuffer = newBuffer;
            _charBuffer = newBuffer;
            _decoder.decode(_buffer, _charBuffer, isEnd);
        }
    }
    
    protected void ensureAvailable(int bytes) throws IOException {
        if (_buffer.remaining() < bytes) {
            fillBuffer();
        }
    }

    protected void fillBuffer() throws IOException {
        _buffer.clear();
        _readSize = _channel.read(_buffer);
        if (_readSize <= 0) {
            _atEnd = true;
        }
        _buffer.flip();
    }
    
    private CharBuffer getCharBuffer() {
        if (_charBuffer == null) {
            _charBuffer = CharBuffer.allocate(1024);
        }
        _charBuffer.clear();
        return _charBuffer;
    }
    
    private CharsetDecoder getDecoder() {
        if (_decoder == null) {
            _decoder = Charset.availableCharsets().get("UTF-8").newDecoder();
        }
        _decoder.reset();
        return _decoder;
    }

    public long getChannelPosition() throws IOException {
        return _channel.position() - _readSize + _buffer.position();
    }
}
