package allurosi.housebuddy.logging;

import com.google.firebase.Timestamp;

public class LogEntry {

    private String changeLocation;
    private String changeAction;
    private String fullName;
    private Timestamp timeStamp;

    public LogEntry() {}

    public LogEntry(String changeLocation, String changeAction, String fullName, Timestamp timeStamp) {
        this.changeLocation = changeLocation;
        this.changeAction = changeAction;
        this.fullName = fullName;
        this.timeStamp = timeStamp;
    }

    public String getChangeLocation() {
        return changeLocation;
    }

    public void setChangeLocation(String changeLocation) {
        this.changeLocation = changeLocation;
    }

    public String getChangeAction() {
        return changeAction;
    }

    public void setChangeAction(String changeAction) {
        this.changeAction = changeAction;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

}
