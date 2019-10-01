package com.g10.chit_chat.chatapp.chat.presentation;

import com.g10.chit_chat.chatapp.main.model.FirebaseInteractor;
import com.g10.chit_chat.chatapp.main.view.MainView;

public class ChatPresenter {
    private MainView mainView;
    private FirebaseInteractor firebaseInteractor;

    public ChatPresenter(MainView view, FirebaseInteractor firebaseInteractor) {
        mainView = view;
        this.firebaseInteractor = firebaseInteractor;
    }

    public void onDestroy() {
        mainView = null;
        firebaseInteractor = null;
    }
}
