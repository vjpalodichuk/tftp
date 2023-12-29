/*
 * File: AckMessage.java
 */
package com.capital7software.network.tftp.messages;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.OpCode;
import com.capital7software.network.tftp.TftpOption;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * 
 */
public class AckMessage implements Message, OptionableMessage, IdableMessage {
    protected static final int HEADER_SIZE = 6;
    protected static final OpCode DEFAULT_OP_CODE = OpCode.ACK;
    protected static final int DEFAULT_ID = 0;
    
    OpCode opCode;
    int id;
    List<MessageOption> options;
    
    public AckMessage() throws TftpException {
        this(DEFAULT_OP_CODE, DEFAULT_ID);
    }

    public AckMessage(int id) throws TftpException {
        this(DEFAULT_OP_CODE, id);
    }

    public AckMessage(OpCode opCode) throws TftpException {
        this(opCode, DEFAULT_ID);
    }

    public AckMessage(OpCode opCode, int id) throws TftpException {
        if (opCode != null && opCode != OpCode.ACK && opCode != OpCode.OACK) {
            throw new TftpException("opCode must be ACK or OACK for this message type.");
        }
        
        this.opCode = opCode;
        this.id = id;
        this.options = new LinkedList<>();
    }
    
    @Override
    public OpCode getOpCode() {
        return opCode;
    }

    /**
     *
     * @return A list of MessageOption.
     * @throws com.capital7software.network.exception.TftpException If OpCode doesn't equal OACK
     */
    @Override
    public List<MessageOption> getOptions() throws TftpException {
        if (opCode != OpCode.OACK) {
            throw new TftpException("cannot get options from an ACK message. Use OACK.");
        }
        
        return Collections.unmodifiableList(options);
    }

    /**
     *
     * @param option The option to add
     * @return True is returned if the option was successfully added
     * @throws com.capital7software.network.exception.TftpException If OpCode doesn't equal OACK
     */
    @Override
    public boolean addOption(MessageOption option) throws TftpException {
        if (opCode != OpCode.OACK) {
            throw new TftpException("cannot add options to an ACK message. Use OACK.");
        }
        
        boolean answer = false;
        
        if (option != null)  {
            answer = options.add(option);
        }
        
        return answer;
    }

    /**
     * Add all the options to this message
     * @param options The list of options to add
     * @return True if the options were successfully added
     * @throws com.capital7software.network.exception.TftpException If OpCode doesn't equal OACK
     */
    @Override
    public boolean addOptionAll(Collection<MessageOption> options) throws TftpException {
        if (opCode != OpCode.OACK) {
            throw new TftpException("cannot add options to an ACK message. Use OACK.");
        }

        return OptionableMessage.addOptionAll(options, this.options);
    }

    @Override
    public int getPayloadSize() {
        int size = HEADER_SIZE;
        
        for (MessageOption option : options) {
            size += option.getByteSize();
        }
        
        return size;
    }

    public void setOpCode(OpCode opCode) throws TftpException {
        if ((opCode != OpCode.ACK && opCode != OpCode.OACK)) {
            throw new TftpException("opCode must be ACK or OACK for this message type.");
        }
        
        this.opCode = opCode;
        
        if (opCode == OpCode.ACK) {
            options.clear();
        }
    }

    @Override
    public byte[] getPayload() {
        var buffer = new byte[getPayloadSize()];
        var offset = Message.writeOpCodeAndId(id, (short) opCode.getValue(), 0, buffer);
        
        // Write out any options if we are an OACK
        if (opCode == OpCode.OACK) {
            for (var option : options) {
                System.arraycopy(option.getBytes(), 0, buffer, offset, option.getByteSize());
                offset += option.getByteSize();
            }
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
        if (bytes == null || (bytes[offset] != 0 || (bytes[offset + 1] != OpCode.ACK.getValue() && bytes[offset + 1] != OpCode.OACK.getValue()))) {
            throw new TftpException("The specified buffer is not for an ACK or OACK message.");
        }
        
        int end = offset + length;
        opCode = bytes[offset + 1] == OpCode.ACK.getValue() ? OpCode.ACK : OpCode.OACK;
        id = Message.getIdFromPayload(bytes, offset);
        
        offset += HEADER_SIZE;
        
        // Load the options
        if (opCode == OpCode.OACK && offset < end) {
            while (offset < end) {
                // Options always come in pairs!
                MessageOption option = new TftpOption(bytes, offset);
                options.add(option);
                offset += option.getByteSize();
            }
        }
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.opCode);
        hash = 53 * hash + this.id;
        hash = 53 * hash + Objects.hashCode(this.options);
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
        final AckMessage other = (AckMessage) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.opCode != other.opCode) {
            return false;
        }
        return Objects.equals(this.options, other.options);
    }

    @Override
    public String toString() {
        return "AckMessage{" + "opCode = " + opCode + ", id = " + id + ", options = " + options + '}';
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }
    
}
