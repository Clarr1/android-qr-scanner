
package com.example.qrscanner;import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddStudents extends AppCompatActivity {

    String subjectName;

    DatabaseHelper db;
    RecyclerView recyclerView;

    StudentAdapter adapter;
    ArrayList<Student> presentStudents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_students);

        subjectName = getIntent().getStringExtra("subject_name");

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.studentRecyclerView);

        FloatingActionButton scanBtn = findViewById(R.id.scan_id);
        scanBtn.setOnClickListener(v -> scanCode());

        db = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.studentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        presentStudents = new ArrayList<>();
        adapter = new StudentAdapter(presentStudents, position -> {
            Student student = presentStudents.get(position);

            View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_attendance, null);

            TextView title = dialogView.findViewById(R.id.delete_title);
            TextView message = dialogView.findViewById(R.id.delete_message);
            MaterialButton cancelBtn = dialogView.findViewById(R.id.btn_cancel);
            MaterialButton deleteBtn = dialogView.findViewById(R.id.btn_delete);

            title.setText("Delete Attendance");
            message.setText("Remove " + student.getName() + " from today's attendance?");

            AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setView(dialogView)
                    .setCancelable(true)
                    .create();

            cancelBtn.setOnClickListener(v -> dialog.dismiss());

            deleteBtn.setOnClickListener(v -> {
                int subjectId = db.getSubjectIdByName(subjectName);
                if (subjectId != -1 && db.deleteAttendance(student.getId(), subjectId)) {
                    presentStudents.remove(position);
                    adapter.notifyItemRemoved(position);
                    showCustomToast("Attendance deleted");
                }
                dialog.dismiss();
            });

            dialog.show();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setAdapter(adapter);

        //  LOAD SAVED ATTENDANCE FROM DATABASE
        loadAttendance();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_students_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAttendance();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.add_students_menu) {
            showFormPopup(subjectName);
            return true;
        }

        if (item.getItemId() == R.id.export_attendance) {
            int subjectId = db.getSubjectIdByName(subjectName);
            if (subjectId == -1) {
               showCustomToast("Subject not found");
                return true;
            }

            exportAttendanceToDownloads(subjectId);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void showFormPopup(String subjectName) {

        View popupView = getLayoutInflater().inflate(R.layout.form_add_students, null);
        View rootView = findViewById(android.R.id.content);

        // BLUR BACKGROUND
        View dimView = new View(this);
        dimView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        dimView.setBackgroundColor(0x99000000); // dim effect
        ((ViewGroup) rootView).addView(dimView);

        // POPUP
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(24f);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);

        popupWindow.setOnDismissListener(() ->
                ((ViewGroup) rootView).removeView(dimView)
        );

        // POPUP ELEMENTS
        TextView subjectTitle = popupView.findViewById(R.id.subject_title);
        EditText studentBox = popupView.findViewById(R.id.add_student_text_box);
        Button addBtn = popupView.findViewById(R.id.add_btn);
        Button cancelBtn = popupView.findViewById(R.id.cancel_btn);

        subjectTitle.setText(subjectName);

        cancelBtn.setOnClickListener(v -> popupWindow.dismiss());

        addBtn.setOnClickListener(v -> {

            int subjectId = db.getSubjectIdByName(subjectName);
            if (subjectId == -1) {
                showCustomToast("Subject not found");
                return;
            }

            String input = studentBox.getText().toString().trim();
            if (input.isEmpty()) return;

            String[] lines = input.split("\n");

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                // Format: studentNumber, fullName, section
                db.addStudentFromLine(line);

                String studentNumber = line.split(",")[0].trim();
                Student student = db.getStudentByNumber(studentNumber);

                if (student != null) {
                    db.enrollStudentToSubject(student.getId(), subjectId);
                }
            }

         showCustomToast("Student added and enrolled");
            popupWindow.dismiss();
        });

    }


//scan

    private void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher =
            registerForActivityResult(new ScanContract(), result -> {

                if (result.getContents() == null) return;

                String studentNumber = result.getContents().trim();

                int subjectId = db.getSubjectIdByName(subjectName);
                if (subjectId == -1) {
                   showCustomToast("Subject not found");
                    return;
                }

                //  GET STUDENT
                Student student = db.getStudentByNumber(studentNumber);

                //  NULL CHECK (CRITICAL)
                if (student == null) {
                    showCustomToast("Student not registered");
                    return;
                }

                int studentId = student.getId();

                //  SUBJECT ENROLLMENT CHECK
                if (!db.isStudentInSubject(studentId, subjectId)) {
                    showCustomToast("Student is not enrolled in this subject");
                    return;
                }
                View rootView = findViewById(android.R.id.content);
                View popupView = getLayoutInflater().inflate(R.layout.popup_student_found, null);

// Dim background
                View dimView = new View(this);
                dimView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                dimView.setBackgroundColor(0x99000000);
                ((ViewGroup) rootView).addView(dimView);

// Popup
                PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        true
                );
                popupWindow.setElevation(24f);
                popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);

                popupWindow.setOnDismissListener(() ->
                        ((ViewGroup) rootView).removeView(dimView)
                );

// Views
                TextView info = popupView.findViewById(R.id.student_info);
                MaterialButton btnCancel = popupView.findViewById(R.id.btn_cancel);
                MaterialButton btnMarkPresent = popupView.findViewById(R.id.btn_mark_present);

// Set content
                info.setText(
                        "Name: " + student.getName() + "\n" +
                                "Student No: " + student.getStudentNumber()
                );

// Actions
                btnCancel.setOnClickListener(v -> popupWindow.dismiss());

                btnMarkPresent.setOnClickListener(v -> {

                    DatabaseHelper.ScanResult scanResult =
                            db.scanAttendance(student.getStudentNumber(), subjectId);

                    switch (scanResult) {
                        case SUCCESS:
                            // Get the updated student with time from DB
                            Student updatedStudent = db.getStudentByNumber(student.getStudentNumber());

                            // For attendance today, get the time from attendance table
                            ArrayList<Student> tempList = db.getPresentStudents(subjectId);
                            for (Student s : tempList) {
                                if (s.getId() == student.getId()) {
                                    updatedStudent = s;
                                    break;
                                }
                            }

                            presentStudents.add(updatedStudent);
                            adapter.notifyItemInserted(presentStudents.size() - 1);
                            showCustomToast("Attendance marked");
                            break;

                        case ALREADY_SCANNED:
                            showCustomToast("Already scanned today");
                            break;

                        default:
                            showCustomToast("Failed to mark attendance");
                    }

                    popupWindow.dismiss();
                });

            });

    private void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.toastText);
        text.setText(message);

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM, 0, 150);
        toast.show();
    }

    private void loadAttendance() {
        int subjectId = db.getSubjectIdByName(subjectName);

        if (subjectId == -1) return;

        presentStudents.clear();
        presentStudents.addAll(db.getPresentStudents(subjectId));
        adapter.notifyDataSetChanged();
    }

    private void exportAttendanceToDownloads(int subjectId) {

        String fileName = "Attendance_" + subjectName + "_" +
                new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".csv";

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = getContentResolver().insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
            );
        } else {
            uri = getContentResolver().insert(
                    MediaStore.Files.getContentUri("external"),
                    values
            );
        }

        if (uri == null) {
            showCustomToast("Failed to create file");
            return;
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {

            boolean success = db.exportAttendanceToCSV(subjectId, outputStream);

            if (success) {
                showCustomToast("Saved to downloads");

                // Clear today's attendance
                SQLiteDatabase dbWrite = db.getWritableDatabase();
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                dbWrite.delete(
                        "attendance_table",
                        "subject_id=? AND attendance_date=?",
                        new String[]{String.valueOf(subjectId), today}
                );

                // Optionally update your RecyclerView
                presentStudents.clear();
                adapter.notifyDataSetChanged();

                // Open CSV
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "text/csv");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Open CSV with"));

    } else {
               showCustomToast("Export failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
           showCustomToast("Error writing file");
        }
    }


}