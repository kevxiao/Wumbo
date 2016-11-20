package com.star.patrick.wumbo.view;

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
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.star.patrick.wumbo.model.Channel;
import com.star.patrick.wumbo.model.ChannelImpl;
import com.star.patrick.wumbo.model.ChannelList;
import com.star.patrick.wumbo.model.ChannelManager;
import com.star.patrick.wumbo.model.ChannelManagerImpl;
import com.star.patrick.wumbo.model.ContactsTracker;
import com.star.patrick.wumbo.model.ContactsTrackerImpl;
import com.star.patrick.wumbo.DatabaseHandler;
import com.star.patrick.wumbo.MessageBroadcastReceiver;
import com.star.patrick.wumbo.MessageCourier;
import com.star.patrick.wumbo.MessageCourierImpl;
import com.star.patrick.wumbo.R;
import com.star.patrick.wumbo.model.User;
import com.star.patrick.wumbo.model.message.Message;
import com.star.patrick.wumbo.wifidirect.HandshakeDispatcherService;
import com.star.patrick.wumbo.wifidirect.MessageDispatcherService;
import com.star.patrick.wumbo.wifidirect.WifiDirectService;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity implements Observer {

    private ChannelManager channelManager;
    private ContactsTracker contacts;
    private Channel msgChannel;
    private ChannelList msgChannelList;
    public static final String TAG = "SE464";
    private ChatAdapter chatAdapter;
    private ListView listView;
    private Message lastMessage;
    private User me;
    private PrivateKey mePrivateKey;
    private List<ChannelListItem> channels;

    private ImageButton sendBtn;
    private ImageButton cameraBtn;
    private ImageButton createChannelBtn;
    private EditText editMsg;
    private DrawerLayout drawerLayout;
    private ListView channelListView;

    private Runnable onStartCallback;
    private ActionBar supportActionBar;
    private ActionBarDrawerToggle drawerToggle;
    private MessageCourier messageCourier;
    private MessageBroadcastReceiver messageBroadcastReceiver;

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

        String senderName = (extras != null && extras.getString("name") != null && !extras.getString("name").isEmpty()) ? extras.getString("name") : "Anonymous";

        messageCourier = new MessageCourierImpl(this);

        DatabaseHandler db = new DatabaseHandler(this, messageCourier);
        me = db.getMe();
        if (null == me) {
            Log.d("SE464", "MainActivity: did not find me");
            KeyPair userKeys = generateKeyPair();
            String encodedPrivateKey = getEncodedPrivateKey(userKeys.getPrivate());
            me = new User(senderName, userKeys != null ? userKeys.getPublic() : null);
            db.addUser(me);
            db.setMe(me.getId(), encodedPrivateKey);
        } else {
            Log.d("SE464", "MainActivity: me existed");
            me = new User(me.getId(), senderName, me.getPublicKey());
            db.updateSenderDisplayName(me.getId(), senderName);
            try {
                byte[] privkey = Base64.decode(db.getMePrivateKey(), Base64.DEFAULT);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privkey);
                KeyFactory fact = KeyFactory.getInstance("RSA");
                mePrivateKey = fact.generatePrivate(keySpec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }

        channelManager = new ChannelManagerImpl(this, messageCourier);
        contacts = new ContactsTrackerImpl(this);

        messageBroadcastReceiver = new MessageBroadcastReceiver(this);
        messageBroadcastReceiver.add(channelManager);
        messageBroadcastReceiver.add(messageCourier);
        messageBroadcastReceiver.add(contacts);

        byte[] encodedKey = Base64.decode(getResources().getString(R.string.public_secret_key), Base64.DEFAULT);
        msgChannel = new ChannelImpl(
                UUID.fromString(getResources().getString(R.string.public_uuid)),
                getResources().getString(R.string.public_name),
                this,
                messageCourier,
                new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES")
        );
        msgChannel.addObserver(this);
        channelManager.addChannel(msgChannel);

//        msgChannelList = new ChannelListImpl();
//        msgChannelList.put(UUID.fromString(getResources().getString(R.string.public_uuid)), msgChannel);

        sendBtn = (ImageButton) findViewById(R.id.sendBtn);
        cameraBtn = (ImageButton) findViewById(R.id.cameraIcon);
        editMsg = (EditText) findViewById(R.id.editMsg);
        createChannelBtn = (ImageButton) findViewById(R.id.createChannelIcon);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editMsg.getText().toString().isEmpty()) {
                    msgChannel.send(me, editMsg.getText().toString());
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

        createChannelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        if (onStartCallback != null) {
            onStartCallback.run();
        }

        chatAdapter = new ChatAdapter(MainActivity.this, new ArrayList<Message>(), me.getId());

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

        update(null, null);
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
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivityForResult(myIntent, 2);

        } else if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.d("SE464", "Updating messages");
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
        Log.d("SE464", "MainActivity: New messages has size: " + newMessages.size());
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

    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        switch(requestCode) {
//            case 0:
//                if(resultCode == RESULT_OK){
//                    Uri selectedImage = imageReturnedIntent.getData();
//                    //imageview.setImageURI(selectedImage);
//                }
//
//                break;
            case 1:
                if(returnedIntent != null){
                    Uri selectedImage = returnedIntent.getData();
                    Log.d("SE464", "Selected image: "+selectedImage.getPath());
                    msgChannel.send(me, selectedImage);
                    //imageview.setImageURI(selectedImage);
                }
                break;
            case 2:
                if (returnedIntent != null){
                    me.setDisplayName(returnedIntent.getStringExtra("new_name"));
                }
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

    private static KeyPair generateKeyPair() {
        KeyPair userKeys = null;
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            Log.d("SE464", "Start generating user key pair");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(Calendar.getInstance().getTimeInMillis());
            kpg.initialize(2048, sr);
            userKeys = kpg.generateKeyPair();
            if(userKeys != null) {
                Log.d("SE464", "Generated user key pair");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return userKeys;
    }

    private static String getEncodedPrivateKey(PrivateKey key) {
        String privkey = "";
        try {
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec spec = fact.getKeySpec(key, PKCS8EncodedKeySpec.class);
            byte[] packed = spec.getEncoded();
            privkey = Base64.encodeToString(packed, Base64.DEFAULT);
            Log.d("SE464", "Private key: " + privkey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return privkey;
    }

    public static String getEncodedPublicKey(PublicKey key) {
        String publkey = "";
        try {
            KeyFactory fact = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec spec = fact.getKeySpec(key, X509EncodedKeySpec.class);
            publkey = Base64.encodeToString(spec.getEncoded(), Base64.DEFAULT);
            Log.d("SE464", "Public key: " + publkey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return publkey;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}