package com.reylo.rego.Main.Matches.Chat;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.reylo.rego.R;

import java.io.IOException;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, Runnable{

    //Universal
    public LinearLayout messageContainer;

    //This users text messages
    public ConstraintLayout mTextMessages;
    public ConstraintLayout mTextMessagesConstraintLayout;
    public EmojiconTextView mUserTextMessage;
    public TextView mUserTextTimestamp;

    //Other users text messages
    public ConstraintLayout otherTextMessages;
    public ConstraintLayout otherTextMessagesConstraintLayout;
    public ImageView otherUserTextMessagesProfilePicture;
    public EmojiconTextView otherUserTextMessage;
    public TextView otherUserTextTimestamp;

    //This users audio messages
    public ConstraintLayout mAudioMessages;
    public ConstraintLayout mAudioMessagesConstraintLayout;
    public ImageView mUserAudioMessagesProfilePicture;
    public ImageView mUserAudioMessagesPlayButton;
    public SeekBar mUserAudioMessagesSeekBar;
    public TextView mUserAudioTimestamp;
    public TextView mUserAudioTimeTracker;

    //Other users audio messages
    public ConstraintLayout otherAudioMessages;
    public ConstraintLayout otherAudioMessagesConstraintLayout;
    public ImageView otherUserAudioMessagesProfilePicture;
    public ImageView otherUserAudioMessagesPlayButton;
    public SeekBar otherUserAudioMessagesSeekBar;
    public TextView otherUserAudioTimestamp;
    public TextView otherUserAudioTimeTracker;

    public TextView bothAudioMessageFileHolder;
    public MediaPlayer thisAudioMessagePlayer = new MediaPlayer();

    //This users camera content messages
    public ConstraintLayout mCameraContentMessages;
    public RoundedImageView mCameraContentImage;

    //Other users camera content messages
    public ConstraintLayout otherCameraContentMessages;
    public RoundedImageView otherCameraContentImage;

    //This users video messages
    public ConstraintLayout mVideoMessages;
    public com.devbrackets.android.exomedia.ui.widget.VideoView mVideoView;
    public ConstraintLayout mVideoThumbnailMessages;
    public RoundedImageView  mVideoThumbnailImage;

    //Other users video messages
    public ConstraintLayout otherVideoMessages;
    public com.devbrackets.android.exomedia.ui.widget.VideoView otherVideoView;
    public ConstraintLayout otherVideoThumbnailMessages;
    public RoundedImageView otherVideoThumbnailImage;

    //Url Gone View Holder for video and images to be viewed in full screen
    public TextView fullScreenViewUrlHolder;

    //This users contact messages
    public ConstraintLayout mContactMessages;
    public ConstraintLayout mContactMessagesConstraintLayout;
    public ImageView mContactMessagesProfilePhoto;
    public TextView mContactMessagesDisplayName;
    public TextView mContactMessagesPhoneNumber;
    public TextView mContactMessagesTimeStamp;
    public ImageView mContactMessagesDropDown;

    //Other users contact messages
    public ConstraintLayout otherContactMessages;
    public ConstraintLayout otherContactMessagesConstraintLayout;
    public ImageView otherContactMessagesProfilePhoto;
    public TextView otherContactMessagesDisplayName;
    public TextView otherContactMessagesPhoneNumber;
    public TextView otherContactMessagesTimeStamp;
    public ImageView otherContactMessagesDropDown;

    //This users contact messages
    public ConstraintLayout mAttachmentMessages;
    public ConstraintLayout mAttachmentMessagesConstraintLayout;
    public ImageView mAttachmentMessagesFilePhoto;
    public TextView mAttachmentMessagesTitle;
    public TextView mAttachmentMessagesSizeType;
    public TextView mAttachmentMessagesTimeStamp;
    public ImageView mAttachmentMessagesDropDown;

    //Other users contact messages
    public ConstraintLayout otherAttachmentMessages;
    public ConstraintLayout otherAttachmentMessagesConstraintLayout;
    public ImageView otherAttachmentMessagesFilePhoto;
    public TextView otherAttachmentMessagesTitle;
    public TextView otherAttachmentMessagesSizeType;
    public TextView otherAttachmentMessagesTimeStamp;
    public ImageView otherAttachmentMessagesDropDown;


    public ChatViewHolder(final View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        //Universal
        messageContainer = itemView.findViewById(R.id.chat_message_container);

        //This users text messages
        mTextMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_my_text_messages);
        mTextMessagesConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.chat_my_text_messages_constraint_container);
        mUserTextMessage = (EmojiconTextView) itemView.findViewById(R.id.chat_my_text_messages_user_message);
        mUserTextTimestamp = (TextView) itemView.findViewById(R.id.chat_my_text_messages_user_time_stamp);

        //Other users text messages
        otherTextMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_other_text_messages);
        otherTextMessagesConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.chat_other_text_messages_constraint_container);
        otherUserTextMessagesProfilePicture = (ImageView) itemView.findViewById(R.id.chat_other_text_messages_user_profile_picture);
        otherUserTextMessage = (EmojiconTextView) itemView.findViewById(R.id.chat_other_text_messages_user_message);
        otherUserTextTimestamp = (TextView) itemView.findViewById(R.id.chat_other_text_messages_user_time_stamp);

        //This users audio messages
        mAudioMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_my_audio_message);
        mAudioMessagesConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.chat_my_audio_messages_constraint_layout);
        mUserAudioMessagesProfilePicture = (ImageView) itemView.findViewById(R.id.chat_my_audio_messages_profile_pic);
        mUserAudioMessagesPlayButton = (ImageView) itemView.findViewById(R.id.chat_my_audio_messages_play_button);
        mUserAudioMessagesSeekBar = (SeekBar) itemView.findViewById(R.id.chat_my_audio_messages_seek_bar);
        mUserAudioTimestamp = (TextView) itemView.findViewById(R.id.chat_my_audio_messages_timestamp);
        mUserAudioTimeTracker = (TextView) itemView.findViewById(R.id.chat_my_audio_messages_time_tracker);

        //Other users audio messages
        otherAudioMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_other_audio_message);
        otherAudioMessagesConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.chat_other_audio_messages_constraint_layout);
        otherUserAudioMessagesProfilePicture = (ImageView) itemView.findViewById(R.id.chat_other_audio_messages_profile_picture);
        otherUserAudioMessagesPlayButton = (ImageView) itemView.findViewById(R.id.chat_other_audio_messages_play_button);
        otherUserAudioMessagesSeekBar = (SeekBar) itemView.findViewById(R.id.chat_other_audio_messages_seek_bar);
        otherUserAudioTimestamp = (TextView) itemView.findViewById(R.id.chat_other_audio_messages_timestamp);
        otherUserAudioTimeTracker = (TextView) itemView.findViewById(R.id.chat_other_audio_messages_time_tracker);

        bothAudioMessageFileHolder = (TextView) itemView.findViewById(R.id.chat_both_audio_message_url_holders_gone);

        //This users camera content messages
        mCameraContentMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_my_camera_content_message);
        mCameraContentImage = (RoundedImageView) itemView.findViewById(R.id.chat_my_camera_content_message_image_view);

        //Other users camera content messages
        otherCameraContentMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_other_camera_content_message);
        otherCameraContentImage = (RoundedImageView) itemView.findViewById(R.id.chat_other_camera_content_message_image_view);
        mVideoThumbnailMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_my_video_thumbnail_message);
        mVideoThumbnailImage = (RoundedImageView) itemView.findViewById(R.id.chat_my_video_thumbnail_message_image_view);

        //This users video messages
        mVideoMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_my_video_message);
        mVideoView = (com.devbrackets.android.exomedia.ui.widget.VideoView) itemView.findViewById(R.id.chat_my_video_message_video_view);
        otherVideoThumbnailMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_other_video_thumbnail_message);
        otherVideoThumbnailImage = (RoundedImageView) itemView.findViewById(R.id.chat_other_video_thumbnail_message_image_view);

        //Other users video messages
        otherVideoMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_other_video_message);
        otherVideoView = (com.devbrackets.android.exomedia.ui.widget.VideoView) itemView.findViewById(R.id.chat_other_video_message_video_view);

        //Url Gone View Holder for video and images to be viewed in full screen
        fullScreenViewUrlHolder = (TextView) itemView.findViewById(R.id.chat_full_screen_view_url_holders_gone);

        //This users contact messages
        mContactMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_my_contact_message);
        mContactMessagesConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.chat_my_contact_messages_constraint_layout);
        mContactMessagesProfilePhoto = (ImageView) itemView.findViewById(R.id.chat_my_contact_message_profile_photo);
        mContactMessagesDisplayName = (TextView) itemView.findViewById(R.id.chat_my_contact_message_display_name);
        mContactMessagesPhoneNumber = (TextView) itemView.findViewById(R.id.chat_my_contact_message_contact_number);
        mContactMessagesTimeStamp = (TextView) itemView.findViewById(R.id.chat_my_contact_message_timestamp);
        mContactMessagesDropDown = (ImageView) itemView.findViewById(R.id.chat_my_contact_message_drop_down);

        //Other users contact messages
        otherContactMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_other_contact_message);
        otherContactMessagesConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.chat_other_contact_messages_constraint_layout);
        otherContactMessagesProfilePhoto = (ImageView) itemView.findViewById(R.id.chat_other_contact_message_profile_photo);
        otherContactMessagesDisplayName = (TextView) itemView.findViewById(R.id.chat_other_contact_message_display_name);
        otherContactMessagesPhoneNumber = (TextView) itemView.findViewById(R.id.chat_other_contact_message_contact_number);
        otherContactMessagesTimeStamp = (TextView) itemView.findViewById(R.id.chat_other_contact_message_timestamp);
        otherContactMessagesDropDown = (ImageView) itemView.findViewById(R.id.chat_other_contact_message_drop_down);

        //This users attachment messages
        mAttachmentMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_my_attachment_message);
        mAttachmentMessagesConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.chat_my_attachment_constraint_layout);
        mAttachmentMessagesFilePhoto = (ImageView) itemView.findViewById(R.id.chat_my_attachment_message_file_photo);
        mAttachmentMessagesTitle = (TextView) itemView.findViewById(R.id.chat_my_attachment_message_title);
        mAttachmentMessagesSizeType = (TextView) itemView.findViewById(R.id.chat_my_attachment_message_size_type);
        mAttachmentMessagesTimeStamp = (TextView) itemView.findViewById(R.id.chat_my_attachment_message_timestamp);
        mAttachmentMessagesDropDown = (ImageView) itemView.findViewById(R.id.chat_my_attachment_message_drop_down);

        //Other users attachment messages
        otherAttachmentMessages = (ConstraintLayout) itemView.findViewById(R.id.chat_other_attachment_message);
        otherAttachmentMessagesConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.chat_other_attachment_constraint_layout);
        otherAttachmentMessagesFilePhoto = (ImageView) itemView.findViewById(R.id.chat_other_attachment_message_file_photo);
        otherAttachmentMessagesTitle = (TextView) itemView.findViewById(R.id.chat_other_attachment_message_title);
        otherAttachmentMessagesSizeType = (TextView) itemView.findViewById(R.id.chat_other_attachment_message_size_type);
        otherAttachmentMessagesTimeStamp = (TextView) itemView.findViewById(R.id.chat_other_attachment_message_timestamp);
        otherAttachmentMessagesDropDown = (ImageView) itemView.findViewById(R.id.chat_other_attachment_message_drop_down);

        //Allowing systems default emoticons to be used
        mUserTextMessage.setUseSystemDefault(true);
        otherUserTextMessage.setUseSystemDefault(true);

        //TODO: ADD INTO NEW THREAD OR ASYNC TASK TO LIFT LOAD OFF MAIN FRAME
        //TODO: LINK SEEK BAR AND TIME TRACKER TO AUDIO FILE PROGRESS
        otherUserAudioMessagesPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!thisAudioMessagePlayer.isPlaying()){

                    startPlaying();

                    otherUserAudioMessagesPlayButton.setBackgroundResource(R.drawable.icon_pause_button_light_gray);

                    Runnable seekbarRunnable = new Runnable() {
                        @Override
                        public void run() {
                            int mediaMax = thisAudioMessagePlayer.getDuration();
                            int mediaPos = thisAudioMessagePlayer.getCurrentPosition();
                            otherUserAudioMessagesSeekBar.setMax(mediaMax);
                            otherUserAudioMessagesSeekBar.setProgress(mediaPos);
                        }
                    };

                    seekbarRunnable.run();

                    thisAudioMessagePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {

                            stopPlaying();
                            otherUserAudioMessagesPlayButton.setBackgroundResource(R.drawable.icon_play_button_light_gray);
                            otherUserAudioMessagesSeekBar.setProgress(0);

                        }
                    });

                } else {

                    stopPlaying();
                    otherUserAudioMessagesPlayButton.setBackgroundResource(R.drawable.icon_play_button_light_gray);

                }

            }
        });

        //TODO: ADD INTO NEW THREAD OR ASYNC TASK TO LIFT LOAD OFF MAIN FRAME
        //TODO: LINK SEEK BAR AND TIME TRACKER TO AUDIO FILE PROGRESS
        mUserAudioMessagesPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!thisAudioMessagePlayer.isPlaying()){

                    startPlaying();

                    mUserAudioMessagesPlayButton.setBackgroundResource(R.drawable.icon_pause_button_light_gray);

                    thisAudioMessagePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {

                            stopPlaying();
                            mUserAudioMessagesPlayButton.setBackgroundResource(R.drawable.icon_play_button_light_gray);

                        }
                    });

                } else {

                    stopPlaying();
                    mUserAudioMessagesPlayButton.setBackgroundResource(R.drawable.icon_play_button_light_gray);

                }

            }
        });

        //TODO: ADD FUNCTIONALITY TO THE FOLLOWING:
        //for camera content
        mCameraContentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        otherCameraContentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //for contacts
        mContactMessagesDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        otherContactMessagesDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //for attachments
        mAttachmentMessagesFilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mAttachmentMessagesTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mAttachmentMessagesSizeType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mAttachmentMessagesDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        otherAttachmentMessagesFilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        otherAttachmentMessagesTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        otherAttachmentMessagesSizeType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        otherAttachmentMessagesDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public void onClick(View view) {

    }

    private void startPlaying() {

        String audioFilePath = bothAudioMessageFileHolder.getText().toString();

        try {

            thisAudioMessagePlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            thisAudioMessagePlayer.setDataSource(audioFilePath);
            thisAudioMessagePlayer.prepare(); // might take long! (for buffering, etc)
            thisAudioMessagePlayer.start();

        } catch (IOException e) {

        }

    }

    private void stopPlaying() {

        try{

            thisAudioMessagePlayer.stop();
            thisAudioMessagePlayer.reset();
            thisAudioMessagePlayer.release();
            thisAudioMessagePlayer = null;
            thisAudioMessagePlayer = new MediaPlayer();


        } catch(RuntimeException stopException) {

        }

    }

    @Override
    public void run() {



    }

}
