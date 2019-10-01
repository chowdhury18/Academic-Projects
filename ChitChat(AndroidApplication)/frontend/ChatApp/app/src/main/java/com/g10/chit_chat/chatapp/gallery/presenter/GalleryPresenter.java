package com.g10.chit_chat.chatapp.gallery.presenter;

import com.g10.chit_chat.chatapp.BaseAppCompatActivity;
import com.g10.chit_chat.chatapp.gallery.model.GalleryInteractor;
import com.g10.chit_chat.chatapp.gallery.view.GalleryView;
import com.g10.chit_chat.chatapp.utils.image.ImagePropertiesGallery;

import java.util.List;

public class GalleryPresenter implements GalleryInteractor.OnFinishedFetchingImagesListener {
    private GalleryView galleryView;
    private GalleryInteractor galleryInteractor;

    public GalleryPresenter(GalleryView galleryView, GalleryInteractor galleryInteractor) {
        this.galleryInteractor = galleryInteractor;
        this.galleryView = galleryView;
    }

    public void getAllImages(String threadId) {
        galleryInteractor.getAllImages(threadId, this);
    }

    @Override
    public void onImagesReceived(List<ImagePropertiesGallery> listOfSectionedImages) {
        galleryView.onImagesReceived(listOfSectionedImages);
    }

    @Override
    public void onDestroy() {
        galleryView = null;
    }

    public void labelImages(List<ImagePropertiesGallery> listOfImages, BaseAppCompatActivity activity) {
        galleryInteractor.labelImages(listOfImages, activity, this);
    }

    public void onFinishedLabeling(List<ImagePropertiesGallery> listOfImages) {
        galleryView.onFinishedLabeling(listOfImages);
    }
}
