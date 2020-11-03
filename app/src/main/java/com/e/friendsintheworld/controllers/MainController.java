package com.e.friendsintheworld.controllers;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.e.friendsintheworld.MainActivity;
import com.e.friendsintheworld.MessageHandler;
import com.e.friendsintheworld.R;
import com.e.friendsintheworld.SharedViewModel;
import com.e.friendsintheworld.TCPConnection;
import com.e.friendsintheworld.models.Group;
import com.e.friendsintheworld.models.Pin;
import com.e.friendsintheworld.models.RegisteredGroup;
import com.e.friendsintheworld.ui.ChatFragment;
import com.e.friendsintheworld.ui.MainFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainController {
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private MainActivity activity;
    private MainFragment mainFragment;
    private ServiceConnection serviceConn;
    private TCPConnection connection;
    private boolean bound = false;
    private Receive receive;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String currentLatitude;
    private String currentLongitude;
    private int FASTEST_INTERVAL = 20000;
    private Location currentLocation = null;
    private long locationUpdatedAt = Long.MIN_VALUE;
    private SharedViewModel sharedViewModel;
    private GroupsController groupsController;
    private Boolean rotation = false;
    private Boolean chatOpened = false;

    public MainController(MainActivity mainActivity, Boolean rotation) {
        this.activity = mainActivity;
        this.mainFragment = new MainFragment();
        this.mainFragment.setController(this);
        this.sharedViewModel = ViewModelProviders.of(this.activity).get(SharedViewModel.class);
        this.rotation = rotation;

        Intent intent = new Intent(activity, TCPConnection.class);
        intent.putExtra(TCPConnection.IP, "18.196.3.242");
        intent.putExtra(TCPConnection.PORT, "7117");
        activity.startService(intent);

        serviceConn = new ServiceConn();

        boolean result = activity.bindService(intent, serviceConn, 0);

        if (!result)
            Log.v("Controller-constructor", "No binding");

        locationManager = (LocationManager)this.activity.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                boolean sendLocation = false;

                if(currentLocation == null){
                    currentLocation = location;
                    locationUpdatedAt = System.currentTimeMillis();
                    sendLocation = true;
                } else {
                    long lastSent = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - locationUpdatedAt);
                    String locationLat = String.valueOf(location.getLatitude());
                    String locationLng = String.valueOf(location.getLongitude());
                    if (lastSent >= TimeUnit.MILLISECONDS.toSeconds(FASTEST_INTERVAL)  || ( !currentLatitude.equals(locationLat.substring(0, Math.min(locationLat.length(), 7)) ) || !currentLongitude.equals(locationLng.substring(0,Math.min(locationLng.length(),7) )) )){
                        currentLocation = location;
                        locationUpdatedAt = System.currentTimeMillis();
                        sendLocation = true;
                    }
                }

                if(sendLocation){
                    String lat = String.valueOf(currentLocation.getLatitude());
                    String lng = String.valueOf(currentLocation.getLongitude());

                    currentLatitude = lat.substring(0, Math.min(lat.length() , 7) );
                    currentLongitude = lng.substring(0,Math.min(lng.length() , 7) );
                    ArrayList<RegisteredGroup> registeredGroupsArr = sharedViewModel.getRegisteredGroups().getValue();
                    if (registeredGroupsArr != null){
                        for (int i=0 ; i<registeredGroupsArr.size(); i++){
                            connection.send(MessageHandler.MSG_TYPE_LOCATION, new String[]{registeredGroupsArr.get(i).getID(), currentLongitude , currentLatitude});

                            Log.v("location sent", registeredGroupsArr.get(i).getID() + "  " + currentLatitude + "  " + currentLongitude);
                        }
                    }

                    connection.send(MessageHandler.MSG_TYPE_GROUPS,null);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

    }

    public Fragment getMainFragment() {
        return this.mainFragment;
    }

    public MainActivity getActivity(){
        return this.activity;
    }

    public void setMap(){
        mainFragment.setMap();
    }

    public void onDestroy() {
        if (bound){
            if (sharedViewModel.getRegisteredGroups().getValue() != null){

                ArrayList<RegisteredGroup> registeredArr = sharedViewModel.getRegisteredGroups().getValue();

                for (int i=0 ; i<registeredArr.size(); i++){

                    connection.send(MessageHandler.MSG_TYPE_UNREGISTER, new String[]{registeredArr.get(i).getID()});
                }
            }

            connection.disconnect();
            activity.unbindService(serviceConn);
            receive.stopListener();
            locationManager.removeUpdates(locationListener);
            bound =false;
        }
    }

    public void showGroupsFragment() {
        groupsController = new GroupsController(this, connection);
        this.activity.setFragment(groupsController.getFragment(), true);
    }

    public void getCurrentLocation(int requestCode){
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION :
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, locationListener);
                }
                break;
        }
    }

    public void showChat(String groupName) {
        chatOpened = true;
        ChatFragment chatFragment = new ChatFragment();

        this.activity.setFragment(chatFragment, true);

    }

    private class ServiceConn implements ServiceConnection {
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            TCPConnection.LocalService ls = (TCPConnection.LocalService) binder;
            connection = ls.getService();
            connection.connect();

            receive = new Receive();
            receive.start();
            bound = true;

            if (rotation){
                if (sharedViewModel.getRegisteredGroups().getValue() != null){

                    ArrayList<RegisteredGroup> registeredArr = sharedViewModel.getRegisteredGroups().getValue();
                    sharedViewModel.setLocationsUpdated(System.currentTimeMillis());
                    sharedViewModel.setRegisteredGroups(new ArrayList<RegisteredGroup>());

                    for (int i=0 ; i<registeredArr.size(); i++){

                        connection.send(MessageHandler.MSG_TYPE_REGISTER, new String[]{registeredArr.get(i).getGroupName(), "Basel"});
                    }
                    registeredArr.clear();

                }

            }

            if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            }
            else {
                    getCurrentLocation(REQUEST_ACCESS_FINE_LOCATION);
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    }

    private class Receive extends Thread {
        private String result;

        public void stopListener() {
            interrupt();
            receive = null;
        }

        public void run() {
            try {
                while (receive != null) {
                    result = connection.receive();
                    if(!result.equals("EXCEPTION"))
                        readResult();
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
                Log.v("Receive MainCont Excep", e.toString());
            }
        }

        private void readResult() {
            try {
                Log.v("received Message", result);
                final JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray;
                String messageType = jsonObject.getString(MessageHandler.ATTRIBUTE_TYPE);
                Log.v("received Message type", messageType);

                switch (messageType){
                    case (MessageHandler.MSG_TYPE_GROUPS):
                        groupsReceived(jsonObject);
                        break;
                    case (MessageHandler.MSG_TYPE_REGISTER):
                        registeredReceived(jsonObject);
                        break;
                    case (MessageHandler.MSG_TYPE_UNREGISTER):
                        unregisterReceived(jsonObject);
                        break;
                    case (MessageHandler.MSG_TYPE_LOCATIONS):
                        locationsReceived(jsonObject);
                        break;
                    case (MessageHandler.MSG_TYPE_TEXTCHAT):
                        textChatReceived(jsonObject);
                        break;
                    case (MessageHandler.MSG_TYPE_EXCEPTION):
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(activity,  jsonObject.getString(MessageHandler.ATTRIBUTE_MESSAGE), Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;

                }
            } catch (JSONException e) {
                Log.v("Exception Read result", e.toString());
                e.printStackTrace();
            }

        }
    }

    private void textChatReceived(JSONObject jsonObject) throws JSONException {
        String groupName = jsonObject.getString(MessageHandler.ATTRIBUTE_GROUP);
        String senderName = jsonObject.getString(MessageHandler.ATTRIBUTE_MEMBER);
        String msgText = jsonObject.getString(MessageHandler.ATTRIBUTE_TEXT);
        Log.v("textChatReceived", chatOpened + "           " + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        if (chatOpened){
            Log.v("textChatReceived", msgText + "           " + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        }
    }

    private void locationsReceived(JSONObject jsonObject) throws JSONException {
        String groupName = jsonObject.getString(MessageHandler.ATTRIBUTE_GROUP);
        JSONArray jsonArray = jsonObject.getJSONArray(MessageHandler.ATTRIBUTE_LOCATION);
        ArrayList<Pin> membersLocationsArr = new ArrayList<>();

        for(int i=0; i<jsonArray.length();i++){
            String member = jsonArray.getJSONObject(i).getString(MessageHandler.ATTRIBUTE_MEMBER);
            String lat = jsonArray.getJSONObject(i).getString(MessageHandler.ATTRIBUTE_LATITUDE);
            String lng = jsonArray.getJSONObject(i).getString(MessageHandler.ATTRIBUTE_LONGITUDE);

            if (!lat.equals("NaN")){
                LatLng position = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                membersLocationsArr.add(new Pin(member, position));
            }
        }
        sharedViewModel.addLocations(groupName, membersLocationsArr);
    }

    private void unregisterReceived(JSONObject jsonObject) throws JSONException {
        String id = jsonObject.getString(MessageHandler.ATTRIBUTE_GROUP_ID);
        sharedViewModel.unregister(id);
    }

    private void registeredReceived(JSONObject jsonObject) throws JSONException {
        String groupName = jsonObject.getString(MessageHandler.ATTRIBUTE_GROUP);
        String id = jsonObject.getString(MessageHandler.ATTRIBUTE_GROUP_ID);
        sharedViewModel.addRegisteredGroup(groupName, id);

        if (currentLocation != null){
            connection.send(MessageHandler.MSG_TYPE_LOCATION, new String[]{id, String.valueOf(currentLocation.getLongitude()), String.valueOf(currentLocation.getLatitude())});
        }
    }

    private void groupsReceived(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray(MessageHandler.ATTRIBUTE_GROUPS);
            Group[] groupsArr = new Group[jsonArray.length()];

            for(int i=0; i<jsonArray.length();i++){
                String groupStr = jsonArray.getJSONObject(i).getString(MessageHandler.ATTRIBUTE_GROUP);
                Group group = new Group(groupStr , false, false);
                groupsArr[i] = group ;
            }

            sharedViewModel.setGroups(new GroupsAdapter(activity, groupsArr));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private class GroupsAdapter extends ArrayAdapter<Group> {
        private LayoutInflater inflater;

        public GroupsAdapter(Context context, Group[] groupsList) {
            super(context, R.layout.groups_row,groupsList);
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Group group = getItem(position);
            final String groupName = group.getGroupName();
            final ViewHolder holder;
            if(convertView==null) {
                convertView = inflater.inflate(R.layout.groups_row,parent,false);
                holder = new ViewHolder();
                holder.tvTitle = (TextView)convertView.findViewById(R.id.txtGroupTitle);
                holder.btnUnregister = convertView.findViewById(R.id.btnUnregister);
                holder.btnRegister = convertView.findViewById(R.id.btnRegister);
                holder.btnSend = convertView.findViewById(R.id.btnSend);
                holder.btnShowOnMap = convertView.findViewById(R.id.btnShow);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.tvTitle.setText(groupName.substring(0 , Math.min(group.getGroupName().length(), 25)));


            if (sharedViewModel.checkIfRegistered(groupName)){
                holder.btnUnregister.setVisibility(View.VISIBLE);
                holder.btnSend.setVisibility(View.VISIBLE);
                holder.btnShowOnMap.setVisibility(View.VISIBLE);
                Log.v("aaaaaaaaa" , sharedViewModel.getIfShownOnMap(groupName) + "  " + groupName);
                if (sharedViewModel.getIfShownOnMap(groupName)){
                    holder.btnShowOnMap.setChecked(true);
                } else{
                    holder.btnShowOnMap.setChecked(false);
                }
                holder.btnRegister.setVisibility(View.GONE);
                holder.btnUnregister.setTag(groupName);
                holder.btnRegister.setTag(groupName);

                holder.btnUnregister.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String id = sharedViewModel.getIdOf(groupName);
                        connection.send(MessageHandler.MSG_TYPE_UNREGISTER, new String[]{id});
                        connection.send(MessageHandler.MSG_TYPE_GROUPS,null);
                    }
                });

                holder.btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showChat(groupName);
                    }
                });

                holder.btnShowOnMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ArrayList<RegisteredGroup> registeredGroupArr = sharedViewModel.getRegisteredGroups().getValue();
                        for (int i=0 ; i<registeredGroupArr.size() ; i++){
                            if (registeredGroupArr.get(i).getGroupName().equals(groupName)){
                                if (holder.btnShowOnMap.isChecked()){
                                    Log.v("clickkked" , holder.btnShowOnMap.isChecked() + " ") ;

                                    registeredGroupArr.get(i).setShowOnMap(true);
                                } else{
                                    Log.v("clickkked" , holder.btnShowOnMap.isChecked() + " ") ;

                                    registeredGroupArr.get(i).setShowOnMap(false);
                                }
                                return;
                            }
                        }
                        sharedViewModel.setRegisteredGroups(registeredGroupArr);
                        registeredGroupArr.clear();
                        connection.send(MessageHandler.MSG_TYPE_GROUPS,null);
                    }
                });

            } else{
                holder.btnUnregister.setVisibility(View.GONE);
                holder.btnSend.setVisibility(View.GONE);
                holder.btnShowOnMap.setVisibility(View.GONE);
                holder.btnRegister.setVisibility(View.VISIBLE);

                holder.btnRegister.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connection.send(MessageHandler.MSG_TYPE_REGISTER, new String[]{groupName, "Basel"});
                        connection.send(MessageHandler.MSG_TYPE_GROUPS,null);
                    }
                });
            }


            return convertView;
        }

        private class ViewHolder {
            private TextView tvTitle;
            private Button btnUnregister;
            private Button btnRegister;
            private Button btnSend;
            private ToggleButton btnShowOnMap;


        }
    }

}
