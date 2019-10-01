package com.g10.chit_chat.chatapp.main.model;

import android.support.annotation.NonNull;
import android.util.Log;

import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class FirebaseManager {
    public volatile static FirebaseManager firebaseManager;
    private DatabaseReference dbUserReference;
    private static final String TAG = "MCC_PROJECT";

    FirebaseUser firebaseUser;

    private FirebaseManager() {
        //this.userId = userId;
        initFirebase();
    }

    public static FirebaseManager getInstance() {
        if (firebaseManager == null) {
            synchronized (FirebaseManager.class) {
                firebaseManager = new FirebaseManager();
            }
        }
        return firebaseManager;
    }

    private void initFirebase() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        dbUserReference = FirebaseDatabaseUtil.getDatabase().
                getReference("Users").child(firebaseUser.getUid());
        Log.d(TAG, "firebaseUser.getUid(): " + firebaseUser.getUid());

        dbUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Log.d(TAG, "current user id: " + user.getId());
                //username.setText(userDataModel.getUsername());
                if(user.getImageURL().equals("default")){
                    //profileImage.setImageResource(R.mipmap.ic_launcher);
                } else {
                    //Glide.with(MainActivity.this).load(userDataModel.getImageURL()).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabaseUtil.getDatabase().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        reference.child("Chats").push().setValue(hashMap);
    }

    private void readMessages(final String myid, final String userid, final String imageurl) {

    }

    public void onDestroy() {
        firebaseManager = null;
    }
}
