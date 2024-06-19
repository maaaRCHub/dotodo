package com.bluecrimson.todo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    FloatingActionButton addButton;
    ImageButton optionButton, gridViewButton, sortButton, crossCloseButton, selectAllButton, deleteButtonManual;
    LinearLayout mainMenuLayout, deleteMenuLayout, emptyLayout;

    CardView optionsLayout, sortLayout;
    TextView deleteButton, exitButton, sortByUpcomingButton, sortByCreatedOnButton, sortByPriorityButton, countSelectedTextView ;

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private DBHelper dbHelper;
    private List<Task> taskList;

    public String sortingPreference = "upcoming"; // Default sorting preference

    private boolean isGridLayout = false; // Flag to track current layout state
    private static final int spanCount = 2; // Number of columns in grid layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        initializeViews();
        dbHelper = new DBHelper(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager.
        taskList = dbHelper.getAllTasks();
        toggleEmptyLayout();
        taskAdapter = new TaskAdapter(this, taskList, this, dbHelper);
        recyclerView.setAdapter(taskAdapter);

        sortTasksByUpcoming();

        // Open AddActivity.
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsLayout.setVisibility(View.GONE);
                sortLayout.setVisibility(View.GONE);
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });

        // Open sort options
        sortButton.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.custom_animation);
            sortLayout.startAnimation(animation);

            if (sortLayout.getVisibility() == View.VISIBLE) {
                // If optionsLayout is already visible, hide it with animation
                Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                sortLayout.startAnimation(fadeOut);
                sortLayout.setVisibility(View.GONE);
            } else {
                // If optionsLayout is not visible, show it with animation
                Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                sortLayout.startAnimation(fadeIn);
                optionsLayout.setVisibility(View.GONE);
                sortLayout.setVisibility(View.VISIBLE);

                final Handler handler = new Handler();
                Runnable hideRunnable = new Runnable() {
                    @Override
                    public void run() {
                        sortLayout.setVisibility(View.GONE);
                    }
                };
                handler.postDelayed(hideRunnable, 5000);
            }
        });

        // Set sorting preference as "upcoming"
        sortByUpcomingButton.setOnClickListener(v -> {
            sortTasksByUpcoming();
            sortingPreference = "upcoming";
            sortLayout.setVisibility(View.GONE);
        });
        // Set sorting preference as "date/Created On"
        sortByCreatedOnButton.setOnClickListener(v -> {
            sortingPreference = "date";
            sortTasksByCreatedOn();
            sortLayout.setVisibility(View.GONE);
        });
        // Set sorting preference as "priority"
        sortByPriorityButton.setOnClickListener(v -> {
            sortingPreference = "priority";
            sortTasksByPriority();
            sortLayout.setVisibility(View.GONE);
        });

        // Set sort and more options layout visibility to 'gone' when mainLayout is clicked.
        mainMenuLayout.setOnClickListener(v -> {
            optionsLayout.setVisibility(View.GONE);
            sortLayout.setVisibility(View.GONE);
        });

        // Change layout from linear to grid and vice versa.
        gridViewButton.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.custom_animation);
            gridViewButton.startAnimation(animation);
            toggleLayoutManager();
        });

        // Open more options layout
        optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.custom_animation);
                optionButton.startAnimation(animation);

                if (optionsLayout.getVisibility() == View.VISIBLE) {
                    // If optionsLayout is already visible, hide it with animation
                    Animation fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                    optionsLayout.startAnimation(fadeOut);
                    optionsLayout.setVisibility(View.GONE);
                } else {
                    // If optionsLayout is not visible, show it with animation
                    Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    optionsLayout.startAnimation(fadeIn);
                    sortLayout.setVisibility(View.GONE);
                    optionsLayout.setVisibility(View.VISIBLE);

                    final Handler handler = new Handler();
                    Runnable hideRunnable = () -> optionsLayout.setVisibility(View.GONE);
                    handler.postDelayed(hideRunnable, 9000);
                }
            }
        });

//        // Set a click listener for the searchButton
//        searchButton.setOnClickListener(v -> {
//            optionsLayout.setVisibility(View.GONE);
//            Animation clickAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_click_animation);
//            searchButton.startAnimation(clickAnimation);
//            // optionsLayout.setVisibility(View.GONE);
//            Toast.makeText(MainActivity.this, "Feature Not implemented!", Toast.LENGTH_SHORT).show();
//        });

        // Open delete menu
        deleteButton.setOnClickListener(v -> {
            Animation clickAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_click_animation);
            deleteButton.startAnimation(clickAnimation);
            optionsLayout.setVisibility(View.GONE);
            sortLayout.setVisibility(View.GONE);
            toggleMainMenu();
        });

        // Exit app
        exitButton.setOnClickListener(v -> {
            Animation clickAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_click_animation);
            exitButton.startAnimation(clickAnimation);
            optionsLayout.setVisibility(View.GONE);
            finishAffinity();
            System.exit(0);
        });



//--------------------------------------------------------------------------------------------------------------------------------------

        // In Delete menu

        // Close delete menu
        crossCloseButton.setOnClickListener(v -> closeDeleteMenu());

        // Select all tasks
        selectAllButton.setOnClickListener(v -> {
            if(taskAdapter.getSelectedCount() == taskAdapter.getItemCount()){
                taskAdapter.deSelectAll();
                countSelectedTextView.setText("0");
                selectAllButton.setImageResource(R.drawable.round_select_all_24);
                return;
            }
            taskAdapter.selectAll();
            countSelectedTextView.setText(String.valueOf(taskAdapter.getSelectedCount()));
            selectAllButton.setImageResource(R.drawable.baseline_deselect_24);
            updateDeleteMenu();
        });

        // Delete selected tasks
        deleteButtonManual.setOnClickListener(v -> {
            int selectedCount = taskAdapter.getSelectedCount();
            if (selectedCount > 0) {
                showDeleteConfirmationDialog();
            } else {
                Toast.makeText(this, "No tasks selected to delete", Toast.LENGTH_SHORT).show();
            }
        });

    }

// -------------------------------------------------------------------------------------------------------------------------------------------------

//    private void showSortDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Sort by:")
//                .setItems(new String[]{"Upcoming", "Overdue", "Priority"}, (dialog, which) -> {
//                    switch (which) {
//                        case 0:
//                            sortingPreference = "upcoming";
//                            sortTasksByUpcoming();
//                            break;
//                        case 1:
//                            sortingPreference = "overdue";
//                            sortTasksByOverdue();
//                            break;
//                        case 2:
//                            sortingPreference = "priority";
//                            sortTasksByPriority();
//                            break;
//                    }
//                });
//        builder.create().show();
//    }
//
//    private void toggleMainMenu() {
//        if (mainMenuLayout.getVisibility() == View.VISIBLE) {
//            mainMenuLayout.setVisibility(View.GONE);
//        } else {
//            mainMenuLayout.setVisibility(View.VISIBLE);
//        }
//    }

    // Toggle between main menu and delete menu
    private void toggleMainMenu() {
        if (mainMenuLayout.getVisibility() == View.VISIBLE) {
            mainMenuLayout.setVisibility(View.GONE);
            deleteMenuLayout.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
        } else {
            mainMenuLayout.setVisibility(View.VISIBLE);
            deleteMenuLayout.setVisibility(View.GONE);
            addButton.setVisibility(View.VISIBLE);
        }
    }

    // Toggle between LinearLayoutManager and GridLayoutManager
    private void toggleLayoutManager() {
        RecyclerView.LayoutManager layoutManager;
        if (isGridLayout) {
            layoutManager = new LinearLayoutManager(this);
            gridViewButton.setImageResource(R.drawable.baseline_grid_view_24);
        } else {
            layoutManager = new GridLayoutManager(this, spanCount);
            gridViewButton.setImageResource(R.drawable.baseline_view_stream_24);
        }
        recyclerView.setLayoutManager(layoutManager);
        isGridLayout = !isGridLayout; // Toggle the layout flag
        recyclerView.setAdapter(taskAdapter); // Resetting adapter to refresh the view
    }

    // Show confirmation dialog for deleting selected tasks
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete the selected tasks?")
                .setPositiveButton("Delete", (dialog, id) -> {
                    taskAdapter.deleteSelectedTasks();
                    toggleEmptyLayout();
                    closeDeleteMenu();
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.dismiss();
                });
        builder.create().show();
    }

    // Update delete menu visibility based on selected tasks
    private void updateDeleteMenu() {
        if (taskAdapter.getSelectedCount() > 0) {
            deleteMenuLayout.setVisibility(View.VISIBLE);
            mainMenuLayout.setVisibility(View.GONE);
        } else {
            deleteMenuLayout.setVisibility(View.GONE);
            toggleEmptyLayout();
            mainMenuLayout.setVisibility(View.VISIBLE);
        }
    }

    // Close delete menu
    private void closeDeleteMenu() {
        deleteMenuLayout.setVisibility(View.GONE);
        mainMenuLayout.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);
        taskAdapter.clearSelection();
        toggleEmptyLayout();
    }

    // Close main menu
    private void closeMainMenu(){
        mainMenuLayout.setVisibility(View.GONE);
    }

    private void toggleEmptyLayout(){
        if(!taskList.isEmpty() && taskAdapter == null || taskAdapter.getItemCount() == 0){
            emptyLayout.setVisibility(View.VISIBLE);
        }else{
            emptyLayout.setVisibility(View.GONE);
        }
    }


// -------------------------------------------------------------------------------------------------------------------------------------

//
    // OLD Sorting

//    private void sortTasksByUpcoming() {
//        Collections.sort(taskList, new Comparator<Task>() {
//            @Override
//            public int compare(Task t1, Task t2) {
//                return parseDate(t1.getStartDateTime()).compareTo(parseDate(t2.getStartDateTime()));
//            }
//        });
//        taskAdapter.updateTasks(taskList);
//    }


//    private void sortTasksByOverdue() {
//        Collections.sort(taskList, new Comparator<Task>() {
//            @Override
//            public int compare(Task t1, Task t2) {
//                return parseDate(t2.getStartDateTime()).compareTo(parseDate(t1.getStartDateTime()));
//            }
//        });
//        taskAdapter.updateTasks(taskList);
//    }


//    private void sortTasksByPriority() {
//        Collections.sort(taskList, new Comparator<Task>() {
//            @Override
//            public int compare(Task t1, Task t2) {
//                return t1.getPriorityOption().compareTo(t2.getPriorityOption());
//            }
//        });
//        taskAdapter.updateTasks(taskList);
//    }


//    private Date parseDate(String date) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//        try {
//            return sdf.parse(date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return new Date();
//    }


// ---------------------------------------------------------------------------------------------------------------------------

    // Sorting


    // Sort tasks by upcoming date and time
    private void sortTasksByUpcoming() {
        if (taskList.isEmpty()) {
            return;
        }
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());

        // Sort tasks by start date and time, putting empty dates at the bottom
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                String startDate1 = t1.getStartDateTime();
                String startDate2 = t2.getStartDateTime();

                // Handle null or empty date/time values
                if ((startDate1 == null || startDate1.isEmpty()) && (startDate2 == null || startDate2.isEmpty())) {
                    return 0; // Both tasks have no date/time
                } else if (startDate1 == null || startDate1.isEmpty()) {
                    return 1; // t1 has no date/time, should go to bottom
                } else if (startDate2 == null || startDate2.isEmpty()) {
                    return -1; // t2 has no date/time, should go to bottom
                }

                // Parse dates and times
                Date date1 = parseDateTime(startDate1, dateTimeFormat);
                Date date2 = parseDateTime(startDate2, dateTimeFormat);

                if (date1 == null || date2 == null) {
                    return 0; // If parsing fails, consider them equal for robustness
                }
                return date1.compareTo(date2);
            }

            private Date parseDateTime(String dateTime, SimpleDateFormat dateTimeFormat) {
                try {
                    return dateTimeFormat.parse(dateTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null; // Return null if parsing fails
                }
            }
        });
        taskAdapter.updateTasks(taskList);
    }


    // Sort tasks by priority
    private void sortTasksByPriority() {
        if (taskList.isEmpty()) {
            return;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                // Handle null or empty priority values
                String priorityT1 = t1.getPriorityOption();
                String priorityT2 = t2.getPriorityOption();

                if (priorityT1 == null && priorityT2 == null) {
                    return 0;
                } else if (priorityT1 == null) {
                    return 1;
                } else if (priorityT2 == null) {
                    return -1;
                }
                int priorityComparison = Integer.compare(getPriorityValue(priorityT2), getPriorityValue(priorityT1));
                if (priorityComparison == 0) {
                    Date t1StartDate = parseDate(t1.getStartDateTime(), dateFormat);
                    Date t2StartDate = parseDate(t2.getStartDateTime(), dateFormat);

                    if (t1StartDate == null && t2StartDate == null) {
                        return 0;
                    } else if (t1StartDate == null) {
                        return 1;
                    } else if (t2StartDate == null) {
                        return -1;
                    } else {
                        return t1StartDate.compareTo(t2StartDate);
                    }
                }
                return priorityComparison;
            }
        });
        taskAdapter.updateTasks(taskList);
    }

    // Sort tasks by created on date
    private void sortTasksByCreatedOn() {
        if (taskList.isEmpty()) {
            return;
        }
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                return Integer.compare(t2.getId(), t1.getId()); // Sort by ID in descending order
            }
        });
        taskAdapter.updateTasks(taskList);
    }

    // Parse date string using provided date format
    private Date parseDate(String dateString, SimpleDateFormat dateFormat) {
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Get priority value based on priority string
    private int getPriorityValue(String priority) {
        switch (priority) {
            case "High":
                return 3;
            case "Medium":
                return 2;
            case "Low":
                return 1;
            default:
                return 0;
        }
    }


// ----------------------------------------------------------------------------------------------------------------------------------

    // Delete task by on click on 'mark as done' button

    @Override
    public void onMarkAsDoneClick(Task task) {
        if(sortLayout.getVisibility() == View.VISIBLE || optionsLayout.getVisibility() == View.VISIBLE){
            sortLayout.setVisibility(View.GONE);
            optionsLayout.setVisibility(View.GONE);
            return;
        }
        if(taskAdapter.getSelectedCount() != 0){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Task completed: " + task.getTitle() + "?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    taskList.remove(task);
                    taskAdapter.updateTasks(taskList);
                    dbHelper.deleteTask(task.getId());
                    Toast.makeText(this, "Task completed", Toast.LENGTH_SHORT).show();
                    toggleEmptyLayout();
                    showUndoSnackbar(task);
                })
                .setNegativeButton("No", (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }


    // Task single click listener
    @Override
    public void onTaskClick(Task selectedTask, int position) {

        if(sortLayout.getVisibility() == View.VISIBLE || optionsLayout.getVisibility() == View.VISIBLE){
            sortLayout.setVisibility(View.GONE);
            optionsLayout.setVisibility(View.GONE);
            return;
        }

        if(taskAdapter.getSelectedCount() != 0 || deleteMenuLayout.getVisibility() == View.VISIBLE){
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                LinearLayout lLayout = viewHolder.itemView.findViewById(R.id.itemLinearLayout);
                lLayout.setBackground(getResources().getDrawable(R.drawable.rounded_corner_onselect));
            }

            taskAdapter.toggleSelection(selectedTask.getId());
             runOnUiThread(()-> {
                 taskAdapter.notifyItemChanged(position);
             });

            if(taskAdapter.getSelectedCount() == 0){
                toggleMainMenu();
                taskAdapter.notifyItemChanged(position);
                runOnUiThread(()-> {
                    taskAdapter.notifyItemChanged(position);
                });
            }

            int count = taskAdapter.getSelectedCount();
            countSelectedTextView.setText(String.valueOf(count));
            return;
        }

        optionsLayout.setVisibility(View.GONE);
        sortLayout.setVisibility(View.GONE);

        // On click task Show task start & end time with title
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(selectedTask.getTitle());
//        builder.setMessage("Start Date: " + selectedTask.getStartDateTime() + "\nEnd Date: " + selectedTask.getEndDateTime());
//        builder.setPositiveButton("OK", null);
//        builder.show();

        // Open AddActivity with selected task data

        Intent intent = new Intent(MainActivity.this, AddActivity.class);
        intent.putExtra("id", selectedTask.getId());
        intent.putExtra("title", selectedTask.getTitle());
        intent.putExtra("description", selectedTask.getDescription());
        intent.putExtra("startDateTime", selectedTask.getStartDateTime());
        intent.putExtra("endDateTime", selectedTask.getEndDateTime());
        intent.putExtra("repeat", selectedTask.getRepeatOption());
        intent.putExtra("priority", selectedTask.getPriorityOption());
        startActivity(intent);

    }


    // Task long click listener
    @Override
    public void onTaskLongClick(Task task, int position) {
        if(sortLayout.getVisibility() == View.VISIBLE || optionsLayout.getVisibility() == View.VISIBLE){
            sortLayout.setVisibility(View.GONE);
            optionsLayout.setVisibility(View.GONE);
            return;
        }
        if(taskAdapter.getSelectedCount() != 0) {
            return;
        }

        addButton.setVisibility(View.GONE);
        optionsLayout.setVisibility(View.GONE);
        sortLayout.setVisibility(View.GONE);
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            LinearLayout lLayout = viewHolder.itemView.findViewById(R.id.itemLinearLayout);
            lLayout.setBackground(getResources().getDrawable(R.drawable.rounded_corner_onselect));
        }

        taskAdapter.toggleSelection(task.getId());
        taskAdapter.notifyItemChanged(position);
        int count = taskAdapter.getSelectedCount();
        countSelectedTextView.setText(String.valueOf(count));
        updateDeleteMenu();
    }

//    NO MORE USE:

//    private void loadTasks() {
//        List<Task> tasks = dbHelper.getAllTasks();
//        if (taskAdapter == null) {
//            taskAdapter = new TaskAdapter(this, tasks, this, dbHelper);
//
//            recyclerView.setAdapter(taskAdapter);
//        } else {
//            taskAdapter.setTasks(tasks);
//        }
//        taskList.clear();
//        taskList.addAll(dbHelper.getAllTasks());
//        taskAdapter.notifyDataSetChanged();
//
//    }


// --------------------------------------------------------------------------------------------------------------------------------------

    // Show undo snackbar with 1 minute delay

    private void showUndoSnackbar(Task task) {
        Handler handler = new Handler();
        Runnable runnable = () -> {
            if (!taskList.contains(task)) {
                dbHelper.deleteTask(task.getId());
            }
        };
        handler.postDelayed(runnable, 60000);
        Toast.makeText(this, "You have 1 minute to undo this action", Toast.LENGTH_LONG).show();
        deleteTaskWithUndo(task);
    }

    // Delete task with undo

    private void deleteTaskWithUndo(Task task) {
        taskList.remove(task);
        taskAdapter.updateTasks(taskList);
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Task deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", v -> {
            taskList.add(task); // Add the task back to the list
            taskAdapter.notifyDataSetChanged();
            toggleEmptyLayout();
            taskAdapter.updateTasks(taskList);
            dbHelper.addTask(task);
        });

        // Delete task from database after 1 minute

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                    dbHelper.deleteTask(task.getId());
                }
            }
        });
        snackbar.setDuration(60000); // 1 minute
        snackbar.show();
    }


// --------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh tasks on resume
        taskList = dbHelper.getAllTasks();
        if (sortingPreference.equals("priority")) {
            sortTasksByPriority();
        } else if (sortingPreference.equals("date")) {
            sortTasksByCreatedOn();
        } else {
            sortTasksByUpcoming();
        }
        toggleEmptyLayout();
    }

    @Override
    public void onBackPressed() {
        if(optionsLayout.getVisibility() == View.VISIBLE || sortLayout.getVisibility() == View.VISIBLE || deleteMenuLayout.getVisibility() == View.VISIBLE) {
            optionsLayout.setVisibility(View.GONE);
            sortLayout.setVisibility(View.GONE);
            if(deleteMenuLayout.getVisibility() == View.VISIBLE){
                closeDeleteMenu();
            }
            return;
        }
        super.onBackPressed();
    }

// ------------------------------------------------------------------------------------------------------------------------------------------

    // Initialize views.
    private void initializeViews() {
        addButton = findViewById(R.id.floatingActionButton);
        optionButton = findViewById(R.id.optionButton);

        optionsLayout = findViewById(R.id.optionsLayout);
        gridViewButton = findViewById(R.id.gridViewButton);
        deleteButton = findViewById(R.id.mainDeleteButton);
        exitButton = findViewById(R.id.exitButton);

        recyclerView = findViewById(R.id.recyclerListView);

        sortButton = findViewById(R.id.sortButton);
        sortLayout = findViewById(R.id.sortLayout);
        sortByUpcomingButton = findViewById(R.id.upcomingSortButton);
        sortByCreatedOnButton = findViewById(R.id.createdOnSortButton);
        sortByPriorityButton = findViewById(R.id.prioritySortButton);

        countSelectedTextView = findViewById(R.id.countSelectedTextView);
        selectAllButton = findViewById(R.id.selectAllButton);
        deleteButtonManual = findViewById(R.id.deleteButtonManual);
        crossCloseButton = findViewById(R.id.crossCloseButton);

        deleteMenuLayout = findViewById(R.id.deleteMenuLayout);
        mainMenuLayout = findViewById(R.id.mainMenuLayout);

        emptyLayout = findViewById(R.id.emptyLayout);
    }
}