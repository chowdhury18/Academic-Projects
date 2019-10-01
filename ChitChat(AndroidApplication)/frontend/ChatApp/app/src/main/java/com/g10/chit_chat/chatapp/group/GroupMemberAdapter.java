package com.g10.chit_chat.chatapp.group;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

import java.util.List;

public class GroupMemberAdapter extends BaseRecyclerViewAdapter<GroupMemberAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;

    public GroupMemberAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public GroupMemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, viewGroup, false);

        return new GroupMemberAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberAdapter.ViewHolder viewHolder, int i) {
        final User user = mUsers.get(i);
        StringUtils.setUsername(viewHolder.username, user.getUsername());
        ImageHelper.applyProfileImageValue(user.getImageURL(), viewHolder.profile_image, mContext);
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
