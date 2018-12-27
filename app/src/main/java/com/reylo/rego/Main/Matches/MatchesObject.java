package com.reylo.rego.Main.Matches;



public class MatchesObject {

    private String userId;
    private String firstName;
    private String profilePicURL;

    public MatchesObject (String userId, String firstName, String profilePicURL){
        this.userId = userId;
        this.firstName = firstName;
        this.profilePicURL = profilePicURL;
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


}
