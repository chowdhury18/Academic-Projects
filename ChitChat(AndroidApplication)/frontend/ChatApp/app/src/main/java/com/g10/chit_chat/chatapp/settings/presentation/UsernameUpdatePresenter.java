package com.g10.chit_chat.chatapp.settings.presentation;

import android.text.TextUtils;

import com.g10.chit_chat.chatapp.settings.view.UsernameUpdateView;
import com.g10.chit_chat.chatapp.settings.model.UsernameUpdateInteractor;

public class UsernameUpdatePresenter implements UsernameUpdateInteractor.OnUsernameUpdateFinishedListener{
    UsernameUpdateView usernameUpdateView;
    UsernameUpdateInteractor usernameUpdateInteractor;

    public UsernameUpdatePresenter(UsernameUpdateView usernameUpdateView, UsernameUpdateInteractor usernameUpdateInteractor){
        this.usernameUpdateView = usernameUpdateView;
        this.usernameUpdateInteractor = usernameUpdateInteractor;
    }

    public void readUserInfo(){
        usernameUpdateInteractor.readUserInfo(this);
    }

    public void updateUsername(String username){
        usernameUpdateInteractor.updateUsername(username, this);
    }

    public void validateUsername(String username){
        if (TextUtils.isEmpty(username)) {
            onErrorValidation("Username cannot be empty.");
        }
        if (username.contains(" ")) {
            onErrorValidation("Username cannot contain space.");
        } else {
            usernameUpdateInteractor.validateUsername(username,this);
        }
    }

    public void onDestroy() {
        usernameUpdateView = null;
    }

    @Override
    public void setUserName(String userName) {
        usernameUpdateView.setUsername(userName);
    }

    @Override
    public void navigateToSetting() {
        usernameUpdateView.navigateToSetting();
    }

    @Override
    public void onErrorValidation(String errorMsg) {
        usernameUpdateView.onErrorValidation(errorMsg);
    }

    @Override
    public void onSuccessValidating() {
        usernameUpdateView.onSuccessValidating();
    }
}
