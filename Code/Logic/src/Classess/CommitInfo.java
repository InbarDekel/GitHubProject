package Classess;

public class CommitInfo {

    public CommitInfo(String date, String username, String SHA, String description) {
        this.creationDate = date;
        this.username = username;
        this.SHA1 = SHA;
        this.description = description;
    }

    String creationDate;
    String username;
    String SHA1;

    public void setDate(String date) {
        this.creationDate = date;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSHA(String SHA) {
        this.SHA1 = SHA;
    }



    public String getDate() {
        return creationDate;
    }

    public String getUsername() {
        return username;
    }

    public String getSHA() {
        return SHA1;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    String description;
}