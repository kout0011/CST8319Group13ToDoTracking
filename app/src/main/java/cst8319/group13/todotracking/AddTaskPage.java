package cst8319.group13.todotracking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;

public class AddTaskPage extends AppCompatActivity {

    private EditText editTextTaskName, editTextNote;
    private Button buttonAddTask, buttonCancel, buttonAddDueDate, buttonRemindMe;
    private DatabaseReference databaseReference;
    private TextView dueDateTextView;
    private String selectedDueDate = "";
    private boolean remindme;

    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task_page);

        databaseReference = FirebaseDatabase.getInstance().getReference("Task");

        id = getIntent().getIntExtra("id", -1);

        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextNote = findViewById(R.id.editTextNote);
        buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonAddDueDate = findViewById(R.id.buttonAddDueDate);
        buttonRemindMe = findViewById(R.id.buttonRemindMe);

        dueDateTextView = findViewById(R.id.buttonAddDueDate);

        buttonAddDueDate.setOnClickListener(view -> showDatePickerDialog());

        buttonAddTask.setOnClickListener(view -> saveTaskToFirebase());

        buttonCancel.setOnClickListener(view -> {
            Intent intent = new Intent(AddTaskPage.this, UserHomePage.class);
            startActivity(intent);
        });

        buttonRemindMe.setOnClickListener(view -> {
            remindme = !remindme;
            String un = "Un-";
            if (remindme){
                un = "";
            }
            Toast.makeText(AddTaskPage.this, "Reminder " + un + "Set", Toast.LENGTH_SHORT).show();
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format and set the selected date
                    selectedDueDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    dueDateTextView.setText(selectedDueDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void saveTaskToFirebase() {
        String taskName = editTextTaskName.getText().toString().trim();
        String note = editTextNote.getText().toString().trim();

        JustinTask justinTask = new JustinTask(selectedDueDate, note, taskName, id, "1", remindme);

        String taskId = databaseReference.push().getKey();

        if (taskId != null) {
            justinTask.TaskId = taskId;
            databaseReference.child(taskId).setValue(justinTask).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Intent intent = new Intent(AddTaskPage.this, UserHomePage.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(AddTaskPage.this, "Failed to add task", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
