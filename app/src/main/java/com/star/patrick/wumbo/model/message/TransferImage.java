package com.star.patrick.wumbo.model.message;

import java.io.Serializable;

/**
 * Message content for an image message to be transferred
 */
public class TransferImage implements MessageContent, Serializable {
    private MessageType type = MessageType.IMAGE;
    private byte[] content;

    /**
     * Constructor for image object to be transferred
     * @param image The byte array representing an encoded image bitmap
     */
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
