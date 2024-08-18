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
    private TaskAdapter taskAdapter;
    private Button newButton, editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home_page);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        justinTaskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(justinTaskList, this);
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

    // Picking background color for list items
    private int getColorForTask(JustinTask justinTask) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date expirationDate = null;
        try {
            expirationDate = dateFormat.parse(justinTask.DueDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (expirationDate == null) {
            return Color.GRAY;
        }

        Date currentDate = new Date();
        if (currentDate.after(expirationDate)) {
            return Color.parseColor("#ff9591"); //Expired
        } else if (justinTask.checked) {
            return Color.parseColor("#bfbfbf"); //Checked
        } else {
            return Color.parseColor("#808080"); //Default
        }
    }

    // Task Adapter
    private class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

        private List<JustinTask> justinTaskList;
        private JustinOnItemClickListener JustinOnItemClickListener;

        public TaskAdapter(List<JustinTask> justinTasks, JustinOnItemClickListener listener) {
            this.justinTaskList = justinTasks;
            this.JustinOnItemClickListener = listener;
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            JustinTask justinTask = justinTaskList.get(position);
            holder.textViewTask.setBackgroundColor(getColorForTask(justinTask));
            String text = justinTask.TaskName;
            if (getColorForTask(justinTask) == Color.parseColor("#ff9591")) {
                text = text + " - Expired ( ! )";
            }
            holder.textViewTask.setText(text);
            holder.checkBoxTask.setChecked(justinTask.checked);

            holder.itemView.setOnClickListener(v -> {
                if (JustinOnItemClickListener != null) {
                    JustinOnItemClickListener.onItemClick(position);
                }
            });

            holder.checkBoxTask.setOnClickListener(v -> {
                if (JustinOnItemClickListener != null) {
                    JustinOnItemClickListener.onCheckBoxClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return justinTaskList.size();
        }

        public class TaskViewHolder extends RecyclerView.ViewHolder {
            TextView textViewTask;
            CheckBox checkBoxTask;

            public TaskViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewTask = itemView.findViewById(R.id.textViewTask);
                checkBoxTask = itemView.findViewById(R.id.checkBoxTask);
            }
        }
    }
}
