package com.g10.chit_chat.chatapp.gallery.view;

import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.g10.chit_chat.chatapp.R;

public class GalleryFullScreen extends AppCompatActivity {
    private ViewPager viewPager;
    private FullScreenImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_full_screen);

        Bundle extras = getIntent().getExtras();
        String imageUrl = extras.getString("IMAGE_URL");
        viewPager = findViewById(R.id.pager);
        adapter = new FullScreenImageAdapter(this, imageUrl);
        viewPager.setAdapter(adapter);

//        ConstraintLayout layout = findViewById(R.id.layout_gallery_full_screen);
//        layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//    }
//
//    @Override
//    protected void onStart() {
        super.onStart();
        int pos = getIntent().getIntExtra("position", 0);
        viewPager.setCurrentItem(pos);
    }
}
