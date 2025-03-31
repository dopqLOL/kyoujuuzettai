package com.example.contentful_javasilver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController; // Import NavController
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.contentful_javasilver.adapter.ProblemListAdapter;
import com.example.contentful_javasilver.data.QuizEntity; // Import QuizEntity
import com.example.contentful_javasilver.databinding.FragmentProblemListBinding;
import com.example.contentful_javasilver.viewmodels.QuizViewModel;

// Implement the listener interface
public class ProblemListFragment extends Fragment implements ProblemListAdapter.OnItemClickListener {

    private FragmentProblemListBinding binding;
    private QuizViewModel quizViewModel;
    private ProblemListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quizViewModel = new ViewModelProvider(requireActivity()).get(QuizViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProblemListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearchView();
        setupBackButton(); // Add call to setup back button
        observeViewModel();

        // Load all problems initially
        quizViewModel.loadAllProblems();
    }

    private void setupRecyclerView() {
        // Pass 'this' as the listener when creating the adapter
        adapter = new ProblemListAdapter(this);
        binding.recyclerViewProblems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewProblems.setAdapter(adapter);
    }

    private void setupSearchView() {
        binding.searchViewProblems.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Not needed, using onQueryTextChange for live filtering
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                quizViewModel.setSearchQuery(newText);
                return true;
            }
        });

        binding.searchViewProblems.setOnCloseListener(() -> {
            quizViewModel.setSearchQuery(""); // Clear search
            return false;
        });
    }

    private void setupBackButton() {
        binding.backButton.setOnClickListener(v -> {
            // Navigate directly to HomeFragment
            NavHostFragment.findNavController(this).navigate(R.id.homeFragment);
        });
    }

    private void observeViewModel() {
        quizViewModel.getGroupedProblemList().observe(getViewLifecycleOwner(), problems -> {
            adapter.submitList(problems);
        });

        quizViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Can show a loading indicator if needed
        });

        quizViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            // Can show an error message if needed
        });
    }

    // Implementation of the click listener interface
    @Override
    public void onItemClick(QuizEntity quiz) {
        // Navigate to QuizFragment, passing the clicked quiz ID
        NavController navController = NavHostFragment.findNavController(this);
        // Create action arguments (assuming argument name is "problemId" in nav_graph)
        ProblemListFragmentDirections.ActionProblemListFragmentToQuizFragment action =
                ProblemListFragmentDirections.actionProblemListFragmentToQuizFragment(quiz.getQid());
        navController.navigate(action);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
