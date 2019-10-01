package com.g10.chit_chat.chatapp.chat.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.g10.chit_chat.chatapp.ApplicationData;
import com.g10.chit_chat.chatapp.BaseRecyclerViewAdapter;
import com.g10.chit_chat.chatapp.R;
import com.g10.chit_chat.chatapp.datamodel.Chat;
import com.g10.chit_chat.chatapp.datamodel.Thread;
import com.g10.chit_chat.chatapp.datamodel.User;
import com.g10.chit_chat.chatapp.datamodel.UserChatInfo;
import com.g10.chit_chat.chatapp.utils.StringUtils;
import com.g10.chit_chat.chatapp.utils.firebase.GlideApp;
import com.g10.chit_chat.chatapp.utils.image.ImageHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class MessageAdapter extends BaseRecyclerViewAdapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context mContext;
    private List<Chat> mChats;
    private String imageurl;
    private FirebaseUser fuser;
    private Thread threadData;

    public MessageAdapter(Context mContext, List<Chat> mChats, String imageurl, Thread threadData) {
        this.mContext = mContext;
        this.mChats = mChats;
        this.imageurl = imageurl;
        this.threadData = threadData;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, viewGroup, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, viewGroup, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder viewHolder, int i) {
        Chat chat = mChats.get(i);

        UserChatInfo userChatInfo = threadData.getUsers().get(mChats.get(i).getSender());

        if (viewHolder.username != null) {
            Log.e(TAG, mChats.get(i).getSender());
            String username = "Deleted User";
            if (userChatInfo != null) {
                username = userChatInfo.getUsername();
            }
            StringUtils.setUsername(viewHolder.username, username);
            if (Thread.SINGLE_CHAT == threadData.getType()) {
                viewHolder.username.setVisibility(View.GONE);
            } else {
                if (i > 0 && mChats.get(i).getSender().equals(mChats.get(i-1).getSender())) {
                    viewHolder.username.setVisibility(View.GONE);
                } else {
                    viewHolder.username.setVisibility(View.VISIBLE);
                }
            }
        }

        if (viewHolder.profileImage != null) {
            if (Thread.SINGLE_CHAT == threadData.getType()) {
                viewHolder.profileImage.setVisibility(View.GONE);
            } else {
                if (i > 0 && mChats.get(i).getSender().equals(mChats.get(i-1).getSender())) {
                    viewHolder.profileImage.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.profileImage.setVisibility(View.VISIBLE);
                    String senderImageUrl = "default";
                    if (userChatInfo != null) {
                        senderImageUrl = threadData.getUsers()
                                .get(mChats.get(i).getSender()).getImageURL();
                    }
                    ImageHelper.applyProfileImageValue(senderImageUrl, viewHolder.profileImage, mContext);
                }
            }
        }

        if (chat.getMessage() != null && !chat.getMessage().isEmpty()) {
            viewHolder.showMessage.setVisibility(View.VISIBLE);
            viewHolder.imageMessage.setVisibility(View.GONE);
            viewHolder.showMessage.setText(chat.getMessage());
        } else {
            viewHolder.showMessage.setVisibility(View.GONE);
            viewHolder.imageMessage.setVisibility(View.VISIBLE);
            String currentImageResSetting = ApplicationData
                    .getCurrentUser()
                    .getImageResolutionSetting().name();
            if (currentImageResSetting.equals(User.ImageResolutionOptions.FULL.name())) {
                showImage(chat.getImageUrlFull(), viewHolder);
            } else if (currentImageResSetting.equals(User.ImageResolutionOptions.HIGH.name())) {
                showImage(chat.getImageUrlHigh(), viewHolder);
            } else if (currentImageResSetting.equals(User.ImageResolutionOptions.LOW.name())) {
                showImage(chat.getImageUrlLow(), viewHolder);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView showMessage;
        public TextView username;
        public ImageView profileImage;
        public ImageView imageMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            showMessage = itemView.findViewById(R.id.show_message);
            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profile_image);
            imageMessage = itemView.findViewById(R.id.image_message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChats.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public void showImage(String imageUrl, final MessageAdapter.ViewHolder holder) {
        if (imageUrl.startsWith("gs://")) {
            Log.d(TAG, "imageUrl: " + imageUrl);
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReferenceFromUrl(imageUrl);
            GlideApp.with(holder.imageMessage.getContext())
                    .load(storageReference)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(ImageHelper.createLoadingImage(mContext))
                    .into(holder.imageMessage);

            storageReference.getDownloadUrl()
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                downloadImageToGallery(task.getResult().toString());
                            } else {
                                Log.e(TAG, "cannot download image to gallery");
                            }
                        }
                    });
        } else if (imageUrl.startsWith("http")) {
            holder.imageMessage.setImageDrawable(ImageHelper.createLoadingImage(mContext));
            downloadImageToGallery(imageUrl);
        }
    }

    private void downloadImageToGallery(final String downloadImageUrl) {
        Picasso.get()
                .load(downloadImageUrl)
                .into(new Target() {
                          @Override
                          public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                              try {
                                  String root = Environment
                                          .getExternalStorageDirectory()
                                          .toString();
                                  File myDir = new File(root + "/chit_chat");

                                  Log.d(TAG, "myDir: " + myDir);

                                  if (!myDir.exists()) {
                                      myDir.mkdirs();
                                  }

                                  Log.d(TAG, "imageUrl: " + downloadImageUrl);

                                  String name = URLUtil.guessFileName(downloadImageUrl,
                                          null, null);

                                  Log.d(TAG, "filename: " + name);

                                  myDir = new File(myDir, name);

                                  FileOutputStream out = new FileOutputStream(myDir);
                                  bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                                  Log.d(TAG, "location: " + bitmap.toString());

                                  out.flush();
                                  out.close();
                              } catch(Exception e){

                                  Log.e(TAG, "error download: " + e.getMessage());
                              }
                          }

                          @Override
                          public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                          }

                          @Override
                          public void onPrepareLoad(Drawable placeHolderDrawable) {
                          }
                      }
                );
    }
}
