package com.star.patrick.wumbo.model.message;

import android.net.Uri;

import java.io.Serializable;

/**
 * Local message content for an image message
 */
public class Image implements MessageContent, Serializable {
    private MessageType type = MessageType.IMAGE;
    private String filepath;

    /**
     * Local image message constructor
     * @param uri URI for the image
     */
    public Image(Uri uri) {
        filepath = uri.toString();
    }

    @Override
    public MessageType getType(){
        return type;
    }

    @Override
    public Object getMessageContent() {
        return filepath;
    }
}
