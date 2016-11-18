package com.star.patrick.wumbo.message;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.star.patrick.wumbo.MainActivity;

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
    private Bitmap content;
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
            content = MediaStore.Images.Media.getBitmap(c.getContentResolver(), Uri.fromFile(new File(filepath)));
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

    private String getAbsolutePath(Uri uri, Context c) {
        if (Build.VERSION.SDK_INT >= 19) {
            String id = "";
            if (uri.getLastPathSegment().split(":").length > 1)
                id = uri.getLastPathSegment().split(":")[1];
            else if (uri.getLastPathSegment().split(":").length > 0)
                id = uri.getLastPathSegment().split(":")[0];
            if (id.length() > 0) {
                final String[] imageColumns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION};
                Uri tempUri = uri;
                Cursor imageCursor = c.getContentResolver().query(tempUri, imageColumns, MediaStore.Images.Media._ID + "=" + id, null, null);
                if (imageCursor.moveToFirst()) {
                    return imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.ORIENTATION};
            Cursor cursor = c.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } else
                return null;
        }

    }
    private void setFilepath(Context c){
        filepath = id.toString() + ".jpg";

        ContextWrapper cw = new ContextWrapper(c);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,filepath);
        filepath = mypath.getAbsolutePath();
    }
}
