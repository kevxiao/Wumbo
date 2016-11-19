package com.star.patrick.wumbo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.star.patrick.wumbo.message.EncryptedMessage;
import com.star.patrick.wumbo.wifidirect.WifiDirectService;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.UUID;

public class ChannelManagerImpl extends Observable implements ChannelManager {
    private MainActivity mainContext;
    private Map<UUID,Channel> channels = new LinkedHashMap<>();
    private Map<UUID,String> channelNames = new LinkedHashMap<>();

    //Why the fuck is this shit here????
    private DatabaseHandler db;

    public ChannelManagerImpl(MainActivity context, MessageCourier messageCourier) {
        this.mainContext = context;

        db = new DatabaseHandler(context, mainContext, messageCourier);
    }

    @Override
    public void receive(EncryptedMessage msg) {
        Log.d("SE 464", "Channel manager is receiving");
        if (channels.containsKey(msg.getChannelId())) {
            channels.get(msg.getChannelId()).receive(msg);
        }
    }

    @Override
    public void addChannel(Channel channel) {
        channels.put(channel.getId(), channel);
        channelNames.put(channel.getId(), channel.getName());
        setChanged();
        notifyObservers();
    }

    @Override
    public void removeChannel(Channel channel) {
        channels.remove(channel.getId());
    }

    @Override
    public Map<UUID, String> getChannels() {
        return channelNames;
    }

    @Override
    public Channel getChannel(UUID channelId) {
        return channels.get(channelId);
    }

}
