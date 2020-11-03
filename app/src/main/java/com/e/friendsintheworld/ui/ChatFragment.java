package com.e.friendsintheworld.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.e.friendsintheworld.R;

public class ChatFragment extends Fragment {
    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        setRetainInstance(true);


        initializeComponents(view);
        registerListeners();

        return view;    }

    private void initializeComponents(View view) {
    }

    private void registerListeners() {
    }
}