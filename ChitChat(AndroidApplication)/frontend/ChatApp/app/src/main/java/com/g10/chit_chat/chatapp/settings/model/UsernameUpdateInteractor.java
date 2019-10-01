package com.g10.chit_chat.chatapp.settings.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsernameUpdateInteractor {
    public interface OnUsernameUpdateFinishedListener {
        void setUserName(String userName);
        void navigateToSetting();
        void onErrorValidation(String errorMsg);
        void onSuccessValidating();
    }

    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference reference = FirebaseDatabaseUtil.getDatabase().getReference("Users");
    String updatedUsername;
    Boolean sameUsername = false, flag = false;
    ValueEventListener valueEventListener;
    ValueEventListener updateUsernameListener;
    ValueEventListener validateUsernameListener;
    ValueEventListener updateThreadListener;

    public void readUserInfo(final OnUsernameUpdateFinishedListener listener){
        if (valueEventListener == null) {
            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);
                        assert user != null;
                        assert firebaseUser != null;
                        if(user.getId().equals(firebaseUser.getUid())){
                            Log.d("MY_TAG", "ReadUserInfo(Update) - ID: " + user.getId() + " name: " + user.getUsername());
                            reference.removeEventListener(valueEventListener);
                            listener.setUserName(user.getUsername());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };

            reference.addListenerForSingleValueEvent(valueEventListener);
        }

    }

    public void updateUsername(final String username, final OnUsernameUpdateFinishedListener listener){
        updatedUsername = username;
        if (ApplicationData.getCurrentUser().getUsername().equals(username)) {
            listener.navigateToSetting();
            return;
        }
        final Map<String, Object> updatedMap = new HashMap<>();
        updatedMap.put("username", username);
        reference.child(ApplicationData.getCurrentUser().getId()).updateChildren(updatedMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                final DatabaseReference threads = reference.child(ApplicationData.getCurrentUser().getId()).child("threads");
                if (updateThreadListener == null) {
                    updateThreadListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String, Object> threadMap  = (Map) dataSnapshot.getValue();
                            if (threadMap != null) {
                                for (String threadId : threadMap.keySet()) {
                                    FirebaseDatabaseUtil.getDatabase().getReference("Details")
                                            .child(threadId)
                                            .child("details")
                                            .child("users")
                                            .child(ApplicationData.getCurrentUser().getId())
                                            .updateChildren(updatedMap);
                                }
                            }
                            threads.removeEventListener(updateThreadListener);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };
                }
                threads.addValueEventListener(updateThreadListener);
                listener.navigateToSetting();
            }
        });
    }

    public void validateUsername(final String username, final OnUsernameUpdateFinishedListener listener){
        if (ApplicationData.getCurrentUser().getUsername().equals(username)) {
            listener.onSuccessValidating();
            return;
        }
        reference = FirebaseDatabaseUtil.getDatabase().getReference().child("Users");
        sameUsername = false;
        Log.d("MY_TAG","validateUserListener: " + validateUsernameListener);
        if (validateUsernameListener == null) {
            validateUsernameListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        sameUsername = collectUsername((Map<String,Object>) dataSnapshot.getValue(), username);
                        Log.d("MY_TAG", "sameUsername: " + sameUsername);
                        reference.removeEventListener(validateUsernameListener);
                        if (sameUsername) {
                            listener.onErrorValidation("Same username exists");
                        } else {
                            listener.onSuccessValidating();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };

            reference.addValueEventListener(validateUsernameListener);
        } else if(validateUsernameListener != null){
            listener.onSuccessValidating();
        }
    }

    private boolean collectUsername(Map<String,Object> users, String username) {
        ArrayList<String> userNames = new ArrayList<>();
        flag = false;
        if(!users.isEmpty()){
            for (Map.Entry<String, Object> entry : users.entrySet()){
                //Get user map
                Map singleUser = (Map) entry.getValue();
                //Get phone field and append to list
                userNames.add((String) singleUser.get("username"));
            }
            for(int i = 0; i < userNames.size(); i++){
                if(username.equals(userNames.get(i))){
                    flag = true;
                    break;
                }
            }
        }
        Log.d("MY_TAG", "flag: " + flag);
        return flag;
    }
}
