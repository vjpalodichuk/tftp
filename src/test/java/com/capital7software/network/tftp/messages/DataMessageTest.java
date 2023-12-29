/*
 * File: DataMessageTest.java
 */
package com.capital7software.network.tftp.messages;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.OpCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Vincent Palodichuk
 */
public class DataMessageTest {
    
    public DataMessageTest() {
    }

    /**
     * Test of getOpCode method, of class DataMessage.
     */
    @Test
    public void testGetOpCode() {
        System.out.println("getOpCode");
        DataMessage instance = new DataMessage();
        OpCode expResult = OpCode.DATA;
        OpCode result = instance.getOpCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPayloadSize method, of class DataMessage.
     */
    @Test
    public void testGetPayloadSize() {
        System.out.println("getPayloadSize");
        DataMessage instance = new DataMessage();
        int expResult = DataMessage.HEADER_SIZE;
        int result = instance.getPayloadSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPayload method, of class DataMessage.
     */
    @Test
    public void testGetPayload() {
        System.out.println("getPayload");
        DataMessage instance = new DataMessage();
        byte[] expResult = {0, (byte)OpCode.DATA.getValue(), 0, 0, 0, 1};
        byte[] result = instance.getPayload();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getId method, of class DataMessage.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        DataMessage instance = new DataMessage();
        long expResult = 1;
        long result = instance.getId();
        assertEquals(expResult, result);
    }

    /**
     * Test of fromPayload method, of class DataMessage.
     */
    @Test
    public void testFromPayload() {
        Throwable exception = assertThrows(TftpException.class, () -> {
            System.out.println("fromPayload");
            DataMessage instance = new DataMessage();
            instance.fromPayload(null);
        });
        assertEquals("The specified buffer is not for a DATA message.", exception.getMessage());
    }

    /**
     * Test of getBlock method, of class DataMessage.
     */
    @Test
    public void testGetBlock() {
        System.out.println("getBlock");
        DataMessage instance = new DataMessage();
        byte[] result = instance.getBlock();
        assertArrayEquals(null, result);
    }
    
}
