package com.g10.chit_chat.chatapp.gallery.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.utils.firebase.GlideApp;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.g10.chit_chat.chatapp.utils.image.TouchImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.security.MessageDigest;

public class FullScreenImageAdapter extends PagerAdapter {
    private Activity activity;
    private String imageUrl;
    private LayoutInflater inflater;
    private float rotation;

    public FullScreenImageAdapter(Activity activity,
                                  String imageUrl) {
        this.activity = activity;
        this.imageUrl = imageUrl;
        rotation = 0;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == ((RelativeLayout) o);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageButton btnClose;

        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_gallery, container,
                false);

        final TouchImageView imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);

        btnClose = viewLayout.findViewById(R.id.btnClose);

        if (imageUrl.startsWith("gs://")) {
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(imageUrl);

            GlideApp
                    .with(imgDisplay.getContext())
                    .load(storageReference)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(ImageHelper.createLoadingImage(activity))
                    .into(imgDisplay);
        } else {
            imgDisplay.setImageDrawable(ImageHelper.createLoadingImage(activity));
        }

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });

        ImageButton btnRotate = viewLayout.findViewById(R.id.btnRotate);
        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotation += 90.0;
                if (rotation > 270) {
                    rotation = 0;
                }

                if (imageUrl.startsWith("gs://")) {
                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(imageUrl);

                    GlideApp
                            .with(imgDisplay.getContext())
                            .load(storageReference)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(ImageHelper.createLoadingImage(activity))
                            .into(imgDisplay);
                } else {
                    imgDisplay.setImageDrawable(ImageHelper.createLoadingImage(activity));
                }

                if (imageUrl.startsWith("gs://")) {
                    GlideApp
                            .with(imgDisplay.getContext())
                            .load(FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl))
                            .placeholder(ImageHelper.createLoadingImage(activity))
                            .apply(new RequestOptions()
                                    .fitCenter()
                                    .transform(new RotateTransformation(imgDisplay.getContext(),
                                            rotation)))
                            .into(imgDisplay);
                } else {
                    imgDisplay.setImageDrawable(ImageHelper.createLoadingImage(activity));
                }

            }
        });

        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }

    public class RotateTransformation extends BitmapTransformation {

        private float rotateRotationAngle = 0f;

        public RotateTransformation(Context context, float rotateRotationAngle) {
            //super(context);

            this.rotateRotationAngle = rotateRotationAngle;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Matrix matrix = new Matrix();

            matrix.postRotate(rotateRotationAngle);

            return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update(("rotate" + rotateRotationAngle).getBytes());
        }
    }
}
