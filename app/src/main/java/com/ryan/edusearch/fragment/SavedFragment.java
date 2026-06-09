package com.ryan.edusearch.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavedFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private CourseDbHelper dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.rv_saved);
        progressBar = view.findViewById(R.id.progress_saved);
        tvEmpty = view.findViewById(R.id.tv_empty_saved);
        dbHelper = new CourseDbHelper(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedCourses();
    }

    private void loadSavedCourses() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        executor.execute(() -> {
            List<Course> saved = dbHelper.getSavedCourses();
            handler.post(() -> {
                progressBar.setVisibility(View.GONE);
                if (saved.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    CourseAdapter adapter = new CourseAdapter(getContext(),
                            new ArrayList<>(saved), new CourseAdapter.OnCourseListener() {
                        @Override
                        public void onCourseClick(Course course) { openDetail(course); }
                        @Override
                        public void onSaveToggle(Course course, int position) {
                            executor.execute(() -> {
                                dbHelper.deleteCourse(course.getId());
                                handler.post(() -> loadSavedCourses());
                            });
                        }
                    });
                    recyclerView.setAdapter(adapter);
                }
            });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
