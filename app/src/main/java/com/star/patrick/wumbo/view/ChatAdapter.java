package com.star.patrick.wumbo.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.star.patrick.wumbo.R;
import com.star.patrick.wumbo.model.ChannelImpl;
import com.star.patrick.wumbo.model.message.Message;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class ChatAdapter extends BaseAdapter {

    //A list of the current messages
    private final List<Message> messages;
    private Activity context;
    //A reference to the user's ID
    private UUID meId;
    //A reference to the msg that is sent to the gallery to delete
    public static File lastMsg = null;

    public ChatAdapter(Activity context, List<Message> messages, UUID meId) {
        this.context = context;
        this.messages = messages;
        this.meId = meId;
        Log.d("SE464", "ChatAdaptor: created Messages size is " + messages.size());
    }

    @Override
    public int getCount() {
        return null != messages ? messages.size() : 0;
    }

    @Override
    public Message getItem(int position) {
        return null != messages ? messages.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        //Get the item that is related to the view
        final Message msg = getItem(position);
        RelativeLayout.LayoutParams lparams;
        LinearLayout.LayoutParams sparams, tparams, cparams;
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Check for repeat messages
        if (convertView == null) {
            convertView = vi.inflate(R.layout.message_view, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Set alignment of the message depending on sender
        lparams = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
        sparams = (LinearLayout.LayoutParams) holder.sender.getLayoutParams();
        tparams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
        cparams = (LinearLayout.LayoutParams) holder.contentWithBg.getLayoutParams();
        if(msg.getUser().getId().equals(meId)) {
            lparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            sparams.gravity = Gravity.END;
            tparams.gravity = Gravity.END;
            cparams.gravity = Gravity.END;
        } else {
            lparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            sparams.gravity = Gravity.START;
            tparams.gravity = Gravity.START;
            cparams.gravity = Gravity.START;
        }
        holder.content.setLayoutParams(lparams);
        holder.sender.setLayoutParams(sparams);
        holder.txtInfo.setLayoutParams(tparams);
        holder.contentWithBg.setLayoutParams(cparams);
        holder.sender.setText(msg.getUser().getDisplayName());
        holder.txtInfo.setText(msg.getReceiveTime().toString());

        //Set message content depending on the type of message
        switch(msg.getContent().getType()){
            case TEXT:
                //Set the text to be a message content and make image holder to invisible
                holder.txtMessage.setVisibility(View.VISIBLE);
                holder.txtMessage.setText((String)msg.getContent().getMessageContent());
                holder.imgMessage.setImageResource(0);
                break;
            case IMAGE:
                Bitmap image = BitmapFactory.decodeFile((String) msg.getContent().getMessageContent());
                //Scale the image to an arbitrarily appropriate size
                if (image != null) {
                    if(image.getHeight() > image.getWidth() && image.getHeight() > 512) {
                        int width = (int) (image.getWidth() * (512.0 / image.getHeight()));
                        image = Bitmap.createScaledBitmap(image, width, 512, true);
                    } else if (image.getWidth() > 512) {
                        int height = (int) (image.getHeight() * (512.0 / image.getWidth()));
                        image = Bitmap.createScaledBitmap(image, 512, height, true);
                    }
                    holder.imgMessage.setImageBitmap(image);
                }
                //Hide text view
                holder.txtMessage.setVisibility(View.GONE);
                //Send image to gallery on image click
                holder.imgMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] parts = ((String) msg.getContent().getMessageContent()).split("/");
                        String fileName = parts[parts.length-1];
                        File dest = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES), fileName);
                        File src = new File((String)msg.getContent().getMessageContent());
                        try {
                            ChannelImpl.copyFile(src, dest);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);

                        Uri uri = Uri.fromFile(dest);
                        Log.d("SE464", "Sending "+uri.getPath()+" to Gallery");
                        intent.setDataAndType(uri, "image/*");
                        lastMsg = dest;
                        context.startActivityForResult(intent, MainActivity.GALLERY_ACTIVITY);
                    }
                });
                break;
        }

        return convertView;
    }

    //Create a holder from the view
    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.imgMessage = (ImageView) v.findViewById(R.id.imgMessage);
        holder.contentWithBg = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        holder.sender = (TextView) v.findViewById(R.id.user);
        return holder;
    }

    //Add to the list of the current messages
    public void add(Message msg) {
        messages.add(msg);
    }

    //Add multiple messages to the current messages
    public void addAll(List<Message> msgs) {
        Log.d("SE464", "ChatAdaptor: before Messages size is " + msgs.size());
        messages.addAll(msgs);
        Log.d("SE464", "ChatAdaptor: after Messages size is " + msgs.size());
    }

    //A holder for the parts of message_view layout
    private static class ViewHolder {
        private TextView txtMessage;
        private ImageView imgMessage;
        private TextView txtInfo;
        private TextView sender;
        private LinearLayout content;
        private LinearLayout contentWithBg;
        private UUID id;
    }
}