

package com.example.qrscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
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
                String newSubject = add_class_input.getText().toString().trim();
                if (!newSubject.isEmpty()) {

                    // Add to database
                    DatabaseHelper db = new DatabaseHelper(AddClass.this);
                    db.addSubject(newSubject);

                    // Return the new subject to the previous activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newSubject", newSubject);
                    setResult(RESULT_OK, resultIntent);

                    finish(); // close AddClass activity
                }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish(); // close activity
            }
        });

    }
}