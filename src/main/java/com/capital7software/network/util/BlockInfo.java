/*
 * File: BlockInfo.java
 */
package com.capital7software.network.util;

import com.capital7software.network.exception.TftpException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.logging.Logger;

/**
 *
 * @author Vincent Palodichuk
 */
public class BlockInfo implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(BlockInfo.class.getName());

    /**
     * 
     */
    public static final int DEFAULT_VALUE = -1;
    
    /**
     * 
     */
    public static final int MAX_BLOCK_SIZE = 65464;
    
    /**
     * 
     */
    public static final int DEFAULT_BLOCK_SIZE = 512;
    
    /**
     * 
     */
    public static final int MIN_BLOCK_SIZE = 1;
    
    /**
     * The id property
     */
    public static final String PROP_ID = "id";

    /**
     * The size property
     */
    public static final String PROP_SIZE = "size";

    /**
     * The filename property
     */
    public static final String PROP_FILENAME = "filename";

    /**
     * The offset property
     */
    public static final String PROP_OFFSET = "offset";

    /**
     * The MD5 Hash property
     */
    public static final String PROP_MD5 = "md5";

    private transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private String filename;
    private int id;
    private long offset;
    private long size;
    private final String md5;

    /**
     * Initializes an empty block
     */
    public BlockInfo() {
        this(null, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, null);
    }
    
    /**
     * Initializes a block with the specified values and no MD5 Hash
     * 
     * @param filename the filename that this block belongs to
     * @param id the id of this block. Can be any integer value
     * @param offset the offset in bytes in to filename that this block starts at
     * @param size the length in bytes of this block
     */
    public BlockInfo(String filename, int id, long offset, long size) {
        this(filename, id, offset, size, null);
    }
    
    /**
     * Initializes a block with the specified values
     * 
     * @param filename the filename that this block belongs to
     * @param id the id of this block. Can be any integer value
     * @param offset the offset in bytes in to filename that this block starts at
     * @param size the length in bytes of this block
     * @param md5 the MD5 Hash of this block
     */
    public BlockInfo(String filename, int id, long offset, long size, String md5) {
        this.filename = filename;
        this.id = id;
        this.offset = offset;
        this.size = size;
        this.md5 = md5;
    }
    
    /**
     * Get the MD5 Hash of this block
     *
     * @return the MD5 Hash of this block
     */
    public String getMd5() {
        return md5;
    }

    /**
     * Get the filename that this block belongs to
     *
     * @return the filename that this block belongs to
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Set the filename that this block belongs to
     *
     * @param filename the new filename that this block belongs to
     */
    public void setFilename(String filename) {
        if (!Objects.equals(this.filename, filename)) {
            String oldPath = this.filename;
            this.filename = filename;
            getPcs().firePropertyChange(PROP_FILENAME, oldPath, filename);
        }
    }

    /**
     * Get the size of this block in bytes
     *
     * @return the size of this block in bytes
     */
    public long getSize() {
        return size;
    }

    /**
     * Set the size of this block in bytes
     *
     * @param size the new size of this block in bytes
     */
    public void setSize(long size) {
        if (this.size != size) {
            long oldSize = this.size;
            this.size = size;
            getPcs().firePropertyChange(PROP_SIZE, oldSize, size);
        }
    }

    /**
     * Get the offset of this block in to the file that is associated with it.
     *
     * @return the offset where this block begins in the associated file.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Set the offset of this block in to the file that is associated with it.
     *
     * @param offset the new offset where this block begins in the associated file.
     */
    public void setOffset(long offset) {
        if (this.offset != offset) {
            long oldOffset = this.offset;
            this.offset = offset;
            getPcs().firePropertyChange(PROP_OFFSET, oldOffset, offset);
        }
    }

    /**
     * Get the id of this block
     *
     * @return the id of this block
     */
    public int getId() {
        return id;
    }

    /**
     * Set the id of this block
     *
     * @param id the new id of this block
     */
    public void setId(int id) {
        if (this.id != id) {
            long oldId = this.id;
            this.id = id;
            getPcs().firePropertyChange(PROP_ID, oldId, id);
        }
    }

    /**
     * Adds the specified PropertyChangeListener. Registers a
     * PropertyChangeListener that is notified when the properties of this
     * block change.
     *
     * @param listener the listener that will be notified of changes
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getPcs().addPropertyChangeListener(listener);
    }

    /**
     * Removes the specified PropertyChangeListener so that it is no longer
     * notified of changes to this block.
     *
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getPcs().removePropertyChangeListener(listener);
    }

    /**
     * Reads the data from the file for this block. If this block has an associated
 MD5 Hash, the read block is validated. If MD5 validation fails, a
 TftpException is thrown. If the block is successfully
 read and validated a byte array is returned.
 <p>
     * <b>Notes:</b>
     * <ul>
     *  <li>
     *   The first two bytes of the byte array are the OpCode (3).
     *  </li>
     *  <li>
     *   The next two bytes (byte 3 and 4) are the id of this block. 
     *  </li>
     * </ul>
     * 
     * @param output if true, the method will output what it is doing to the
     * console
     * @return a byte array with the data from the file that this block refers
     * to 
     * @throws TftpException Indicates that the MD5 Hash 
     * validation failed or that some other IO operation failed.
     */
    public byte[] read(boolean output) throws TftpException {
        byte[] answer;
        
        try (SeekableByteChannel sbc = Files.newByteChannel(Paths.get(filename), StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int)size);

            if (output) {
                System.out.printf("Reading Block %d...%n", id);
            }

            sbc.position(offset);

            if (size > 0) {
                long count = sbc.read(buffer);

                if (count <= 0) {
                    throw new TftpException("Failed to read block data from the file.");
                }

                if (output) {
                    System.out.printf("Read %d %s from offset %d%n", count, count == 1 ? "byte" : "bytes", offset);
                }
                
                if (md5 != null) {
                    ByteArrayInputStream stream = new ByteArrayInputStream(buffer.array());
                    String md5Read = MD5.hashByteArray(stream, false, (int)size, 0);

                    if (md5.compareToIgnoreCase(md5Read) != 0) {
                        throw new TftpException("MD5 Hash comparison failed.");
                    }

                    if (output) {
                        System.out.println("MDS Hash comparison passed.");
                    }
                }
            }
            
            answer = buffer.array();
        } catch (IOException ex) {
            throw new TftpException(ex.getMessage(), ex);
        }
        return answer;
    }
    
    /**
     * Reads the date from the file for this block. If this block has an associated
     * MD5 Hash, the read block is validated. If MD5 validation fails, a
     * TftpException is thrown. If the block is successfully
     * read and validated a byte array is returned. 
     * 
     * @param data the data to be written to the file associated with this block.
     * this cannot be null.
     * @param output if true, the method will output what it is doing to the
     * console
     * @return the actual number of bytes written
     * @throws TftpException Indicates that the MD5 Hash 
     * validation failed or that some other IO operation failed.
     * @throws IllegalArgumentException indicates that data is null.
     */
    public int write(byte[] data, boolean output) throws TftpException {
        int answer;
        
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null.");
        }
        
        if (data.length < size) {
            throw new IllegalArgumentException("The size of the buffer is smaller then the amount of data to write.");
        }
        
        try (SeekableByteChannel sbc = Files.newByteChannel(Paths.get(filename), StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.wrap(data, 0, (int) size);

            if (output) {
                System.out.printf("Writing Block %d...%n", id);
            }

            sbc.position(offset);

            answer = sbc.write(buffer);

            if (answer != size) {
                throw new TftpException("Failed to write block data to the file.");
            }
            
            if (output) {
                System.out.printf("Wrote %d %s starting at offset %d%n", answer, answer == 1 ? "byte" : "bytes", offset);
            }

            if (md5 != null) {
                String md5Read = MD5.hashByteArray(new ByteArrayInputStream(buffer.array()), false, (int) size, 0);

                if (md5.compareToIgnoreCase(md5Read) != 0) {
                    throw new TftpException("MD5 Hash comparison failed.");
                }

                if (output) {
                    System.out.println("MDS Hash comparison passed.");
                }
            }
        } catch (IOException ex) {
            throw new TftpException(ex.getMessage(), ex);
        }
        
        return answer;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.filename);
        hash = 97 * hash + this.id;
        hash = 97 * hash + (int) (this.offset ^ (this.offset >>> 32));
        hash = 97 * hash + (int) (this.size ^ (this.size >>> 32));
        hash = 97 * hash + Objects.hashCode(this.md5);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BlockInfo other = (BlockInfo) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.offset != other.offset) {
            return false;
        }
        if (this.size != other.size) {
            return false;
        }
        if (!Objects.equals(this.filename, other.filename)) {
            return false;
        }
        return Objects.equals(this.md5, other.md5);
    }

    @Override
    public String toString() {
        return "BlockInfo{" + "filename = " + filename + ", id = " + id + ", offset = " + offset + ", size = " + size + ", md5 = " + md5 + '}';
    }
    
    private PropertyChangeSupport getPcs() {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        
        return pcs;
    }
}
