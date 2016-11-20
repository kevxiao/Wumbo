package com.star.patrick.wumbo.model.message;

import com.star.patrick.wumbo.model.User;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by giliam on 11/19/2016.
 */

public class ChannelInvite implements MessageContent, Serializable {
    private MessageType type = MessageType.CHANNEL_INVITE;
    private Info content;

    public ChannelInvite(UUID id, String name, String key, User invitee) {
        content = new Info(id, name, key, invitee);
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
        private final User invitee;

        public Info(UUID id, String name, String key, User invitee) {
            this.id = id;
            this.name = name;
            this.key = key;
            this.invitee = invitee;
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

        public User getInvitee() {
            return invitee;
        }
    }
}
