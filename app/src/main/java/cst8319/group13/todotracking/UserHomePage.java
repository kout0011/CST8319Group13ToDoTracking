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

public class UserHomePage extends AppCompatActivity implements OnItemClickListener {

    private RecyclerView recyclerView;
    private List<Task> taskList;
    private TaskAdapter taskAdapter;
    private Button newButton, editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home_page);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this);
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

    private void updateTaskInFirebase(Task updatedTask) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Task");
        databaseReference.child(String.valueOf(updatedTask.TaskId)).setValue(updatedTask)
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
                taskList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Task task = snapshot.getValue(Task.class);

                    if (task != null) {
                        taskList.add(task);
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
        Task clickedTask = taskList.get(position);
        Bundle bundle = new Bundle();
        bundle.putParcelable("task", clickedTask);

        Intent intent = new Intent(UserHomePage.this, TaskEditPage.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onCheckBoxClick(int position) {
        Task clickedTask = taskList.get(position);
        clickedTask.checked = !clickedTask.checked;

        updateTaskInFirebase(clickedTask);

        taskList.set(position, clickedTask);
        taskAdapter.notifyItemChanged(position);
    }

    // Picking background color for list items
    private int getColorForTask(Task task) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date expirationDate = null;
        try {
            expirationDate = dateFormat.parse(task.DueDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (expirationDate == null) {
            return Color.GRAY;
        }

        Date currentDate = new Date();
        if (currentDate.after(expirationDate)) {
            return Color.parseColor("#ff9591"); //Expired
        } else if (task.checked) {
            return Color.parseColor("#bfbfbf"); //Checked
        } else {
            return Color.parseColor("#808080"); //Default
        }
    }

    // Task Adapter
    private class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

        private List<Task> taskList;
        private OnItemClickListener onItemClickListener;

        public TaskAdapter(List<Task> tasks, OnItemClickListener listener) {
            this.taskList = tasks;
            this.onItemClickListener = listener;
        }

        @NonNull
        @Override
        public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
            return new TaskViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
            Task task = taskList.get(position);
            holder.textViewTask.setBackgroundColor(getColorForTask(task));
            String text = task.TaskName;
            if (getColorForTask(task) == Color.parseColor("#ff9591")) {
                text = text + " - Expired ( ! )";
            }
            holder.textViewTask.setText(text);
            holder.checkBoxTask.setChecked(task.checked);

            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position);
                }
            });

            holder.checkBoxTask.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onCheckBoxClick(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return taskList.size();
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
