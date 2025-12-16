package com.example.qrscanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "attendance_db";
    private static final int DATABASE_VERSION = 2;

    // Student table
    private static final String STUDENT_TABLE = "students_table";
    private static final String STUDENT_ID = "student_id";
    private static final String STUDENT_NUMBER = "student_number";
    private static final String STUDENT_FULLNAME = "student_full_name";
    private static final String STUDENT_SECTION = "section";

    // Subject table
    private static final String SUBJECT_TABLE = "subject_table";
    private static final String SUBJECT_ID = "subject_id";
    private static final String SUBJECT_NAME = "subject_name";

    // Attendance table
    private static final String ATTENDACE_TABLE = "attendance_table";
    private static final String ATTENDACE_ID = "attendace_id";
    private static final String DATE = "attendance_date";
    private static final String TIME = "attendance_time";
    private static final String STATUS = "attendance_status";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db); db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // STUDENT TABLE
        String createStudentTable = "CREATE TABLE " + STUDENT_TABLE + " (" +
                STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                STUDENT_NUMBER + " TEXT, " +
                STUDENT_FULLNAME + " TEXT, " +
                STUDENT_SECTION + " TEXT" +
                ");";

        // SUBJECT TABLE
        String createSubjectTable = "CREATE TABLE " + SUBJECT_TABLE + " (" +
                SUBJECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SUBJECT_NAME + " TEXT" +
                ");";

        // ATTENDANCE TABLE
        String createAttendanceTable = "CREATE TABLE " + ATTENDACE_TABLE + " (" +
                ATTENDACE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                STUDENT_ID + " INTEGER, " +
                SUBJECT_ID + " INTEGER, " +
                DATE + " TEXT, " +
                TIME + " TEXT, " +
                STATUS + " TEXT, " +
                "FOREIGN KEY(" + STUDENT_ID + ") REFERENCES " + STUDENT_TABLE + "(" + STUDENT_ID + "), " +
                "FOREIGN KEY(" + SUBJECT_ID + ") REFERENCES " + SUBJECT_TABLE + "(" + SUBJECT_ID + ")" +
                ");";

        db.execSQL(createStudentTable);
        db.execSQL(createSubjectTable);
        db.execSQL(createAttendanceTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ATTENDACE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SUBJECT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + STUDENT_TABLE);
        onCreate(db);
    }


    // INSERT STUDENTS

    void addStudents(String student_number, String student_full_name, String student_section) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(STUDENT_NUMBER, student_number);
        cv.put(STUDENT_FULLNAME, student_full_name);
        cv.put(STUDENT_SECTION, student_section);

        long result = db.insert(STUDENT_TABLE, null, cv);  // ← FIXED HERE

        if (result == -1) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show();
        }
    }


    // MULTI-LINE IMPORT
    void addStudentFromLine(String line) {

        String[] parts = line.split(",");

        if (parts.length != 3) {
            Toast.makeText(context, "Invalid format", Toast.LENGTH_SHORT).show();
            return;
        }

        String number = parts[0].trim();
        String name = parts[1].trim();
        String section = parts[2].trim();

        addStudents(number, name, section);
    }

    void addMultipleStudents(String inputText) {

        String[] lines = inputText.split("\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            addStudentFromLine(line);
        }
    }

    // SUBJECTS
    void addSubject(String subject_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(SUBJECT_NAME, subject_name);

        long result = db.insert(SUBJECT_TABLE, null, cv);

        if (result == -1) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show();
        }
    }

    Cursor readAllSubject() {
        String query = "SELECT * FROM " + SUBJECT_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();

        if (db != null) {
            return db.rawQuery(query, null);
        }
        return null;
    }

    private int getStudentIdByNumber(String studentNumber) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + STUDENT_ID +
                        " FROM " + STUDENT_TABLE +
                        " WHERE " + STUDENT_NUMBER + " = ?",
                new String[]{studentNumber}
        );

        int studentId = -1;

        if (cursor.moveToFirst()) {
            studentId = cursor.getInt(0);
        }

        cursor.close();
        return studentId;
    }

    void scanAttendance(String studentNumber, int subjectId) {

        int studentId = getStudentIdByNumber(studentNumber);

        if (studentId == -1) {
            Toast.makeText(context, "Student not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String today = getCurrentDate();

        if (alreadyScanned(studentId, subjectId, today)) {
            Toast.makeText(context, "Already scanned today", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(STUDENT_ID, studentId);
        cv.put(SUBJECT_ID, subjectId);
        cv.put(DATE, today);
        cv.put(TIME, getCurrentTime());
        cv.put(STATUS, "Present");

        long result = db.insert(ATTENDACE_TABLE, null, cv);

        if (result == -1) {
            Toast.makeText(context, "Attendance failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Attendance recorded", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean alreadyScanned(int studentId, int subjectId, String date) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + ATTENDACE_TABLE +
                        " WHERE " + STUDENT_ID + "=? AND " +
                        SUBJECT_ID + "=? AND " +
                        DATE + "=?",
                new String[]{String.valueOf(studentId), String.valueOf(subjectId), date}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }

    private String getCurrentTime() {
        return new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }

    int getSubjectIdByName(String subjectName) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT subject_id FROM subject_table WHERE subject_name = ?",
                new String[]{subjectName}
        );

        int subjectId = -1;
        if (cursor.moveToFirst()) {
            subjectId = cursor.getInt(0);
        }

        cursor.close();
        return subjectId;
    }


}
