package com.g10.chit_chat.chatapp.gallery.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.g10.chit_chat.chatapp.BaseRecyclerViewAdapter;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.utils.firebase.GlideApp;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.g10.chit_chat.chatapp.utils.image.ImagePropertiesGallery;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class PlainGalleryAdapter extends BaseRecyclerViewAdapter<PlainGalleryAdapter.ItemViewHolder> {
    private Context context;
    private List<ImagePropertiesGallery> listOfImages;

    public PlainGalleryAdapter(List<ImagePropertiesGallery> listOfImages, Context context) {
        this.context = context;
        this.listOfImages = listOfImages;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View view = LayoutInflater.from(context)
                .inflate(R.layout.item_in_gallery,
                viewGroup, false);
        return new PlainGalleryAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder itemViewHolder, int i) {
        final PlainGalleryAdapter.ItemViewHolder holder =
                (PlainGalleryAdapter.ItemViewHolder) itemViewHolder;
        //final String imageUrl = listOfSectionedImages.get(section).get(offset);
        final String imageUrl = listOfImages.get(i).imageUrl;
        //Log.d(TAG, "imageUrl adapter: " + imageUrl);

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
                Intent i = new Intent(context, GalleryFullScreen.class);
                String strName = imageUrl;
                i.putExtra("IMAGE_URL", strName);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOfImages.size();
    }

    protected class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pictures_in_gallery);
        }
    }
}
