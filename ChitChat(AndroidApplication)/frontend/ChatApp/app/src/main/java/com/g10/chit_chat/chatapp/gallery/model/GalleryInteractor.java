package com.g10.chit_chat.chatapp.gallery.model;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.BaseAppCompatActivity;
import com.g10.chit_chat.chatapp.datamodel.Chat;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.StringUtils;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.g10.chit_chat.chatapp.utils.firebase.GlideApp;
import com.g10.chit_chat.chatapp.gallery.presenter.GalleryPresenter;
import com.g10.chit_chat.chatapp.utils.image.ImageCategories;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.g10.chit_chat.chatapp.utils.image.ImagePropertiesGallery;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GalleryInteractor {
    public interface OnFinishedFetchingImagesListener {
        void onImagesReceived(List<ImagePropertiesGallery> listOfSectionedImages);
        void onDestroy();
    }

    private static String TAG = "CHIT_CHAT";

    public void getAllImages(final String threadId,
                                                     final GalleryPresenter galleryPresenter) {
        final List<ImagePropertiesGallery> listImages = new ArrayList<>();

        // Avoid currentUser sees images sent at times before his join time to the chat thread.
        final Query imgQuery = FirebaseDatabaseUtil
                .getDatabase()
                .getReference("/Threads/" + threadId
                + "/messages/gallery")
                .orderByChild("timestamp")
                .startAt(Long.valueOf(ApplicationData.getCurrentUser().getThreads().get(threadId).toString()));

        imgQuery.keepSynced(true);
        // TODO: clean this callback hell
        imgQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final int size = (int) dataSnapshot.getChildrenCount();
                if (size == 0) {
                    galleryPresenter.onImagesReceived(null);
                    return;
                }

                Log.e(TAG, "getChildrenCount: " + size);
                imgQuery.limitToFirst(size)
                        .addValueEventListener(new ValueEventListener() {
                            int i = 0;

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    final Chat chat = d.getValue(Chat.class);
                                    // Do not worry, it's cached, then not all of this query use network
                                    FirebaseDatabaseUtil
                                            .getDatabase()
                                            .getReference("/Users/" + chat.getSender() + "/username/")
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    String imageUrl;
                                                    if (ApplicationData.getCurrentUser().getImageResolutionSetting()
                                                            == User.ImageResolutionOptions.FULL) {
                                                        imageUrl = chat.getImageUrlFull();
                                                    } else if (ApplicationData.getCurrentUser().getImageResolutionSetting()
                                                            == User.ImageResolutionOptions.HIGH) {
                                                        imageUrl = chat.getImageUrlHigh();
                                                    } else {
                                                        imageUrl = chat.getImageUrlLow();
                                                    }
                                                    listImages.add(new ImagePropertiesGallery(imageUrl,
                                                            // We display capitalized username.
                                                            StringUtils.capitalize(dataSnapshot.getValue().toString()), chat.getTimestampLong()));
                                                    i++;
                                                    if (i == size) {
                                                        // TODO: IT MAY NEED TO REMOVE LISTENER
                                                        Collections.reverse(listImages);
                                                        galleryPresenter.onImagesReceived(listImages);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void labelImages(final List<ImagePropertiesGallery> listOfImages,
                            BaseAppCompatActivity activity,
                            final GalleryPresenter galleryPresenter) {
        final int imageItems = listOfImages.size();
        final AtomicLong sequenceNumber = new AtomicLong(0);
        for (ImagePropertiesGallery image : listOfImages) {
            final ImagePropertiesGallery oneImage = image;
            if (oneImage.label == null || oneImage.label.isEmpty()) {
                Log.d(TAG, "image does not have label yet");

                GlideApp
                        .with(activity)
                        .asBitmap()
                        .load(oneImage.imageUrl.startsWith("gs")
                                ?
                                FirebaseStorage.getInstance().getReferenceFromUrl(oneImage.imageUrl)
                                : oneImage.imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(ImageHelper.createLoadingImage(activity))
                        .into(new SimpleTarget<Bitmap>() {

                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                FirebaseVisionImage fImage = FirebaseVisionImage.fromBitmap(resource);
                                FirebaseVisionCloudDetectorOptions options =
                                        new FirebaseVisionCloudDetectorOptions.Builder()
                                                .setMaxResults(1)
                                                .build();
                                FirebaseVisionCloudLabelDetector detector = FirebaseVision
                                        .getInstance().getVisionCloudLabelDetector(options);

                                detector.detectInImage(fImage)
                                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLabel>>() {
                                            @Override
                                            public void onSuccess(List<FirebaseVisionCloudLabel> firebaseVisionCloudLabels) {
                                                for (FirebaseVisionCloudLabel f : firebaseVisionCloudLabels) {
                                                    oneImage.label = f.getLabel();
                                                    oneImage.category = getCategory(oneImage.label);
                                                    Log.d(TAG, "firebaseVisionCloudLabels: " + f.getLabel());

                                                    long currentCounter = sequenceNumber.getAndIncrement();
                                                    if (currentCounter == imageItems - 1) {
                                                        galleryPresenter.onFinishedLabeling(listOfImages);
                                                    }
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "cloud-based detectInImage error: " + e.getMessage());
                                            }
                                        });
                            }
                        });
            } else {
                Log.d(TAG, "image has label");
                long currentCounter = sequenceNumber.getAndIncrement();
                if (currentCounter == imageItems - 1) {
                    galleryPresenter.onFinishedLabeling(listOfImages);
                }
            }
        }
    }

    private String getCategory(String label) {
        Log.d(TAG, "label: " + label);
        for (Map.Entry<String, List<String>> dict : ImageCategories.CATEGORIES.entrySet()) {
            Log.d(TAG, "key: " + dict.getKey());
            for (String value : dict.getValue()) {
                if (label.contains(value)) {
                    Log.d(TAG, "CATEGORY: " + dict.getKey());
                    return dict.getKey();
                }
            }
        }
        return "Others";
    }
}
