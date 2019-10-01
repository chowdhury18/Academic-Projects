package com.g10.chit_chat.chatapp.gallery.view;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.codewaves.stickyheadergrid.StickyHeaderGridAdapter;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.utils.firebase.GlideApp;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.g10.chit_chat.chatapp.utils.image.ImagePropertiesGallery;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class SectionedGalleryAdapter extends StickyHeaderGridAdapter {
    private Context context;
    private List<List<ImagePropertiesGallery>> listOfSectionedImages;
    private List<String> sectionNames;

    public SectionedGalleryAdapter(List<List<ImagePropertiesGallery>> listOfSectionedImages,
                                   List<String> sectionNames,
                                   Context context) {
        this.listOfSectionedImages = listOfSectionedImages;
        this.context = context;
        this.sectionNames = sectionNames;
    }

    @Override
    public int getSectionCount() {
        return listOfSectionedImages.size();
    }

    @Override
    public int getSectionItemCount(int section) {
        return listOfSectionedImages.get(section).size();
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_section_gallery,
                        parent, false);
        return new HeaderSectionViewHolder(view);
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_gallery,
                parent, false);
        return new GalleryItemViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int section) {
        Log.d("CHIT_CHAT", "onBindHeaderviewholder");
        final HeaderSectionViewHolder holder = (HeaderSectionViewHolder)viewHolder;
        holder.labelView.setText(sectionNames.get(section));
    }

    @Override
    public void onBindItemViewHolder(ItemViewHolder viewHolder, int section, int offset) {
        final GalleryItemViewHolder holder = (GalleryItemViewHolder) viewHolder;
        final String imageUrl = listOfSectionedImages.get(section).get(offset).imageUrl;

        Log.d("CHIT_CHAT", "imgurl: " + imageUrl);
        if (imageUrl.startsWith("gs://")) {
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(imageUrl);

            GlideApp
                    .with(holder.imageView.getContext())
                    .load(storageReference)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(ImageHelper.createLoadingImage(context))
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageDrawable(ImageHelper.createLoadingImage(context));
        }

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(holder.imageView.getContext(), imageUrl, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(context, GalleryFullScreen.class);
                String strName = imageUrl;
                i.putExtra("IMAGE_URL", strName);
                context.startActivity(i);
            }
        });
    }

    private static class HeaderSectionViewHolder extends HeaderViewHolder {
        TextView labelView;

        HeaderSectionViewHolder(View itemView) {
            super(itemView);
            labelView = (TextView) itemView.findViewById(R.id.section_title);
        }
    }

    private static class GalleryItemViewHolder extends ItemViewHolder {
        ImageView imageView;
        GalleryItemViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pictures_in_gallery);
        }
    }
}
