/*
 * File: FileInfo.java
 */
package com.capital7software.network.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vincent Palodichuk
 */
public class FileInfo implements Serializable {
    private static final Logger ERRORS = Logger.getLogger(FileInfo.class.getName());
    private static final int MIN_NUM_BLOCKS = 12;
    
    /**
     * The filename property
     */
    public static final String PROP_FILENAME = "filename";
    
    /**
     * The blocks property
     */
    public static final String PROP_BLOCKS = "blocks";
    
    /**
     * The size property
     */
    public static final String PROP_SIZE = "size";
    
    /**
     * The md5 property
     */
    public static final String PROP_MD5 = "md5";

    /**
     * The blockSize property
     */
    public static final String PROP_BLOCK_SIZE = "blockSize";

    private transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private String filename;
    private long size;
    private String md5;
    private List<BlockInfo> blocks = new ArrayList<>();
    private long blockSize;
    
    /**
     * Get the MD5 Hash of this file
     *
     * @return the MD5 Hash of this file
     */
    public String getMd5() {
        return md5;
    }

    /**
     * Get the size of this file in bytes
     *
     * @return the size of this file in bytes
     */
    public long getSize() {
        return size;
    }
    
    /**
     * Returns an unmodifiable list view of the blocks contained within this
 file
     *
     * @return the list of blocks that represent this file
     */
    public List<BlockInfo> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    /**
     * Get the filename that this file belongs to
     *
     * @return the filename that this file belongs to
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Set the filename that this block belongs to. When the filename is set,
 the size, block size, blocks, and MD5 Hash are calculated if the file
 exists.
     *
     * @param filename the new filename that this block belongs to
     */
    public void setFilename(String filename) {
        if (!Objects.equals(this.filename, filename)) {
            String oldPath = this.filename;
            this.filename = filename;
            getPcs().firePropertyChange(PROP_FILENAME, oldPath, filename);
            
            calculateFileInfo();
        }
    }

    /**
     * Populates the properties of this file info based on the file associated
     * with this file. If the file does not exist, then nothing is changed.
     * Otherwise, the size, MD5 Hash, block size, and blocks are all populated.
     */
    public void calculateFileInfo() {
        final Path path = Paths.get(filename);
        if (Files.exists(path)) {
            try {
                long oldSize = this.size;
                String oldMd5 = this.md5;
                long oldBlockSize = this.blockSize;
                List<BlockInfo> oldBlocks = this.blocks;
                this.size = Files.size(path);
                this.md5 = MD5.hashFile(path);
                this.blockSize = calculateBlockSize(this.size, MIN_NUM_BLOCKS, BlockInfo.MIN_BLOCK_SIZE, BlockInfo.MAX_BLOCK_SIZE);
                long numBlocks = this.size / this.blockSize;
                if (this.size % this.blockSize != 0) {
                    numBlocks++;
                }
                this.blocks = MD5.hashBlocks(path, numBlocks, this.blockSize);
                
                if (this.blocks.size() > 0) {
                    BlockInfo block = this.blocks.get(this.blocks.size() - 1);
                    if (block.getSize() == this.blockSize) {
                        blocks.add(MD5.hashEmptyBlock(path, block.getId() + 1));
                    }
                }
                
                getPcs().firePropertyChange(PROP_SIZE, oldSize, this.size);
                getPcs().firePropertyChange(PROP_MD5, oldMd5, this.md5);
                getPcs().firePropertyChange(PROP_BLOCK_SIZE, oldBlockSize, this.blockSize);
                getPcs().firePropertyChange(PROP_SIZE, oldBlocks, Collections.unmodifiableList(this.blocks));
            }catch (IOException ex) {
                ERRORS.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Get the size for each of the blocks in this file
     * The size of the last block in the file may be less than or equal
     * to this value
     *
     * @return the size for each block in this file
     */
    public long getBlockSize() {
        return blockSize;
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
     * Used to calculate the size of each block given the specified requirements
     * @param length the length of the file in bytes. Must be greater than 0
     * @param minBlocks the minimum number of blocks to divide the file into
     * @param minSize the minimum size that a block may be
     * @param maxSize the maximum size that a block may be
     * @throws IllegalArgumentException indicates that minBlocks is less than 1
     * @return the long
     */
    protected static long calculateBlockSize(long length, int minBlocks, int minSize, int maxSize) {
        if (minBlocks <= 0) {
            throw new IllegalArgumentException("minBlocks must be greater than 0.");
        }
        
        long answer;
        
        answer = length / minBlocks;
        
        if (answer > maxSize) {
            answer = maxSize;
        } else if (answer < minSize) {
            answer = minSize;
        }
        
        return answer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileInfo fileInfo)) return false;
        return getSize() == fileInfo.getSize() &&
                Objects.equals(getFilename(), fileInfo.getFilename()) &&
                Objects.equals(getMd5(), fileInfo.getMd5());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFilename(), getSize(), getMd5());
    }

    @Override
    public String toString() {
        return "FileInfo{" + "filename = " + filename + ", size = " + size + ", md5 = " + md5 + ", blockSize = " + blockSize + ", blocks = " + blocks + '}';
    }

    public int getNumBlocks() {
        return blocks.size();
    }
    
    private PropertyChangeSupport getPcs() {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        
        return pcs;
    }
}
