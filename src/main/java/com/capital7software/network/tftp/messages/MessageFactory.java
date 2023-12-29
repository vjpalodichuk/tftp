/**
 * File: MessageFactory
 */
package com.capital7software.network.tftp.messages;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.OpCode;

/**
 *
 * @author Vincent Palodichuk
 */
public class MessageFactory {

    public static Message getMessage(byte[] data, int offset, int length) throws TftpException {
        Message answer = null;
        
        if (data != null && data.length - offset >= 2) {
            int iCode = (short)(data[offset] << 8 | data[offset + 1]);
            
            if (iCode == OpCode.ACK.getValue() || iCode == OpCode.OACK.getValue()) {
                answer = new AckMessage();
                answer.fromPayload(data, offset, length);
            } else if (iCode == OpCode.RRQ.getValue() || iCode == OpCode.WRQ.getValue()) {
                answer = new FileMessage();
                answer.fromPayload(data, offset, length);
            } else if (iCode == OpCode.DATA.getValue()) {
                answer = new DataMessage();
                answer.fromPayload(data, offset, length);
            } else if (iCode == OpCode.ERROR.getValue()) {
                answer = new ErrorMessage();
                answer.fromPayload(data, offset, length);
            }
        }
        
        return answer;
    }
    
}
