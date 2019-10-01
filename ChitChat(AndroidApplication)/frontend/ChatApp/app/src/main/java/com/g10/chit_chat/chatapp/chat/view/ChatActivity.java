package com.g10.chit_chat.chatapp.chat.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.BaseAppCompatActivity;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.utils.StringUtils;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.g10.chit_chat.chatapp.datamodel.Chat;
import com.g10.chit_chat.chatapp.datamodel.Thread;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.g10.chit_chat.chatapp.gallery.view.GalleryActivity;
import com.g10.chit_chat.chatapp.group.groupmembers.GroupMembersActivity;
import com.g10.chit_chat.chatapp.main.view.MainActivity;
import com.g10.chit_chat.chatapp.utils.firebase.FCMService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends BaseAppCompatActivity {
    public static final String THREAD_DATA = "threadData";
    public static final String THREAD_ID = "threadId";
    private ImageView profileImage;
    private TextView username;
    private ImageButton btnSend;
    private EditText textSend;
    private ImageButton btnPickPitures;

    private FirebaseUser fUser;
    private DatabaseReference usersReference;
    private DatabaseReference threadsReference;
    private DatabaseReference detailsReference;

    private MessageAdapter messageAdapter;
    private List<Chat> mchats;
    private RecyclerView recyclerView;

    private Intent intent;
    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAMERA = 0;
    private String currentPhotoFromCameraPath;
    private String receiverId;
    private String threadId;
    private Thread currentThreadDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        FCMService.updateToken();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btnSend = findViewById(R.id.btn_send);
        textSend = findViewById(R.id.text_send);
        btnPickPitures = findViewById(R.id.btn_attach_picture);

        btnPickPitures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] options = {getString(R.string.from_camera), getString(R.string.from_gallery)};

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(getString(R.string.pick_image_source));
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (getString(R.string.from_camera).equals(options[i])) {
                            pickPhotoFromCamera();
                        } else if (getString(R.string.from_gallery).equals(options[i])) {
                            pickPhotoFromGallery();
                        }
                    }
                });
                builder.show();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = textSend.getText().toString();

                if(!msg.equals("")){
                    sendMessage(fUser.getUid(), threadId, msg);
                } else {
                    Toast.makeText(ChatActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }

                textSend.setText("");
            }
        });

        usersReference = FirebaseDatabaseUtil.getDatabase().getReference("Users");
        threadsReference = FirebaseDatabaseUtil.getDatabase().getReference("Threads");
        detailsReference = FirebaseDatabaseUtil.getDatabase().getReference("Details");

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        intent = getIntent();
        Log.d(TAG, "thread id luar angkasa: " + intent.getStringExtra("fromThreadId"));
        if (intent.getStringExtra(THREAD_ID) != null) {
            threadId = intent.getStringExtra(THREAD_ID);
        } else if (intent.getStringExtra("fromThreadId") != null) {
            threadId = intent.getStringExtra("fromThreadId");
        }

        if (!ApplicationData.getCurrentUser().getThreads().containsKey(threadId)) {
            Log.e(TAG, "Something bad happened with " + threadId);
            onBackPressed();
            return;
        }
        Log.d(TAG, "ON CREATE, threadId: " + threadId);

    }

    @Override
    public void onStart() {
        Log.d(TAG, "ON START, threadId: " + threadId);
        Thread threadData = (Thread) intent.getSerializableExtra(THREAD_DATA);
        if (threadData != null) {
            applyThreadData(threadData);
        } else {
            detailsReference
                    .child(threadId)
                    .child("details")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            applyThreadData(dataSnapshot.getValue(Thread.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
        super.onStart();
    }

    private void applyThreadData(Thread thread) {
        currentThreadDetails = thread;
        invalidateOptionsMenu();
        String currentId = fUser.getUid();

        if (currentThreadDetails.getType() == Thread.SINGLE_CHAT) { // single chat type
            for (String receiverID : currentThreadDetails.getUsers().keySet()) {
                if(!receiverID.equals(currentId)) {
                    receiverId = receiverID;
                    usersReference.child(receiverID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            StringUtils.setUsername(username, user.getUsername());
                            ImageHelper.applyProfileImageValue(user.getImageURL(), profileImage, ChatActivity.this);

                            readMessages(user.getImageURL(), threadId);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        } else {    // group chat type
            username.setText(currentThreadDetails.getName());
            ImageHelper.setCircleImage(profileImage, R.drawable.ic_grey_group);

            readMessages("default", threadId);
        }
    }

    private void sendMessage(String senderId, final String threadId, String message) {
        DatabaseReference reference = threadsReference.child(threadId).child("messages/chats");
        Chat chat = new Chat(senderId, message);

        reference.push().setValue(chat);
        detailsReference.child(threadId).child("details").child(Thread.LAST_MESSAGE_FIELD_NAME)
                .setValue(message);
        detailsReference.child(threadId).child("details").child(Thread.LAST_MESSAGE_SENDER_FIELD_NAME)
                .setValue(senderId);
        detailsReference.child(threadId).child("details").child(Thread.LAST_MESSAGE_TIMESTAMP_FIELD_NAME)
                .setValue(ServerValue.TIMESTAMP);
        detailsReference.child(threadId).child("details").child(Thread.LAST_MESSAGE_IS_STATUS_MESSAGE_FIELD_NAME)
                .setValue(0);
    }

    private void readMessages(final String imageurl, final String threadId) {
        mchats = new ArrayList<>();

        Query queryChat = threadsReference.child(threadId)
                .child("messages/chats")
                .orderByChild("timestamp")
                .startAt(Long.valueOf(ApplicationData.getCurrentUser().getThreads().get(threadId).toString()));

        ValueEventListener messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    mchats.add(chat);

                    messageAdapter = new MessageAdapter(ChatActivity.this, mchats, imageurl, currentThreadDetails);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        queryChat.addValueEventListener(messageListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentThreadDetails != null) {
            if (currentThreadDetails.getType() == Thread.SINGLE_CHAT) {
                getMenuInflater().inflate(R.menu.menu_single, menu);
            } else {
                getMenuInflater().inflate(R.menu.menu_group, menu);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_open_image_gallery:
                Intent intent = new Intent(ChatActivity.this, GalleryActivity.class);
                intent.putExtra(THREAD_ID, threadId);
                intent.putExtra(THREAD_DATA, currentThreadDetails);
                startActivity(intent);
                return true;
            case R.id.action_show_group_members:
                Intent membersIntent = new Intent(ChatActivity.this, GroupMembersActivity.class);
                membersIntent.putExtra(THREAD_DATA, currentThreadDetails);
                startActivity(membersIntent);
                return true;

            case R.id.action_leave_group:
                if (currentThreadDetails.getUsers().size() == 1) {
                    Toast.makeText(ChatActivity.this, "You cannot leave. This group must have at least one member.", Toast.LENGTH_SHORT).show();
                    return true;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.ask_leave_group, currentThreadDetails.getName()))
                        .setTitle(R.string.app_name);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String threadId = currentThreadDetails.getId();
                        final String userId = ApplicationData.getCurrentUser().getId();
                        final DatabaseReference threadReference = FirebaseDatabaseUtil.getDatabase()
                                .getReference("Details")
                                .child(threadId)
                                .child("details")
                                .child("users")
                                .child(userId);

                        threadReference.removeValue().addOnCompleteListener(
                                new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        // Remove the left user from Threads/users also.
                                        FirebaseDatabaseUtil.getDatabase()
                                                .getReference("Threads")
                                                .child(threadId)
                                                .child("users")
                                                .child(userId).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        DatabaseReference usersReference = FirebaseDatabaseUtil
                                                                .getDatabase()
                                                                .getReference("Users");

                                                        usersReference
                                                                .child(userId)
                                                                .child("threads")
                                                                .child(threadId)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        threadReference.child("/" + threadId + "/details/").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });

                        Map<String, Object> updateThreadDetailsMap = new HashMap<>();
                        updateThreadDetailsMap.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_FIELD_NAME, StringUtils.capitalize(ApplicationData.getCurrentUser().getUsername()) + " just left the group " + currentThreadDetails.getName());
                        updateThreadDetailsMap.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_SENDER_FIELD_NAME, ApplicationData.getCurrentUser().getUsername());
                        updateThreadDetailsMap.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_TIMESTAMP_FIELD_NAME, ServerValue.TIMESTAMP);
                        updateThreadDetailsMap.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_IS_STATUS_MESSAGE_FIELD_NAME, 1);
                        FirebaseDatabaseUtil.getDatabase()
                                .getReference("Details").updateChildren(updateThreadDetailsMap);
                    }
                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
        }
        return true;
    }

    /* ---------- For image related part -------------------- */

    private void pickPhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = ImageHelper.createImageJPGFile(this);
            } catch (IOException ex) {
                Log.e(TAG, "error create image file: " + ex.getMessage());
            }

            if (photoFile != null) {
                currentPhotoFromCameraPath = photoFile.getAbsolutePath();
                Uri photoURI;
                photoURI = FileProvider.getUriForFile(this,
                        "com.g10.chit_chat.chatapp.fileprovider",
                        photoFile);
                Log.d(TAG, "currentPhotoFromCameraPath: " + currentPhotoFromCameraPath);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAMERA);
            }
        }
    }

    private void pickPhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        Log.d(TAG, "requestCode: " + requestCode);
        Log.d(TAG, "data: " + currentPhotoFromCameraPath);
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri photoUri = data.getData();

            ImageHelper.uploadImage(photoUri, threadId, fUser.getUid(),
                    receiverId, this);

        }

        if (requestCode == REQUEST_IMAGE_CAMERA && resultCode == RESULT_OK
                && currentPhotoFromCameraPath != null) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data"); // thumbnail only
            Log.d(TAG, "uploading picture...");
            File f = new File(currentPhotoFromCameraPath);
            Uri photoUri = Uri.fromFile(f);
            ImageHelper.uploadImage(photoUri, threadId, fUser.getUid(),
                    receiverId, this);

        }
    }
}
