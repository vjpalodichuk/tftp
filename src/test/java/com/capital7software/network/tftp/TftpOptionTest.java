/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capital7software.network.tftp;

import com.capital7software.network.tftp.messages.MessageOptionCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Vincent Palodichuk
 */
public class TftpOptionTest {
    
    public TftpOptionTest() {
    }
    
    @BeforeAll
    public static void setUp() {
    }

    /**
     * Test of getName method, of class TftpOption.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        TftpOption instance = new TftpOption();
        NetAscii expResult = null;
        NetAscii result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setName method, of class TftpOption.
     */
    @Test
    public void testSetName() {
        System.out.println("setName");
        NetAscii name = null;
        TftpOption instance = new TftpOption();
        instance.setName(name);
    }

    /**
     * Test of getValue method, of class TftpOption.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        TftpOption instance = new TftpOption();
        NetAscii expResult = null;
        NetAscii result = instance.getValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of setValue method, of class TftpOption.
     */
    @Test
    public void testSetValue() {
        System.out.println("setValue");
        NetAscii value = null;
        TftpOption instance = new TftpOption();
        instance.setValue(value);
    }

    /**
     * Test of getByteSize method, of class TftpOption.
     */
    @Test
    public void testGetByteSize() {
        System.out.println("getByteSize");
        TftpOption instance = new TftpOption();
        int expResult = -1;
        int result = instance.getByteSize();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBytes method, of class TftpOption.
     */
    @Test
    public void testGetBytes() {
        System.out.println("getBytes");
        TftpOption instance = new TftpOption();
        byte[] expResult = null;
        byte[] result = instance.getBytes();
        assertArrayEquals(expResult, result);
    }
    
    /**
     * Test of getBytes method, of class TftpOption.
     */
    @Test
    public void testFromBytesWithRealOption() {
        System.out.println("testGetBytesWithRealOption");
        TftpOption instance = new TftpOption(MessageOptionCode.BLOCK_SIZE.getValue(), "8192");
        byte[] bytes = instance.getBytes();
        
        TftpOption option = new TftpOption(bytes);
        
        boolean expResult = true;
        boolean result = Objects.equals(option, instance);
        assertEquals(expResult, result);
    }
    
}
