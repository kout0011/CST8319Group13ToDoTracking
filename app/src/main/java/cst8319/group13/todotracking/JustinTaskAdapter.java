package cst8319.group13.todotracking;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class JustinTaskAdapter extends RecyclerView.Adapter<JustinTaskAdapter.TaskViewHolder> {

    private List<JustinTask> justinTaskList;
    private JustinOnItemClickListener JustinOnItemClickListener;

    public JustinTaskAdapter(List<JustinTask> justinTasks, JustinOnItemClickListener listener) {
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
}
