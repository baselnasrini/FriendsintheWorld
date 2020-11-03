package com.e.friendsintheworld.controllers;

import androidx.fragment.app.Fragment;

import com.e.friendsintheworld.MessageHandler;
import com.e.friendsintheworld.TCPConnection;
import com.e.friendsintheworld.ui.ChatFragment;
import com.e.friendsintheworld.ui.GroupsFragment;

public class GroupsController {
    private MainController mainController;
    private GroupsFragment groupsFragment;
    private TCPConnection connection;
    private boolean chatOpened;

    public GroupsController(MainController mainController, TCPConnection connection){
        this.mainController = mainController;
        this.connection = connection;
        this.groupsFragment = new GroupsFragment();
        this.groupsFragment.setController(this);
        connection.send(MessageHandler.MSG_TYPE_GROUPS,null);
    }

    public Fragment getFragment() {
        return this.groupsFragment;
    }

    public void addNewGroup() {
        // TODO username
        connection.send(MessageHandler.MSG_TYPE_REGISTER, new String[]{groupsFragment.getNewGroupTitle(), "Basel"});
        connection.send(MessageHandler.MSG_TYPE_GROUPS,null);
    }


    public boolean isChatOpened() {
        return chatOpened;
    }

}
