package cst8319.group13.todotracking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class TaskEditPage extends AppCompatActivity {
    private static final String TAG = "TaskEditPage";

    private EditText editTextTaskName, editTextNote;
    private Button buttonUpdate, buttonDelete, buttonCancel, buttonAddDueDate, buttonRemindMe;
    private TextView textViewTitle, dueDateTextView;
    private String selectedDueDate = "";
    private DatabaseReference databaseReference;
    private boolean remindme;
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit_page);

        databaseReference = FirebaseDatabase.getInstance().getReference("Task");

        // Initialize UI components
        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextNote = findViewById(R.id.editTextNote);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonAddDueDate = findViewById(R.id.buttonAddDueDate);
        buttonRemindMe = findViewById(R.id.buttonRemindMe);
        textViewTitle = findViewById(R.id.textViewTitle);
        dueDateTextView = findViewById(R.id.textViewAddNote);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            task = bundle.getParcelable("task");

            if (task != null) {
                // Set the UI with the task data
                editTextTaskName.setText(task.TaskName);
                editTextNote.setText(task.Notes);
                dueDateTextView.setText(task.DueDate);
                remindme = task.remindme;
                updateRemindMeButton(); // Update button text based on remindme value

                // Set listeners
                buttonUpdate.setOnClickListener(view -> updateTaskToFirebase());
                buttonDelete.setOnClickListener(view -> deleteTaskFromFirebase());
                buttonCancel.setOnClickListener(view -> finish());
                buttonAddDueDate.setOnClickListener(view -> showDatePickerDialog());
                buttonRemindMe.setOnClickListener(view -> remindMe());
            }
        }
    }

    private void updateRemindMeButton() {
        String remindMeText = remindme ? "Un-Set Reminder" : "Set Reminder";
        buttonRemindMe.setText(remindMeText);
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDueDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    dueDateTextView.setText(selectedDueDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void remindMe() {
        remindme = !remindme;

        task.remindme = remindme;
        updateRemindMeButton();
        String un = remindme ? "" : "Un-";
        Toast.makeText(TaskEditPage.this, "Reminder " + un + "Set", Toast.LENGTH_SHORT).show();
    }

    private void updateTaskToFirebase() {
        task.TaskName = editTextTaskName.getText().toString().trim();
        task.Notes = editTextNote.getText().toString().trim();
        task.DueDate = selectedDueDate;
        task.remindme = remindme;

        if (task.TaskId != null) {
            databaseReference.child(task.TaskId).setValue(task).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(TaskEditPage.this, "Task has been updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TaskEditPage.this, UserHomePage.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(TaskEditPage.this, "Failed to update task", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteTaskFromFirebase() {
        if (task.TaskId != null) {
            databaseReference.child(task.TaskId).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(TaskEditPage.this, "Task deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TaskEditPage.this, UserHomePage.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(TaskEditPage.this, "Failed to delete task", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(TaskEditPage.this, "Task ID is missing", Toast.LENGTH_SHORT).show();
        }
    }
}
