package com.example.contentful_javasilver.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View; // Import View
import com.example.contentful_javasilver.data.QuizEntity;
import com.example.contentful_javasilver.databinding.ItemChapterHeaderBinding;
import com.example.contentful_javasilver.databinding.ItemProblemBinding;

public class ProblemListAdapter extends ListAdapter<Object, RecyclerView.ViewHolder> {

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(QuizEntity quiz);
    }

    private OnItemClickListener listener;

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_PROBLEM = 1;

    public ProblemListAdapter(OnItemClickListener listener) { // Add listener to constructor
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    // Setter for the listener (optional, if not using constructor injection)
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof String) {
            return VIEW_TYPE_HEADER;
        } else if (item instanceof QuizEntity) {
            return VIEW_TYPE_PROBLEM;
        }
        // Should not happen with proper data preparation
        throw new IllegalArgumentException("Invalid view type at position " + position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER) {
            ItemChapterHeaderBinding binding = ItemChapterHeaderBinding.inflate(inflater, parent, false);
            return new ChapterHeaderViewHolder(binding);
        } else { // VIEW_TYPE_PROBLEM
            ItemProblemBinding binding = ItemProblemBinding.inflate(inflater, parent, false);
            // Pass the listener to the ViewHolder
            return new ProblemViewHolder(binding, listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = getItem(position);
        if (holder instanceof ChapterHeaderViewHolder) {
            ((ChapterHeaderViewHolder) holder).bind((String) item);
        } else if (holder instanceof ProblemViewHolder) {
            ((ProblemViewHolder) holder).bind((QuizEntity) item);
        }
    }

    // ViewHolder for Chapter Headers
    static class ChapterHeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemChapterHeaderBinding binding;

        ChapterHeaderViewHolder(ItemChapterHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String chapterTitle) {
            binding.textViewChapterHeader.setText(chapterTitle);
        }
    }

    // ViewHolder for Problems
    static class ProblemViewHolder extends RecyclerView.ViewHolder {
        private final ItemProblemBinding binding;
        private final OnItemClickListener listener; // Add listener field

        ProblemViewHolder(ItemProblemBinding binding, OnItemClickListener listener) { // Receive listener
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener; // Store listener
        }

        void bind(QuizEntity quiz) {
            binding.textViewQid.setText(quiz.getQid()); // Remove "QID: " prefix
            binding.textViewQuestionCategory.setText(quiz.getQuestionCategory());

            // Set click listener on the item view
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(quiz); // Pass the clicked quiz item
                }
            });
        }
    }

    private static final DiffUtil.ItemCallback<Object> DIFF_CALLBACK = new DiffUtil.ItemCallback<Object>() {
        @Override
        public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
            if (oldItem instanceof String && newItem instanceof String) {
                return oldItem.equals(newItem);
            } else if (oldItem instanceof QuizEntity && newItem instanceof QuizEntity) {
                return ((QuizEntity) oldItem).getQid().equals(((QuizEntity) newItem).getQid());
            }
            return false; // Different types or null
        }

        @Override
        public boolean areContentsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
             if (oldItem instanceof String && newItem instanceof String) {
                return oldItem.equals(newItem); // Strings are immutable
            } else if (oldItem instanceof QuizEntity && newItem instanceof QuizEntity) {
                // Compare relevant fields if needed, for now, assume if items are same, contents are same
                 return ((QuizEntity) oldItem).equals(newItem); // Requires equals() implementation in QuizEntity
            }
            return false;
        }
    };
}
