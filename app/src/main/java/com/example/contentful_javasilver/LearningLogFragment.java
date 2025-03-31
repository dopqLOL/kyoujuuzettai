package com.example.contentful_javasilver;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Add this import

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController; // Add this import
import androidx.navigation.Navigation; // Add this import
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contentful_javasilver.adapter.HistoryAdapter;
import com.example.contentful_javasilver.viewmodels.HistoryViewModel;


/**
 * Fragment to display the detailed learning log (history list).
 * Reuses HistoryViewModel and HistoryAdapter.
 */
public class LearningLogFragment extends Fragment {

    private HistoryViewModel historyViewModel;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewModel (reusing the existing HistoryViewModel)
        // Use requireActivity() to scope the ViewModel to the Activity if needed,
        // or 'this' to scope it to the fragment. Fragment scope is usually fine here.
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_learning_log, container, false);

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_learning_log); // Use the ID from fragment_learning_log.xml
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true); // Optional: if list size changes won't affect layout size

        // Setup Adapter (reusing the existing HistoryAdapter)
        adapter = new HistoryAdapter();
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
        historyViewModel.getAllHistory().observe(getViewLifecycleOwner(), quizHistories -> {
            // Log received data (optional)
            if (quizHistories == null) {
                Log.d("LearningLogFragment", "Observer received null data.");
            } else {
                Log.d("LearningLogFragment", "Observer received " + quizHistories.size() + " items.");
            }

            // Update the cached copy of the history in the adapter.
            adapter.submitList(quizHistories);

            // Optionally, add logic to show a "No history" message if the list is empty
        });
    }
}
