package com.ryan.edusearch.network;

import com.ryan.edusearch.model.CourseResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface UdemyApiService {
    @GET("courses/")
    Call<CourseResponse> getCourses(
            @Header("X-RapidAPI-Key") String apiKey,
            @Header("X-RapidAPI-Host") String apiHost,
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @Query("search") String search,
            @Query("language") String language,
            @Query("price") String price
    );
}
