/*
 * FileMessageTest.java
 */
package com.capital7software.network.tftp.messages;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.OpCode;
import com.capital7software.network.tftp.TftpOption;
import com.capital7software.network.tftp.TransferMode;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * 
 */
public class FileMessageTest {
    
    public FileMessageTest() {
    }
    
    @BeforeAll
    public static void setUp() {
    }

    /**
     * Test of getOpCode method, of class FileMessage.
     */
    @Test
    public void testGetOpCode() throws TftpException {
        System.out.println("getOpCode");
        FileMessage instance = new FileMessage();
        OpCode expResult = OpCode.NONE;
        OpCode result = instance.getOpCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOptions method, of class FileMessage.
     */
    @Test
    public void testGetOptions() throws TftpException {
        System.out.println("getOptions");
        FileMessage instance = new FileMessage();
        List<MessageOption> expResult = new LinkedList<>();
        List<MessageOption> result = instance.getOptions();
        assertEquals(expResult, result);
    }

    /**
     * Test of addOption method, of class FileMessage.
     */
    @Test
    public void testAddOption() throws TftpException {
        System.out.println("addOption");
        MessageOption option = null;
        FileMessage instance = new FileMessage();
        boolean expResult = false;
        boolean result = instance.addOption(option);
        assertEquals(expResult, result);
    }

    /**
     * Test of addOptionAll method, of class FileMessage.
     */
    @Test
    public void testAddOptionAll() throws TftpException {
        System.out.println("addOptionAll");
        Collection<MessageOption> options = null;
        FileMessage instance = new FileMessage();
        boolean expResult = false;
        boolean result = instance.addOptionAll(options);
        assertEquals(expResult, result);
    }

    /**
     * Test of getPayloadSize method, of class FileMessage.
     */
    @Test
    public void testGetPayloadSize() throws TftpException {
        System.out.println("getPayloadSize");
        FileMessage instance = new FileMessage();
        int expResult = 9;
        int result = instance.getPayloadSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPayload method, of class FileMessage.
     */
    @Test
    public void testGetPayload() throws TftpException {
        System.out.println("getPayload");
        FileMessage instance = new FileMessage();
        byte[] expResult = {0, (byte)OpCode.NONE.getValue(), 0, 0x6F, 0x63, 0x74, 0x65, 0x74, 0};
        byte[] result = instance.getPayload();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of fromPayload method, of class FileMessage.
     */
    @Test
    public void testFromPayload() {
        Throwable exception = assertThrows(TftpException.class, () -> {
            System.out.println("fromPayload");
            byte[] bytes = null;
            FileMessage instance = new FileMessage();
            instance.fromPayload(bytes);
        });
        assertEquals("The specified buffer is not for a RRQ or WRQ message.", exception.getMessage());
    }

    @Test
    public void testRealSendFileMessage() throws TftpException {
        System.out.println("testRealSendMessage");
        FileMessage instance = new FileMessage(OpCode.WRQ, "test.pdf", TransferMode.OCTET);
        
        byte[] bytes = instance.getPayload();
        assertNotNull(bytes);
        
        instance.addOption(new TftpOption(MessageOptionCode.BLOCK_SIZE, "8192"));
        instance.addOption(new TftpOption(MessageOptionCode.TIMEOUT, "10"));
        instance.addOption(new TftpOption(MessageOptionCode.FILE_SIZE, "9891257"));
        instance.addOption(new TftpOption(MessageOptionCode.FILE_MD5, "FILE_MD5"));
        instance.addOption(new TftpOption(MessageOptionCode.BLOCK_COUNT, "13"));

        byte[] newBytes = instance.getPayload();
        assertNotEquals(bytes.length, newBytes.length);
        
        FileMessage instance2 = new FileMessage();
        try {
            instance2.fromPayload(newBytes);
        } catch (TftpException ex) {
            Logger.getLogger(FileMessageTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals(instance, instance2);
        System.out.println(instance);
        System.out.println(instance2);
    }
}
