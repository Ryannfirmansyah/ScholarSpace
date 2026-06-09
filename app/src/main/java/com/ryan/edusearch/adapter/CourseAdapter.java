package com.ryan.edusearch.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ryan.edusearch.R;
import com.ryan.edusearch.model.Course;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    public interface OnCourseListener {
        void onCourseClick(Course course);
        void onSaveToggle(Course course, int position);
    }

    private final Context context;
    private final List<Course> courses;
    private final OnCourseListener listener;

    public CourseAdapter(Context context, List<Course> courses, OnCourseListener listener) {
        this.context = context;
        this.courses = courses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Course c = courses.get(position);
        h.tvTitle.setText(c.getTitle());
        h.tvInstructor.setText(c.getInstructorName());
        h.tvPrice.setText(c.isPaid() ? c.getPrice() : "🆓 Gratis");
        h.tvLectures.setText(c.getNumLectures() + " lectures");
        h.ratingBar.setRating(c.getAvgRating());
        h.tvRating.setText(String.format("%.1f", c.getAvgRating()));
        h.tvHeadline.setText(c.getHeadline());

        h.btnSave.setImageResource(c.isSaved() ?
                R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_border);

        if (!c.getImageUrl().isEmpty()) {
            Glide.with(context).load(c.getImageUrl())
                    .placeholder(R.drawable.ic_course_placeholder)
                    .into(h.ivThumbnail);
        } else {
            h.ivThumbnail.setImageResource(R.drawable.ic_course_placeholder);
        }

        h.itemView.setOnClickListener(v -> listener.onCourseClick(c));
        h.btnSave.setOnClickListener(v -> listener.onSaveToggle(c, position));
    }

    @Override
    public int getItemCount() { return courses.size(); }

    public void updateList(List<Course> newList) {
        courses.clear();
        courses.addAll(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        ImageButton btnSave;
        TextView tvTitle, tvInstructor, tvPrice, tvLectures, tvRating, tvHeadline;
        RatingBar ratingBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            btnSave = itemView.findViewById(R.id.btn_save);
            tvTitle = itemView.findViewById(R.id.tv_course_title);
            tvInstructor = itemView.findViewById(R.id.tv_instructor);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvLectures = itemView.findViewById(R.id.tv_lectures);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvHeadline = itemView.findViewById(R.id.tv_headline);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }
    }
}
