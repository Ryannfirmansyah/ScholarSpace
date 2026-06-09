package com.ryan.edusearch.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CourseResponse {
    @SerializedName("count")
    private int count;
    @SerializedName("results")
    private List<Course> results;

    public int getCount() { return count; }
    public List<Course> getResults() { return results; }
}
