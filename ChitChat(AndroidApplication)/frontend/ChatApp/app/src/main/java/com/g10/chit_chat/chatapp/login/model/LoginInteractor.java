package com.g10.chit_chat.chatapp.login.model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class LoginInteractor {
    public interface OnLoginFinishedListener {
        void onSuccess();
        void onError();
        void onStart();
    }

    private static final String TAG = "CHIT_CHAT";

    public void login(String email, String password,
                      final OnLoginFinishedListener listener) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            listener.onSuccess();
                        } else {
                            listener.onError();
                        }
                    }
                });

    }
}
