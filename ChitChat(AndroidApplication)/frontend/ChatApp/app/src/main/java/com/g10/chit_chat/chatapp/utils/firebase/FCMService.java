package com.g10.chit_chat.chatapp.utils.firebase;

import android.support.annotation.NonNull;
import android.util.Log;

import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;

public class FCMService extends FirebaseMessagingService {
    private static final String TAG = "CHIT_CHAT";

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        User.instanceId = token;
        updateToken();
    }

    public static void updateToken() {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        final String token = task.getResult().getToken();

                        Log.d(TAG, "Updating token to: " + token);
                        if (ApplicationData.getCurrentUser() != null) {
                            Log.d(TAG, "ApplicationData.getCurrentUser().getId(): " + ApplicationData.getCurrentUser().getId());
                            FirebaseDatabaseUtil.getDatabase()
                                    .getReference("Users")
                                    .child(ApplicationData.getCurrentUser().getId())
                                    .child("instanceId")
                                    .setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "update successfull");
                                        User.instanceId = token;
                                    } else {
                                        Log.e(TAG, "UPDATE failed");
                                    }
                                }
                            });
                        }
                    }
                });


    }


}
