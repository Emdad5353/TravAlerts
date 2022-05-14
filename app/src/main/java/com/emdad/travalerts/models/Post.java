package com.emdad.travalerts.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.UUID;

@IgnoreExtraProperties
public class Post {
    private String id;
    private String place_id;
    private String user_id;
    private String comment;
    private String image;
    private String video;
    private float rating;
    private boolean isVisitor;
    private long created_at;
    private long updated_at;

    public Post() {
    }

    public Post(String id, String place_id, String user_id, String comment, String image, String video, float rating, boolean isVisitor, long created_at, long updated_at) {
        this.id = id;
        this.place_id = place_id;
        this.user_id = user_id;
        this.comment = comment;
        this.image = image;
        this.video = video;
        this.rating = rating;
        this.isVisitor = isVisitor;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isVisitor() {
        return isVisitor;
    }

    public void setVisitor(boolean visitor) {
        isVisitor = visitor;
    }

    public long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", place_id='" + place_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", comment='" + comment + '\'' +
                ", image='" + image + '\'' +
                ", video='" + video + '\'' +
                ", rating=" + rating +
                ", isVisitor=" + isVisitor +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}
