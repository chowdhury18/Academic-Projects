package com.g10.chit_chat.chatapp.searchuser;

import android.app.Activity;
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
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.BaseAppCompatActivity;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.firebase.FirebaseDatabaseUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchUserActivity extends BaseAppCompatActivity {

    ImageButton btnSearch;
    EditText textSearch;
    private RecyclerView searchResultView;
    SearchUserAdapter searchUserAdapter;
    List<User> mUsers = new ArrayList<>();

    DatabaseReference reference;
    Query query;

    String targetUserId;
    String targetUsername;

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "datasnapshot.val: " + dataSnapshot.getValue());
            mUsers.clear();
            if (dataSnapshot.exists()) {
                TextView username = findViewById(R.id.username);
                ImageView profileImage = findViewById(R.id.profile_image);

                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    User user = child.getValue(User.class);

                    if (!user.getId().equals(ApplicationData.getCurrentUser().getId())) {
                        mUsers.add(user);
                    }
                }

                if (mUsers.isEmpty()) {
                    Toast.makeText(SearchUserActivity.this,"User Not Found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SearchUserActivity.this,"User Not Found", Toast.LENGTH_SHORT).show();
            }
            searchUserAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Find People");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnSearch = findViewById(R.id.btn_search);
        textSearch = findViewById(R.id.text_search_user);
        searchResultView = findViewById(R.id.search_result);
        searchResultView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        searchUserAdapter = new SearchUserAdapter(SearchUserActivity.this, mUsers);
        searchResultView.setAdapter(searchUserAdapter);

        textSearch.addTextChangedListener(new TextWatcher() {
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

        textSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    String keywords = textSearch.getText().toString();
                    if (TextUtils.isEmpty(keywords)) {
                        Toast.makeText(SearchUserActivity.this,"Empty field", Toast.LENGTH_SHORT).show();
                    } else {
                        searchUser(keywords);
                    }

                    return true;
                }
                return false;
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keywords = textSearch.getText().toString();
                if (TextUtils.isEmpty(keywords)) {
                    Toast.makeText(SearchUserActivity.this,"Empty field", Toast.LENGTH_SHORT).show();
                } else {
                    searchUser(keywords);
                }

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (query != null) {
            query.removeEventListener(valueEventListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    private void searchUser(String keywords) {
        reference = FirebaseDatabaseUtil.getDatabase()
                .getReference("Users");
        reference.keepSynced(true);
        query = reference.orderByChild("username").startAt(keywords.toLowerCase()).endAt(keywords.toLowerCase() + "\uf8ff");

        query.addValueEventListener(valueEventListener);
    }
}
