package com.g10.chit_chat.chatapp.datamodel;

import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.security.Timestamp;
import java.util.Map;

public class Chat {

    //public static final String loadingImageUrl = "https://github.com/adikabintang/kuliah/raw/master/mobile_cloud_computing/proj_resource/5.gif";
    private String sender;
    private String receiver;
    private String message;
    private String imageUrlLow = ImageHelper.loadingIconUrl;
    private String imageUrlHigh = ImageHelper.loadingIconUrl;
    private String imageUrlFull = ImageHelper.loadingIconUrl;
    private Long timestamp;

    public Chat(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public Chat(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public Chat() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setImageUrlLow(String url) {
        imageUrlLow = url;
    }

    public String getImageUrlLow() {
        return imageUrlLow;
    }

    public void setImageUrlHigh(String url) {
        imageUrlHigh = url;
    }

    public String getImageUrlHigh() {
        return imageUrlHigh;
    }

    public void setImageUrlFull(String url) {
        imageUrlFull = url;
    }

    public String getImageUrlFull() {
        return imageUrlFull;
    }

    public Map<String, String> getTimestamp() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public long getTimestampLong() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
