package com.g10.chit_chat.chatapp.settings.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.settings.model.UsernameUpdateInteractor;
import com.g10.chit_chat.chatapp.settings.presentation.UsernameUpdatePresenter;
import com.rengwuxian.materialedittext.MaterialEditText;

public class UsernameUpdateActivity extends AppCompatActivity implements UsernameUpdateView{


    MaterialEditText username;
    UsernameUpdatePresenter usernameUpdatePresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username_update);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_activity_username_upadte);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usernameUpdatePresenter = new UsernameUpdatePresenter(this, new UsernameUpdateInteractor());

        username = findViewById(R.id.username);

        //read user info
        usernameUpdatePresenter.readUserInfo();
    }

    @Override
    public void navigateToSetting() {
        Log.d("MY_TAG", " After updating the username going back to settings");
        finish();
    }

    @Override
    public void setUsername(String userName) {
        username.setText(userName);
    }

    @Override
    public void onErrorValidation(String errorMsg) {
        Toast.makeText(UsernameUpdateActivity.this,errorMsg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessValidating() {
        usernameUpdatePresenter.updateUsername(username.getText().toString().toLowerCase());
    }

    @Override
    protected void onDestroy() {
        usernameUpdatePresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_username, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.confirm:
                usernameUpdatePresenter.validateUsername(username.getText().toString().toLowerCase().trim());
                return true;
        }
        return false;
    }

}
