package Classess;

public class Notification {

    private String message;//rep name and the user who forked/pr message/pr status
    private User userToShowTo;
    private  String dateOfNotification;

    public void setMessageToShow(String messageToShow) {
        this.messageToShow = messageToShow;
    }

    public String getMessageToShow() {
        return messageToShow;
    }

    private String messageToShow;

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDateOfNotification(String dateOfNotification) {
        this.dateOfNotification = dateOfNotification;
    }

    public void setUserToShowTo(User userToShowTo) {
        this.userToShowTo = userToShowTo;
    }


    public String getMessage() {
        return message;
    }

    public String getDateOfNotification() {
        return dateOfNotification;
    }

    public User getUserToShowTo() {
        return userToShowTo;
    }



    public String toString()
    {
        StringBuilder b = new StringBuilder();
        b.append(message);
        b.append(System.lineSeparator());
        b.append("On " + dateOfNotification);
return  b.toString();
    }
}
