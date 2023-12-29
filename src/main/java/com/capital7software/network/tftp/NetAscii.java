/*
 * File: NetAscii.java
 */
package com.capital7software.network.tftp;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * 
 */
public class NetAscii {
    public static final char CHAR_CR = '\r';
    public static final char CHAR_LF = '\n';
    public static final char CHAR_NULL = '\0';

    protected String original;
    protected String value;

    private NetAscii() {
        this(null);
    }

    public NetAscii(String string) {
        this.original = string;
        if (string != null) {
            this.value = convertToNetAscii(string);
        }
    }
    
    public static NetAscii fromBytes(byte[] bytes, int skipNulls) {
        return fromBytes(bytes, 0, skipNulls);
    }
    
    public static NetAscii fromBytes(byte[] bytes, int offset, int skipNulls) {
        List<Integer> nulls = new ArrayList<>();
        
        for (int i = offset; i < bytes.length && nulls.size() <= skipNulls; i++) {
            if (bytes[i] == CHAR_NULL) {
                nulls.add(i);
            }
        }
        
        offset = skipNulls > 0 ? nulls.get(skipNulls - 1) + 1 : offset;
        int length = skipNulls > 0 ? nulls.get(skipNulls) - (nulls.get(skipNulls - 1) + 1) : nulls.get(0) - offset;
        String string = new String(bytes, offset, length, StandardCharsets.UTF_8);
        
        return new NetAscii(string);
    }
    
    /**
     * Converts the specified string in UTF-8 format to NetAscii. The returned
     * string will have all new-line character replaced with carriage return and
     * new-line characters. 
     * 
     * @param string The string to convert to. Cannot be null or empty.
     * 
     * @return The specified string in NetAscii with all new-line sequences
     * replaced with carriage return new-line characters.
     */
    public static String convertToNetAscii(String string) {
        String answer = null;
        
        if (string != null) {
            if (string.isEmpty()) {
                answer = string;
            } else {
                answer = convertToCRLFs(string);
            }
        }
        
        return answer;
    }

    /**
     * Converts the specified string in NetAscii format to UTF-8. The returned
     * string will have only new-line character instead of carriage return and
     * new-line characters. 
     * 
     * @param string The string to convert from. Cannot be null or empty.
     * 
     * @return The specified string in UTF-8 with all carriage return new-line 
     * sequences replaced with just a new-line character.
     */
    public static String convertFromNetAscii(String string) {
        String answer = null;
        
        if (string != null) {
            if (string.isEmpty()) {
                answer = string;
            } else {
                answer = convertToHost(string);
            }
        }
        
        return answer;
    }

    protected static String convertToCRLFs(String string) {
        String answer = string;
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        int numCr = 0;
        int numLf = 0;
        int numAdds = 0;
        
        for (int i = 0; i < bytes.length; i++) {
            switch (bytes[i]) {
                case CHAR_CR -> {
                    if (i + 1 < bytes.length) {
                        if (bytes[i + 1] != CHAR_LF && bytes[i + 1] != CHAR_NULL) {
                            numAdds++;
                        }
                    } else {
                        numAdds++;
                    }
                    numCr++;
                }
                case CHAR_LF -> {
                    if (i - 1 >= 0) {
                        if (bytes[i - 1] != CHAR_LF) {
                            numAdds++;
                        }
                    } else {
                        numAdds++;
                    }
                    numLf++;
                }
            }
        }
        
        if (numCr != numLf) {
            byte[] newBytes = new byte[bytes.length + numAdds];
            numAdds = 0;

            for (int i = 0, j = 0; i < bytes.length && j < newBytes.length; i++, j++) {
                switch (bytes[i]) {
                    case CHAR_CR -> {
                        if (i + 1 < bytes.length) {
                            if (bytes[i + 1] != CHAR_LF && bytes[i + 1] != CHAR_NULL) {
                                newBytes[j] = bytes[i];
                                newBytes[++j] = CHAR_NULL;
                                numAdds++;
                            } else {
                                newBytes[j] = bytes[i];
                            }
                        } else {
                            newBytes[j] = bytes[i];
                            newBytes[++j] = CHAR_NULL;
                            numAdds++;
                        }
                    }
                    case CHAR_LF -> {
                        if (i - 1 >= 0) {
                            if (bytes[i - 1] != CHAR_CR) {
                                newBytes[j] = CHAR_CR;
                                newBytes[++j] = bytes[i];
                                numAdds++;
                            } else {
                                newBytes[j] = bytes[i];
                            }
                        } else {
                            newBytes[j] = CHAR_CR;
                            newBytes[++j] = bytes[i];
                            numAdds++;
                        }
                    }
                    default -> newBytes[j] = bytes[i];
                }
                
                answer = new String(newBytes, 0, bytes.length + numAdds, StandardCharsets.UTF_8);
            }
        }
        
        return answer;
    }
    
    protected static String convertToHost(String string) {
        String answer = string;
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        
        byte[] newBytes = new byte[bytes.length];

        int removed = 0;
        boolean convert = !System.lineSeparator().equals("\r\n");
        
        for (int i = 0, j = 0; i < bytes.length && j < newBytes.length; i++, j++) {
            switch (bytes[i]) {
                case CHAR_CR -> {
                    if (convert && i + 1 < bytes.length) {
                        if (bytes[i + 1] == CHAR_LF) {
                            newBytes[j] = bytes[++i];
                            removed++;
                        } else if (bytes[i + 1] == CHAR_NULL) {
                            ++i;
                        } else {
                            newBytes[j] = bytes[i];
                        }
                    } else {
                        newBytes[j] = bytes[i];
                    }
                }
                case CHAR_LF -> {
                    if (convert && i - 1 >= 0) {
                        if (bytes[i - 1] == CHAR_CR) {
                            newBytes[j - 1] = bytes[i++];
                            removed++;
                            if (i < bytes.length) {
                                newBytes[j] = bytes[i];
                            } else {
                                newBytes[j] = CHAR_LF;
                            }
                        } else {
                            newBytes[j] = bytes[i];
                        }
                    } else {
                        newBytes[j] = bytes[i];
                    }
                }
                case CHAR_NULL -> removed++;
                default -> newBytes[j] = bytes[i];
            }

            answer = new String(newBytes, 0, bytes.length - removed, StandardCharsets.UTF_8);
        }
        
        return answer;
    }

    /**
     * Returns the original unaltered UTF-8 string.
     * 
     * @return the original unaltered UTF-8 string.
     */
    public String getOriginal() {
        return original;
    }

    /**
     * Returns the original string converted into NetAscii string.
     * 
     * @return the original string converted into NetAscii string.
     */
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.original);
        hash = 61 * hash + Objects.hashCode(this.value);
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
        final NetAscii other = (NetAscii) obj;
        if (!Objects.equals(this.original, other.original)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        return value;
    }
    
    public int length() {
        return value.length();
    }
    
    public byte[] getBytes() {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
