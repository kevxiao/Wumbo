package com.star.patrick.wumbo;

import com.star.patrick.wumbo.message.EncryptedMessage;

/**
 * Created by giliam on 11/19/2016.
 */

public interface MessageCourier {
    void send(EncryptedMessage msg);
}