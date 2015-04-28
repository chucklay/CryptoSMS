package com.claymon.android.cryptosms;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;


public class WelcomeActivity extends ActionBarActivity {

    Button goButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        goButton = (Button) findViewById(R.id.goButton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gotoConvos = new Intent(getApplicationContext(), FragmentContainer.class);
                startActivity(gotoConvos);
            }
        });
    }
}
