package com.star.patrick.wumbo.model.message;

import com.star.patrick.wumbo.model.User;

import java.io.Serializable;
import java.util.UUID;

/**
 * Message content object for a channel invite message
 */
public class ChannelInvite implements MessageContent, Serializable {
    private MessageType type = MessageType.CHANNEL_INVITE;
    private Info content;

    /**
     * Constructor for channel invite message using channel info
     * @param channelId Channel ID for the invite
     * @param channelName Channel name for the invite
     * @param channelKey Channel secret key for the invite
     * @param inviter The user that is sending the invite
     */
    public ChannelInvite(UUID channelId, String channelName, String channelKey, User inviter) {
        content = new Info(channelId, channelName, channelKey, inviter);     // create channel info object
    }

    @Override
    public Object getMessageContent() {
        return content;
    }

    @Override
    public MessageType getType() {
        return type;
    }

    /**
     * Class for the channel info for the message content
     */
    public static class Info implements Serializable {
        private final UUID channelId;
        private final String channelName;
        private final String channelKey;
        private final User inviter;

        /**
         * Constructor for channel info object using provided info
         * @param channelId Channel ID for the invite
         * @param channelName Channel name for the invite
         * @param channelKey Channel secret key for the invite
         * @param inviter The user that is sending the invite
         */
        public Info(UUID channelId, String channelName, String channelKey, User inviter) {
            this.channelId = channelId;
            this.channelName = channelName;
            this.channelKey = channelKey;
            this.inviter = inviter;
        }

        public UUID getId() {
            return channelId;
        }

        public String getName() {
            return channelName;
        }

        public String getKey() {
            return channelKey;
        }

        public User getInviter() {
            return inviter;
        }
    }
}
