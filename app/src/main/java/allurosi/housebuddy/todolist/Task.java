package allurosi.housebuddy.todolist;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Task implements Comparable<Task>, Parcelable {

    private String name;
    private String description = "This task has no description.";

    public Task(String name) {
        this.name = name;
    }

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // TODO: additional constructor with list of people

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
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
        return name.equals(task.getName()) && description.equals(task.getDescription());
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Description: " + description;
    }

    @Override
    public int compareTo(@NonNull Task t) {
        return name.compareTo(t.name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(description);
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
        name = in.readString();
        description = in.readString();
    }

}
