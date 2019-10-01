package com.g10.chit_chat.chatapp.datamodel;

import android.media.Image;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;

public class User implements Serializable {

    public enum ImageResolutionOptions {
        LOW("Low"),
        HIGH("High"),
        FULL("Full");

        private String displayValue;

        ImageResolutionOptions(String displayValue) {
            this.displayValue = displayValue;
        }

        public String getDisplayValue() {
            return displayValue;
        }
    }

    private String id;
    private String username;
    private String imageURL;
    private String imageResolutionSetting;
    private HashMap<String, Object> threads = new HashMap<>();
    public static String instanceId;

    public User(String id, String username, String imageURL) {
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public ImageResolutionOptions getImageResolutionSetting() {
        if (imageResolutionSetting == null) {
            setImageResolutionSetting(ImageResolutionOptions.FULL.name());
        }
        return ImageResolutionOptions.valueOf(imageResolutionSetting);
    }

    public HashMap<String, Object> getThreads() {
        return threads;
    }

    public void setThreads(HashMap<String, Object> threads) {
        this.threads = threads;
    }

    public void setImageResolutionSetting(String imageResolutionSetting) {
        this.imageResolutionSetting = imageResolutionSetting;
    }

}
