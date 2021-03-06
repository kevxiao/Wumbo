package com.star.patrick.wumbo.view;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.star.patrick.wumbo.R;
import com.star.patrick.wumbo.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CreateChannelActivity extends AppCompatActivity {
    private ImageButton continueButton;
    private ListView contactListView;
    private EditText channelNameEditText;
    private List<User> invitedUsers = new ArrayList<>();
    private ArrayAdapter<User> contactListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_channel_view);

        List<User> users = (List<User>) getIntent().getExtras().getSerializable(CONTACT_LIST);

        channelNameEditText = (EditText) findViewById(R.id.channel_name_text);
        contactListView = (ListView) findViewById(R.id.contact_list);
        continueButton = (ImageButton) findViewById(R.id.continue_creating_channel_button);

        contactListAdapter = new ArrayAdapter<>(this, R.layout.contact_list_item, users);
        contactListView.setAdapter(contactListAdapter);
        contactListView.setOnItemClickListener(new ContactListItemClickListener());

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnName = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable(INVITED_USERS, (Serializable)invitedUsers);
                returnName.putExtras(bundle);
                String channelName = channelNameEditText.getText().toString();
                if (channelName.equals("")) {
                    channelName = getResources().getString(R.string.default_channel_name);
                }
                returnName.putExtra(CHANNEL_NAME, channelName);
                setResult(RESULT_OK, returnName);
                finish();
            }
        });

        ViewOutlineProvider continueButtonOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int size = getResources().getDimensionPixelSize(R.dimen.circle_button_size);
                outline.setOval(0, 0, size, size);
            }
        };

        continueButton.setOutlineProvider(continueButtonOutlineProvider);
        continueButton.setClipToOutline(true);
    }

    public static final String CONTACT_LIST = "contact_list";
    public static final String CHANNEL_NAME = "channel_name";
    public static final String INVITED_USERS = "invited_users";

    private class ContactListItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d("SE464", "CreateChannel: Selected user at position: " + position);
            User user = (User) parent.getItemAtPosition(position);
            if(!invitedUsers.contains(user)) {
                view.setBackgroundColor(Color.parseColor("#20000000"));
                invitedUsers.add(user);
            } else {
                view.setBackgroundColor(Color.parseColor("#00000000"));
                invitedUsers.remove(user);
            }
        }
    }
}
