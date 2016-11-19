package com.star.patrick.wumbo;

import java.util.Map;
import java.util.UUID;

import com.star.patrick.wumbo.message.EncryptedMessage;
import com.star.patrick.wumbo.message.Message;

public interface ChannelManager {
    void receive(EncryptedMessage msg);
    void addChannel(Channel channel);
    void removeChannel(Channel channel);
    Map<UUID,String> getChannels();
    Channel getChannel(UUID channelId);
}
