package com.bluecrimson.todo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddActivity extends AppCompatActivity {

    ImageButton backButton;
    TextView saveButton, startDateTimeTextView, endDateTimeTextView;
    ImageButton deleteButton;
    LinearLayout selectStartDateTime, selectEndDateTime, endDateTimeLayout;
    Calendar startDateTimeCalendar, endDateTimeCalendar;
    Calendar selectedDateTimeCalendar;

    EditText titleEditText, descriptionEditText;
    Spinner repeatSpinner, prioritySpinner;

    private String selectedRepeatOption, selectedPriorityOption;
    private Boolean startDateTimeSelected = false;
    private int taskId = 0;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();

        // Retrieve task data from intent extras
        Intent intent = getIntent();
        if (intent != null) {
            taskId = intent.getIntExtra("id", -1);
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            String startDateTime = intent.getStringExtra("startDateTime");
            String endDateTime = intent.getStringExtra("endDateTime");
            String repeatOption = intent.getStringExtra("repeat");
            String priorityOption = intent.getStringExtra("priority");

            // Set pre-filled data in EditText fields
            titleEditText.setText(title);
            descriptionEditText.setText(description);
            startDateTimeTextView.setText(startDateTime);
            endDateTimeTextView.setText(endDateTime);

            // Set pre-selected repeat option in Spinner and pre-selected priority option in Spinner
            selectedRepeatOption = repeatOption;
            selectedPriorityOption = priorityOption;
        }

        setupSpinners();
        backButton.setOnClickListener(v -> finish());

        startDateTimeCalendar = Calendar.getInstance();
        endDateTimeCalendar = Calendar.getInstance();

        selectStartDateTime.setOnClickListener(v -> {
            openDateTimePicker(startDateTimeCalendar, true);
            endDateTimeTextView.setText(startDateTimeTextView.getText().toString());

        });
        selectEndDateTime.setOnClickListener(v -> {
            openDateTimePicker(endDateTimeCalendar, false);
        });

        saveButton.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.custom_animation);
            saveButton.startAnimation(animation);

            if (taskId != -1) {
                updateTask();
                taskId = 0;
            }else {
                setupSpinners();
                if (!startDateTimeSelected) {
                    selectedRepeatOption = null; // Set repeat option to null if start date/time not selected
                }

                String title = titleEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();
                String startDateTime = startDateTimeTextView.getText().toString().trim();
                String endDateTime = endDateTimeTextView.getText().toString().trim();
                String repeatOption = selectedRepeatOption;
                String priorityOption = selectedPriorityOption;

                if (title.isEmpty()) {
                    Toast.makeText(AddActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    // Save the task to the database
                    // check if title has numerical value, if yes then toast to remove them
                    if (title.matches("\\d+")) {
                        Toast.makeText(AddActivity.this, "Title cannot contain numerical value", Toast.LENGTH_SHORT).show();
                    }else{
                        Task task = new Task();
                        task.setTitle(title);
                        task.setDescription(description);
                        task.setStartDateTime(startDateTime);
                        task.setEndDateTime(endDateTime);
                        task.setRepeatOption(repeatOption);
                        task.setPriorityOption(priorityOption);
                        DBHelper dbHelper = new DBHelper(this);
                        dbHelper.addTask(task);

                        Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        });

        // Set click listener for delete button
        deleteButton.setOnClickListener(v -> {
            if(taskId != 0) {
                if(!titleEditText.getText().toString().isEmpty()){
                    deleteTask(taskId);
                    taskId = 0;
                    finish(); // Finish the activity after deletion
                }else {
                    Toast.makeText(this, "No task to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setupSpinners() {
        // Set up the repeat spinner
        ArrayAdapter<CharSequence> repeatAdapter = ArrayAdapter.createFromResource(this,
                R.array.repeat_options, android.R.layout.simple_spinner_item);
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(repeatAdapter);

        // Set pre-selected repeat option if available
        if (selectedRepeatOption != null) {
            int repeatPosition = repeatAdapter.getPosition(selectedRepeatOption);
            repeatSpinner.setSelection(repeatPosition);
        }

        repeatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRepeatOption = parent.getItemAtPosition(position).toString();
                if(selectedRepeatOption.equals("None")){
                    selectedRepeatOption = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRepeatOption = null;
            }
        });

        // Set up the priority spinner
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(this,
                R.array.priority_options, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        // Set pre-selected priority option if available
        if (selectedPriorityOption != null) {
            int priorityPosition = priorityAdapter.getPosition(selectedPriorityOption);
            prioritySpinner.setSelection(priorityPosition);
        }

        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPriorityOption = parent.getItemAtPosition(position).toString();
                if(selectedPriorityOption.equals("None")){
                    selectedPriorityOption = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { selectedPriorityOption = null; }
        });
    }


    private void openDateTimePicker(Calendar dateTimeCalendar, boolean isStartDateTime) {
        selectedDateTimeCalendar = dateTimeCalendar; // Assign the current calendar instance to selectedDateTimeCalendar

        int year = dateTimeCalendar.get(Calendar.YEAR);
        int month = dateTimeCalendar.get(Calendar.MONTH);
        int day = dateTimeCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDateTimeCalendar.set(Calendar.YEAR, selectedYear);
                    selectedDateTimeCalendar.set(Calendar.MONTH, selectedMonth);
                    selectedDateTimeCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    openTimePicker(selectedDateTimeCalendar, isStartDateTime);
                },
                year, month, day);

        // For the end date/time, disable dates earlier than the start date/time
        if (!isStartDateTime) {
            datePickerDialog.getDatePicker().setMinDate(startDateTimeCalendar.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    private void openTimePicker(Calendar dateTimeCalendar, boolean isStartDateTime) {
        int hour = dateTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = dateTimeCalendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    dateTimeCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    dateTimeCalendar.set(Calendar.MINUTE, selectedMinute);

                    if (isStartDateTime) {
                        // Update start date/time TextView with selected date/time
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                        String dateTimeString = dateFormat.format(dateTimeCalendar.getTime());
                        startDateTimeTextView.setText(dateTimeString);
                        startDateTimeSelected = true;





                        // Set end date/time default to start date/time if not already set
                        if (endDateTimeTextView.getText().toString().isEmpty()) {
                            endDateTimeCalendar.setTimeInMillis(dateTimeCalendar.getTimeInMillis());
                            endDateTimeTextView.setText(dateTimeString);
                        }else{
                            //compare startDateTime and endDateTime, make use of
                            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
                            try {
                                Date tempStartDateTime = dateTimeFormat.parse(dateTimeString);
                                Date tempEndDateTime = dateTimeFormat.parse(endDateTimeTextView.getText().toString());
                                assert tempEndDateTime != null;
                                if (tempEndDateTime.before(tempStartDateTime)) {
                                    endDateTimeCalendar.setTimeInMillis(dateTimeCalendar.getTimeInMillis());
                                    endDateTimeTextView.setText(dateTimeString);
                                }
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        // Ensure end date/time is not before start date/time
                        if (dateTimeCalendar.before(startDateTimeCalendar)) {
                            showDateTimeValidationError();
                        } else {
                            // Update end date/time TextView with selected date/time
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                            String dateTimeString = dateFormat.format(dateTimeCalendar.getTime());
                            endDateTimeTextView.setText(dateTimeString);
                        }
                    }
                },
                hour, minute, false);

        timePickerDialog.show();
    }

    private void showDateTimeValidationError() {
        // Show alert to user indicating that end date/time cannot be before start date/time
        // You can use AlertDialog or Toast to show the error message
        Toast.makeText(this, "End date/time cannot be before start date/time", Toast.LENGTH_SHORT).show();
    }

    private void deleteTask(int taskId) {
        dbHelper = new DBHelper(this);
        dbHelper.deleteTask(taskId);
        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
    }

    private void updateTask() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String startDateTime = startDateTimeTextView.getText().toString().trim();
        String endDateTime = endDateTimeTextView.getText().toString().trim();
        String repeatOption = selectedRepeatOption;
        String priorityOption = selectedPriorityOption;

        if (title.isEmpty()) {
            Toast.makeText(AddActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
        } else {
            if (title.matches("\\d+")) {
                Toast.makeText(AddActivity.this, "Title cannot contain numerical value", Toast.LENGTH_SHORT).show();
            } else {
                Task task = new Task();
                task.setId(taskId); // Set the existing task ID
                task.setTitle(title);
                task.setDescription(description);
                task.setStartDateTime(startDateTime);
                task.setEndDateTime(endDateTime);
                task.setRepeatOption(repeatOption);
                task.setPriorityOption(priorityOption);
                DBHelper dbHelper = new DBHelper(this);
                dbHelper.updateTask(task);
                taskId = 0;

                Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.addDeleteButton);
        selectStartDateTime = findViewById(R.id.startDateTimelLayout);
        selectEndDateTime = findViewById(R.id.endDateTimelLayout);


        startDateTimeTextView = findViewById(R.id.startDateTimeTextView);
        endDateTimeTextView = findViewById(R.id.endDateTimeTextView);

        repeatSpinner = findViewById(R.id.repeatSpinner);
        prioritySpinner = findViewById(R.id.prioritySpinner);

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        endDateTimeLayout = findViewById(R.id.endDateTimelLayoutPrimary);

    }
}