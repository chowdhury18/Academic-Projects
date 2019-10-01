package com.g10.chit_chat.chatapp.gallery.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codewaves.stickyheadergrid.StickyHeaderGridLayoutManager;
import com.g10.chit_chat.chatapp.BaseAppCompatActivity;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.chat.view.ChatActivity;
import com.g10.chit_chat.chatapp.datamodel.Chat;
import com.g10.chit_chat.chatapp.datamodel.Thread;
import com.g10.chit_chat.chatapp.gallery.model.GalleryInteractor;
import com.g10.chit_chat.chatapp.gallery.presenter.GalleryPresenter;
import com.g10.chit_chat.chatapp.utils.image.ImagePropertiesGallery;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GalleryActivity extends BaseAppCompatActivity implements GalleryView {
    private String threadId;
    private GalleryPresenter galleryPresenter;
    private RecyclerView recyclerView;
    private StickyHeaderGridLayoutManager sectionedLayoutManager;
    private List<ImagePropertiesGallery> listOfImages;
    private ProgressBar progressBar;
    private TextView noImageTextView;
    private RelativeLayout blocker;
    private Boolean isViewBlocked;
    private Thread currentThreadDetails;
    private enum ViewSortedBy {
        SORTED_BY_DATE,
        SORTED_BY_SENDER_NAME,
        SORTED_BY_CATEGORY
    }
    private ViewSortedBy currentViewSortedBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        isViewBlocked = false;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_image_gallery);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycler_view_gallery);
        noImageTextView = findViewById(R.id.text_in_gallery);
        progressBar = findViewById(R.id.gallery_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);
        blocker = findViewById(R.id.layout_blocker);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            threadId = extras.getString(ChatActivity.THREAD_ID);
            Log.d(TAG, "extras: " + threadId);
        }
        //currentThreadDetails = (Thread) getIntent().getSerializableExtra(ChatActivity.THREAD_DATA);
    }

    @Override
    public void onStart() {
        galleryPresenter = new GalleryPresenter(this, new GalleryInteractor());
        blockView();
        galleryPresenter.getAllImages(threadId);

        sectionedLayoutManager = new StickyHeaderGridLayoutManager(3);
        sectionedLayoutManager.setSpanSizeLookup(new StickyHeaderGridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int section, int position) {
                return 1;
            }
        });

        super.onStart();
    }

    @Override
    public void onDestroy() {
        galleryPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onImagesReceived(List<ImagePropertiesGallery> listOfImages) {
        unblockView();
        this.listOfImages = listOfImages;
        if (listOfImages == null) {
            noImageTextView.setVisibility(View.VISIBLE);
        } else {
            noImageTextView.setVisibility(View.GONE);
            sortByDate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_in_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.gallery_sort_by_date:
                sortByDate();
                return true;
            case R.id.gallery_sort_by_sender:
                sortBySender();
                return true;
            case R.id.gallery_sort_by_tag:
                sortedByCategory();
                return true;
            case R.id.gallery_get_full_images:
                showFullImages();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortBySender() {
        if (listOfImages == null) {
            // Do nothing in this case
            return;
        }
        blockView();
        List<List<ImagePropertiesGallery>> listOfSectionedImages;
        listOfSectionedImages = new ArrayList<>();

        List<String> sectionNames = new ArrayList<>();

        int numberOfImages = listOfImages.size();
        for (int i = 0; i < numberOfImages; i++) {
            if (!sectionNames.contains(listOfImages.get(i).senderName)) {
                sectionNames.add(listOfImages.get(i).senderName);
                listOfSectionedImages.add(new ArrayList<ImagePropertiesGallery>());
            }
            int position = sectionNames.indexOf(listOfImages.get(i).senderName);
            listOfSectionedImages.get(position).add(listOfImages.get(i));
        }

        if (isViewBlocked) {
            unblockView();
        }

        recyclerView.setAdapter(new SectionedGalleryAdapter(listOfSectionedImages, sectionNames,
                this));
        recyclerView.setLayoutManager(sectionedLayoutManager);
        currentViewSortedBy = ViewSortedBy.SORTED_BY_SENDER_NAME;
    }

    private void sortByDate() {
        if (listOfImages == null) {
            // Do nothing in this case
            return;
        }
        blockView();
        List<List<ImagePropertiesGallery>> listOfSectionedImages;
        listOfSectionedImages = new ArrayList<>();

        List<String> sectionNames = new ArrayList<>();

        int numberOfImages = listOfImages.size();
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Date dateNow = new Date();
        String todayDateAsString = dateFormat.format(dateNow);
        long timestampYesterday = dateNow.getTime() - (24 * 3600 * 1000);
        String yesterdayDateAsString = dateFormat.format(timestampYesterday);
        int position = 0;
        for (int i = 0; i < numberOfImages; i++) {
            String dateAsString = dateFormat.format(new Timestamp(listOfImages.get(i).timestamp));
            if (todayDateAsString.equals(dateAsString)) {
                dateAsString = "Today";
            } else if (yesterdayDateAsString.equals(dateAsString)) {
                dateAsString = "Yesterday";
            }

            if (!sectionNames.contains(dateAsString)) {
                sectionNames.add(dateAsString);
                position = sectionNames.indexOf(dateAsString);
                listOfSectionedImages.add(new ArrayList<ImagePropertiesGallery>());
            }

            listOfSectionedImages.get(position).add(listOfImages.get(i));
        }

        if (isViewBlocked) {
            unblockView();
        }
        recyclerView.setAdapter(new SectionedGalleryAdapter(listOfSectionedImages, sectionNames,
                this));
        recyclerView.setLayoutManager(sectionedLayoutManager);
        currentViewSortedBy = ViewSortedBy.SORTED_BY_DATE;
    }

    private void showFullImages() {
        if (listOfImages == null) {
            // Do nothing in this case
            return;
        }

        blockView();
        for (ImagePropertiesGallery imageUrl : listOfImages) {
            imageUrl.imageUrl = imageUrl.imageUrl.replaceAll("(low|high)","full");
        }

        if (currentViewSortedBy == ViewSortedBy.SORTED_BY_DATE) {
            sortByDate();
        } else if (currentViewSortedBy == ViewSortedBy.SORTED_BY_SENDER_NAME) {
            sortBySender();
        } else if (currentViewSortedBy == ViewSortedBy.SORTED_BY_CATEGORY) {
            sortedByCategory();
        }
    }

    private void sortedByCategory() {
        if (listOfImages == null) {
            // Do nothing in this case
            return;
        }

        Log.d(TAG, "sortedByCategory");
        // set progressbar to visible
        blockView();
        galleryPresenter.labelImages(listOfImages, this);
    }

    @Override
    public void onFinishedLabeling(List<ImagePropertiesGallery> listOfImages) {
        this.listOfImages = listOfImages;
        List<List<ImagePropertiesGallery>> listOfSectionedImages;
        listOfSectionedImages = new ArrayList<>();

        List<String> sectionNames = new ArrayList<>();

        int numberOfImages = listOfImages.size();
        for (int i = 0; i < numberOfImages; i++) {
            if (!sectionNames.contains(listOfImages.get(i).category)) {
                sectionNames.add(listOfImages.get(i).category);
                listOfSectionedImages.add(new ArrayList<ImagePropertiesGallery>());
            }
            int position = sectionNames.indexOf(listOfImages.get(i).category);
            listOfSectionedImages.get(position).add(listOfImages.get(i));
        }

        if (isViewBlocked) {
            unblockView();
        }

        recyclerView.setAdapter(new SectionedGalleryAdapter(listOfSectionedImages, sectionNames,
                this));
        recyclerView.setLayoutManager(sectionedLayoutManager);
        currentViewSortedBy = ViewSortedBy.SORTED_BY_CATEGORY;
    }

    private void blockView() {
        isViewBlocked = true;
        progressBar.setVisibility(View.VISIBLE);
        blocker.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void unblockView() {
        isViewBlocked = false;
        progressBar.setVisibility(View.INVISIBLE);
        blocker.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}

