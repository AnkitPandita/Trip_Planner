package edu.uncc.cci.mobileapps.hw07_group25;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class Message {
    String id;
    String senderEmail;
    String body;
    Timestamp timestamp;
    boolean photo;

    public Map<String, Object> toHashMap() {
        Map<String, Object> tripMap = new HashMap<>();
        tripMap.put("id", this.id);
        tripMap.put("senderEmail", this.senderEmail);
        tripMap.put("body", this.body);
        tripMap.put("timestamp", FieldValue.serverTimestamp());
        tripMap.put("photo", this.photo);
        return tripMap;
    }

    public Message(Map tripMap) {
        this.id = (String) tripMap.get("id");
        this.senderEmail = (String) tripMap.get("senderEmail");
        this.body = (String) tripMap.get("body");
        this.timestamp = (Timestamp) tripMap.get("timestamp");
        this.photo = (boolean) tripMap.get("photo");
    }

    public Message() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isPhoto() {
        return photo;
    }

    public void setPhoto(boolean photo) {
        this.photo = photo;
    }
}