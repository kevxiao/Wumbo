package com.star.patrick.wumbo.message;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.star.patrick.wumbo.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.UUID;

/**
 * Created by jesse on 16/11/16.
 */

public class Image implements MessageContent, Serializable {
    private MessageType type = MessageType.IMAGE;
    private byte[] content;
    private String filepath;
    private UUID id;

    Image(Uri uri, Context c, UUID id){
        this.id = id;

        setFilepath(c);

        try {
            copyFile(new File(getAbsolutePath(uri, c)), new File(filepath));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        Log.d("SE464","Copying image from "+sourceFile.getAbsolutePath()+" to "+destFile.getAbsolutePath());

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

    public void createImageFromFilepath(Context c){
        try{
            Log.d("SE464","Create bitmap from " + filepath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            MediaStore.Images.Media.getBitmap(c.getContentResolver(), Uri.fromFile(new File(filepath))).compress(Bitmap.CompressFormat.PNG, 100, stream);
            content = stream.toByteArray();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void deleteBitmap (){
        content = null;
    }

    public void saveBitmap(Context c) {
        if (content != null) {
            setFilepath(c);
            Log.d("SE464", "Saving bitmap to " + filepath);
            FileOutputStream out = null;
            try{
                Bitmap bitmapImg = BitmapFactory.decodeByteArray(content, 0, content.length);
                out = new FileOutputStream(filepath);
                bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, out);
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

    private String getAbsolutePath(Uri uri, Context c) {
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = c.getContentResolver().query(uri, projection, null, null, null);
        String path = uri.getPath();
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
            cursor.close();
        }
        return path;
    }
    private void setFilepath(Context c){
        filepath = id.toString() + ".jpg";

        ContextWrapper cw = new ContextWrapper(c);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, filepath);
        filepath = mypath.getAbsolutePath();
    }
}
