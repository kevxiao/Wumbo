package com.star.patrick.wumbo.view;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.star.patrick.wumbo.DatabaseHandler;
import com.star.patrick.wumbo.Encryption;
import com.star.patrick.wumbo.MessageBroadcastReceiver;
import com.star.patrick.wumbo.MessageCourier;
import com.star.patrick.wumbo.MessageCourierImpl;
import com.star.patrick.wumbo.R;
import com.star.patrick.wumbo.model.Channel;
import com.star.patrick.wumbo.model.ChannelImpl;
import com.star.patrick.wumbo.model.ChannelManager;
import com.star.patrick.wumbo.model.ChannelManagerImpl;
import com.star.patrick.wumbo.model.ContactsTracker;
import com.star.patrick.wumbo.model.ContactsTrackerImpl;
import com.star.patrick.wumbo.model.User;
import com.star.patrick.wumbo.model.message.Message;
import com.star.patrick.wumbo.wifidirect.HandshakeDispatcherService;
import com.star.patrick.wumbo.wifidirect.MessageDispatcherService;
import com.star.patrick.wumbo.wifidirect.WifiDirectService;

import java.io.File;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import static com.star.patrick.wumbo.view.CreateChannelActivity.CONTACT_LIST;
import static com.star.patrick.wumbo.view.CreateChannelActivity.CHANNEL_NAME;
import static com.star.patrick.wumbo.view.CreateChannelActivity.INVITED_USERS;

public class MainActivity extends AppCompatActivity implements Observer {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public static final String TAG = "SE464";

    private ChannelManager channelManager;
    private ContactsTracker contacts;
    private Channel msgChannel;
    private Message lastMessage;
    private User me;
    private List<ChannelListItem> channels;
    private MessageCourier messageCourier;
    private MessageBroadcastReceiver messageBroadcastReceiver;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private View selectedChannelItem;
    private ChatAdapter chatAdapter;
    private ArrayAdapter<ChannelListItem> channelListAdapter;
    private ListView msgListView;
    private ListView channelListView;
    private EditText editMsg;
    private NotificationView notificationView;

    private Runnable onStartCallback;

    public static final int MAIN_ACTIVITY = 0;
    public static final int PICTURE_ACTIVITY = 1;
    public static final int SETTINGS_ACTIVITY = 2;
    public static final int CHANNEL_ACTIVITY = 3;
    public static final int GALLERY_ACTIVITY = 4;
    public static final int INVITE_ACTIVITY = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get the sender name found
        Bundle extras = getIntent().getExtras();
        String senderName = (extras != null && extras.getString("name") != null && !extras.getString("name").isEmpty()) ? extras.getString("name") : "Anonymous";

        buildView();

        buildModels(senderName);

        buildControllers();

        buildListAdapters();

        //Link the views and models
        channelManager.addObserver(this);
        msgChannel.addObserver(this);
        for (UUID channelId : channelManager.getChannels().keySet()) {
            channelManager.getChannel(channelId).addObserver(notificationView);
        }
        msgChannel.deleteObserver(notificationView);

        startBackgroundServices();

        if (onStartCallback != null) {
            onStartCallback.run();
        }
        update(null, null);

        final List<ChannelListItem> channelList = createChannelItemList(channelManager.getChannels());
        for (final ChannelListItem channelItem : channelList) {
            if (channelItem.getId().equals(msgChannel.getId())){
                channelListView.clearFocus();
                channelListView.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("SE464", "MainActivity: layout change: selecting: " + channelList.indexOf(channelItem) + ", num children: " + channelListView.getChildCount());
                        channelListView.requestFocusFromTouch();
                        channelListView.setSelection(channelList.indexOf(channelItem));
                        channelListView.requestFocus();
                        selectChannelItem(channelListView.getChildAt(channelListView.getSelectedItemPosition()));
                        Log.d("SE464", "MainActivity: layout change: selected: " + channelListView.getSelectedItemPosition());
                    }
                });
            }
        }
    }

    private void startBackgroundServices() {
        Intent intent = new Intent(this, WifiDirectService.class);
        startService(intent);

        intent = new Intent(this, HandshakeDispatcherService.class);
        startService(intent);

        intent = new Intent(this, MessageDispatcherService.class);
        startService(intent);
    }

    private void buildListAdapters() {
        chatAdapter = new ChatAdapter(MainActivity.this, new ArrayList<Message>(), me.getId());
        msgListView.setAdapter(chatAdapter);
        chatAdapter.notifyDataSetChanged();

        channelListAdapter = new ArrayAdapter<>(this, R.layout.channel_list_item, new ArrayList<ChannelListItem>());
        channelListView.setOnItemClickListener(new ChannelListItemClickListener());
        channelListView.setAdapter(channelListAdapter);
    }

    private void buildControllers() {
        final ImageButton sendBtn = (ImageButton) findViewById(R.id.sendBtn);
        ImageButton cameraBtn = (ImageButton) findViewById(R.id.cameraIcon);
        LinearLayout createChannelBtn = (LinearLayout) findViewById(R.id.add_channel_row);
        LinearLayout inviteBtn = (LinearLayout) findViewById(R.id.invite_row);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send a message with the text to the current channel
                if(!editMsg.getText().toString().isEmpty()) {
                    msgChannel.send(me, editMsg.getText().toString());
                    editMsg.setText("");
                }
            }
        });

        ViewOutlineProvider sendBtnOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int size = getResources().getDimensionPixelSize(R.dimen.send_button_size);
                outline.setOval(0, 0, size, size);
            }
        };

        sendBtn.setOutlineProvider(sendBtnOutlineProvider);
        sendBtn.setClipToOutline(true);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send the photo to the current channel
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    cameraButtonAction();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        ViewOutlineProvider cameraBtnOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int size = getResources().getDimensionPixelSize(R.dimen.send_button_size);
                outline.setOval(0, 0, size, size);
            }
        };

        cameraBtn.setOutlineProvider(cameraBtnOutlineProvider);
        cameraBtn.setClipToOutline(true);

        createChannelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start a new flow to create a channel. Give it the contact list
                Intent intent = new Intent(MainActivity.this, CreateChannelActivity.class);
                Bundle bundle = new Bundle();
                Map<UUID, User> myContacts = new HashMap<>(contacts.getContacts());
                myContacts.remove(me.getId());
                bundle.putSerializable(CONTACT_LIST, (Serializable) new ArrayList<>(myContacts.values()));
                intent.putExtras(bundle);
                startActivityForResult(intent, CHANNEL_ACTIVITY);
            }
        });

        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start a new flow to create a channel. Give it the contact list
                Intent intent = new Intent(MainActivity.this, InviteActivity.class);
                Bundle bundle = new Bundle();
                Map<UUID, User> myContacts = new HashMap<>(contacts.getContacts());
                myContacts.remove(me.getId());
                bundle.putSerializable(CONTACT_LIST, new ArrayList<>(myContacts.values()));
                bundle.putSerializable(CHANNEL_NAME, msgChannel.getName());
                intent.putExtras(bundle);
                startActivityForResult(intent, INVITE_ACTIVITY);
            }
        });
    }

    private void buildModels(String senderName) {
        messageCourier = new MessageCourierImpl(this);

        //Access to database for saved user data
        DatabaseHandler db = new DatabaseHandler(this, messageCourier);

        me = db.getMe();

        PrivateKey mePrivateKey;
        if (null == me) {
            //If it is the users first launch, they need to load keys for them self
            Log.d("SE464", "MainActivity: did not find me");
            KeyPair userKeys = Encryption.generateKeyPair();
            String encodedPrivateKey = Encryption.getEncodedPrivateKey(userKeys.getPrivate());
            me = new User(senderName, userKeys != null ? userKeys.getPublic() : null);

            //add myself to the database
            db.addUser(me);
            db.setMe(me.getId(), encodedPrivateKey);
            mePrivateKey = userKeys.getPrivate();
        } else {
            //Otherwise create 'me' from the info in the database
            Log.d("SE464", "MainActivity: me existed");
            me = new User(me.getId(), senderName, me.getPublicKey());

            //update the display name in the database
            db.updateUserDisplayName(me.getId(), senderName);
            mePrivateKey = Encryption.getPrivateKeyFromEncoding(db.getMePrivateKey());
        }

        //Create the core models
        channelManager = new ChannelManagerImpl(this, messageCourier, me.getId(), mePrivateKey);
        contacts = new ContactsTrackerImpl(this);
        msgChannel = new ChannelImpl(
                UUID.fromString(getResources().getString(R.string.public_uuid)),
                getResources().getString(R.string.public_name),
                this,
                messageCourier,
                Encryption.getSecretKeyFromEncoding(getResources().getString(R.string.public_secret_key))
        );
        channelManager.addChannel(msgChannel);

        //Create the message broadcast receiver, and add the observers
        messageBroadcastReceiver = new MessageBroadcastReceiver(this);
        messageBroadcastReceiver.add(channelManager);
        messageBroadcastReceiver.add(messageCourier);
        messageBroadcastReceiver.add(contacts);
    }

    private void buildView() {
        setContentView(R.layout.activity_main);

        //Build the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);

        //Build the drawer layout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        msgListView = (ListView) findViewById(R.id.myList);
        channelListView = (ListView) findViewById(R.id.channel_list);
        editMsg = (EditText) findViewById(R.id.editMsg);

        notificationView = new NotificationView(this);
    }

    private static List<ChannelListItem> createChannelItemList(Map<UUID,String> channels) {
        List<ChannelListItem> channelListItems = new ArrayList<>();
        for (Map.Entry<UUID,String> c : channels.entrySet()) {
            channelListItems.add(new ChannelListItem(c.getValue(), c.getKey()));
        }
        return channelListItems;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivityForResult(myIntent, SETTINGS_ACTIVITY);

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
                } else if (newMessages.size() == 1 && newMessages.get(i).getId().equals(lastMessage.getId())) {
                    newMessages = new ArrayList<>();
                } else if (newMessages.get(i).getId().equals(lastMessage.getId())) {
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

        //update the channel list, lazily, it just reloads the whole thing.
        channelListAdapter.clear();
        List<ChannelListItem> channelList = createChannelItemList(channelManager.getChannels());
        channelListAdapter.addAll(channelList);

        //update the channel title
        toolbar.setTitle(msgChannel.getName());
    }

    private Runnable onStopCallback;

    public void setOnStopCallback(Runnable runnable) {
        this.onStopCallback = runnable;
    }

    public void setOnStartCallback(Runnable runnable) {
        this.onStartCallback = runnable;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        switch(requestCode) {
            case PICTURE_ACTIVITY:
                if(resultCode == RESULT_OK)
                {
                    final boolean isCamera;
                    if(returnedIntent == null)
                    {
                        isCamera = true;
                    }
                    else
                    {
                        final String action = returnedIntent.getAction();
                        if(action == null)
                        {
                            isCamera = false;
                        }
                        else
                        {
                            isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        }
                    }

                    Uri selectedImageUri;
                    if(isCamera)
                    {
                        File file = new File(Environment.getExternalStorageDirectory().getPath(), "Wumbo-temp.jpg");
                        Uri outputFileUri = Uri.fromFile(file);
                        selectedImageUri = outputFileUri;
                    }
                    else
                    {
                        selectedImageUri = returnedIntent == null ? null : returnedIntent.getData();
                    }
                    if (selectedImageUri != null){
                        Log.d("SE464", "Image returned and stored at "+selectedImageUri.getPath());
                        msgChannel.send(me, selectedImageUri);
                    }

                }
                break;
            case SETTINGS_ACTIVITY:
                if (returnedIntent != null){
                    me.setDisplayName(returnedIntent.getStringExtra("new_name"));
                }
                break;
            case CHANNEL_ACTIVITY:
                if (null != returnedIntent) {
                    String channelName = returnedIntent.getStringExtra(CreateChannelActivity.CHANNEL_NAME);
                    List<User> invited = (ArrayList<User>)returnedIntent.getExtras().getSerializable(INVITED_USERS);
                    Channel channel = new ChannelImpl(channelName, this, messageCourier);
                    channelManager.createChannel(channel, me, invited);

                    channel.addObserver(notificationView);
                }
                break;
            case GALLERY_ACTIVITY:
                Log.d("SE464", "returned from gallery");
                ChatAdapter.lastMsg.delete();
                break;
            case INVITE_ACTIVITY:
                if (null != returnedIntent) {
                    List<User> invited = (ArrayList<User>)returnedIntent.getExtras().getSerializable(INVITED_USERS);
                    channelManager.inviteToChannel(msgChannel, me, invited);
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
            ChannelListItem channelListItem = (ChannelListItem)parent.getItemAtPosition(position);
            Log.d("SE464", "MainActivity: Switching to channel " + channelListItem.toString() + ", position: " + position);

            msgChannel.deleteObserver(MainActivity.this);
            msgChannel.addObserver(notificationView);

            msgChannel = channelManager.getChannel(channelListItem.getId());
            msgChannel.addObserver(MainActivity.this);
            msgChannel.deleteObserver(notificationView);

            chatAdapter = new ChatAdapter(MainActivity.this, new ArrayList<Message>(), me.getId());
            lastMessage = null;
            msgListView.setAdapter(chatAdapter);
            MainActivity.this.update(null, null);
            drawerLayout.closeDrawers();
            selectChannelItem(view);
            MainActivity.this.update(null, null);
        }
    }

    private void selectChannelItem(View view) {
        if(selectedChannelItem != null) {
            selectedChannelItem.setBackgroundColor(Color.parseColor("#00000000"));
        }
        selectedChannelItem = view;
        selectedChannelItem.setBackgroundColor(Color.parseColor("#20000000"));
    }

    private void cameraButtonAction() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "Wumbo-temp.jpg");
        Uri outputFileUri = Uri.fromFile(file);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        //Gallery
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        final Intent chooserIntent = Intent.createChooser(pickPhoto, "Select Source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        startActivityForResult(chooserIntent , PICTURE_ACTIVITY);//one can be replaced with any action code
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraButtonAction();
                } else {
                    Toast.makeText(this, R.string.read_ext_perm_denied, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        msgChannel.addObserver(notificationView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        msgChannel.deleteObserver(notificationView);
    }
}
