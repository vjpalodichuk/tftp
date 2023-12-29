/*
 * File: OptionableMessage.java
 */
package com.capital7software.network.tftp.messages;

import com.capital7software.network.exception.TftpException;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Vincent Palodichuk
 */
public interface OptionableMessage {
    List<MessageOption> getOptions() throws TftpException;
    boolean addOption(MessageOption option) throws TftpException;
    boolean addOptionAll(Collection<MessageOption> options) throws TftpException;

    static boolean addOptionAll(Collection<MessageOption> sourceOptions, List<MessageOption> destinationOptions) {
        var answer = true;

        if (sourceOptions != null) {
            for (var option : sourceOptions) {
                var temp = destinationOptions.add(option);

                if (!temp && answer) {
                    answer = false;
                }
            }
        } else {
            answer = false;
        }

        return answer;
    }
}
