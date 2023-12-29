/**
 * File: ClientThread.java
 */
package com.capital7software.network.tftp.client;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.OpCode;
import com.capital7software.network.tftp.Tftp;
import com.capital7software.network.tftp.TftpSendRequest;
import com.capital7software.network.tftp.TransferMode;
import com.capital7software.network.tftp.messages.*;
import com.capital7software.network.util.BlockInfo;
import com.capital7software.network.util.FileInfo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 *
 * @author Vincent Palodichuk
 */
public class ClientThread  extends Thread {
    public final static String SHUTDOWN_MSG = ".";
    private final static Logger ERRORS = Logger.getLogger(ClientThread.class.getName());
    
    private final InetAddress server;
    private final int destPort;
    private final String filename;
    private final DatagramSocket socket;
    private volatile boolean stopped = false;
    
    public ClientThread(InetAddress address, int port, String filename) throws SocketException {
        this.server = address;
        this.destPort = port;
        this.filename = filename;
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(Tftp.DEFAULT_SOCKET_TIMEOUT);
    }
    
    public void halt() {
        this.stopped = true;
    }
    
    @Override
    public void run() {
        System.out.printf("Preparing to send %s to server...%n", filename);
        System.out.println("Gathering file information...");
        FileInfo fi = generateFileInfo();
        
        if (fi == null) {
            System.out.println("Unable to validate the source file to send. Exiting...");
            return;
        }
        
        SocketAddress dest = negotiateSendTransfer(fi);
        
        if (dest != null) {
            try {
                System.out.println("Sending file to receiver...");

                for (BlockInfo ci : fi.getBlocks()) {
                    DataMessage message = new DataMessage(ci.getId());
                    message.setBlock(ci.read(false), (int) ci.getSize());
                    byte[] data = message.getPayload();
                    byte[] input = new byte[data.length * 2];
                    System.out.printf("Sending Block: %s... ", ci.getId());
//                    System.out.println(ci);

                    DatagramPacket output = new DatagramPacket(data, data.length, dest);
                    DatagramPacket receive = new DatagramPacket(input, input.length);
                    socket.send(output);
                    System.out.printf("Block: %s has been sent.%n", ci.getId());
                    socket.receive(receive);

                    try {
                        Message msg = MessageFactory.getMessage(receive.getData(), receive.getOffset(), receive.getLength());
                        System.out.printf("Received an %s message... ", msg.getOpCode());

                        if (msg.getOpCode() == OpCode.ACK) {
                            AckMessage response = (AckMessage)msg;
                            System.out.printf("Message details: [%s] - [%d]%n", response.getOpCode(), response.getId());
                        } else if (msg.getOpCode() == OpCode.ERROR) {
                            ErrorMessage response = (ErrorMessage)msg;
                            System.out.printf("Message details: [%s] - [%s] - [%s]%n", response.getOpCode(), response.getErrorCode(), response.getMsg().getValue());
                            System.out.println("Exiting...");
                            return;
                        }
                    } catch (TftpException ex) {
                        ERRORS.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }

                System.out.println("File has been sent to the receiver.");
                System.out.println("Exiting...");
            } catch (TftpException | IOException ex) {
                ERRORS.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    protected FileInfo generateFileInfo() {
        FileInfo answer = new FileInfo();
        
        answer.setFilename(filename);

        System.out.printf("Number of bytes: %d%n", answer.getSize());
        System.out.printf("File MD5 Hash: %s%n", answer.getMd5());
        System.out.println("Calculating block size and number of blocks...");

        long chunkSize = answer.getBlockSize();
        final List<BlockInfo> blocks = answer.getBlocks();
        long lastChunkSize = blocks.get(blocks.size() - 1).getSize();
        int numberOfChunks = blocks.size();

        if (lastChunkSize != chunkSize) {
            --numberOfChunks;
        }

        System.out.printf("%d %s of %d %s%n",
                numberOfChunks, numberOfChunks == 1 ? "block" : "blocks",
                chunkSize, chunkSize == 1 ? "byte" : "bytes");
        if (lastChunkSize != chunkSize) {
            System.out.printf("1 block of %d %s%n",
                lastChunkSize, lastChunkSize == 1 ? "byte" : "bytes");
            numberOfChunks++;
        }

        System.out.println("Validating blocks...");
        int count = 0;
        for (BlockInfo md5Chunk : blocks) {
            //System.out.println(String.format("Block %d MD5 Hash: %s", md5Chunk.getId(), md5Chunk.getMd5()));
            try {
                if (md5Chunk.read(false) != null) {
                    //System.out.println(String.format("Block %d successfully read from the file", md5Chunk.getId()));
                    count++;
                } else {
                    System.out.printf("Block %d not read from the file%n", md5Chunk.getId());
                }
            } catch (TftpException ex) {
                System.out.printf("Block %d not read from the file%n", md5Chunk.getId());
               //ERRORS.log(Level.SEVERE, null, ex);
            }
        }

        if (count != blocks.size()) {
            System.out.println("Unable to validate the blocks in the source file.");
            halt();
            answer = null;
        } else {
            System.out.println("File has been validated.");
        }
        
        return answer;
    }

    private SocketAddress negotiateSendTransfer(FileInfo fi) {
        SocketAddress answer = null;
        
        try {
            System.out.printf("Negotiating transfer settings with destination %s [%s]...%n", server.getHostName(), server.getHostAddress());
            TftpSendRequest request = new TftpSendRequest(socket, new InetSocketAddress(server, destPort), fi, TransferMode.OCTET);
            request.addDefaultOptions();
            boolean connected = request.connect();

            if (!connected) {
                System.out.printf("Unable to negotiate transfer settings with destination %s [%s]. Exiting...%n", server.getHostName(), server.getHostAddress());
            } else {
                answer = request.getOutgoing();
            }

        } catch (TftpException ex) {
            ERRORS.log(Level.SEVERE, null, ex);
        }
        
        return answer;
    }
}
