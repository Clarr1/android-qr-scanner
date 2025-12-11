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


// student table
    private static final String STUDENT_TABLE = "students_table";
    private static final String STUDENT_ID = "student_id";
    private static final String STUDENT_NUMBER = "student_number";
    private static final String STUDENT_FULLNAME = "student_full_name";
    private static final String STUDENT_SECTION = "section";


// Subject table
    private static final String SUBJECT_TABLE = "subject_table";
    private static final String SUBJECT_ID = "subject_id";
    private static final String SUBJECT_NAME = "subject_name";

    //attendace_table
    private static final String ATTENDACE_TABLE = "attendance_table";
    private static final String ATTENDACE_ID = "attendace_id";
    private static final String DATE = "attendance_date";
    private  static final String TIME = "attendance_time";
    private static final String STATUS = "attendace_status";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
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

    public boolean insertScan(String qrText){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("qr_text", qrText);
        long result = db.insert(ATTENDACE_TABLE, null, values);
        return result != -1;
    }

    void addStudents(String student_number, String student_full_name, String student_section){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(STUDENT_NUMBER, student_number);
        cv.put(STUDENT_FULLNAME, student_full_name);
        cv.put(STUDENT_SECTION, student_section);

        long result = db.insert(ATTENDACE_TABLE, null, cv);
        if(result == -1){
            Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show();
        }

    }

    void addStudentFromLine(String line){
        String[] parts = line.split(",");

        if(parts.length != 3){
            Toast.makeText(context, "Invalid format", Toast.LENGTH_SHORT).show();
            return;
        }
        String number = parts[0].trim();
        String name = parts[1].trim();
        String section = parts[2].trim();

        addStudents(number,name,section);
    }

    void addMultipleStudents(String inputText){
        String[] lines = inputText.split("\n");

        for(String line: lines){
            if(line.trim().isEmpty()) continue;
            addStudentFromLine(line);
        }
    }

    void addSubject(String subject_name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(SUBJECT_NAME, subject_name);

        long result = db.insert(SUBJECT_TABLE, null, cv);
        if(result == -1){
            Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show();
        }
    }

    Cursor readAllSubject(){
        String query = "SELECT * FROM " + SUBJECT_TABLE;
        SQLiteDatabase db =  this.getReadableDatabase();

        Cursor cursor = null;

        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

}
