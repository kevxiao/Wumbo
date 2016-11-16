package com.star.patrick.wumbo.message;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by jesse on 16/11/16.
 */

public class Image implements MessageContent {
    private MessageType type = MessageType.IMAGE;
    private Bitmap content;
    private Uri filepath;

    Image(Uri file){
        filepath = file;
    }

    public void createImageFromFilepath(){

    }

    public void deleteBitmap (){
        content = null;
    }

    public MessageType getType(){
        return type;
    }

    public Uri getFilepath(){
        return filepath;
    }

    @Override
    public Object getMessageContent() {
        return content;
    }
}
