package com.star.patrick.wumbo;

import android.content.Context;
import android.util.Log;

import com.star.patrick.wumbo.message.ChannelInvite;
import com.star.patrick.wumbo.message.EncryptedMessage;
import com.star.patrick.wumbo.message.Message;
import com.star.patrick.wumbo.message.MessageContent;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.UUID;

public class ChannelManagerImpl extends Observable implements ChannelManager {
    private final MessageCourier messageCourier;
    private Context context;
    private Map<UUID,Channel> channels = new LinkedHashMap<>();
    private Map<UUID,String> channelNames = new LinkedHashMap<>();


    public ChannelManagerImpl(Context context, MessageCourier messageCourier) {
        this.context = context;
        this.messageCourier = messageCourier;

        DatabaseHandler db = new DatabaseHandler(context, messageCourier);
        channels = db.getChannels();
        for (Channel channel : channels.values()) {
            channelNames.put(channel.getId(), channel.getName());
        }
    }

    @Override
    public void receive(EncryptedMessage msg) {
        Log.d("SE 464", "Channel manager is receiving");

        if (msg.getContentType().equals(MessageContent.MessageType.CHANNEL_INVITE)) {
            createChannel(msg);
        } else if (channels.containsKey(msg.getChannelId())) {
            channels.get(msg.getChannelId()).receive(msg);
        }
    }

    private Channel createChannel(EncryptedMessage emsg) {
        Message msg = null;
        //KEVIN YOU FIGURE THIS SHIT OUT
        try {
            msg = new Message(emsg, null);
        } catch(Exception e) {
            return null;
        }

        ChannelInvite.Info channelInfo = (ChannelInvite.Info) msg.getContent();

//        Channel channel = new ChannelImpl(
//                channelInfo.getId(),
//                channelInfo.getName(),
//                context,
//                messageCourier,
//                channelInfo.getKey()
//        );
//        addChannel(channel);
        return null;
    }

    @Override
    public void addChannel(Channel channel) {
        channels.put(channel.getId(), channel);
        channelNames.put(channel.getId(), channel.getName());
        setChanged();
        notifyObservers();

        //Add to db
        DatabaseHandler db = new DatabaseHandler(context, messageCourier);
        db.addChannel(channel);
    }

    @Override
    public void removeChannel(Channel channel) {
        channels.remove(channel.getId());

        //Remove from  db
        DatabaseHandler db = new DatabaseHandler(context, messageCourier);
        db.removeChannel(channel.getId());
    }

    @Override
    public Map<UUID, String> getChannels() {
        return channelNames;
    }

    @Override
    public Channel getChannel(UUID channelId) {
        return channels.get(channelId);
    }

    @Override
    public void createChannel(Channel channel, User inviter, List<User> invitedUsers) {
        this.addChannel(channel);
        for (User user : invitedUsers) {
            Message msg = new Message(
                    new ChannelInvite(
                            channel.getId(),
                            channel.getName(),
                            channel.getKey(),
                            user
                    ),
                    inviter,
                    new Timestamp(Calendar.getInstance().getTime().getTime()),
                    UUID.randomUUID()
            );
            //KEVIN FIX THIS SHIT
            //messageCourier.send(new EncryptedMessage(msg, ???));
        }
    }
}
