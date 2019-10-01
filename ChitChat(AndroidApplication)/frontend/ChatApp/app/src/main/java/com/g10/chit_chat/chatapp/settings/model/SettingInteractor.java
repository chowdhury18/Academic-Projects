package com.g10.chit_chat.chatapp.settings.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FilenameUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SettingInteractor {
    public interface OnSettingFinishedListener {
        void onSuccessSetImageResolutionToUser();
        void setUserEmailAddress(String emailAddress);
        void setUserName(String userName);
        void setUserProfileImage(String imageUrl);
    }

    private String[] resolutionOptions = {User.ImageResolutionOptions.LOW.name(),
            User.ImageResolutionOptions.HIGH.name(),
            User.ImageResolutionOptions.FULL.name()
    };
    private static final String TAG = "CHIT_CHAT";
    private ValueEventListener firebaseListener;
    StorageReference imagePath;

    public SettingInteractor() {

    }

    public void setImageResolutionToUser(int resOption, final OnSettingFinishedListener listener) {
        FirebaseDatabaseUtil.getDatabase()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("imageResolutionSetting")
                .setValue(User.ImageResolutionOptions.valueOf(resolutionOptions[resOption].toUpperCase()))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "set image res successfull");
                            listener.onSuccessSetImageResolutionToUser();
                        } else {
                            Log.e(TAG, "set image res failed");
                        }
                    }
                });

    }

    public void readUserInfo(final OnSettingFinishedListener listener) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("MY_TAG", "userEmail: " + firebaseUser.getEmail());
        listener.setUserEmailAddress(firebaseUser.getEmail());
        listener.setUserName(ApplicationData.getCurrentUser().getUsername());
        listener.setUserProfileImage(ApplicationData.getCurrentUser().getImageURL());
    }

    public  void updateProfileImage(final Uri imageUri,
                                    byte[] bitmapImageData,
                                    final OnSettingFinishedListener listener) {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        final String userId = firebaseUser.getUid();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        if (imageUri != null) {
             imagePath = storageReference.child("user/profile/"
                     + userId + "/IMG_" + timeStamp + "."
                     + FilenameUtils.getExtension(imageUri.getLastPathSegment()));
             imagePath.putBytes(bitmapImageData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                 @Override
                 public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                     FirebaseDatabaseUtil.getDatabase()
                             .getReference("Users")
                             .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                             .child("imageURL")
                             .setValue(imagePath.toString())
                             .addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {
                                     listener.setUserProfileImage(imagePath.toString());
                                     updateImageUrlInDetails(userId, imagePath.toString());
                                 }
                             });
                 }
             });
             Log.d(TAG,"profile image is changed");
        }
    }

    private void setImageUrlPlaceHolderUrl() {
        ApplicationData.getCurrentUser().setImageURL(ImageHelper.loadingIconUrl);
    }

    private void updateImageUrlInDetails(final String userId, final String imageUrl) {
        FirebaseDatabaseUtil.getDatabase()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("threads")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, String> threadIdAndJoinTime = (HashMap<String, String>) dataSnapshot.getValue();
                        if (threadIdAndJoinTime != null) {
                            for (Map.Entry<String, String> entry : threadIdAndJoinTime.entrySet()) {
                                String threadId = entry.getKey();
                                FirebaseDatabaseUtil.getDatabase()
                                        .getReference("/Details/" + threadId + "/details/users/"
                                                + userId + "/imageURL").setValue(imageUrl);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
