package com.chorded.app.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.chorded.app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Навигация по вкладкам
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // По умолчанию открывается вкладка "Аккорды"
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new ChordsFragment())
                .commit();

        // Обработка нажатий в нижнем меню
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int id = item.getItemId();
            if (id == R.id.nav_chords) selected = new ChordsFragment();
            else if (id == R.id.nav_recommendations) selected = new RecommendationsFragment();
            else if (id == R.id.nav_profile) selected = new ProfileFragment();

            if (selected != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, selected)
                        .commit();
            }
            return true;
        });
    }
}
