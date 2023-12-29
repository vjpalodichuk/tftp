/*
 * File: TftpOption.java
 */
package com.capital7software.network.tftp;

import com.capital7software.network.tftp.messages.MessageOption;
import com.capital7software.network.tftp.messages.MessageOptionCode;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Options that control the connection as well as sending and receiving data.
 */
public class TftpOption implements MessageOption {
    protected NetAscii name;
    protected NetAscii value;

    public TftpOption() {
        this((NetAscii) null, null);
    }

    public TftpOption(String name, String value) {
        this(new NetAscii(name), new NetAscii(value));
    }

    public TftpOption(MessageOptionCode code, NetAscii value) {
        this(new NetAscii(code.getValue()), value);
    }

    public TftpOption(MessageOptionCode code, String value) {
        this(new NetAscii(code.getValue()), new NetAscii(value));
    }

    public TftpOption(NetAscii name, NetAscii value) {
        this.name = name;
        this.value = value;
    }

    public TftpOption(byte[] bytes) {
        this(bytes, 0);
    }

    public TftpOption(byte[] bytes, int offset) {
        fromBytes(bytes, offset);
    }

    @Override
    public NetAscii getName() {
        return name;
    }

    @Override
    public void setName(NetAscii name) {
        this.name = name;
    }

    @Override
    public NetAscii getValue() {
        return value;
    }

    @Override
    public void setValue(NetAscii value) {
        this.value = value;
    }

    @Override
    public int getByteSize() {
        int answer = -1;

        if (name != null && value != null) {
            answer = name.getValue().length() + value.getValue().length() + 2;
        }

        return answer;
    }

    @Override
    public byte[] getBytes() {
        byte[] answer = null;

        if (name != null && value != null) {
            byte[] sName = name.getValue().getBytes(StandardCharsets.UTF_8);
            byte[] sValue = value.getValue().getBytes(StandardCharsets.UTF_8);
            answer = new byte[sName.length + sValue.length + 2];

            System.arraycopy(sName, 0, answer, 0, sName.length);
            answer[sName.length] = 0;
            System.arraycopy(sValue, 0, answer, sName.length + 1, sValue.length);
            answer[answer.length - 1] = 0;
        }

        return answer;
    }

    @Override
    public final void fromBytes(byte[] bytes) {
        fromBytes(bytes, 0);
    }

    @Override
    public final void fromBytes(byte[] bytes, int offset) {
        name = NetAscii.fromBytes(bytes, offset, 0);
        value = NetAscii.fromBytes(bytes, offset, 1);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.name);
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
        final TftpOption other = (TftpOption) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        return "{name = " + name + ", value = " + value + '}';
    }
}
