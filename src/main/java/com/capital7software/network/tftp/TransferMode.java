/**
 * File: TransferMode.java
 */
package com.capital7software.network.tftp;

/**
 *
 * 
 */
public enum TransferMode {
    NET_ASCII("netascii"),
    OCTET("octet"),
    MAIL("mail"),
    UNICODE("unicode"),
    BIN64("bin64");
    
    private final String value;
    
    TransferMode(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
