package com.ryan.edusearch.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.ryan.edusearch.R;
import com.ryan.edusearch.db.CourseDbHelper;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_INSTRUCTOR = "instructor";
    public static final String EXTRA_HEADLINE = "headline";
    public static final String EXTRA_PRICE = "price";
    public static final String EXTRA_IMAGE = "image";
    public static final String EXTRA_RATING = "rating";
    public static final String EXTRA_LECTURES = "lectures";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent i = getIntent();
        String id = i.getStringExtra(EXTRA_ID);
        String title = i.getStringExtra(EXTRA_TITLE);
        String instructor = i.getStringExtra(EXTRA_INSTRUCTOR);
        String headline = i.getStringExtra(EXTRA_HEADLINE);
        String price = i.getStringExtra(EXTRA_PRICE);
        String image = i.getStringExtra(EXTRA_IMAGE);
        float rating = i.getFloatExtra(EXTRA_RATING, 0f);
        int lectures = i.getIntExtra(EXTRA_LECTURES, 0);
        String url = i.getStringExtra(EXTRA_URL);

        ImageView ivImg = findViewById(R.id.iv_detail_image);
        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvInstructor = findViewById(R.id.tv_detail_instructor);
        TextView tvHeadline = findViewById(R.id.tv_detail_headline);
        TextView tvPrice = findViewById(R.id.tv_detail_price);
        TextView tvLectures = findViewById(R.id.tv_detail_lectures);
        RatingBar ratingBar = findViewById(R.id.detail_rating_bar);
        TextView tvRating = findViewById(R.id.tv_detail_rating);
        Button btnOpen = findViewById(R.id.btn_open_course);
        Button btnSave = findViewById(R.id.btn_save_course);
        Button btnBack = findViewById(R.id.btn_detail_back);

        Glide.with(this).load(image).placeholder(R.drawable.ic_course_placeholder).into(ivImg);
        tvTitle.setText(title);
        tvInstructor.setText("Instruktur: " + instructor);
        tvHeadline.setText(headline);
        tvPrice.setText(price);
        tvLectures.setText(lectures + " lectures");
        ratingBar.setRating(rating);
        tvRating.setText(String.format("%.1f / 5.0", rating));

        CourseDbHelper db = new CourseDbHelper(this);
        updateSaveButton(btnSave, db.isSaved(id));

        btnSave.setOnClickListener(v -> {
            if (db.isSaved(id)) {
                db.deleteCourse(id);
                Toast.makeText(this, "Dihapus dari simpanan", Toast.LENGTH_SHORT).show();
            } else {
                // Simpan minimal data ke DB
                com.ryan.edusearch.db.SavedCourse sc = new com.ryan.edusearch.db.SavedCourse(
                        id, title, headline, price, image, rating, lectures, instructor, url);
                db.saveCourse(sc);
                Toast.makeText(this, "Kursus disimpan!", Toast.LENGTH_SHORT).show();
            }
            updateSaveButton(btnSave, db.isSaved(id));
        });

        btnOpen.setOnClickListener(v -> {
            if (url != null && !url.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.udemy.com" + url)));
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void updateSaveButton(Button btn, boolean saved) {
        btn.setText(saved ? "🗑️ Hapus Simpanan" : "🔖 Simpan Kursus");
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                saved ? 0xFFC62828 : 0xFF1565C0));
    }
}
