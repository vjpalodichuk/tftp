/*
 * FileMessage.java
 */
package com.capital7software.network.tftp.messages;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.NetAscii;
import com.capital7software.network.tftp.OpCode;
import com.capital7software.network.tftp.TftpOption;
import com.capital7software.network.tftp.TransferMode;

import java.util.*;

/**
 * @author Vincent Palodichuk
 */
public class FileMessage implements Message, OptionableMessage {
    protected static final int HEADER_SIZE = 2;
    protected static final TransferMode DEFAULT_MODE = TransferMode.OCTET;
    protected static final OpCode DEFAULT_OP_CODE = OpCode.NONE;

    OpCode opCode;
    NetAscii filename;
    NetAscii mode;
    List<MessageOption> options;

    public FileMessage() throws TftpException {
        this(DEFAULT_OP_CODE, null, DEFAULT_MODE);
    }

    public FileMessage(OpCode opCode, String filename) throws TftpException {
        this(opCode, filename, DEFAULT_MODE);
    }

    public FileMessage(OpCode opCode, String filename, TransferMode mode) throws TftpException {
        if (opCode == null || (opCode != DEFAULT_OP_CODE && opCode != OpCode.RRQ && opCode != OpCode.WRQ)) {
            throw new TftpException("opCode must be RRQ or WRQ for this message type.");
        }

        this.opCode = opCode;
        this.filename = (filename != null && !filename.isEmpty()) ? new NetAscii(filename) : null;
        this.mode = mode != null ? new NetAscii(mode.getValue()) : new NetAscii(DEFAULT_MODE.getValue());
        this.options = new LinkedList<>();
    }

    @Override
    public OpCode getOpCode() {
        return opCode;
    }

    @Override
    public List<MessageOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    @Override
    public boolean addOption(MessageOption option) {
        boolean answer = false;

        if (option != null) {
            answer = options.add(option);
        }

        return answer;
    }

    @Override
    public boolean addOptionAll(Collection<MessageOption> options) {
        return OptionableMessage.addOptionAll(options, this.options);
    }

    @Override
    public int getPayloadSize() {
        int size = HEADER_SIZE;

        if (filename != null) {
            size += filename.getBytes().length;
        }

        size++;
        if (mode != null) {
            size += mode.getBytes().length;
        }

        size++;

        for (MessageOption option : options) {
            size += option.getByteSize();
        }

        return size;
    }

    public void setOpCode(OpCode opCode) throws TftpException {
        if ((opCode != OpCode.RRQ && opCode != OpCode.WRQ)) {
            throw new TftpException("opCode must be RRQ or WRQ for this message type.");
        }

        this.opCode = opCode;
    }

    public void setFilename(NetAscii filename) {
        this.filename = filename;
    }

    public void setMode(TransferMode mode) {
        this.mode = new NetAscii(mode.getValue());
    }

    @Override
    public byte[] getPayload() {
        int size = getPayloadSize();
        byte[] buffer = new byte[size];
        short op = (short) opCode.getValue();
        int offset = 0;

        // Write the opcode:
        buffer[offset++] = (byte) ((op & 0xFF00) >> 8); // hi-word
        buffer[offset++] = (byte) (op & 0x00FF); // lo-word

        // Write the filename and mode
        byte[] tBuffer;
        if (filename != null) {
            tBuffer = filename.getBytes();
            System.arraycopy(tBuffer, 0, buffer, offset, tBuffer.length);
            offset += tBuffer.length;
        }
        buffer[offset++] = NetAscii.CHAR_NULL;
        if (mode != null) {
            tBuffer = mode.getBytes();
            System.arraycopy(tBuffer, 0, buffer, offset, tBuffer.length);
            offset += tBuffer.length;
        }
        buffer[offset++] = NetAscii.CHAR_NULL;

        // Write out any options
        for (var option : options) {
            System.arraycopy(option.getBytes(), 0, buffer, offset, option.getByteSize());
            offset += option.getByteSize();
        }

        return buffer;
    }

    @Override
    public void fromPayload(byte[] bytes) throws TftpException {
        fromPayload(bytes, 0, bytes != null ? bytes.length : 0);
    }

    @Override
    public void fromPayload(byte[] bytes, int length) throws TftpException {
        fromPayload(bytes, 0, length);
    }

    @Override
    public void fromPayload(byte[] bytes, int offset, int length) throws TftpException {
        if (bytes == null || (bytes[offset] != 0 || (bytes[offset + 1] != OpCode.WRQ.getValue() && bytes[offset + 1] != OpCode.RRQ.getValue()))) {
            throw new TftpException("The specified buffer is not for a RRQ or WRQ message.");
        }

        int end = offset + length;

        opCode = bytes[offset + 1] == OpCode.WRQ.getValue() ? OpCode.WRQ : OpCode.RRQ;
        offset += 2;

        // Read the filename and mode
        if (offset < end - 2) {
            filename = NetAscii.fromBytes(bytes, offset, 0);
            offset += filename.length();
        }
        offset++;
        if (offset < end - 1) {
            mode = NetAscii.fromBytes(bytes, offset, 0);
            offset += mode.length();
        }
        offset++;

        // Load the options
        while (offset < end) {
            // Options always come in pairs!
            MessageOption option = new TftpOption(bytes, offset);
            options.add(option);
            offset += option.getByteSize();
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.opCode);
        hash = 19 * hash + Objects.hashCode(this.filename);
        hash = 19 * hash + Objects.hashCode(this.mode);
        hash = 19 * hash + Objects.hashCode(this.options);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileMessage other = (FileMessage) obj;
        if (this.opCode != other.opCode) {
            return false;
        }
        if (!Objects.equals(this.filename, other.filename)) {
            return false;
        }
        if (!Objects.equals(this.mode, other.mode)) {
            return false;
        }
        return Objects.equals(this.options, other.options);
    }

    public NetAscii getFilename() {
        return filename;
    }

    public NetAscii getMode() {
        return mode;
    }


    @Override
    public String toString() {
        return "FileMessage{" + "opCode = " + opCode + ", filename = " + filename + ", mode = " + mode + ", options = " + options + '}';
    }
}
