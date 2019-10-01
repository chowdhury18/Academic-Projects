package com.g10.chit_chat.chatapp.group.addmembers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
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
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.g10.chit_chat.chatapp.group.register.RegisterGroupActivity;
import com.g10.chit_chat.chatapp.main.view.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddGroupMembersActivity extends BaseAppCompatActivity {

    public interface OnDataChangeListener{
        public void onDataChanged(HashMap<String, User> groupMemberIdUsernameMap);
    }

    public static final String GROUP_MEMBERS = "groupMembers";
    EditText textSearchUser;
    TextView numOfSelectedMembers;
    ImageButton buttonSearch;
    RecyclerView usersToAddView;
    UserToAddAdapter userToAddAdapter;
    Query query;
    private Menu menu;

    List<User> usersToAdd = new ArrayList<>();

    private HashMap<String, User> groupMemberIdUsernameMap = new HashMap<>();

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "datasnapshot.val: " + dataSnapshot.getValue());

            usersToAdd.clear();
            if (dataSnapshot.exists()) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    User user = child.getValue(User.class);

                    if (!user.getId().equals(ApplicationData.getCurrentUser().getId())) {
                        usersToAdd.add(user);
                    }
                }
                if (usersToAdd.isEmpty()) {
                    Toast.makeText(AddGroupMembersActivity.this,"User Not Found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AddGroupMembersActivity.this,"User Not Found", Toast.LENGTH_SHORT).show();
            }
            userToAddAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_members);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_register_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textSearchUser = findViewById(R.id.text_search_user);
        buttonSearch = findViewById(R.id.btn_search);
        usersToAddView = findViewById(R.id.users_to_add_view);
        numOfSelectedMembers = findViewById(R.id.num_of_selected_members);

        Intent intent = getIntent();
        HashMap<String, User> groupMembers = (HashMap) intent.getSerializableExtra(AddGroupMembersActivity.GROUP_MEMBERS);
        if (groupMembers == null) {
            groupMembers = new HashMap<>();
            groupMembers.put(ApplicationData.getCurrentUser().getId(), ApplicationData.getCurrentUser());
        }
        updateSelectedGroupMembers(groupMembers);

        usersToAddView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        userToAddAdapter = new UserToAddAdapter(AddGroupMembersActivity.this, usersToAdd, groupMemberIdUsernameMap) {

            @Override
            public void onDataChanged(HashMap<String, User> groupMemberIdUsernameMap) {
                updateSelectedGroupMembers(groupMemberIdUsernameMap);
            }
        };
        usersToAddView.setAdapter(userToAddAdapter);

        textSearchUser.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String username = textSearchUser.getText().toString();

                    if (TextUtils.isEmpty(username)) {
                        Toast.makeText(AddGroupMembersActivity.this,"Empty field", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AddGroupMembersActivity.this,"Empty field", Toast.LENGTH_SHORT).show();
                } else {
                    searchUser(username);
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void updateSelectedGroupMembers(HashMap<String, User> groupMemberIdUsernameMap) {
        this.groupMemberIdUsernameMap = groupMemberIdUsernameMap;
        int size = AddGroupMembersActivity.this.groupMemberIdUsernameMap.size();
        if (size > 1) {
            numOfSelectedMembers.setText((size - 1) + " members selected");
        } else {
            numOfSelectedMembers.setText("Select members");
        }
        this.invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_check_menu, menu);
        this.menu = menu;

        int size = AddGroupMembersActivity.this.groupMemberIdUsernameMap.size();
        if (size > 1) {
            this.menu.findItem(R.id.action_check).setVisible(true);
        } else {
            this.menu.findItem(R.id.action_check).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_check:
                Intent intent = new Intent(AddGroupMembersActivity.this, RegisterGroupActivity.class);
                intent.putExtra(GROUP_MEMBERS, (HashMap) groupMemberIdUsernameMap);
                startActivity(intent);
                finish();
                return true;
//            default:
//                startActivity(new Intent(AddGroupMembersActivity.this, MainActivity.class));
//                finish();
//                return true;
        }
        return false;
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
