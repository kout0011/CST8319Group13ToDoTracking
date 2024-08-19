package cst8319.group13.todotracking;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;

import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserHomePage extends AppCompatActivity implements JustinOnItemClickListener {

    private RecyclerView recyclerView;
    private List<JustinTask> justinTaskList;
    private JustinTaskAdapter taskAdapter;
    private Button newButton, editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home_page);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        justinTaskList = new ArrayList<>();
        taskAdapter = new JustinTaskAdapter(justinTaskList, this);
        recyclerView.setAdapter(taskAdapter);

        newButton = findViewById(R.id.buttonAdd);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserHomePage.this, AddTaskPage.class);
                startActivity(intent);
            }
        });
      
        // Get the username from the Intent
        String username = getIntent().getStringExtra("USERNAME");

        // Update the textViewTitle with the username
        TextView textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText(username + "'s TO-DO");

        // Find the Logout button by its ID
        Button buttonLogout = findViewById(R.id.buttonLogout);

        // Set an OnClickListener on the Logout button
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a confirmation dialog before logging out
                new AlertDialog.Builder(UserHomePage.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() { // Change "OK" to "Yes"
                            public void onClick(DialogInterface dialog, int which) {
                                // Clear user data from SharedPreferences
                                clearUserData();

                                // Logout the user and navigate back to MainActivity
                                Intent intent = new Intent(UserHomePage.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish(); // Close the UserHomePage so that it is not in the back stack
                            }
                        })
                        .setNegativeButton("No", null) // Change "Cancel" to "No"
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
      
        fetchDataFromFirebase();
    }

    private void updateTaskInFirebase(JustinTask updatedJustinTask) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Task");
        databaseReference.child(String.valueOf(updatedJustinTask.TaskId)).setValue(updatedJustinTask)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(UserHomePage.this, "Task updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserHomePage.this, "Failed to update task", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserHomePage.this, "Failed to update task", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchDataFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Task");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                justinTaskList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    JustinTask justinTask = snapshot.getValue(JustinTask.class);

                    if (justinTask != null) {
                        justinTaskList.add(justinTask);
                    }
                }
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("UserHomePage", "Failed to read data", databaseError.toException());
                Toast.makeText(UserHomePage.this, "Failed to read data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onItemClick(int position) {
        JustinTask clickedJustinTask = justinTaskList.get(position);
        Bundle bundle = new Bundle();
        bundle.putParcelable("task", clickedJustinTask);

        Intent intent = new Intent(UserHomePage.this, TaskEditPage.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onCheckBoxClick(int position) {
        JustinTask clickedJustinTask = justinTaskList.get(position);
        clickedJustinTask.checked = !clickedJustinTask.checked;

        updateTaskInFirebase(clickedJustinTask);

        justinTaskList.set(position, clickedJustinTask);
        taskAdapter.notifyItemChanged(position);

    }
  

    private void clearUserData() {
        // Clear the user data from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}
