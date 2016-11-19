package com.star.patrick.wumbo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.star.patrick.wumbo.message.Message;
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
    private Set<UUID> receivedMessageIds = new HashSet<>();

    //Why the fuck is this shit here????
    private DatabaseHandler db;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SE464", "Channel received a message");
            String action = intent.getAction();
            if (WUMBO_MESSAGE_INTENT_ACTION.equals(action)) {
                Message msg = (Message) intent.getSerializableExtra(WUMBO_MESSAGE_EXTRA);
                receive(msg);
            }
        }
    };

    public ChannelManagerImpl(MainActivity context, Sender me) {
        this.mainContext = context;

        final IntentFilter filter = new IntentFilter();
        filter.addAction(WUMBO_MESSAGE_INTENT_ACTION);

        mainContext.setOnStartCallback(new Runnable() {
            @Override
            public void run() {
                mainContext.registerReceiver(receiver, filter);
            }
        });
        mainContext.setOnStopCallback(new Runnable() {
            @Override
            public void run() {
                try {
                    mainContext.unregisterReceiver(receiver);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });

        db = new DatabaseHandler(context, me, mainContext, this);
    }

    @Override
    public void receive(Message msg) {
        Log.d("SE464", "ChannelManager receive");
        if (!receivedMessageIds.contains(msg.getId())) {
            Log.d("SE464", "ChannelManager first time receiving");
            send(msg);
            if (channels.containsKey(msg.getChannelId())) {
                channels.get(msg.getChannelId()).receive(msg);
            }
        }
    }

    @Override
    public void send(Message msg) {
        Log.d("SE464", "ChannelManager send");
        receivedMessageIds.add(msg.getId());

        //add to db if it is a channel the user cares about, for some reason?
        if (channels.containsKey(msg.getChannelId())) {
            Log.d("SE464", "ChannelMngr: i should log a message now");
            db.addMessage(msg);
        } else {
            Log.d("SE464", "ChannelMngr: why don't i contain this channel?");
        }

        Intent sendMsgIntent = new Intent(mainContext, WifiDirectService.class);
        sendMsgIntent.setAction(WifiDirectService.SEND_MESSAGE_ACTION);
        sendMsgIntent.putExtra(WifiDirectService.EXTRA_MESSAGE, msg);
        mainContext.startService(sendMsgIntent);
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

    public static final String WUMBO_MESSAGE_INTENT_ACTION = "com.star.patrick.wumbo.MESSAGE";
    public static final String WUMBO_MESSAGE_EXTRA = "message";
}
