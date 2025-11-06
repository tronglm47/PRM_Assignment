package com.example.prm_assignment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.prm_assignment.ui.fragments.BookingFragment;
import com.example.prm_assignment.ui.fragments.ChatFragment;
import com.example.prm_assignment.ui.fragments.HomeFragment;
import com.example.prm_assignment.ui.fragments.ProfileFragment;
import com.example.prm_assignment.ui.fragments.SubscriptionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                switchTo(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_chat) {
                switchTo(new ChatFragment());
                return true;
            } else if (itemId == R.id.nav_subscription) {
                switchTo(new SubscriptionFragment());
                return true;
            } else if (itemId == R.id.nav_booking) {
                switchTo(new BookingFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchTo(new ProfileFragment());
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Logout is handled in ProfileFragment with logout button
    }

    private void switchTo(@NonNull Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.fragment_container, fragment);
        tx.commit();
    }
}

