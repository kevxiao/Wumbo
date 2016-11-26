package com.star.patrick.wumbo.model;

import com.star.patrick.wumbo.MessageReceiver;
import com.star.patrick.wumbo.model.message.EncryptedMessage;

import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.UUID;

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
