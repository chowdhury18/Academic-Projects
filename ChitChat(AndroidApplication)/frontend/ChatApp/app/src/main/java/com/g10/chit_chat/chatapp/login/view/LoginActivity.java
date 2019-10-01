package com.g10.chit_chat.chatapp.login.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.g10.chit_chat.chatapp.BaseAppCompatActivity;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.g10.chit_chat.chatapp.main.view.MainActivity;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.login.model.LoginInteractor;
import com.g10.chit_chat.chatapp.login.presentation.LoginPresenter;
import com.g10.chit_chat.chatapp.register.view.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends BaseAppCompatActivity implements LoginView {

    MaterialEditText email, password;
    Button btnLogin,btnRegister;
    LoginPresenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageHelper.getPermissionDownloadImage(this);

        email = (MaterialEditText) findViewById(R.id.email);
        password = (MaterialEditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnRegister = (Button) findViewById(R.id.btn_register);

        loginPresenter = new LoginPresenter(this, new LoginInteractor());

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(email.getText().toString()) || TextUtils.isEmpty(password.getText().toString())){
                    Toast.makeText(LoginActivity.this, "Empty Fields", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(LoginActivity.this,
                            "Logging in...", Toast.LENGTH_SHORT).show();
                    loginPresenter.login(email.getText().toString(),
                            password.getText().toString());
                }

            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPresenter.register();
            }
        });
    }


    @Override
    public void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    @Override
    public void failedLogin() {
        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
//        loginPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onStart() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "firebase current user (null or not): ");

        if (firebaseUser != null) {
            Log.d(TAG, "Logged in, straight to the chat page");
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        super.onStart();
    }
}
