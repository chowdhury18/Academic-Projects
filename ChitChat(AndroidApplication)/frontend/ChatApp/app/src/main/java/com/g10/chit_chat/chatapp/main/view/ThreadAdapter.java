package com.g10.chit_chat.chatapp.main.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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
import com.g10.chit_chat.chatapp.datamodel.UserChatInfo;
import com.g10.chit_chat.chatapp.utils.StringUtils;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Map;

public class ThreadAdapter extends BaseRecyclerViewAdapter<ThreadAdapter.ViewHolder> {

    private List<Thread> mThreads;
    private FirebaseUser fuser;

    public ThreadAdapter(Context mContext, List<Thread> mThreads) {
        this.mContext = mContext;
        this.mThreads = mThreads;
    }

    @NonNull
    @Override
    public ThreadAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.thread_item, viewGroup, false);

        return new ThreadAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ThreadAdapter.ViewHolder viewHolder, int i) {
        final Thread thread = mThreads.get(i);

        if (thread.getType() == Thread.SINGLE_CHAT) {
            String targetUsername = null;
            String targetImageURL = null;
            for (Map.Entry<String, UserChatInfo> entry : thread.getUsers().entrySet()) {
                if (!entry.getKey().equals(ApplicationData.getCurrentUser().getId())) {
                    targetUsername = entry.getValue().getUsername();
                    targetImageURL = entry.getValue().getImageURL();
                    break;
                }
            }

            StringUtils.setUsername(viewHolder.username, targetUsername);
            if (Thread.LAST_MESSAGE_NONE.equals(thread.getLastMessage())) {
                viewHolder.lastMessage.setText("");
            } else {
                viewHolder.lastMessage.setText(thread.getLastMessage());

            }
            Log.d(TAG, "targateImageUrl: " + targetImageURL + ", targetUsername: " + targetUsername);
            ImageHelper.applyProfileImageValue(targetImageURL, viewHolder.profile_image, mContext);

        } else {
            ImageHelper.setCircleImage(viewHolder.profile_image, R.drawable.ic_black_group);
            viewHolder.username.setText(thread.getName());
            if (thread.getLastMessageTimestamp() == null) {
                viewHolder.lastMessage.setText("");
            } else {
                String displayMessage = thread.getLastMessage();
                if (!thread.checkLastMessageIsStatusMessage()) {
                    String author = "You: ";
                    if (!thread.getLastMessageSenderId().equals(ApplicationData.getCurrentUser().getId())) {
                        if (!thread.getUsers().keySet().contains(thread.getLastMessageSenderId())) {
                            author = "Deleted User: ";
                        } else {
                            for (Map.Entry<String, UserChatInfo> entry : thread.getUsers().entrySet()) {
                                if (entry.getKey().equals(thread.getLastMessageSenderId())) {
                                    author = StringUtils.capitalize(entry.getValue().getUsername()) + ": ";
                                    break;
                                }
                            }
                        }

                    }
                    displayMessage = "<font color='" + getColorStr(mContext, R.color.colorPrimary) + "'>" + author + "</font>" + displayMessage;
                }
                viewHolder.lastMessage.setText(Html.fromHtml(displayMessage, Html.FROM_HTML_MODE_LEGACY));
//                    viewHolder.lastMessage.setText(displayMessage);
            }
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra(ChatActivity.THREAD_ID, thread.getId());
                mContext.startActivity(intent);
            }
        });

    }

    private String getColorStr(Context context, int res) {
        return "#" + Integer.toHexString(ContextCompat.getColor(context, res) & 0x00ffffff);
    }

    @Override
    public int getItemCount() {
        return mThreads.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public TextView lastMessage;
        public ImageView profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }

        public void setHidden(boolean hidden) {
            if (hidden) {
                this.itemView.setVisibility(View.GONE);
            } else {
                this.itemView.setVisibility(View.VISIBLE);
            }
        }
    }


}

