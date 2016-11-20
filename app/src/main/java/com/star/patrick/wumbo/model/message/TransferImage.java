package com.star.patrick.wumbo.model.message;

import java.io.Serializable;

/**
 * Created by Kevin Xiao on 2016-11-19.
 */

public class TransferImage implements MessageContent, Serializable {
    private MessageType type = MessageType.IMAGE;
    private byte[] content;

    public TransferImage(byte[] image) {
        this.content = image;
    }

    @Override
    public Object getMessageContent() {
        return content;
    }

    @Override
    public MessageType getType() {
        return type;
    }
}
