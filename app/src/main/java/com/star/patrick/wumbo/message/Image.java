package com.star.patrick.wumbo.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.UUID;

/**
 * Created by jesse on 16/11/16.
 */

public class Image implements MessageContent {
    private MessageType type = MessageType.IMAGE;
    private Bitmap content;
    private String filepath;
    private Context c;
    private UUID id;

    Image(Uri file, Context context, UUID id){
        this.id = id;
        c = context;
        filepath = id.toString() + ".png";

        try {
            copyFile(new File(file.getPath()), new File(filepath));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }

    }

    public void createImageFromFilepath(){
        try{
            content = MediaStore.Images.Media.getBitmap(c.getContentResolver(), Uri.parse(filepath));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void deleteBitmap (){
        content = null;
    }

    public void saveBitmap() {
        if (content != null) {
            FileOutputStream out = null;
            try{
                out = new FileOutputStream(filepath);
                content.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public String getFilepath(){
        return filepath;
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
