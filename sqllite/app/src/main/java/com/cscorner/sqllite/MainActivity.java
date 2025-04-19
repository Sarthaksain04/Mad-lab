package com.cscorner.sqllite;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText Rollno, Name, Marks;
    Button Insert, Delete, Update, View, ViewAll;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Rollno = findViewById(R.id.Rollno);
        Name = findViewById(R.id.Name);
        Marks = findViewById(R.id.Marks);
        Insert = findViewById(R.id.Insert);
        Delete = findViewById(R.id.Delete);
        Update = findViewById(R.id.Update);
        View = findViewById(R.id.View);
        ViewAll = findViewById(R.id.ViewAll);

        Insert.setOnClickListener(this);
        Delete.setOnClickListener(this);
        Update.setOnClickListener(this);
        View.setOnClickListener(this);
        ViewAll.setOnClickListener(this);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
    }

    @Override
    public void onClick(View view) {
        String rollNo = Rollno.getText().toString().trim();
        String name = Name.getText().toString().trim();
        String marks = Marks.getText().toString().trim();

        if (view == Insert) {
            if (rollNo.isEmpty() || name.isEmpty() || marks.isEmpty()) {
                showMessage("Error", "Please enter all values");
                return;
            }
            ContentValues values = new ContentValues();
            values.put("rollno", rollNo);
            values.put("name", name);
            values.put("marks", marks);
            db.insert("student", null, values);
            showMessage("Success", "Record added");
            clearText();
        }

        if (view == Delete) {
            if (rollNo.isEmpty()) {
                showMessage("Error", "Please enter Rollno");
                return;
            }
            int deletedRows = db.delete("student", "rollno=?", new String[]{rollNo});
            if (deletedRows > 0) {
                showMessage("Success", "Record Deleted");
            } else {
                showMessage("Error", "Invalid Rollno");
            }
            clearText();
        }

        if (view == Update) {
            if (rollNo.isEmpty()) {
                showMessage("Error", "Please enter Rollno");
                return;
            }
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("marks", marks);
            int updatedRows = db.update("student", values, "rollno=?", new String[]{rollNo});
            if (updatedRows > 0) {
                showMessage("Success", "Record Modified");
            } else {
                showMessage("Error", "Invalid Rollno");
            }
            clearText();
        }

        if (view == View) {
            if (rollNo.isEmpty()) {
                showMessage("Error", "Please enter Rollno");
                return;
            }
            Cursor c = db.rawQuery("SELECT * FROM student WHERE rollno=?", new String[]{rollNo});
            if (c.moveToFirst()) {
                Name.setText(c.getString(1));
                Marks.setText(c.getString(2));
            } else {
                showMessage("Error", "Invalid Rollno");
                clearText();
            }
            c.close();
        }

        if (view == ViewAll) {
            Cursor c = db.rawQuery("SELECT * FROM student", null);
            if (c.getCount() == 0) {
                showMessage("Error", "No records found");
                return;
            }
            StringBuilder buffer = new StringBuilder();
            while (c.moveToNext()) {
                buffer.append("Rollno: ").append(c.getString(0)).append("\n");
                buffer.append("Name: ").append(c.getString(1)).append("\n");
                buffer.append("Marks: ").append(c.getString(2)).append("\n\n");
            }
            showMessage("Student Details", buffer.toString());
            c.close();
        }
    }

    public void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
    }

    public void clearText() {
        Rollno.setText("");
        Name.setText("");
        Marks.setText("");
        Rollno.requestFocus();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "StudentDB";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS student(rollno TEXT PRIMARY KEY, name TEXT, marks TEXT);";

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS student");
            onCreate(db);
 }
    }
}