package com.g10.chit_chat.chatapp.main.view;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.BaseAppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.utils.StringUtils;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.g10.chit_chat.chatapp.chat.view.ChatActivity;
import com.g10.chit_chat.chatapp.datamodel.Thread;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.utils.firebase.FCMService;
import com.g10.chit_chat.chatapp.login.view.LoginActivity;
import com.g10.chit_chat.chatapp.group.addmembers.AddGroupMembersActivity;
import com.g10.chit_chat.chatapp.searchuser.SearchUserActivity;
import com.g10.chit_chat.chatapp.settings.view.SettingActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends BaseAppCompatActivity implements MainView {

    private DrawerLayout mDrawerLayout;

    private RecyclerView recyclerView;
    private ThreadAdapter threadAdapter;
    private List<Thread> mThreads;
    Map<String, Object> data;

    private View navHeader;
    ImageView profileImage;
    TextView username;
    TextView txtEmail;
    private Long imageModificationTime;
    private NavigationView navigationView;
    public static final int FOUND_USER_FROM_SEARCHING = 0;
    private ApplicationData.UserThreadsUpdateSubscriber userThreadsUpdateSubscriber;
    private ApplicationData.UserDataUpdateSubscriber userDataUpdateSubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate called!");

        ImageHelper.getPermissionDownloadImage(this);

        // video 3
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24px);

        mDrawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);

        // navigation header
        navHeader = navigationView.getHeaderView(0);
        username = (TextView) navHeader.findViewById(R.id.username);
        profileImage = navHeader.findViewById(R.id.profile_image);
        txtEmail = navHeader.findViewById(R.id.email);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        if (menuItem.getItemId() == R.id.logout) {
                            ApplicationData.removeCurrentUser();
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(MainActivity.this,
                                    LoginActivity.class));
                            finish();
                            return true;
                        } else if (menuItem.getItemId() == R.id.settings) {
                            Log.d(TAG, "settings clicked");
                            startActivity(new Intent(MainActivity.this,
                                    SettingActivity.class));
                        } else {
                            Log.d(TAG, "new group clicked");
                            startActivity(new Intent(MainActivity.this,
                                    AddGroupMembersActivity.class));
                        }

                        // set item as selected to persist highlight
                        menuItem.setChecked(true);

                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

        mThreads = new ArrayList<>();

        threadAdapter = new ThreadAdapter(MainActivity.this, mThreads);
        recyclerView.setAdapter(null);
        recyclerView.setAdapter(threadAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "Main Activity stopped");
    }

    private void readThread() {
        Log.d(TAG, "reading Thread");
        mThreads.clear();
        for (Thread threadDetail : ApplicationData.getUserThreadDetails()) {
            if (Thread.GROUP_CHAT == threadDetail.getType() ||
                    threadDetail.getLastMessageTimestamp() != null) {
                mThreads.add(threadDetail);
            }
        }
        threadAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FOUND_USER_FROM_SEARCHING) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.THREAD_DATA, data.getStringExtra(ChatActivity.THREAD_DATA));
                intent.putExtra(ChatActivity.THREAD_ID, data.getStringExtra(ChatActivity.THREAD_ID));
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.action_search:
                startActivityForResult(new Intent(MainActivity.this, SearchUserActivity.class), FOUND_USER_FROM_SEARCHING);
                return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationData.removeUserThreadsUpdateSubscribers(userThreadsUpdateSubscriber);
        userThreadsUpdateSubscriber = null;

        ApplicationData.removeUserDataUpdateSubscribers(userDataUpdateSubscriber);
        userDataUpdateSubscriber = null;
    }

    @Override
    public void onStart() {
        if (ApplicationData.getCurrentUser() == null) {
            ApplicationData.initCurrentUser(new ApplicationData.InitCurrentUserCallback() {
                @Override
                public void onAfterInitCurrentUser(User currentUser) {
                    FCMService.updateToken();
                    applyUserData(currentUser);
                    readThread();

                    initIfNeededUserThreadsUpdateSubscriber();
                    initIfNeededUserDataUpdateSubscriber();
                }
            });
        } else {
            // Always update main activity with currentUser data to reflect all updates
            applyUserData(ApplicationData.getCurrentUser());
            readThread();

            initIfNeededUserThreadsUpdateSubscriber();
            initIfNeededUserDataUpdateSubscriber();
        }

        super.onStart();
    }

    private void initIfNeededUserDataUpdateSubscriber() {
        if (userDataUpdateSubscriber == null) {
            userDataUpdateSubscriber = new ApplicationData.UserDataUpdateSubscriber() {

                @Override
                public void onAfterUserDataUpdate(User currentUser) {
                    Log.d(TAG, "Update on currentUser data");
                    applyUserData(currentUser);
                }
            };
            ApplicationData.addUserDataUpdateSubscribers(userDataUpdateSubscriber);
        }
    }

    private void initIfNeededUserThreadsUpdateSubscriber() {
        if (userThreadsUpdateSubscriber == null) {
            userThreadsUpdateSubscriber = new ApplicationData.UserThreadsUpdateSubscriber() {

                @Override
                public void onAfterUserThreadsUpdate(List<Thread> autoSortedThreads) {
                    Log.d(TAG, "Update on thread data");
                    readThread();
                }
            };
            ApplicationData.addUserThreadsUpdateSubscribers(userThreadsUpdateSubscriber);
        }
    }

    private void applyUserData(User currentUser) {
        ImageHelper.applyProfileImageValue(currentUser.getImageURL(), profileImage, MainActivity.this);
        StringUtils.setUsername(username, currentUser.getUsername());
        txtEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

}
