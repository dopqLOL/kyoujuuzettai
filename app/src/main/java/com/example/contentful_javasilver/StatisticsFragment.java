package com.example.contentful_javasilver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Added import

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController; // Added import
import androidx.navigation.Navigation; // Added import
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contentful_javasilver.adapter.StatisticsAdapter;
import com.example.contentful_javasilver.viewmodels.StatisticsViewModel;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel statisticsViewModel;
    private RecyclerView recyclerView;
    private StatisticsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewModel
        statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_statistics);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true); // Optional optimization

        // Setup Adapter
        adapter = new StatisticsAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup Back Button
        ImageButton backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            // Navigate directly to HomeFragment
            navController.navigate(R.id.homeFragment);
        });

        // Observe the LiveData from the ViewModel
        statisticsViewModel.getProblemStatistics().observe(getViewLifecycleOwner(), problemStats -> {
            // Update the cached copy of the stats in the adapter.
            if (problemStats != null) {
                adapter.submitList(problemStats);
            }
            // Optionally, add logic for an empty state (e.g., show a message if list is empty)
        });
    }
}
