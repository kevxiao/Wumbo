package com.star.patrick.wumbo.message;

/**
 * Created by jesse on 16/11/16.
 */

public class Text implements MessageContent {
    private MessageType type = MessageType.TEXT;
    String content;

    public Text(String text){
        content = text;
    }

    @Override
    public Object getMessageContent() {
        return content;
    }
}
