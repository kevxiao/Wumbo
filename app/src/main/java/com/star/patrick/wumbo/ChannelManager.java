package com.star.patrick.wumbo;

import com.star.patrick.wumbo.message.Message;

public interface ChannelManager {
    void receive(Message msg);
    void send(Message msg);
    void addChannel(Channel channel);
    void removeChannel(Channel channel);
}
