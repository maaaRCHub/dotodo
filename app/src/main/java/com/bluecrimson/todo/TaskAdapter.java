package com.bluecrimson.todo;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private List<Task> tasks;
    private final OnTaskClickListener listener;
    private final DBHelper dbHelper;

    // To keep track of selected task IDs
    private final Set<Integer> selectedTaskIds = new HashSet<>();

    public TaskAdapter(Context context, List<Task> tasks, OnTaskClickListener listener, DBHelper dbHelper) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item_layout, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        if(!tasks.isEmpty()){
            holder.itemLinearLayout.setBackground(context.getResources().getDrawable(R.drawable.rounded_corner_onselect));

        }

        Task task = tasks.get(position);
        holder.titleTextView.setText(task.getTitle());

        // Reset descriptionTextView visibility and text
        holder.descriptionTextView.setVisibility(View.GONE);
        holder.descriptionTextView.setText("");

        // Reset dateTimeTextView text
        holder.itemDateTimeLayout.setVisibility(View.GONE);
        holder.dateTimeTextView.setText("");

        // Reset priorityTextView visibility, text, and background tint
        holder.priorityTextView.setVisibility(View.GONE);
        holder.priorityTextView.setText("");
        holder.priorityTextView.setBackgroundTintList(null); // Clear background tint

        // Bind new data
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            holder.descriptionTextView.setVisibility(View.VISIBLE);
            holder.descriptionTextView.setText(task.getDescription());
        }

        // Bind new data
        if (task.getStartDateTime() != null && !task.getStartDateTime().isEmpty()) {
            holder.itemDateTimeLayout.setVisibility(View.VISIBLE);
            holder.dateTimeTextView.setText(task.getStartDateTime());

            if (task.getRepeatOption() != null) {
                holder.alarmImageView.setImageResource(R.drawable.baseline_repeat_24);
            }
        } else {
            holder.itemDateTimeLayout.setVisibility(View.GONE);
        }

        // Bind new data
        if (task.getPriorityOption() != null) {
            holder.priorityTextView.setVisibility(View.VISIBLE);
            holder.priorityTextView.setText(task.getPriorityOption());

            int backgroundColor;
            switch (task.getPriorityOption()) {
                case "High":
                    backgroundColor = Color.parseColor("#FAD9A1");
                    break;
                case "Medium":
                    backgroundColor = Color.parseColor("#F9F9C5");
                    break;
                case "Low":
                    backgroundColor = Color.parseColor("#D9F8C4");
                    break;
                default:
                    backgroundColor = Color.parseColor("#fcfffb");
                    break;
            }
            holder.priorityTextView.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        }

        // Set background color based on selection
        if (selectedTaskIds.contains(task.getId())) {
            holder.itemLinearLayout.setBackground(context.getResources().getDrawable(R.drawable.rounded_corner_onselect));
        } else {
            holder.itemLinearLayout.setBackgroundColor(Color.parseColor("#fcfffb"));
        }

        // Set on mark as done click listener
        holder.markAsDoneLinearLayout.setOnClickListener(v -> listener.onMarkAsDoneClick(task));

        // Set on task single click listener
        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task, position));

        // Set on task long click listener
        holder.itemView.setOnLongClickListener(v -> {
            listener.onTaskLongClick(task, position);
            return true;
        });
    }

// ----------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }


    // Update tasks
    public void updateTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    // Select all tasks
    public void selectAll() {
        selectedTaskIds.clear();
        for (Task task : tasks) {
            selectedTaskIds.add(task.getId());
        }
        notifyDataSetChanged();
    }

    // Deselect all tasks
    public void deSelectAll() {
        selectedTaskIds.clear();
        notifyDataSetChanged();
    }

    // Clear selection
    public void clearSelection() {
        selectedTaskIds.clear();
        notifyDataSetChanged();
    }

    // Delete selected tasks
    public void deleteSelectedTasks() {
        for (int taskId : selectedTaskIds) {
            dbHelper.deleteTask(taskId);
        }
        tasks = dbHelper.getAllTasks();
        selectedTaskIds.clear();
        notifyDataSetChanged();
    }

    // Get the number of selected tasks
    public int getSelectedCount() {
        return selectedTaskIds.size();
    }

    // Toggle selection for a specific task
    void toggleSelection(int taskId) {
        if (selectedTaskIds.contains(taskId)) {
            selectedTaskIds.remove(taskId);
        } else {
            selectedTaskIds.add(taskId);
            int size =  selectedTaskIds.size();
        }
        notifyDataSetChanged();
    }

// ---------------------------------------------------------------------------------------------------------------------------------------------

    public static class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{
        public TextView titleTextView;
        public TextView descriptionTextView;
        public TextView dateTimeTextView;
        public ImageView alarmImageView;
        public LinearLayout markAsDoneLinearLayout, itemDateTimeLayout, itemLinearLayout;
        public TextView priorityTextView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            alarmImageView = itemView.findViewById(R.id.alarmImageView);
            markAsDoneLinearLayout = itemView.findViewById(R.id.markAsDoneInTaskItemLinearLayout);
            itemDateTimeLayout = itemView.findViewById(R.id.itemDateTimeLayout);
            priorityTextView = itemView.findViewById(R.id.priorityTextView);
            itemLinearLayout = itemView.findViewById(R.id.itemLinearLayout);
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }

    public interface OnTaskClickListener {
        void onMarkAsDoneClick(Task task);
        void onTaskClick(Task task, int position);
        void onTaskLongClick(Task task, int position);
    }
}