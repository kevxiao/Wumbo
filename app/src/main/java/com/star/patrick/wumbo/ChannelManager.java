package com.star.patrick.wumbo;

import java.util.Map;
import java.util.UUID;

public interface ChannelManager {
    void receive(Message msg);
    void send(Message msg);
    void addChannel(Channel channel);
    void removeChannel(Channel channel);
    Map<UUID,String> getChannels();
    Channel getChannel(UUID channelId);
}
