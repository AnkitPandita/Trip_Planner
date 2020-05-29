package edu.uncc.cci.mobileapps.hw07_group25;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Trip implements Parcelable {
    String id;
    String title;
    double latitude;
    double longitude;
    String coverImageUrl;
    String admin;
    ArrayList<String> users;

    protected Trip(Parcel in) {
        id = in.readString();
        title = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        coverImageUrl = in.readString();
        admin = in.readString();
        users = in.createStringArrayList();
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    public Map<String, Object> toHashMap() {
        Map<String, Object> tripMap = new HashMap<>();
        tripMap.put("id", this.id);
        tripMap.put("title", this.title);
        tripMap.put("latitude", this.latitude);
        tripMap.put("longitude", this.longitude);
        tripMap.put("coverImageUrl", this.coverImageUrl);
        tripMap.put("admin", this.admin);
        String[] item = new String[this.users.size()];
        for (int i = 0; i < this.users.size(); i++) {
            item[i] = this.users.get(i);
        }
        tripMap.put("users", FieldValue.arrayUnion(item));
        return tripMap;
    }

    public Trip(Map tripMap) {
        this.id = (String) tripMap.get("id");
        this.title = (String) tripMap.get("title");
        this.latitude = (double) tripMap.get("latitude");
        this.longitude = (double) tripMap.get("longitude");
        this.coverImageUrl = (String) tripMap.get("coverImageUrl");
        this.admin = (String) tripMap.get("admin");
        this.users = (ArrayList<String>) tripMap.get("users");
    }

    public Trip() {
        this.users = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(coverImageUrl);
        parcel.writeString(admin);
        parcel.writeStringList(users);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    @Override
    public boolean equals(Object anObject) {
        if (!(anObject instanceof Trip)) {
            return false;
        }
        Trip otherMember = (Trip) anObject;
        return otherMember.getId().equals(getId());
    }
}
