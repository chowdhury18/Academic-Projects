package com.g10.chit_chat.chatapp.main.model;

public class FirebaseInteractor {
    private FirebaseManager firebaseManager;

    public FirebaseInteractor() {
        firebaseManager = FirebaseManager.getInstance();
    }

    public void onDestroy() {
        firebaseManager.onDestroy();
    }
}
