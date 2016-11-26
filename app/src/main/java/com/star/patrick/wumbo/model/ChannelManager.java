package com.star.patrick.wumbo.model;

import com.star.patrick.wumbo.MessageReceiver;

import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.UUID;

/**
 * Interface for ChannelManager has following responsibilities/interactions:
 * - Keeping track of channels
 * - Creating channels and inviting others
 * - Observable interface so that Observers may subscribe/unsubscribe and get notified
 */
public interface ChannelManager extends MessageReceiver {
    void addChannel(Channel channel);
    void removeChannel(Channel channel);
    void createChannel(Channel channel, User inviter, List<User> invitedUsers);
    Map<UUID,String> getChannels();
    Channel getChannel(UUID channelId);
    void addObserver(Observer obs);
    void deleteObserver(Observer obs);
}
