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
    private String filepath;

    Image(Uri uri) {
        filepath = uri.toString();
    }

    @Override
    public MessageType getType(){
        return type;
    }

    @Override
    public Object getMessageContent() {
        return filepath;
    }
}
