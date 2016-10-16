package com.star.patrick.wumbo;

/**
 * Created by Kevin Xiao on 2016-10-15.
 */

public interface NetworkManager {
    void send(Message msg);
    void onReceive(Message msg);
}
