/**
 * File: MessageOptionCode.java
 */
package com.capital7software.network.tftp.messages;

/**
 * The available OpCodes for the MessageOption interface
 * 
 */
public enum MessageOptionCode {
    BLOCK_SIZE("blksize"),
    FILE_SIZE("tsize"),
    TIMEOUT("timeout"),
    BLOCK_COUNT("blkcnt"),
    FILE_MD5("tmd5"),
    RETRY_COUNT("retry");
    
    private final String value;
    
    MessageOptionCode(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
