package com.chorded.app.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.chorded.app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // При первом открытии показываем список песен
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new SongsFragment())
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            if (item.getItemId() == R.id.nav_chords) {
                selected = new ChordsFragment();
            }
            else if (item.getItemId() == R.id.nav_recommendations) {
                selected = new RecommendationsFragment();
            }
            else if (item.getItemId() == R.id.nav_songs) {
                selected = new SongsFragment();
            }
            else if (item.getItemId() == R.id.nav_profile) {
                selected = new ProfileFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, selected)
                    .commit();

            return true;
        });

    }
}
