package com.example.qrscanner;

public class Student {
    private int id;
    private String name;
    private String studentNumber;
    private String status;
    private String time;

    public Student(int id, String name, String studentNumber, String status, String time) {
        this.id = id;
        this.name = name;
        this.studentNumber = studentNumber;
        this.status = status;
        this.time = time;
    }

    // getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getStudentNumber() { return studentNumber; }
    public String getStatus() { return status; }
    public String getTime() { return time; }
}

