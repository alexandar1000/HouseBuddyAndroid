package allurosi.housebuddy.todolist;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class Task implements Comparable<Task>, Parcelable {

    private String taskId;
    private String taskName;
    private String taskDesc;

    private Boolean isCompleted = false;

    // Empty constructor for FireStore
    Task() {}

    Task(String name) {
        this.taskName = name;
    }

    Task(String taskName, String taskDesc) {
        this.taskName = taskName;
        this.taskDesc = taskDesc;
    }

    // Copy constructor
    Task(Task original) {
        this.taskId = original.taskId;
        this.taskName = original.taskName;
        this.taskDesc = original.taskDesc;
        this.isCompleted = original.isCompleted;
    }

    // TODO: additional constructor with list of people

    @Exclude
    String getTaskId() {
        return taskId;
    }

    // Methods used by FireBase have to be public..
    public String getTaskName() {
        return taskName;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public Boolean isCompleted() {
        return isCompleted;
    }

    @Exclude
    void setTaskId(String task_id) {
        this.taskId = task_id;
    }

    public void setTaskName(String task_name) {
        this.taskName = task_name;
    }

    public void setTaskDesc(String task_desc) {
        this.taskDesc = task_desc;
    }

    public void setIsCompleted(Boolean bool) {
        isCompleted = bool;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Task task = (Task) obj;
        if (taskDesc == null ^ task.taskDesc == null) {
            // Only one of both descriptions is null, return false
            return false;
        } else if (taskDesc == null) {
            // Both are null, compare all other fields
            return taskId.equals(task.getTaskId()) && taskName.equals(task.getTaskName()) && (isCompleted == task.isCompleted);
        } else {
            // Both are not null, compare all fields
            return taskId.equals(task.getTaskId()) && taskName.equals(task.getTaskName()) && taskDesc.equals(task.getTaskDesc())
                    && (isCompleted == task.isCompleted);
        }
    }

    @Override
    public String toString() {
        return "Id: " + taskId + ", Name: " + taskName + ", Description: " + taskDesc + ", Completed: " + isCompleted;
    }

    @Override
    public int compareTo(@NonNull Task t) {
        return taskName.compareTo(t.taskName);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(taskId);
        parcel.writeString(taskName);
        parcel.writeString(taskDesc);
        parcel.writeInt(isCompleted ? 1 : 0);
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel parcel) {
            return new Task(parcel);
        }

        @Override
        public Task[] newArray(int i) {
            return new Task[i];
        }
    };

    private Task(Parcel in) {
        taskId = in.readString();
        taskName = in.readString();
        taskDesc = in.readString();
        isCompleted = in.readInt() != 0;
    }

}
