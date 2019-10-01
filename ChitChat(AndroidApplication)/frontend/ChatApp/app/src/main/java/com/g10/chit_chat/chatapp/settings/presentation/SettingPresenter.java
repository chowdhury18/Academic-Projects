package com.g10.chit_chat.chatapp.settings.presentation;

import android.net.Uri;
import android.util.Log;

import com.g10.chit_chat.chatapp.settings.view.SettingView;
import com.g10.chit_chat.chatapp.settings.model.SettingInteractor;

public class SettingPresenter implements SettingInteractor.OnSettingFinishedListener{
    SettingView settingView;
    SettingInteractor settingInteractor;

    public SettingPresenter(SettingView settingView, SettingInteractor settingInteractor){
        this.settingView = settingView;
        this.settingInteractor = settingInteractor;
        Log.d("MY_TAG", "settingpresenter settingView: " + settingView);
    }

    public void setImageResolutionToUser(int which){
        settingInteractor.setImageResolutionToUser(which, this);
    }

    public void readUserInfo(){
        settingInteractor.readUserInfo(this);
    }

    public void updateProfileImage(Uri imageUri, byte[] bitmapImageData) {
        settingInteractor.updateProfileImage(imageUri, bitmapImageData, this);
    }

    public void onDestroy() {
        settingView = null;
    }

    @Override
    public void onSuccessSetImageResolutionToUser() {
        settingView.onSuccessSetImageResolutionToUser();
    }

    @Override
    public void setUserEmailAddress(String emailAddress) {
        settingView.setUserEmailAddess(emailAddress);
    }

    @Override
    public void setUserName(String userName) {
        Log.d("MY_TAG","setUsername(presenter): " + userName);
        Log.d("MY_TAG", "settingView: " + settingView);
        settingView.setUserName(userName);
    }

    @Override
    public void setUserProfileImage(String imageUrl) {
        settingView.setUserProfileImage(imageUrl);
    }

}
