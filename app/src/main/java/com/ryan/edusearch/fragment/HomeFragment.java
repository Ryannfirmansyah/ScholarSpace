package com.ryan.edusearch.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ryan.edusearch.R;
import com.ryan.edusearch.activity.DetailActivity;
import com.ryan.edusearch.adapter.CourseAdapter;
import com.ryan.edusearch.db.CourseDbHelper;
import com.ryan.edusearch.model.Course;
import com.ryan.edusearch.model.CourseResponse;
import com.ryan.edusearch.network.ApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String API_KEY = "874f31c739msh89718c96aa3af87p1c731ejsn238ce3f29621";
    private static final String API_HOST = "udemy-paid-courses-for-free-api.p.rapidapi.com";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvError;
    private Button btnRetry;
    private CourseAdapter adapter;
    private final List<Course> courseList = new ArrayList<>();
    private CourseDbHelper dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.rv_courses);
        progressBar = view.findViewById(R.id.progress_bar);
        tvError = view.findViewById(R.id.tv_error);
        btnRetry = view.findViewById(R.id.btn_retry);
        dbHelper = new CourseDbHelper(requireContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CourseAdapter(getContext(), courseList, new CourseAdapter.OnCourseListener() {
            @Override
            public void onCourseClick(Course course) { openDetail(course); }
            @Override
            public void onSaveToggle(Course course, int position) { toggleSave(course, position); }
        });
        recyclerView.setAdapter(adapter);

        btnRetry.setOnClickListener(v -> fetchCourses());
        fetchCourses();
    }

    private void fetchCourses() {
        showLoading();
        ApiClient.getService().getCourses(API_KEY, API_HOST, 1, 20, "programming", "en", null)
                .enqueue(new Callback<CourseResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CourseResponse> call,
                                           @NonNull Response<CourseResponse> response) {
                        if (response.isSuccessful() && response.body() != null &&
                                response.body().getResults() != null) {
                            List<Course> results = response.body().getResults();
                            // Cek status saved di background thread
                            executor.execute(() -> {
                                for (Course c : results) {
                                    c.setSaved(dbHelper.isSaved(c.getId()));
                                }
                                handler.post(() -> {
                                    courseList.clear();
                                    courseList.addAll(results);
                                    adapter.notifyDataSetChanged();
                                    showContent();
                                });
                            });
                        } else {
                            loadFromDb();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<CourseResponse> call, @NonNull Throwable t) {
                        loadFromDb();
                    }
                });
    }

    private void loadFromDb() {
        executor.execute(() -> {
            List<Course> saved = dbHelper.getSavedCourses();
            handler.post(() -> {
                if (saved.isEmpty()) {
                    showError("Tidak ada koneksi & belum ada data tersimpan.");
                } else {
                    courseList.clear();
                    courseList.addAll(saved);
                    adapter.notifyDataSetChanged();
                    showContent();
                    Toast.makeText(getContext(), "Menampilkan data offline", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void toggleSave(Course course, int position) {
        executor.execute(() -> {
            if (dbHelper.isSaved(course.getId())) {
                dbHelper.deleteCourse(course.getId());
                course.setSaved(false);
            } else {
                dbHelper.saveCourse(course);
                course.setSaved(true);
            }
            handler.post(() -> adapter.notifyItemChanged(position));
        });
    }

    private void openDetail(Course course) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_ID, course.getId());
        intent.putExtra(DetailActivity.EXTRA_TITLE, course.getTitle());
        intent.putExtra(DetailActivity.EXTRA_INSTRUCTOR, course.getInstructorName());
        intent.putExtra(DetailActivity.EXTRA_HEADLINE, course.getHeadline());
        intent.putExtra(DetailActivity.EXTRA_PRICE, course.getPrice());
        intent.putExtra(DetailActivity.EXTRA_IMAGE, course.getImageUrl());
        intent.putExtra(DetailActivity.EXTRA_RATING, course.getAvgRating());
        intent.putExtra(DetailActivity.EXTRA_LECTURES, course.getNumLectures());
        intent.putExtra(DetailActivity.EXTRA_URL, course.getUrl());
        startActivity(intent);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
    }

    private void showError(String msg) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(msg);
        btnRetry.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
