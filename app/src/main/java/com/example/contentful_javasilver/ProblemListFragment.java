package com.example.contentful_javasilver;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu; // Import Menu
import android.view.MenuInflater; // Import MenuInflater
import android.view.MenuItem; // Import MenuItem
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView; // Import SearchView
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
        setHasOptionsMenu(true); // Inform the fragment that it has an options menu
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
        // Removed setupSearchView() and setupBackButton() calls
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

    // Removed setupSearchView() and setupBackButton() methods

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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.problem_list_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false; // Handle search query submission if needed
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    quizViewModel.setSearchQuery(newText); // Filter list based on text change
                    return true;
                }
            });

            searchView.setOnCloseListener(() -> {
                quizViewModel.setSearchQuery(""); // Clear search when closed
                return false;
            });
        }
    }

    // Handle menu item selection if necessary (though search is handled by action view)
    // @Override
    // public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    //     if (item.getItemId() == R.id.action_search) {
    //         // Action handled by SearchView itself
    //         return true;
    //     }
    //     return super.onOptionsItemSelected(item);
    // }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
