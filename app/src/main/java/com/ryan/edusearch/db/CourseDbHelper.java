package com.ryan.edusearch.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ryan.edusearch.model.Course;

import java.util.ArrayList;
import java.util.List;

public class CourseDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "edusearch.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "saved_courses";

    public CourseDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                "id TEXT PRIMARY KEY, title TEXT, headline TEXT, " +
                "price TEXT, image_url TEXT, avg_rating REAL, " +
                "num_lectures INTEGER, instructor TEXT, url TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void saveCourse(Course course) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", course.getId());
        cv.put("title", course.getTitle());
        cv.put("headline", course.getHeadline());
        cv.put("price", course.getPrice());
        cv.put("image_url", course.getImageUrl());
        cv.put("avg_rating", course.getAvgRating());
        cv.put("num_lectures", course.getNumLectures());
        cv.put("instructor", course.getInstructorName());
        cv.put("url", course.getUrl());
        db.insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void deleteCourse(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, "id=?", new String[]{id});
        db.close();
    }

    public boolean isSaved(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM " + TABLE + " WHERE id=?", new String[]{id});
        boolean exists = c.getCount() > 0;
        c.close();
        db.close();
        return exists;
    }

    public List<Course> getSavedCourses() {
        List<Course> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE, null);
        if (c.moveToFirst()) {
            do {
                SavedCourse sc = new SavedCourse(
                        c.getString(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("title")),
                        c.getString(c.getColumnIndexOrThrow("headline")),
                        c.getString(c.getColumnIndexOrThrow("price")),
                        c.getString(c.getColumnIndexOrThrow("image_url")),
                        c.getFloat(c.getColumnIndexOrThrow("avg_rating")),
                        c.getInt(c.getColumnIndexOrThrow("num_lectures")),
                        c.getString(c.getColumnIndexOrThrow("instructor")),
                        c.getString(c.getColumnIndexOrThrow("url"))
                );
                list.add(sc);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }
}
