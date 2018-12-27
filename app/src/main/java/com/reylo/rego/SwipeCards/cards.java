package com.reylo.rego.SwipeCards;



public class cards {

    private String userId;
    private String firstName;
    private int age;
    private String OnePic;
    private String TwoPic;
    private String ThreePic;
    private String FourPic;
    private String FivePic;
    private String SixPic;
    private String School;
    private String JobTitle;

    public cards (){
    }

    public cards (String userId, String firstName, int age,
                  String OnePic, String TwoPic, String ThreePic,
                  String FourPic, String FivePic, String SixPic,
                  String School, String JobTitle){

        this.userId = userId;
        this.firstName = firstName;
        this.age = age;
        this.OnePic = OnePic;
        this.TwoPic = TwoPic;
        this.ThreePic = ThreePic;
        this.FourPic = FourPic;
        this.FivePic = FivePic;
        this.SixPic = SixPic;
        this.School = School;
        this.JobTitle = JobTitle;

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getOnePic() {
        return OnePic;
    }

    public void setOnePic(String onePic) {
        this.OnePic = onePic;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getTwoPic() {
        return TwoPic;
    }

    public void setTwoPic(String twoPic) {
        TwoPic = twoPic;
    }

    public String getThreePic() {
        return ThreePic;
    }

    public void setThreePic(String threePic) {
        ThreePic = threePic;
    }

    public String getFourPic() {
        return FourPic;
    }

    public void setFourPic(String fourPic) {
        FourPic = fourPic;
    }

    public String getFivePic() {
        return FivePic;
    }

    public void setFivePic(String fivePic) {
        FivePic = fivePic;
    }

    public String getSixPic() {
        return SixPic;
    }

    public void setSixPic(String sixPic) {
        SixPic = sixPic;
    }

    public String getSchool() {
        return School;
    }

    public void setSchool(String school) {
        School = school;
    }

    public String getJobTitle() {
        return JobTitle;
    }

    public void setJobTitle(String jobTitle) {
        JobTitle = jobTitle;
    }
}