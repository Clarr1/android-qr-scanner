package com.example.qrscanner;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddStudents extends AppCompatActivity {
    EditText add_student_text_box;
    Button add_btn, cancel_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_students);


    add_btn = findViewById(R.id.add_btn);
    cancel_btn = findViewById(R.id.cancel_btn);
    add_student_text_box = findViewById(R.id.add_student_text_box);

    add_btn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        DatabaseHelper myDB = new DatabaseHelper(AddStudents.this);
            String text = add_student_text_box.getText().toString();
            myDB.addMultipleStudents(text);
        }
    });

    }
}