package com.example.qrscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<String> subject_name;

    CustomAdapter(Context context, ArrayList<String> subject_name){
        this.context = context;
        this.subject_name = subject_name;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.subject_display, parent, false); // <-- should be a row layout
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.subject_name.setText(subject_name.get(position));
        holder.mainLayout.setOnClickListener(new
                View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, AddStudents.class);
                        intent.putExtra("subject_name", subject_name.get(position));
                        context.startActivity(intent);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return subject_name.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView subject_name;
        LinearLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            subject_name = itemView.findViewById(R.id.subject_name);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
