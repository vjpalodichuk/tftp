/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.capital7software.network.tftp;

import com.capital7software.network.exception.TftpException;
import com.capital7software.network.tftp.messages.*;
import com.capital7software.network.util.FileInfo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * 
 */
public class TftpSendRequest extends TftpConnection {
    private final static Logger ERRORS = Logger.getLogger(TftpSendRequest.class.getName());
    protected FileMessage message;

    public TftpSendRequest(DatagramSocket server, SocketAddress dest, FileInfo fileInfo, TransferMode mode) {
        super(server, dest, fileInfo, mode);
    }
    
    @Override
    public boolean connect() throws TftpException {
        boolean answer;
        try {
            FileMessage request = new FileMessage(getOpCode(), fileInfo.getFilename(), TransferMode.OCTET);
            if (!options.isEmpty()) {
                request.addOptionAll(options);
            }
            
            byte[] data = request.getPayload();
            byte[] input = new byte[data.length * 2];
            
            DatagramPacket output = new DatagramPacket(data, data.length, address);
            DatagramPacket receive = new DatagramPacket(input, input.length);
            
            socket.send(output); // Send the connection request
            socket.receive(receive); // Wait for the response
            
            outgoing = receive.getSocketAddress();
            
            Message msg = MessageFactory.getMessage(receive.getData(), receive.getOffset(), receive.getLength());
            
            AckMessage response = new AckMessage();
            
            if (null == response.getOpCode()) {
                System.out.println("Received Unknown Response: " + response.getOpCode());
                answer = false;
                closeConnection();
            } else switch (msg.getOpCode()) {
                case ACK -> {
                    response.fromPayload(receive.getData(), 0, receive.getLength());
                    if (response.getId() == 0) {
                        System.out.println("Using TFTP with no options.");
                        answer = true;
                        options.clear();
                    } else {
                        System.out.println("Received Unknown Response: " + response.getOpCode());
                        answer = false;
                        closeConnection();
                    }
                }
                case OACK -> {
                    response.fromPayload(receive.getData(), 0, receive.getLength());
                    if (response.getId() == 0) {
                        System.out.println("Using TFTP with options: " + response.getOptions().toString());
                        options.clear();
                        options.addAll(response.getOptions());
                        answer = true;
                    } else {
                        System.out.println("Received Unknown Response: " + response.getOpCode());
                        answer = false;
                        closeConnection();
                    }
                }
                case ERROR -> {
                    ErrorMessage error = (ErrorMessage) msg;
                    System.out.println("Error received: " + error.getMsg().getValue());
                    answer = false;
                    closeConnection();
                }
                default -> {
                    System.out.println("Received Unknown Response: " + response.getOpCode());
                    answer = false;
                    closeConnection();
                }
            }
        } catch (SocketTimeoutException ex) {
            System.out.println("Time out waiting to connect");
            answer = false;
            closeConnection();
        } catch (IOException ex) {
            ERRORS.log(Level.SEVERE, null, ex);
            answer = false;
            closeConnection();
        }
        
        return answer;
    }

    private void closeConnection() {
        outgoing = null;
    }

    @Override
    public OpCode getOpCode() {
        return OpCode.WRQ;
    }
    
}
