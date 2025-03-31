package com.example.contentful_javasilver.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contentful_javasilver.R;
import com.example.contentful_javasilver.data.ProblemStats;

import java.util.Locale;

public class StatisticsAdapter extends ListAdapter<ProblemStats, StatisticsAdapter.StatisticsViewHolder> {

    public StatisticsAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ProblemStats> DIFF_CALLBACK = new DiffUtil.ItemCallback<ProblemStats>() {
        @Override
        public boolean areItemsTheSame(@NonNull ProblemStats oldItem, @NonNull ProblemStats newItem) {
            // Compare based on the unique identifier (problemId)
            return oldItem.problemId.equals(newItem.problemId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProblemStats oldItem, @NonNull ProblemStats newItem) {
            // Check if counts are the same
            return oldItem.correctCount == newItem.correctCount &&
                   oldItem.incorrectCount == newItem.incorrectCount;
        }
    };

    @NonNull
    @Override
    public StatisticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_statistics, parent, false);
        return new StatisticsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticsViewHolder holder, int position) {
        ProblemStats currentStats = getItem(position);
        holder.problemIdTextView.setText(currentStats.problemId);
        holder.correctTextView.setText(String.format(Locale.getDefault(), "正: %d", currentStats.correctCount));
        holder.incorrectTextView.setText(String.format(Locale.getDefault(), "誤: %d", currentStats.incorrectCount));
        holder.accuracyTextView.setText(String.format(Locale.getDefault(), "率: %.1f%%", currentStats.getAccuracy()));
    }

    public static class StatisticsViewHolder extends RecyclerView.ViewHolder {
        public TextView problemIdTextView;
        public TextView correctTextView;
        public TextView incorrectTextView;
        public TextView accuracyTextView;

        public StatisticsViewHolder(View itemView) {
            super(itemView);
            problemIdTextView = itemView.findViewById(R.id.text_stats_problem_id);
            correctTextView = itemView.findViewById(R.id.text_stats_correct);
            incorrectTextView = itemView.findViewById(R.id.text_stats_incorrect);
            accuracyTextView = itemView.findViewById(R.id.text_stats_accuracy);
        }
    }
}
