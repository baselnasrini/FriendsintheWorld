package com.e.friendsintheworld.models;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

public class Group implements Parcelable {
    private String groupName;
    private boolean isUserMember;
    private boolean isShownOnMap;

    public Group(String groupName, boolean isUserMember, boolean isShownOnMap) {
        this.groupName = groupName;
        this.isUserMember = isUserMember;
        this.isShownOnMap = isShownOnMap;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isUserMember() {
        return isUserMember;
    }

    public void setUserMember(boolean userMember) {
        isUserMember = userMember;
    }

    public boolean isShownOnMap() {
        return isShownOnMap;
    }

    public void setIsShownOnMap(boolean isShownOnMap) {
        this.isShownOnMap = isShownOnMap;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dest.writeBoolean(isUserMember);
            dest.writeBoolean(isShownOnMap);
        }
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    protected Group(Parcel in) {
        groupName = in.readString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isShownOnMap = in.readBoolean();
            isUserMember = in.readBoolean();
        }
    }
}
