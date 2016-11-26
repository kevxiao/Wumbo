package com.star.patrick.wumbo.model;

import android.content.Context;
import android.util.Log;

import com.star.patrick.wumbo.DatabaseHandler;
import com.star.patrick.wumbo.Encryption;
import com.star.patrick.wumbo.MessageCourier;
import com.star.patrick.wumbo.model.message.ChannelInvite;
import com.star.patrick.wumbo.model.message.EncryptedMessage;
import com.star.patrick.wumbo.model.message.Message;
import com.star.patrick.wumbo.model.message.MessageContent;

import java.security.PrivateKey;
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
    private UUID meId;
    private PrivateKey mePrivateKey;

    public ChannelManagerImpl(Context context, MessageCourier messageCourier, UUID meId, PrivateKey mePrivateKey) {
        this.context = context;
        this.messageCourier = messageCourier;
        this.meId = meId;
        this.mePrivateKey = mePrivateKey;

        DatabaseHandler db = new DatabaseHandler(context, messageCourier);
        channels = db.getChannels();
        for (Channel channel : channels.values()) {
            channelNames.put(channel.getId(), channel.getName());
        }
    }

    @Override
    public void receive(EncryptedMessage msg) {
        Log.d("SE 464", "Channel manager is receiving");

        if (msg.getContentType().equals(MessageContent.MessageType.CHANNEL_INVITE)
                && msg.getUser().getId().equals(meId)) {
            createChannel(msg);
        } else if (channels.containsKey(msg.getChannelId())) {
            channels.get(msg.getChannelId()).receive(msg);
        }
    }

    private Channel createChannel(EncryptedMessage emsg) {
        Log.d("SE464", "ChannelManager: createChannel: privateKey: " + Encryption.getEncodedPrivateKey(mePrivateKey));
        Message msg = new Message(emsg, mePrivateKey);
        Log.d("SE464", "ChannelManager: createChannel: id " + msg.getId());
        Log.d("SE464", "ChannelManager: createChannel: user " + msg.getUser());
        Log.d("SE464", "ChannelManager: createChannel: content null?" + (null == msg.getContent() ? " yes" : " no"));

        ChannelInvite.Info channelInfo = (ChannelInvite.Info) msg.getContent().getMessageContent();

        Channel channel = new ChannelImpl(
                channelInfo.getId(),
                channelInfo.getName(),
                context,
                messageCourier,
                Encryption.getSecretKeyFromEncoding(channelInfo.getKey())
        );
        addChannel(channel);
        return channel;
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
                            inviter
                    ),
                    user,
                    new Timestamp(Calendar.getInstance().getTime().getTime()),
                    UUID.randomUUID()
            );
            Log.d("SE464", "ChannelManager: createInvite: userId,name = " + user.getId() + "," + user );
            Log.d("SE464", "ChannelManager: createInvite: publicKey = " + Encryption.getEncodedPublicKey(user.getPublicKey()) );
            messageCourier.send(new EncryptedMessage(msg, user.getPublicKey()));
        }
    }
}
