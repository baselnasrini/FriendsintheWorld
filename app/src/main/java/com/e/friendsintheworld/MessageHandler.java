package com.e.friendsintheworld;

import android.util.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;

public class MessageHandler {
    public static final String MSG_TYPE_EXCEPTION = "exception";
    public static final String MSG_TYPE_REGISTER = "register";
    public static final String MSG_TYPE_UNREGISTER = "unregister";
    public static final String MSG_TYPE_MEMBERS = "members";
    public static final String MSG_TYPE_GROUPS = "groups";
    public static final String MSG_TYPE_LOCATION = "location";
    public static final String MSG_TYPE_LOCATIONS = "locations";
    public static final String MSG_TYPE_TEXTCHAT = "textchat";
    public static final String MSG_TYPE_IMAGECHAT = "imagechat";
    public static final String MSG_TYPE_UPLOAD = "upload";

    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_GROUP = "group";
    public static final String ATTRIBUTE_GROUPS = "groups";
    public static final String ATTRIBUTE_GROUP_ID = "id";
    public static final String ATTRIBUTE_MEMBERS = "members";
    public static final String ATTRIBUTE_MESSAGE = "message";
    public static final String ATTRIBUTE_LOCATION = "location";
    public static final String ATTRIBUTE_LONGITUDE = "longitude";
    public static final String ATTRIBUTE_LATITUDE = "latitude";
    public static final String ATTRIBUTE_TEXT = "text";
    public static final String ATTRIBUTE_MEMBER = "member";
    public static final String ATTRIBUTE_IMAGE_ID = "imageid";
    public static final String ATTRIBUTE_PORT = "port";

    public static String JSONMessage(String type, String[] values) {
        String message = "";
        switch (type){
            case (MessageHandler.MSG_TYPE_GROUPS):
                message = requestGroupStr();
                break;
            case (MessageHandler.MSG_TYPE_REGISTER):
                message = requestRegisterStr(values);
                break;
            case (MessageHandler.MSG_TYPE_UNREGISTER):
                message = requestUnregisterStr(values);
                break;
            case (MessageHandler.ATTRIBUTE_LOCATION):
                message = requestLocationStr(values);
                break;
        }
        return message;
    }

    private static String requestLocationStr(String[] values) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value(MSG_TYPE_LOCATION)
                    .name("id").value(values[0])
                    .name("longitude").value(values[1])
                    .name("latitude").value(values[2])
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    private static String requestUnregisterStr(String[] values) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value(MSG_TYPE_UNREGISTER)
                    .name("id").value(values[0])
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    private static String requestRegisterStr( String[] values) {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name("type").value(MSG_TYPE_REGISTER)
                    .name("group").value(values[0])
                    .name("member").value(values[1])
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    private static String requestGroupStr() {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter( stringWriter );
        try {
            writer.beginObject()
                    .name(ATTRIBUTE_TYPE).value(MSG_TYPE_GROUPS)
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }
}
