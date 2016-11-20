package com.star.patrick.wumbo.message;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by giliam on 11/19/2016.
 */

public class ChannelInvite implements MessageContent, Serializable {
    private MessageType type = MessageType.CHANNEL_INVITE;
    private Info content;

    public ChannelInvite(UUID id, String name, String key) {
        content = new Info(id, name, key);
    }

    @Override
    public Object getMessageContent() {
        return content;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    public static class Info implements Serializable {
        private final UUID id;
        private final String name;
        private final String key;

        public Info(UUID id, String name, String key) {
            this.id = id;
            this.name = name;
            this.key = key;
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getKey() {
            return key;
        }
    }
}
