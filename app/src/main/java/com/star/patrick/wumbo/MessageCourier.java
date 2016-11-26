package com.star.patrick.wumbo;

import com.star.patrick.wumbo.model.message.EncryptedMessage;

/**
 * An interface that acts as a courier that handles sending messages
 */
public interface MessageCourier extends MessageReceiver {
    void send(EncryptedMessage msg);
}
