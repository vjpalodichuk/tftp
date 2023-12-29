/**
 * File: OpCode.java
 */
package com.capital7software.network.tftp;

/**
 *
 * 
 */
public enum OpCode {
    NONE(0),
    RRQ(1),
    WRQ(2),
    DATA(3),
    ACK(4),
    ERROR(5),
    OACK(6),
    UNKNOWN(99);
    
    private final int value;
    
    OpCode(int code) {
        this.value = code;
    }
    
    public int getValue() {
        return this.value;
    }
}
