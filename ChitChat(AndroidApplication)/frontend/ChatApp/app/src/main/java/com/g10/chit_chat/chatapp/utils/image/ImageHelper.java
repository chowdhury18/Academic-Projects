package com.g10.chit_chat.chatapp.utils.image;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.datamodel.Chat;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.g10.chit_chat.chatapp.utils.firebase.GlideApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;

public class ImageHelper {
    private static final String TAG = "CHIT_CHAT";

    public static String loadingIconUrl =
           "https://github.com/adikabintang/kuliah/blob/master/mobile_cloud_computing/proj_resource/loading_small.gif";

    public static File createImageJPGFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        // mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static void uploadImage(final Uri photoUri,
                                   final String threadId,
                                   final String senderUid,
                                   String receiverUid,
                                   final Context context) {
        DatabaseReference reference = FirebaseDatabaseUtil.getDatabase().getReference();

        Chat mImg = new Chat();
        mImg.setSender(senderUid);
        mImg.setReceiver(receiverUid);
        final String messageId = reference.push().getKey();

        reference.child("Threads").child(threadId)
                .child("messages/chats").child(messageId).setValue(mImg);

        DatabaseReference detailReference = FirebaseDatabaseUtil
                .getDatabase()
                .getReference();

        detailReference.child("Details").child(threadId)
                .child("details").child("lastMessage").setValue("[Photo]");
        detailReference.child("Details").child(threadId)
                .child("details").child("lastMessageSenderId").setValue(senderUid);
        detailReference.child("Details").child(threadId)
                .child("details").child("lastMessageTimestamp").setValue(ServerValue.TIMESTAMP);

        Log.d(TAG, "upload messageId: " + messageId);

        // https://firebase.google.com/docs/storage/android/upload-files

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        String filename = getFileName(photoUri, context);
        String fileExtension = FilenameUtils.getExtension(filename);

        long nowMs = System.currentTimeMillis();
        String uploadedImageLocation = "images/" + senderUid + "/full/img-" +
                String.valueOf(nowMs) + "." + fileExtension;

        final StorageReference riversRef = storageReference.child(uploadedImageLocation);

        final String settingRes = ApplicationData.getCurrentUser().getImageResolutionSetting().name();

        FirebaseDatabaseUtil.getDatabase().getReference("/Threads/" + threadId + "/users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, String> usersAndTime = (HashMap<String, String>) dataSnapshot.getValue();
                        String listOfUserIds = "";
                        for (Map.Entry<String, String> entry : usersAndTime.entrySet()) {
                            listOfUserIds += entry.getKey();
                        }
                        StorageMetadata metadata = new StorageMetadata.Builder()
                                .setCustomMetadata("output_image_res", settingRes)
                                .setCustomMetadata("sender", senderUid)
                                .setCustomMetadata("messageId", messageId)
                                .setCustomMetadata("threadId", threadId)
                                .setCustomMetadata("owners", listOfUserIds)
                                .build();

                        UploadTask uploadTask;
                        if (settingRes == User.ImageResolutionOptions.FULL.name()) {
                            uploadTask = riversRef.putFile(photoUri, metadata);
                        } else {
                            uploadTask = riversRef.putBytes(resizeImageToByte(photoUri, settingRes,
                                    context), metadata);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private static String getFileName(Uri uri, Context context) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver()
                    .query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private static byte[] resizeImageToByte(Uri photoUri, String resSetting, Context context) {
        try {
            Bitmap inputImage = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(), photoUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap newImage;
            // https://stackoverflow.com/questions/15440647/scaled-bitmap-maintaining-aspect-ratio
            if (resSetting == User.ImageResolutionOptions.LOW.name()) {
                float factor = 640 / (float) inputImage.getWidth();
                newImage =  Bitmap.createScaledBitmap(inputImage, 640,
                        (int) (inputImage.getHeight() * factor), true);
                //newImage = Bitmap.createScaledBitmap(inputImage, 640, 480, true);
            } else {
                float factor = 1280 / (float) inputImage.getWidth();
                newImage =  Bitmap.createScaledBitmap(inputImage, 1280,
                        (int) (inputImage.getHeight() * factor), true);
                //newImage = Bitmap.createScaledBitmap(inputImage, 1280, 960, true);
            }

            newImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "error resizeImageToByte: " + e.getMessage());
            return new byte[0];
        }
    }

    public static void getPermissionDownloadImage(Activity activity) {
        if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG,"You have permission");
        } else {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(activity, permissions, 1);
        }
    }

    public static void applyProfileImageValue(String imageUrl, ImageView profileImage, Context context) {
        if (imageUrl.equals("default")) {
            setCircleImage(profileImage, R.drawable.default_avatar);
        } else {
            try {
                if (imageUrl.startsWith("gs://")) {
                    StorageReference storageReference =
                            FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

                    GlideApp.with(context)
                            .load(storageReference)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(createLoadingImage(context, true))
                            .into(profileImage);
                } else if (imageUrl.startsWith("http")) {
                    profileImage.setImageDrawable(createLoadingImage(context, true));
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error on apply the image: " + imageUrl);
                Log.e(TAG, "Error on applying the above image, message: " + ex.getMessage());
                setCircleImage(profileImage, R.drawable.default_avatar);
            }
        }
    }

    public static void setCircleImage(ImageView profileImage, @RawRes @DrawableRes @Nullable Integer id) {
        GlideApp.with(profileImage.getContext())
                .load(id)
                .apply(RequestOptions.circleCropTransform())
                .into(profileImage);
    }

    @NonNull
    public static Drawable createLoadingImage(Context context) {
        return createLoadingImage(context, false);
    }

    @NonNull
    public static Drawable createLoadingImage(Context context, boolean circle) {
        try {
            return new GifDrawable(context.getResources(),
                    circle ? R.drawable.loading_small_circle : R.drawable.loading_small);
        } catch (IOException ex) {
            Log.e(TAG, "Error on creating the loading image, message: " + ex.getMessage());
            return ContextCompat.getDrawable(context, R.drawable.loading_small);
        }
    }
}
