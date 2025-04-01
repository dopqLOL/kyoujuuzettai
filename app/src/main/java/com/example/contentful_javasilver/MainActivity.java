package com.example.contentful_javasilver;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.example.contentful_javasilver.data.QuizDao;
import com.example.contentful_javasilver.data.QuizDatabase;
import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.databinding.ActivityMainBinding;
import com.example.contentful_javasilver.utils.SecurePreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        MaterialToolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        // Define top-level destinations for AppBarConfiguration
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment // Only Home is top-level now
        ).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();
            String destLabel = destination.getLabel() != null ? destination.getLabel().toString() : "No Label";
            NavDestination currentDest = controller.getCurrentDestination();
            int currentId = currentDest != null ? currentDest.getId() : -1;
            String currentLabel = (currentDest != null && currentDest.getLabel() != null) ? currentDest.getLabel().toString() : "None";

            Log.d("MainActivity", "Navigating from: " + currentLabel + " (" + currentId + ") to: " + destLabel + " (" + destId + ")");

            if (destId == R.id.startFragment || destId == R.id.loadingFragment) {
                Log.d("MainActivity", "Hiding Toolbar and Bottom Navigation for " + destLabel);
                binding.appBarLayout.setVisibility(View.GONE);
                binding.bottomNavigation.setVisibility(View.GONE);
            } else {
                Log.d("MainActivity", "Showing Toolbar and Bottom Navigation for " + destLabel);
                binding.appBarLayout.setVisibility(View.VISIBLE);
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            }
        });

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        Log.d("MainActivity", "Standard Bottom Navigation setup complete.");

        String apiKey = BuildConfig.CONTENTFUL_ACCESS_TOKEN;
        String spaceId = BuildConfig.CONTENTFUL_SPACE_ID;
        SecurePreferences.initializeSecureKeys(getApplicationContext(), apiKey, spaceId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d("MainActivity", "onSupportNavigateUp called."); // Add log
        NavDestination currentDestination = navController.getCurrentDestination();
        if (currentDestination != null) { // Add null check log
             Log.d("MainActivity", "Current Destination ID: " + currentDestination.getId() + ", Label: " + currentDestination.getLabel());
        } else {
             Log.d("MainActivity", "Current Destination is null.");
        }

        if (currentDestination != null &&
                (currentDestination.getId() == R.id.navigation_history ||
                 currentDestination.getId() == R.id.navigation_bookmark ||
                 currentDestination.getId() == R.id.problemListFragment ||
                 currentDestination.getId() == R.id.chapterFragment)) { // Also handle chapterFragment explicitly
            Log.d("MainActivity", "Navigating to HomeFragment explicitly for ID: " + currentDestination.getId()); // Add log
            // If on a bottom nav screen (other than home), problem list, or chapter, navigate to home
            // Use NavOptions to pop up to homeFragment to avoid building up the back stack
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true) // Pop back to home, inclusive means home itself is popped if already there, then re-added
                    .build();
            navController.navigate(R.id.homeFragment, null, navOptions);
            return true; // Indicate navigation was handled
        } else {
            Log.d("MainActivity", "Using default navigateUp behavior."); // Add log
            // Otherwise, use the default navigateUp behavior
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.homeFragment // Only Home is top-level
            ).build();
            return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
        }
    }

}
