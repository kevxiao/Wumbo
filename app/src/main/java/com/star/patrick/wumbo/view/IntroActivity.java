package com.star.patrick.wumbo.view;

import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;

import com.star.patrick.wumbo.DatabaseHandler;
import com.star.patrick.wumbo.R;
import com.star.patrick.wumbo.model.User;


/**
 * A login screen that offers login via email/password.
 */
public class IntroActivity extends AppCompatActivity {

    // UI references.
    private EditText mNameTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_intro);
        mNameTxt = (EditText) findViewById(R.id.name_text);

        DatabaseHandler db = new DatabaseHandler(this, null);
        User user = db.getMe();
        if (null != user) {
            mNameTxt.setText(user.getDisplayName());
            mNameTxt.setSelection(mNameTxt.getText().length());
        }

        ImageButton continueBtn = (ImageButton) findViewById(R.id.continue_button);
        continueBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startMainIntent = new Intent(view.getContext(), MainActivity.class);
                String name = mNameTxt.getText().toString();
                if (name.equals("")) {
                    name = getResources().getString(R.string.default_name);
                }
                startMainIntent.putExtra("name", name);
                startActivity(startMainIntent);
                finish();
            }
        });

        ViewOutlineProvider continueBtnOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int size = getResources().getDimensionPixelSize(R.dimen.circle_button_size);
                outline.setOval(0, 0, size, size);
            }
        };

        continueBtn.setOutlineProvider(continueBtnOutlineProvider);
        continueBtn.setClipToOutline(true);
    }
}

