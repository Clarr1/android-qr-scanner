package com.example.qrscanner;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btn_scan;
    DatabaseHelper db;
    RecyclerView recyclerView;
    ArrayList<String> subject_name;
    CustomAdapter customAdapter;

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
                startActivity(intent);
            }
        });

        displaySubject();

        customAdapter = new CustomAdapter(MainActivity.this, subject_name);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }

    void displaySubject() {
        Cursor cursor = db.readAllSubject();
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No subject", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                subject_name.add(cursor.getString(1));
            }
        }
    }

}   // ← END OF CLASS
