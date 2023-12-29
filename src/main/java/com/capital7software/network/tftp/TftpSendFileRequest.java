/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capital7software.network.tftp;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.util.FileInfo;

import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 *
 * @author Vincent Palodichuk
 */
public class TftpSendFileRequest extends TftpFileConnection{

    public TftpSendFileRequest(DatagramSocket server, SocketAddress dest, FileInfo fileInfo, TransferMode mode) {
        super(server, dest, fileInfo, mode);
    }

    @Override
    public boolean connect() throws TftpException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OpCode getOpCode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
