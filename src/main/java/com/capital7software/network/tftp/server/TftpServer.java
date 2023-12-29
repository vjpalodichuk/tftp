/**
 * File: TftpServer.java
 */
package com.capital7software.network.tftp.server;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.ErrorCode;
import com.capital7software.network.tftp.NetAscii;
import com.capital7software.network.tftp.OpCode;
import com.capital7software.network.tftp.messages.*;
import com.capital7software.network.util.BlockInfo;
import com.capital7software.network.util.MD5;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Vincent Palodichuk
 */
public class TftpServer implements Runnable {
    private final static String DEFAULT_ERROR_MESSAGE = "An unknown error has occurred.";
    public final static String WRITING_MSG = "Wrote: [%s]-[%s]-[%s]%n";
    public final static String RECEIVING_MSG = "Received: [%s]-[%s]-[%s]%n";
    private final static int DEFAULT_TIMEOUT = 10000; // 10 seconds.
    private final static String SUFFIX = "_received";
    public final static int DEFAULT_PORT = 69;
    public final static int DEFAULT_BUFFER_SIZE = 16384; // 16 KB
    public final static String SHUTDOWN_MSG = ".";
    private final int bufferSize; // in bytes
    private final int port;
    private static final Logger ERRORS = Logger.getLogger(TftpServer.class.getName());
    private volatile boolean isShutDown;

    public TftpServer(int port, int bufferSize) {
        this.isShutDown = false;
        this.bufferSize = bufferSize;
        this.port = port;
    }

    public TftpServer(int port) {
        this(port, DEFAULT_BUFFER_SIZE);
    }

    public TftpServer() {
        this(DEFAULT_PORT);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[bufferSize];
        try (DatagramSocket socket = new DatagramSocket(port)) {
            socket.setSoTimeout(DEFAULT_TIMEOUT);

            while (true) {
                if (isShutDown) {
                    System.out.println("Shutting down...");
                    return;
                }

                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

                try {
                    // Wait to receive wake-up packet
                    System.out.println("Waiting for a request...");
                    socket.receive(incoming);
                    Message msg = MessageFactory.getMessage(incoming.getData(), incoming.getOffset(), incoming.getLength());
                    System.out.println("File Receiver woke-up");

                    if (msg == null || (msg.getOpCode() != OpCode.RRQ && msg.getOpCode() != OpCode.WRQ)) {
                        sendError(socket, incoming, ErrorCode.ILLEGAL_TFTP_OPERATION, "Unknown packet");
                        shutDown();
                    } else if (msg.getOpCode() == OpCode.WRQ) {
                        FileMessage message = (FileMessage) msg;
                        String file = message.getFilename().getValue();
                        String mode = message.getMode().getValue();

                        String realFile = prepareFile(file);

                        if (realFile == null) {
                            System.out.printf("File %s already exists. Exiting...%n", file);
                            sendError(socket, incoming, ErrorCode.FILE_ALREADY_EXISTS, "The file already exists on this server.");
                            shutDown();
                        } else {
                            System.out.printf("Preparing to receive %s in %s mode%n", file, mode);

                            int blockSize = 512;
                            System.out.printf("Block size set to %d byte(s)%n", blockSize);
                            int timeout = DEFAULT_TIMEOUT;
                            System.out.printf("Timeout set to %d second(s)%n", timeout / 1_000);

                            String md5 = null;
                            long numBlocks = -1;

                            List<MessageOption> options = new LinkedList<>();

                            for (MessageOption option : message.getOptions()) {
                                if (Objects.equals(option.getName().getValue(), MessageOptionCode.BLOCK_SIZE.getValue())) {
                                    int newBlockSize = Integer.parseInt(option.getValue().getValue());
                                    if (newBlockSize != blockSize) {
                                        blockSize = newBlockSize;
                                        System.out.printf("Block size changed to %d byte(s)%n", blockSize);
                                    }
                                    options.add(option);
                                } else if (Objects.equals(option.getName().getValue(), MessageOptionCode.BLOCK_COUNT.getValue())) {
                                    numBlocks = Long.parseLong(option.getValue().getValue());
                                    System.out.printf("Number of blocks to receive is %d%n", numBlocks);
                                    options.add(option);
                                } else if (Objects.equals(option.getName().getValue(), MessageOptionCode.FILE_MD5.getValue())) {
                                    md5 = option.getValue().getValue();
                                    System.out.printf("MD5 Hash of file being received is %s%n", md5);
                                    options.add(option);
                                } else if (Objects.equals(option.getName().getValue(), MessageOptionCode.FILE_SIZE.getValue())) {
                                    var fileSize = Long.parseLong(option.getValue().getValue());
                                    System.out.printf("File size is %d byte(s)%n", fileSize);
                                    options.add(option);
                                } else if (Objects.equals(option.getName().getValue(), MessageOptionCode.TIMEOUT.getValue())) {
                                    int newTimeout = Integer.parseInt(option.getValue().getValue()) * 1_000;
                                    if (newTimeout != timeout) {
                                        timeout = newTimeout;
                                        socket.setSoTimeout(timeout);
                                        System.out.printf("Timeout changed to %d second(s)%n", timeout / 1_000);
                                    }
                                    options.add(option);
                                }
                            }

                            oackHello(socket, incoming);
                            System.out.println("Said hello");
                            if (blockSize > buffer.length) {
                                int oldLength = buffer.length;
                                buffer = new byte[blockSize + 8];
                                incoming.setData(buffer);
                                System.out.printf("Changed size of the receive buffer from %d bytes to %d bytes.%n", oldLength, buffer.length);
                            }
                            System.out.println("Waiting for file data...");
                            receiveFile(socket, incoming, options, (int) numBlocks, blockSize, realFile, md5);
                            System.out.println("File received.");
                            shutDown();
                        }
                    }
                } catch (SocketTimeoutException ignored) {
                } catch (IOException ex) {
                    ERRORS.log(Level.WARNING, null, ex);
                } catch (TftpException ex) {
                    ERRORS.log(Level.SEVERE, null, ex);
                }
            }
        } catch (SocketException ex) {
            ERRORS.log(Level.SEVERE, "Could not bind to port: " + port, ex);
        }
    }

    public void shutDown() {
        this.isShutDown = true;
    }

    public void echo(DatagramSocket socket, DatagramPacket packet) throws IOException {
        DatagramPacket outgoing = new DatagramPacket(packet.getData(), packet.getLength(), packet.getSocketAddress());
        socket.send(outgoing);
    }

    public void ackHello(DatagramSocket socket, DatagramPacket packet) throws IOException {
        try {
            AckMessage message = new AckMessage();
            DatagramPacket outgoing = new DatagramPacket(message.getPayload(), message.getPayloadSize(), packet.getSocketAddress());
            socket.send(outgoing);
        } catch (TftpException ex) {
            ERRORS.log(Level.SEVERE, null, ex);
        }
    }

    public void oackHello(DatagramSocket socket, DatagramPacket packet) throws IOException {
        try {
            AckMessage message = new AckMessage(OpCode.OACK);
            FileMessage fMessage = new FileMessage();

            fMessage.fromPayload(packet.getData(), packet.getOffset(), packet.getLength());

            message.addOptionAll(fMessage.getOptions());

            DatagramPacket outgoing = new DatagramPacket(message.getPayload(), message.getPayloadSize(), packet.getSocketAddress());
            socket.send(outgoing);
        } catch (TftpException ex) {
            ERRORS.log(Level.SEVERE, null, ex);
        }
    }

    public void ackData(DatagramSocket socket, DatagramPacket packet) throws IOException {
        try {
            AckMessage message = new AckMessage();
            DataMessage dMessage = new DataMessage();
            dMessage.fromPayload(packet.getData(), packet.getOffset(), packet.getLength());
            message.setId(dMessage.getId());
            DatagramPacket outgoing = new DatagramPacket(message.getPayload(), message.getPayloadSize(), packet.getSocketAddress());
            socket.send(outgoing);
        } catch (TftpException ex) {
            ERRORS.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TftpServer server = new TftpServer();
        Thread t = new Thread(server);
        t.start();
    }

    private String prepareFile(String inFile) {
        String answer = "";
        try {
            String directory = System.getProperty("user.dir");
            String fullPath = inFile;
            String ext = "";
            String name;

            int extIndex = fullPath.lastIndexOf('.');

            if (extIndex >= 0) {
                ext = fullPath.substring(extIndex);
            }

            int index = fullPath.lastIndexOf(File.separatorChar);

            if (index >= 0) {
                name = fullPath.substring(index + 1, extIndex >= 0 ? extIndex : fullPath.length());
            } else {
                fullPath = Paths.get(fullPath).getFileName().toString();

                extIndex = fullPath.lastIndexOf('.');

                if (extIndex >= 0) {
                    ext = fullPath.substring(extIndex);
                }

                name = fullPath.substring(0, extIndex >= 0 ? extIndex : fullPath.length());
            }

            String outFileName = name + SUFFIX + ext;

            answer = directory + File.separatorChar + outFileName;
            Path path = Paths.get(answer);
            if (Files.exists(path)) {
                answer = null;
            } else {
                Files.createFile(path);
            }

            return answer;
        } catch (IOException ex) {
            ERRORS.log(Level.SEVERE, null, ex);
        }
        return answer;
    }

    private void receiveFile(DatagramSocket socket, DatagramPacket incoming, List<MessageOption> options, int numBlocks, int blockSize, String filename, String md5) throws TftpException, IOException {
        for (int i = 0; i < numBlocks; i++) {
            socket.receive(incoming);
            DataMessage message = new DataMessage();
            message.fromPayload(incoming.getData(), incoming.getOffset(), incoming.getLength());

            if (i + 1 != message.getId()) {
                sendError(socket, incoming, ErrorCode.ILLEGAL_TFTP_OPERATION, "Unexpected block number received");
                break;
            }

            long offset = ((long) message.getId() - 1) * blockSize;
            byte[] buffer = message.getBlock();
            long end = buffer != null ? offset + (long) buffer.length : offset;
            System.out.printf((RECEIVING_MSG), message.getId(), offset, end - 1);
            BlockInfo block = new BlockInfo(filename, message.getId(), offset, i + 1 == numBlocks ? Objects.requireNonNull(buffer).length : blockSize);
            try {
                if (offset < 0) {
                    sendError(socket, incoming, ErrorCode.NOT_DEFINED, "Overflow error.");
                    System.out.println("Overflow error. Exiting...");
                    throw new TftpException("Overflow error!");
                }
                block.write(buffer, true);
            } catch (TftpException ex) {
                sendError(socket, incoming, ErrorCode.ACCESS_VIOLATION, ex.getMessage());
                throw ex;
            }
            System.out.printf((WRITING_MSG), message.getId(), offset, end - 1);
            ackData(socket, incoming);
        }

        String fileMd5 = MD5.hashFile(Paths.get(filename), false);

        if (md5 != null) {
            if (md5.compareToIgnoreCase(fileMd5) != 0) {
                System.out.println("MD5 Hash validation failed for the received file.");
            } else {
                System.out.println("MD5 Hash validated for the received file.");
            }
        } else {
            System.out.println("Skipping MD5 Hash validation.");
        }
    }

    private void sendError(DatagramSocket socket, DatagramPacket incoming, ErrorCode code, String msg) {
        try {
            ErrorMessage message = new ErrorMessage();

            message.setErrorCode(code);
            if (msg == null || msg.isEmpty()) {
                msg = DEFAULT_ERROR_MESSAGE;
            }

            message.setMsg(new NetAscii(msg));
            DatagramPacket outgoing = new DatagramPacket(message.getPayload(), message.getPayloadSize(), incoming.getSocketAddress());
            socket.send(outgoing);
        } catch (IOException ex) {
            ERRORS.log(Level.SEVERE, null, ex);
        }
    }
}
