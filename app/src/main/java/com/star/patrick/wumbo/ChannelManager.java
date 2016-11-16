package com.star.patrick.wumbo;

/**
 * Created by giliam on 11/15/2016.
 */

public interface ChannelManager {
    void receive(Message msg);
    void addChannel(Channel channel);
    void removeChannel(Channel channel);
}
