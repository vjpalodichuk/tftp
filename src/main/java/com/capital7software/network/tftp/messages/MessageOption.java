/**
 * File: MessageOption.java
 */
package com.capital7software.network.tftp.messages;

import com.capital7software.network.tftp.NetAscii;

/**
 *
 */
public interface MessageOption {
    /**
     * Returns the name of this Option
     *
     * @return The name of this Option or null if no name has been set
     */
    NetAscii getName();

    /**
     * Sets the name of this Option to the specified name
     *
     * @param name The name to set this Option to.
     */
    void setName(NetAscii name);

    /**
     * Returns the value of this Option
     *
     * @return The value of this Option or null if no value has been set
     */
    NetAscii getValue();

    /**
     * Sets the value of this Option to the specified value
     *
     * @param value The value to set this Option to.
     */
    void setValue(NetAscii value);

    /**
     * The required number of bytes to store the name and value of this Option in a byte buffer.
     *
     * @return The required number of bytes to store the name and value of this Option in a byte buffer.
     */
    int getByteSize();

    /**
     * Stores this Option's name and value in to a new byte buffer and returns the byte buffer
     *
     * @return The new byte buffer that contains the name and value of this Option
     */
    byte[] getBytes();

    /**
     * Retrieves the option name and value from the start of the specified byte buffer
     * and stores it in the name and value instance members.
     *
     * @param bytes The buffer that contains the data to read
     */
    void fromBytes(byte[] bytes);

    /**
     * Retrieves the option name and value from the specified byte buffer and offset
     * and stores it in the name and value instance members.
     *
     * @param bytes  The buffer that contains the data to read
     * @param offset The starting offset to read from
     */
    void fromBytes(byte[] bytes, int offset);
}
