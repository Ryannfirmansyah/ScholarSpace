package com.ryan.edusearch.model;

import com.google.gson.annotations.SerializedName;

public class Course {
    @SerializedName("id")
    private String id;
    @SerializedName("title")
    private String title;
    @SerializedName("url")
    private String url;
    @SerializedName("paid")
    private boolean paid;
    @SerializedName("price")
    private String price;
    @SerializedName("headline")
    private String headline;
    @SerializedName("num_lectures")
    private int numLectures;
    @SerializedName("num_subscribers")
    private int numSubscribers;
    @SerializedName("avg_rating")
    private float avgRating;
    @SerializedName("image_480x270")
    private String imageUrl;
    @SerializedName("visible_instructors")
    private java.util.List<Instructor> instructors;

    private boolean isSaved = false;

    public String getId() { return id; }
    public String getTitle() { return title != null ? title : ""; }
    public String getUrl() { return url != null ? url : ""; }
    public boolean isPaid() { return paid; }
    public String getPrice() { return price != null ? price : "Free"; }
    public String getHeadline() { return headline != null ? headline : ""; }
    public int getNumLectures() { return numLectures; }
    public int getNumSubscribers() { return numSubscribers; }
    public float getAvgRating() { return avgRating; }
    public String getImageUrl() { return imageUrl != null ? imageUrl : ""; }
    public java.util.List<Instructor> getInstructors() { return instructors; }
    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { isSaved = saved; }

    public String getInstructorName() {
        if (instructors != null && !instructors.isEmpty()) {
            return instructors.get(0).getDisplayName();
        }
        return "Unknown";
    }
}
