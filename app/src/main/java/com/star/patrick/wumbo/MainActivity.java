package com.star.patrick.wumbo;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.star.patrick.wumbo.wifidirect.HandshakeDispatcherService;
import com.star.patrick.wumbo.wifidirect.MessageDispatcherService;
import com.star.patrick.wumbo.wifidirect.WifiDirectService;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements Observer {

    private ChannelManager channelManager;
    private Channel msgChannel;
    private ChannelList msgChannelList;
    private NetworkManager networkManager;
    public static final String TAG = "SE464";
    private ChatAdapter chatAdapter;
    private ListView listView;
    private Message lastMessage;
    private Sender me;

    private ImageButton sendBtn;
    private ImageButton cameraBtn;
    private EditText editMsg;

    private Runnable onStartCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();

        me = new Sender(extras != null && extras.getString("name") != null && !extras.getString("name").isEmpty() ? extras.getString("name") : "Anonymous");

        channelManager = new ChannelManagerImpl(this);
        networkManager = new NetworkManagerImpl();
        msgChannel = new ChannelImpl(UUID.fromString(getResources().getString(R.string.public_uuid)), getResources().getString(R.string.public_name), networkManager, this, me, channelManager);
        channelManager.addChannel(msgChannel);
        msgChannelList = new ChannelListImpl();
        msgChannelList.put(UUID.fromString(getResources().getString(R.string.public_uuid)), msgChannel);

        sendBtn = (ImageButton) findViewById(R.id.sendBtn);
        cameraBtn = (ImageButton) findViewById(R.id.cameraIcon);
        editMsg = (EditText) findViewById(R.id.editMsg);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editMsg.getText().toString().isEmpty()) {
                    msgChannel.send(editMsg.getText().toString());
                    editMsg.setText("");
                }
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(takePicture, 0);//zero can be replaced with any action code

                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
            }
        });

        if (onStartCallback != null) {
            onStartCallback.run();
        }

        msgChannel.addObserver(this);

        List<Message> messages = new ArrayList<>();//MessageListImpl.getMockMessageList();

        chatAdapter = new ChatAdapter(MainActivity.this, messages, me.getId());

        listView = (ListView) findViewById(R.id.myList);
        listView.setAdapter(chatAdapter);

        chatAdapter.notifyDataSetChanged();

        Intent intent = new Intent(this, WifiDirectService.class);
        startService(intent);

        intent = new Intent(this, HandshakeDispatcherService.class);
        startService(intent);

        intent = new Intent(this, MessageDispatcherService.class);
        startService(intent);

//        Message msg = new Message("intent message", me, new Timestamp(Calendar.getInstance().getTimeInMillis()), UUID.randomUUID());
//        Intent messageIntent = new Intent(ChannelImpl.WUMBO_MESSAGE_INTENT_ACTION);
//        messageIntent.putExtra(ChannelImpl.WUMBO_MESSAGE_EXTRA, msg);
//        sendBroadcast(messageIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void update(Observable observable, Object o) {
        List<Message> newMessages;

        if (null == lastMessage) {
            newMessages = msgChannel.getAllMessages();
        } else {
            newMessages = msgChannel.getAllMessagesSince(lastMessage.getReceiveTime());

            for ( int i = 0; i < newMessages.size(); i++ ) {
                if (newMessages.get(i).getReceiveTime() != lastMessage.getReceiveTime()) {
                    break;
                }
                else if (newMessages.get(i).getId() == lastMessage.getId()) {
                    if (i < newMessages.size() - 1) {
                        newMessages = newMessages.subList(i + 1, newMessages.size());
                        break;
                    }
                }
            }
        }
        if (null != newMessages && newMessages.size() >= 1) {
            lastMessage = newMessages.get(newMessages.size()-1);

            chatAdapter.addAll(newMessages);
            chatAdapter.notifyDataSetChanged();
        }
    }

    private Runnable onStopCallback;
    public void setOnStopCallback(Runnable runnable) {
        this.onStopCallback = runnable;
    }

    public void setOnStartCallback(Runnable runnable) {
        this.onStartCallback = runnable;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
//            case 0:
//                if(resultCode == RESULT_OK){
//                    Uri selectedImage = imageReturnedIntent.getData();
//                    //imageview.setImageURI(selectedImage);
//                }
//
//                break;
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    //imageview.setImageURI(selectedImage);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (onStopCallback != null) {
            onStopCallback.run();
        }
    }
}
