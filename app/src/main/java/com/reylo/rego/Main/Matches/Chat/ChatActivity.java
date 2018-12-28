package com.reylo.rego.Main.Matches.Chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.reylo.rego.Utils.RandomStringGenerator;
import com.reylo.rego.R;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatActivity extends AppCompatActivity {

    // declare firebase objects
    private FirebaseAuth mAuth;

    // declare this user's id's
    private String thisUserID;
    private String chatId;

    //declare database references
    private DatabaseReference usersDb;
    private DatabaseReference theseUsersChatId;
    private DatabaseReference chatDb;
    private DatabaseReference chatDbPlaceHolder;

    // declare chat view components
    private RecyclerView chatRecyclerView;
    private LinearLayoutManager chatLayoutManager;
    private RecyclerView.Adapter chatAdapter;

    //  other user's data
    private String otherUserId;
    private String otherUserName;
    private String otherUserProfilePhotoUrl;

    // declare ui components
    private EmojiconEditText chatActivityEditText;
    private ImageView chatActivitySendButtonImageView;
    private ImageView chatActivityRecordAudioButtonImageView;
    private ImageView chatActivityAttachmentsButtonImageView;
    private ImageView chatActivityCameraButtonImageView;
    private ImageView chatActivityEmoticonButtonImageView;
    private ConstraintLayout chatActivityTextAndButtonConstraintLayout;
    private ConstraintLayout chatActivityConstraintLayoutHolder;

    //For PERMISSIONS
    private final int PERMISSION_TO_RECORD_AUDIO = 1;
    private final int PERMISSION_TO_USE_CAMERA = 1;
    private final int PERMISSION_TO_READ_EXTERNAL_STORAGE = 1;
    private final int PERMISSIONS_FOR_READING_AND_WRITING_CONTACTS = 1;

    //For audio message
    private MediaRecorder chatActivityAudioMessage;
    private String audioMessageFile = null;
    private StorageReference audioMessageStorageReference;
    private String audioMessageStorageReferenceUrl;

    //For camera
    static private final int CAMERA_REQUEST_CODE = 1880;
    private StorageReference cameraButtonStorageReference;
    private String cameraIntentPhotoPath;
    private Uri photoURI;

    //For attachments message
    private StorageReference attachmentButtonStorageReference;
    private Dialog attachmentsDialog;

    //For video
    static private final int VIDEO_REQUEST_CODE = 1881;
    private StorageReference videoButtonStorageReference;

    //For gallery
    static private final int GALLERY_REQUEST_CODE = 1882;

    //For music
    static private final int MUSIC_REQUEST_CODE = 1883;

    //For files of all types
    static private final int FILE_REQUEST_CODE = 1884;

    //For contacts
    static private final int CONTACT_REQUEST_CODE = 1885;
    private StorageReference contactButtonStorageReference;
    private Uri uriContact;
    private String contactID;

    //For contacts
    static private final int LOCATION_REQUEST_CODE = 1886;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //information retrieved from matches view holders on click
        otherUserId = getIntent().getExtras().getString("otherUserId");
        otherUserName = getIntent().getExtras().getString("otherUserName");
        otherUserProfilePhotoUrl = getIntent().getExtras().getString("otherUserProfilePhotoUrl");

        //Firebase instantiations
        mAuth = FirebaseAuth.getInstance();
        thisUserID = mAuth.getCurrentUser().getUid();
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        theseUsersChatId = FirebaseDatabase.getInstance().getReference().child("Users").child(thisUserID).child("Match").child("Connected").child(otherUserId).child("ChatId");
        chatDbPlaceHolder = FirebaseDatabase.getInstance().getReference().child("Chat");

        chatRecyclerView = (RecyclerView) findViewById(R.id.chat_activity_messages_recycler_view);
        chatRecyclerView.setNestedScrollingEnabled(false);
        chatRecyclerView.setHasFixedSize(false);
        chatLayoutManager = new LinearLayoutManager(ChatActivity.this);
        chatLayoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(chatLayoutManager);
        chatAdapter = new ChatAdapter(getDataSetChat(), ChatActivity.this);
        chatRecyclerView.setAdapter(chatAdapter);

        chatActivityEditText = (EmojiconEditText) findViewById(R.id.chat_activity_send_message_text_box);
        chatActivitySendButtonImageView = (ImageView) findViewById(R.id.chat_activity_send_message_icon);
        chatActivityRecordAudioButtonImageView = (ImageView) findViewById(R.id.chat_activity_record_audio_message_icon);
        chatActivityAttachmentsButtonImageView = (ImageView) findViewById(R.id.chat_activity_attachments_icon);
        chatActivityCameraButtonImageView = (ImageView) findViewById(R.id.chat_activity_camera_icon);
        chatActivityEmoticonButtonImageView = (ImageView) findViewById(R.id.chat_activity_emoticons_icon);
        chatActivityTextAndButtonConstraintLayout = (ConstraintLayout) findViewById(R.id.chat_activity_message_text_and_button_container);
        chatActivityConstraintLayoutHolder = (ConstraintLayout) findViewById(R.id.chat_activity_layout_holder);

        //Emojicon Support
        final EmojIconActions emojiconActions = new EmojIconActions(this, findViewById(android.R.id.content), chatActivityEditText, chatActivityEmoticonButtonImageView);
        emojiconActions.setIconsIds(R.drawable.icon_keyboard_blue, R.drawable.icon_happy_face_blue);
        emojiconActions.setUseSystemEmoji(true);
        chatActivityEditText.setUseSystemDefault(true);

        //File name for audio message
        audioMessageFile = Environment.getExternalStorageDirectory().getAbsolutePath();
        audioMessageFile += "/audio_message.3gp";

        //Dialog for attachments popup
        attachmentsDialog = new Dialog(this);
        //This needs to be placed before set content view, otherwise an exception will be thrown
        attachmentsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        getChatId();

        chatActivityEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (resultChat.size() > 5) {

                    chatRecyclerView.smoothScrollToPosition(resultChat.size() -1);

                }

            }
        });

        chatActivityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!chatActivityEditText.getText().toString().equals("")) {

                    chatActivitySendButtonImageView.setVisibility(View.VISIBLE);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(chatActivityConstraintLayoutHolder);
                    constraintSet.connect(R.id.chat_activity_message_text_and_button_container, ConstraintSet.RIGHT, R.id.chat_activity_send_message_icon, ConstraintSet.LEFT);
                    constraintSet.connect(R.id.chat_activity_message_text_and_button_container, ConstraintSet.END, R.id.chat_activity_send_message_icon, ConstraintSet.START);
                    constraintSet.applyTo(chatActivityConstraintLayoutHolder);
                    chatActivityRecordAudioButtonImageView.setVisibility(View.GONE);
                    chatActivityCameraButtonImageView.setVisibility(View.GONE);

                } else {

                    chatActivityRecordAudioButtonImageView.setVisibility(View.VISIBLE);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(chatActivityConstraintLayoutHolder);
                    constraintSet.connect(R.id.chat_activity_message_text_and_button_container, ConstraintSet.RIGHT, R.id.chat_activity_record_audio_message_icon, ConstraintSet.LEFT);
                    constraintSet.connect(R.id.chat_activity_message_text_and_button_container, ConstraintSet.END, R.id.chat_activity_record_audio_message_icon, ConstraintSet.START);
                    constraintSet.applyTo(chatActivityConstraintLayoutHolder);
                    chatActivitySendButtonImageView.setVisibility(View.GONE);
                    chatActivityCameraButtonImageView.setVisibility(View.VISIBLE);

                }
            }
        });

        chatActivityRecordAudioButtonImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionevent) {

                if (!requestAndReturnAudioPermissions()) {

                    requestAndReturnAudioPermissions();

                } else {

                    if (motionevent.getAction() == MotionEvent.ACTION_DOWN) {

                        startRecording();

                    } else if (motionevent.getAction() == MotionEvent.ACTION_UP) {

                        stopRecording();

                    }

                }

                return false;
            }
        });

        chatActivityEmoticonButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chatActivityEmoticonButtonImageView.setOnClickListener(null);

                chatActivityEmoticonButtonImageView.setBackgroundResource(0);

                emojiconActions.ShowEmojIcon();

                chatActivityEmoticonButtonImageView.performClick();

            }
        });

        chatActivityAttachmentsButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showAttachmentsDialog();

            }
        });

        chatActivityCameraButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!requestAndReturnCameraPermissions()) {

                    requestAndReturnCameraPermissions();

                } else {

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null){

                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {

                        }

                        if (photoFile != null) {

                            photoURI = FileProvider.getUriForFile(ChatActivity.this,
                                    "com.reylo.rego.fileprovider",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);

                        }

                    }

                }

            }
        });

        chatActivitySendButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }

        });

        emojiconActions.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {

            }

            @Override
            public void onKeyboardClose() {

            }
        });

    }

    //Create Popup next to the bottom of the screen
    //The popup contains buttons for:
    // taking photos and videos
    // getting photos and videos from the gallery
    // sending music and other files
    // sending contact info
    // accessing google maps for users location (needs to send snapshot as add on feature)
    // and finally dismissing the popup itself
    private void showAttachmentsDialog() {

        ImageView cameraButton;
        ImageView videoButton;
        ImageView galleryButton;
        ImageView musicButton;
        ImageView fileButton;
        ImageView contactButton;
        ImageView locationButton;
        ImageView dismissButton;


        attachmentsDialog.setContentView(R.layout.chat_attachment_popup);

        cameraButton = (ImageView) attachmentsDialog.findViewById(R.id.attachmentsCamera);
        videoButton = (ImageView) attachmentsDialog.findViewById(R.id.attachmentsVideo);
        galleryButton = (ImageView) attachmentsDialog.findViewById(R.id.attachmentsGallery);
        musicButton = (ImageView) attachmentsDialog.findViewById(R.id.attachmentsMusic);
        fileButton = (ImageView) attachmentsDialog.findViewById(R.id.attachmentsFile);
        contactButton = (ImageView) attachmentsDialog.findViewById(R.id.attachmentsContact);
        locationButton = (ImageView) attachmentsDialog.findViewById(R.id.attachmentsLocation);
        dismissButton = (ImageView) attachmentsDialog.findViewById(R.id.attachmentsClose);

        //TODO: LOW PRIORITY: SHOW PHOTO IN GALLERY AFTER TAKEN
        //take photo
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!requestAndReturnCameraPermissions()) {

                    requestAndReturnCameraPermissions();

                } else {

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null){

                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {

                        }

                        if (photoFile != null) {

                            photoURI = FileProvider.getUriForFile(ChatActivity.this,
                                    "com.reylo.rego.fileprovider",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);

                        }

                    }

                }

            }
        });

        //take video
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!requestAndReturnCameraPermissions()) {

                    requestAndReturnCameraPermissions();

                } else {

                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    if (takeVideoIntent.resolveActivity(getPackageManager()) != null){

                        startActivityForResult(takeVideoIntent, VIDEO_REQUEST_CODE);

                    }

                }

            }
        });

        //TODO: LOW PRIORITY: USE MULTIPLE MEDIA PICKER LIBRARY SO MULTIPLE IMAGES AND VIDEOS CAN BE RETRIEVED AT ONE TIME
        //get attachment from gallery
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!requestAndReturnReadExternalStoragePermissions()) {

                    requestAndReturnReadExternalStoragePermissions();

                } else {

                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryIntent.setType("image/* video/*");

                    if (galleryIntent.resolveActivity(getPackageManager()) != null){

                        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);

                    }

                }

            }
        });

        //get music attachment
        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!requestAndReturnReadExternalStoragePermissions()) {

                    requestAndReturnReadExternalStoragePermissions();

                } else {

                    Intent musicIntent = new Intent (Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);

                    if (musicIntent.resolveActivity(getPackageManager()) != null){

                        startActivityForResult(musicIntent, MUSIC_REQUEST_CODE);

                    }

                }

            }
        });

        //get files
        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!requestAndReturnReadExternalStoragePermissions()) {

                    requestAndReturnReadExternalStoragePermissions();

                } else {

                    Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    fileIntent.setType("*/*");

                    if (fileIntent.resolveActivity(getPackageManager()) != null){

                        startActivityForResult(fileIntent, FILE_REQUEST_CODE);

                    }

                }

            }
        });

        //get contacts
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!requestAndReturnReadExternalStoragePermissions()) {

                    requestAndReturnReadExternalStoragePermissions();

                } else if (!requestAndReturnContactPermissions()) {

                    requestAndReturnContactPermissions();

                }else {

                    startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), CONTACT_REQUEST_CODE);

                }

            }
        });

        //TODO: FIX THIS CODE TO ALLOW SENDING A SNAPSHOT OF THIS USER'S LOCATION
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String latitude = "-33.9075462";
                String longitude = "151.2354568";

                String uri = "geo:" + latitude + ","
                        +longitude + "?q=" + latitude
                        + "," + longitude;

                startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse(uri)));

            }
        });

        //Dismiss dialog
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attachmentsDialog.dismiss();
            }
        });

        attachmentsDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        attachmentsDialog.getWindow().setGravity(Gravity.BOTTOM);
        attachmentsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        attachmentsDialog.show();

    }

    // this function sends a text message to the database
    // so all participants in the chat can read the contents
    // This also smooth scrolls to the bottom of the recycler view on send
    private void sendMessage() {

        String messageToBeSent = chatActivityEditText.getText().toString();

        long timeInMilliseconds = System.currentTimeMillis();
        String messageTimestamp = Long.toString(timeInMilliseconds);

        String keyMarker = RandomStringGenerator.randomAlphaNumeric(10);

        if (!messageToBeSent.isEmpty()){

            DatabaseReference messageToBeSentDb = chatDb.push();

            final Map latestMessage = new HashMap();
            latestMessage.put("messageCreator", thisUserID);
            latestMessage.put("audioRecording", null);
            latestMessage.put("messageText", messageToBeSent);
            latestMessage.put("messageCameraContent", null);
            latestMessage.put("messageVideoContent", null);
            latestMessage.put("messageAttachmentContent", null);
            latestMessage.put("messageAttachmentSize", null);
            latestMessage.put("contactMessageName", null);
            latestMessage.put("contactMessagePhoneNumber", null);
            latestMessage.put("contactMessageProfilePhoto", null);
            latestMessage.put("messageTimestamp", messageTimestamp);
            latestMessage.put("keyMarker", keyMarker);

            messageToBeSentDb.setValue(latestMessage);

            if (resultChat.size() > 5) {

                chatRecyclerView.smoothScrollToPosition(resultChat.size());

            }

        }

        chatActivityEditText.setText(null);

    }

    // this function sends a audio message to the database
    // so all participants in the chat can listen to the contents
    // This also smooth scrolls to the bottom of the recycler view on send
    private void sendAudioMessage() {

        final DatabaseReference messageToBeSentDb = chatDb.push();

        final Map latestMessage = new HashMap();

        audioMessageStorageReference = FirebaseStorage.getInstance().getReference().child("AudioMessages").child(messageToBeSentDb.toString());
        Uri audioMessageUri = Uri.fromFile(new File(audioMessageFile));
        audioMessageStorageReference.putFile(audioMessageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                long timeInMilliseconds = System.currentTimeMillis();
                String messageTimestamp = Long.toString(timeInMilliseconds);

                String keyMarker = RandomStringGenerator.randomAlphaNumeric(10);

                String audioRecording = taskSnapshot.getDownloadUrl().toString();
                latestMessage.put("messageCreator", thisUserID);
                latestMessage.put("audioRecording", audioRecording);
                latestMessage.put("messageText", null);
                latestMessage.put("messageCameraContent", null);
                latestMessage.put("messageVideoContent", null);
                latestMessage.put("messageAttachmentContent", null);
                latestMessage.put("messageAttachmentSize", null);
                latestMessage.put("contactMessageName", null);
                latestMessage.put("contactMessagePhoneNumber", null);
                latestMessage.put("contactMessageProfilePhoto", null);
                latestMessage.put("messageTimestamp", messageTimestamp);
                latestMessage.put("keyMarker", keyMarker);

                messageToBeSentDb.setValue(latestMessage);

                if (resultChat.size() > 5) {
                    chatRecyclerView.smoothScrollToPosition(resultChat.size());
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(ChatActivity.this, "Audio message could not be sent at this time", Toast.LENGTH_LONG).show();

            }
        });

    }

    // this function sends a image message to the database
    // so all participants in the chat can see the contents
    // This also smooth scrolls to the bottom of the recycler view on send
    private void sendImageMessage(Uri uri) {

        final DatabaseReference messageToBeSentDb = chatDb.push();
        final Map latestMessage = new HashMap();

        cameraButtonStorageReference = FirebaseStorage.getInstance().getReference().child("ImageMessages").child(messageToBeSentDb.toString()).child(uri.getLastPathSegment());

        cameraButtonStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                long timeInMilliseconds = System.currentTimeMillis();
                String messageTimestamp = Long.toString(timeInMilliseconds);

                String keyMarker = RandomStringGenerator.randomAlphaNumeric(10);

                String messageCameraContent = taskSnapshot.getDownloadUrl().toString();
                latestMessage.put("messageCreator", thisUserID);
                latestMessage.put("audioRecording", null);
                latestMessage.put("messageText", null);
                latestMessage.put("messageCameraContent", messageCameraContent);
                latestMessage.put("messageVideoContent", null);
                latestMessage.put("messageAttachmentContent", null);
                latestMessage.put("messageAttachmentSize", null);
                latestMessage.put("contactMessageName", null);
                latestMessage.put("contactMessagePhoneNumber", null);
                latestMessage.put("contactMessageProfilePhoto", null);
                latestMessage.put("messageTimestamp", messageTimestamp);
                latestMessage.put("keyMarker", keyMarker);

                messageToBeSentDb.setValue(latestMessage);

                if (resultChat.size() > 5) {
                    chatRecyclerView.smoothScrollToPosition(resultChat.size());
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }

    // gets chat id from the connection these two users made
    private void getChatId() {
        theseUsersChatId.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    chatId = dataSnapshot.getValue().toString();
                    chatDb = chatDbPlaceHolder.child(chatId);
                    retrieveChatMessages();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    // listen in chat database for new messages
    // for each message, check validity
    // categorize messages by sender
    // create new chat objects for each message
    private void retrieveChatMessages() {

        chatDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.exists()) {

                    String messageCreator = null;
                    String messageText = null;
                    String audioRecording = null;
                    String messageTimestamp = null;
                    String messageCameraContent = null;
                    String messageVideoContent = null;
                    String messageAttachmentContent = null;
                    String messageAttachmentSize = null;
                    String contactMessageName = null;
                    String contactMessagePhoneNumber = null;
                    String contactMessageProfilePhoto = null;
                    String keyMarker = null;


                    if (dataSnapshot.child("messageCreator").getValue() != null) {

                        messageCreator = dataSnapshot.child("messageCreator").getValue().toString();

                    }

                    if (dataSnapshot.child("messageText").getValue() != null){

                        messageText = dataSnapshot.child("messageText").getValue().toString();

                    }

                    if (dataSnapshot.child("audioRecording").getValue() != null) {

                        audioRecording = dataSnapshot.child("audioRecording").getValue().toString();

                    }

                    if (dataSnapshot.child("messageCameraContent").getValue() != null) {

                        messageCameraContent = dataSnapshot.child("messageCameraContent").getValue().toString();

                    }

                    if (dataSnapshot.child("messageVideoContent").getValue() != null) {

                        messageVideoContent = dataSnapshot.child("messageVideoContent").getValue().toString();

                    }

                    if (dataSnapshot.child("messageAttachmentContent").getValue() != null) {

                        messageAttachmentContent = dataSnapshot.child("messageAttachmentContent").getValue().toString();

                    }

                    if (dataSnapshot.child("messageAttachmentSize").getValue() != null) {

                        messageAttachmentSize = dataSnapshot.child("messageAttachmentSize").getValue().toString();

                    }

                    if (dataSnapshot.child("contactMessageName").getValue() != null) {

                        contactMessageName = dataSnapshot.child("contactMessageName").getValue().toString();

                    }

                    if (dataSnapshot.child("contactMessagePhoneNumber").getValue() != null) {

                        contactMessagePhoneNumber = dataSnapshot.child("contactMessagePhoneNumber").getValue().toString();

                    }

                    if (dataSnapshot.child("contactMessageProfilePhoto").getValue() != null) {

                        contactMessageProfilePhoto = dataSnapshot.child("contactMessageProfilePhoto").getValue().toString();

                    }

                    if (dataSnapshot.child("messageTimestamp").getValue() != null) {

                        messageTimestamp = dataSnapshot.child("messageTimestamp").getValue().toString();

                    }

                    if (dataSnapshot.child("keyMarker").getValue() != null) {

                        keyMarker = dataSnapshot.child("keyMarker").getValue().toString();

                    }


                    if (messageCreatorAndSomeContent(messageCreator, messageText, audioRecording, messageCameraContent,
                            messageAttachmentContent, messageAttachmentSize, contactMessageName, contactMessagePhoneNumber, contactMessageProfilePhoto, messageVideoContent)) {

                        //the message was sent from this user
                        if (messageCreator.equals(thisUserID)) {

                            ChatObject newMessage = new ChatObject(messageText, true, null, null,
                                                                    audioRecording, messageTimestamp, messageCameraContent, messageAttachmentContent,
                                                                    messageAttachmentSize, contactMessageName, contactMessagePhoneNumber,
                                                                    contactMessageProfilePhoto, messageVideoContent, keyMarker);
                            resultChat.add(newMessage);
                            chatAdapter.notifyDataSetChanged();
                            //TODO: ADD NOTIFICATION TO SCROLL DOWN AS NEW MESSAGES ARE RECEIVED
                            chatRecyclerView.smoothScrollToPosition(resultChat.size());

                        }

                        //this message was sent from some other user
                        if (!messageCreator.equals(thisUserID)) {

                            ChatObject newMessage = new ChatObject(messageText, false, otherUserName, otherUserProfilePhotoUrl,
                                                                    audioRecording, messageTimestamp, messageCameraContent, messageAttachmentContent,
                                                                    messageAttachmentSize, contactMessageName, contactMessagePhoneNumber,
                                                                    contactMessageProfilePhoto, messageVideoContent, keyMarker);
                            resultChat.add(newMessage);
                            chatAdapter.notifyDataSetChanged();
                            //TODO: ADD NOTIFICATION TO SCROLL DOWN AS NEW MESSAGES ARE RECEIVED
                            chatRecyclerView.smoothScrollToPosition(resultChat.size());

                        }

                    }

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    private ArrayList<ChatObject> resultChat = new ArrayList<ChatObject>();
    private List<ChatObject> getDataSetChat() {

        return resultChat;

    }

    // if permissions to record audio are not granted, ask for the
    // return true or false as to whether the are granted
    private boolean requestAndReturnAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Show user dialog to grant permission to record audio
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_TO_RECORD_AUDIO);

        }

        //If permission is granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            return true;

        } else {

            return false;

        }

    }

    // if permissions to camera are not granted, ask for the
    // return true or false as to whether the are granted
    private boolean requestAndReturnCameraPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSION_TO_USE_CAMERA);

        }

        //If permission is granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            return true;

        } else {

            return false;

        }

    }

    // if permissions to read and write contacts are not granted, ask for the
    // return true or false as to whether the are granted
    private boolean requestAndReturnContactPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            //Give user option to still opt-in the permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_FOR_READING_AND_WRITING_CONTACTS);

        }

        //If permission is granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CONTACTS)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {

            return true;

        } else {

            return false;

        }

    }

    // if permissions to read external storage are not given, request for them
    // return true or false after user response as to whether they were given or not
    private boolean requestAndReturnReadExternalStoragePermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //Give user option to still opt-in the permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_TO_READ_EXTERNAL_STORAGE);

        }

        //If permission is granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

            return true;

        } else {

            return false;

        }

    }

    // start recording the audio message
    private void startRecording() {

        chatActivityAudioMessage = new MediaRecorder();
        chatActivityAudioMessage.setAudioSource(MediaRecorder.AudioSource.MIC);
        chatActivityAudioMessage.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        chatActivityAudioMessage.setOutputFile(audioMessageFile);
        chatActivityAudioMessage.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {

            chatActivityAudioMessage.prepare();
            chatActivityAudioMessage.start();

        } catch (IOException e) {

        }

    }

    // stop recording the audio message,  then send the audio message
    private void stopRecording() {

        try {

            chatActivityAudioMessage.stop();
            chatActivityAudioMessage.reset();
            chatActivityAudioMessage.release();
            chatActivityAudioMessage = null;

        } catch(RuntimeException stopException) {

        }

        sendAudioMessage();
    }

    // handles intent results
    // dismisses the attachments dialog
    // depending on the request codes, the uri will be used to send a certain type of message
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        attachmentsDialog.dismiss();

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {

            if (photoURI != null) {

                sendImageMessage(photoURI);
                photoURI = null;

            }

        }

        if (requestCode == VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {

            Uri uri = data.getData();
            sendVideoMessage(uri);

        }

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {

            Uri uri = data.getData();
            if(isImage(this, uri)) {

                sendImageMessage(uri);

            } else if(isVideo(this, uri)){

                sendVideoMessage(uri);

            }

        }

        if (requestCode == MUSIC_REQUEST_CODE && resultCode == RESULT_OK) {

            Uri uri = data.getData();
            sendAttachmentMessage(uri);

        }

        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK) {

            Uri uri = data.getData();
            sendAttachmentMessage(uri);

        }

        if (requestCode == CONTACT_REQUEST_CODE && resultCode == RESULT_OK) {

            uriContact = data.getData();
            sendContactMessage(retrieveContactName(), retrieveContactNumber(), retrieveContactPhoto());

        }

    }

    // this function sends a contact message to the database
    // so all participants in the chat can see the contact
    // This also smooth scrolls to the bottom of the recycler view on send
    private void sendContactMessage(final String contactMessageName, final String contactMessagePhoneNumber, final Bitmap contactMessageProfilePhoto) {

        final DatabaseReference messageToBeSentDb = chatDb.push();
        final Map latestMessage = new HashMap();

        long timeInMilliseconds = System.currentTimeMillis();
        final String messageTimestamp = Long.toString(timeInMilliseconds);

        final String keyMarker = RandomStringGenerator.randomAlphaNumeric(10);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        if (contactMessageProfilePhoto != null) {

            contactMessageProfilePhoto.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

            byte[] data = byteArrayOutputStream.toByteArray();

            contactButtonStorageReference = FirebaseStorage.getInstance().getReference().child("ContactMessages").child(messageToBeSentDb.toString());
            UploadTask uploadContactMessage = contactButtonStorageReference.putBytes(data);
            uploadContactMessage.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String contactMessageProfilePhotoDownloadUrl = taskSnapshot.getDownloadUrl().toString();
                    latestMessage.put("messageCreator", thisUserID);
                    latestMessage.put("audioRecording", null);
                    latestMessage.put("messageText", null);
                    latestMessage.put("messageCameraContent", null);
                    latestMessage.put("messageVideoContent", null);
                    latestMessage.put("messageAttachmentContent", null);
                    latestMessage.put("messageAttachmentSize", null);
                    latestMessage.put("contactMessageName", contactMessageName);
                    latestMessage.put("contactMessagePhoneNumber", contactMessagePhoneNumber);
                    latestMessage.put("contactMessageProfilePhoto", contactMessageProfilePhotoDownloadUrl);
                    latestMessage.put("messageTimestamp", messageTimestamp);
                    latestMessage.put("keyMarker", keyMarker);

                    messageToBeSentDb.setValue(latestMessage);

                    if (resultChat.size() > 5) {

                        chatRecyclerView.smoothScrollToPosition(resultChat.size());

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        } else {

            String contactMessageProfilePhotoDownloadUrl = "default";
            latestMessage.put("messageCreator", thisUserID);
            latestMessage.put("audioRecording", null);
            latestMessage.put("messageText", null);
            latestMessage.put("messageCameraContent", null);
            latestMessage.put("messageVideoContent", null);
            latestMessage.put("messageAttachmentContent", null);
            latestMessage.put("messageAttachmentSize", null);
            latestMessage.put("contactMessageName", contactMessageName);
            latestMessage.put("contactMessagePhoneNumber", contactMessagePhoneNumber);
            latestMessage.put("contactMessageProfilePhoto", contactMessageProfilePhotoDownloadUrl);
            latestMessage.put("messageTimestamp", messageTimestamp);
            latestMessage.put("keyMarker", keyMarker);

            messageToBeSentDb.setValue(latestMessage);

            if (resultChat.size() > 5) {

                chatRecyclerView.smoothScrollToPosition(resultChat.size());

            }

        }


    }

    // this function sends a file message to the database
    // so all participants in the chat can download the contents
    // This also smooth scrolls to the bottom of the recycler view on send
    private void sendAttachmentMessage(final Uri uri) {

        final DatabaseReference messageToBeSentDb = chatDb.push();
        final Map latestMessage = new HashMap();

        attachmentButtonStorageReference = FirebaseStorage.getInstance().getReference().child("AttachmentMessages").child(messageToBeSentDb.toString()).child(uri.getLastPathSegment());

        attachmentButtonStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                long timeInMilliseconds = System.currentTimeMillis();
                String messageTimestamp = Long.toString(timeInMilliseconds);

                String keyMarker = RandomStringGenerator.randomAlphaNumeric(10);

                String messageAttachmentContent = getNameOfFileFromUri(uri);
                String messageAttachmentSize = getFileSizeFromUri(uri);


                latestMessage.put("messageCreator", thisUserID);
                latestMessage.put("audioRecording", null);
                latestMessage.put("messageText", null);
                latestMessage.put("messageCameraContent", null);
                latestMessage.put("messageVideoContent", null);
                latestMessage.put("messageAttachmentContent", messageAttachmentContent);
                latestMessage.put("messageAttachmentSize", messageAttachmentSize);
                latestMessage.put("contactMessageName", null);
                latestMessage.put("contactMessagePhoneNumber", null);
                latestMessage.put("contactMessageProfilePhoto", null);
                latestMessage.put("messageTimestamp", messageTimestamp);
                latestMessage.put("keyMarker", keyMarker);

                messageToBeSentDb.setValue(latestMessage);

                if (resultChat.size() > 5) {

                    chatRecyclerView.smoothScrollToPosition(resultChat.size());

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    // this function sends a video message to the database
    // so all participants in the chat can watch the contents
    // This also smooth scrolls to the bottom of the recycler view on send
    private void sendVideoMessage(Uri uri) {

        final DatabaseReference messageToBeSentDb = chatDb.push();
        final Map latestMessage = new HashMap();

        videoButtonStorageReference = FirebaseStorage.getInstance().getReference().child("VideoMessages").child(messageToBeSentDb.toString()).child(uri.getLastPathSegment());

        videoButtonStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                long timeInMilliseconds = System.currentTimeMillis();
                String messageTimestamp = Long.toString(timeInMilliseconds);

                String keyMarker = RandomStringGenerator.randomAlphaNumeric(10);

                String messageVideoContent = taskSnapshot.getDownloadUrl().toString();
                latestMessage.put("messageCreator", thisUserID);
                latestMessage.put("audioRecording", null);
                latestMessage.put("messageText", null);
                latestMessage.put("messageCameraContent", null);
                latestMessage.put("messageAttachmentContent", null);
                latestMessage.put("messageAttachmentSize", null);
                latestMessage.put("messageVideoContent", messageVideoContent);
                latestMessage.put("contactMessageName", null);
                latestMessage.put("contactMessagePhoneNumber", null);
                latestMessage.put("contactMessageProfilePhoto", null);
                latestMessage.put("messageTimestamp", messageTimestamp);
                latestMessage.put("keyMarker", keyMarker);
                messageToBeSentDb.setValue(latestMessage);

                if (resultChat.size() > 5) {
                    chatRecyclerView.smoothScrollToPosition(resultChat.size());
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    //TODO:  CHANGE FILE PATH TO GALLERY TO BE VISIBLE
    // stores the image to external files directory
    private File createImageFile() throws IOException {

        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        cameraIntentPhotoPath = image.getAbsolutePath();
        return image;

    }

    // retrieve and return contact's photo as a bitmap
    private Bitmap retrieveContactPhoto() {

        Bitmap photo = null;

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.valueOf(contactID)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
            }

            if (inputStream != null) {
                inputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return photo;

    }

    // retrieves and returns contact number as string
    private String retrieveContactNumber() {

        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

        return contactNumber;

    }

    // retrieves contact name as string
    private String retrieveContactName() {

        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

        }

        cursor.close();

        return contactName;

    }

    // tests mime types and returns whether the given uri involves an image
    public boolean isImage(Context context, Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {

            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);

        } else {

            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());

        }

        if(mimeType.equals("image/jpeg") || mimeType.equals("image/bmp") || mimeType.equals("image/gif")
                || mimeType.equals("image/jpg") || mimeType.equals("image/png") || mimeType.equals("image/x-ms-bmp")
                || mimeType.equals("image/vnd.wap.wbmp") || mimeType.equals("image/webp")) {

            return true;

        } else {

            return false;

        }

    }

    // tests mime types and returns whether the given uri involves a video
    public boolean isVideo(Context context, Uri uri) {

        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {

            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);

        } else {

            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());

        }

        if (mimeType.equals("video/x-ms-wmv") || mimeType.equals("video/x-ms-asf") || mimeType.equals("video/mpeg")
                || mimeType.equals("video/3gpp") || mimeType.equals("video/mp4") || mimeType.equals("video/3gpp2") || mimeType.equals("video/x-matroska")
                || mimeType.equals("video/webm") || mimeType.equals("video/mp2ts") || mimeType.equals("video/avi")) {

            return true;

        } else {

            return false;

        }

    }

    // returns true or false as to whether there is a creator of the message and content
    private boolean messageCreatorAndSomeContent(String messageCreator, String messageText, String audioRecording,
                                                    String messageCameraContent, String messageAttachmentContent,
                                                    String messageAttachmentSize, String contactMessageName,
                                                    String contactMessagePhoneNumber, String contactMessageProfilePhoto,
                                                    String messageVideoContent) {

        if(messageCreator != null
                && (messageText != null || audioRecording != null
                    || messageCameraContent != null || (messageAttachmentContent != null && messageAttachmentSize != null)
                    || (contactMessageName != null && contactMessagePhoneNumber != null && contactMessageProfilePhoto != null)
                    || messageVideoContent != null)) {

            return true;

        } else {

            return false;

        }

    }


    // dismiss dialog is it still exists
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //dismiss keyboard if it stays open when closing the application
        if (attachmentsDialog != null) {

            attachmentsDialog.dismiss();
            attachmentsDialog = null;

        }


    }

    // use uri to get name of file, if not available, use path of last segment
    private String getNameOfFileFromUri(Uri uri) {

        String filename = "";

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }

        if (filename == null || filename.equals("")) {

            filename = uri.getPath();

            int last_index = filename.lastIndexOf("/");

            if (last_index != -1) {
                filename = filename.substring(++last_index);
            }

            if (filename.equals("")) {
                filename = uri.getLastPathSegment();
            }

        }

        return filename;

    }

    // use uri to get size of file
    private String getFileSizeFromUri(Uri uri) {

        String size = "";

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                size = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
            }
        } finally {
            cursor.close();
        }

        double megabytes = (double) Integer.valueOf(size) / (1024*1024);

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        size = decimalFormat.format(megabytes) + "MB";

        return size;

    }

}