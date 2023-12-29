/**
 * File: ErrorCode.java
 */
package com.capital7software.network.tftp;

/**
 *
 * 
 */
public enum ErrorCode {
    NOT_DEFINED(0),
    FILE_NOT_FOUND(1),
    ACCESS_VIOLATION(1),
    DISK_FULL(3),
    ILLEGAL_TFTP_OPERATION(4),
    UNKNOWN_TRANSFER_ID(5),
    FILE_ALREADY_EXISTS(6),
    NO_SUCH_USER(7);
    
    private final int value;
    
    ErrorCode(int code) {
        this.value = code;
    }
    
    public int getValue() {
        return this.value;
    }
}
