package com.star.patrick.wumbo;

import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.UUID;

import com.star.patrick.wumbo.message.EncryptedMessage;
import com.star.patrick.wumbo.message.Message;

public interface ChannelManager extends MessageReceiver {
    void receive(EncryptedMessage msg);
    void addChannel(Channel channel);
    void removeChannel(Channel channel);
    void createChannel(Channel channel, User inviter, List<User> invitedUsers);
    Map<UUID,String> getChannels();
    Channel getChannel(UUID channelId);
    void addObserver(Observer obs);
    void deleteObserver(Observer obs);
}
