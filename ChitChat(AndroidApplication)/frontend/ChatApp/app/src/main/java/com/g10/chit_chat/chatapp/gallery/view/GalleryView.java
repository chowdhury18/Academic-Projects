package com.g10.chit_chat.chatapp.gallery.view;

import com.g10.chit_chat.chatapp.utils.image.ImagePropertiesGallery;

import java.util.List;

public interface GalleryView {
    public void onImagesReceived(List<ImagePropertiesGallery> listOfImages);
    public void onFinishedLabeling(List<ImagePropertiesGallery> listOfImages);
}
