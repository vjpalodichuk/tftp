/**
 * File: ErrorMessageTest.java
 */
package com.capital7software.network.tftp.messages;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.ErrorCode;
import com.capital7software.network.tftp.OpCode;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Vincent Palodichuk
 */
public class ErrorMessageTest {
    
    public ErrorMessageTest() {
    }
    
    @BeforeAll
    public static void setUp() {
    }

    /**
     * Test of getOpCode method, of class ErrorMessage.
     */
    @Test
    public void testGetOpCode() {
        System.out.println("getOpCode");
        ErrorMessage instance = new ErrorMessage();
        OpCode expResult = OpCode.ERROR;
        OpCode result = instance.getOpCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPayloadSize method, of class ErrorMessage.
     */
    @Test
    public void testGetPayloadSize() {
        int DEFAULT_ERROR_MESSAGE_PAYLOAD_SIZE = 35;
        System.out.println("getPayloadSize");
        ErrorMessage instance = new ErrorMessage();
        int result = instance.getPayloadSize();
        assertEquals(DEFAULT_ERROR_MESSAGE_PAYLOAD_SIZE, result);
    }

    /**
     * Test of getPayload method, of class ErrorMessage.
     */
    @Test
    public void testGetPayload() {
        int DEFAULT_ERROR_MESSAGE_PAYLOAD_SIZE = 35;
        System.out.println("getPayload");
        ErrorMessage instance = new ErrorMessage();
        byte[] result = instance.getPayload();
        assertEquals(DEFAULT_ERROR_MESSAGE_PAYLOAD_SIZE, result.length);
    }

    /**
     * Test of fromPayload method, of class ErrorMessage.
     */
    @Test
    public void testFromPayload() {
        Throwable exception = assertThrows(TftpException.class, () -> {
            System.out.println("fromPayload");
            ErrorMessage instance = new ErrorMessage();
            instance.fromPayload(null);
        });
        assertEquals("The specified buffer is not for an ERROR message.", exception.getMessage());
    }
    
    @Test
    public void testRealErrorMessage() {
        System.out.println("testRealErrorMessage");
        ErrorMessage instance = new ErrorMessage(ErrorCode.FILE_ALREADY_EXISTS, "The file already exists.");
        
        byte[] bytes = instance.getPayload();

        assertNotNull(bytes);

        byte[] newBytes = instance.getPayload();
        ErrorMessage instance2 = new ErrorMessage();
        
        try {
            instance2.fromPayload(newBytes);
        } catch (TftpException ex) {
            Logger.getLogger(ErrorMessageTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals(instance, instance2);
        System.out.println(instance);
        System.out.println(instance2);
    }
    
    
}
