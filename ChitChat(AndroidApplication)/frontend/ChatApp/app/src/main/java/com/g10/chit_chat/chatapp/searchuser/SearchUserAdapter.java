package com.g10.chit_chat.chatapp.searchuser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.BaseRecyclerViewAdapter;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.chat.view.ChatActivity;
import com.g10.chit_chat.chatapp.datamodel.Thread;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.StringUtils;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchUserAdapter extends BaseRecyclerViewAdapter<SearchUserAdapter.ViewHolder> {

    private List<User> mUsers;

    public SearchUserAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, viewGroup, false);

        return new SearchUserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final User user = mUsers.get(i);
        StringUtils.setUsername(viewHolder.username, user.getUsername());
        Log.d(TAG, "get username: " + user.getUsername() + " get id: " + user.getId() +
                    " " + "image url: " + user.getImageURL());
        ImageHelper.applyProfileImageValue(user.getImageURL(), viewHolder.profile_image, mContext);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String targetUserId = user.getId();
                final String targetUsername = user.getUsername();
                final String currentUserId = ApplicationData.getCurrentUser().getId();
                final String threadId;
                final DatabaseReference detailsReference;
                threadId = (currentUserId.compareTo(targetUserId) < 0 ? currentUserId.concat(targetUserId) : targetUserId.concat(currentUserId));

                detailsReference = FirebaseDatabaseUtil
                        .getDatabase()
                        .getReference("Details");

                if (detailsReference != null) {
                    DatabaseReference threadReference = detailsReference.child("/" + threadId + "/details");
                    threadReference.keepSynced(true);
                    threadReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            Log.e(TAG, "check exist dataSnapshot for thread: " + dataSnapshot.exists());
                            if (dataSnapshot.exists()) {
                                Thread thread = dataSnapshot.getValue(Thread.class);
                                Log.e(TAG, "check thread having data or not: " + (thread != null) + " " + thread.getCreationDate() + " " + thread.getUsers().size());
                                Intent returnIntent = new Intent(mContext, ChatActivity.class);
                                returnIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                returnIntent.putExtra(ChatActivity.THREAD_DATA, dataSnapshot.getValue(Thread.class));
                                returnIntent.putExtra(ChatActivity.THREAD_ID, threadId);
                                getParentActivity().setResult(Activity.RESULT_OK, returnIntent);
                                getParentActivity().finish();

                                return;

                            }
                            // set users who participate in the single chat
                            Map<String, Object> userMap = new HashMap<>();

                            userMap.put("/" + threadId + "/details/" + "users/" + currentUserId + "/joinTime", ServerValue.TIMESTAMP);
                            userMap.put("/" + threadId + "/details/" + "users/" + currentUserId + "/username", ApplicationData.getCurrentUser().getUsername());
                            userMap.put("/" + threadId + "/details/" + "users/" + currentUserId + "/imageURL", ApplicationData.getCurrentUser().getImageURL());
                            userMap.put("/" + threadId + "/details/" + "users/" + targetUserId + "/joinTime", ServerValue.TIMESTAMP);
                            userMap.put("/" + threadId + "/details/" + "users/" + targetUserId + "/username", targetUsername);
                            userMap.put("/" + threadId + "/details/" + "users/" + targetUserId + "/imageURL", user.getImageURL());
                            userMap.put("/" + threadId + "/details/" + "type", Thread.SINGLE_CHAT);
                            userMap.put("/" + threadId + "/details/" + "name", Thread.DEFAULT_THREAD_NAME);
                            userMap.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_FIELD_NAME, Thread.LAST_MESSAGE_NONE);
                            userMap.put("/" + threadId + "/details/" + "creationDate", ServerValue.TIMESTAMP);
                            userMap.put("/" + threadId + "/details/" + "id", threadId);

                            final String finalThreadId = threadId;

                            detailsReference
                                    .updateChildren(userMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            DatabaseReference threadReference = FirebaseDatabaseUtil
                                                    .getDatabase()
                                                    .getReference("Threads");

                                            // set sender and receiver in the Threads database
                                            Map<String, Object> clientMap = new HashMap<>();
                                            clientMap.put("/" + finalThreadId + "/users/" + currentUserId, ServerValue.TIMESTAMP);
                                            clientMap.put("/" + finalThreadId + "/users/" + targetUserId, ServerValue.TIMESTAMP);

                                            threadReference
                                                    .updateChildren(clientMap)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            DatabaseReference usersReference = FirebaseDatabaseUtil
                                                                    .getDatabase()
                                                                    .getReference("Users");

                                                            // put the new threadId into threads node in the user child
                                                            Map<String, Object> threadMap = new HashMap<>();
                                                            threadMap.put(finalThreadId, ServerValue.TIMESTAMP);

                                                            // set chat room where sender join
                                                            usersReference
                                                                    .child(currentUserId)
                                                                    .child("threads")
                                                                    .updateChildren(threadMap);

                                                            // set chat room where receiver join
                                                            usersReference
                                                                    .child(targetUserId)
                                                                    .child("threads")
                                                                    .updateChildren(threadMap)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            Intent returnIntent = new Intent(mContext, ChatActivity.class);
                                                                            returnIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            returnIntent.putExtra(ChatActivity.THREAD_DATA, dataSnapshot.getValue(Thread.class));
                                                                            returnIntent.putExtra(ChatActivity.THREAD_ID, threadId);
                                                                            getParentActivity().setResult(Activity.RESULT_OK, returnIntent);
                                                                            getParentActivity().finish();
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
        }
    }
}
