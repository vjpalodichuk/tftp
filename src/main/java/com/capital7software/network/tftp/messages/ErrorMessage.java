/*
 * ErrorMessage.java
 */
package com.capital7software.network.tftp.messages;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.ErrorCode;
import com.capital7software.network.tftp.NetAscii;
import com.capital7software.network.tftp.OpCode;

import java.util.Objects;

/**
 *
 */
public class ErrorMessage implements Message {
    protected static final int HEADER_SIZE = 4;
    protected static final ErrorCode DEFAULT_ERROR_CODE = ErrorCode.NOT_DEFINED;
    protected static final String DEFAULT_ERROR_MESSAGE = "An unknown error has occurred.";
    
    protected OpCode opCode;
    protected int errorCode;
    protected NetAscii msg;
    
    public ErrorMessage() {
        this(DEFAULT_ERROR_CODE, DEFAULT_ERROR_MESSAGE);
    }
    
    public ErrorMessage(String msg) {
        this(DEFAULT_ERROR_CODE, msg);
    }
    public ErrorMessage(ErrorCode errorCode, String msg) {
        opCode = OpCode.ERROR;
        this.errorCode = errorCode != null ? errorCode.getValue() : DEFAULT_ERROR_CODE.getValue();
        this.msg = (msg != null && !msg.isEmpty()) ? new NetAscii(msg) : null;
    }
    
    @Override
    public OpCode getOpCode() {
        return opCode;
    }

    @Override
    public int getPayloadSize() {
        int size = HEADER_SIZE;
        
        if (msg != null) {
            size += msg.getBytes().length;
        }
        size++;
        
        return size;
    }

    @Override
    public byte[] getPayload() {
        byte[] buffer = new byte[getPayloadSize()];
        int offset = Message.writeOpCodeAndError(errorCode, (short) opCode.getValue(), 0, buffer);

        // Write the message
        if (msg != null) {
            byte[] tBuffer = msg.getBytes();
            System.arraycopy(tBuffer, 0, buffer, offset, tBuffer.length);
            offset += tBuffer.length;
        }

        buffer[offset++] = NetAscii.CHAR_NULL;
        
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
        if (bytes == null || (bytes[offset] != 0 || bytes[offset + 1] != OpCode.ERROR.getValue())) {
            throw new TftpException("The specified buffer is not for an ERROR message.");
        }
        
        int end = offset + length;
        opCode = bytes[offset + 1] == OpCode.ERROR.getValue() ? OpCode.ERROR : OpCode.UNKNOWN;
        offset += 2;
        errorCode = (((bytes[offset]) << 8) | ((bytes[offset + 1])));
        offset += 2;
        
        // Read the message
        if (offset < end - 1) {
            msg = NetAscii.fromBytes(bytes, offset, 0);
            offset += msg.length();
        }
        offset++;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode.getValue();
    }

    public NetAscii getMsg() {
        return msg;
    }

    public void setMsg(NetAscii msg) {
        this.msg = msg;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.opCode);
        hash = 37 * hash + this.errorCode;
        hash = 37 * hash + Objects.hashCode(this.msg);
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
        final ErrorMessage other = (ErrorMessage) obj;
        if (this.errorCode != other.errorCode) {
            return false;
        }
        if (this.opCode != other.opCode) {
            return false;
        }
        return Objects.equals(this.msg, other.msg);
    }

    @Override
    public String toString() {
        return "ErrorMessage{" + "opCode = " + opCode + ", errorCode = " + errorCode + ", msg = " + msg + '}';
    }

}
