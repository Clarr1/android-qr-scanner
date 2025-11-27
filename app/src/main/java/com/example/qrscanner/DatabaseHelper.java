package com.example.qrscanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "attendance_db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "attendance";



    private static final String STUDENT_TABLE = "students_table";
    private static final String STUDENT_ID = "student_id";
    private static final String STUDENT_NUMBER = "student_number";
    private static final String STUDENT_FULLNAME = "student_full_name";
    private static final String STUDENT_SECTION = "section";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query_1 = "CREATE TABLE " + STUDENT_TABLE+
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, qr_text TEXT)";


        String query_2 = "CREATE TABLE " + STUDENT_TABLE + " ("
                + STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + STUDENT_NUMBER + " TEXT, "
                + STUDENT_FULLNAME + " TEXT, "
                + STUDENT_SECTION + " TEXT"
                + ");";

        db.execSQL(query_1);
        db.execSQL(query_2);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + STUDENT_TABLE);
        onCreate(db);
    }

    public boolean insertScan(String qrText){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("qr_text", qrText);
        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    void addStudents(String student_number, String student_full_name, String student_section){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(STUDENT_NUMBER, student_number);
        cv.put(STUDENT_FULLNAME, student_full_name);
        cv.put(STUDENT_SECTION, student_section);

        long result = db.insert(TABLE_NAME, null, cv);
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

}
