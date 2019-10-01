package com.g10.chit_chat.chatapp.group.newmember;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.BaseRecyclerViewAdapter;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.chat.view.ChatActivity;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.datamodel.Thread;
import com.g10.chit_chat.chatapp.utils.StringUtils;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.g10.chit_chat.chatapp.group.groupmembers.GroupMembersActivity;
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

public class NewMemberAdapter extends BaseRecyclerViewAdapter<NewMemberAdapter.ViewHolder> {

    private List<User> mUsers;
    private Thread threadData;

    public NewMemberAdapter(Context mContext, List<User> mUsers, Thread threadData) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.threadData = threadData;
    }

    @NonNull
    @Override
    public NewMemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, viewGroup, false);

        return new NewMemberAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NewMemberAdapter.ViewHolder viewHolder, int i) {
        final User user = mUsers.get(i);
        if (threadData.getUsers().keySet().contains(user.getId())) {
            viewHolder.itemView.setBackgroundResource(R.color.colorGrey);
        } else {
            viewHolder.itemView.setBackgroundResource(0);
        }
        StringUtils.setUsername(viewHolder.username, user.getUsername());
        ImageHelper.applyProfileImageValue(user.getImageURL(), viewHolder.profile_image, mContext);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!threadData.getUsers().keySet().contains(user.getId())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    builder.setMessage(mContext.getString(R.string.ask_add_member_to_the_group, StringUtils.capitalize(user.getUsername()), threadData.getName()))
                            .setTitle(R.string.app_name);

                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // store members into users child of Threads Table
                            final DatabaseReference detailsReference = FirebaseDatabaseUtil
                                    .getDatabase()
                                    .getReference("Details");

                            Map<String, Object> userMap = new HashMap<>();
                            final String threadId = threadData.getId();
                            final String userId = user.getId();

                            userMap.put("/" + threadId + "/details/" + "users/" + userId + "/joinTime", ServerValue.TIMESTAMP);
                            userMap.put("/" + threadId + "/details/" + "users/" + userId + "/username", user.getUsername());
                            userMap.put("/" + threadId + "/details/" + "users/" + userId + "/imageURL", user.getImageURL());
                            userMap.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_FIELD_NAME, StringUtils.capitalize(ApplicationData.getCurrentUser().getUsername()) + " just added " + StringUtils.capitalize(user.getUsername()) + " to the group " + threadData.getName());
                            userMap.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_SENDER_FIELD_NAME, ApplicationData.getCurrentUser().getUsername());
                            userMap.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_TIMESTAMP_FIELD_NAME, ServerValue.TIMESTAMP);
                            userMap.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_IS_STATUS_MESSAGE_FIELD_NAME, 1);

                            detailsReference
                                    .updateChildren(userMap)
                                    .addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            DatabaseReference threadReference = FirebaseDatabaseUtil
                                                    .getDatabase()
                                                    .getReference("Threads");

                                            // set sender and receiver in the Threads database
                                            Map<String, Object> clientMap = new HashMap<>();
                                            clientMap.put("/" + threadId + "/users/" + userId, ServerValue.TIMESTAMP);

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
                                                            threadMap.put(threadId, ServerValue.TIMESTAMP);

                                                            usersReference
                                                                    .child(userId)
                                                                    .child("threads")
                                                                    .updateChildren(threadMap)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            detailsReference.child("/" + threadId + "/details/").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                    Intent intent = new Intent(mContext, GroupMembersActivity.class);
                                                                                    intent.putExtra(ChatActivity.THREAD_DATA, dataSnapshot.getValue(Thread.class));
                                                                                    getParentActivity().startActivity(intent);
                                                                                    getParentActivity().finish();
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                            });

                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
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
