package com.g10.chit_chat.chatapp.settings.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.BaseAppCompatActivity;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.StringUtils;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.g10.chit_chat.chatapp.settings.model.SettingInteractor;
import com.g10.chit_chat.chatapp.settings.presentation.SettingPresenter;

import java.io.File;
import java.io.IOException;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingActivity extends BaseAppCompatActivity implements SettingView {
    private TextView settingResolutionTextView;
    private TextView currentResSetting;
    private TextView username;
    private TextView email;
    private ImageView profileImage;
    private AlertDialog imageSettingDialog;
    SettingPresenter settingPresenter;
    String mCurrentPhotoPath;
    Uri selectedImageUri;
    private Bitmap bitmapProfileImage;
    Integer REQUEST_IMAGE_CAPTURE = 1, SELECT_FILE = 0;
    private String[] resolutionOptions = {User.ImageResolutionOptions.LOW.getDisplayValue(),
            User.ImageResolutionOptions.HIGH.getDisplayValue(),
            User.ImageResolutionOptions.FULL.getDisplayValue()
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //SettingPresenter
        Log.d("MY_TAG", "Creating Setting Presenter");
        settingPresenter = new SettingPresenter(this,new SettingInteractor());

        //user information
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        profileImage = findViewById(R.id.profile_image);

        //image resolution
        settingResolutionTextView = findViewById(R.id.imageResResttingClickableText);
        currentResSetting = findViewById(R.id.current_res_setting);

        View.OnClickListener imageResSettingAction = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Setting is clicked");

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Image Resolution Setting");
                builder.setSingleChoiceItems(resolutionOptions, ApplicationData.getCurrentUser().getImageResolutionSetting().ordinal(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settingPresenter.setImageResolutionToUser(which);
                    }
                });
                imageSettingDialog = builder.show();
            }
        };

        settingResolutionTextView.setOnClickListener(imageResSettingAction);
        currentResSetting.setOnClickListener(imageResSettingAction);

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this,
                        UsernameUpdateActivity.class));
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfileImage();
            }
        });
    }

    //update profile image starts here
    public void updateProfileImage(){
        final CharSequence[] options = { getString(R.string.from_camera), getString(R.string.from_gallery)};
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("Update Image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which].equals(getString(R.string.from_camera))){
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if(takePictureIntent.resolveActivity(getPackageManager()) != null){
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex){

                        }
                        if(photoFile != null){
                            Uri photoURI = FileProvider.getUriForFile(SettingActivity.this,
                                    "com.g10.chit_chat.chatapp.fileprovider", photoFile);
                            selectedImageUri = photoURI;
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                } else if(options[which].equals(getString(R.string.from_gallery))){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Select File"),SELECT_FILE);
                }
            }
        });
        builder.show();
    }

    private File createImageFile() throws IOException{
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                try {
                    Bitmap b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    profileImage.setImageBitmap(b);

                    //image resizing
                    float factor = 640 / (float) b.getWidth();
                    bitmapProfileImage =  Bitmap.createScaledBitmap(b, 640,
                            (int) (b.getHeight() * factor), true);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmapProfileImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();

                    settingPresenter.updateProfileImage(selectedImageUri,imageData);
                    Log.d(TAG,"selectedImageUri(Camera): " + selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == SELECT_FILE) {
                selectedImageUri = data.getData();
                Log.d(TAG, "SETTING datagetdata: " + selectedImageUri);
                profileImage.setImageURI(selectedImageUri);

                //image resizing
                try {
                    Bitmap b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    float factor = 640 / (float) b.getWidth();
                    bitmapProfileImage =  Bitmap.createScaledBitmap(b, 640,
                            (int) (b.getHeight() * factor), true);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmapProfileImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();

                    settingPresenter.updateProfileImage(selectedImageUri,imageData);
                    Log.d(TAG,"selectedImageUri(Gallery): " + selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        settingPresenter.readUserInfo();
        currentResSetting.setText(ApplicationData.getCurrentUser().getImageResolutionSetting().getDisplayValue());
        super.onStart();
    }

    @Override
    public void onSuccessSetImageResolutionToUser() {
        currentResSetting.setText(ApplicationData.getCurrentUser().getImageResolutionSetting().getDisplayValue());
        imageSettingDialog.dismiss();
    }

    @Override
    public void setUserEmailAddess(String emailAddess) {
        email.setText(emailAddess);
    }

    @Override
    public void setUserName(String userName) {
        StringUtils.setUsername(username, userName);
    }

    @Override
    public void setUserProfileImage(String imageUrl) {
        ImageHelper.applyProfileImageValue(imageUrl, profileImage, SettingActivity.this);
    }

    @Override
    protected void onDestroy() {
        settingPresenter.onDestroy();
        super.onDestroy();
    }

}
