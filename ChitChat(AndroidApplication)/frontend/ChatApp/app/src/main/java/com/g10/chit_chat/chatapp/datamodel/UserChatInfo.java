package com.g10.chit_chat.chatapp.datamodel;

import java.io.Serializable;
import java.util.Date;

public class UserChatInfo implements Serializable {
    private Long joinTime;
    private String username;
    private String imageURL;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Long joinTime) {
        this.joinTime = joinTime;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

}
