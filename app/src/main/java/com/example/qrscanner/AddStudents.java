package com.example.qrscanner;

import android.os.Bundle;
import android.view.Gravity;
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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class AddStudents extends AppCompatActivity {

    String subjectName;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_students);

        subjectName = getIntent().getStringExtra("subject_name");

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        FloatingActionButton scanBtn = findViewById(R.id.scan_id);
        scanBtn.setOnClickListener(v -> scanCode());

        db = new DatabaseHelper(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_students_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.scan_id) {
            scanCode();
            return true;
        }

        if (item.getItemId() == R.id.add_students_menu) {
            showFormPopup(subjectName);
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
            db.addMultipleStudents(studentBox.getText().toString());
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

                if (result.getContents() != null) {

                    String studentNumber = result.getContents().trim();
                    int subjectId = db.getSubjectIdByName(subjectName);

                    if (subjectId == -1) {
                        Toast.makeText(this, "Subject not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Scan Result");
                    builder.setMessage("Student Number: " + studentNumber);

                    builder.setPositiveButton("Mark Present", (dialog, which) -> {
                        db.scanAttendance(studentNumber, subjectId);
                        dialog.dismiss();
                    });

                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                    builder.show();
                }
            });
}
