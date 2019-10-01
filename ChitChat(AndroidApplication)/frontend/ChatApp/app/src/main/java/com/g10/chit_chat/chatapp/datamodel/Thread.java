package com.g10.chit_chat.chatapp.datamodel;

import android.content.Intent;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.HashMap;

public class Thread implements Serializable, Comparable<Thread> {
    public static final String DEFAULT_THREAD_NAME = "default";

    public static final String LAST_MESSAGE_FIELD_NAME = "lastMessage";
    public static final String LAST_MESSAGE_NONE = "none";

    public static final String LAST_MESSAGE_SENDER_FIELD_NAME = "lastMessageSenderId";

    public static final String LAST_MESSAGE_TIMESTAMP_FIELD_NAME = "lastMessageTimestamp";

    public static final String LAST_MESSAGE_IS_STATUS_MESSAGE_FIELD_NAME = "lastMessageIsStatusMessage";

    public static final int SINGLE_CHAT = 1;
    public static final int GROUP_CHAT = 2;

    private Long creationDate;
    private String id;
    private String lastMessage;
    private String lastMessageSenderId;
    private Long lastMessageTimestamp;
    private Integer lastMessageIsStatusMessage; // not null and equal 1 => to know if the last message is the status only; null or 0 for the last message is a real message.
    private String name;
    private int type;
    private HashMap<String, UserChatInfo> users;

    public Thread() {
    }

    public Thread(Long creationDate, int type, String name) {
        this.creationDate = creationDate;
        this.type = type;
        this.name = name;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Integer getLastMessageIsStatusMessage() {
        return lastMessageIsStatusMessage;
    }

    public boolean checkLastMessageIsStatusMessage() {
        return lastMessageIsStatusMessage != null && lastMessageIsStatusMessage.equals(1);
    }

    public void setLastMessageIsStatusMessage(Integer lastMessageIsStatusMessage) {
        this.lastMessageIsStatusMessage = lastMessageIsStatusMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, UserChatInfo> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, UserChatInfo> users) {
        this.users = users;
    }
    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public Long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    @Override
    public int compareTo(@NonNull Thread o) {
        if (this.getLastMessageTimestamp() == null && o.getLastMessageTimestamp() == null) {
            return 0;
        }
        // We apply the compare for descending order and null will be on top.
        // => Apply negative comparison and priority for null.

        if (this.getLastMessageTimestamp() == null) {
            return -1;
        }
        if (o.getLastMessageTimestamp() == null) {
            return 1;
        }
        return o.getLastMessageTimestamp().compareTo(this.getLastMessageTimestamp());
    }
}
