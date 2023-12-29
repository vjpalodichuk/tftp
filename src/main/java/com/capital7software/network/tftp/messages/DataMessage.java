/*
 * File: DataMessage.java
 */
package com.capital7software.network.tftp.messages;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.OpCode;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 */
public class DataMessage implements Message, IdableMessage {
    public static final int HEADER_SIZE = 6;
    protected static final int DEFAULT_ID = 1;
    protected static final OpCode DEFAULT_OP_CODE = OpCode.DATA;
    
    protected OpCode opCode;
    protected int id;
    protected byte[] block;

    public DataMessage() {
        this(DEFAULT_ID);
    }
    
    public DataMessage(int id) {
        this.id = id;
        opCode = DEFAULT_OP_CODE;
        block = null;
    }
    
    @Override
    public OpCode getOpCode() {
        return opCode;
    }

    @Override
    public int getPayloadSize() {
        int size = HEADER_SIZE;
        
        if (block != null) {
            size += block.length;
        }
        
        return size;
    }

    @Override
    public byte[] getPayload() {
        var buffer = new byte[getPayloadSize()];
        Message.writeOpCodeAndId(id, (short) opCode.getValue(), 0, buffer);


        // Write out any data
        if (block != null) {
            System.arraycopy(block, 0, buffer, HEADER_SIZE, block.length);
        }
        
        return buffer;
    }

    @Override
    public int getId() {
        return id;
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
        if (bytes == null || (bytes[offset] != 0 || (bytes[offset + 1] != OpCode.DATA.getValue()))) {
            throw new TftpException("The specified buffer is not for a DATA message.");
        }
        
        int end = offset + length - 1;
        opCode = bytes[offset + 1] == OpCode.DATA.getValue() ? OpCode.DATA : OpCode.UNKNOWN;
        id = Message.getIdFromPayload(bytes, offset);

        offset += HEADER_SIZE;
        
        // Load the data
        if (offset < end) {
            block = new byte[length - HEADER_SIZE];
            
            System.arraycopy(bytes, offset, block, 0, block.length);
        }
    }

    public byte[] getBlock() {
        return block;
    }

    public void setBlock(byte[] block, int length) {
        this.block = new byte[length];
        
        System.arraycopy(block, 0, this.block, 0, length);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.opCode);
        hash = 17 * hash + this.id;
        hash = 17 * hash + Arrays.hashCode(this.block);
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
        final DataMessage other = (DataMessage) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.opCode != other.opCode) {
            return false;
        }
        return Arrays.equals(this.block, other.block);
    }

    @Override
    public String toString() {
        return "DataMessage{" + "opCode = " + opCode + ", id = " + id + ", block = " + Arrays.toString(block) + '}';
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }
    
}
