package com.g10.chit_chat.chatapp.utils.image;

public class ImagePropertiesGallery {
    public String senderName = ""; // by default, no section
    public String imageUrl;
    public String label; // for cloud based image labelling
    public String category;
    public Long timestamp;

    public ImagePropertiesGallery(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ImagePropertiesGallery(String imageUrl, String senderName) {
        this.imageUrl = imageUrl;
        this.senderName = senderName;
    }

    public ImagePropertiesGallery(String imageUrl, String senderName, Long timestamp) {
        this.imageUrl = imageUrl;
        this.senderName = senderName;
        this.timestamp = timestamp;
    }

    public ImagePropertiesGallery(String imageUrl, String senderName, String label) {
        this.imageUrl = imageUrl;
        this.senderName = senderName;
        this.label = label;
    }
}
