package com.bluecrimson.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Todo.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_START_DATE_TIME = "startDateTime";
    private static final String COLUMN_END_DATE_TIME = "endDateTime";
    private static final String COLUMN_REPEAT = "repeatOption";
    private static final String COLUMN_PRIORITY = "priorityOption";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_START_DATE_TIME + " TEXT, " +
                COLUMN_END_DATE_TIME + " TEXT, " +
                COLUMN_REPEAT + " TEXT, " +
                COLUMN_PRIORITY + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

// -----------------------------------------------------------------------------------------------------------------------------
    // Add task to database
    public void addTask(Task newTask) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, newTask.getTitle());
        values.put(COLUMN_DESCRIPTION, newTask.getDescription());
        values.put(COLUMN_START_DATE_TIME, newTask.getStartDateTime());
        values.put(COLUMN_END_DATE_TIME, newTask.getEndDateTime());
        values.put(COLUMN_REPEAT, newTask.getRepeatOption());
        values.put(COLUMN_PRIORITY, newTask.getPriorityOption());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // Get all tasks from database
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                task.setStartDateTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE_TIME)));
                task.setEndDateTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DATE_TIME)));
                task.setRepeatOption(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPEAT)));
                task.setPriorityOption(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY)));
                task.setIsSelected(false);
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    // Delete task from database
    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Update task in database
    public boolean updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, task.getTitle());
        contentValues.put(COLUMN_DESCRIPTION, task.getDescription());
        contentValues.put(COLUMN_START_DATE_TIME, task.getStartDateTime());
        contentValues.put(COLUMN_END_DATE_TIME, task.getEndDateTime());
        contentValues.put(COLUMN_REPEAT, task.getRepeatOption());
        contentValues.put(COLUMN_PRIORITY, task.getPriorityOption());
        db.update(TABLE_NAME, contentValues, COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        return true;
    }

    // NOT USED:
//    public Task getTask(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
//        Task task = null;
//        if (cursor != null && cursor.moveToFirst()) {
//            task = new Task();
//            task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
//            task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
//            task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
//            task.setStartDateTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE_TIME)));
//            task.setEndDateTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DATE_TIME)));
//            task.setRepeatOption(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPEAT)));
//            task.setPriorityOption(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY)));
//        }
//        if (cursor != null) {
//        }
//        return task;
//    }
}