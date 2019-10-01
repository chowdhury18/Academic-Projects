package com.g10.chit_chat.chatapp.register.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.firebase.FCMService;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterInteractor {
    public interface OnRegisterFinishedListener {
        void onSuccess();
        void onError();
        void onUsernameError();
        void onSuccessValidating();
    }

    private static final String TAG = "CHIT_CHAT";

    DatabaseReference reference;
    FirebaseStorage storage;
    StorageReference storageReference,imagePath;
    String imageAddress;
    boolean flag = false;
    boolean sameUsername = false;

    public void validateUsername(final String user, final OnRegisterFinishedListener listener){
        reference = FirebaseDatabaseUtil.getDatabase().getReference().child("Users");
        sameUsername = false;
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    sameUsername = collectUsername((Map<String,Object>) dataSnapshot.getValue(), user);
                    Log.d("MY_TAG", "sameUsername: " + sameUsername);
                    if (sameUsername) {
                        listener.onUsernameError();
                    } else {
                        listener.onSuccessValidating();
                    }
                }else{
                    listener.onSuccessValidating();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void register(final String username,
                         String email,
                         String password,
                         final Uri imageUri,
                         final byte[] imageData,
                         final OnRegisterFinishedListener listener) {

        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = FirebaseAuth.
                                        getInstance().
                                        getCurrentUser();

                                assert firebaseUser != null;
                                final String userid = firebaseUser.getUid();

                                //Firebase Storage
                                assert storage != null;
                                storage = FirebaseStorage.getInstance();
                                storageReference = storage.getReference();
                                assert storageReference != null;

                                if(imageUri == null){
                                    imageAddress = "default";
                                } else {
                                    //imagePath = storageReference.child("profile").child(imageUri.getLastPathSegment());
                                    Log.d("MY_TAG", "imageUri: " + imageUri.getLastPathSegment());
                                    //imagePath = storageReference.child("user/profile/" + userid + "." + FilenameUtils.getExtension(imageUri.getLastPathSegment()));
                                    imagePath = storageReference.child("user/profile/"
                                                + userid + "/IMG_" + timeStamp + "."
                                                + FilenameUtils.getExtension(imageUri.getLastPathSegment()));
                                    String URL_path = "user/profile/"
                                            + userid + "." + FilenameUtils.getExtension(imageUri.getLastPathSegment());
                                    Log.d("MY_TAG", "URL_path: user/profile/"
                                            + userid + "." + FilenameUtils.getExtension(imageUri.getLastPathSegment()));
                                    //imagePath.putFile(imageUri);
                                    Log.d("MY_TAG", "imagePath: " + imagePath.toString());
                                    imageAddress = imagePath.toString();

                                    imagePath.putBytes(imageData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            Log.d("MY_TAG", "Done image uploading");
                                            FirebaseDatabaseUtil.getDatabase()
                                                    .getReference("Users")
                                                    .child(userid).child("imageURL")
                                                    .setValue(imageAddress);
                                        }
                                    });
//                                    imagePath.putFile(imageUri).addOnSuccessListener(
//                                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                                @Override
//                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                                    imagePath.getDownloadUrl()
//                                                            .addOnCompleteListener(new OnCompleteListener<Uri>() {
//                                                                @Override
//                                                                public void onComplete(@NonNull Task<Uri> task) {
//                                                                    if (task.isSuccessful()) {
//                                                                        Uri downloadUri = task.getResult();
//                                                                        Log.d(TAG, "DownloadUri: " + downloadUri.toString());
//                                                                    } else {
//                                                                        // Handle failures
//                                                                        // ...
//                                                                    }
//                                                                }
//                                                            });
//                                                }
//                                            }
//                                    );

                                    //Image Address

                                }

                                reference = FirebaseDatabaseUtil.getDatabase().getReference("Users").child(userid);
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("id", userid);
                                hashMap.put("username", username);
                                if ("default".equals(imageAddress)) {
                                    hashMap.put("imageURL", imageAddress);

                                } else {
                                    hashMap.put("imageURL", ImageHelper.loadingIconUrl);
                                }
                                hashMap.put("imageResolutionSetting",
                                        User.ImageResolutionOptions.FULL.name());

                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "Registration successfull");
                                            FCMService.updateToken();
                                            listener.onSuccess();
                                        }
                                    }
                                });
                            } else {
                                Log.e(TAG, "error registration: " + task.getException().getMessage());
                                listener.onError();
                            }
                        }
                    });

    }

    private boolean collectUsername(Map<String,Object> users, String username) {
        ArrayList<String> userNames = new ArrayList<>();
        flag = false;
        if(!users.isEmpty()){
            for (Map.Entry<String, Object> entry : users.entrySet()){

                //Get user map
                Map singleUser = (Map) entry.getValue();
                //Get phone field and append to list
                userNames.add((String) singleUser.get("username"));
            }
            for(int i = 0; i < userNames.size(); i++){
                if(username.equals(userNames.get(i))){
                    flag = true;
                    break;
                }
            }
        }

        Log.d("MY_TAG", "flag: " + flag);
        return flag;
    }
}
