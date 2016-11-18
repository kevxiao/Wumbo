package com.star.patrick.wumbo.message;

import java.io.Serializable;

/**
 * Created by jesse on 16/11/16.
 */

public interface MessageContent{
    Object getMessageContent();
    MessageType getType();
    enum MessageType{
        TEXT,
        IMAGE
    }
}
