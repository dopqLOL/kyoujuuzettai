package com.example.contentful_javasilver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class StartFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the root layout
        View rootView = view.findViewById(R.id.start_fragment_root);

        // Set click listener on the root layout
        rootView.setOnClickListener(v -> {
            // Navigate to LoadingFragment
            try {
                Navigation.findNavController(v).navigate(R.id.action_start_to_loading);
            } catch (Exception e) {
                // Handle potential navigation errors (e.g., if already navigating)
                android.util.Log.e("StartFragment", "Navigation failed", e);
            }
        });
    }
}
