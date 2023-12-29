/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capital7software.network.exception;

/**
 * The Reversi game may throw a ReversiException if a game specific error arises.
 * Examine the message in the exception for further information.
 * 
 * @author Vincent J. Palodichuk
 */
public class TftpException extends Exception {

    /**
     * Initializes this exception.
     */
    public TftpException() {
        super();
    }

    /**
     * Initializes this exception with the specified message.
     * 
     * @param msg The message to include with the exception.
     */
    public TftpException(String msg) {
        super(msg);
    }
    
    /**
     * Initializes this exception with the specified message.
     * 
     * @param msg The message to include with the exception.
     * @param ex The Throwable that lead to this exception being thrown.
     */
    public TftpException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
