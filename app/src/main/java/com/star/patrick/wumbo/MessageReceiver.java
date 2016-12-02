package com.star.patrick.wumbo;

import com.star.patrick.wumbo.model.message.EncryptedMessage;

public interface MessageReceiver {
    void receive(EncryptedMessage msg);
}
