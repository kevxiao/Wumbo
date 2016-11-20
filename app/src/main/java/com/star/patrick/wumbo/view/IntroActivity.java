package com.star.patrick.wumbo.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;

import com.star.patrick.wumbo.R;


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

        ImageButton continueBtn = (ImageButton) findViewById(R.id.continue_button);
        continueBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startMainIntent = new Intent(view.getContext(), MainActivity.class);
                startMainIntent.putExtra("name", mNameTxt.getText().toString());
                startActivity(startMainIntent);
                finish();
            }
        });
    }
}

