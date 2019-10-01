package com.g10.chit_chat.chatapp.group.addmembers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.g10.chit_chat.chatapp.BaseRecyclerViewAdapter;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.StringUtils;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;

import java.util.HashMap;
import java.util.List;

public abstract class UserToAddAdapter extends BaseRecyclerViewAdapter<UserToAddAdapter.ViewHolder> implements AddGroupMembersActivity.OnDataChangeListener {

    private Context mContext;
    private List<User> mUsers;
    private HashMap<String, User> groupMemberIdUsernameMap;

    public UserToAddAdapter(Context mContext, List<User> mUsers, HashMap<String, User> groupMemberIdUsernameMap) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.groupMemberIdUsernameMap = groupMemberIdUsernameMap;
    }

    @NonNull
    @Override
    public UserToAddAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, viewGroup, false);

        return new UserToAddAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserToAddAdapter.ViewHolder viewHolder, int i) {
        final User user = mUsers.get(i);
        if (groupMemberIdUsernameMap.keySet().contains(user.getId())) {
            viewHolder.itemView.setBackgroundResource(R.color.colorGrey);
        } else {
            viewHolder.itemView.setBackgroundResource(0);
        }
        StringUtils.setUsername(viewHolder.username, user.getUsername());
        ImageHelper.applyProfileImageValue(user.getImageURL(), viewHolder.profile_image, mContext);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // remove userid if selected twice
                if (groupMemberIdUsernameMap.keySet().contains(user.getId())) {
                    groupMemberIdUsernameMap.remove(user.getId());
                    viewHolder.itemView.setBackgroundResource(0);
                } else {
                    groupMemberIdUsernameMap.put(user.getId(), user);
                    viewHolder.itemView.setBackgroundResource(R.color.colorGrey);
                }

                onDataChanged(groupMemberIdUsernameMap);
                Log.d(TAG, "count member: " + String.valueOf(groupMemberIdUsernameMap.size()));
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
