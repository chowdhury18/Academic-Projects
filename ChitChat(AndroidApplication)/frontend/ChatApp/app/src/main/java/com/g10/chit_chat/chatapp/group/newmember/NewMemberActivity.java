package com.g10.chit_chat.chatapp.group.newmember;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.BaseAppCompatActivity;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.chat.view.ChatActivity;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.datamodel.Thread;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.g10.chit_chat.chatapp.group.groupmembers.GroupMembersActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NewMemberActivity extends BaseAppCompatActivity {

    EditText textSearchUser;
    ImageButton buttonSearch;
    RecyclerView newMembersView;
    NewMemberAdapter newMemberAdapter;
    Query query;
    Thread threadData;

    List<User> newMembers = new ArrayList<>();

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "datasnapshot.val: " + dataSnapshot.getValue());

            newMembers.clear();
            if (dataSnapshot.exists()) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    User user = child.getValue(User.class);

                    if (!user.getId().equals(ApplicationData.getCurrentUser().getId())) {
                        newMembers.add(user);
                    }
                }
                if (newMembers.isEmpty()) {
                    Toast.makeText(NewMemberActivity.this,"User Not Found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(NewMemberActivity.this,"User Not Found", Toast.LENGTH_SHORT).show();
            }
            newMemberAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_member);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_add_new_member);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textSearchUser = findViewById(R.id.text_search_user);
        buttonSearch = findViewById(R.id.btn_search);
        newMembersView = findViewById(R.id.new_members_view);

        Intent intent = getIntent();
        threadData = (Thread) intent.getSerializableExtra(ChatActivity.THREAD_DATA);

        newMembersView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        newMemberAdapter = new NewMemberAdapter(NewMemberActivity.this, newMembers, threadData);
        newMembersView.setAdapter(newMemberAdapter);

        textSearchUser.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String username = textSearchUser.getText().toString();

                    if (TextUtils.isEmpty(username)) {
                        Toast.makeText(NewMemberActivity.this,"Empty field", Toast.LENGTH_SHORT).show();
                    } else {
                        searchUser(username);
                    }

                    return true;
                }
                return false;
            }
        });

        textSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 3) {
                    searchUser(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = textSearchUser.getText().toString();

                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(NewMemberActivity.this,"Empty field", Toast.LENGTH_SHORT).show();
                } else {
                    searchUser(username);
                }

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(NewMemberActivity.this, GroupMembersActivity.class);
        intent.putExtra(ChatActivity.THREAD_DATA, threadData);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        // remove listener to user database
        if (query != null) {
            query.removeEventListener(valueEventListener);
        }
    }

    private void searchUser(String keywords) {
        DatabaseReference reference = FirebaseDatabaseUtil.getDatabase()
                .getReference("Users");
        reference.keepSynced(true);
        query = reference.orderByChild("username").startAt(keywords.toLowerCase()).endAt(keywords.toLowerCase() + "\uf8ff");

        query.addListenerForSingleValueEvent(valueEventListener);
    }
}
