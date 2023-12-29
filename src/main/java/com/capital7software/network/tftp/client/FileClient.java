/*
 * File: FileClient.java
 */
package com.capital7software.network.tftp.client;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vincent Palodichuk
 */
public class FileClient {
    private static final Logger ERRORS = Logger.getLogger(FileClient.class.getName());
    private final static int PORT = 69;
    private static final String DEFAULT_HOST = "localhost";
    private static final String MSG_FMT = "Host: %s [%s]%n";

    /**
     * @param args the command line arguments
     */
    public static void main(String @NotNull [] args) {
        if (args.length == 0 || args[0] == null || args[0].trim().isEmpty() || !Files.exists(Paths.get(args[0]))) {
            printUsage();
        } else {
            try {
                String hostname = DEFAULT_HOST;
                
                if (args.length == 2) {
                    hostname = args[1];
                }
                
                InetAddress ia = InetAddress.getByName(hostname);
                
                System.out.printf((MSG_FMT) + "%n", hostname, ia.getHostAddress());
                
                Thread sender = new ClientThread(ia, PORT, args[0]);
                sender.start();
            } catch (UnknownHostException | SocketException ex) {
                ERRORS.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    private static void printUsage() {
        System.out.println("A filename to a file that exists on this system is required.");
    }
}
