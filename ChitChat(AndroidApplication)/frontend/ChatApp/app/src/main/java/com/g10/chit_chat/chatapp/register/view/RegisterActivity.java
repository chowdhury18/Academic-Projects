package com.g10.chit_chat.chatapp.register.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.g10.chit_chat.chatapp.BaseAppCompatActivity;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.login.view.LoginActivity;
import com.g10.chit_chat.chatapp.main.view.MainActivity;
import com.g10.chit_chat.chatapp.register.model.RegisterInteractor;
import com.g10.chit_chat.chatapp.register.presentation.RegisterPresenter;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterActivity extends BaseAppCompatActivity implements RegisterView {

    MaterialEditText username, email, password,re_password;
    ImageView profileImage;
    Button btnRegister;
    RegisterPresenter registerPresenter;
    Integer REQUEST_CAMERA = 1, SELECT_FILE = 0, REQUEST_IMAGE_CAPTURE = 1;
    Uri selectedImageUri;

    String usernameTxt;
    String emailTxt;
    String passwordTxt;
    String re_passwordTxt;
    String mCurrentPhotoPath;
    private Bitmap bitmapProfileImage;
    private byte[] imageData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = (MaterialEditText) findViewById(R.id.username);
        email = (MaterialEditText) findViewById(R.id.email);
        password = (MaterialEditText) findViewById(R.id.password);
        profileImage = (ImageView)  findViewById(R.id.imageView);
        re_password = (MaterialEditText) findViewById(R.id.re_password);
        btnRegister = (Button) findViewById(R.id.btn_register);

        registerPresenter = new RegisterPresenter(this, new RegisterInteractor());

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usernameTxt = username.getText().toString().trim().toLowerCase();
                emailTxt = email.getText().toString();
                passwordTxt = password.getText().toString();
                re_passwordTxt = re_password.getText().toString();

                if (TextUtils.isEmpty(usernameTxt)
                        || TextUtils.isEmpty(emailTxt) || TextUtils.isEmpty(passwordTxt)) {
                    Toast.makeText(RegisterActivity.this,
                            "Do not leave empty field", Toast.LENGTH_SHORT).show();
                } else {
                    if (passwordTxt.length() < 6) {
                        Toast.makeText(RegisterActivity.this,
                                "Password length at least 6 characters",
                                Toast.LENGTH_SHORT).show();
                        return;
                    } else if (!passwordTxt.equals(re_passwordTxt)){
                        Toast.makeText(RegisterActivity.this,
                                "Password does not match.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(RegisterActivity.this, "Registering...",
                            Toast.LENGTH_LONG).show();
                    registerPresenter.validateUsername(usernameTxt);
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {registerPresenter.uploadImage();
            }
        });
    }


    @Override
    public void navigateToHome() {
        Toast.makeText(RegisterActivity.this,
                "Registration complete", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void uploadImage() {
        final String[] options = {getString(R.string.from_camera), getString(R.string.from_gallery)};

        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle(R.string.pick_image_source);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(getString(R.string.from_camera).equals(options[i])){
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        //TAKE PHOTO USING CAMERA...START
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                        }
                        Log.d("MY_TAG", "photoFile: " + photoFile);
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(RegisterActivity.this,
                                    "com.g10.chit_chat.chatapp.fileprovider",
                                    photoFile);
                            Log.d("MY_TAG", "photoURI: " + photoURI);
                            selectedImageUri = photoURI;

                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        }
                        //TAKE PHOTO USING CAMERA...END
                    }
                } else if(getString(R.string.from_gallery).equals(options[i])){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent,"Select File"),SELECT_FILE);
                }
            }
        });
        builder.show();
    }


    //TAKE PHOTO USING CAMERA...START
    private File createImageFile() throws IOException {
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
    //TAKE PHOTO USING CAMERA...END

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
                    imageData = baos.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (requestCode == SELECT_FILE) {
                selectedImageUri = data.getData();
                Log.d(TAG, "REGISTERACTIVITY datagetdata: " + selectedImageUri);
                profileImage.setImageURI(selectedImageUri);

                //image resizing
                try {
                    Bitmap b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    float factor = 640 / (float) b.getWidth();
                    bitmapProfileImage =  Bitmap.createScaledBitmap(b, 640,
                            (int) (b.getHeight() * factor), true);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmapProfileImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    imageData = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void errorRegistration() {
        Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void errorUsernamevalidation() {
        Toast.makeText(RegisterActivity.this, "Same username exists", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessValidating() {
        Log.d("MY_TAG", "onSuccessValidating: " + usernameTxt + " " + emailTxt);
        registerPresenter.register(usernameTxt,
                emailTxt,
                passwordTxt, selectedImageUri, imageData);
        //navigateToHome();
    }



    @Override
    public void onDestroy() {
        registerPresenter.onDestroy();
        super.onDestroy();
    }

}
