package com.e.friendsintheworld.models;

import java.util.ArrayList;

public class RegisteredGroup {
    private String groupName;
    private String id;
    private ArrayList<Pin> membersLocations;
    private boolean showOnMap;

    public RegisteredGroup(String groupName, String id) {
        this.groupName = groupName;
        this.id = id;
        this.showOnMap = false;
    }


    public ArrayList<Pin> getMembersLocations(){
        return membersLocations;
    }

    public void setMembersLocations(ArrayList<Pin> membersLocations) {
        this.membersLocations = membersLocations;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public boolean isShowOnMap() {
        return showOnMap;
    }

    public void setShowOnMap(boolean showOnMap) {
        this.showOnMap = showOnMap;
    }
}
