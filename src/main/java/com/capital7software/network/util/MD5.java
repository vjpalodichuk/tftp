/**
 * File: MD5.java
 */
package com.capital7software.network.util;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vincent Palodichuk
 */
public class MD5 {
    private static final Logger LOGGER = Logger.getLogger(MD5.class.getName());
    private static final int DEFAULT_BUFFER_SIZE = 32768;
    
    public static String hashFile(Path file) {
        return hashFile(file, false);
    }
    
    public static String hashFile(Path file, boolean output) {
        String answer = "";
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(file, StandardOpenOption.READ));
                    DigestInputStream dis = new DigestInputStream(bis, md)) {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int count;
                
                if (output) {
                    System.out.println("Generating File MD5 Hash...");
                }
                
                while ((count = dis.read(buffer)) > 0) {
                    if (output) {
                        System.out.printf("Read %d %s%n", count, count == 1 ? "byte" : "bytes");
                    }
                }

                answer = hashToString(md, output);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return answer;
    }

    @NotNull
    private static String hashToString(MessageDigest md, boolean output) {
        var buffer = md.digest();
        var sb = new StringBuilder();

        for (byte b : buffer) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        var answer = sb.toString();

        if (output) {
            System.out.printf("MDS Hash: %s%n", answer);
        }
        return answer;
    }

    public static String hashByteArray(ByteArrayInputStream is) {
        return hashByteArray(is, false);
    }
    
    public static String hashByteArray(ByteArrayInputStream is, boolean output) {
        return hashByteArray(is, output, is.available(), 0);
    }
    
    public static String hashByteArray(ByteArrayInputStream is, boolean output, int bufferSize, int offset) {
        String answer = "";
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[bufferSize];
            
            if (output) {
                System.out.println("Generating Byte Array MD5 Hash...");
            }
            
            int count;
            var skipped = is.skip(offset);

            if (skipped != offset) {
                LOGGER.log(Level.WARNING, "The number of bytes to skip: " + offset + " and actually skipped: "
                        + skipped + " are not equal");
            }

            count = is.read(buffer, 0, bufferSize);
            md.update(buffer, 0, count);

            if (output) {
                System.out.printf("Read %d %s%n", count, count == 1 ? "byte" : "bytes");
            }

            answer = hashToString(md, output);

        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return answer;
    }
    
    public static List<BlockInfo> hashBlocks(Path file, long numberOfBlocks, long blockSize) {
        return hashBlocks(file, numberOfBlocks, blockSize, false);
    }
    
    public static List<BlockInfo> hashBlocks(Path file, long numberOfBlocks, long blockSize, boolean output) {
        List<BlockInfo> answer = new ArrayList<>((int)numberOfBlocks);
        
        for (int i = 0; i < numberOfBlocks; i++) {
            try (SeekableByteChannel sbc = Files.newByteChannel(file, StandardOpenOption.READ)) {
                long size = blockSize;
                long offset = i * blockSize;
                if (i + 1 == numberOfBlocks && Files.size(file) % blockSize != 0) {
                    size = (Files.size(file) % blockSize);
                }
                ByteBuffer buffer = ByteBuffer.allocate((int)size);

                if (output) {
                    System.out.printf("Generating Block %d MD5 Hash...%n", i + 1);
                }

                sbc.position(offset);
                
                long count = sbc.read(buffer);

                if (count <= 0) {
                    break;
                }

                if (output) {
                    System.out.printf("Read %d %s from offset %d%n", count, count == 1 ? "byte" : "bytes", i * blockSize);
                }
                
                byte[] byteBuffer = buffer.array();
                
                // Shrink the byte array if fewer bytes were read than allocated.
                // If we don't, then the MD5 hash comparison will fail when
                // this block is later read from the BlockInfo object.
                if (byteBuffer.length != count) {
                    byte[] temp = new byte[(int)count];
                    System.arraycopy(byteBuffer, 0, temp, 0, (int)count);
                    byteBuffer = temp;
                }

                BlockInfo ci = new BlockInfo(file.toString(), i + 1, offset, count,
                        hashByteArray(new ByteArrayInputStream(byteBuffer)));

                if (output) {
                    System.out.printf("Block %d MDS Hash: %s%n", i + 1, ci.getMd5());
                }
                
                answer.add(ci);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        
        return answer;
    }
    
    /**
     * Used to get the hash and block information for a zero byte block.
     * 
     * @param file The Path that contains the file to hash
     * @param id The block id
     * @return A BlockInfo instance with the hash
     */
    public static BlockInfo hashEmptyBlock(Path file, int id) {
        return hashEmptyBlock(file, id, false);
    }
    
    /**
     * Used to get the hash and block information for a zero byte block.
     *
     * @param file The Path that contains the file to hash
     * @param id The block id
     * @param output If true, outputs information to the system console
     * @return A BlockInfo instance with the hash
     */
    public static BlockInfo hashEmptyBlock(Path file, int id, boolean output) {
        BlockInfo answer;
        
        if (output) {
            System.out.printf("Generating Empty Block %d MD5 Hash...%n", id);
        }

        answer = new BlockInfo(file.toString(), id, 0, 0,
                        hashByteArray(new ByteArrayInputStream(new byte[0])));

        if (output) {
            System.out.printf("Block %d MDS Hash: %s%n", id, answer.getMd5());
        }

        return answer;
    }
}
