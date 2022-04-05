package hohoaiphong_19110262.hcmute.edu.vn.chapappandroid.findfriends;

public class FindFriendModel {
    private String userName;
    private String photoName;
    private String userId;
    private boolean requestSend;

    public FindFriendModel(String userName, String photoName, String userId, boolean requestSend) {
        this.userName = userName;
        this.photoName = photoName;
        this.userId = userId;
        this.requestSend = requestSend;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRequestSend() {
        return requestSend;
    }

    public void setRequestSend(boolean requestSend) {
        this.requestSend = requestSend;
    }
}
