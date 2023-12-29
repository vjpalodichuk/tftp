package com.capital7software.network.tftp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 *
 * @author Vincent Palodichuk
 */
public class NetAsciiTest {

    public NetAsciiTest() {
    }

    /**
     * Test of convertToCRLFs method, of class NetAscii.
     */
    @Test
    public void testNetAsciiConstructor() {
        System.out.println("testNetAsciiConstructor");
        String string = "\nTest\nTest\nTest\n\r";
        String expResult = "\r\nTest\r\nTest\r\nTest\r\n" + NetAscii.CHAR_CR + NetAscii.CHAR_NULL;
        NetAscii netascii = new NetAscii(string);
        String result = netascii.getValue();
        int compare = 0;

        assertEquals(compare, expResult.compareTo(result));
        assertEquals(string, netascii.getOriginal());
        assertEquals(expResult, result);
    }

    /**
     * Test of convertToCRLFs method, of class NetAscii.
     */
    @Test
    public void testConvertToNetAscii() {
        System.out.println("testConvertToNetAscii");
        String string = "\nTest\nTest\nTest\n\r";
        String expResult = "\r\nTest\r\nTest\r\nTest\r\n" + NetAscii.CHAR_CR + NetAscii.CHAR_NULL;
        String result = NetAscii.convertToNetAscii(string);
        int compare = 0;

        assertEquals(compare, expResult.compareTo(result));

        assertEquals(expResult, result);
    }

    /**
     * Test of convertLFs method, of class NetAscii.
     */
    @Test
    public void testConvertFromNetAsciiToHost() {
        System.out.println("testConvertFromNetAscii");
        String string = "\r\nTest\r\nTest\r\nTest\r\n\r\n" + NetAscii.CHAR_CR + NetAscii.CHAR_NULL;
        String expResult = System.lineSeparator() + "Test" +
                System.lineSeparator() + "Test" + System.lineSeparator() +
                "Test" + System.lineSeparator() + System.lineSeparator() + "\r";
        String result = NetAscii.convertFromNetAscii(string);
        int compare = 0;

        assertEquals(compare, expResult.compareTo(result));

        assertEquals(expResult, result);
    }

}
