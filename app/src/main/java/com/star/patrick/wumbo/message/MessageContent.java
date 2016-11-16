package com.star.patrick.wumbo.message;

/**
 * Created by jesse on 16/11/16.
 */

public interface MessageContent {
    Object getMessageContent();
    enum MessageType{
        TEXT,
        IMAGE
    }
}
