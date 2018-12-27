package com.reylo.rego.Basics;

public class UsersInfo {

    private String firstName, userBirthday, gender, OnePic;

    public UsersInfo() {

    }

    public UsersInfo(String firstName, String OnePic) {

        this.firstName = firstName;
        this.OnePic = OnePic;

    }

    public UsersInfo(String firstName, String userBirthday, String gender, String OnePic) {

        this.firstName = firstName;
        this.userBirthday = userBirthday;
        this.gender = gender;
        this.OnePic = OnePic;
        
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getUserBirthday() {
        return userBirthday;
    }

    public void setUserBirthday(String userBirthday) {
        this.userBirthday = userBirthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOnePic() {
        return OnePic;
    }

    public void setOnePic(String onePic) {
        OnePic = onePic;
    }
}
