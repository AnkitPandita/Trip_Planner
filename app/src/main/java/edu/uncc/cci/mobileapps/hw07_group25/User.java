package edu.uncc.cci.mobileapps.hw07_group25;

import java.util.HashMap;
import java.util.Map;

public class User {
    String id;
    String fname;
    String lname;
    String gender;
    String avatarUrl;

    public Map<String, Object> toHashMap() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", this.id);
        userMap.put("fname", this.fname);
        userMap.put("lname", this.lname);
        userMap.put("gender", this.gender);
        userMap.put("avatarUrl", this.avatarUrl);
        return userMap;
    }

    public User(Map userMap) {
        this.id = (String) userMap.get("id");
        this.fname = (String) userMap.get("fname");
        this.lname = (String) userMap.get("lname");
        this.gender = (String) userMap.get("gender");
        this.avatarUrl = (String) userMap.get("avatarUrl");
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public boolean equals(Object anObject) {
        if (!(anObject instanceof User)) {
            return false;
        }
        User otherMember = (User) anObject;
        return otherMember.getId().equals(getId());
    }
}
