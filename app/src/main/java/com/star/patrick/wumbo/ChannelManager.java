package com.star.patrick.wumbo;

public interface ChannelManager {
    void receive(Message msg);
    void addChannel(Channel channel);
    void removeChannel(Channel channel);
}
