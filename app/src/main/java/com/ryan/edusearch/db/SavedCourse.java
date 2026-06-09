package com.ryan.edusearch.db;

import com.ryan.edusearch.model.Course;

// Simple POJO untuk kursus yang disimpan dari SQLite
public class SavedCourse extends Course {
    private String savedId;
    private String savedTitle;
    private String savedHeadline;
    private String savedPrice;
    private String savedImageUrl;
    private float savedRating;
    private int savedLectures;
    private String savedInstructor;
    private String savedUrl;

    public SavedCourse(String id, String title, String headline, String price,
                       String imageUrl, float rating, int lectures, String instructor, String url) {
        this.savedId = id;
        this.savedTitle = title;
        this.savedHeadline = headline;
        this.savedPrice = price;
        this.savedImageUrl = imageUrl;
        this.savedRating = rating;
        this.savedLectures = lectures;
        this.savedInstructor = instructor;
        this.savedUrl = url;
        setSaved(true);
    }

    @Override public String getId() { return savedId; }
    @Override public String getTitle() { return savedTitle; }
    @Override public String getHeadline() { return savedHeadline; }
    @Override public String getPrice() { return savedPrice; }
    @Override public String getImageUrl() { return savedImageUrl; }
    @Override public float getAvgRating() { return savedRating; }
    @Override public int getNumLectures() { return savedLectures; }
    @Override public String getInstructorName() { return savedInstructor; }
    @Override public String getUrl() { return savedUrl; }
}
