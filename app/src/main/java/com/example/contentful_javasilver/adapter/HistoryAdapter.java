package com.example.contentful_javasilver.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.core.content.ContextCompat; // For colors
import androidx.recyclerview.widget.RecyclerView;

import com.example.contentful_javasilver.R;
// import com.example.contentful_javasilver.data.ProblemStats; // No longer needed
import com.example.contentful_javasilver.data.QuizHistory; // Import QuizHistory

import java.text.SimpleDateFormat; // For date formatting
import java.util.Date; // For date formatting
import java.util.Locale; // For date formatting

public class HistoryAdapter extends ListAdapter<QuizHistory, HistoryAdapter.HistoryViewHolder> { // Changed type parameter

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());

    public HistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<QuizHistory> DIFF_CALLBACK = new DiffUtil.ItemCallback<QuizHistory>() { // Changed type parameter
        @Override
        public boolean areItemsTheSame(@NonNull QuizHistory oldItem, @NonNull QuizHistory newItem) {
            // Primary key comparison (assuming 'id' is the primary key)
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull QuizHistory oldItem, @NonNull QuizHistory newItem) {
            // Check if all relevant fields are the same
            return oldItem.problemId.equals(newItem.problemId) &&
                   oldItem.isCorrect == newItem.isCorrect &&
                   oldItem.timestamp == newItem.timestamp;
        }
    };

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false); // Use the updated item_history.xml
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        // Get the data model based on position
        QuizHistory currentHistory = getItem(position); // Changed type
        // Set item views based on your views and data model
        holder.problemIdTextView.setText(currentHistory.problemId); // Display problem ID directly

        // Set result text and color
        if (currentHistory.isCorrect) {
            holder.resultTextView.setText("正解");
            holder.resultTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.correct_color));
        } else {
            holder.resultTextView.setText("不正解");
            holder.resultTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.incorrect_color));
        }

        // Format and set timestamp
        try {
            holder.timestampTextView.setText(dateFormat.format(new Date(currentHistory.timestamp)));
        } catch (Exception e) {
            holder.timestampTextView.setText("----/--/-- --:--"); // Fallback for invalid timestamp
        }
    }

    // Provide a reference to the views for each data item
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView problemIdTextView;
        public TextView resultTextView; // Changed from correct/incorrect/accuracy
        public TextView timestampTextView; // Added for timestamp

        public HistoryViewHolder(View itemView) {
            super(itemView);
            // Update IDs based on the new item_history.xml layout
            problemIdTextView = itemView.findViewById(R.id.text_history_problem_id);
            resultTextView = itemView.findViewById(R.id.text_history_result);
            timestampTextView = itemView.findViewById(R.id.text_history_timestamp);
        }
    }
}
