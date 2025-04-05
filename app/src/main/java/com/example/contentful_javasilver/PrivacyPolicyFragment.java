package com.example.contentful_javasilver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PrivacyPolicyFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);

        // Setup BottomNavigationView
        // Note: Toolbar setup is removed as it's assumed to be handled by MainActivity
        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_navigation);
        // Ensure the correct item is selected based on the current destination
        // This might need adjustment depending on how navigation is structured
        // If Privacy Policy is considered a top-level destination, add it to the menu
        // If not, you might want to hide the bottom nav or handle selection differently
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Toolbar navigation click is assumed to be handled by MainActivity's toolbar setup

        // Hide BottomNavigationView if it's not a top-level destination
        // MainActivity might handle this globally, check MainActivity.java
        // ((MainActivity) requireActivity()).setBottomNavigationVisibility(false); // Example
    }

    @Override
    public void onResume() {
        super.onResume();
        // Optionally hide BottomNavigationView when this fragment is shown
        // MainActivity might handle this based on destination changes
        // ((MainActivity) requireActivity()).setBottomNavigationVisibility(false); // Example
    }

    @Override
    public void onPause() {
        super.onPause();
        // Optionally show BottomNavigationView when leaving this fragment
        // MainActivity might handle this based on destination changes
        // ((MainActivity) requireActivity()).setBottomNavigationVisibility(true); // Example
    }
}
