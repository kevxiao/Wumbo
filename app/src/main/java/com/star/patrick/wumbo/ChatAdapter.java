package com.star.patrick.wumbo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.star.patrick.wumbo.message.Image;
import com.star.patrick.wumbo.message.Message;
import com.star.patrick.wumbo.message.MessageContent;

import java.util.List;
import java.util.UUID;

public class ChatAdapter extends BaseAdapter {

    private final List<Message> messages;
    private Activity context;
    private UUID meId;

    public ChatAdapter(Activity context, List<Message> messages, UUID meId) {
        this.context = context;
        this.messages = messages;
        this.meId = meId;
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
        if(msg.getSender().getId() == meId) {
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
        holder.sender.setText(msg.getSender().getDisplayName());
        holder.txtInfo.setText(msg.getReceiveTime().toString());

        switch(msg.getContent().getType()){
            case TEXT:
                holder.txtMessage.setVisibility(View.VISIBLE);
                holder.txtMessage.setText((String)msg.getContent().getMessageContent());
                holder.imgMessage.setImageResource(0);
                break;
            case IMAGE:
                ((Image)msg.getContent()).createImageFromFilepath(context);
                byte[] imgArray = (byte[])msg.getContent().getMessageContent();
                Bitmap original = BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length);
                if(original != null) {
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
        holder.sender = (TextView) v.findViewById(R.id.sender);
        return holder;
    }

    public void add(Message msg) {
        messages.add(msg);
    }

    public void addAll(List<Message> msgs) {
        messages.addAll(msgs);
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