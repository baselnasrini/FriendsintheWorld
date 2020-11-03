package com.e.friendsintheworld;

import android.widget.ArrayAdapter;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e.friendsintheworld.models.Group;
import com.e.friendsintheworld.models.Pin;
import com.e.friendsintheworld.models.RegisteredGroup;

import java.util.ArrayList;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<ArrayAdapter<Group>> mGroups;
    private MutableLiveData<ArrayList<RegisteredGroup>> mRegisteredGroups;
    private MutableLiveData<Long> mLocationsUpdated;

    public SharedViewModel () {
        mGroups = new MutableLiveData<>();
        mRegisteredGroups = new MutableLiveData<>();
        mLocationsUpdated = new MutableLiveData<>();
    }

    public MutableLiveData<ArrayAdapter<Group>> getGroups() {
        if(mGroups == null){
            mGroups = new MutableLiveData<>();
        }
        return mGroups;
    }

    public void setGroups(ArrayAdapter<Group> groups) {
        mGroups.postValue(groups);
    }

    public MutableLiveData<ArrayList<RegisteredGroup>> getRegisteredGroups(){
        if(mRegisteredGroups == null){
            mRegisteredGroups = new MutableLiveData<>();
        }
        return mRegisteredGroups;
    }

    public void setRegisteredGroups(ArrayList<RegisteredGroup> registeredGroups){
        mRegisteredGroups.postValue(registeredGroups);
    }

    public boolean checkIfRegistered(String group){
        if (mRegisteredGroups.getValue() != null){
            ArrayList<RegisteredGroup> registeredGroupArr = mRegisteredGroups.getValue();
            for (int i=0 ; i< registeredGroupArr.size() ; i++){
                if (registeredGroupArr.get(i).getGroupName().equals(group))
                    return true;
            }
        }
        return false;
    }

    public void unregister(String id){
        ArrayList<RegisteredGroup> registeredGroupArr = mRegisteredGroups.getValue();
        for (int i=0 ; i< registeredGroupArr.size() ; i++){
            if (registeredGroupArr.get(i).getID().equals(id)){
                registeredGroupArr.remove(i);
                break;
            }
        }
        mRegisteredGroups.postValue(registeredGroupArr);
    }

    public void addLocations(String group, ArrayList<Pin> pinsArr){
        ArrayList<RegisteredGroup> registeredGroupArr = mRegisteredGroups.getValue();

        for (int i=0 ; i< registeredGroupArr.size() ; i++){
            if (registeredGroupArr.get(i).getGroupName().equals(group)){

                registeredGroupArr.get(i).setMembersLocations(pinsArr);
                break;
            }
        }
        mRegisteredGroups.postValue(registeredGroupArr);
        setLocationsUpdated(System.currentTimeMillis());
    }

    public void addRegisteredGroup(String group, String id){
        ArrayList<RegisteredGroup> groupsArr = new ArrayList<>();

        if(mRegisteredGroups.getValue() != null){
            groupsArr = mRegisteredGroups.getValue();
        }

        groupsArr.add(new RegisteredGroup(group, id));

        mRegisteredGroups.postValue(groupsArr);
    }

    public String getIdOf (String group){
        ArrayList<RegisteredGroup> registeredGroupArr = mRegisteredGroups.getValue();

        for (int i=0 ; i < registeredGroupArr.size() ; i++){

            if (registeredGroupArr.get(i).getGroupName().equals(group)){
                return registeredGroupArr.get(i).getID();
            }
        }
        return "" ;
    }

    public MutableLiveData<Long> getLocationsUpdated() {
        return mLocationsUpdated;
    }

    public void setLocationsUpdated(long locationsUpdated) {
        mLocationsUpdated.postValue(locationsUpdated);
    }

    public boolean getIfShownOnMap(String groupName){
        ArrayList<RegisteredGroup> registeredGroupArr = mRegisteredGroups.getValue();
        for (int i=0 ; i < registeredGroupArr.size() ; i++){

            if (registeredGroupArr.get(i).getGroupName().equals(groupName)){
                if (registeredGroupArr.get(i).isShowOnMap()){
                    return true;
                }
            }
        }
        return false;
    }
}
