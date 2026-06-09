package com.ryan.edusearch.model;

import com.google.gson.annotations.SerializedName;

public class Instructor {
    @SerializedName("display_name")
    private String displayName;

    public String getDisplayName() { return displayName != null ? displayName : ""; }
}
