package com.chorded.app.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.chorded.app.session.AppSession;
import com.chorded.app.R;
import com.chorded.app.main.ChordsFragment;
import com.chorded.app.main.ProfileFragment;
import com.chorded.app.main.RecommendationsFragment;
import com.chorded.app.main.SongsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppSession.get().init(this);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        openFragment(new ChordsFragment());

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment = null;

            if (id == R.id.nav_chords) fragment = new ChordsFragment();
            else if (id == R.id.nav_songs) fragment = new SongsFragment();
            else if (id == R.id.nav_recommendations) fragment = new RecommendationsFragment();
            else if (id == R.id.nav_profile) fragment = new ProfileFragment();

            if (fragment != null) openFragment(fragment);
            return true;
        });
    }

    private void openFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
