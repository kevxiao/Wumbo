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

    /**
     * Gets existing from the database
     * @param messageCourier will use this to send messages (channel invites)
     */
    public ChannelManagerImpl(
            Context context,
            MessageCourier messageCourier,
            UUID meId,
            PrivateKey mePrivateKey
    ) {
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

    /**
     * Will create channels from channel invites it receives
     * Will forward the received messages to the channels they are sent to/from
     */
    @Override
    public void receive(EncryptedMessage msg) {
        Log.d("SE 464", "Channel manager is receiving");

        //Check if it is a channel invite, and it is meant for this user
        if (msg.getContentType().equals(MessageContent.MessageType.CHANNEL_INVITE)
                && msg.getUser().getId().equals(meId)) {
            createChannel(msg);
        } else if (channels.containsKey(msg.getChannelId())) {
            //Forward to a channel it has that it was meant for
            channels.get(msg.getChannelId()).receive(msg);
        }
    }

    /**
     * Create a channel from an encrypted channel invite message
     * @return the newly created channel
     */
    private Channel createChannel(EncryptedMessage emsg) {
        Log.d("SE464", "ChannelManager: createChannel: privateKey: "
                + Encryption.getEncodedPrivateKey(mePrivateKey));

        //decrypt message with private key
        Message msg = new Message(emsg, mePrivateKey);

        //get the channel invite info and create a new channel, add it to the known channels
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

    /**
     * Add channel to the channels known, names known, and the database
     */
    @Override
    public void addChannel(Channel channel) {
        channels.put(channel.getId(), channel);
        channelNames.put(channel.getId(), channel.getName());
        setChanged();
        notifyObservers();

        //Add to database
        DatabaseHandler db = new DatabaseHandler(context, messageCourier);
        db.addChannel(channel);
    }

    /**
     * Remove channel from known channels
     */
    @Override
    public void removeChannel(Channel channel) {
        channels.remove(channel.getId());
        channelNames.remove(channel.getId());

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

    /**
     * Create a new channel, and send out invites
     * @param channel The newly created channel
     * @param inviter Whoever created the channel and invited others
     * @param invitedUsers The list of users to invite to the newly created channel
     */
    @Override
    public void createChannel(Channel channel, User inviter, List<User> invitedUsers) {
        //Add to own list of channels
        this.addChannel(channel);

        //Send out invites to the list of invitedUsers via messageCourier
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
            Log.d("SE464", "ChannelManager: createInvite: userId,name = "
                    + user.getId() + "," + user );
            Log.d("SE464", "ChannelManager: createInvite: publicKey = "
                    + Encryption.getEncodedPublicKey(user.getPublicKey()) );
            messageCourier.send(new EncryptedMessage(msg, user.getPublicKey()));
        }
    }
}
