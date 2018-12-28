package com.reylo.rego.Main.Matches.Chat;



public class ChatObject {

    private String messageText;
    private Boolean isThisUserTheCreator;
    private String otherUserName;
    private String otherUserProfilePhotoUrl;
    private String audioRecordingUrl;
    private String messageCameraContent;
    private String messageAttachmentContent;
    private String messageAttachmentSize;
    private String contactMessageName;
    private String contactMessagePhoneNumber;
    private String contactMessageProfilePhoto;
    public String messageTimestamp;
    public String messageVideoContent;
    public String keyMarker;


    public ChatObject(String messageText, Boolean isThisUserTheCreator,
                      String otherUserName, String otherUserProfilePhotoUrl,
                      String audioRecordingUrl, String messageTimestamp,
                      String messageCameraContent, String messageAttachmentContent,
                      String messageAttachmentSize, String contactMessageName,
                      String contactMessagePhoneNumber, String contactMessageProfilePhoto,
                      String messageVideoContent, String keyMarker) {

        this.messageText = messageText;
        this.isThisUserTheCreator = isThisUserTheCreator;
        this.otherUserName = otherUserName;
        this.otherUserProfilePhotoUrl = otherUserProfilePhotoUrl;
        this.audioRecordingUrl = audioRecordingUrl;
        this.messageTimestamp = messageTimestamp;
        this.messageCameraContent = messageCameraContent;
        this.messageAttachmentSize = messageAttachmentSize;
        this.messageAttachmentContent = messageAttachmentContent;
        this.contactMessageName = contactMessageName;
        this.contactMessagePhoneNumber = contactMessagePhoneNumber;
        this.contactMessageProfilePhoto = contactMessageProfilePhoto;
        this.messageVideoContent = messageVideoContent;
        this.keyMarker = keyMarker;

    }

    public String getMessageAttachmentSize() {
        return messageAttachmentSize;
    }

    public void setMessageAttachmentSize(String messageAttachmentSize) {
        this.messageAttachmentSize = messageAttachmentSize;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Boolean getThisUserTheCreator() {
        return isThisUserTheCreator;
    }

    public void setThisUserTheCreator(Boolean thisUserTheCreator) {
        isThisUserTheCreator = thisUserTheCreator;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserProfilePhotoUrl() {
        return otherUserProfilePhotoUrl;
    }

    public void setOtherUserProfilePhotoUrl(String otherUserProfilePhotoUrl) {
        this.otherUserProfilePhotoUrl = otherUserProfilePhotoUrl;
    }

    public String getAudioRecordingUrl() {
        return audioRecordingUrl;
    }

    public void setAudioRecordingUrl(String audioRecordingUrl) {
        this.audioRecordingUrl = audioRecordingUrl;
    }

    public String getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(String messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    public String getMessageCameraContent() {
        return messageCameraContent;
    }

    public void setMessageCameraContent(String messageCameraContent) {
        this.messageCameraContent = messageCameraContent;
    }

    public String getMessageAttachmentContent() {
        return messageAttachmentContent;
    }

    public void setMessageAttachmentContent(String messageAttachmentContent) {
        this.messageAttachmentContent = messageAttachmentContent;
    }

    public String getContactMessageName() {
        return contactMessageName;
    }

    public void setContactMessageName(String contactMessageName) {
        this.contactMessageName = contactMessageName;
    }

    public String getContactMessagePhoneNumber() {
        return contactMessagePhoneNumber;
    }

    public void setContactMessagePhoneNumber(String contactMessagePhoneNumber) {
        this.contactMessagePhoneNumber = contactMessagePhoneNumber;
    }

    public String getContactMessageProfilePhoto() {
        return contactMessageProfilePhoto;
    }

    public void setContactMessageProfilePhoto(String contactMessageProfilePhoto) {
        this.contactMessageProfilePhoto = contactMessageProfilePhoto;
    }

    public String getMessageVideoContent() {
        return messageVideoContent;
    }

    public void setMessageVideoContent(String messageVideoContent) {
        this.messageVideoContent = messageVideoContent;
    }

    public String getKeyMarker() {
        return keyMarker;
    }

    public void setKeyMarker(String keyMarker) {
        this.keyMarker = keyMarker;
    }

}
