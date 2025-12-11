package com.example.qrscanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        db = new DatabaseHelper(this); // initialize database

        btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(view -> scanCode());

        // add class (for testing)
        FloatingActionButton add_class_btn= findViewById(R.id.add_class_btn);

//        subject_name = findViewById(R.id.subject_name);

        add_class_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddClass.class);
                startActivity(intent);
            }
        });

        db = new DatabaseHelper(MainActivity.this);

        subject_name = new ArrayList<>();

        displaySubject();

        customAdapter = new CustomAdapter(MainActivity.this, this.subject_name);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }

    private void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {

        if (result.getContents() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("Ok", (dialogInterface, i) -> {

                boolean ok = db.insertScan(result.getContents());

                if (ok)
                    Toast.makeText(MainActivity.this, "Saved to database!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, "Save failed!", Toast.LENGTH_SHORT).show();

                dialogInterface.dismiss();
            });
            builder.show();
        }

    });

    void displaySubject(){
        Cursor cursor = db.readAllSubject();
        if(cursor.getCount() == 0){
            Toast.makeText(this, "No subject", Toast.LENGTH_SHORT).show();
        }else{
            while (cursor.moveToNext()){
                subject_name.add(cursor.getString(1));
            }
        }
    }

}
