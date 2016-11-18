package com.star.patrick.wumbo.message;

import java.io.Serializable;

/**
 * Created by jesse on 16/11/16.
 */

public class Text implements MessageContent, Serializable{
    private MessageType type = MessageType.TEXT;
    String content;

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
