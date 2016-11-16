package com.star.patrick.wumbo.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by jesse on 16/11/16.
 */

public class Image implements MessageContent {
    private MessageType type = MessageType.IMAGE;
    private Bitmap content;
    private Uri filepath;
    private Context c;

    Image(Uri file, Context context){
        filepath = file;
        c = context;
    }

    public void createImageFromFilepath(){
        try{
            content = MediaStore.Images.Media.getBitmap(c.getContentResolver(), filepath);
        } catch (Exception e){
            e.printStackTrace();
        }
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
