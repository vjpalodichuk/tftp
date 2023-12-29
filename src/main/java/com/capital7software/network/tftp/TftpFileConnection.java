/**
 * File: TftpFileConnection.java
 */
package com.capital7software.network.tftp;

import com.capital7software.network.tftp.messages.IdableMessage;
import com.capital7software.network.util.FileInfo;

import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 *
 */
public abstract class TftpFileConnection extends TftpConnection implements IdableMessage {
    protected int id;

    public TftpFileConnection(DatagramSocket server, SocketAddress dest, FileInfo fileInfo, TransferMode mode) {
        super(server, dest, fileInfo, mode);
    }
    
    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }
    
}
