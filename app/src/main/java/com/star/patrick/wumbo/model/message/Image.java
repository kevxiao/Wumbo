package com.star.patrick.wumbo.model.message;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by jesse on 16/11/16.
 */

public class Image implements MessageContent, Serializable {
    private MessageType type = MessageType.IMAGE;
    private String filepath;

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
