package com.star.patrick.wumbo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
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

import com.star.patrick.wumbo.message.Image;
import com.star.patrick.wumbo.message.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.star.patrick.wumbo.MainActivity.TAG;

public class ChatAdapter extends BaseAdapter {

    private final List<Message> messages;
    private Activity context;
    private UUID meId;

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
        Message msg = getItem(position);
        RelativeLayout.LayoutParams lparams;
        LinearLayout.LayoutParams sparams, tparams, cparams;
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = vi.inflate(R.layout.message_view, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

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

        switch(msg.getContent().getType()){
            case TEXT:
                holder.txtMessage.setVisibility(View.VISIBLE);
                holder.txtMessage.setText((String)msg.getContent().getMessageContent());
                holder.imgMessage.setImageResource(0);
                break;
            case IMAGE:
                Log.d(TAG, (String) msg.getContent().getMessageContent());
                Bitmap original = BitmapFactory.decodeFile((String) msg.getContent().getMessageContent());
                if (original != null) {
                    int size = (int) (original.getHeight() * (512.0 / original.getWidth()));
                    Bitmap scaled = Bitmap.createScaledBitmap(original, 512, size, true);
                    holder.imgMessage.setImageBitmap(scaled);
                }
                holder.txtMessage.setVisibility(View.GONE);
                break;
        }

        return convertView;
    }

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

    public void add(Message msg) {
        messages.add(msg);
    }

    public void addAll(List<Message> msgs) {
        Log.d("SE464", "ChatAdaptor: before Messages size is " + msgs.size());
        messages.addAll(msgs);
        Log.d("SE464", "ChatAdaptor: after Messages size is " + msgs.size());
    }

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