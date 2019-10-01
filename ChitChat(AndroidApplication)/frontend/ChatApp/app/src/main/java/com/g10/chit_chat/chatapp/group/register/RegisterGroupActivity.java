package com.g10.chit_chat.chatapp.group.register;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.BaseAppCompatActivity;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.datamodel.Thread;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.StringUtils;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.g10.chit_chat.chatapp.group.GroupMemberAdapter;
import com.g10.chit_chat.chatapp.group.addmembers.AddGroupMembersActivity;
import com.g10.chit_chat.chatapp.main.view.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterGroupActivity extends BaseAppCompatActivity {
    EditText textGroupSubject;
    TextView txtNumOfMembers;
    private RecyclerView recyclerView;
    private GroupMemberAdapter groupUserAdapter;
    private List<User> selectedUsers = new ArrayList<>();

    private HashMap<String, User> groupMemberIdUsernameMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_group);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_register_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        groupMemberIdUsernameMap = (HashMap) intent.getSerializableExtra(AddGroupMembersActivity.GROUP_MEMBERS);

        selectedUsers.clear();
        selectedUsers.addAll(groupMemberIdUsernameMap.values());

        recyclerView = findViewById(R.id.members_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        groupUserAdapter = new GroupMemberAdapter(RegisterGroupActivity.this, selectedUsers);
        recyclerView.setAdapter(groupUserAdapter);

        textGroupSubject = findViewById(R.id.group_name);
        txtNumOfMembers = findViewById(R.id.num_of_members);
        txtNumOfMembers.setText(groupMemberIdUsernameMap.size() + " members");

        textGroupSubject.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    String groupSubject = textGroupSubject.getText().toString();

                    if (TextUtils.isEmpty(groupSubject)) {
                        Toast.makeText(RegisterGroupActivity.this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                    } else if (groupMemberIdUsernameMap.size() == 1) {
                        Toast.makeText(RegisterGroupActivity.this, "Please insert another member", Toast.LENGTH_SHORT).show();
                    } else {
                        registerGroup(groupSubject);
                    }

                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_check_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_check:
                String groupSubject = textGroupSubject.getText().toString();

                if (TextUtils.isEmpty(groupSubject)) {
                    Toast.makeText(RegisterGroupActivity.this, "Group name cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (groupMemberIdUsernameMap.size() == 1) {
                    Toast.makeText(RegisterGroupActivity.this, "Please insert another member", Toast.LENGTH_SHORT).show();
                } else {
                    registerGroup(groupSubject);
                }
                return true;

            default:
                Intent intent = new Intent(RegisterGroupActivity.this, AddGroupMembersActivity.class);
                intent.putExtra(AddGroupMembersActivity.GROUP_MEMBERS, (HashMap) groupMemberIdUsernameMap);
                startActivity(intent);
                finish();
                return true;
        }
    }

    private void registerGroup(final String groupSubject) {
        Log.d(TAG, "group name: " + groupSubject);

        for (String memberId : groupMemberIdUsernameMap.keySet()) {
            Log.d(TAG, "member: " + memberId);
        }

        // store members into users child of Threads Table
        DatabaseReference detailsReference = FirebaseDatabaseUtil.getDatabase()
                .getReference("Details");
        final String threadId = detailsReference.push().getKey();
        Log.d(TAG, "key: "+threadId);

        Map<String, Object> threadUpdates = new HashMap<>();
        for (Map.Entry<String, User> entry: groupMemberIdUsernameMap.entrySet()) {
            threadUpdates.put("/" + threadId + "/details/" + "users/" + entry.getKey() + "/joinTime", ServerValue.TIMESTAMP);
            threadUpdates.put("/" + threadId + "/details/" + "users/" + entry.getKey() + "/username", entry.getValue().getUsername());
            threadUpdates.put("/" + threadId + "/details/" + "users/" + entry.getKey() + "/imageURL", entry.getValue().getImageURL());
        }
        threadUpdates.put("/" + threadId + "/details/" + "type", Thread.GROUP_CHAT);
        threadUpdates.put("/" + threadId + "/details/" + "name", groupSubject);
        threadUpdates.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_FIELD_NAME, StringUtils.capitalize(ApplicationData.getCurrentUser().getUsername()) + " just created the group " + groupSubject);
        threadUpdates.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_SENDER_FIELD_NAME, ApplicationData.getCurrentUser().getUsername());
        threadUpdates.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_TIMESTAMP_FIELD_NAME, ServerValue.TIMESTAMP);
        threadUpdates.put("/" + threadId + "/details/" + Thread.LAST_MESSAGE_IS_STATUS_MESSAGE_FIELD_NAME, 1);
        threadUpdates.put("/" + threadId + "/details/" + "creationDate", ServerValue.TIMESTAMP);
        threadUpdates.put("/" + threadId + "/details/" + "id", threadId);

        detailsReference
                .updateChildren(threadUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        DatabaseReference threadReference = FirebaseDatabaseUtil
                                .getDatabase()
                                .getReference("Threads");

                        // set sender and receiver in the Threads database
                        Map<String, Object> clientMap = new HashMap<>();
                        for (Map.Entry<String, User> entry: groupMemberIdUsernameMap.entrySet()) {
                            clientMap.put("/" + threadId + "/users/" + entry.getKey(), ServerValue.TIMESTAMP);
                        }

                        threadReference
                                .updateChildren(clientMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        DatabaseReference usersReference = FirebaseDatabaseUtil.getDatabase()
                                                .getReference("Users");

                                        // store group name into threads child of Users Table
                                        Map<String, Object> childUpdates = new HashMap<>();
                                        for (String memberId : groupMemberIdUsernameMap.keySet()) {
                                            childUpdates.put("/" + memberId + "/threads/" + threadId, ServerValue.TIMESTAMP);
                                        }
                                        usersReference
                                                .updateChildren(childUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Intent intent = new Intent(RegisterGroupActivity.this, MainActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                        });
                                    }
                                });
                    }
                });
    }
}
