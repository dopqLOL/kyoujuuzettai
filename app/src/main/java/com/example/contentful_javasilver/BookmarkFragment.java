package com.example.contentful_javasilver;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions; // Import NavOptions
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Import ImageButton
import com.example.contentful_javasilver.adapter.ProblemListAdapter; // Reuse ProblemListAdapter
import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.viewmodels.BookmarkViewModel;

import java.util.ArrayList;
import java.util.List; // Import List

public class BookmarkFragment extends Fragment {

    private static final String TAG = "BookmarkFragment"; // Add TAG for logging
    private BookmarkViewModel bookmarkViewModel;
    private ProblemListAdapter adapter; // Reuse ProblemListAdapter
    private RecyclerView recyclerView;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bookmark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.bookmark_recycler_view);
        navController = Navigation.findNavController(view);
        ImageButton backButton = view.findViewById(R.id.backButton); // Find back button

        // Set listener for back button to navigate to HomeFragment
        backButton.setOnClickListener(v -> {
            // Navigate to HomeFragment and clear back stack up to HomeFragment
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true) // Pop up to homeFragment, inclusive
                    .build();
            navController.navigate(R.id.homeFragment, null, navOptions);
        });

        // Initialize Adapter with listener
        adapter = new ProblemListAdapter(quiz -> {
            // Navigate to QuizFragment when a bookmarked item is clicked
            // Pass only the required 'qid' argument as 'isRandomMode' has a default value
            BookmarkFragmentDirections.ActionBookmarkFragmentToQuizFragment action =
                    BookmarkFragmentDirections.actionBookmarkFragmentToQuizFragment(quiz.getQid());
            navController.navigate(action);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Initialize ViewModel
        bookmarkViewModel = new ViewModelProvider(this).get(BookmarkViewModel.class);

        // Observe bookmarked quizzes
        bookmarkViewModel.getBookmarkedQuizzes().observe(getViewLifecycleOwner(), quizzes -> {
            // --- ここからログ追加 ---
            if (quizzes != null) {
                Log.d(TAG, "Observer received " + quizzes.size() + " bookmarked quizzes.");
            } else {
                Log.d(TAG, "Observer received null list.");
            }
            // --- ここまでログ追加 ---

            // Update the adapter's data using submitList
            if (quizzes != null) {
                // ProblemListAdapter expects List<Object>, so cast List<QuizEntity>
                adapter.submitList(new ArrayList<>(quizzes));
                Log.d(TAG, "Submitted list to adapter."); // submitList呼び出しログ
            } else {
                adapter.submitList(new ArrayList<>()); // Submit empty list if null
                Log.d(TAG, "Submitted empty list to adapter."); // submitList呼び出しログ
            }
        });
    }
}
