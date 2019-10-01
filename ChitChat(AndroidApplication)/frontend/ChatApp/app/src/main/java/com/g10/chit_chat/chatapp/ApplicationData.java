package com.g10.chit_chat.chatapp;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.g10.chit_chat.chatapp.datamodel.Thread;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.datamodel.UserChatInfo;
import com.g10.chit_chat.chatapp.main.view.MainActivity;
import com.g10.chit_chat.chatapp.utils.ChitChatConstants;
import com.g10.chit_chat.chatapp.utils.StringUtils;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class ApplicationData {
    private static User currentUser = null;
    private static DatabaseReference userRef = null;
    private static ValueEventListener updateUserListener = null;
    private static List<UserDataUpdateSubscriber> userDataUpdateSubscribers = null;
    public static abstract class UserDataUpdateSubscriber {
        private boolean enable = true;

        public final boolean isEnable() {
            return enable;
        }

        public final void setEnable(boolean enable) {
            this.enable = enable;
        }

        public abstract void onAfterUserDataUpdate(User currentUser);
    }

    private static TreeSet<Thread> autoSortedThreads = null;
    private static Map<String, Pair<DatabaseReference, Thread>> userThreads = null;
    private static List<UserThreadsUpdateSubscriber> userThreadsUpdateSubscribers = null;
    public static abstract class UserThreadsUpdateSubscriber {
        private boolean enable = true;

        public final boolean isEnable() {
            return enable;
        }

        public final void setEnable(boolean enable) {
            this.enable = enable;
        }

        public abstract void onAfterUserThreadsUpdate(List<Thread> autoSortedThreads);
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public interface InitCurrentUserCallback {
        void onAfterInitCurrentUser(User currentUser);
    }

    public static void initCurrentUser(final InitCurrentUserCallback callback) {
        // This check to avoid errors on multiple invoking this method.
        if (userRef == null) {
            userDataUpdateSubscribers = new ArrayList<>();

            userRef = FirebaseDatabaseUtil.getDatabase()
                    .getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            userRef.keepSynced(true);
            updateUserListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    Log.d(ChitChatConstants.TAG, "Update current user data");
                    if (currentUser == null) {
                        currentUser = user;

                        // We init user details once.
                        initUserDetails();

                        // Avoid invoking callback on next time update currentUser data.
                        // Of course, this callback must be called only on currentUser initialization.
                        callback.onAfterInitCurrentUser(currentUser);

                    } else {
                        currentUser = user;

                        for (UserDataUpdateSubscriber subscriber : userDataUpdateSubscribers) {
                            if (subscriber.isEnable()) {
                                subscriber.onAfterUserDataUpdate(currentUser);
                            }
                        }

                        updateUserDetails();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
            userRef.addValueEventListener(updateUserListener);
        } else {
            callback.onAfterInitCurrentUser(currentUser);
        }
    }

    private static void updateUserDetails() {
        // Deconstruct deleted thread details.
        boolean hasRemovedThreads = false;
        for (final Map.Entry<String, Pair<DatabaseReference, Thread>> threadDataEntry : userThreads.entrySet()) {
            if (!currentUser.getThreads().keySet().contains(threadDataEntry.getKey())) {
                Log.d(ChitChatConstants.TAG, "This threadId " + threadDataEntry.getKey() + " must be removed.");
                Pair<DatabaseReference, Thread> pair = threadDataEntry.getValue();
                if (pair != null) {
                    pair.first.keepSynced(false);
                    userThreads.remove(threadDataEntry);
                    autoSortedThreads.remove(pair.second);

                    hasRemovedThreads = true;
                }
            }
        }
        if (hasRemovedThreads) {
            Log.d(ChitChatConstants.TAG, "Has remove some threads");
            for (UserThreadsUpdateSubscriber subscriber : userThreadsUpdateSubscribers) {
                if (subscriber.isEnable()) {
                    subscriber.onAfterUserThreadsUpdate(getUserThreadDetails());
                }
            }
        }

        // Add new thread details listeners
        for (Iterator<String> it = currentUser.getThreads().keySet().iterator(); it.hasNext(); ) {
            final String threadId = it.next();
            Pair<DatabaseReference, Thread> pair = userThreads.get(threadId);
            if (pair == null) {
                final DatabaseReference threadRef = FirebaseDatabaseUtil.getDatabase()
                        .getReference("Details")
                        .child(threadId).child("details");
                threadRef.keepSynced(true);
                final ValueEventListener threadValListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Thread threadDetail = dataSnapshot.getValue(Thread.class);
                        Log.d(ChitChatConstants.TAG, "Update data of thread: " + threadId);
                        if (threadDetail != null && !currentUser.getThreads().keySet().contains(threadDetail.getId())) {
                            Log.d(ChitChatConstants.TAG, "Oop! An already removed thread listener has reloaded, be aware on this case. The ghost threadId: " + threadDetail.getId());
                            // Do nothing in this case because we already handled at deconstructing deleted thread details.
                            return;
                        }

                        Pair<DatabaseReference, Thread> databaseReferenceThreadPair = userThreads.get(threadId);
                        if (databaseReferenceThreadPair != null) {
                            Thread currentThreadDetail = databaseReferenceThreadPair.second;
                            if (currentThreadDetail != null) {
                                autoSortedThreads.remove(currentThreadDetail);
                            }
                        }

                        if (threadDetail != null) {
                            userThreads.put(threadId, Pair.create(threadRef, threadDetail));
                            autoSortedThreads.add(threadDetail);
                        } else {
                            Log.e(ChitChatConstants.TAG, "Something bad happened with " + threadId + ", there is no thread detail of the thread.");
                            threadRef.keepSynced(false);
                            userThreads.remove(threadId);
                        }

                        for (UserThreadsUpdateSubscriber subscriber : userThreadsUpdateSubscribers) {
                            if (subscriber.isEnable()) {
                                subscriber.onAfterUserThreadsUpdate(getUserThreadDetails());
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                threadRef.addValueEventListener(threadValListener);
            }
        }
    }

    private static void destroyUserDetails() {
        autoSortedThreads = null;
        for (Pair<DatabaseReference, Thread> pair : userThreads.values()) {
            pair.first.keepSynced(false);
        }
        userThreads = null;
        userThreadsUpdateSubscribers = null;
    }

    private static void initUserDetails() {
        autoSortedThreads = new TreeSet<>();
        userThreads = new HashMap<>();
        userThreadsUpdateSubscribers = new ArrayList<>();

        updateUserDetails();
    }

    public static List<Thread> getUserThreadDetails() {
        return new ArrayList<>(autoSortedThreads);
    }

    public static void addUserDataUpdateSubscribers(UserDataUpdateSubscriber subscriber) {
        userDataUpdateSubscribers.add(subscriber);
    }

    public static void removeUserDataUpdateSubscribers(UserDataUpdateSubscriber subscriber) {
        if (subscriber != null && userDataUpdateSubscribers != null) {
            userDataUpdateSubscribers.remove(subscriber);
        }
    }

    public static void addUserThreadsUpdateSubscribers(UserThreadsUpdateSubscriber subscriber) {
        userThreadsUpdateSubscribers.add(subscriber);
    }

    public static void removeUserThreadsUpdateSubscribers(UserThreadsUpdateSubscriber subscriber) {
        if (subscriber != null && userThreadsUpdateSubscribers != null) {
            userThreadsUpdateSubscribers.remove(subscriber);
        }
    }

    public static void removeCurrentUser() {
        destroyUserDetails();

        userRef.removeEventListener(updateUserListener);
        userRef = null;
        updateUserListener = null;
        currentUser = null;
        userDataUpdateSubscribers = null;
    }
}
