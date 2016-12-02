package com.star.patrick.wumbo.view;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.star.patrick.wumbo.R;
import com.star.patrick.wumbo.model.Channel;
import com.star.patrick.wumbo.model.ChannelImpl;
import com.star.patrick.wumbo.model.message.Message;
import com.star.patrick.wumbo.model.message.MessageContent;

import java.util.Observable;
import java.util.Observer;

public class NotificationView implements Observer {
    private final Context mainContext;

    public NotificationView(Context mainContext) {
        this.mainContext = mainContext;
    }

    @Override
    public void update(Observable observable, Object o) {
        try {
            Message msg = (Message) o;
            Channel channel = (ChannelImpl) observable;
            String channelName = channel.getName();

            // add the message content to notification
            NotificationCompat.Builder notifBuilder =
                    new NotificationCompat.Builder(mainContext)
                            .setSmallIcon(R.drawable.ic_wumbo)
                            .setContentTitle(channelName)
                            .setContentText(msg.getContent().getType() == MessageContent.MessageType.TEXT ? (String) msg.getContent().getMessageContent() : "Open to see image.");

            // create intent for launching app when clicked
            Intent resultIntent = new Intent(mainContext, MainActivity.class);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            // set the task stack for the back button
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mainContext);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(mainContext.getApplicationContext(), (int) System.currentTimeMillis(), resultIntent, 0);
            // delete notification if clicked
            notifBuilder.setAutoCancel(true);
            notifBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager =
                    (NotificationManager) mainContext.getSystemService(Context.NOTIFICATION_SERVICE);
            // add notification to android notification manager
            mNotificationManager.notify(0, notifBuilder.build());
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }
}
