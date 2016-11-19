package com.star.patrick.wumbo;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.star.patrick.wumbo.message.Message;
import com.star.patrick.wumbo.wifidirect.HandshakeDispatcherService;
import com.star.patrick.wumbo.wifidirect.MessageDispatcherService;
import com.star.patrick.wumbo.wifidirect.WifiDirectService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private List<ChannelListItem> channels;

    private ImageButton sendBtn;
    private ImageButton cameraBtn;
    private EditText editMsg;
    private DrawerLayout drawerLayout;
    private ListView channelListView;

    private Runnable onStartCallback;
    private ActionBar supportActionBar;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);

        setSupportActionBar(toolbar);
        supportActionBar = getSupportActionBar();

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        Bundle extras = getIntent().getExtras();

        me = new Sender(extras != null && extras.getString("name") != null && !extras.getString("name").isEmpty() ? extras.getString("name") : "Anonymous");

        channelManager = new ChannelManagerImpl(this, me);
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

        DatabaseHandler db = new DatabaseHandler(this, me, this, channelManager);
        List<Message> messages = db.getAllMessages();//new ArrayList<>();//MessageListImpl.getMockMessageList();x`x`x`

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

        channelListView = (ListView) findViewById(R.id.channel_list);

        channels = new ArrayList<>();
        for (Map.Entry<UUID,String> c : channelManager.getChannels().entrySet()) {
            channels.add(new ChannelListItem(c.getValue(), c.getKey()));
        }
        channelListView.setAdapter(new ArrayAdapter<>(this, R.layout.channel_list_item, channels));
        channelListView.setOnItemClickListener(new ChannelListItemClickListener());

        //DatabaseHandler db = new DatabaseHandler(this.getApplicationContext(), me, this, channelManager);

//        Message dbTestMsg = new Message("FUCK ME", me, new Timestamp(10), UUID.fromString(getResources().getString(R.string.public_uuid)));
//        dbTestMsg.setReceiveTime(new Timestamp(10));
//        db.addMessage(dbTestMsg);
//        Message dbRetreivedMsg = db.getMessage(dbTestMsg.getId());
//        if (null == dbRetreivedMsg) {
//            Log.d("SE464", "retrieved is null");
//        } else {
//            Log.d("SE464", "id: "+dbRetreivedMsg.getId() + " text: "+dbRetreivedMsg.getContent().getMessageContent()+" receiveTime: "+dbRetreivedMsg.getReceiveTime());
//        }
//        if (dbRetreivedMsg.getReceiveTime().equals(dbTestMsg.getReceiveTime())) {
//            Log.d("SE464", "This message seemed to be retreived right");
//        } else {
//            Log.d("SE464", "Uh oh, retreived times are different");
//        }
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
        } else if (drawerToggle.onOptionsItemSelected(item)) {
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
                if(imageReturnedIntent != null){
                    Uri selectedImage = imageReturnedIntent.getData();
                    Log.d("SE464", "Selected image: "+selectedImage.getPath());
                    msgChannel.send(selectedImage, this);
                    //imageview.setImageURI(selectedImage);
                }
                break;
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (onStopCallback != null) {
            onStopCallback.run();
        }
    }

    private static class ChannelListItem {
        String name;
        UUID id;

        public ChannelListItem(String name, UUID id) {
            this.name = name;
            this.id = id;
        }

        public String toString() {
            return name;
        }

        public UUID getId() {
            return id;
        }

    }

    private class ChannelListItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //supportActionBar.setTitle(?);
        }
    }
}
