package com.g10.chit_chat.chatapp.login.presentation;

import com.g10.chit_chat.chatapp.login.model.LoginInteractor;
import com.g10.chit_chat.chatapp.login.view.LoginView;

public class LoginPresenter implements LoginInteractor.OnLoginFinishedListener {
    LoginView loginView;
    LoginInteractor loginInteractor;

    public LoginPresenter(LoginView loginView, LoginInteractor loginInteractor) {
        this.loginInteractor = loginInteractor;
        this.loginView = loginView;
    }

    public void login(String email, String password) {
        loginInteractor.login(email, password, this);
    }

    public void register(){
        loginView.navigateToRegister();
    }

    public void onDestroy() {
        loginView = null;
    }

    @Override
    public void onSuccess() {
        loginView.navigateToHome();
    }

    @Override
    public void onError() {
        loginView.failedLogin();
    }

    @Override
    public void onStart() {

    }
}
