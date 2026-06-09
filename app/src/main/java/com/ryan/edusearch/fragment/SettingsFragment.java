package com.ryan.edusearch.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.ryan.edusearch.R;
import com.ryan.edusearch.activity.MainActivity;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("EduPref", android.content.Context.MODE_PRIVATE);

        Switch switchDark = view.findViewById(R.id.switch_dark_mode);
        TextView tvVersion = view.findViewById(R.id.tv_version);
        Button btnClearSaved = view.findViewById(R.id.btn_clear_saved);

        switchDark.setChecked(prefs.getBoolean("dark_mode", false));
        tvVersion.setText("EduSearch v1.0 — Tema: Pendidikan");

        switchDark.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            // Restart MainActivity agar tema teraplikasi
            requireActivity().recreate();
        });

        btnClearSaved.setOnClickListener(v -> {
            com.ryan.edusearch.db.CourseDbHelper db =
                    new com.ryan.edusearch.db.CourseDbHelper(requireContext());
            for (com.ryan.edusearch.model.Course c : db.getSavedCourses()) {
                db.deleteCourse(c.getId());
            }
            Toast.makeText(getContext(), "Semua kursus tersimpan dihapus", Toast.LENGTH_SHORT).show();
        });
    }
}
