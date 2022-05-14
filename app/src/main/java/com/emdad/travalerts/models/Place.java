package com.emdad.travalerts.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.GeoPoint;

import java.util.UUID;

@IgnoreExtraProperties
public class Place implements Parcelable {
    private String id;
    private String name;
    private String description;
    private String image;
    private String address;
    private GeoPoint geo_location;
    private int range;
    private int rating;
    private String userId;
    private String crated_at;
    private String updated_at;

    public Place() {
    }

    public Place(String id, String name, String description, String image, String address, GeoPoint geo_location, int range, int rating, String userId, String crated_at, String updated_at) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.image = image;
        this.address = address;
        this.geo_location = geo_location;
        this.range = range;
        this.rating = rating;
        this.userId = userId;
        this.crated_at = crated_at;
        this.updated_at = updated_at;
    }

    protected Place(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        image = in.readString();
        address = in.readString();
        range = in.readInt();
        rating = in.readInt();
        userId = in.readString();
        crated_at = in.readString();
        updated_at = in.readString();
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public GeoPoint getGeo_location() {
        return geo_location;
    }

    public void setGeo_location(GeoPoint geo_location) {
        this.geo_location = geo_location;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCrated_at() {
        return crated_at;
    }

    public void setCrated_at(String crated_at) {
        this.crated_at = crated_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "Place{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", address='" + address + '\'' +
                ", geo_location='" + geo_location + '\'' +
                ", range=" + range +
                ", rating='" + rating + '\'' +
                ", userId='" + userId + '\'' +
                ", crated_at='" + crated_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(image);
        parcel.writeString(address);
        parcel.writeInt(range);
        parcel.writeInt(rating);
        parcel.writeString(userId);
        parcel.writeString(crated_at);
        parcel.writeString(updated_at);
    }
}
