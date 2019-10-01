package com.g10.chit_chat.chatapp.register.presentation;

import android.net.Uri;

import com.g10.chit_chat.chatapp.register.model.RegisterInteractor;
import com.g10.chit_chat.chatapp.register.view.RegisterView;

public class RegisterPresenter implements RegisterInteractor.OnRegisterFinishedListener {
    RegisterView registerView;
    RegisterInteractor registerInteractor;

    public RegisterPresenter(RegisterView registerView,
                             RegisterInteractor registerInteractor) {
        this.registerView = registerView;
        this.registerInteractor = registerInteractor;
    }

    public void register(String username, String email, String password, Uri imageUri, byte[] imageData) {
        registerInteractor.register(username, email, password, imageUri,imageData, this);
    }

    public void validateUsername(String username){
        registerInteractor.validateUsername(username, this);
    }

    public void uploadImage(){
        registerView.uploadImage();
    }

    public void onDestroy() {
        registerView = null;
    }

    @Override
    public void onSuccess() {
        registerView.navigateToHome();
    }

    @Override
    public void onError() {
        registerView.errorRegistration();
    }

    @Override
    public void onSuccessValidating() {
        registerView.onSuccessValidating();
    }

    @Override
    public void onUsernameError() {
        registerView.errorUsernamevalidation();
    }

}
