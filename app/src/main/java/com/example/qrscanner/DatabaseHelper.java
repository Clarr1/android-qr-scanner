package com.example.qrscanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "attendance_db";
    private static final int DATABASE_VERSION = 3;

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

    //Subject table
    private static final String STUDENT_SUBJECT_TABLE = "student_subject_table";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
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
                "FOREIGN KEY(" + SUBJECT_ID + ") REFERENCES " + SUBJECT_TABLE + "(" + SUBJECT_ID + ") ON DELETE CASCADE" +
                ");";

        String createStudentSubjectTable = "CREATE TABLE " + STUDENT_SUBJECT_TABLE + " (" +
                STUDENT_ID + " INTEGER, " +
                SUBJECT_ID + " INTEGER, " +
                "PRIMARY KEY (" + STUDENT_ID + ", " + SUBJECT_ID + "), " +
                "FOREIGN KEY(" + STUDENT_ID + ") REFERENCES " + STUDENT_TABLE + "(" + STUDENT_ID + "), " +
                "FOREIGN KEY(" + SUBJECT_ID + ") REFERENCES " + SUBJECT_TABLE + "(" + SUBJECT_ID + ") ON DELETE CASCADE" +
                ");";

        db.execSQL(createStudentTable);
        db.execSQL(createSubjectTable);
        db.execSQL(createAttendanceTable);
        db.execSQL(createStudentSubjectTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ATTENDACE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + STUDENT_SUBJECT_TABLE);


        db.execSQL("DROP TABLE IF EXISTS " + SUBJECT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + STUDENT_TABLE);
        onCreate(db);
    }


    // STUDENT METHODS

    public boolean addStudents(String student_number, String student_full_name, String student_section) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(STUDENT_NUMBER, student_number);
        cv.put(STUDENT_FULLNAME, student_full_name);
        cv.put(STUDENT_SECTION, student_section);

        long result = db.insert(STUDENT_TABLE, null, cv);

        return result != -1; // true = success, false = failed
    }

    public boolean addStudentFromLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 3) {
//            Toast.makeText(context, "Invalid format", Toast.LENGTH_SHORT).show();
            return false;
        }

        String number = parts[0].trim();
        String name = parts[1].trim();
        String section = parts[2].trim();

        addStudents(number, name, section);
        return true;
    }

    public void addMultipleStudents(String inputText) {
        String[] lines = inputText.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            addStudentFromLine(line);
        }
    }

    // =======================
    // SUBJECT METHODS
    // =======================

    public boolean addSubject(String subject_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SUBJECT_NAME, subject_name);

        long result = db.insert(SUBJECT_TABLE, null, cv);
        return result != -1;
    }

    public Cursor readAllSubject() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + SUBJECT_TABLE, null);
    }

    public int getSubjectIdByName(String subjectName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + SUBJECT_ID + " FROM " + SUBJECT_TABLE + " WHERE " + SUBJECT_NAME + "=?",
                new String[]{subjectName});
        int id = -1;
        if (cursor.moveToFirst()) id = cursor.getInt(0);
        cursor.close();
        return id;
    }

    // ATTENDANCE METHODS

    private int getStudentIdByNumber(String studentNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + STUDENT_ID + " FROM " + STUDENT_TABLE + " WHERE " + STUDENT_NUMBER + "=?",
                new String[]{studentNumber});
        int id = -1;
        if (cursor.moveToFirst()) id = cursor.getInt(0);
        cursor.close();
        return id;
    }

    private boolean alreadyScanned(int studentId, int subjectId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + ATTENDACE_TABLE +
                        " WHERE " + STUDENT_ID + "=? AND " +
                        SUBJECT_ID + "=? AND " +
                        DATE + "=?",
                new String[]{String.valueOf(studentId), String.valueOf(subjectId), date});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf =
                new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Manila")); //
        return sdf.format(new Date());
    }

    // Result enum
    public enum ScanResult { SUCCESS, ALREADY_SCANNED, STUDENT_NOT_FOUND, FAILED }

    public ScanResult scanAttendance(String studentNumber, int subjectId) {
        int studentId = getStudentIdByNumber(studentNumber);
        if (studentId == -1) return ScanResult.STUDENT_NOT_FOUND;

        String today = getCurrentDate();
        if (alreadyScanned(studentId, subjectId, today)) return ScanResult.ALREADY_SCANNED;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(STUDENT_ID, studentId);
        cv.put(SUBJECT_ID, subjectId);
        cv.put(DATE, today);
        cv.put(TIME, getCurrentTime());
        cv.put(STATUS, "Present");

        long result = db.insert(ATTENDACE_TABLE, null, cv);
        if (result == -1) return ScanResult.FAILED;

        return ScanResult.SUCCESS;
    }


    // GET STUDENTS WITH STATUS

    public ArrayList<Student> getStudentsWithStatus(int subjectId) {
        ArrayList<Student> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getCurrentDate();

        String query = "SELECT s." + STUDENT_ID + ", s." + STUDENT_FULLNAME + ", s." + STUDENT_NUMBER + ", " +
                "CASE WHEN a." + STATUS + "='Present' THEN 'Present' ELSE 'Absent' END AS status, " +
                "a." + TIME +
                " FROM " + STUDENT_TABLE + " s " +
                "LEFT JOIN " + ATTENDACE_TABLE + " a ON s." + STUDENT_ID + "=a." + STUDENT_ID +
                " AND a." + SUBJECT_ID + "=? AND a." + DATE + "=?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(subjectId), today});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String studentNumber = cursor.getString(2);
                String status = cursor.getString(3);
                String time = cursor.isNull(4) ? "" : cursor.getString(4); // may be null if Absent

                list.add(new Student(id, name, studentNumber, status, time));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Student getStudentByNumber(String studentNumber) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + STUDENT_TABLE + " WHERE " + STUDENT_NUMBER + "=?",
                new String[]{studentNumber}
        );

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(STUDENT_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(STUDENT_FULLNAME));
            String number = cursor.getString(cursor.getColumnIndexOrThrow(STUDENT_NUMBER));

            cursor.close();
            return new Student(id, name, number, "Absent", "");
        }

        cursor.close();
        return null;
    }

    public ArrayList<Student> getPresentStudents(int subjectId) {
        ArrayList<Student> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getCurrentDate();

        String query = "SELECT s." + STUDENT_ID + ", s." + STUDENT_FULLNAME + ", s." + STUDENT_NUMBER +
                ", a." + STATUS + ", a." + TIME +
                " FROM " + ATTENDACE_TABLE + " a " +
                " INNER JOIN " + STUDENT_TABLE + " s ON s." + STUDENT_ID + " = a." + STUDENT_ID +
                " WHERE a." + SUBJECT_ID + " = ? " +
                " AND a." + DATE + " = ? " +
                " AND a." + STATUS + " = 'Present'";

        Cursor cursor = db.rawQuery(query,
                new String[]{String.valueOf(subjectId), today});

        if (cursor.moveToFirst()) {
            do {
                list.add(new Student(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        "Present",
                        cursor.getString(4) // time
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }


    public void enrollStudentToSubject(int studentId, int subjectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(STUDENT_ID, studentId);
        cv.put(SUBJECT_ID, subjectId);

        db.insertWithOnConflict(
                STUDENT_SUBJECT_TABLE,
                null,
                cv,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    public boolean isStudentInSubject(int studentId, int subjectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + STUDENT_SUBJECT_TABLE +
                        " WHERE " + STUDENT_ID + "=? AND " + SUBJECT_ID + "=?",
                new String[]{String.valueOf(studentId), String.valueOf(subjectId)}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean deleteSubject(int subjectId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                SUBJECT_TABLE,
                SUBJECT_ID + "=?",
                new String[]{String.valueOf(subjectId)}
        );

        return result > 0;
    }

    public boolean updateSubject(int subjectId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SUBJECT_NAME, newName);

        int result = db.update(
                SUBJECT_TABLE,
                cv,
                SUBJECT_ID + "=?",
                new String[]{String.valueOf(subjectId)}
        );

        return result > 0;
    }

    public boolean deleteAttendance(int studentId, int subjectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getCurrentDate();

        int rows = db.delete(ATTENDACE_TABLE,
                STUDENT_ID + "=? AND " + SUBJECT_ID + "=? AND " + DATE + "=?",
                new String[]{String.valueOf(studentId), String.valueOf(subjectId), today});

        return rows > 0;
    }

    public boolean exportAttendanceToCSV(int subjectId, OutputStream os) {
        Cursor cursor = null;
        try {
            cursor = getAttendanceCursor(subjectId);
            if (cursor.getCount() == 0) {
                return false; // No data to export
            }

            OutputStreamWriter writer = new OutputStreamWriter(os);
            writer.write("Student No,Name,Time\n");

            while (cursor.moveToNext()) {
                writer.write(
                        cursor.getString(0) + "," +
                                cursor.getString(1) + "," +
                                cursor.getString(2) + "\n"
                );
            }

            writer.flush();
            writer.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }



    public Cursor getAttendanceCursor(int subjectId) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT s." + STUDENT_NUMBER + ", s." + STUDENT_FULLNAME + ", a." + TIME +
                        " FROM " + ATTENDACE_TABLE + " a" +
                        " JOIN " + STUDENT_TABLE + " s ON a." + STUDENT_ID + " = s." + STUDENT_ID +
                        " WHERE a." + SUBJECT_ID + " = ?" +
                        " ORDER BY a." + TIME + " ASC",
                new String[]{String.valueOf(subjectId)}
        );
    }

    public int getAttendanceCountForToday(int subjectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ATTENDACE_TABLE +
                        " WHERE " + SUBJECT_ID + "=? AND " + DATE + "=?",
                new String[]{String.valueOf(subjectId), today}
        );

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

}
