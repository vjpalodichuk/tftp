/*
 * File: AckMessageTest.java
 */
package com.capital7software.network.tftp.messages;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.OpCode;
import com.capital7software.network.tftp.TftpOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class AckMessageTest {
    
    public AckMessageTest() {
    }

    @Test
    public void testRealAckMessage() throws TftpException {
        System.out.println("testRealAckMessage");
        AckMessage instance = new AckMessage(OpCode.OACK);
        
        byte[] bytes = instance.getPayload();
        assertNotNull(bytes);
        
        instance.addOption(new TftpOption(MessageOptionCode.BLOCK_SIZE, "8192"));
        instance.addOption(new TftpOption(MessageOptionCode.TIMEOUT, "10"));
        instance.addOption(new TftpOption(MessageOptionCode.FILE_SIZE, "9891257"));
        instance.addOption(new TftpOption(MessageOptionCode.FILE_MD5, "FILE_MD5"));
        instance.addOption(new TftpOption(MessageOptionCode.BLOCK_COUNT, "13"));

        byte[] newBytes = instance.getPayload();
        assertNotEquals(bytes.length, newBytes.length);
        
        AckMessage instance2 = new AckMessage();
        try {
            instance2.fromPayload(newBytes);
        } catch (TftpException ex) {
            Logger.getLogger(AckMessageTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals(instance, instance2);
        System.out.println(instance);
        System.out.println(instance2);
    }
    
}
