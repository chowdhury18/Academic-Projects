package com.g10.chit_chat.chatapp.utils.firebase;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseUtil {
    private static FirebaseDatabase mDatabase;

    private FirebaseDatabaseUtil() {}

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            synchronized (FirebaseDatabaseUtil.class) {
                if (mDatabase == null) {
                    mDatabase = FirebaseDatabase.getInstance();
                    mDatabase.setPersistenceEnabled(true);
                }
            }
        }

        return mDatabase;
    }
}
