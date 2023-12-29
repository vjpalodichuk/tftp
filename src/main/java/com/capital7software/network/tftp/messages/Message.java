/**
 * File: Message.java
 */
package com.capital7software.network.tftp.messages;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.OpCode;
import org.jetbrains.annotations.NotNull;

/**
 *
 * 
 */
public interface Message {
    OpCode getOpCode();
    int getPayloadSize();
    byte[] getPayload();
    void fromPayload(byte[] bytes) throws TftpException;
    void fromPayload(byte[] bytes, int length) throws TftpException;
    void fromPayload(byte[] bytes, int offset, int length) throws TftpException;

    static int writeOpCodeAndId(int id, short op, int index, byte @NotNull [] buffer) {
        var offset = index;

        // Write the opcode:
        buffer[offset++] = (byte) ((op & 0xFF00) >> 8); // hi-word
        buffer[offset++] = (byte) (op & 0x00FF); // lo-word

        // Write the id:
        buffer[offset++] = (byte) (((id) & 0xFF000000) >> 24); // hi-dword
        buffer[offset++] = (byte) (((id) & 0x00FF0000) >> 16); // hi-dword lo-word
        buffer[offset++] = (byte) (((id) & 0x0000FF00) >> 8); // lo-dword hi-word
        buffer[offset++] = (byte) ((id) & 0x000000FF) ; // lo-dword lo-word

        return offset;
    }

    static int writeOpCodeAndError(int error, short op, int index, byte @NotNull [] buffer) {
        var offset = index;

        // Write the opcode:
        buffer[offset++] = (byte) ((op & 0xFF00) >> 8); // hi-word
        buffer[offset++] = (byte) (op & 0x00FF); // lo-word

        // Write the errCode:
        buffer[offset++] = (byte)((error & 0xFF00) >> 8); // hi-word
        buffer[offset++] = (byte)(error & 0x00FF); // lo-word

        return offset;
    }

    static int getIdFromPayload(byte @NotNull [] bytes, int startIndex) {
        var offset = startIndex + 2;
        int sid = (((bytes[offset]) << 24) | (0x00FF0000 & ((bytes[offset + 1]) << 16)) | (0x0000FF00 & ((bytes[offset + 2]) << 8)) | (0x000000FF & (bytes[offset + 3])));
        if (sid < 0) {
            return 0x000000000000FFFF & sid;
        } else {
            return sid;
        }
    }
}
