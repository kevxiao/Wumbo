package com.star.patrick.wumbo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.star.patrick.wumbo.wifidirect.HandshakeDispatcherService;
import com.star.patrick.wumbo.wifidirect.WiFiDirectBroadcastReceiver;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements Observer {

    private Channel mMsgChannel;
    private ChannelList mMsgChannelList;
    private NetworkManager mNetworkMgr;
    public static final String TAG = "SE464";
    private ChatAdapter chatAdapter;
    private ListView listView;

    private ImageButton sendBtn;
    private EditText editMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNetworkMgr = new NetworkManagerImpl();
        mMsgChannel = new ChannelImpl(getResources().getString(R.string.public_name), mNetworkMgr);
        mMsgChannelList = new ChannelListImpl();
        mMsgChannelList.put(UUID.fromString(getResources().getString(R.string.public_uuid)), mMsgChannel);

        sendBtn = (ImageButton) findViewById(R.id.sendBtn);
        editMsg = (EditText) findViewById(R.id.editMsg);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMsgChannel.send(editMsg.getText().toString());
                editMsg.setText("");
            }
        });

        List<Message> messages = MessageListImpl.getMockMessageList();

        chatAdapter = new ChatAdapter(MainActivity.this, messages);

        listView = (ListView) findViewById(R.id.myList);
        listView.setAdapter(chatAdapter);

        chatAdapter.notifyDataSetChanged();
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

    }
}
