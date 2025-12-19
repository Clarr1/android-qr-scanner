package com.example.qrscanner;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

//import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btn_scan;
    DatabaseHelper db;
    RecyclerView recyclerView;
    ArrayList<String> subject_name;
    CustomAdapter customAdapter;

    private ActivityResultLauncher<Intent> addClassLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                            String newSubject = result.getData().getStringExtra("newSubject");

                            if (newSubject != null) {
                                subject_name.add(newSubject);
                                customAdapter.notifyItemInserted(subject_name.size() - 1);
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
//        btn_scan = findViewById(R.id.btn_scan);
        FloatingActionButton add_class_btn = findViewById(R.id.add_class_btn);

        db = new DatabaseHelper(this);

        subject_name = new ArrayList<>();

        add_class_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddClass.class);
                addClassLauncher.launch(intent);
            }
        });

        displaySubject();

        customAdapter = new CustomAdapter(
                MainActivity.this,
                subject_name,
                new CustomAdapter.OnSubjectActionListener() {
                    @Override
                    public void onEdit(String subject) {
                        editSubject(subject);
                    }

                    @Override
                    public void onDelete(String subject) {
                        deleteSubject(subject);
                    }
                }
        );
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }   // ← END OF onCreate()

    private void editSubject(String oldSubjectName) {

        int subjectId = db.getSubjectIdByName(oldSubjectName);
        if (subjectId == -1) {
           showCustomToast("Subject not found");
            return;
        }

        // Inflate custom dialog layout
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_subject, null);

        TextView title = view.findViewById(R.id.subject_title);
        TextInputEditText subjectInput = view.findViewById(R.id.subject_name);
        MaterialButton cancelBtn = view.findViewById(R.id.cancel_btn);
        MaterialButton acceptBtn = view.findViewById(R.id.accept_btn);

        title.setText("Edit Subject");
        subjectInput.setText(oldSubjectName);
        subjectInput.setSelection(oldSubjectName.length());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        // Transparent background (important for rounded card)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        acceptBtn.setOnClickListener(v -> {
            String newName = subjectInput.getText().toString().trim();

            if (newName.isEmpty()) {
                subjectInput.setError("Subject name required");
                return;
            }

            boolean updated = db.updateSubject(subjectId, newName);

            if (updated) {
                showCustomToast("Subject updated");
                displaySubject();
                customAdapter.notifyDataSetChanged();
                dialog.dismiss();
            } else {
                showCustomToast("Update failed");
            }
        });

        dialog.show();
    }


    private void deleteSubject(String subjectName) {

        int subjectId = db.getSubjectIdByName(subjectName);
        if (subjectId == -1) {
            showCustomToast("Subject not found");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_subject, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);
        MaterialButton btnDelete = view.findViewById(R.id.btn_delete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            boolean deleted = db.deleteSubject(subjectId);

            if (deleted) {
                showCustomToast("Subject deleted");
                displaySubject();
                customAdapter.notifyDataSetChanged();
            } else {
                showCustomToast("Delete failed");
            }
            dialog.dismiss();
        });

    }

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


    void displaySubject() {
        subject_name.clear();

        Cursor cursor = db.readAllSubject();
        if (cursor.getCount() == 0) {
            showCustomToast("No classes");
        } else {
            while (cursor.moveToNext()) {
                subject_name.add(cursor.getString(1));
            }
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        displaySubject();
        customAdapter.notifyDataSetChanged();
    }

}
