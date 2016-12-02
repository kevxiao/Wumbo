package com.star.patrick.wumbo.model.message;

import java.io.Serializable;

/**
 * Message content for a text message
 */
public class Text implements MessageContent, Serializable{
    private MessageType type = MessageType.TEXT;
    private String content;

    /**
     * Text message constructor
     * @param text Text to be sent and shown in the message
     */
    public Text(String text){
        content = text;
    }

    @Override
    public MessageType getType(){
        return type;
    }

    @Override
    public Object getMessageContent() {
        return content;
    }
}
