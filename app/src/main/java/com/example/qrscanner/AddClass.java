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

public class AddClass extends AppCompatActivity {

    EditText add_class_input;
    Button accept_btn, cancel_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_class);

        accept_btn = findViewById(R.id.accept_btn);
        cancel_btn = findViewById(R.id.cancel_btn);
        add_class_input = findViewById(R.id.subject_name);

        accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper db =  new DatabaseHelper(AddClass.this);
                db.addSubject(add_class_input.getText().toString().trim());
            }

        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // close activity
            }
        });


    }
}