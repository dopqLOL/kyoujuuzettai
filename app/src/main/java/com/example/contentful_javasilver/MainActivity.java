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
// import androidx.navigation.ui.NavigationUI; // Remove this import as we handle title manually
import androidx.navigation.ui.NavigationUI; // Keep for setupActionBarWithNavController initially, but disable title setting

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import android.widget.TextView; // Import TextView
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
        // Setup with NavController but we will handle the title manually
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        // Disable default title setting by NavigationUI
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }


        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();
            String destLabel = destination.getLabel() != null ? destination.getLabel().toString() : "No Label";
            NavDestination currentDest = controller.getCurrentDestination();
            int currentId = currentDest != null ? currentDest.getId() : -1;
            String currentLabel = (currentDest != null && currentDest.getLabel() != null) ? currentDest.getLabel().toString() : "None";

            Log.d("MainActivity", "Navigating from: " + currentLabel + " (" + currentId + ") to: " + destLabel + " (" + destId + ")");

            TextView customTitle = binding.toolbarTitleCustom; // Get custom title TextView

            if (destId == R.id.startFragment || destId == R.id.loadingFragment) {
                Log.d("MainActivity", "Hiding Toolbar and Bottom Navigation for " + destLabel);
                binding.appBarLayout.setVisibility(View.GONE);
                binding.bottomNavigation.setVisibility(View.GONE);
                customTitle.setVisibility(View.GONE); // Hide custom title as well
            } else {
                Log.d("MainActivity", "Showing Toolbar and Bottom Navigation for " + destLabel);
                binding.appBarLayout.setVisibility(View.VISIBLE);
                binding.bottomNavigation.setVisibility(View.VISIBLE);
                customTitle.setVisibility(View.VISIBLE); // Show custom title

                // Set the text of the custom title TextView
                if (destination.getLabel() != null) {
                    customTitle.setText(destination.getLabel());
                    Log.d("MainActivity", "Setting custom title to: " + destination.getLabel());
                } else {
                    customTitle.setText(""); // Clear title if no label
                    Log.d("MainActivity", "Setting empty custom title");
                 }

                 // Adjust start margin for the custom title TextView
                 // We want the home title to start where titles on other pages start (after the back arrow)
                 // Standard title start is often around 72dp when nav icon is present.
                 // The default margin in XML is 16dp.
                 android.view.ViewGroup.MarginLayoutParams layoutParams = (android.view.ViewGroup.MarginLayoutParams) customTitle.getLayoutParams();
                 if (destId == R.id.homeFragment) {
                     // Set margin to ~72dp for home fragment to align with title position when back arrow is present
                     int marginStartPixels = (int) (72 * getResources().getDisplayMetrics().density);
                     layoutParams.leftMargin = marginStartPixels;
                     Log.d("MainActivity", "Setting custom title start margin to 72dp for HomeFragment");
                 } else {
                     // Reset margin to the default 16dp for other fragments (as defined in XML)
                     int marginStartPixels = (int) (16 * getResources().getDisplayMetrics().density);
                     layoutParams.leftMargin = marginStartPixels;
                     Log.d("MainActivity", "Resetting custom title start margin to 16dp for " + destLabel);
                 }
                 customTitle.setLayoutParams(layoutParams); // Apply layout parameter changes


                 // Update BottomNavigationView selected item based on destination
                // Check if the destination ID matches one of the menu item IDs
                if (destId == R.id.homeFragment ||
                    destId == R.id.chapterFragment || // Assuming chapter is a main tab now based on menu
                    destId == R.id.navigation_history ||
                    destId == R.id.navigation_bookmark) {
                     // Check if the menu item exists before trying to select it
                     if (binding.bottomNavigation.getMenu().findItem(destId) != null) {
                         Log.d("MainActivity", "Updating BottomNav selection to: " + destLabel + " (" + destId + ")");
                         // Use setChecked to avoid re-triggering the listener
                         binding.bottomNavigation.getMenu().findItem(destId).setChecked(true);
                     } else {
                         Log.w("MainActivity", "Destination ID " + destId + " not found in BottomNav menu.");
                     }
                } else {
                     Log.d("MainActivity", "Destination " + destLabel + " not in BottomNav menu, selection unchanged.");
                }
            }
        });

        // NavigationUI.setupWithNavController(binding.bottomNavigation, navController); // Use standard setup - Replaced by manual listener below
        // Log.d("MainActivity", "Standard Bottom Navigation setup complete."); // Log setup

        // Add listener to prevent navigation when the same item is reselected
        // binding.bottomNavigation.setOnNavigationItemReselectedListener(item -> {
            // アイテムが再選択された場合は、何もしない (ナビゲーションを防ぐ)
        //    Log.d("MainActivity", "Item reselected: " + item.getTitle() + ", doing nothing.");
            // イベントを消費したことを示す必要はない (何もしないので)
        // });

        // Manual setup for BottomNavigationView item selection
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            NavDestination currentDestination = navController.getCurrentDestination();
            int currentDestId = (currentDestination != null) ? currentDestination.getId() : -1;

            // Prevent navigating to the same destination
            if (itemId == currentDestId) {
                Log.d("MainActivity", "Already on destination: " + item.getTitle());
                return false; // Do not consume the event, allow default behavior (e.g., reselection animation) if any
            }

            // Define NavOptions for top-level destinations to clear back stack
            NavOptions.Builder navOptionsBuilder = new NavOptions.Builder()
                    .setLaunchSingleTop(true) // Avoid multiple copies of the same destination
                    .setRestoreState(true); // Restore state when navigating back

            // Determine the start destination of the graph or a suitable top-level destination
            int startDestinationId = R.id.homeFragment;

            // Pop up to the start destination of the graph to avoid building up back stack.
            // For top-level destinations, pop up to the start destination.
            // Check if the target destination is one of the main bottom nav items
             if (itemId == R.id.homeFragment || itemId == R.id.navigation_history || itemId == R.id.navigation_bookmark || itemId == R.id.settingsFragment || itemId == R.id.chapterFragment) { // Include chapterFragment if it's a main tab
                 navOptionsBuilder.setPopUpTo(startDestinationId, false); // Pop up to home, but don't pop home itself
             }
             // For other destinations, default behavior (or specific logic if needed) might apply,
             // but the standard setup usually handles this. Here we focus on bottom nav items.


            // Handle navigation for each item
            try {
                 navController.navigate(itemId, null, navOptionsBuilder.build());
                 return true; // Event handled
            } catch (IllegalArgumentException e) {
                 Log.e("MainActivity", "Failed to navigate to destination ID: " + itemId, e);
                 return false; // Indicate navigation failed or wasn't handled
            }
        });


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
