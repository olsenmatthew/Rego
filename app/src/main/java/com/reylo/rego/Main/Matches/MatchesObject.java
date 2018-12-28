package com.reylo.rego.Main.Matches;

// each match needs a uid, first name and profile picture
public class MatchesObject {

    private String userId;
    private String firstName;
    private String profilePicURL;
    private String lastMessageContent;
    private String lastMessageTimestamp;

    public MatchesObject (String userId, String firstName, String profilePicURL){
        this(userId, firstName, profilePicURL, null, null);
    }

    public MatchesObject (String userId, String firstName, String profilePicURL, String lastMessageContent, String lastMessageTimestamp) {
        this.userId = userId;
        this.firstName = firstName;
        this.profilePicURL = profilePicURL;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public MatchesObject (String firstName, String profilePicURL){
        this.firstName = firstName;
        this.profilePicURL = profilePicURL;
    }

    public MatchesObject (){
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getfirstName() {
        return firstName;
    }

    public void setfirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getprofilePicURL() {
        return profilePicURL;
    }

    public void setprofilePicURL(String profilePicURL) {
        this.profilePicURL = profilePicURL;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getProfilePicURL() {
        return profilePicURL;
    }

    public void setProfilePicURL(String profilePicURL) {
        this.profilePicURL = profilePicURL;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public String getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(String lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }
}
