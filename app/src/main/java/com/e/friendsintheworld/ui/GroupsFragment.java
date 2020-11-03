package com.e.friendsintheworld.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.e.friendsintheworld.R;
import com.e.friendsintheworld.SharedViewModel;
import com.e.friendsintheworld.controllers.GroupsController;
import com.e.friendsintheworld.models.Group;

public class GroupsFragment extends Fragment {
    private GroupsController groupsController;
    private ListView lvGroups;
    private SharedViewModel sharedViewModel;
    private Button addGroupButton;
    private TextView tvNewGroup;
    private Button btnUnregister;

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        setRetainInstance(true);

        initializeComponents(view);
        registerListeners();

        return view;
    }

    private void initializeComponents(View view) {
        lvGroups = view.findViewById(R.id.lvGroups);
        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        addGroupButton = view.findViewById(R.id.btnAddNewGroup);
        tvNewGroup = view.findViewById(R.id.txtAddNewGroup);
        //btnUnregister = view.findViewById(R.id.btnUnregister);
    }

    private void registerListeners() {

        sharedViewModel.getGroups().observe(getViewLifecycleOwner(), new Observer<ArrayAdapter<Group>>() {
            @Override
            public void onChanged(ArrayAdapter<Group> groupsList) {
                lvGroups.setAdapter(groupsList);
            }
        });

        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                }
                if (getNewGroupTitle().length() > 0){
                    groupsController.addNewGroup();
                    tvNewGroup.setText("");
                }
            }
        });

    }

    public String getNewGroupTitle(){
        return tvNewGroup.getText().toString().trim();
    }

    public void setController(GroupsController groupsController) {
        this.groupsController = groupsController;
    }


}