/*
 * TftpConnection.java
 */
package com.capital7software.network.tftp;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.messages.MessageOption;
import com.capital7software.network.tftp.messages.MessageOptionCode;
import com.capital7software.network.tftp.messages.OptionableMessage;
import com.capital7software.network.util.FileInfo;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * 
 */
public abstract class TftpConnection implements OptionableMessage {
    private final static Logger ERRORS = Logger.getLogger(TftpConnection.class.getName());
    
    protected final DatagramSocket socket;
    protected SocketAddress outgoing;
    protected final SocketAddress address;
    protected final FileInfo fileInfo;
    protected final TransferMode mode;
    protected final List<MessageOption> options;
    
    TftpConnection(DatagramSocket server, SocketAddress dest, FileInfo fileInfo, TransferMode mode) {
        this.socket = server;
        address = dest;
        this.fileInfo = fileInfo;
        this.mode = mode;
        this.options = new LinkedList<>();
    }
    
    public void addDefaultOptions() {
        try {
            addOption(new TftpOption(MessageOptionCode.BLOCK_COUNT, "" + fileInfo.getNumBlocks()));
            addOption(new TftpOption(MessageOptionCode.BLOCK_SIZE, "" + fileInfo.getBlockSize()));
            addOption(new TftpOption(MessageOptionCode.FILE_SIZE, "" + fileInfo.getSize()));
            addOption(new TftpOption(MessageOptionCode.FILE_MD5, fileInfo.getMd5()));
            addOption(new TftpOption(MessageOptionCode.RETRY_COUNT, Tftp.DEFAULT_RETRY_COUNT));
            addOption(new TftpOption(MessageOptionCode.TIMEOUT, Tftp.DEFAULT_CONNECTION_TIMEOUT));
        } catch (TftpException ex) {
            ERRORS.log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public List<MessageOption> getOptions() {
        return options;
    }

    @Override
    public boolean addOption(MessageOption option) throws TftpException {
        boolean answer = false;
        
        if (option != null) {
            answer = options.add(option);
        }
        
        return answer;
    }

    @Override
    public boolean addOptionAll(Collection<MessageOption> options) {
        return OptionableMessage.addOptionAll(options, this.options);
    }
    
    abstract public boolean connect() throws TftpException;
    
    abstract public OpCode getOpCode();

    public SocketAddress getOutgoing() {
        return outgoing;
    }   
    
}
