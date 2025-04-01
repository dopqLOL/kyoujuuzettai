package com.example.contentful_javasilver;

import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton; // Added import

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment"; // TAG for logging

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the updated layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        NavController navController = Navigation.findNavController(view);

        // Removed Back Button setup code as it's handled by MainActivity's Toolbar

        // Setup Navigation Buttons
        Button buttonStatistics = view.findViewById(R.id.button_statistics);
        Button buttonLearningLog = view.findViewById(R.id.button_learning_log);

        buttonStatistics.setOnClickListener(v -> {
            // Navigate to StatisticsFragment
            navController.navigate(R.id.action_historyFragment_to_statisticsFragment);
        });

        buttonLearningLog.setOnClickListener(v -> {
            // Navigate to LearningLogFragment
            navController.navigate(R.id.action_historyFragment_to_learningLogFragment);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
