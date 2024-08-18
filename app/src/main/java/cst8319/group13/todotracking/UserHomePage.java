package cst8319.group13.todotracking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
}
