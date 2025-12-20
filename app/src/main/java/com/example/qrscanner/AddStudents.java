
package com.example.qrscanner;import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
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

    ImageView empty_attendance;
    TextView no_attendance;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_students);

        subjectName = getIntent().getStringExtra("subject_name");

        empty_attendance = findViewById(R.id.empty_attendance);
        no_attendance = findViewById(R.id.no_attendance);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.outline_arrow_back_24);

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
                    updateEmptyState();
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

        if (item.getItemId() == android.R.id.home) {
            // Go back to previous activity
            onBackPressed();
            return true;
        }

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

        // BLUR / DIM BACKGROUND
        View dimView = new View(this);
        dimView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        dimView.setBackgroundColor(0x99000000); // dim effect
        ((ViewGroup) rootView).addView(dimView);

        // WRAP POPUP IN SCROLLVIEW TO HANDLE KEYBOARD
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.addView(popupView);

        // POPUPWINDOW
        PopupWindow popupWindow = new PopupWindow(
                scrollView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // KEYBOARD ADJUSTMENT
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // ELEVATION & SHOW
        popupWindow.setElevation(24f);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);

        popupWindow.setOnDismissListener(() -> ((ViewGroup) rootView).removeView(dimView));

        // POPUP ELEMENTS
        TextView subjectTitle = popupView.findViewById(R.id.subject_title);
        EditText studentBox = popupView.findViewById(R.id.add_student_text_box);

        subjectTitle.setText(subjectName);

//         Example placeholder text
        String exampleText = "243453521,John Doe,BSIT3O\n" +
                "23151100,Juan Gomez,BSIT3A\n" +
                "23151101,Ana Santos,BSIT3B";

        studentBox.setText(exampleText);
        studentBox.setTextColor(Color.GRAY);

        studentBox.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && studentBox.getCurrentTextColor() == Color.GRAY) {
                studentBox.setText("");
                studentBox.setTextColor(Color.BLACK);
            }
        });

        // BUTTONS
        Button addBtn = popupView.findViewById(R.id.add_btn);
        Button cancelBtn = popupView.findViewById(R.id.cancel_btn);

        cancelBtn.setOnClickListener(v -> popupWindow.dismiss());

        addBtn.setOnClickListener(v -> {
            int subjectId = db.getSubjectIdByName(subjectName);
            if (subjectId == -1) {
                showCustomToast("Subject not found");
                return;
            }

            String input = studentBox.getText().toString().trim();
            if (input.isEmpty() || studentBox.getCurrentTextColor() == Color.GRAY) {
                showCustomToast("Please enter student data");
                return;
            }

            String[] lines = input.split("\n");

            boolean allSuccess = true;

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                boolean success = db.addStudentFromLine(line);
                if (!success) {
                    allSuccess = false;
                    continue; // skip enrollment for invalid line
                }

                String studentNumber = line.split(",")[0].trim();
                Student student = db.getStudentByNumber(studentNumber);

                if (student != null) {
                    db.enrollStudentToSubject(student.getId(), subjectId);
                }
            }

// Show toast after processing all lines
            if (allSuccess) {
                showCustomToast("All students added and enrolled");
            } else {
                showCustomToast("Some lines were invalid");
            }

            popupWindow.dismiss();

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

                Student student = db.getStudentByNumber(studentNumber);
                if (student == null) {
                    showCustomToast("Student not registered");
                    return;
                }

                if (!db.isStudentInSubject(student.getId(), subjectId)) {
                    showCustomToast("Student is not enrolled in this subject");
                    return;
                }

                // ROOT VIEW & DIM BACKGROUND
                View rootView = findViewById(android.R.id.content);
                View popupView = getLayoutInflater().inflate(R.layout.popup_student_found, null);
                View dimView = new View(this);
                dimView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                dimView.setBackgroundColor(0x99000000);
                ((ViewGroup) rootView).addView(dimView);

                // POPUP WINDOW
                PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        true
                );
                popupWindow.setElevation(24f);
                popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
                popupWindow.setOnDismissListener(() -> ((ViewGroup) rootView).removeView(dimView));

                // POPUP ELEMENTS
                TextView info = popupView.findViewById(R.id.student_info);
                MaterialButton btnCancel = popupView.findViewById(R.id.btn_cancel);
                MaterialButton btnMarkPresent = popupView.findViewById(R.id.btn_mark_present);

                info.setText("Name: " + student.getName() + "\n" +
                        "Student No: " + student.getStudentNumber());

                btnCancel.setOnClickListener(v -> popupWindow.dismiss());

                btnMarkPresent.setOnClickListener(v -> {

                    // MARK ATTENDANCE
                    DatabaseHelper.ScanResult scanResult = db.scanAttendance(student.getStudentNumber(), subjectId);

                    switch (scanResult) {
                        case SUCCESS:
                            // Fetch updated student
                            Student updatedStudent = db.getStudentByNumber(student.getStudentNumber());

                            // Update RecyclerView immediately
                            int index = -1;
                            for (int i = 0; i < presentStudents.size(); i++) {
                                if (presentStudents.get(i).getStudentNumber()
                                        .equals(updatedStudent.getStudentNumber())) {
                                    index = i;
                                    break;
                                }
                            }

                            if (index != -1) {
                                presentStudents.set(index, updatedStudent);
                                adapter.notifyItemChanged(index);
                            } else {
                                presentStudents.add(updatedStudent);
                                adapter.notifyItemInserted(presentStudents.size() - 1);
                            }

                            updateEmptyState();
                            showCustomToast("Attendance marked");

                            // Optional: reload from DB to ensure full sync (includes any other changes)
                            loadAttendance();
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
        new Thread(() -> {
            int subjectId = db.getSubjectIdByName(subjectName);

            ArrayList<Student> tempList = new ArrayList<>();
            if (subjectId != -1) {
                tempList.addAll(db.getPresentStudents(subjectId));
            }

            runOnUiThread(() -> {
                presentStudents.clear();
                presentStudents.addAll(tempList);
                adapter.notifyDataSetChanged();

                boolean isEmpty = tempList.isEmpty() || subjectId == -1;
                if (empty_attendance != null) empty_attendance.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                if (no_attendance != null) no_attendance.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            });
        }).start();
    }



    private void exportAttendanceToDownloads(int subjectId) {
        // Check if there is attendance today
        int count = db.getAttendanceCountForToday(subjectId);
        if (count == 0) {
            showCustomToast("No attendance records to export");
            return;
        }

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

                // Update RecyclerView
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


    private void updateEmptyState() {
        boolean isEmpty = presentStudents.isEmpty();
        if (empty_attendance != null) empty_attendance.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (no_attendance != null) no_attendance.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }



}