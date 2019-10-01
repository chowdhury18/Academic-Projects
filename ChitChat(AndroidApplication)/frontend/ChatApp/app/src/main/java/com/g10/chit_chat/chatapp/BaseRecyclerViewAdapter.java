package com.g10.chit_chat.chatapp;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.g10.chit_chat.chatapp.utils.ChitChatConstants;

public abstract class BaseRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected static final String TAG = ChitChatConstants.TAG;
    protected Context mContext;

    protected Activity getParentActivity() {
        return (Activity) mContext;
    }
}
