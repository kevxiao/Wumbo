package com.star.patrick.wumbo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ChatAdapter extends BaseAdapter {

    private final List<Message> messages;
    private Activity context;

    public ChatAdapter(Activity context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
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
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = vi.inflate(R.layout.message_view, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //boolean myMsg = msg.getIsme();//Just a dummy check
        //to simulate whether it me or other sender
        holder.txtMessage.setText(msg.getText());
        holder.sender.setText(msg.getSender().getDisplayName());
        holder.txtInfo.setText(msg.getReceiveTime().toString());

        return convertView;
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
//        holder.content = (LinearLayout) v.findViewById(R.id.content);
//        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
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
        private TextView txtInfo;
        private TextView sender;
//        private LinearLayout content;
//        private LinearLayout contentWithBG;
    }
}