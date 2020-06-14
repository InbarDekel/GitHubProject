package Classess;

import java.util.Date;

public class PushRequest {

    private Branch basicBranch;
    private Branch targetBranch;
    private String descriptionOfPush;
    private String openStatus;
    private String statusOfDiff;

    public void setRepName(String repName) {
        this.repName = repName;
    }

    public String getRepName() {
        return repName;
    }

    private String repName;

    public String getOpenStatus() {
        return openStatus;
    }

    public void setMessageToShow(String messageToShow) {
        this.messageToShow = messageToShow;
    }

    public String getMessageToShow() {
        return messageToShow;
    }

    private String messageToShow;
    public String getPRDate() {
        return PRDate;
    }

    public void setPRDate(String PRDate) {
        this.PRDate = PRDate;
    }

    private String PRDate;

    public User getRequsetedUser() {
        return requsetedUser;
    }

    public void setRequsetedUser(User requsetedUser) {
        this.requsetedUser = requsetedUser;
    }

    private User requsetedUser;
    private  User proposedUser;

    public void setProposedUser(User proposedUser) {
        this.proposedUser = proposedUser;
    }

    public User getProposedUser() {
        return proposedUser;
    }

    public void setBasicBranch(Branch basicBranch) {
        this.basicBranch = basicBranch;
    }

    public void setTargetBranch(Branch targetBranch) {
        this.targetBranch = targetBranch;
    }

    public void setDescriptionOfPush(String descriptionOfPush) {
        this.descriptionOfPush = descriptionOfPush;
    }

    public void setOpenStatus(String openStatus) {
        this.openStatus = openStatus;
    }

    public void setStatusOfDiff(String statusOfDiff) {
        this.statusOfDiff = statusOfDiff;
    }

    public Branch getBasicBranch() {
        return basicBranch;
    }

    public Branch getTargetBranch() {
        return targetBranch;
    }

    public String getDescriptionOfPush() {
        return descriptionOfPush;
    }

    public String isOpenStatus() {
        return openStatus;
    }

    public String getStatusOfDiff() {
        return statusOfDiff;
    }

    public PushRequest(Branch basicBranch, Branch targetBranch, String descriptionOfPush,User RRu,User u) {
        this.basicBranch = basicBranch;
        this.targetBranch = targetBranch;
        this.descriptionOfPush = descriptionOfPush;
        openStatus = "Open";
        requsetedUser = RRu;
        proposedUser = u;
        PRDate = GitManager.getDateFromObject(new Date());
        repName = u.getCurRep().getRepositoryName();
    }
public String PRToString(User u)
{
    StringBuilder b = new StringBuilder();
    b.append(u.getName() +"initiated a PR");
    b.append(System.lineSeparator());
    b.append("from"+targetBranch + "to"+ basicBranch);
    b.append(System.lineSeparator());
b.append("On "+PRDate);
    b.append(System.lineSeparator());
    if(openStatus.equals("Open"))
b.append("Status of request is open");
    else
        b.append("Status of request is close");
return b.toString();
}

}
