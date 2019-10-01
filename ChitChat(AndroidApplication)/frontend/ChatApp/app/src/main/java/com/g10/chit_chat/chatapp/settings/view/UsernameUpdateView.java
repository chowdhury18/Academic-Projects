package com.g10.chit_chat.chatapp.settings.view;

public interface UsernameUpdateView {
    void navigateToSetting();
    void onErrorValidation(String errorMsg);
    void setUsername(String username);
    void onSuccessValidating();
}
