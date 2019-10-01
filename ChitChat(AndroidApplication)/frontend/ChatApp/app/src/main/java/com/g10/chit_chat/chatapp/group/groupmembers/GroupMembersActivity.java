package com.g10.chit_chat.chatapp.group.groupmembers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.g10.chit_chat.chatapp.BaseAppCompatActivity;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.chat.view.ChatActivity;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.datamodel.Thread;
import com.g10.chit_chat.chatapp.datamodel.UserChatInfo;
import com.g10.chit_chat.chatapp.group.GroupMemberAdapter;
import com.g10.chit_chat.chatapp.group.newmember.NewMemberActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupMembersActivity extends BaseAppCompatActivity {
    TextView textGroupName;
    TextView txtNumOfMembers;
    private RecyclerView recyclerView;
    private GroupMemberAdapter groupUserAdapter;
    private List<User> selectedUsers = new ArrayList<>();
    private Thread currentThreadData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_group_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        currentThreadData = (Thread) intent.getSerializableExtra(ChatActivity.THREAD_DATA);

        textGroupName = findViewById(R.id.group_name);
        txtNumOfMembers = findViewById(R.id.group_num_of_members);
        recyclerView = findViewById(R.id.members_list);
    }

    @Override
    public void onStart() {
        selectedUsers.clear();
        for (Map.Entry<String, UserChatInfo> entry : currentThreadData.getUsers().entrySet()) {
            User user = new User();
            user.setId(entry.getKey());
            user.setUsername(entry.getValue().getUsername());
            user.setImageURL(entry.getValue().getImageURL());
            selectedUsers.add(user);
        }

        textGroupName.setText(currentThreadData.getName());
        txtNumOfMembers.setText(selectedUsers.size() + " members");

        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        groupUserAdapter = new GroupMemberAdapter(GroupMembersActivity.this, selectedUsers);
        recyclerView.setAdapter(groupUserAdapter);

        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_members, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_add_new_members:
                Intent newMemberIntent = new Intent(GroupMembersActivity.this, NewMemberActivity.class);
                newMemberIntent.putExtra(ChatActivity.THREAD_DATA, currentThreadData);
                startActivity(newMemberIntent);
                finish();
                return true;

            default:
                return true;
        }
    }
}
