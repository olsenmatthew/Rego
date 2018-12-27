package com.reylo.rego.Main.Matches.Chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.reylo.rego.Utils.ImageManager;
import com.reylo.rego.Utils.InternalStorage;
import com.reylo.rego.R;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.devbrackets.android.exomedia.core.video.scale.ScaleType.*;


public class ChatAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    private List<ChatObject> chatList;
    private Context context;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private MediaPlayer thisAudioMessagePlayer = new MediaPlayer();
    private StorageReference storageReference;
    private StorageReference firebaseStorage;

    public ChatAdapter(List<ChatObject> chatList, Context context){
        this.chatList = chatList;
        this.context = context;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_messages_view_holder, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);
        ChatViewHolder chatRCV  = new ChatViewHolder(layoutView);
        return chatRCV;

    }

    @Override
    public void onBindViewHolder(final ChatViewHolder holder, int position) {

        holder.messageContainer.setVisibility(View.GONE);

        String audioTrackerDefault = "00:01";

        String bubbleTimestamp = createChatBubbleTimestamp(position);

        if (chatList.get(position).getThisUserTheCreator() != null) {

            //If This user
            if (chatList.get(position).getThisUserTheCreator()) {

                if (isTextMessage(position)) {

                    showMyTextMessage(holder, position, bubbleTimestamp);

                } else if (isAudioMessage(position)) {

                    showMyAudioMessage(holder, position, bubbleTimestamp, audioTrackerDefault);

                } else if (isCameraContentMessage(position)) {

                    showMyCameraContentMessages(holder, position, bubbleTimestamp, audioTrackerDefault);

                } else if (isVideoMessage(position)) {

                    try {
                        showMyVideoMessages(holder, position, bubbleTimestamp, audioTrackerDefault);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else if (isAttachmentMessage(position)) {

                    showMyAttachmentMessages(holder, position, bubbleTimestamp, audioTrackerDefault);

                } else if (isContactMessage(position)) {

                    showMyContactMessages(holder, position, bubbleTimestamp, audioTrackerDefault);

                }

            } else if (!chatList.get(position).getThisUserTheCreator()) {

                if (isTextMessage(position)) {

                    showOtherTextMessage(holder, position, bubbleTimestamp);

                } else if (isAudioMessage(position)) {

                    showOtherAudioMessage(holder, position, bubbleTimestamp, audioTrackerDefault);

                } else if (isCameraContentMessage(position)) {

                    showOtherCameraContentMessages(holder, position, bubbleTimestamp, audioTrackerDefault);

                } else if (isVideoMessage(position)) {

                    showOtherVideoMessages(holder, position, bubbleTimestamp, audioTrackerDefault);

                } else if (isAttachmentMessage(position)) {

                    showOtherAttachmentMessages(holder, position, bubbleTimestamp, audioTrackerDefault);

                } else if (isContactMessage(position)) {

                    showOtherContactMessages(holder, position, bubbleTimestamp, audioTrackerDefault);

                }

            }

            holder.messageContainer.setVisibility(View.VISIBLE);

        }

    }

    public static class CircleTransform extends BitmapTransformation {
        public CircleTransform(Context context) {
            super(context);
        }

        @Override protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {

            return circleCrop(pool, toTransform);

        }

        private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {

            if (source == null) return null;

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            // TODO this could be acquired from the pool too
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {

                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override public String getId() {
            return getClass().getName();
        }

    }

    @Override
    public int getItemCount() {
        return this.chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private boolean isTextMessage(int position) {

        if (chatList.get(position).getMessageText() != null && chatList.get(position).getAudioRecordingUrl() == null
                && chatList.get(position).getMessageCameraContent() == null && chatList.get(position).getMessageAttachmentContent() == null
                && chatList.get(position).getContactMessageName() == null && chatList.get(position).getContactMessagePhoneNumber() == null
                && chatList.get(position).getContactMessageProfilePhoto() == null) {

            return true;

        } else {

            return false;

        }

    }

    private boolean isAudioMessage(int position) {

        if (chatList.get(position).getMessageText() == null && chatList.get(position).getAudioRecordingUrl() != null
                && chatList.get(position).getMessageCameraContent() == null && chatList.get(position).getMessageAttachmentContent() == null
                && chatList.get(position).getContactMessageName() == null && chatList.get(position).getContactMessagePhoneNumber() == null
                && chatList.get(position).getContactMessageProfilePhoto() == null) {

            return true;

        } else {

            return false;

        }

    }

    private boolean isCameraContentMessage(int position) {

        if (chatList.get(position).getMessageText() == null && chatList.get(position).getAudioRecordingUrl() == null
                && chatList.get(position).getMessageCameraContent() != null && chatList.get(position).getMessageAttachmentContent() == null
                && chatList.get(position).getContactMessageName() == null && chatList.get(position).getContactMessagePhoneNumber() == null
                && chatList.get(position).getContactMessageProfilePhoto() == null) {

            return true;

        } else {

            return false;

        }

    }

    private boolean isVideoMessage(int position) {

        if (chatList.get(position).getMessageText() == null && chatList.get(position).getAudioRecordingUrl() == null
                && chatList.get(position).getMessageCameraContent() == null && chatList.get(position).getMessageAttachmentContent() == null
                && chatList.get(position).getContactMessageName() == null && chatList.get(position).getContactMessagePhoneNumber() == null
                && chatList.get(position).getContactMessageProfilePhoto() == null && chatList.get(position).getMessageVideoContent() != null) {

            return true;

        } else {

            return false;

        }

    }

    private boolean isAttachmentMessage(int position) {

        if (chatList.get(position).getMessageText() == null && chatList.get(position).getAudioRecordingUrl() == null
                && chatList.get(position).getMessageCameraContent() == null && chatList.get(position).getMessageAttachmentContent() != null
                && chatList.get(position).getContactMessageName() == null && chatList.get(position).getContactMessagePhoneNumber() == null
                && chatList.get(position).getContactMessageProfilePhoto() == null) {

            return true;

        } else {

            return false;

        }

    }

    private boolean isContactMessage(int position) {

        if (chatList.get(position).getMessageText() == null && chatList.get(position).getAudioRecordingUrl() == null
                && chatList.get(position).getMessageCameraContent() == null && chatList.get(position).getMessageAttachmentContent() == null
                && chatList.get(position).getContactMessageName() != null && chatList.get(position).getContactMessagePhoneNumber() != null
                && chatList.get(position).getContactMessageProfilePhoto() != null) {

            return true;

        } else {

            return false;

        }

    }

    private void showMyTextMessage (final ChatViewHolder holder, int position, String bubbleTimestamp) {

        //hide all other message views in new thread
        new Thread(new Runnable() {
            public void run() {

                holder.mAudioMessages.setVisibility(View.GONE);
                holder.otherAudioMessages.setVisibility(View.GONE);
                holder.otherTextMessages.setVisibility(View.GONE);
                holder.mCameraContentMessages.setVisibility(View.GONE);
                holder.mVideoMessages.setVisibility(View.GONE);
                holder.otherCameraContentMessages.setVisibility(View.GONE);
                holder.mAttachmentMessages.setVisibility(View.GONE);
                holder.otherAttachmentMessages.setVisibility(View.GONE);
                holder.mContactMessages.setVisibility(View.GONE);
                holder.otherVideoMessages.setVisibility(View.GONE);
                holder.otherContactMessages.setVisibility(View.GONE);

            }
        }).start();

        //message is not currently saved internally
        if (!InternalStorage.isStoredInternally(chatList.get(position).getKeyMarker(), context)) {

            //Show only this users text message
            holder.mUserTextMessage.setText(chatList.get(position).getMessageText());
            holder.mUserTextTimestamp.setText(bubbleTimestamp);
            holder.mUserTextMessage.setTextColor(Color.parseColor("#ffffff"));
            holder.mUserTextTimestamp.setTextColor(Color.parseColor("#ffffff"));
            holder.mTextMessages.setVisibility(View.VISIBLE);

            //save message internally
            InternalStorage.saveMessageContentInternally(chatList.get(position).getKeyMarker(), chatList.get(position).getMessageText(), context);

        } else {    //message is stored internally

            String textFileContents = InternalStorage.getTextFileContents(chatList.get(position).getKeyMarker(), context);
            //if the text file contents is equal to the random alpha numeric string generated, then the file couldn't be read
            //if the file couldn't be read, this message won't be shown
            if (!textFileContents.equals(chatList.get(position).getKeyMarker())) {

                //Show only this users text message
                if (!textFileContents.equals("")) {

                    //this users text message isn't empty, show it
                    holder.mUserTextMessage.setText(textFileContents);
                    holder.mUserTextTimestamp.setText(bubbleTimestamp);
                    holder.mUserTextMessage.setTextColor(Color.parseColor("#ffffff"));
                    holder.mUserTextTimestamp.setTextColor(Color.parseColor("#ffffff"));
                    holder.mTextMessages.setVisibility(View.VISIBLE);

                } else {

                    //this users text message is empty, therefore we don't show it
                    holder.mTextMessages.setVisibility(View.GONE);

                }

            } else {

                //this users text message couldn't be read, therefore we don't show it
                holder.mTextMessages.setVisibility(View.GONE);

            }

        }

    }

    private void showMyAudioMessage (final ChatViewHolder holder, final int position, final String bubbleTimestamp, final String audioTrackerDefault) {

        new Thread(new Runnable() {
            public void run() {

                //Show only this users audio message
                holder.mTextMessages.setVisibility(View.GONE);
                holder.otherAudioMessages.setVisibility(View.GONE);
                holder.otherTextMessages.setVisibility(View.GONE);
                holder.mVideoMessages.setVisibility(View.GONE);
                holder.mCameraContentMessages.setVisibility(View.GONE);
                holder.otherCameraContentMessages.setVisibility(View.GONE);
                holder.mAttachmentMessages.setVisibility(View.GONE);
                holder.otherContactMessages.setVisibility(View.GONE);
                holder.otherVideoMessages.setVisibility(View.GONE);
                holder.mContactMessages.setVisibility(View.GONE);
                holder.otherAttachmentMessages.setVisibility(View.GONE);

            }
        }).start();

        holder.mUserAudioTimestamp.setText(bubbleTimestamp);
        holder.mUserAudioTimeTracker.setText(audioTrackerDefault);
        holder.mUserAudioTimestamp.setTextColor(Color.parseColor("#ffffff"));
        holder.mUserAudioTimeTracker.setTextColor(Color.parseColor("#ffffff"));
        FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("OnePic").child("OnePic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String profilePicURL = dataSnapshot.getValue().toString();
                Glide.with(context)
                        .load(profilePicURL)
                        .centerCrop()
                        .transform(new ChatAdapter.CircleTransform(context))
                        .into(holder.mUserAudioMessagesProfilePicture);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //setting audio filepath
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(chatList.get(position).getAudioRecordingUrl());

        File localFile = new File(context.getFilesDir(), chatList.get(position).getKeyMarker());

        if (!InternalStorage.internalFileExists(chatList.get(position).getKeyMarker(), context)) {

            InternalStorage.saveFileInternally(storageReference, localFile, context);

            holder.bothAudioMessageFileHolder.setText(localFile.getAbsolutePath());

            holder.mAudioMessages.setVisibility(View.VISIBLE);

        } else {

            holder.bothAudioMessageFileHolder.setText(localFile.getAbsolutePath());

            holder.mAudioMessages.setVisibility(View.VISIBLE);

        }

    }

    private void showMyCameraContentMessages (final ChatViewHolder holder, final int position, String bubbleTimestamp, String audioTrackerDefault) {

        new Thread(new Runnable() {
            public void run() {

                //Show only this users camera content message
                holder.mTextMessages.setVisibility(View.GONE);
                holder.mAudioMessages.setVisibility(View.GONE);
                holder.mVideoMessages.setVisibility(View.GONE);
                holder.mAttachmentMessages.setVisibility(View.GONE);
                holder.mContactMessages.setVisibility(View.GONE);
                holder.otherAudioMessages.setVisibility(View.GONE);
                holder.otherTextMessages.setVisibility(View.GONE);
                holder.otherVideoMessages.setVisibility(View.GONE);
                holder.otherCameraContentMessages.setVisibility(View.GONE);
                holder.otherContactMessages.setVisibility(View.GONE);
                holder.otherAttachmentMessages.setVisibility(View.GONE);

            }
        }).start();

        if (!InternalStorage.isStoredInternally (chatList.get(position).getKeyMarker(), context)) {

            //Setting Camera Content
            Glide.with(context)
                    .load(chatList.get(position).getMessageCameraContent())
                    .asBitmap()
                    .centerCrop()
                    .into(holder.mCameraContentImage);

            holder.mCameraContentMessages.setVisibility(View.VISIBLE);
            holder.fullScreenViewUrlHolder.setText(chatList.get(position).getMessageCameraContent());

            //save message internally in new thread
            new Thread(new Runnable() {
                public void run() {

                    //convert image url to bitmap and then to byte array
                    Bitmap bitmap = ImageManager.getBitmapFromUrl(chatList.get(position).getMessageCameraContent());

                    byte[] bytes = new byte[0];

                    if (bitmap != null) {

                        bytes = ImageManager.getBytesFromBitmap(bitmap, 100); // 100 refers to the image quality percentage

                    }

                    String byteArrayString = Base64.encodeToString(bytes, Base64.DEFAULT);

                    InternalStorage.saveMessageContentInternally(chatList.get(position).getKeyMarker(), byteArrayString, context);

                }
            }).start();

        } else {//is stored internally, therefore, load from the internal file

            String textFileContents = InternalStorage.getTextFileContents(chatList.get(position).getKeyMarker(), context);
            //if the text file contents is equal to the random alpha numeric string generated, then the file couldn't be read
            //if the file couldn't be read, this message won't be shown
            if (!textFileContents.equals(chatList.get(position).getKeyMarker())) {

                //Show only this users text message
                if (!textFileContents.equals("")) {

                    //this users message isn't empty, show it

                    //this converts the string of bytes to a byte array
                    byte[] imageByteArray = Base64.decode(textFileContents, Base64.DEFAULT);

                    //use glide to load the image from the byte array
                    Glide.with(context)
                            .load(imageByteArray)
                            .asBitmap()
                            .centerCrop()
                            .into(holder.mCameraContentImage);

                    holder.mCameraContentMessages.setVisibility(View.VISIBLE);
                    holder.fullScreenViewUrlHolder.setText(chatList.get(position).getMessageCameraContent());

                } else {

                    //this users image message is empty, therefore we don't show it
                    holder.mCameraContentMessages.setVisibility(View.GONE);

                }

            } else {

                //this users image message couldn't be read, therefore we don't show it
                holder.mCameraContentMessages.setVisibility(View.GONE);

            }

        }

    }

    private void showMyVideoMessages (final ChatViewHolder holder, final int position, String bubbleTimestamp, String audioTrackerDefault) throws IOException {


        new Thread(new Runnable() {
            public void run() {

                //Show only this users camera content message
                holder.mTextMessages.setVisibility(View.GONE);
                holder.mAudioMessages.setVisibility(View.GONE);
                holder.mCameraContentMessages.setVisibility(View.GONE);
                holder.mAttachmentMessages.setVisibility(View.GONE);
                holder.mContactMessages.setVisibility(View.GONE);
                holder.otherAudioMessages.setVisibility(View.GONE);
                holder.otherVideoMessages.setVisibility(View.GONE);
                holder.otherTextMessages.setVisibility(View.GONE);
                holder.otherCameraContentMessages.setVisibility(View.GONE);
                holder.otherContactMessages.setVisibility(View.GONE);
                holder.otherAttachmentMessages.setVisibility(View.GONE);

            }
        }).start();

        //Setting Video Content
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(chatList.get(position).getMessageVideoContent());

        File localFile = new File(context.getFilesDir(), chatList.get(position).getKeyMarker());

        if (!InternalStorage.internalFileExists(chatList.get(position).getKeyMarker(), context)) {

            InternalStorage.saveFileInternally(storageReference, localFile, context);

            //play video from internal storage
            Uri mVideoUri = Uri.parse(localFile.toString());
            //holder.mVideoView.setVideoURI(mVideoUri);
            holder.mVideoView.setVideoPath(localFile.getAbsolutePath());
            holder.mVideoView.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared() {
                    holder.mVideoView.seekTo(0);
                }
            });
            holder.mVideoView.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion() {

                    holder.mVideoView.restart();
                    holder.mVideoView.setOnPreparedListener(new OnPreparedListener() {
                        @Override
                        public void onPrepared() {
                            holder.mVideoView.seekTo(0);
                            holder.mVideoView.pause();
                        }
                    });

                }
            });
            if(holder.mVideoView.getVideoControls() != null){
                holder.mVideoView.getVideoControls().hide();
            }

            holder.mVideoView.setScaleType(CENTER_CROP);

            holder.mVideoMessages.setVisibility(View.VISIBLE);

            holder.fullScreenViewUrlHolder.setText(mVideoUri.toString());

        } else {

            //play video from internal storage
            Uri mVideoUri = Uri.parse(localFile.toString());
            holder.mVideoView.setVideoPath(localFile.getAbsolutePath());
            holder.mVideoView.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared() {
                    holder.mVideoView.seekTo(0);
                }
            });
            holder.mVideoView.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion() {

                    holder.mVideoView.restart();
                    holder.mVideoView.setOnPreparedListener(new OnPreparedListener() {
                        @Override
                        public void onPrepared() {
                            holder.mVideoView.seekTo(0);
                            holder.mVideoView.pause();
                        }
                    });

                }
            });
            if(holder.mVideoView.getVideoControls() != null){
                holder.mVideoView.getVideoControls().hide();
            }

            holder.mVideoView.setScaleType(CENTER_CROP);

            holder.mVideoMessages.setVisibility(View.VISIBLE);

            holder.fullScreenViewUrlHolder.setText(mVideoUri.toString());

        }

    }

    private void showMyAttachmentMessages (final ChatViewHolder holder, int position, String bubbleTimestamp, String audioTrackerDefault) {

        //Show only this users attachment messages
        holder.mTextMessages.setVisibility(View.GONE);
        holder.mAudioMessages.setVisibility(View.GONE);
        holder.mContactMessages.setVisibility(View.GONE);
        holder.mVideoMessages.setVisibility(View.GONE);
        holder.mCameraContentMessages.setVisibility(View.GONE);
        holder.otherCameraContentMessages.setVisibility(View.GONE);
        holder.otherContactMessages.setVisibility(View.GONE);
        holder.otherVideoMessages.setVisibility(View.GONE);
        holder.otherAudioMessages.setVisibility(View.GONE);
        holder.otherTextMessages.setVisibility(View.GONE);
        holder.otherAttachmentMessages.setVisibility(View.GONE);

        holder.mAttachmentMessagesTimeStamp.setText(bubbleTimestamp);
        holder.mAttachmentMessagesTitle.setText(chatList.get(position).getMessageAttachmentContent());
        holder.mAttachmentMessagesSizeType.setText(chatList.get(position).getMessageAttachmentContent());
        holder.mAttachmentMessagesTimeStamp.setTextColor(Color.parseColor("#ffffff"));
        holder.mAttachmentMessagesTitle.setTextColor(Color.parseColor("#ffffff"));
        holder.mAttachmentMessagesSizeType.setTextColor(Color.parseColor("#ffffff"));

        holder.mAttachmentMessages.setVisibility(View.VISIBLE);

    }

    private void showMyContactMessages (final ChatViewHolder holder, final int position, String bubbleTimestamp, String audioTrackerDefault) {

        new Thread(new Runnable() {
            public void run() {

                //Show only this users contact messages
                holder.mTextMessages.setVisibility(View.GONE);
                holder.mAudioMessages.setVisibility(View.GONE);
                holder.mCameraContentMessages.setVisibility(View.GONE);
                holder.mVideoMessages.setVisibility(View.GONE);
                holder.mAttachmentMessages.setVisibility(View.GONE);
                holder.otherAudioMessages.setVisibility(View.GONE);
                holder.otherVideoMessages.setVisibility(View.GONE);
                holder.otherTextMessages.setVisibility(View.GONE);
                holder.otherContactMessages.setVisibility(View.GONE);
                holder.otherCameraContentMessages.setVisibility(View.GONE);
                holder.otherAttachmentMessages.setVisibility(View.GONE);

            }
        }).start();

        if(chatList.get(position).getContactMessageProfilePhoto().equals("default")) {

            holder.mContactMessagesProfilePhoto.setBackgroundResource(R.drawable.icon_profile_picture_gray);

        } else {

            Glide.with(context)
                    .load(chatList.get(position).getContactMessageProfilePhoto())
                    .centerCrop()
                    .transform(new ChatAdapter.CircleTransform(context))
                    .into(holder.mContactMessagesProfilePhoto);

        }

        holder.mContactMessagesTimeStamp.setText(bubbleTimestamp);
        holder.mContactMessagesDisplayName.setText(chatList.get(position).getContactMessageName());
        holder.mContactMessagesPhoneNumber.setText(chatList.get(position).getContactMessagePhoneNumber());
        holder.mContactMessagesTimeStamp.setTextColor(Color.parseColor("#ffffff"));
        holder.mContactMessagesDisplayName.setTextColor(Color.parseColor("#ffffff"));
        holder.mContactMessagesPhoneNumber.setTextColor(Color.parseColor("#ffffff"));

        holder.mContactMessages.setVisibility(View.VISIBLE);

    }
    
    private void showOtherTextMessage (final ChatViewHolder holder, final int position, String bubbleTimestamp) {

        new Thread(new Runnable() {
            public void run() {

                holder.mAudioMessages.setVisibility(View.GONE);
                holder.otherAudioMessages.setVisibility(View.GONE);
                holder.mTextMessages.setVisibility(View.GONE);
                holder.mCameraContentMessages.setVisibility(View.GONE);
                holder.mVideoMessages.setVisibility(View.GONE);
                holder.otherCameraContentMessages.setVisibility(View.GONE);
                holder.otherVideoMessages.setVisibility(View.GONE);
                holder.mAttachmentMessages.setVisibility(View.GONE);
                holder.otherAttachmentMessages.setVisibility(View.GONE);
                holder.mContactMessages.setVisibility(View.GONE);
                holder.otherContactMessages.setVisibility(View.GONE);

            }
        }).start();

        //message is not currently saved internally
        if (!InternalStorage.isStoredInternally(chatList.get(position).getKeyMarker(), context)) {

            //show the other users text message
            Glide.with(context)
                    .load(chatList.get(position).getOtherUserProfilePhotoUrl())
                    .centerCrop()
                    .transform(new ChatAdapter.CircleTransform(context))
                    .into(holder.otherUserTextMessagesProfilePicture);

            holder.otherUserTextTimestamp.setText(bubbleTimestamp);
            holder.otherUserTextMessage.setText(chatList.get(position).getMessageText());
            holder.otherUserTextMessage.setTextColor(Color.parseColor("#000000"));
            holder.otherUserTextTimestamp.setTextColor(Color.parseColor("#000000"));
            holder.otherTextMessages.setVisibility(View.VISIBLE);

            //save message internally
            InternalStorage.saveMessageContentInternally(chatList.get(position).getKeyMarker(), chatList.get(position).getMessageText(), context);

        } else {    //message is stored internally

            String textFileContents = InternalStorage.getTextFileContents(chatList.get(position).getKeyMarker(), context);

            if (!textFileContents.equals(chatList.get(position).getKeyMarker())) {

                //Show only this users text message
                if (!textFileContents.equals("")) {

                    //the other users text message isn't empty, show it
                    //TODO: SAVE THE OTHER USER'S PROFILE PICTURE INTERNALLY
                    Glide.with(context)
                            .load(chatList.get(position).getOtherUserProfilePhotoUrl())
                            .centerCrop()
                            .transform(new ChatAdapter.CircleTransform(context))
                            .into(holder.otherUserTextMessagesProfilePicture);

                    holder.otherUserTextTimestamp.setText(bubbleTimestamp);
                    holder.otherUserTextMessage.setText(chatList.get(position).getMessageText());
                    holder.otherUserTextMessage.setTextColor(Color.parseColor("#000000"));
                    holder.otherUserTextTimestamp.setTextColor(Color.parseColor("#000000"));
                    holder.otherTextMessages.setVisibility(View.VISIBLE);

                } else {

                    //the other users text message is empty, therefore we don't show it
                    holder.otherTextMessages.setVisibility(View.GONE);

                }

            } else {

                //the other users text message couldn't be read, therefore we don't show it
                holder.otherTextMessages.setVisibility(View.GONE);

            }

        }

    }

    private void showOtherAudioMessage (final ChatViewHolder holder, final int position, String bubbleTimestamp, String audioTrackerDefault) {

        new Thread(new Runnable() {
            public void run() {

                //Show only other users audio message
                holder.mAudioMessages.setVisibility(View.GONE);
                holder.otherTextMessages.setVisibility(View.GONE);
                holder.mTextMessages.setVisibility(View.GONE);
                holder.mCameraContentMessages.setVisibility(View.GONE);
                holder.mVideoMessages.setVisibility(View.GONE);
                holder.otherCameraContentMessages.setVisibility(View.GONE);
                holder.mAttachmentMessages.setVisibility(View.GONE);
                holder.otherVideoMessages.setVisibility(View.GONE);
                holder.otherAttachmentMessages.setVisibility(View.GONE);
                holder.mContactMessages.setVisibility(View.GONE);
                holder.otherContactMessages.setVisibility(View.GONE);

            }
        }).start();

        Glide.with(context)
                .load(chatList.get(position).getOtherUserProfilePhotoUrl())
                .centerCrop()
                .transform(new ChatAdapter.CircleTransform(context))
                .into(holder.otherUserAudioMessagesProfilePicture);

        //Set text for timestamp and audio stamp here
        holder.otherUserAudioTimestamp.setText(bubbleTimestamp);
        holder.otherUserAudioTimeTracker.setText(audioTrackerDefault);
        holder.otherUserAudioTimestamp.setTextColor(Color.parseColor("#000000"));
        holder.otherUserAudioTimeTracker.setTextColor(Color.parseColor("#000000"));
        holder.otherAudioMessages.setVisibility(View.VISIBLE);

        //Setting audio filepath
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(chatList.get(position).getAudioRecordingUrl());

        File localFile = new File(context.getFilesDir(), chatList.get(position).getKeyMarker());

        if (!InternalStorage.internalFileExists(chatList.get(position).getKeyMarker(), context)) {

            InternalStorage.saveFileInternally(storageReference, localFile, context);

            holder.bothAudioMessageFileHolder.setText(localFile.getAbsolutePath());

            holder.otherAudioMessages.setVisibility(View.VISIBLE);

        } else {

            holder.bothAudioMessageFileHolder.setText(localFile.getAbsolutePath());

            holder.otherAudioMessages.setVisibility(View.VISIBLE);

        }

    }

    private void showOtherCameraContentMessages (final ChatViewHolder holder, final int position, String bubbleTimestamp, String audioTrackerDefault) {

        new Thread(new Runnable() {
            public void run() {

                //Show other users camera content message
                holder.mTextMessages.setVisibility(View.GONE);
                holder.mAudioMessages.setVisibility(View.GONE);
                holder.mAttachmentMessages.setVisibility(View.GONE);
                holder.mContactMessages.setVisibility(View.GONE);
                holder.mVideoMessages.setVisibility(View.GONE);
                holder.otherVideoMessages.setVisibility(View.GONE);
                holder.mCameraContentMessages.setVisibility(View.GONE);
                holder.otherAudioMessages.setVisibility(View.GONE);
                holder.otherTextMessages.setVisibility(View.GONE);
                holder.otherContactMessages.setVisibility(View.GONE);
                holder.otherAttachmentMessages.setVisibility(View.GONE);

            }
        }).start();

        if (!InternalStorage.isStoredInternally(chatList.get(position).getKeyMarker(), context)) {

            //Setting Camera Content
            Glide.with(context)
                    .load(chatList.get(position).getMessageCameraContent())
                    .asBitmap()
                    .centerCrop()
                    .into(holder.otherCameraContentImage);

            holder.otherCameraContentMessages.setVisibility(View.VISIBLE);
            holder.fullScreenViewUrlHolder.setText(chatList.get(position).getMessageCameraContent());

            //save message internally in new thread
            new Thread(new Runnable() {
                public void run() {

                    //convert image url to bitmap and then to byte array
                    Bitmap bitmap = ImageManager.getBitmapFromUrl(chatList.get(position).getMessageCameraContent());

                    byte[] bytes = new byte[0];

                    if (bitmap != null) {

                        bytes = ImageManager.getBytesFromBitmap(bitmap, 100); // 100 refers to the image quality percentage

                    }

                    String byteArrayString = Base64.encodeToString(bytes, Base64.DEFAULT);

                    InternalStorage.saveMessageContentInternally(chatList.get(position).getKeyMarker(), byteArrayString, context);

                }
            }).start();


        } else {//is stored internally, therefore, load from the internal file

            String textFileContents = InternalStorage.getTextFileContents(chatList.get(position).getKeyMarker(), context);

            if (!textFileContents.equals(chatList.get(position).getKeyMarker())) {

                //Show only this users text message
                if (!textFileContents.equals("")) {

                    //this users message isn't empty, show it

                    //this converts the string of bytes to a byte array
                    byte[] imageByteArray = Base64.decode(textFileContents, Base64.DEFAULT);

                    //use glide to load the image from the byte array
                    Glide.with(context)
                            .load(imageByteArray)
                            .asBitmap()
                            .centerCrop()
                            .into(holder.otherCameraContentImage);

                    holder.otherCameraContentMessages.setVisibility(View.VISIBLE);
                    holder.fullScreenViewUrlHolder.setText(chatList.get(position).getMessageCameraContent());

                } else {

                    //this users image message is empty, therefore we don't show it
                    holder.otherCameraContentMessages.setVisibility(View.GONE);

                }

            } else {

                //this users image message couldn't be read, therefore we don't show it
                holder.otherCameraContentMessages.setVisibility(View.GONE);

            }

        }

    }

    private void showOtherVideoMessages (final ChatViewHolder holder, final int position, String bubbleTimestamp, String audioTrackerDefault) {

        new Thread(new Runnable() {
            public void run() {

                //Show only this users camera content message
                holder.mTextMessages.setVisibility(View.GONE);
                holder.mAudioMessages.setVisibility(View.GONE);
                holder.mCameraContentMessages.setVisibility(View.GONE);
                holder.mAttachmentMessages.setVisibility(View.GONE);
                holder.mContactMessages.setVisibility(View.GONE);
                holder.otherAudioMessages.setVisibility(View.GONE);
                holder.mVideoMessages.setVisibility(View.GONE);
                holder.otherTextMessages.setVisibility(View.GONE);
                holder.otherCameraContentMessages.setVisibility(View.GONE);
                holder.otherContactMessages.setVisibility(View.GONE);
                holder.otherAttachmentMessages.setVisibility(View.GONE);

            }
        }).start();

        //Setting Video Content

        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(chatList.get(position).getMessageVideoContent());

        File localFile = new File(context.getFilesDir(), chatList.get(position).getKeyMarker());

        if (!InternalStorage.internalFileExists(chatList.get(position).getKeyMarker(), context)) {

            InternalStorage.saveFileInternally(storageReference, localFile, context);

            //play video from internal storage
            Uri mVideoUri = Uri.parse(localFile.toString());
            //holder.otherVideoView.setVideoURI(mVideoUri);
            holder.otherVideoView.setVideoPath(localFile.getAbsolutePath());
            holder.otherVideoView.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared() {
                    holder.otherVideoView.seekTo(0);
                }
            });
            holder.otherVideoView.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion() {

                    holder.otherVideoView.restart();
                    holder.otherVideoView.setOnPreparedListener(new OnPreparedListener() {
                        @Override
                        public void onPrepared() {
                            holder.otherVideoView.seekTo(0);
                            holder.otherVideoView.pause();
                        }
                    });

                }
            });
            if(holder.otherVideoView.getVideoControls() != null){
                holder.otherVideoView.getVideoControls().hide();
            }

            holder.otherVideoView.setScaleType(CENTER_CROP);

            holder.otherVideoMessages.setVisibility(View.VISIBLE);

            holder.fullScreenViewUrlHolder.setText(mVideoUri.toString());

        } else {

            //play video from internal storage
            Uri mVideoUri = Uri.parse(localFile.toString());
            //holder.otherVideoView.setVideoURI(mVideoUri);
            holder.otherVideoView.setVideoPath(localFile.getAbsolutePath());
            holder.otherVideoView.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared() {
                    holder.otherVideoView.seekTo(0);
                }
            });
            holder.otherVideoView.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion() {

                    holder.otherVideoView.restart();
                    holder.otherVideoView.setOnPreparedListener(new OnPreparedListener() {
                        @Override
                        public void onPrepared() {
                            holder.otherVideoView.seekTo(0);
                            holder.otherVideoView.pause();
                        }
                    });

                }
            });
            if(holder.otherVideoView.getVideoControls() != null){
                holder.otherVideoView.getVideoControls().hide();
            }

            holder.otherVideoView.setScaleType(CENTER_CROP);

            holder.otherVideoMessages.setVisibility(View.VISIBLE);

            holder.fullScreenViewUrlHolder.setText(mVideoUri.toString());

        }


    }

    private void showOtherAttachmentMessages (final ChatViewHolder holder, int position, String bubbleTimestamp, String audioTrackerDefault) {

        //Show other users attachment message
        holder.mTextMessages.setVisibility(View.GONE);
        holder.mAudioMessages.setVisibility(View.GONE);
        holder.mAttachmentMessages.setVisibility(View.GONE);
        holder.mContactMessages.setVisibility(View.GONE);
        holder.mVideoMessages.setVisibility(View.GONE);
        holder.otherVideoMessages.setVisibility(View.GONE);
        holder.mCameraContentMessages.setVisibility(View.GONE);
        holder.otherAudioMessages.setVisibility(View.GONE);
        holder.otherTextMessages.setVisibility(View.GONE);
        holder.otherCameraContentMessages.setVisibility(View.GONE);
        holder.otherContactMessages.setVisibility(View.GONE);

        holder.otherAttachmentMessagesTimeStamp.setText(bubbleTimestamp);
        holder.otherAttachmentMessagesTitle.setText(chatList.get(position).getMessageAttachmentContent());
        holder.otherAttachmentMessagesSizeType.setText(chatList.get(position).getMessageAttachmentContent());
        holder.otherAttachmentMessagesTimeStamp.setTextColor(Color.parseColor("#000000"));
        holder.otherAttachmentMessagesTitle.setTextColor(Color.parseColor("#000000"));
        holder.otherAttachmentMessagesSizeType.setTextColor(Color.parseColor("#000000"));

        holder.otherAttachmentMessages.setVisibility(View.VISIBLE);

    }

    private void showOtherContactMessages (final ChatViewHolder holder, final int position, String bubbleTimestamp, String audioTrackerDefault) {

        new Thread(new Runnable() {
            public void run() {

                //Show other users attachment message
                holder.mTextMessages.setVisibility(View.GONE);
                holder.mAudioMessages.setVisibility(View.GONE);
                holder.mAttachmentMessages.setVisibility(View.GONE);
                holder.mVideoMessages.setVisibility(View.GONE);
                holder.mContactMessages.setVisibility(View.GONE);
                holder.otherVideoMessages.setVisibility(View.GONE);
                holder.mCameraContentMessages.setVisibility(View.GONE);
                holder.otherAudioMessages.setVisibility(View.GONE);
                holder.otherTextMessages.setVisibility(View.GONE);
                holder.otherCameraContentMessages.setVisibility(View.GONE);
                holder.otherAttachmentMessages.setVisibility(View.GONE);

            }
        }).start();

        if(chatList.get(position).getContactMessageProfilePhoto().equals("default")) {

            holder.otherContactMessagesProfilePhoto.setBackgroundResource(R.drawable.icon_profile_picture_blue);

        } else {

            Glide.with(context)
                    .load(chatList.get(position).getContactMessageProfilePhoto())
                    .centerCrop()
                    .transform(new ChatAdapter.CircleTransform(context))
                    .into(holder.otherContactMessagesProfilePhoto);

        }

        holder.otherContactMessagesTimeStamp.setText(bubbleTimestamp);
        holder.otherContactMessagesDisplayName.setText(chatList.get(position).getContactMessageName());
        holder.otherContactMessagesPhoneNumber.setText(chatList.get(position).getContactMessagePhoneNumber());
        holder.otherContactMessagesTimeStamp.setTextColor(Color.parseColor("#000000"));
        holder.otherContactMessagesDisplayName.setTextColor(Color.parseColor("#000000"));
        holder.otherContactMessagesPhoneNumber.setTextColor(Color.parseColor("#000000"));

        holder.otherContactMessages.setVisibility(View.VISIBLE);

    }

    //Creating a timestamp
    private String createChatBubbleTimestamp(int position) {

        //Setting bubbleTimestamp
        String time = null;
        String firstOrLastTwelve = null;
        if (chatList.get(position).messageTimestamp != null) {
            Long millis = Long.parseLong(chatList.get(position).messageTimestamp);
            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(millis);
            int minutes = calendar.get(Calendar.MINUTE);
            int hours = calendar.get(Calendar.HOUR);
            int hoursOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            firstOrLastTwelve = null;
            if (hoursOfDay < 12) {
                firstOrLastTwelve = " AM";
            } else {
                firstOrLastTwelve = " PM";
            }
            if (hours == 0) {
                hours = 12;
            }
            time = String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
        }
        if (time == null) {
            time = "";
        }
        if (firstOrLastTwelve == null) {
            firstOrLastTwelve = "";
        }

        return (time + firstOrLastTwelve);

    }



}
