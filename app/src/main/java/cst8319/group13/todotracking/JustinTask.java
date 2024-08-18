package cst8319.group13.todotracking;

import android.os.Parcel;
import android.os.Parcelable;

public class JustinTask implements Parcelable {
    public String DueDate;
    public String Notes;
    public String TaskName;
    public int UserId;
    public String TaskId;
    public boolean checked;
    public boolean remindme;

    public JustinTask() {
    }

    public JustinTask(String dueDate, String notes, String taskName, int userId, String TaskId, boolean remindme) {
        this.DueDate = dueDate;
        this.Notes = notes;
        this.TaskName = taskName;
        this.UserId = userId;
        this.TaskId = TaskId;
        this.checked = false;
        this.remindme = remindme;
    }

    protected JustinTask(Parcel in) {
        DueDate = in.readString();
        Notes = in.readString();
        TaskName = in.readString();
        UserId = in.readInt();
        TaskId = in.readString();
        remindme = in.readByte() != 0;
    }

    public static final Creator<JustinTask> CREATOR = new Creator<JustinTask>() {
        @Override
        public JustinTask createFromParcel(Parcel in) {
            return new JustinTask(in);
        }

        @Override
        public JustinTask[] newArray(int size) {
            return new JustinTask[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(DueDate);
        parcel.writeString(Notes);
        parcel.writeString(TaskName);
        parcel.writeInt(UserId);
        parcel.writeString(TaskId);
        parcel.writeByte((byte) (remindme ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }
}